<template>
  <FeatherTooltip
    :title="flowTooltipTitle(node)"
    :alignment="PointerAlignment.center"
    :placement="PopoverPlacement.top"
    v-slot="{ attrs, on }">
    <div v-if="hasIngressFlow(node) || hasEgressFlow(node)" v-bind="attrs" v-on="on" class="pointer">
      <FeatherIcon v-if="hasIngressFlow(node)" :icon="ArrowBack" class="flow-icon" />
      <br v-if="hasIngressFlow(node) && hasEgressFlow(node)" style="height: 40px" />
      <FeatherIcon v-if="hasEgressFlow(node)" :icon="ArrowBack" class="flow-icon egress" />
    </div>
  </FeatherTooltip>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { FeatherTooltip, PointerAlignment, PopoverPlacement } from '@featherds/tooltip'
import { FeatherIcon } from '@featherds/icon'
import ArrowBack from '@featherds/icon/navigation/ArrowBack'
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

<style lang="scss" scoped>

.flow-icon {
  font-size: 1.5em;

  &.egress {
    transform: scaleX(-1);
  }
}
</style>
