<template>
  <div class="onms-node-availability">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="items" />
      </div>
    </div>
    <div class="header">
      <h2>{{ heading }}</h2>
    </div>
    <p
      v-if="pageState === 'resolving'"
      data-test="node-availability-resolving"
    >
      Looking up node {{ lookupValue }}…
    </p>
    <NodeLookupMessage
      v-else-if="pageState === 'invalid' || pageState === 'notFound' || pageState === 'error'"
      :state="pageState"
      :value="lookupValue"
      :route-for="nodeAvailabilityRoute"
      @retry="followRoute"
    />
    <NodeAvailabilityPanel
      v-else-if="nodeStore.nodeLoaded"
      :node="nodeStore.node"
      :base-href="baseHref"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import NodeAvailabilityPanel from '@/components/Nodes/Availability/NodeAvailabilityPanel.vue'
import NodeLookupMessage from '@/components/Nodes/NodeLookupMessage.vue'
import { useNodeRouteResolution } from '@/components/Nodes/hooks/useNodeRouteResolution'
import { nodeAvailabilityRoute } from '@/lib/nodeDetailsRoute'
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

// The node is loaded for its label only: the panel fetches its availability by id.
const loadNode = (id: string) => nodeStore.getNodeById({ id } as Node)

// A foreign id is resolved and replaced with the node id on this page, not on Node Details.
const { lookupValue, pageState, followRoute } = useNodeRouteResolution(() => props.id ?? '', nodeAvailabilityRoute, loadNode)

const heading = computed<string>(() => {
  if (pageState.value !== 'node') {
    return 'Availability'
  }

  // The store starts with an empty node object, so the label is what says the fetch has landed.
  const node = nodeStore.node

  return node?.label ? `Availability for ${node.label} (id: ${node.id})` : 'Availability for'
})

const items = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu.homeUrl, isAbsoluteLink: true },
  { label: 'Nodes', to: '/nodes' },
  { label: 'Node Details', to: `/node/${encodeURIComponent(lookupValue.value)}` },
  { label: 'Availability', to: '#', position: 'last' }
])
</script>

<style lang="scss" scoped>
.onms-node-availability {
  padding: 1.5em;

  .header {
    margin-bottom: 1.25em;
  }
}
</style>
