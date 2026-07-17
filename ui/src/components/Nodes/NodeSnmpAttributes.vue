<template>
  <div class="card">
    <div class="title headline3">SNMP Attributes</div>
    <div class="onms-row" v-for="item in items" :key="item.label">
      <div class="onms-col-3">
        <span class="attribute-label">{{ item.label }}:</span>
      </div>
      <div class="onms-col-9">
        <span class="attribute-value">{{ item.value }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
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

.card {
  background: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.08);
  padding: 15px;
  margin-bottom: 15px;

  .title {
    padding: 5px 10px 0px 10px;
  }

  .attribute-label {
    font-weight: bold;
  }

  // .attribute-value {
  // }
}
</style>
