import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import DeviceConfigBackup from '@/containers/DeviceConfigBackup.vue'
import FileEditor from '@/containers/FileEditor.vue'
import Resources from '@/components/Resources/Resources.vue'
import Graphs from '@/components/Resources/Graphs.vue'
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'

const { adminRole, dcbRole } = useRole()
const { showSnackBar } = useSnackbar()

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
      component: () => import('@/containers/NodeDetails.vue')
    },
    {
      path: '/plugins/:extensionId/:resourceRootPath/:moduleFileName',
      name: 'Plugin',
      props: true,
      component: () => import('@/containers/Plugin.vue')
    },
    {
      path: '/file-editor',
      name: 'FileEditor',
      component: FileEditor,
      beforeEnter: (to, from) => {
        if (!adminRole.value) {
          showSnackBar({ msg: 'No route access.'})
          return from.path
        }
      },
    },
    {
      path: '/logs',
      name: 'Logs',
      component: () => import('@/containers/Logs.vue'),
      beforeEnter: (to, from) => {
        if (!adminRole.value) {
          showSnackBar({ msg: 'No route access.'})
          return from.path
        }
      },
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
      path: '/device-config-backup',
      name: 'DeviceConfigBackup',
      component: DeviceConfigBackup,
      beforeEnter: (to, from) => {
        if (!dcbRole.value) {
          showSnackBar({ msg: 'No route access.'})
          return from.path
        }
      },
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

export default router
