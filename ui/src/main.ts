import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Notifications from "@kyvg/vue3-notification"
import "@featherds/styles"
import "@featherds/styles/themes/open-light.css"

createApp({
  render: () => h(App)
})
  .use(router)
  .use(store)
  .use(Notifications)
  .mount('#app')
