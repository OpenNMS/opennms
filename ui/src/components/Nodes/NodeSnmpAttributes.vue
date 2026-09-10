<template>
  <NodeDetailsPanel title="SNMP Attributes">
    <div class="onms-row" v-for="item in items" :key="item.label">
      <div class="onms-col-3">
        <span class="attribute-label">{{ item.label }}:</span>
      </div>
      <div class="onms-col-9">
        <span class="attribute-value">{{ item.value }}</span>
      </div>
    </div>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import { Node } from '@/types'

const props = defineProps({
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const items = computed(() => [
  {
    label: 'Name',
    value: props.node?.sysName || 'N/A'
  },
  {
    label: 'Sys Object ID',
    value: props.node?.sysObjectId || 'N/A'
  },
  {
    label: 'Sys Location',
    value: props.node?.sysLocation || 'N/A'
  },
  {
    label: 'Contact',
    value: props.node?.sysContact || 'N/A'
  },
  {
    label: 'Description',
    value: props.node?.sysDescription || 'N/A'
  }
])
</script>

<style lang="scss" scoped>
.attribute-label {
  font-weight: bold;
}
</style>
