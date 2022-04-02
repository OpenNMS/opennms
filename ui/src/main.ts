import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import VueDiff from 'vue-diff'

import '@featherds/styles'

import 'vue-diff/dist/index.css'

import dateFormatDirective from './directives/v-date'

createApp({
  render: () => h(App)
})
  .use(VueDiff)
  .use(router)
  .use(store)
  .directive('date', dateFormatDirective)
  .mount('#app')
