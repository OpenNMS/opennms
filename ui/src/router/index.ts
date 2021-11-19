import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory('/opennms/ui'),
  routes: [
    {
      path: '/',
      name: 'nodes',
      component: () => import('@/containers/Nodes.vue')
    },
    {
      path: '/node/:id',
      name: 'Node Details',
      component: () => import('@/containers/NodeDetails.vue')
    },
    {
      path: '/inventory',
      name: 'Inventory',
      component: () => import('@/containers/Inventory.vue'),
      children: [
        {
          path: '',
          component: () => import('@/components/Inventory/StepAdd.vue')
        },
        {
          path: 'configure',
          component: () => import('@/components/Inventory/StepConfigure.vue')
        },
        {
          path: 'schedule',
          component: () => import('@/components/Inventory/StepSchedule.vue')
        }
      ]
    },
    {
      path: '/file-editor',
      name: 'FileEditor',
      component: () => import('@/containers/FileEditor.vue')
    },
    {
      path: '/logs',
      name: 'Logs',
      component: () => import('@/containers/Logs.vue')
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
