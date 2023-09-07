<template>
  <FeatherDialog :modelValue="visible" relative :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <div class="feather-row" v-for="item in nodeItems" :key="item.label">
        <div class="feather-col-4">
          <span class="label">{{ item.label }}</span>
        </div>
        <div class="feather-col-8">
          <a v-if="item.link" :href="item.link">{{ item.text }}</a>
          <span v-else>{{ item.text }}</span>
        </div>
      </div>
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { FeatherDialog } from '@featherds/dialog'
import { hasEgressFlow, hasIngressFlow } from './utils'
import { Node } from '@/types'

const props = defineProps({
  computeNodeLink: {
    required: true,
    type: Function as PropType<(id: number | string) => string>
  },
  visible: {
    required: true,
    type: Boolean
  },
  node: {
    required: false,
    type: Object as PropType<Node>
  }
})

defineEmits(['close'])

const labels = reactive({
  title: 'Node Details',
  close: 'Close'
})

const EMPTY = '--'

const nodeItems = computed(() => {
  return [
    { label: 'Node ID', text: props.node?.id, link: props.computeNodeLink(props.node?.id || 0) },
    { label: 'Node Label', text: props.node?.label, link: props.computeNodeLink(props.node?.id || 0) },
    { label: 'Location', text: props.node?.location },
    { label: 'FS:FID', text: `${props.node?.foreignSource}:${props.node?.foreignId}` },
    { label: 'Sys Contact', text: props.node?.sysContact || EMPTY },
    { label: 'Sys Location', text: props.node?.sysLocation || EMPTY },
    { label: 'Sys Description', text: props.node?.sysDescription || EMPTY },
    { label: 'Flows', text: flowsText(props.node) }
  ]
})

const flowsText = (node?: Node) => {
  if (node) {
    const a = [hasIngressFlow(node) ? 'Ingress' : '', hasEgressFlow(node) ? 'Egress' : ''].filter(x => x.length > 0)

    if (a.length === 2) {
      return `${a[0]} / ${a[1]}`
    } else if (a.length === 1) {
      return a[0]
    }
  }

  return EMPTY
}
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
