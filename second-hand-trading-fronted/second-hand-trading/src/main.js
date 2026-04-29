import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus';

import api from './api/index.js';

let sta={
    isLogin:false,
    adminName:''
};

/* 让它少提示一个错误信息 */
// Vue.config.productionTip = false; // Vue 3中不再需要

const app = createApp(App);
const pinia = createPinia();

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, {
    size: 'medium'
});

// 将api添加到全局属性
app.config.globalProperties.$api = api;
app.config.globalProperties.$sta = sta;

// 初始化Pinia Store
app.use(pinia);


import { useUserStore } from './stores/user';

// 页面加载时恢复管理员登录状态
const storedAdminState = localStorage.getItem('adminState');
if (storedAdminState) {
    const adminState = JSON.parse(storedAdminState);
    if (adminState.isLogin && adminState.adminName) {
        sta.isLogin = true;
        sta.adminName = adminState.adminName;
    }
}

router.beforeEach((to, from, next) => {
    document.title = `${to.meta.title}`;
    const publicPaths = ['/', '/index', '/search', '/details', '/login', '/sign-in', '/login-admin'];
    const userStore = useUserStore();
    const isAdminPath = to.path.startsWith('/platform-admin') || to.path.startsWith('/admin');
    const needsUserLogin = !publicPaths.includes(to.path) && !isAdminPath;
    
    // 管理员页面单独鉴权
    if (isAdminPath) {
        // 如果是管理员页面但没有管理员登录状态，跳转到管理员登录
        if (!sta.isLogin) {
            ElMessage.warning('请先登录管理员账号');
            next('/login-admin');
            return;
        }
        next();
        return;
    }

    if (needsUserLogin) {
        api.getUserInfo().then(res=>{
           console.log('getUserInfo:',res);
           if(res.status_code!==1){
               userStore.logout();
               next('/login');
           }else {
               res.data.signInTime=res.data.signInTime.substring(0,10);
               userStore.login(res.data);
               next();
           }
        }).catch(e=>{
            userStore.logout();
            next('/login');
        });

    }else{
        next();
    }
});

app.use(router);
app.mount('#app');
