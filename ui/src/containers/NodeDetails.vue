<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="items" />
    </div>
  </div>
  <div class="feather-row" style="flex-wrap: inherit;">
    <div class="feather-col-6">
      <NodeAvailabilityGraphVue />
      <InterfacesTabsVue />
    </div>
    <div class="feather-col-6">
      <EventsTable />
      <OutagesTable />
    </div>
  </div>
</template>
  
<script setup lang="ts">
import EventsTable from '@/components/Nodes/EventsTable.vue'
import OutagesTable from '@/components/Nodes/OutagesTable.vue'
import InterfacesTabsVue from '@/components/Nodes/InterfacesTabs.vue'
import NodeAvailabilityGraphVue from '@/components/Nodes/NodeAvailabilityGraph.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { BreadCrumb } from '@/types'
const items: BreadCrumb[] = [
  { label: 'Nodes', to: '/' },
  { label: 'Node Details', to: '#', position: 'last' }
]
</script>
