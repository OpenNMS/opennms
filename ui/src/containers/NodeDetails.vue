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
      </div>
    </div>
    <div
      v-if="nodeLoaded"
      class="onms-row"
    >
      <div class="onms-col-12">
        <NodeDetailsHeader :node="nodeStore.node" />
        <NodeDetailsLinks
          :baseHref="baseHref"
          :node="nodeStore.node"
          :snmpPrimaryIpAddress="snmpPrimaryIpAddress"
          :existsInRequisition="nodeStore.existsInRequisition"
          :services="nodeStore.linkServices"
          :triggerNodeInfo="onNodeInfo"
        />
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
      <NodeDetailsAdditionalTab v-else-if="activeTab === 'additional'" />
      <NodeDetailsNetworkTab v-else />
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
import NodeDetailsAdditionalTab from '@/components/Nodes/NodeDetailsAdditionalTab.vue'
import NodeDetailsDialog from '@/components/Nodes/NodeDetailsDialog.vue'
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import NodeDetailsLinks from '@/components/Nodes/NodeDetailsLinks.vue'
import NodeDetailsMainTab from '@/components/Nodes/NodeDetailsMainTab.vue'
import NodeDetailsNetworkTab from '@/components/Nodes/NodeDetailsNetworkTab.vue'
import useRole from '@/composables/useRole'
import { useMenuStore } from '@/stores/menuStore'
import { NodeDetailsTab, useNodeStore } from '@/stores/nodeStore'
import { BreadCrumb, Node } from '@/types'

const menuStore = useMenuStore()
const nodeStore = useNodeStore()
const { provisionRole } = useRole()

const props = defineProps({
  id: {
    type: String
  }
})

const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)

const tabOptions: { label: string, value: NodeDetailsTab }[] = [
  { label: 'Main', value: 'main' },
  { label: 'Additional', value: 'additional' },
  { label: 'Network', value: 'network' }
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
  nodeStore.getNodeCriticalPath(props.id)
  nodeStore.getNodeLinkServices(props.id)

  await nodeStore.getNodeById({ id: props.id } as Node)
}

onMounted(fetchNode)

watch(() => props.id, fetchNode)

// The Edit in Requisition action needs to know whether the node is in its requisition, which the
// node payload does not say. Asked once the node has loaded, and only of a user who could use
// the answer; roles can arrive after the node, so both are followed.
watch(
  [() => (nodeStore.nodeLoaded ? nodeStore.node : undefined), provisionRole],
  ([node, canEdit]) => {
    if (node && canEdit) {
      nodeStore.getNodeExistsInRequisition(node)
    }
  },
  { immediate: true }
)

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
