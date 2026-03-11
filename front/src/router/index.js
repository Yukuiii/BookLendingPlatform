import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import UserLayout from '../layouts/UserLayout.vue'
import BookSearchView from '../views/BookSearchView.vue'
import BorrowView from '../views/BorrowView.vue'
import CommentView from '../views/CommentView.vue'
import FavoriteView from '../views/FavoriteView.vue'
import NotificationView from '../views/NotificationView.vue'
import PreferenceView from '../views/PreferenceView.vue'
import ProfileView from '../views/ProfileView.vue'
import AdminBookLocationView from '../views/admin/AdminBookLocationView.vue'
import AdminBookManageView from '../views/admin/AdminBookManageView.vue'
import AdminBorrowManageView from '../views/admin/AdminBorrowManageView.vue'
import AdminCommentManageView from '../views/admin/AdminCommentManageView.vue'
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

/**
 * 判断当前用户是否为系统管理员。
 *
 * @param {object|null} currentUser 当前用户
 * @returns {boolean} 是否为系统管理员
 */
function isSystemAdminUser(currentUser) {
  return Number(currentUser?.userType) === 3
}

/**
 * 解析管理端默认首页。
 *
 * @param {object|null} currentUser 当前用户
 * @returns {string} 默认首页路由名
 */
function resolveAdminHomeRouteName(currentUser) {
  return isSystemAdminUser(currentUser) ? 'admin-users' : 'admin-books'
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
        path: 'favorite',
        name: 'favorite',
        component: FavoriteView,
        meta: {
          title: '我的收藏',
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
      {
        path: 'notifications',
        name: 'notifications',
        component: NotificationView,
        meta: {
          title: '信息通知',
        },
      },
      {
        path: 'comments',
        name: 'comments',
        component: CommentView,
        meta: {
          title: '我的评论',
        },
      },
      {
        path: 'preferences',
        name: 'preferences',
        component: PreferenceView,
        meta: {
          title: '个性化设置',
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
    redirect: () => {
      return { name: resolveAdminHomeRouteName(getCurrentUser()) }
    },
    children: [
      {
        path: 'users',
        name: 'admin-users',
        component: AdminUserManageView,
        meta: {
          title: '用户管理',
          systemAdminOnly: true,
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
        path: 'comments',
        name: 'admin-comments',
        component: AdminCommentManageView,
        meta: {
          title: '评论管理',
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
    return { name: isAdminUser(currentUser) ? resolveAdminHomeRouteName(currentUser) : 'books' }
  }
  if (to.meta.userOnly && currentUser && isAdminUser(currentUser)) {
    return { name: resolveAdminHomeRouteName(currentUser) }
  }
  if (to.meta.adminOnly && currentUser && !isAdminUser(currentUser)) {
    return { name: 'books' }
  }
  if (to.meta.systemAdminOnly && currentUser && !isSystemAdminUser(currentUser)) {
    return { name: 'admin-books' }
  }

  return true
})

export default router
