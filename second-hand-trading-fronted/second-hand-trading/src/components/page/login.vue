<template>
  <div class="login-container">
    <div class="background-animation"></div>
    <div class="login-card">
      <div class="login-body">
        <div class="login-title" @click="toIndex">
          <span class="brand-text">校园二手</span>
          <span class="brand-text">闲置物品交易平台</span>
        </div>
        <el-form ref="formRef" :model="userForm" class="login-form">
          <div class="input-group">
            <el-input placeholder="请输入手机号" v-model="userForm.accountNumber" class="login-input">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input placeholder="请输入密码" v-model="userForm.userPassword" class="login-input"
                      @keyup.enter="login" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="login-submit">
            <el-button type="primary" class="submit-btn" :loading="isLoading" @click="login">
              {{ isLoading ? '登录中...' : '立即登录' }}
            </el-button>
          </div>
          <div class="other-submit">
            <router-link to="/sign-in" class="link-text">注册账号</router-link>
            <router-link to="/login-admin" class="link-text">管理员登录</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

// 获取当前实例以访问全局属性
const instance = getCurrentInstance()
const $api = instance.appContext.config.globalProperties.$api
const $message = instance.appContext.config.globalProperties.$message
const $sta = instance.appContext.config.globalProperties.$sta

// 路由
const router = useRouter()

// Pinia Store
const userStore = useUserStore()

// 表单引用
const formRef = ref()
const isLoading = ref(false)

// 表单数据
const userForm = reactive({
  accountNumber: '',
  userPassword: ''
})

// 登录方法
const login = () => {
  if(!userForm.accountNumber || !userForm.userPassword) {
    $message.warning('请输入账号和密码')
    return
  }
  
  isLoading.value = true
  $api.userLogin({
    accountNumber: userForm.accountNumber,
    userPassword: userForm.userPassword
  }).then(res => {
    console.log(res)
    if (res.status_code === 1) {
      res.data.signInTime = res.data.signInTime.substring(0, 10)
      userStore.login(res.data)
      $message.success(`登录成功，欢迎 ${res.data.nickname || '回来'}！`)
      setTimeout(() => {
        router.replace({ path: '/index' })
      }, 500)
    } else {
      $message.error(res.msg)
    }
  }).catch(() => {
    $message.error('登录失败，请检查网络')
  }).finally(() => {
    isLoading.value = false
  })
}

// 跳转到首页
const toIndex = () => {
  router.replace({ path: '/index' })
}
</script>

<style scoped>
    .login-container {
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        width: 100%;
        background-color: #F4F5F7;
        /* 使用渐变背景代替单调的纯色或图片 */
        background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
        overflow: hidden;
    }

    /* 移除之前的动态背景动画，改用 B 站风格的静态插画背景（这里用颜色模拟） */
    .background-animation {
        display: none;
    }

    .login-card {
        width: 480px;
        padding: 0;
        z-index: 1;
        background: #FFFFFF;
        box-shadow: 0 4px 16px rgba(0,0,0,0.08);
        border: none;
        border-radius: 4px; /* B站圆角较小 */
        animation: fadeInUp 0.5s ease-out;
        overflow: hidden;
    }
    
    .login-body {
        padding: 40px;
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .login-title {
        text-align: center;
        margin-bottom: 30px;
        cursor: pointer;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        /* 强制覆盖可能的 transform */
        transform: none !important;
        transition: none !important;
    }
    
    .login-title:hover {
        /* 确保 hover 时也不会动 */
        transform: none !important;
    }
    
    /* 移除所有可能导致移动的样式 */
    .brand-text {
        font-size: 20px;
        font-weight: 500;
        color: var(--text-main);
        margin-bottom: 0;
        letter-spacing: 0;
        display: inline-block;
        /* 强制覆盖 */
        transform: none !important;
        transition: none !important;
    }
    
    .brand-text:hover {
        transform: none !important;
    }



    .input-group {
        margin-bottom: 20px;
        transition: none;
    }
    
    .input-group:focus-within {
        transform: none;
    }

    /* 修复输入框 hover 时边框移动的问题 */
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
        color: var(--text-main);
        line-height: 42px; /* 垂直居中 */
    }

    :deep(.el-input__wrapper.is-focus) {
        border-color: var(--primary-color);
        box-shadow: 0 0 0 1px var(--primary-color) inset; /* 使用内阴影模拟加粗，防止布局抖动 */
    }
    
    :deep(.el-input__wrapper:hover) {
        border-color: #c0c4cc;
    }
    
    :deep(.el-input__wrapper.is-focus:hover) {
        border-color: var(--primary-color);
    }

    :deep(.el-input__prefix) {
        margin-right: 10px;
        display: flex;
        align-items: center;
        font-size: 16px;
        color: var(--text-regular);
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
        letter-spacing: 0;
        background: var(--primary-color);
        border: none;
        transition: all 0.2s;
        box-shadow: none;
    }

    .submit-btn:hover {
        box-shadow: none;
        background-color: var(--primary-hover);
    }
    
    .submit-btn:active {
        background-color: #e36686;
    }

    .other-submit {
        display: flex;
        justify-content: space-between;
        margin-top: 20px;
        padding: 0;
    }

    .link-text {
        color: var(--text-regular);
        font-size: 12px;
        text-decoration: none;
        transition: color 0.2s;
        position: relative;
    }

    .link-text:hover {
        color: var(--primary-color);
    }
    
    .link-text::after {
        display: none;
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