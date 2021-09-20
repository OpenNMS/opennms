import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
// import {
//     LMap,
//     LTileLayer,
//     LMarker
//   } from "@vue-leaflet/vue-leaflet";

createApp(App).use(store).use(router).mount("#app");
// .use(LMap)
// .use(LTileLayer)
// .use(LMarker)

