<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
// Vue 3 Composition API setup

// 解决 ResizeObserver loop completed with undelivered notifications 错误
const debounce = (fn, delay) => {
  let timer = null;
  return function () {
    let context = this;
    let args = arguments;
    clearTimeout(timer);
    timer = setTimeout(function () {
      try {
        fn.apply(context, args);
      } catch (err) {
        const msg = err && err.message ? String(err.message) : '';
        // 路由切换/未登录重定向时，el-select 可能已卸载，忽略该瞬时 getComputedStyle 异常
        if (msg.includes("Failed to execute 'getComputedStyle' on 'Window'")) {
          return;
        }
        throw err;
      }
    }, delay);
  }
}

const _ResizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends _ResizeObserver {
  constructor(callback) {
    callback = debounce(callback, 16);
    super(callback);
  }
}

// 解决弹窗导致的页面抖动问题
let scrollBarWidth = 0;
let bodyPaddingRight = 0;
let isModalOpen = false;

// 计算滚动条宽度
const getScrollBarWidth = () => {
  const outer = document.createElement('div');
  outer.style.visibility = 'hidden';
  outer.style.overflow = 'scroll';
  outer.style.msOverflowStyle = 'scrollbar';
  document.body.appendChild(outer);
  
  const inner = document.createElement('div');
  outer.appendChild(inner);
  
  const scrollbarWidth = outer.offsetWidth - inner.offsetWidth;
  outer.parentNode.removeChild(outer);
  
  return scrollbarWidth;
};

// 当模态框打开时，调整body样式以防止抖动
const modalOpenHandler = () => {
  if (isModalOpen) return;
  isModalOpen = true;
  
  scrollBarWidth = getScrollBarWidth();
  const bodyHasOverflow = document.documentElement.clientHeight < document.documentElement.scrollHeight;
  const bodyOverflowY = window.getComputedStyle(document.body).overflowY;
  
  if (scrollBarWidth > 0 && (bodyHasOverflow || bodyOverflowY === 'scroll')) {
    bodyPaddingRight = parseInt(window.getComputedStyle(document.body).paddingRight, 10);
    document.body.style.paddingRight = `${bodyPaddingRight + scrollBarWidth}px`;
  }
};

// 当模态框关闭时，恢复body样式
const modalCloseHandler = () => {
  if (!isModalOpen) return;
  isModalOpen = false;
  
  document.body.style.paddingRight = `${bodyPaddingRight}px`;
};

// 监听Element Plus弹窗的打开和关闭事件
const originalClassListAdd = DOMTokenList.prototype.add;
DOMTokenList.prototype.add = function(...args) {
  if (args.includes('el-popup-parent--hidden')) {
    modalOpenHandler();
  }
  return originalClassListAdd.call(this, ...args);
};

const originalClassListRemove = DOMTokenList.prototype.remove;
DOMTokenList.prototype.remove = function(...args) {
  if (args.includes('el-popup-parent--hidden')) {
    modalCloseHandler();
  }
  return originalClassListRemove.call(this, ...args);
};
</script>

<style>
/* 全局样式重置与变量定义 */
:root {
  --primary-color: #FB7299;
  --primary-hover: #fc8bab;
  --bg-color: #F4F5F7;
  --text-main: #18191C;
  --text-regular: #61666D;
  --text-secondary: #9499A0;
  --card-bg: #FFFFFF;
  --glass-bg: rgba(255, 255, 255, 0.9);
  --glass-border: 1px solid #E3E5E7;
  --shadow-light: 0 2px 4px rgba(0, 0, 0, 0.08);
  --shadow-hover: 0 8px 16px rgba(0, 0, 0, 0.12);
  --transition-fast: 0.2s ease-in-out;
  --radius-main: 6px;
}

html, body, #app {
  overflow-x: auto;
  background-color: var(--bg-color);
  margin: 0;
  padding: 0;
  min-height: 100vh;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}



/* 全局滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 通用点击波纹效果 (可用于按钮等) */
.ripple-effect {
  position: relative;
  overflow: hidden;
}

/* 通用卡片样式 */
.modern-card {
  background: var(--card-bg);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: var(--shadow-light);
  border: var(--glass-border);
  transition: all var(--transition-fast);
}
.modern-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
}

/* Element Plus 图片预览限制为80% */
.el-image-viewer__wrapper {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.el-image-viewer__canvas {
  width: 80vw !important;
  height: 80vh !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.el-image-viewer__img {
  max-width: 80vw !important;
  max-height: 80vh !important;
  width: auto !important;
  height: auto !important;
  object-fit: contain !important;
}

/* 美化 MessageBox 二次确认弹窗 */
.el-message-box {
  border-radius: 12px !important;
  padding: 24px !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15) !important;
  border: none !important;
  margin-top: -15vh !important;
}

/* 解决弹窗导致的页面抖动问题 - 计算滚动条宽度进行补偿 */
.el-popup-parent--hidden {
  overflow: initial !important;
  /* 计算滚动条宽度并补偿，防止页面抖动 */
  padding-right: calc(100vw - 100%) !important;
}

.el-message-box__header {
  padding-bottom: 16px !important;
}

.el-message-box__title {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #18191C !important;
}

.el-message-box__content {
  padding: 16px 0 !important;
  font-size: 14px !important;
  color: #61666D !important;
  line-height: 1.6 !important;
}

.el-message-box__btns {
  padding-top: 20px !important;
  display: flex !important;
  gap: 12px !important;
  justify-content: flex-end !important;
}

.el-message-box__btns button {
  min-width: 80px !important;
  height: 36px !important;
  border-radius: 6px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  transition: all 0.2s !important;
}

/* 确定按钮 */
.el-message-box__btns .el-button--primary {
  background: linear-gradient(135deg, #FB7299 0%, #FF88AA 100%) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(251, 114, 153, 0.3) !important;
}

.el-message-box__btns .el-button--primary:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(251, 114, 153, 0.4) !important;
}

/* 取消按钮 */
.el-message-box__btns .el-button--default {
  background: #FFFFFF !important;
  border: 1px solid #E3E5E7 !important;
  color: #61666D !important;
}

.el-message-box__btns .el-button--default:hover {
  background: #F6F7F8 !important;
  border-color: #FB7299 !important;
  color: #FB7299 !important;
}

/* 不同类型的图标颜色 */
.el-message-box__status.el-message-box-icon--success {
  color: #67C23A !important;
  font-size: 24px !important;
}

.el-message-box__status.el-message-box-icon--warning {
  color: #E6A23C !important;
  font-size: 24px !important;
}

.el-message-box__status.el-message-box-icon--error {
  color: #F56C6C !important;
  font-size: 24px !important;
}

/* 美化 prompt 输入框 */
.el-message-box__input {
  padding-top: 12px !important;
}

.el-message-box__input input,
.el-message-box__input textarea {
  border: 1px solid #E3E5E7 !important;
  border-radius: 6px !important;
  padding: 10px 12px !important;
  font-size: 14px !important;
  color: #18191C !important;
  transition: all 0.2s !important;
}

.el-message-box__input textarea {
  min-height: 80px !important;
  resize: vertical !important;
  line-height: 1.6 !important;
}

.el-message-box__input input:focus,
.el-message-box__input textarea:focus {
  border-color: #FB7299 !important;
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.1) !important;
}

.el-message-box__input input::placeholder,
.el-message-box__input textarea::placeholder {
  color: #C0C4CC !important;
}

.el-message-box__errormsg {
  color: #F56C6C !important;
  font-size: 12px !important;
  margin-top: 6px !important;
}
</style>