import { createRouter, createWebHashHistory } from 'vue-router'
import FileEditor from '@/containers/FileEditor.vue'
import Logs from '@/containers/Logs.vue'
import Resources from '@/components/Resources/Resources.vue'
import Graphs from '@/components/Resources/Graphs.vue'

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
      path: '/plugin-management',
      name: 'Plugin Management',
      component: () => import('@/containers/PluginManagement.vue')
    },
    {
      path: '/plugins/:extensionId/:resourceRootPath/:moduleFileName',
      name: 'Plugin',
      props: true,
      component: () => import('@/containers/Plugin.vue')
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
      component: FileEditor
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
      path: '/resource-graphs',
      name: 'ResourceGraphs',
      component: () => import('@/containers/ResourceGraphs.vue'),
      children: [
        {
          path: '',
          name: 'Resources',
          component: Resources
        },
        {
          path: 'graphs/:label/:singleGraphDefinition/:singleGraphResourceId',
          component: Graphs,
          props: true
        },
        {
          path: 'graphs',
          name: 'Graphs',
          component: Graphs
        }
      ]
    },
    {
      path: '/open-api',
      name: 'OpenAPI',
      component: () => import('@/containers/OpenAPI.vue')
    },
    {
      path: '/resource-graphs',
      name: 'ResourceGraphs',
      component: () => import('@/containers/ResourceGraphs.vue'),
      children: [
        {
          path: '',
          name: 'Resources',
          component: () => import('@/components/Resources/Resources.vue')
        },
        {
          path: 'graphs/:label/:singleGraphDefinition/:singleGraphResourceId',
          component: () => import('@/components/Resources/Graphs.vue'),
          props: true
        },
        {
          path: 'graphs',
          name: 'Graphs',
          component: () => import('@/components/Resources/Graphs.vue')
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
