<template>
  <div class="card">
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`Status: ${nodeStatus}`" :severity="nodeSeverity" />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`Label: ${props.node?.label}`" severity="info" />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`ID: ${props.node?.id}`" severity="info" />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`Monitoring Location: ${props.node?.location}`" severity="info" />
      </div>
      <div v-if="foreignSourceLabel" class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="foreignSourceLabel" severity="info" data-test="foreign-source-tag" />
      </div>
      <div v-if="hasFlows" class="node-badge-wrapper">
        <OnmsTag
          v-onms-tooltip.top="{ value: flowsTooltip, class: 'node-flows-tooltip' }"
          class="node-chip tooltip-target"
          value="Flow data available"
          severity="success"
          data-test="flows-tag"
        />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag
          v-onms-tooltip.top="categoriesTooltip"
          class="node-chip"
          :class="{ 'tooltip-target': categoriesTooltip }"
          :value="categoriesLabel"
          severity="info"
          data-test="categories-tag"
        />
      </div>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { OnmsTag, OnmsTagSeverity } from '@opennms/onms-ui'

import { formatInDisplayZone } from '@/lib/displayTimeZone'
import { Node } from '@/types'
import { getNodeStatusString, hasRecentFlows } from './utils'

const props = defineProps({
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

// How many category names the badge shows before it gives up and defers to the tooltip.
const CATEGORIES_SHOWN = 2

const categories = computed(() => props.node?.categories ?? [])

const categoriesLabel = computed(() => {
  if (categories.value.length === 0) {
    return 'Categories: None'
  }

  const names = categories.value.map(category => category.name)
  const shown = names.slice(0, CATEGORIES_SHOWN).join(', ')

  return `Categories: ${shown}${names.length > CATEGORIES_SHOWN ? ' ...' : ''}`
})

// Only the names the badge had to drop are worth a tooltip -- with two or fewer it would repeat
// what is already on screen. Undefined rather than '' so the directive binds nothing at all,
// which is also what the tooltip-target cursor keys off.
const categoriesTooltip = computed(() =>
  categories.value.length > CATEGORIES_SHOWN
    ? categories.value.map(category => category.name).join('\n')
    : undefined)

// Only requisitioned nodes have a foreign source and id; the legacy page left the badge out for
// the rest, and so does this.
const foreignSourceLabel = computed<string | undefined>(() =>
  props.node?.foreignSource ? `FS:FID: ${props.node.foreignSource}:${props.node.foreignId}` : undefined)

const hasFlows = computed<boolean>(() => !!props.node && hasRecentFlows(props.node))

const lastFlow = (time: number | undefined) => (time ? formatInDisplayZone(time) : 'None')

// Each time on a line under its label: the tooltip is narrow, and a timestamp sharing a line with
// its label wraps mid-date. The node-flows-tooltip class (styled below) keeps each line whole.
const flowsTooltip = computed(() => [
  'Last ingress flow:',
  lastFlow(props.node?.lastIngressFlow),
  'Last egress flow:',
  lastFlow(props.node?.lastEgressFlow)
].join('\n'))

const nodeStatus = computed(() => {
  return getNodeStatusString(props.node)
})

// getNodeStatusString only ever returns these three, but anything it does not recognise reads
// as unknown rather than as a problem.
const SEVERITY_BY_STATUS: Record<string, OnmsTagSeverity> = {
  Active: 'success',
  Deleted: 'danger',
  Unknown: 'info'
}

const nodeSeverity = computed<OnmsTagSeverity>(() => SEVERITY_BY_STATUS[nodeStatus.value] ?? 'info')
</script>

<style lang="scss" scoped>
.card {
  margin-bottom: 1rem;

  .node-badge-wrapper {
    display: inline-block;
    margin-right: 0.5rem;
  }

  // Only set when there is a tooltip behind the badge: nothing here is clickable, so the cursor
  // is the only cue that hovering shows more.
  .tooltip-target {
    cursor: pointer;
  }
}
</style>

<style lang="scss">
// The tooltip teleports to <body>, so this cannot be scoped. A timestamp is wider than a tooltip's
// default max width and would break mid-date, so this one sizes to its longest line instead.
.node-flows-tooltip {
  max-width: none;

  .p-tooltip-text {
    white-space: pre;
  }
}
</style>
