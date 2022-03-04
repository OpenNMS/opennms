<template>
  <!-- eslint-disable-next-line vue/no-mutating-props -->
  <FeatherDialog v-model="visible" :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <slot name="content" />
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { toRefs, ref } from 'vue'
import { FeatherDialog } from '@featherds/dialog'

const props = defineProps({
  visible: {
    required: true,
    type: Boolean
  },
  deviceName: {
    required: true,
    type: String
  }
})

const { deviceName } = toRefs(props)

const labels = ref({
  title: `DeviceName: ${deviceName.value}`,
  close: 'Close'
})
</script>

<style scoped lang="scss">
.content {
  width: 500px;
}
</style>
