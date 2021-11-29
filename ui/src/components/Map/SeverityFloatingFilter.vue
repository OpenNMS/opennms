<template>
  <div>
    &ge;
    <select :style="{ width: '70px' }" v-model="currentValue" @change="onSelectionChanged()">
      <option v-for="option in severities" :value="option" :key="option">{{ option }}</option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { ref, getCurrentInstance } from 'vue'
const app: any = getCurrentInstance()
const params = app?.ctx.params || app?.data.params
const severities = [
  'Normal',
  'Warning',
  'Minor',
  'Major',
  'Critical'
]
const currentValue = ref("")

const onSelectionChanged = () => {
  if (currentValue.value === '') {
    // clear the filter
    params.parentFilterInstance((instance: any) => {
      instance.onFloatingFilterChanged(null, null)
    })
    return
  }
  params.parentFilterInstance((instance: any) => {
    instance.onFloatingFilterChanged('contains', currentValue.value)
  })
}

// used by agGrid
const onParentModelChanged = (parentModel: any) => {
  // When the filter is empty we will receive a null value here
  if (!parentModel) {
    currentValue.value = ''
  } else {
    currentValue.value = parentModel.filter
  }
}

defineExpose({ onParentModelChanged })
</script>
