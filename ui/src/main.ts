///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements. See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version. You may not use this file except in
/// compliance with the License. You may obtain a copy of the
/// License at:
///
/// https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied. See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { createApp, h } from 'vue'
import { RouteRecordRaw } from 'vue-router'
import VueDiff from 'vue-diff'
import router, { isLegacyPlugin } from './router'
import { createPinia } from 'pinia'
import API from '@/services'
import App from './App.vue'

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import * as Vue from 'vue/dist/vue.esm-bundler'
import * as Pinia from 'pinia'
import * as VueRouter from 'vue-router'

import '@featherds/styles'
import '@featherds/styles/themes/open-light.css'

import 'vue-diff/dist/index.css'

import dateFormatDirective from './directives/v-date'
import { externalComponent, getJSPath } from './components/Plugin/utils'
import { IconDefinition, library } from '@fortawesome/fontawesome-svg-core'

// font-awesome
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// import specific icons and add to library
// TODO: Move to separate fil
// See: https://fontawesome.com/docs/web/use-with/vue/ and following
import {
faArrowLeftLong,
faArrowRightLong,
faBell,
faBellSlash,
faCalendar,
faCircle,
faCogs,
faInfoCircle,
faSearch,
faKey,
faLifeRing,
faMinusCircle,
faPlus,
faPlusCircle,
faQuestionCircle,
faSignOut,
faUser,
faUsers
} from '@fortawesome/free-solid-svg-icons'
const icons = [
faArrowLeftLong,
faArrowRightLong,
faBell,
faBellSlash,
faCalendar,
faCircle,
faCogs,
faPlusCircle,
faPlus,
faInfoCircle,
faKey,
faLifeRing,
faMinusCircle,
faQuestionCircle,
faSearch,
faSignOut,
faUser,
faUsers
] as IconDefinition[]
library.add(...icons);

// let plugins use state mngmnt / router
(window as any).Vue = Vue;
(window as any).Pinia = Pinia;
(window as any).VueRouter = VueRouter;
(window as any)['VRouter'] = router

// plugin scripts must be loaded before app to use their routes
const baseUrl = import.meta.env.VITE_BASE_REST_URL
const plugins = await API.getPlugins()

for (const plugin of plugins) {
if (!isLegacyPlugin(plugin)) {
// add this plugin to routes
// - route 'name' is 'Plugin-extensionId'. Plugins should add their routes as children of this named route
// - route 'path' has the Plugin extensionId as part of the segment rather than as a parameter,
// so it will only match the uniquely-named plugin
// Legacy plugins will add their routes to the 'Plugin' route, but only one legacy plugin
// will work at a time
const routeRecord : RouteRecordRaw =
{
path: `/plugins/${plugin.extensionId}/:resourceRootPath/:moduleFileName`,
name: `Plugin-${plugin.extensionId}`,
props: route => ({
extensionId: plugin.extensionId,
resourceRootPath: route.params.resourceRootPath,
moduleFileName: route.params.moduleFileName
}),
component: () => import('@/containers/Plugin.vue')
}

router.addRoute(routeRecord)
} else {
console.warn(`Warning: plugin '${plugin.menuEntry}' is a legacy plugin. Plugin will not work if any other legacy UI plugins are installed.`)
}

const js = getJSPath(baseUrl, plugin.extensionId, plugin.resourceRootPath, plugin.moduleFileName)
await externalComponent(js)
}

createApp({
render: () => h(App)
})
.use(VueDiff)
.use(router)
.use(createPinia())
.component('font-awesome-icon', FontAwesomeIcon)
.directive('date', dateFormatDirective)
.mount('#app')
