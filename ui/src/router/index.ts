import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'
import Map from '@/containers/Map.vue'
import MapNodes from "@/components/Map/MapNodesGrid.vue";
import MapAlarms from "@/components/Map/MapAlarmsGrid.vue";

const router = createRouter({
  history: createWebHashHistory('/opennms/ui'),
  routes: [
    {
      path: '/',
      name: 'nodes',
      component: Nodes
    },
    {
      path: '/node/:id',
      name: 'Node Details',
      component: NodeDetails
    },
    {
      path: '/:pathMatch(.*)*', // catch other paths and redirect
      redirect: '/'
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
  ]
})

export default router

