import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'
import FileEditor from '@/containers/FileEditor.vue'
import ProvisionDConfig from '@/containers/ProvisionDConfig.vue'
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
      path: '/configuration',
      name: 'Configuration',
      component: ProvisionDConfig
    },
    {
      path: '/logs',
      name: 'Logs',
      component: Logs
    },
    {
      path: '/map',
      name: 'Map',
      component: () => import('@/containers/Map.vue'),
      children: [
        {
          path: '',
          name: 'MapAlarms',
          component: () => import('@/components/Map/MapAlarmsGrid.vue')
        },
        {
          path: 'nodes',
          name: 'MapNodes',
          component: () => import('@/components/Map/MapNodesGrid.vue')
        }
      ]
    },
    {
      path: '/open-api',
      name: 'OpenAPI',
      component: () => import('@/containers/OpenAPI.vue')
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
