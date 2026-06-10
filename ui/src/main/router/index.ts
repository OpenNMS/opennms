///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { createRouter, createWebHashHistory, RouteLocationNormalized } from 'vue-router'
import { Plugin } from '@/types'
import DeviceConfigBackup from '@/containers/DeviceConfigBackup.vue'
import Home from '@/containers/Home.vue'
import FileEditor from '@/containers/FileEditor.vue'
import Graphs from '@/components/Resources/Graphs.vue'
import Resources from '@/components/Resources/Resources.vue'
import ZenithConnect from '@/containers/ZenithConnect.vue'
import ZenithConnectView from '@/components/ZenithConnect/ZenithConnectView.vue'
import ZenithConnectRegister from '@/components/ZenithConnect/ZenithConnectRegister.vue'
import ZenithConnectRegisterResult from '@/components/ZenithConnect/ZenithConnectRegisterResult.vue'
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { useMenuStore } from '@/stores/menuStore'
import { ActiveTabs, SnmpLookupEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'

const { adminRole, filesystemEditorRole, dcbRole, snmpRole, rolesAreLoaded } = useRole()
const menuStore = computed(() => useMenuStore())
const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()

// for backward compatibility with legacy OpenNMS plugins
// should eventually be removed when plugins are compliant with new schema
const isLegacyPlugin = (plugin: Plugin) => {
  if (
    plugin.extensionClass &&
    (plugin.extensionClass === 'org.opennms.plugins.cloud.ui.CloudUiExtension' ||
      plugin.menuEntry === 'Cloud Services') &&
    plugin.moduleFileName === 'uiextension.es.js'
  ) {
    return true
  }

  if (
    plugin.extensionClass &&
    (plugin.extensionClass === 'org.opennms.alec.ui.UIExtension' || plugin.menuEntry === 'ALEC') &&
    plugin.moduleFileName === 'uiextension.es.js'
  ) {
    return true
  }

  return false
}

const zenithConnectEnabled = computed<boolean>(() => menuStore.value?.mainMenu?.zenithConnectEnabled ?? false)

const checkSnmpRole = (from: RouteLocationNormalized, isLookup?: boolean) => {
  if (!snmpRole.value) {
    showSnackBar({ msg: 'Must have the proper SNMP role(s) to access SNMP Config.' })
    router.push(from.path)
    return
  }

  if (isLookup) {
    // SNMP Config auto-lookup mode. This sets the store to ensure the lookup tab is active and in lookup mode.
    // Then SnmpConfigLookupTab will get the 'ipAddress' and 'location' query params and perform the lookup.
    const store = useSnmpConfigStore()
    store.setActiveTab(ActiveTabs.Lookup)
    store.setSnmpLookupEditMode(SnmpLookupEditMode.Lookup)
  }
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

        if (rolesAreLoaded.value) {
          checkRoles()
        } else {
          whenever(rolesAreLoaded, () => checkRoles())
        }
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

        if (rolesAreLoaded.value) {
          checkRoles()
        } else {
          whenever(rolesAreLoaded, () => checkRoles())
        }
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

        if (rolesAreLoaded.value) {
          checkRoles()
        } else {
          whenever(rolesAreLoaded, () => checkRoles())
        }
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
      path: '/nodes',
      name: 'Nodes',
      component: () => import('@/containers/Nodes.vue')
    },
    {
      path: '/node/:id',
      name: 'Node Details',
      component: () => import('@/containers/NodeDetails.vue')
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

        if (rolesAreLoaded.value) {
          checkRoles()
        } else {
          whenever(rolesAreLoaded, () => checkRoles())
        }
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

        if (rolesAreLoaded.value) {
          checkRoles()
        } else {
          whenever(rolesAreLoaded, () => checkRoles())
        }
      }
    },
    {
      path: '/snmp-config',
      name: 'SNMP Config',
      component: () => import('@/containers/SnmpConfiguration.vue'),
      beforeEnter: (to, from) => {
        if (rolesAreLoaded.value) {
          checkSnmpRole(from)
        } else {
          whenever(rolesAreLoaded, () => checkSnmpRole(from))
        }
      }
    },
    {
      path: '/snmp-config/lookup',
      name: 'SNMP Config Lookup',
      component: () => import('@/containers/SnmpConfiguration.vue'),
      beforeEnter: (to, from) => {
        if (rolesAreLoaded.value) {
          checkSnmpRole(from, true)
        } else {
          whenever(rolesAreLoaded, () => checkSnmpRole(from, true))
        }
      }
    },
    {
      path: '/usage-statistics',
      name: 'Usage Statistics',
      component: () => import('@/containers/UsageStatistics.vue'),
      beforeEnter: (to, from) => {
        const checkRoles = () => {
          if (!adminRole.value) {
            showSnackBar({ msg: 'Must be admin to access Usage Statistics.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value) {
          checkRoles()
        } else {
          whenever(rolesAreLoaded, () => checkRoles())
        }
      }
    },
    {
      path: '/zenith-connect',
      name: 'ZenithConnect',
      component: ZenithConnect,
      beforeEnter: (to, from) => {
        const checkZenith = () => {
          if (!zenithConnectEnabled.value) {
            showSnackBar({ msg: 'Zenith Connect must be enabled.' })
            router.push(from.path)
          }
        }

        if (rolesAreLoaded.value && !!menuStore.value?.mainMenu?.baseHref) {
          checkZenith()
        } else {
          whenever(
            () => rolesAreLoaded && !!menuStore.value?.mainMenu?.baseHref,
            () => checkZenith()
          )
        }
      },
      children: [
        {
          path: '',
          name: 'View',
          component: ZenithConnectView
        },
        {
          path: 'register',
          name: 'Register',
          component: ZenithConnectRegister
        },
        {
          path: 'register-result',
          name: 'Register Result',
          component: ZenithConnectRegisterResult
        }
      ]
    },
    {
      path: '/event-config',
      name: 'Event Configuration',
      component: () => import('@/containers/EventConfiguration.vue')
    },
    {
      path: '/event-config/:id',
      name: 'Event Configuration Detail',
      component: () => import('@/containers/EventConfigurationDetail.vue')
    },
    {
      path: '/event-config/create',
      name: 'Event Configuration Create',
      component: () => import('@/containers/EventConfigEventCreate.vue')
    },
    {
      path: '/snmp-data-collection',
      name: 'SNMP Data Collection',
      component: () => import('@/containers/SnmpDataCollection.vue')
    },
    {
      // :id can be either a source id for an existing source, or 'create' for creating a new source
      path: '/snmp-data-collection/source/:id',
      name: 'SNMP Data Collection Source Detail',
      component: () => import('@/containers/SnmpDataCollectionSourceDetail.vue')
    },
    {
      path: '/snmp-data-collection/profile/:id',
      name: 'SNMP Data Collection Profile Detail',
      component: () => import('@/components/SnmpDataCollection/SnmpDataCollectionProfile/SnmpDataCollectionProfileDetails.vue')
    },
    {
      path: '/trapd-config',
      name: 'Trapd Configuration',
      component: () => import('@/containers/TrapdConfiguration.vue')
    },
    {
      path: '/primevue-test',
      name: 'PrimeVueTest',
      component: () => import('@/components/PrimeVueTest.vue')
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
