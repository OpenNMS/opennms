import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'
import EditNode from '../components/Common/Demo/EditNode.vue'
import ProvisionDConfig from '../components/Configuration/ProvisionDConfig.vue'
import RequisitionDefinitionsLayout from '../components/Configuration/RequisitionDefinitionsLayout.vue'
import ThreadPools from '../components/Configuration/ThreadPoolForm.vue'
import FileEditor from '@/containers/FileEditor.vue'
import Logs from '@/containers/Logs.vue'

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
      path: '/file-editor',
      name: 'FileEditor',
      component: FileEditor
    },
    {
      path: '/logs',
      name: 'Logs',
      component: Logs
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
