<template>
  <div class="onms-node-details">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="items" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h2>Node Details for {{ nodeStore.node?.label }}</h2>
      </div>
      <NodeActionsDropdown
        v-if="nodeLoaded"
        :baseHref="baseHref"
        :node="nodeStore.node"
      />
    </div>
    <template v-if="nodeLoaded">
      <div class="onms-row">
        <div class="onms-col-12">
          <NodeDetailsHeader :node="nodeStore.node" />
        </div>
      </div>
      <div class="onms-row" style="flex-wrap: inherit; padding: 4px;">
        <div class="onms-col-6">
          <NodeSnmpAttributes :node="nodeStore.node" />
        </div>
        <div class="onms-col-6">
          <NodeCategoriesPanel :node="nodeStore.node" :base-href="baseHref" />
          <NodeNotificationsPanel :node="nodeStore.node" :base-href="baseHref" />
        </div>
      </div>
    </template>
    <div class="onms-row" style="flex-wrap: inherit; padding: 4px;">
      <div class="onms-col-6">
        <NodeAvailabilityGraph
          v-if="nodeLoaded"
          :node="nodeStore.node"
          :base-href="baseHref"
        />
        <InterfacesTabs />
      </div>
      <div class="onms-col-6">
        <EventsTable />
        <OutagesTable />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import NodeActionsDropdown from '@/components/Nodes/NodeActionsDropdown.vue'
import EventsTable from '@/components/Nodes/EventsTable.vue'
import InterfacesTabs from '@/components/Nodes/InterfacesTabs.vue'
import NodeAvailabilityGraph from '@/components/Nodes/NodeAvailabilityGraph.vue'
import NodeCategoriesPanel from '@/components/Nodes/NodeCategoriesPanel.vue'
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import NodeNotificationsPanel from '@/components/Nodes/NodeNotificationsPanel.vue'
import OutagesTable from '@/components/Nodes/OutagesTable.vue'
import NodeSnmpAttributes from '@/components/Nodes/NodeSnmpAttributes.vue'
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

const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)

// Panels that read the node wait for a real one: the store's node starts as {}, which is
// truthy and answers undefined for every field. The events, outages and interface tables fetch
// by node id themselves and do not need it.
const nodeLoaded = computed<boolean>(() => nodeStore.nodeLoaded)
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

<style lang="scss" scoped>
.onms-node-details {
  padding: 1.5em;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.25em;
    padding: 0;
  }
}
</style>
