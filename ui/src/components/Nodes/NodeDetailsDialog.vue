<template>
  <FeatherDialog :modelValue="visible" relative :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <div class="feather-row" v-for="item in nodeItems" :key="item.label">
        <div class="feather-col-4">
          <span class="label">{{ item.label }}</span>
        </div>
        <div class="feather-col-8">
          {{ item.text }}
        </div>
      </div>
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { FeatherDialog } from '@featherds/dialog'
import { Node } from '@/types'

const props = defineProps({
  visible: {
    required: true,
    type: Boolean
  },
  node: {
    required: false,
    type: Object as PropType<Node>
  }
})

const labels = reactive({
  title: 'Node Details',
  close: 'Close'
})

const nodeItems = computed(() => {
  return [
    { label: 'Node ID', text: props.node?.id },
    { label: 'Node Label', text: props.node?.label },
    { label: 'FS:FID', text: `${props.node?.foreignSource}:${props.node?.foreignId}` }
  ]
})
</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  min-width: 550px;
  position: relative;
}

.label {
  font-weight: bold;
}
</style>
