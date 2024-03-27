<template>
  <FeatherTooltip
    :title="flowTooltipTitle(node)"
    :alignment="PointerAlignment.center"
    :placement="PopoverPlacement.top"
    v-slot="{ attrs, on }">
    <div v-if="hasIngressFlow(node) || hasEgressFlow(node)" v-bind="attrs" v-on="on" class="pointer">
      <font-awesome-icon v-if="hasIngressFlow(node)" :icon="'fa-solid fa-long-arrow-left'"></font-awesome-icon>
      <br v-if="hasIngressFlow(node) && hasEgressFlow(node)" style="height: 40px" />
      <font-awesome-icon v-if="hasEgressFlow(node)" :icon="'fa-solid fa-long-arrow-right'"></font-awesome-icon>
    </div>
  </FeatherTooltip>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { FeatherTooltip, PointerAlignment, PopoverPlacement } from '@featherds/tooltip'
import { hasIngressFlow, hasEgressFlow } from './utils'
import { Node } from '@/types'

defineProps({
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const flowTooltipTitle = (node: Node) => {
  if (hasIngressFlow(node) && hasEgressFlow(node)) {
    return 'Has Ingress/Egress Flows'
  } else if (hasIngressFlow(node)) {
    return 'Has Ingress Flows'
  } else if (hasEgressFlow(node)) {
    return 'Has Egress Flows'
  }

  return ''
}
</script>
