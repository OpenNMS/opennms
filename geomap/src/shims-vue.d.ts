/* eslint-disable */
declare module '@vue-leaflet/vue-leaflet';
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
