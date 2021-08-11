import { createApp, h } from 'vue'
import App from './App.vue'
import router from './router'

createApp({
  render: () => h(App)
})
  .use(router)
  .mount('#app')
