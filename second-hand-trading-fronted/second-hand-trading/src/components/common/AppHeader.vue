<template>
  <div class="header">
    <div class="header-container">
      <div class="app-name">
        <router-link to="/">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="20" height="20" class="bili-logo-icon">
                <path d="M480 352c-70.4 0-128 57.6-128 128s57.6 128 128 128 128-57.6 128-128-57.6-128-128-128zm96 160H384v-64h192v64zm320-384H128c-35.2 0-64 28.8-64 64v512c0 35.2 28.8 64 64 64h768c35.2 0 64-28.8 64-64V192c0-35.2-28.8-64-64-64z m-32 576H160V192h704v512z" fill="currentColor"></path>
                <path d="M288 320h64v64h-64z m384 0h64v64h-64z" fill="currentColor"></path>
            </svg>
            <span class="home-text">首页</span>
        </router-link>
      </div>
      <div class="center-area">
        <div class="search-container">
          <el-input 
            placeholder="搜闲置..." 
            v-model="searchValue" 
            @keyup.enter="searchIdle"
            @clear="handleSearchClear"
            clearable
            class="custom-search">
            <template #append>
              <el-button @click="searchIdle">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <!-- 头像/登录区域 - 紧随搜索框 -->
        <div class="user-area">
            <router-link v-if="!isLogin" class="login-link" to="/login">登录</router-link>
            <el-dropdown
              trigger="hover"
              placement="bottom"
              :show-timeout="80"
              :hide-timeout="220"
              popper-class="user-menu-popper"
              @command="handleUserCommand"
              v-else
              class="user-dropdown">
              <div class="user-info-trigger">
                <el-avatar :size="36" :src="avatarValue ? avatarValue : avatar" class="user-avatar"></el-avatar>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="custom-dropdown">
                  <el-dropdown-item command="me" class="dropdown-menu-item">
                    <div class="dropdown-item-content">
                      <span class="menu-icon"><el-icon><User /></el-icon></span>
                      <span class="menu-text">个人中心</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided class="dropdown-menu-item">
                    <div class="dropdown-item-content logout">
                      <span class="menu-icon"><el-icon><SwitchButton /></el-icon></span>
                      <span class="menu-text">退出登录</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
        </div>
      </div>

      <div class="nav-buttons">
        <el-button type="text" class="nav-icon-btn" @click="toMessage">
          <el-icon><ChatDotRound /></el-icon>消息
        </el-button>
        <el-button type="text" class="nav-icon-btn" @click="toCart">
          <el-icon><ShoppingCart /></el-icon>收藏
        </el-button>
        <el-button type="primary" class="nav-btn" round @click="toRelease">
          <el-icon><Plus /></el-icon>发布闲置
        </el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, getCurrentInstance, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Search, Plus, ChatDotRound, User, SwitchButton, Platform, ShoppingCart, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

// 定义props
const props = defineProps(['searchInput', 'nicknameValue', 'avatarValue'])

// 获取当前实例以访问全局属性
const instance = getCurrentInstance()
const $api = instance.appContext.config.globalProperties.$api
const $message = instance.appContext.config.globalProperties.$message

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const searchValue = ref(props.searchInput || '')
const nickname = ref('登录')
const avatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
const isLogin = ref(false)

// Pinia Store
const userStore = useUserStore()

// 生命周期钩子
onMounted(() => {
  // 进入页面时优先回填 URL 中的搜索词
  searchValue.value = String(route.query.searchValue || props.searchInput || '')
  // console.log("header");
  if (!userStore.getUserNickname) {
    $api.getUserInfo().then(res => {
      console.log('Header getUserInfo:', res)
      if (res.status_code === 1) {
        nickname.value = res.data.nickname
        avatar.value = res.data.avatar
        res.data.signInTime = res.data.signInTime.substring(0, 10)
        userStore.login(res.data)
        isLogin.value = true
      } else {
        userStore.logout();
        isLogin.value = false;
      }
    }).catch(error => {
      // 当获取用户信息失败时（如未登录），不显示错误信息，只是保持未登录状态
      console.log('获取用户信息失败，用户未登录或登录已过期:', error);
      // 清除可能存在的用户信息
      userStore.logout();
      isLogin.value = false;
    });
  } else {
    nickname.value = userStore.getUserNickname
    avatar.value = userStore.getUserAvatar
    isLogin.value = true
  }
})

