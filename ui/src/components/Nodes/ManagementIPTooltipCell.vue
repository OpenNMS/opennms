<template>
  <a
    v-if="ipInfo.label"
    v-tooltip.top="tooltipTitle"
    :href="computeNodeIpInterfaceLink(node.id, ipInfo.label)"
    class="pointer"
  >{{ ipInfo.label }}</a>
</template>

<script setup lang="ts">
import { IpInterface, Node } from '@/types'
import { PropType, computed } from 'vue'
import { IpInterfaceInfo } from '@/types'
import { useIpInterfaceQuery } from '@/components/Nodes/hooks/useIpInterfaceQuery'

const { getBestIpInterfaceForNode } = useIpInterfaceQuery()

const props = defineProps({
  computeNodeIpInterfaceLink: {
    required: true,
    type: Function as PropType<(nodeId: number | string, ipAddress: string) => string>
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  },
  nodeToIpInterfaceMap: {
    required: true,
    type: Object as PropType<Map<string, IpInterface[]>>
  }
})

const ipInfo = computed<IpInterfaceInfo>(() => getBestIpInterfaceForNode(props.node.id, props.nodeToIpInterfaceMap))

const tooltipTitle = computed<string>(() => {
  const managed = ipInfo.value.managed ? 'Managed' : 'Unmanaged'
  const primary = ipInfo.value.primaryLabel
  return [managed, primary].join(', ')
})
</script>

<style lang="scss" scoped>
.pointer {
  cursor: pointer;
}
</style>
