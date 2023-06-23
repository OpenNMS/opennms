import { createRouter, createWebHashHistory } from 'vue-router'
import { Plugin } from '@/types'
import DeviceConfigBackup from '@/containers/DeviceConfigBackup.vue'
import Home from '@/containers/Home.vue'
import FileEditor from '@/containers/FileEditor.vue'
import Resources from '@/components/Resources/Resources.vue'
import Graphs from '@/components/Resources/Graphs.vue'
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'

const { adminRole, filesystemEditorRole, dcbRole, rolesAreLoaded } = useRole()
const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()

// for backward compatibility with legacy OpenNMS plugins
// should eventually be removed when plugins are compliant with new schema
const isLegacyPlugin = (plugin: Plugin) => {
  if (plugin.extensionClass &&
      (plugin.extensionClass === 'org.opennms.plugins.cloud.ui.CloudUiExtension' ||
       plugin.menuEntry === 'Cloud Services') &&
       plugin.moduleFileName === 'uiextension.es.js') {
    return true
  }

  if (plugin.extensionClass &&
      (plugin.extensionClass === 'org.opennms.alec.ui.UIExtension' ||
       plugin.menuEntry === 'ALEC') &&
       plugin.moduleFileName === 'uiextension.es.js') {
    return true
  }

  return false
}

const router = createRouter({
  history: createWebHashHistory('/opennms/ui'),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      // for compatibility with legacy plugins
      // should be removed when all plugins have unique 'extensionId' and follow new pattern
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
        const checkRoles = () => {
          if (!filesystemEditorRole.value) {
            showSnackBar({ msg: 'No role access to file editor.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value) checkRoles()
        else whenever(rolesAreLoaded, () => checkRoles())
      }
    },
    {
      path: '/configuration',
      name: 'Configuration',
      component: () => import('@/containers/ProvisionDConfig.vue'),
      beforeEnter: (to, from) => {
        const checkRoles = () => {
          if (!adminRole.value) {
            showSnackBar({ msg: 'No role access to external requisitions.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value) checkRoles()
        else whenever(rolesAreLoaded, () => checkRoles())
      }
    },
    {
      path: '/logs',
      name: 'Logs',
      component: () => import('@/containers/Logs.vue'),
      beforeEnter: (to, from) => {
        const checkRoles = () => {
          if (!adminRole.value) {
            showSnackBar({ msg: 'No role access to logs.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value) checkRoles()
        else whenever(rolesAreLoaded, () => checkRoles())
      }
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
        const checkRoles = () => {
          if (!dcbRole.value) {
            showSnackBar({ msg: 'No role access to DCB.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value) checkRoles()
        else whenever(rolesAreLoaded, () => checkRoles())
      }
    },
    {
      path: '/scv',
      name: 'SCV',
      component: () => import('@/containers/SecureCredentialsVault.vue'),
      beforeEnter: (to, from) => {
        const checkRoles = () => {
          if (!adminRole.value) {
            showSnackBar({ msg: 'Must be admin to access SCV.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value) checkRoles()
        else whenever(rolesAreLoaded, () => checkRoles())
      }
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
    }
  ]
})

router.beforeEach(() => startSpinner())
router.afterEach(() => stopSpinner())
export default router
export { isLegacyPlugin }