<template>
  <div class="onms-node-details">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="items" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h2>{{ heading }}</h2>
      </div>
      <div
        v-if="pageState === 'node'"
        class="header-controls"
      >
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
    <p
      v-if="pageState === 'resolving'"
      data-test="node-details-resolving"
    >
      Looking up node {{ lookupValue }}…
    </p>
    <NodeLookupMessage
      v-else-if="pageState === 'invalid' || pageState === 'notFound' || pageState === 'error'"
      :state="pageState"
      :value="lookupValue"
      @retry="followRoute"
    />
    <template v-else>
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
            :data-requisition-checked="requisitionChecked || undefined"
          />
        </div>
      </div>

      <!--
        KeepAlive so a tab mounts -- and its tables fetch -- only when first shown, then keeps its
        paging and sorting when the user switches away and back. A hidden tab does not follow the
        route (see useActiveNodeId): it refreshes for the node on screen when it is shown again.
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
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { OnmsSelectButton } from '@opennms/onms-ui'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import NodeDetailsAdditionalTab from '@/components/Nodes/NodeDetailsAdditionalTab.vue'
import NodeDetailsDialog from '@/components/Nodes/NodeDetailsDialog.vue'
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import NodeDetailsLinks from '@/components/Nodes/NodeDetailsLinks.vue'
import NodeDetailsMainTab from '@/components/Nodes/NodeDetailsMainTab.vue'
import NodeDetailsNetworkTab from '@/components/Nodes/NodeDetailsNetworkTab.vue'
import NodeLookupMessage from '@/components/Nodes/NodeLookupMessage.vue'
import { nodeDetailsRoute, parseNodeRouteId } from '@/components/Nodes/nodeDetailsRoute'
import API from '@/services'
import useRole from '@/composables/useRole'
import { useMenuStore } from '@/stores/menuStore'
import { NodeDetailsTab, useNodeStore } from '@/stores/nodeStore'
import { BreadCrumb, Node } from '@/types'

const menuStore = useMenuStore()
const nodeStore = useNodeStore()
const { provisionRole } = useRole()
const route = useRoute()
const router = useRouter()

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

// Fetched alongside the node: the node payload carries no interfaces, and the IP Interfaces
// table holds only whatever page it is showing.
const snmpPrimaryIpAddress = computed<string | undefined>(() => nodeStore.snmpPrimaryIpAddress)

type PageState = 'node' | 'resolving' | 'invalid' | 'notFound' | 'error'

// The tabs read route.params.id themselves (useActiveNodeId), so they mount only once the route
// holds a canonical node id; a foreign id is resolved and replaced first.
const resolveState = ref<'resolving' | 'notFound' | 'error' | null>(null)
const routeId = computed(() => parseNodeRouteId(props.id ?? ''))
const lookupValue = computed(() => (routeId.value.kind === 'numeric' ? routeId.value.id : routeId.value.value))

const pageState = computed<PageState>(() => {
  const rid = routeId.value
  if (rid.kind === 'invalid') {
    return 'invalid'
  }
  if (rid.kind === 'foreign' || rid.id !== props.id) {
    return resolveState.value ?? 'resolving'
  }
  if (nodeStore.nodeLoadError === 'notFound') {
    return 'notFound'
  }
  return nodeStore.nodeLoadError === 'failed' ? 'error' : 'node'
})

const heading = computed<string>(() =>
  pageState.value === 'node' ? `Node Details for ${nodeStore.node?.label ?? ''}`.trimEnd() : 'Node Details')

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const items = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Nodes', to: '/nodes' },
  { label: 'Node Details', to: '#', position: 'last' }
])

const fetchNode = async (id: string) => {
  nodeStore.getNodeSnmpPrimaryInterface(id)
  nodeStore.getNodeCriticalPath(id)
  nodeStore.getNodeLinkServices(id)

  await nodeStore.getNodeById({ id } as Node)
}

// Canonical numeric id: load. Anything else: replace the route with the canonical id (keeping the
// query) and let this watcher run again for it.
let resolveRequest = 0
const followRoute = async () => {
  const rid = routeId.value
  const request = ++resolveRequest
  resolveState.value = null

  if (rid.kind === 'invalid') {
    return
  }
  if (rid.kind === 'numeric') {
    if (rid.id !== props.id) {
      router.replace({ ...nodeDetailsRoute(rid.id), query: route.query })
      return
    }
    await fetchNode(rid.id)
    return
  }

  resolveState.value = 'resolving'
  const result = await API.resolveNodeId(rid.value)
  if (request !== resolveRequest) {
    return
  }
  if (!result.success) {
    resolveState.value = 'error'
  } else if (result.payload === null || result.payload === undefined) {
    resolveState.value = 'notFound'
  } else {
    router.replace({ ...nodeDetailsRoute(result.payload), query: route.query })
  }
}

// Immediate, so the fetch starts (and the store clears an earlier visit's failure) before the
// first render.
watch(() => props.id, followRoute, { immediate: true })

// Whether the requisition check has answered for this node, so the links row's Edit in Requisition
// action is final. On the links row as data-requisition-checked: the smoke tests wait for it
// before they check that the action is absent.
const requisitionChecked = computed(() =>
  nodeStore.nodeLoaded && nodeStore.existsInRequisitionNodeId === nodeStore.node?.id)

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
