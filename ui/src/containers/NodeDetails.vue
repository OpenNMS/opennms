<template>
  <div class="p-grid page">
    <div class="p-col-12">
      <Breadcrumb :home="home" :model="items">
        <template #item="{ item }">
          <router-link :to="item.to">{{ item.label }}</router-link>
        </template>
      </Breadcrumb>
    </div>
    <div class="p-col-6">
      <NodeAvailabilityGraphVue />
      <Panel header="Node Interfaces">
        <InterfacesTabsVue />
      </Panel>
    </div>
    <div class="p-col-6">
      <EventsTable />
      <OutagesTable />
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { ref } from 'vue'
import EventsTable from '@/components/Nodes/EventsTable.vue'
import OutagesTable from '@/components/Nodes/OutagesTable.vue'
import InterfacesTabsVue from '@/components/Nodes/InterfacesTabs.vue'
import NodeAvailabilityGraphVue from '@/components/Nodes/NodeAvailabilityGraph.vue'
import Breadcrumb from 'primevue/breadcrumb'
import Panel from 'primevue/panel'

const home = ref({ label: 'Nodes', to: '/' })
const items = ref([{ label: 'Node Details', to: '#' }])
</script>
  
<style lang="scss" scoped></style>
