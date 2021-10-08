import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'
import Inventory from '@/containers/Inventory.vue'
import StepAdd from '@/components/Inventory/StepAdd.vue'
import StepSchedule from '@/components/Inventory/StepSchedule.vue'
import StepConfigure from '@/components/Inventory/StepConfigure.vue'

const router = createRouter({
  history: createWebHashHistory('/opennms/ui'),
  routes: [
    {
      path: '/',
      name: 'nodes',
      component: Nodes
    },
    {
      path: '/node/:id',
      name: 'Node Details',
      component: NodeDetails
    },
    {
      path: '/inventory',
      name: 'Inventory',
      component: Inventory,
      children: [
        {
          path: '',
          component: StepAdd
        },
        {
          path: 'configure',
          component: StepConfigure
        },
        {
          path: 'schedule',
          component: StepSchedule
        }
      ]
    },
    {
      path: '/inventory',
      name: 'Inventory',
      component: Inventory,
      children: [
        {
          path: '',
          component: StepAdd
        },
        {
          path: 'configure',
          component: StepConfigure
        },
        {
          path: 'schedule',
          component: StepSchedule
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
