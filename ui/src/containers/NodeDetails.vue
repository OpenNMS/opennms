<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="items" />
    </div>
  </div>
  <div class="onms-row" style="flex-wrap: inherit; padding: 4px;">
    <div class="onms-col-6">
      <NodeAvailabilityGraph />
      <InterfacesTabs />
    </div>
    <div class="onms-col-6">
      <EventsTable />
      <OutagesTable />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'

import EventsTable from '@/components/Nodes/EventsTable.vue'
import OutagesTable from '@/components/Nodes/OutagesTable.vue'
import InterfacesTabs from '@/components/Nodes/InterfacesTabs.vue'
import NodeAvailabilityGraph from '@/components/Nodes/NodeAvailabilityGraph.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { BreadCrumb, Node } from '@/types'

const menuStore = useMenuStore()
const nodeStore = useNodeStore()

const props = defineProps({
  id: {
    type: String
  }
})

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const items = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Nodes', to: '/' },
  { label: 'Node Details', to: '#', position: 'last' }
])

const fetchNode = () => {
  if (props.id) {
    nodeStore.getNodeById({ id: props.id } as Node)
  }
}

onMounted(fetchNode)

watch(() => props.id, fetchNode)
</script>
