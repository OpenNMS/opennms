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
      <div class="header-controls">
        <OnmsSelectButton
          v-model="activeTab"
          :options="tabOptions"
          option-label="label"
          option-value="value"
          aria-label="Node details view"
          data-test="node-details-tab-select"
        />
        <NodeActionsDropdown
          v-if="nodeLoaded"
          :baseHref="baseHref"
          :node="nodeStore.node"
          :snmpPrimaryIpAddress="snmpPrimaryIpAddress"
          :triggerNodeInfo="onNodeInfo"
        />
      </div>
    </div>
    <div
      v-if="nodeLoaded"
      class="onms-row"
    >
      <div class="onms-col-12">
        <NodeDetailsHeader :node="nodeStore.node" />
      </div>
    </div>

    <!--
      KeepAlive so a tab mounts -- and its tables fetch -- only when first shown, then keeps its
      paging and sorting when the user switches away and back. Cached tabs still follow the route,
      so moving to another node refreshes them too.
    -->
    <KeepAlive>
      <NodeDetailsMainTab
        v-if="activeTab === 'main'"
        :node="nodeStore.node"
        :nodeLoaded="nodeLoaded"
        :baseHref="baseHref"
      />
      <NodeDetailsAdditionalTab v-else />
    </KeepAlive>

    <NodeDetailsDialog
      :visible="dialogVisible"
      :node="nodeStore.node"
      @close="dialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { OnmsSelectButton } from '@opennms/onms-ui'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import NodeActionsDropdown from '@/components/Nodes/NodeActionsDropdown.vue'
import NodeDetailsAdditionalTab from '@/components/Nodes/NodeDetailsAdditionalTab.vue'
import NodeDetailsDialog from '@/components/Nodes/NodeDetailsDialog.vue'
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import NodeDetailsMainTab from '@/components/Nodes/NodeDetailsMainTab.vue'
import { useEventStore } from '@/stores/eventStore'
import { useMenuStore } from '@/stores/menuStore'
import { NodeDetailsTab, useNodeStore } from '@/stores/nodeStore'
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

const tabOptions: { label: string, value: NodeDetailsTab }[] = [
  { label: 'Main', value: 'main' },
  { label: 'Additional', value: 'additional' }
]

const activeTab = computed<NodeDetailsTab>({
  get: () => nodeStore.nodeDetailsTab,
  set: tab => nodeStore.setNodeDetailsTab(tab)
})

// The node attributes now live behind the actions menu's Info... rather than in a panel of their
// own. The dialog takes the node it is given; this page only ever has one, so the handler
// ignores the node the menu hands back and shows the page's.
const dialogVisible = ref(false)

const onNodeInfo = () => {
  dialogVisible.value = true
}

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

    .header-controls {
      display: flex;
      align-items: center;
      gap: 0.75em;
    }
  }
}
</style>
