<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-body">
        <div class="login-title">
          后台管理系统
        </div>
        <el-form ref="form" :model="userForm" class="login-form" @submit.native.prevent="login">
          <div class="input-group">
            <el-input 
              placeholder="请输入管理员账号" 
              v-model="userForm.accountNumber" 
              class="login-input"
              name="username"
              autocomplete="username">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input 
              placeholder="请输入管理员密码" 
              v-model="userForm.adminPassword" 
              class="login-input"
              @keyup.enter.native="login" 
              show-password
              name="password"
              autocomplete="current-password">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="login-submit">
            <el-button type="primary" class="submit-btn" native-type="submit" :loading="loading">登录</el-button>
          </div>
          <div class="other-submit">
            <router-link to="/login" class="link-text">返回学生登录</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../stores/user';

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const $sta = proxy.$sta;
const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const userForm = reactive({
    accountNumber: '',
    adminPassword: ''
});

const login = () => {
    if (!userForm.accountNumber || !userForm.adminPassword) {
        ElMessage.warning('请输入账号和密码');
        return;
    }
    
    loading.value = true;
    $api.adminLogin({
        accountNumber: userForm.accountNumber,
        adminPassword: userForm.adminPassword
    }).then(res => {
        console.log(res);
        if (res && res.status_code === 1) {
            console.log(res);
            $sta.isLogin = true;
            $sta.adminName = res.data && res.data.adminName;
            // 将管理员登录状态保存到localStorage
            localStorage.setItem('adminState', JSON.stringify({
                isLogin: true,
                adminName: res.data && res.data.adminName
            }));
            ElMessage.success(`登录成功，欢迎 ${res.data && res.data.adminName || '回来'}！`);
            router.replace({path:'/platform-admin'});
        } else {
            const msg = (res && (res.msg || res.message)) || '登录失败，账号或密码错误！';
            ElMessage.error(msg);
        }
    }).catch(err => {
        console.error('adminLogin error:', err);
        // 优先展示后端返回的友好信息（可能在 err 或 err.response.data 中）
        let serverMsg = null;

        // 情况1：请求拦截器或后端直接 reject({ status_code, msg }) 时
        if (err && typeof err === 'object' && (err.status_code !== undefined || err.msg)) {
            serverMsg = err.msg || err.message || null;
        }

        // 情况2：axios error 包含 response.data
        if (!serverMsg && err && err.response && err.response.data) {
            const d = err.response.data;
            if (d && (d.msg || d.status_code !== undefined)) {
                serverMsg = d.msg || d.message || null;
            }
        }

        // 如果能拿到后端提示，直接显示（例如：账号或密码错误）
        if (serverMsg) {
            ElMessage.error(serverMsg);
        } else {
            // 否则判断是否为网络错误或超时，给出网络提示
            const isNetwork = err && (err.message && (err.message.includes('Network') || err.message.includes('timeout')));
            if (isNetwork) {
                ElMessage.error('网络异常，请检查连接后重试');
            } else {
                // 兜底提示为登录失败（避免显示 [object Object]）
                ElMessage.error('登录失败，账号或密码错误！');
            }
        }
    }).finally(() => {
        loading.value = false;
    });
};
</script>

<style scoped>
    :root {
        --primary-color: #00A1D6;
        --primary-hover: #00b5e5;
        --text-main: #212121;
        --text-regular: #999999;
    }

    .login-container {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        width: 100%;
        background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    }

    .login-card {
        width: 480px;
        background: #FFFFFF;
        box-shadow: 0 4px 16px rgba(0,0,0,0.08);
        border-radius: 4px;
        overflow: hidden;
    }

    .login-body {
        padding: 40px;
    }

    .login-title {
        text-align: center;
        margin-bottom: 30px;
        font-size: 20px;
        font-weight: 500;
        color: #212121;
    }

    .input-group {
        margin-bottom: 20px;
    }

    .login-input {
        /* Element UI 样式穿透 */
    }

    /* 修复输入框样式 */
    :deep(.el-input__wrapper) {
        height: 42px;
        border-radius: 4px;
        border: 1px solid #E3E5E7;
        padding: 0 15px;
        transition: all 0.2s;
        background: #fff;
        box-shadow: none; /* 确保没有阴影干扰 */
        box-sizing: border-box; /* 确保边框计算在内 */
    }

    :deep(.el-input__inner) {
        height: 100%;
        border: none;
        padding: 0;
        background: transparent;
        color: #212121;
        line-height: 42px; /* 垂直居中 */
    }

    :deep(.el-input__wrapper.is-focus) {
        border-color: #FB7299; /* 聚焦时边框变粉 */
        box-shadow: 0 0 0 1px #FB7299 inset;
    }
    
    :deep(.el-input__wrapper:hover) {
        border-color: #c0c4cc;
    }
    
    :deep(.el-input__wrapper.is-focus:hover) {
        border-color: #FB7299;
    }

    :deep(.el-input__prefix) {
        margin-right: 10px;
        display: flex;
        align-items: center;
        font-size: 16px;
        color: #999999;
    }

    .login-submit {
        margin-top: 30px;
    }

    .submit-btn {
        width: 100%;
        height: 42px;
        border-radius: 4px;
        font-size: 16px;
        font-weight: 500;
        background: #FB7299; /* B站粉色 */
        border: none;
        box-shadow: none;
        transition: all 0.2s;
    }

    .submit-btn:hover {
        background-color: #fc8bab; /* 悬浮粉色 */
        transform: none;
    }

    .other-submit {
        display: flex;
        justify-content: center;
        margin-top: 20px;
    }

    .link-text {
        color: #999999;
        font-size: 12px;
        text-decoration: none;
        transition: color 0.2s;
    }

    .link-text:hover {
        color: #FB7299; /* 链接悬浮变粉 */
    }

    /* 处理浏览器自动填充样式 */
    :deep(input:-webkit-autofill),
    :deep(input:-webkit-autofill:hover),
    :deep(input:-webkit-autofill:focus),
    :deep(input:-webkit-autofill:active) {
        -webkit-box-shadow: 0 0 0 1000px white inset !important;
        -webkit-text-fill-color: #333 !important;
        transition: background-color 5000s ease-in-out 0s;
    }
</style>