// 路由参数变化时同步输入框，保证跨页返回后仍显示搜索词
watch(
  () => route.query.searchValue,
  (val) => {
    searchValue.value = String(val || '')
  },
  { immediate: true }
)

// 父组件传入搜索词时同步输入框（兼容 search 页）
watch(
  () => props.searchInput,
  (val) => {
    if (val !== undefined && val !== null) {
      searchValue.value = String(val)
    }
  }
)

// 方法定义
const searchIdle = () => {
  const nextSearchValue = searchValue.value == null ? '' : String(searchValue.value)
  const filterablePages = new Set(['/index', '/search'])
  const canFilterInCurrentPage = filterablePages.has(route.path)

  if (canFilterInCurrentPage && String(route.query.searchValue || '') === nextSearchValue) {
    return;
  }
  const query = {
    searchValue: nextSearchValue,
    page: 1
  }

  if (canFilterInCurrentPage) {
    if (route.query.labelName) {
      query.labelName = route.query.labelName
    }
    router.replace({ path: route.path, query })
    return
  }

  // 非筛选页面（如详情页）跳转到首页展示搜索结果
  router.push({ path: '/index', query })
}

const handleSearchClear = () => {
  searchValue.value = ''
  searchIdle()
}

const ensureUserSession = async () => {
  try {
    const res = await $api.getUserInfo()
    if (res && res.status_code === 1 && res.data) {
      if (res.data.signInTime) {
        res.data.signInTime = String(res.data.signInTime).substring(0, 10)
      }
      userStore.login(res.data)
      nickname.value = res.data.nickname || '登录'
      avatar.value = res.data.avatar || avatar.value
      isLogin.value = true
      return true
    }
  } catch (error) {
    console.log('会话校验失败:', error)
  }

  userStore.logout()
  isLogin.value = false
  router.push({ path: '/login' })
  return false
}

const toMe = async () => {
  const ok = await ensureUserSession()
  if (!ok) {
    return
  }
  if ('/me' !== route.path) {
    router.push({ path: '/me' })
  }
}

const toMessage = async () => {
  const ok = await ensureUserSession()
  if (!ok) {
    return
  }
  if ('/message' !== route.path) {
    router.push({ path: '/message' })
  }
}

const toCart = async () => {
  const ok = await ensureUserSession()
  if (!ok) {
    return
  }
  // 保存tab状态，跳转到收藏tab（activeName='3'）
  sessionStorage.setItem('meActiveTab', '3')
  // 强制跳转到个人中心，确保onMounted重新执行
  if ('/me' === route.path) {
    // 如果已经在个人中心，刷新页面以激活tab
    router.go(0);
  } else {
    router.push({ path: '/me' });
  }
}

const toRelease = async () => {
  const ok = await ensureUserSession()
  if (!ok) {
    return
  }
  if ('/release' !== route.path) {
    router.push({ path: '/release' })
  }
}

const handleUserCommand = (command) => {
  if (command === 'me') {
    toMe()
    return
  }
  if (command === 'logout') {
    loginOut()
  }
}

