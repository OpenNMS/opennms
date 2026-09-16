<template>
  <LPopup :options="mapPopupOptions">
    <h3>Node: <a :href="`${baseNodeUrl}${node.id}`" target="_blank">{{ node.label }}</a></h3>

    <span class="larger-icon"><OnmsIcon :icon="Location" /></span>
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
import { PropType, computed } from 'vue'
import { OnmsIcon } from '@opennms/onms-ui'
import Location from '@opennms/onms-ui/icons/action/Location.vue'
import { LPopup } from '@vue-leaflet/vue-leaflet'
import { Node } from '@/types'
import { mapPopupOptions } from './utils'

const props = defineProps({
  baseHref: { type: String, required: true },
  baseNodeUrl: { type: String, required: true },
  node: { type: Object as PropType<Node>, default: () => {
    return
  } },
  ipAddress: { type: String },
  nodeLabelToAlarmSeverity: { type: Function as PropType<(label: string) => string>, required: true }
})

const numToFixedFloat = (num: number | null, decimalPlaces: number): string => {
  if (num === null || num === undefined) {
    return 'N/A'
  }
  return num.toFixed(decimalPlaces)
}

const latitude = computed(() => numToFixedFloat(props.node.assetRecord.latitude ?? null, 6))
const longitude = computed(() => numToFixedFloat(props.node.assetRecord.longitude ?? null, 6))

const getTopologyLink = (node: Node) => {
  return `${props.baseHref}topology?provider=Enhanced Linkd&focus-vertices=${node.id}`
}
</script>

<style lang="scss" scoped>
.larger-icon {
  font-size: 1.35em;
}
</style>
