import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";
import Map from "../views/Map.vue";
import MapNodes from "../views/map/MapNodesGrid.vue";
import MapAlarms from "../views/map/MapAlarmsGrid.vue";

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home,
  },
  {
    path: "/map",
    name: "Map",
    component: Map,
    children: [
      {
        path: "",
        name: "MapNodes",
        component: MapNodes,
      },
      {
        path: "alarms",
        name: "MapAlarms",
        component: MapAlarms,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory('/'),
  routes,
});

export default router;
