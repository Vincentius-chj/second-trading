import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  // 状态
  state: () => ({
    userInfo: {
      id: null,
      nickname: '',
      accountNumber: '',
      avatar: '',
      signInTime: '',
      backgroundImg: ''
    },
    isAuthenticated: false,
    token: null,
    isAdminLoggedIn: false,
    adminName: ''
  }),

  // getters
  getters: {
    isLoggedIn: (state) => !!state.isAuthenticated,
    getUserId: (state) => state.userInfo.id,
    getUserNickname: (state) => state.userInfo.nickname,
    getUserAvatar: (state) => state.userInfo.avatar,
    getUserAccount: (state) => state.userInfo.accountNumber,
    isAdminLoggedIn: (state) => state.isAdminLoggedIn,
    getAdminName: (state) => state.adminName
  },

  // actions
  actions: {
    // 设置用户信息
    setUserInfo(userData) {
      if (userData) {
        this.userInfo = {
          ...this.userInfo,
          ...userData
        };
        this.isAuthenticated = true;
      }
    },

    // 更新用户信息
    updateUserInfo(updates) {
      if (updates && typeof updates === 'object') {
        this.userInfo = {
          ...this.userInfo,
          ...updates
        };
      }
    },

    // 登录
    login(userData, userToken = null) {
      this.setUserInfo(userData);
      if (userToken) {
        this.token = userToken;
      }
      this.isAuthenticated = true;
    },

    // 登出
    logout() {
      this.userInfo = {
        id: null,
        nickname: '',
        accountNumber: '',
        avatar: '',
        signInTime: '',
        backgroundImg: ''
      };
      this.isAuthenticated = false;
      this.token = null;
    },

    // 设置认证状态
    setAuthenticated(status) {
      this.isAuthenticated = status;
    },

    // 管理员登录
    adminLogin(adminData) {
      this.isAdminLoggedIn = true;
      this.adminName = adminData.adminName;
    },

    // 管理员登出
    adminLogout() {
      this.isAdminLoggedIn = false;
      this.adminName = '';
    }
  }
});