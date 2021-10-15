/* eslint-disable */
declare module 'leaflet/dist/leaflet-src.esm'
declare module '@vue-leaflet/vue-leaflet';
declare module 'leaflet.awesome-markers';
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
