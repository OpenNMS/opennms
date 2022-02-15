<template>
  Plugin container
  <Plugin />
</template>

<script setup lang="ts">
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import * as Vue from 'vue/dist/vue.esm-bundler'
import { defineAsyncComponent, onMounted } from 'vue'
import { externalComponent, addStylesheet } from '@/components/Plugin/utils'
window.Vue = Vue

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

const baseUrl = import.meta.env.VITE_BASE_REST_URL
const externalJsUrl = `${baseUrl}/plugins/ui-extension/module/${props.extensionId}?path=${props.resourceRootPath}/${props.moduleFileName}`
const externalCssUrl = `${baseUrl}/plugins/ui-extension/css/${props.extensionId}`
const Plugin: any = defineAsyncComponent(() => externalComponent(externalJsUrl))

onMounted(() => addStylesheet(externalCssUrl))
</script>
