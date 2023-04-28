<template>
  <!-- eslint-disable-next-line vue/no-mutating-props -->
  <FeatherDialog v-model="props.visible" relative :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <slot name="content" />
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { FeatherDialog } from '@featherds/dialog'

const props = defineProps({
  subtitle: {
    required: false,
    type: String
  },
  visible: {
    required: true,
    type: Boolean
  }
})

const labels = reactive({
  title: 'Usage Statistics',
  close: 'Close'
})

watchEffect(() => {
  labels.title = props.subtitle ? `Usage Statistics: ${props.subtitle}` : 'Usage Statistics'
})
</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  max-height: 500px;
  min-width: 550px;
  overflow: auto;
  position: relative;
}
</style>
