<template>
  <LPopup>
    <h3>Node: <a :href="`${baseNodeUrl}${node.id}`" target="_blank">{{ node.label }}</a></h3>

    <span class="larger-icon"><FeatherIcon :icon="Location" /></span>
    {{ latitude }}, {{ longitude }}
    <br />
    <a :href="getTopologyLink(node)">View in Topology Map</a>
    <br />
    <br />
    <div class="flex">
      <div>IP Address:</div>
      <div>{{ ipAddress || 'N/A' }}</div>
    </div>
    <div class="flex">
      <div>IP Address:</div>
      <div>{{ ipAddress || 'N/A' }}</div>
    </div>
    <div class="flex">
      <div>Description:</div>
      <div>{{ node.assetRecord.description || 'N/A' }}</div>
    </div>
    <div class="flex">
      <div>Maint. Contract:</div>
      <div>{{ node.assetRecord.maintcontract || 'N/A' }}</div>
    </div>
    <div class="flex">
      <div>Severity:</div>
      <div>
        <span :class="['alarm-severity', `${nodeLabelToAlarmSeverity(node.label)}`]">
          {{ nodeLabelToAlarmSeverity(node.label) }}
        </span>
      </div>
    </div>
    <div class="flex">
      <div>Category:</div>
      <div>{{ node.categories.length ? node.categories[0].name : 'N/A' }}</div>
    </div>
  </LPopup>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import Location from '@featherds/icon/action/Location'
import { LPopup } from '@vue-leaflet/vue-leaflet'
import { Node } from '@/types'
import { stringToFixedFloat } from './utils'

const props = defineProps({
  baseHref: { type: Object as PropType<string> },
  baseNodeUrl: { type: Object as PropType<string> },
  node: { type: Object as PropType<Node>, default: () => { return }},
  ipAddress: { type: Object as PropType<string> },
  nodeLabelToAlarmSeverity: { type: Function as PropType<(label: string) => string>, required: true }
})

const latitude = computed(() => stringToFixedFloat(props.node.assetRecord.latitude, 6))
const longitude = computed(() => stringToFixedFloat(props.node.assetRecord.longitude, 6))

const getTopologyLink = (node: Node) => {
  return `${props.baseHref}topology?provider=Enhanced Linkd&focus-vertices=${node.id}`
}
</script>

<style lang="scss" scoped>
.larger-icon {
  font-size: 1.35em;
}
</style>
