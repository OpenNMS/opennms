import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import VueDiff from 'vue-diff'
import { createPinia } from 'pinia'
import API from '@/services'

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import * as Vue from 'vue/dist/vue.esm-bundler'
import * as Pinia from 'pinia'
import * as Vuex from 'vuex'
import * as VueRouter from 'vue-router'

import '@featherds/styles'
import '@featherds/styles/themes/open-light.css'

import 'vue-diff/dist/index.css'

import dateFormatDirective from './directives/v-date'
import { externalComponent, getJSPath } from './components/Plugin/utils'
import { library } from '@fortawesome/fontawesome-svg-core'

// font-awesome
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// import specific icons and add to library
// TODO: Move to separate fil
// See: https://fontawesome.com/docs/web/use-with/vue/ and following
import {
  faBell,
  faBellSlash,
  faCalendar,
  faCogs,
  faUsers
} from '@fortawesome/free-solid-svg-icons'
const icons = [
  faBell,
  faBellSlash,
  faCalendar,
  faCogs,
  faUsers
]
library.add(...icons);


// let plugins use state mngmnt / router
(window as any).Vue = Vue;
(window as any).Pinia = Pinia;
(window as any).Vuex = Vuex;
(window as any).VueRouter = VueRouter;
(window as any)['VRouter'] = router;

// plugin scripts must be loaded before app to use their routes
const baseUrl = import.meta.env.VITE_BASE_REST_URL
const plugins = await API.getPlugins()

for (const plugin of plugins) {
  const js = getJSPath(baseUrl, plugin.extensionId, plugin.resourceRootPath, plugin.moduleFileName)
  await externalComponent(js)
}

createApp({
  render: () => h(App)
})
  .use(VueDiff)
  .use(router)
  .use(store)
  .use(createPinia())
  .component('font-awesome-icon', FontAwesomeIcon)
  .directive('date', dateFormatDirective)
  .mount('#app')
