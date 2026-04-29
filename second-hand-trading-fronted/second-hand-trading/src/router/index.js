import { createRouter, createWebHashHistory } from 'vue-router';

// 注意：Vue Router 4中已经移除了对Router原型的直接修改
// replace和push的错误处理现在可以通过其他方式实现

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/index'
    },
    {
      path: '/index',
      /*组件懒加载*/
      component: () => import('../components/page/index.vue'),
      meta: { title: '校园二手闲置物品交易平台' }
    },
    {
      path: '/search',
      component: () => import('../components/page/search.vue'),
      meta: { title: '闲置搜索 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/me',
      component: () => import('../components/page/me.vue'),
      meta: { title: '个人中心 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/message',
      component: () => import('../components/page/message.vue'),
      meta: { title: '消息 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/release',
      component: () => import('../components/page/release.vue'),
      meta: { title: '发布闲置 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/details',
      component: () => import('../components/page/idle-details.vue'),
      meta: { title: '闲置详情 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/order',
      component: () => import('../components/page/order.vue'),
      meta: { title: '订单详情 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/login',
      component: () => import('../components/page/login.vue'),
      meta: { title: '登录 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/sign-in',
      component: () => import('../components/page/sign-in.vue'),
      meta: { title: '注册 | 校园二手闲置物品交易平台' }
    },
    {
      path: '/login-admin',
      component: () => import('../components/page/login-admin.vue'),
      meta: { title: '管理员登陆' }
    },
    {
      path: '/platform-admin',
      component: () => import('../components/page/platform-admin.vue'),
      meta: { title: '后台管理' }
    },
    {
      path: '/admin/add',
      component: () => import('../components/page/admin-add.vue'),
      meta: { title: '添加管理员 | 后台管理' }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
});

// Vue Router 4中自定义replace和push方法的方式
const originalReplace = router.replace;
router.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => err);
};

const originalPush = router.push;
router.push = function push(location) {
  return originalPush.call(this, location).catch(err => err);
};

export default router;
