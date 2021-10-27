import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'
import Demo from '../components/Common/Demo/Demo.vue'
import DataTableDemo from '../components/Common/Demo/DataTableDemo.vue'
import EditNode from '../components/Common/Demo/EditNode.vue'
import ProvisionDConfig from '../components/Configuration/ProvisionDConfig.vue'
import RequisitionDefinitionsLayout from '../components/Configuration/RequisitionDefinitionsLayout.vue'
import ThreadPools from '../components/Configuration/ThreadPoolForm.vue'

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
      path: '/demo',
      name: 'Demo',
      component: Demo
    },
    {
      path: '/dataTableDemo',
      name: 'DataTableDemo',
      component: DataTableDemo
    },
    {
      path: '/provisionDConfig',
      name: 'provisionDConfig',
      component: ProvisionDConfig,
      children: [
        {
          path: '',
          name: 'requisitionDefinitionsLayout',
          component: RequisitionDefinitionsLayout
        },
        {
          path: '/threadPools',
          name: 'threadPools',
          component: ThreadPools
        },
        {
          path: '/edit/:id',
          name: 'reqDefEdit',
          component: EditNode
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
