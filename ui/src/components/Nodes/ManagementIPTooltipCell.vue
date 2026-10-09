<template>
  <a
    v-if="ipInfo.label"
    v-onms-tooltip.top="tooltipTitle"
    :href="interfaceLink(baseHref, node.id, ipInfo.label)"
    class="pointer"
  >{{ ipInfo.label }}</a>
</template>

<script setup lang="ts">
import { IpInterface, Node } from '@/types'
import { PropType, computed } from 'vue'
import { IpInterfaceInfo } from '@/types'
import { useIpInterfaceQuery } from '@/components/Nodes/hooks/useIpInterfaceQuery'
import { useMenuStore } from '@/stores/menuStore'
import { interfaceLink } from '@/lib/linkUtils'

const { getBestIpInterfaceForNode } = useIpInterfaceQuery()
const menuStore = useMenuStore()

const baseHref = computed(() => menuStore.mainMenu.baseHref)

const props = defineProps({
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
