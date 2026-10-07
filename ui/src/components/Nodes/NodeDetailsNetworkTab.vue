<template>
  <div data-test="node-network-tab">
    <p
      v-if="state === 'loading'"
      class="network-message"
      data-test="network-loading"
    >Loading network information&hellip;</p>
    <p
      v-else-if="state === 'failed'"
      class="network-message"
      data-test="network-failed"
    >Unable to load network information for this node.</p>
    <p
      v-else-if="state === 'empty'"
      class="network-message"
      data-test="network-empty"
    >No LLDP, CDP, OSPF, IS-IS or bridge information has been discovered for this node.</p>
    <template v-else>
      <p
        v-if="partial"
        class="network-message"
        data-test="network-partial"
      >Some network information could not be loaded.</p>
      <!-- Link-layer discovery on the left, routing on the right. -->
      <div class="onms-row">
        <div class="onms-col-6">
          <NodeDetailsPanel
            v-if="elements?.lldp"
            title="LLDP"
            data-test="lldp-panel"
          >
            <NodeDetailsFieldList :fields="lldpFields(elements.lldp)" />
          </NodeDetailsPanel>
          <NodeDetailsPanel
            v-if="elements?.cdp"
            title="CDP"
            data-test="cdp-panel"
          >
            <NodeDetailsFieldList :fields="cdpFields(elements.cdp)" />
          </NodeDetailsPanel>
          <NodeDetailsPanel
            v-if="bridgeGroups.length > 0"
            title="Bridge"
            data-test="bridge-panel"
          >
            <div
              v-for="(group, index) in bridgeGroups"
              :key="index"
              class="bridge-group"
              data-test="bridge-group"
            >
              <div class="bridge-group-title">{{ group.title }}</div>
              <NodeDetailsFieldList :fields="group.fields" />
            </div>
          </NodeDetailsPanel>
        </div>
        <div class="onms-col-6">
          <NodeDetailsPanel
            v-if="elements?.ospf"
            title="OSPF"
            data-test="ospf-panel"
          >
            <NodeDetailsFieldList :fields="ospfFields(elements.ospf)" />
          </NodeDetailsPanel>
          <NodeDetailsPanel
            v-if="elements?.isis"
            title="IS-IS"
            data-test="isis-panel"
          >
            <NodeDetailsFieldList :fields="isisFields(elements.isis)" />
          </NodeDetailsPanel>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useEnlinkdStore } from '@/stores/enlinkdStore'
import { NodeEnlinkdElements } from '@/types/enlinkd'
import NodeDetailsFieldList from './NodeDetailsFieldList.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import { BridgeGroup, bridgeGroup, cdpFields, isisFields, lldpFields, ospfFields } from './nodeEnlinkdFields'
import useActiveNodeId from './hooks/useActiveNodeId'

// The legacy node page's Enlinkd boxes (LLDP, CDP, OSPF, IS-IS, bridge), each a panel shown only
// when Enlinkd found something for it. Fetched when the tab is first opened, and again as the
// page moves between nodes: like the Additional tab's tables, this follows the route id -- but
// only while the tab is showing (see useActiveNodeId).
const enlinkdStore = useEnlinkdStore()

const nodeId = useActiveNodeId()

const lastFetchFailed = ref(false)

// The store's slice may still be the previous node's while this one's is in flight.
const elements = computed<NodeEnlinkdElements | undefined>(() =>
  (enlinkdStore.nodeElementsNodeId === nodeId.value ? enlinkdStore.nodeElements : undefined))

const bridgeGroups = computed<BridgeGroup[]>(() => (elements.value?.bridges ?? []).map(bridgeGroup))

const hasAny = computed<boolean>(() => {
  const e = elements.value

  return !!e && !!(e.lldp || e.cdp || e.ospf || e.isis || e.bridges.length > 0)
})

const state = computed<'loading' | 'failed' | 'empty' | 'loaded'>(() => {
  if (!elements.value) {
    return lastFetchFailed.value ? 'failed' : 'loading'
  }

  if (!hasAny.value) {
    return lastFetchFailed.value ? 'failed' : 'empty'
  }

  return 'loaded'
})

// Something loaded, but not everything did.
const partial = computed<boolean>(() => lastFetchFailed.value && hasAny.value)

// Only the latest fetch may say whether loading failed: an earlier one, answering late, would
// otherwise report "superseded" as a failure.
let latestFetch = 0

const fetchElements = async () => {
  const id = nodeId.value

  if (!id) {
    return
  }

  const thisFetch = ++latestFetch
  lastFetchFailed.value = false
  const result = await enlinkdStore.getNodeElements(id)

  if (thisFetch === latestFetch) {
    lastFetchFailed.value = !result.success
  }
}

watch(nodeId, fetchElements, { immediate: true })

defineExpose({ fetchElements })
</script>

<style scoped lang="scss">
.network-message {
  margin: 0 0 1rem;
}

.bridge-group + .bridge-group {
  margin-top: 1.25rem;
}

.bridge-group-title {
  font-weight: bold;
  margin-bottom: 0.75rem;
}
</style>
