import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import '@featherds/styles'

import dateFormatDirective from './directives/v-date'

createApp({
  render: () => h(App)
})
  .use(router)
  .use(store)
  .directive('date', dateFormatDirective)
  .mount('#app')
