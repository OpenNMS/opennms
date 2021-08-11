import { createRouter, createWebHistory } from 'vue-router'
import Nodes from '../containers/Nodes.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'nodes',
      component: Nodes,
    }
  ]
})

export default router
