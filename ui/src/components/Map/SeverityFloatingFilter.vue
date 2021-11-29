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
    app?.ctx.params.parentFilterInstance((instance: any) => {
      instance.onFloatingFilterChanged(null, null)
    })
    return
  }
  app?.ctx.params.parentFilterInstance((instance: any) => {
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

defineExpose({
  onParentModelChanged: onParentModelChanged
})
</script>
