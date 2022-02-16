<template>
  <Container :script="externalJsUrl" :key="$route.fullPath" v-if="externalJsUrl" />
</template>

<script setup lang="ts">
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import * as Vue from 'vue/dist/vue.esm-bundler'
import { onMounted, watch, ref } from 'vue'
import { addStylesheet } from '@/components/Plugin/utils'
import Container from '@/components/Plugin/Container.vue'
window.Vue = Vue

const baseUrl = import.meta.env.VITE_BASE_REST_URL
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
  externalJsUrl.value = `${baseUrl}/plugins/ui-extension/module/${props.extensionId}?path=${props.resourceRootPath}/${props.moduleFileName}`
  const externalCssUrl = `${baseUrl}/plugins/ui-extension/css/${props.extensionId}`
  addStylesheet(externalCssUrl)
}

watch(props, () => addResources())
onMounted(() => addResources())
</script>
