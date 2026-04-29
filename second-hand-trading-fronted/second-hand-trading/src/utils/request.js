import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router'; // 导入路由实例

const isDev = process.env.NODE_ENV === "development";

/* axios功能封装  */
const service = axios.create({
    timeout: 60000,
    baseURL: isDev ? "/api" : "https://second-218367-7-1328505920.sh.run.tcloudbase.com",
    withCredentials: true
});

let lastUnauthToastAt = 0;
let lastBanToastAt = 0;

const notifyUnauthOnce = () => {
    const now = Date.now();
    // 避免同一批失败请求导致重复弹窗
    if (now - lastUnauthToastAt < 1200) {
        return;
    }
    lastUnauthToastAt = now;
    ElMessage.warning('未登录，请先登录');
};

const notifyBanOnce = () => {
    const now = Date.now();
    if (now - lastBanToastAt < 1200) {
        return;
    }
    lastBanToastAt = now;
    ElMessage.error('账号已被封禁');
};

// response interceptor（接收拦截器）
service.interceptors.response.use(
    response => {
        if (response.status === 200) {
            // 检查是否是登录过期或未登录的错误
            if (response.data && response.data.status_code === 0 && response.data.msg) {
                const msg = response.data.msg || '';
                // 仅在明确的会话/认证失效场景下触发跳转：cookie 错误、请先登录、未登录或登录已过期
                if (msg.includes('ACCOUNT_Ban') || msg.includes('账号已被封禁')) {
                    notifyBanOnce();
                    if (window.location.hash.includes('/platform-admin')) {
                        router.push('/login-admin');
                    } else {
                        router.push('/login');
                    }
                    const banError = new Error(msg || '账号已被封禁');
                    banError.msg = msg || '账号已被封禁';
                    banError.status_code = response.data.status_code;
                    banError.data = response.data;
                    return Promise.reject(banError);
                }
                if (msg.includes('COOKIE_ERROR') || msg.includes('请先登录') || msg.includes('请重新登录') || msg.includes('未登录') || msg.includes('登录已过期')) {
                    notifyUnauthOnce();
                    if (window.location.hash.includes('/platform-admin')) {
                        router.push('/login-admin');
                    } else {
                        router.push('/login');
                    }
                    const authError = new Error(msg || '未登录');
                    authError.msg = msg || '未登录';
                    authError.status_code = response.data.status_code;
                    authError.data = response.data;
                    return Promise.reject(authError);
                }
                // 其他业务级错误（例如：登录失败、参数错误）应由调用方处理，直接返回 response.data
            }
            return response.data;
        } else {
            return Promise.reject(response);
        }
    },
    error => {
        console.log(error);
        // 检查是否是401或其他认证错误
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            // 根据当前页面决定跳转到哪个登录页面
            if (window.location.hash.includes('/platform-admin')) {
                router.push('/login-admin');
            } else {
                // 其他情况跳转到普通登录
                router.push('/login');
            }
        }
        return Promise.reject(error);
    }
);

export default service;
