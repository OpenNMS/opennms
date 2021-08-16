import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'

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
  .component('Button', Button)
  .mount('#app')
