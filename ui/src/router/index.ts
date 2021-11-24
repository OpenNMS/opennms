import { createRouter, createWebHashHistory } from 'vue-router'
import Nodes from '@/containers/Nodes.vue'
import NodeDetails from '@/containers/NodeDetails.vue'

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
      component: () => import('@/containers/Map.vue'),
      children: [
        {
          path: "",
          name: "MapNodes",
          component: () => import('@/components/Map/MapNodesGrid.vue')
        },
        {
          path: "alarms",
          name: "MapAlarms",
          component: () => import('@/components/Map/MapAlarmsGrid.vue')
        },
      ],
    },
  ]
})

export default router

