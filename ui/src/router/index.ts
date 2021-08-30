import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'
import Demo from '../components/Common/Demo/Demo.vue'
import DataTableDemo from '../components/Common/Demo/DataTableDemo.vue'
import EditNode from '../components/Common/Demo/EditNode.vue'

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
      path: '/edit/:id',
      name: 'Edit',
      component: EditNode
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
