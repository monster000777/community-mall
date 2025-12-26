import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/Products.vue')
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/Address.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'group-activities',
        name: 'GroupActivities',
        component: () => import('@/views/GroupActivities.vue')
      },
      {
        path: 'group-activities/:id',
        name: 'GroupActivityDetail',
        component: () => import('@/views/GroupActivityDetail.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/products'
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductManage.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderManage.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue')
      },
      {
        path: 'group-activities',
        name: 'AdminGroupActivities',
        component: () => import('@/views/admin/GroupActivityManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  const isLoggedIn = !!userStore.token
  const isAdmin = userStore.isAdmin && userStore.isAdmin()

  if ((to.path === '/login' || to.path === '/register') && isLoggedIn) {
    if (isAdmin) {
      next('/admin')
    } else {
      next('/')
    }
    return
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !isAdmin) {
    if (!isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      next('/')
    }
    return
  }

  next()
})

export default router
