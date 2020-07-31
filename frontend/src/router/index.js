import Vue from 'vue'
import VueRouter from 'vue-router'
import Playlists from '@/views/Playlists'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Playlists',
    component: Playlists
  },
  {
    path: '/optimizations',
    name: 'Optimizations',
    component: () => import(/* webpackChunkName: "about" */ '../views/Optimizations.vue')
  }
]

export default new VueRouter({
  routes
})
