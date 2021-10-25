import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import PrimeVue from 'primevue/config'
import Notifications from "@kyvg/vue3-notification"

import 'primevue/resources/themes/saga-blue/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'

createApp({
  render: () => h(App)
})
  .use(router)
  .use(store)
  .use(PrimeVue)
  .use(Notifications)
  .mount('#app')
