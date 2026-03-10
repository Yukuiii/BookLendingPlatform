import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import UserLayout from '../layouts/UserLayout.vue'
import AnalysisView from '../views/AnalysisView.vue'
import BookSearchView from '../views/BookSearchView.vue'
import BorrowView from '../views/BorrowView.vue'
import FavoriteView from '../views/FavoriteView.vue'
import ProfileView from '../views/ProfileView.vue'
import ReservationView from '../views/ReservationView.vue'
import { getCurrentUser } from '../utils/auth'

const routes = [
  {
    path: '/',
    component: UserLayout,
    meta: {
      requiresAuth: true,
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
    return { name: 'books' }
  }

  return true
})

export default router
