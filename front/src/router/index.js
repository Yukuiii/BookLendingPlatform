import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import UserLayout from '../layouts/UserLayout.vue'
import AnalysisView from '../views/AnalysisView.vue'
import BookSearchView from '../views/BookSearchView.vue'
import BorrowView from '../views/BorrowView.vue'
import FavoriteView from '../views/FavoriteView.vue'
import ProfileView from '../views/ProfileView.vue'
import ReservationView from '../views/ReservationView.vue'
import AdminBookLocationView from '../views/admin/AdminBookLocationView.vue'
import AdminBookManageView from '../views/admin/AdminBookManageView.vue'
import AdminBorrowManageView from '../views/admin/AdminBorrowManageView.vue'
import AdminUserManageView from '../views/admin/AdminUserManageView.vue'
import { getCurrentUser } from '../utils/auth'

/**
 * 判断当前用户是否为管理员。
 *
 * @param {object|null} currentUser 当前用户
 * @returns {boolean} 是否为管理员
 */
function isAdminUser(currentUser) {
  const userType = Number(currentUser?.userType)
  return userType === 2 || userType === 3
}

const routes = [
  {
    path: '/',
    component: UserLayout,
    meta: {
      requiresAuth: true,
      userOnly: true,
    },
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'books',
        component: BookSearchView,
        meta: {
          title: '图书检索',
        },
      },
      {
        path: 'borrow',
        name: 'borrow',
        component: BorrowView,
        meta: {
          title: '我的借阅',
        },
      },
      {
        path: 'reservation',
        name: 'reservation',
        component: ReservationView,
        meta: {
          title: '我的预约',
        },
      },
      {
        path: 'favorite',
        name: 'favorite',
        component: FavoriteView,
        meta: {
          title: '我的收藏',
        },
      },
      {
        path: 'analysis',
        name: 'analysis',
        component: AnalysisView,
        meta: {
          title: '借阅分析',
        },
      },
      {
        path: 'profile',
        name: 'profile',
        component: ProfileView,
        meta: {
          title: '个人信息',
        },
      },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      requiresAuth: true,
      adminOnly: true,
    },
    redirect: '/admin/users',
    children: [
      {
        path: 'users',
        name: 'admin-users',
        component: AdminUserManageView,
        meta: {
          title: '用户管理',
        },
      },
      {
        path: 'books',
        name: 'admin-books',
        component: AdminBookManageView,
        meta: {
          title: '图书管理',
        },
      },
      {
        path: 'locations',
        name: 'admin-locations',
        component: AdminBookLocationView,
        meta: {
          title: '位置管理',
        },
      },
      {
        path: 'borrows',
        name: 'admin-borrows',
        component: AdminBorrowManageView,
        meta: {
          title: '借阅管理',
        },
      },
      {
        path: 'profile',
        name: 'admin-profile',
        component: ProfileView,
        meta: {
          title: '个人信息',
        },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      guestOnly: true,
    },
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: {
      guestOnly: true,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const currentUser = getCurrentUser()

  if (to.meta.requiresAuth && !currentUser) {
    return { name: 'login' }
  }
  if (to.meta.guestOnly && currentUser) {
    return { name: isAdminUser(currentUser) ? 'admin-users' : 'books' }
  }
  if (to.meta.userOnly && currentUser && isAdminUser(currentUser)) {
    return { name: 'admin-users' }
  }
  if (to.meta.adminOnly && currentUser && !isAdminUser(currentUser)) {
    return { name: 'books' }
  }

  return true
})

export default router
