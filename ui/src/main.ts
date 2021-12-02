import { createApp, h, reactive} from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import "@featherds/styles"
import "@featherds/styles/themes/open-light.css"

const GStore = reactive({flashMessage: ''})

createApp({
  render: () => h(App)
})
  .use(router)
  .use(store)
  .provide('GStore', GStore)
  .mount('#app')
