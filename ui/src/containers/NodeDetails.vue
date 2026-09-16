<template>
  <div class="onms-node-details">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="items" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h2>Node Details for {{ nodeTitle }}</h2>
      </div>
      <NodeActionsDropdown
        v-if="nodeLoaded"
        :baseHref="baseHref"
        :node="nodeStore.node"
        :snmpPrimaryIpAddress="snmpPrimaryIpAddress"
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
import { useEventStore } from '@/stores/eventStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { BreadCrumb, Node } from '@/types'

const eventStore = useEventStore()
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

// A node that failed to load has no label to show -- a nonexistent id 404s -- while one still
// in flight has nothing to say yet, so only the failure is spelled out.
// Fetched alongside the node: the node payload carries no interfaces, and the IP Interfaces
// table holds only whatever page it is showing.
const snmpPrimaryIpAddress = computed<string | undefined>(() => nodeStore.snmpPrimaryIpAddress)

const nodeTitle = computed<string>(() => {
  if (nodeStore.nodeLoadFailed) {
    return 'N/A'
  }

  return nodeStore.node?.label ?? ''
})
const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const items = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Nodes', to: '/' },
  { label: 'Node Details', to: '#', position: 'last' }
])

const fetchNode = async () => {
  if (!props.id) {
    return
  }

  nodeStore.getNodeSnmpPrimaryInterface(props.id)

  await nodeStore.getNodeById({ id: props.id } as Node)

  // The events table fetches by node id itself and only replaces its rows on success, so a
  // node that could not be fetched would otherwise keep the previous node's events on screen.
  // Events are the event store's to clear, so the page asks rather than reaching across.
  if (nodeStore.nodeLoadFailed) {
    eventStore.clearEvents()
  }
}

onMounted(fetchNode)

watch(() => props.id, fetchNode)

defineExpose({ fetchNode })
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