const loginOut = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        lockScroll: false,  // 禁用滚动锁定，防止页面移动
      }
    );
    
    $api.logout().then(res => {
      if (res.status_code === 1) {
        userStore.logout()
        console.log("login out")
        ElMessage.success('已成功退出登录')
        if ('/index' === route.path) {
          router.go(0)
        } else {
          router.push({ path: '/index' })
        }
      } else {
        ElMessage.error('网络或系统异常，退出登录失败！')
      }
    }).catch(error => {
      console.error('退出登录失败:', error);
      // 提取错误信息，防止显示[object Object]
      let errorMessage = '网络或系统异常，退出登录失败！';
      if (error && error.response && error.response.data && error.response.data.msg) {
        errorMessage = error.response.data.msg;
      } else if (error.message) {
        errorMessage = error.message;
      } else if (typeof error === 'object' && error.toString) {
        errorMessage = error.toString();
      } else {
        errorMessage = error + '';
      }
      ElMessage.error(errorMessage);
    });
  } catch (error) {
    // 用户取消了退出操作
    if (error !== 'cancel') {
      console.error('退出登录过程中发生错误:', error);
      let errorMessage = '退出登录过程中发生错误！';
      if (error && typeof error === 'object' && error.message) {
        errorMessage = error.message;
      } else if (typeof error === 'object' && error.toString) {
        errorMessage = error.toString();
      } else {
        errorMessage = error + '';
      }
      ElMessage.error(errorMessage);
    } else {
      console.log('用户取消退出登录')
    }
  }

}
</script>
<style scoped>
    .header {
        position: sticky;
        top: 0;
        width: 100%;
        min-width: 1000px; /* 增加最小宽度 */
        height: 64px;
        background: #FFFFFF;
        display: flex;
        justify-content: center;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
        z-index: 1000;
        transition: all 0.3s ease;
    }

    .header-container {
        width: 100%;
        /* max-width: 1000px; */
        min-width: 1000px; /* 调整最小宽度与 header 一致 */
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center; /* 改为居中对齐 */
        padding: 0 24px;
        position: relative; /* 为绝对定位做参照 */
    }

    .app-name {
        position: absolute;
        left: 24px;
        top: 0;
        bottom: 0;
        margin: 0;
        flex-shrink: 0;
        display: flex;
        align-items: center;
    }

    .app-name a {
        color: var(--text-main); /* 默认黑色 */
        font-size: 14px; /* 减小字号 */
        font-weight: 600; /* 保持加粗但稍微细一点 */
        text-decoration: none;
        display: flex;
        align-items: center; /* 关键：垂直居中 */
        justify-content: center;
        transition: color 0.2s;
        white-space: nowrap;
        gap: 6px; /* 图标文字间距 */
        height: 100%; /* 继承父高度 */
        line-height: 1; /* 重置行高 */
    }

    .bili-logo-icon {
        color: var(--primary-color); /* B站粉 */
        display: block; /* 防止inline元素基线对齐问题 */
        margin-top: 1px; /* 向下微调 1px */
    }

    .home-text {
        font-size: 15px; /* 稍微调大一点 */
        line-height: 1; /* 防止行高影响对齐 */
    }
    
    /* 中间区域：搜索框 + 头像 */
    .center-area {
        display: flex;
        align-items: center;
        gap: 12px; /* 搜索框和头像的间距 */
    }
    
    .user-area {
        display: flex;
        align-items: center;
    }

    .app-name a:hover {
        color: var(--primary-color); /* 悬停变蓝 */
    }
    /* 搜索框 B站风格 */
    .search-container {
        width: 500px; /* 固定宽度 */
        margin: 0;
    }
    
    .user-area {
        display: flex;
        align-items: center;
    }

    .nav-buttons {
        position: absolute;
        right: 24px;
        top: 50%;
        transform: translateY(-50%);
        display: flex;
        align-items: center;
        gap: 16px;
    }
    
    /* 消息按钮和收藏按钮之间间距更小 - 使用负margin调整 */
    :deep(.nav-buttons > .nav-icon-btn:nth-child(1)) {
        margin-right: -8px !important;
    }
    
    :deep(.custom-search .el-input__wrapper) {
        border-radius: 8px 0 0 8px;
        background-color: #F1F2F3;
        box-shadow: none !important;
        border: 1px solid transparent;
        padding-left: 15px;
        transition: all 0.2s;
    }
    
    :deep(.custom-search .el-input__wrapper:hover) {
        background-color: #ffffff;
        border-color: #e3e5e7;
    }
    
    :deep(.custom-search .el-input__wrapper.is-focus) {
        background-color: #ffffff;
        border-color: #e3e5e7;
    }

    :deep(.custom-search .el-input__inner) {
        height: 38px;
        line-height: 38px;
        color: var(--text-main);
    }
    
    :deep(.custom-search .el-input-group__append) {
        border-radius: 0 8px 8px 0;
        background-color: #F1F2F3;
        border: none;
        box-shadow: none;
        color: var(--text-main);
        padding: 0;
        width: 48px;
        transition: all 0.2s;
    }
    
    :deep(.custom-search .el-input-group__append:hover) {
        background-color: #E3E5E7;
    }
    
    :deep(.custom-search .el-icon) {
        font-size: 20px;
        font-weight: bold;
        color: var(--text-main);
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    :deep(.custom-search .el-input-group__append button.el-button) {
        margin: 0;
        padding: 0;
        height: 100%;
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .nav-buttons {
        display: flex;
        align-items: center;
        gap: 16px;
    }

    /* B站风格按钮 */
    .nav-btn {
        background: var(--primary-color);
        border: none;
        color: #fff;
        font-weight: 500;
        padding: 0 20px;
        height: 34px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: none;
        transition: all 0.2s;
    }

    .nav-btn :deep(span) {
        display: flex;
        align-items: center;
        gap: 4px;
    }
    
    .nav-btn:hover {
        background-color: var(--primary-hover);
        transform: none;
        box-shadow: none;
    }
    
    .nav-icon-btn {
        font-size: 14px;
        color: var(--text-regular);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 10px;
        background: transparent;
        height: 34px;
        border: none;
    }

    .nav-icon-btn :deep(span) {
        display: flex;
        align-items: center;
        gap: 4px;
    }
    
    .nav-icon-btn .el-icon {
        font-size: 18px;
    }
    
    .nav-icon-btn:hover {
        color: var(--primary-color);
        background-color: transparent;
    }

    .login-link {
        font-size: 14px;
        color: var(--primary-color);
        cursor: pointer;
        text-decoration: none;
        padding: 5px 10px;
        transition: all 0.2s;
        border: none;
        background: transparent;
    }
    
    .login-link:hover {
        color: var(--primary-hover);
        background: transparent;
        box-shadow: none;
        transform: none;
    }

    /* 头像区域 */
    .user-info-trigger {
        cursor: pointer;
        display: flex;
        align-items: center;
      gap: 6px;
      padding: 3px 8px 3px 3px;
      border-radius: 24px;
      transition: all 0.2s ease;
      border: 1px solid #e3e5e7;
      background: #fff;
    }
    
    .user-info-trigger:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      border-color: #c8cdd5;
      transform: translateY(-1px);
    }
    
    /* 隐藏未悬停时的文字，B站风格只显示头像 */
    .username {
        display: none; 
    }
    
    .user-avatar {
        border: 1px solid #E3E5E7;
        box-shadow: none;
        transition: none;
    }

    .dropdown-arrow {
      font-size: 12px;
      color: #8b93a3;
      transition: transform 0.2s ease;
    }

    .user-dropdown:hover .dropdown-arrow {
      transform: rotate(180deg);
    }
    
    .user-info-trigger:hover .user-avatar {
        transform: none;
    }
    
    .custom-dropdown {
      padding: 3px;
      min-width: 109px;
      border-radius: 8px;
      text-align: left;
    }

    .dropdown-menu-item {
      line-height: normal;
    }

    .dropdown-item-content {
        display: flex;
        align-items: center;
      justify-content: flex-start;
      gap: 8px;
      padding: 6px 8px;
        color: var(--text-main);
      font-size: 13px;
      border-radius: 6px;
      font-weight: 400;
    }

    .menu-icon {
      width: 22px;
      height: 22px;
      border-radius: 6px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: transparent;
      color: #606266;
      flex-shrink: 0;
    }

    .menu-text {
      line-height: 1;
      position: relative;
      left: -2px;
      top: 1px;
    }
    
    .dropdown-item-content .el-icon {
        font-size: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .logout {
        color: var(--text-main);
    }

    .logout .menu-icon {
      background: transparent;
      color: #e05b5b;
    }
    
    .logout:hover {
        color: var(--primary-color);
    }

    :deep(.user-menu-popper) {
      border-radius: 8px;
      padding: 3px;
      border: 1px solid #ebeef5;
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.10);
    }

    :deep(.user-menu-popper .el-popper__arrow) {
      left: 50% !important;
      transform: translateX(-50%);
    }

    :deep(.user-menu-popper .el-dropdown-menu__item) {
      padding: 0;
      margin: 2px 0;
      border-radius: 6px;
      transition: background-color 0.2s ease;
    }

    :deep(.user-menu-popper .el-dropdown-menu__item--divided) {
      margin-top: 4px;
    }

    :deep(.user-menu-popper .el-dropdown-menu__item:hover) {
      background-color: #f4f7fb;
    }
</style>
