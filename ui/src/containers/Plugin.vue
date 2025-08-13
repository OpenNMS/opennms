<template>
  <Container :script="externalJsUrl" :key="$route.fullPath" v-if="externalJsUrl" />
</template>

<script setup lang="ts">
import { addStylesheet, getCSSPath, getJSPath } from '@/components/Plugin/utils'
import Container from '@/components/Plugin/Container.vue'

const baseRestUrl = import.meta.env.VITE_BASE_REST_URL
const externalJsUrl = ref<string>('')

const props = defineProps({
  extensionId: {
    required: true,
    type: String
  },
  resourceRootPath: {
    required: true,
    type: String
  },
  moduleFileName: {
    required: true,
    type: String
  }
})

const addResources = () => {
  externalJsUrl.value = getJSPath(baseRestUrl, props.extensionId, props.resourceRootPath, props.moduleFileName)
  const externalCssUrl = getCSSPath(baseRestUrl, props.extensionId)
  addStylesheet(externalCssUrl)
}

watch(props, () => addResources())
onMounted(() => addResources())
</script>
