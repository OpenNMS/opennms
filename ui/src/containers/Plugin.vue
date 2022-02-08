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
  id: {
    required: true,
    type: String
  }
})

const baseUrl = import.meta.env.VITE_BASE_V2_URL
const externalJsUrl = `${baseUrl}/plugins/ui-extension/js/${props.id}`
const externalCssUrl = `${baseUrl}/plugins/ui-extension/css/${props.id}`
const Plugin: any = defineAsyncComponent(() => externalComponent(externalJsUrl))

onMounted(() => addStylesheet(externalCssUrl))
</script>
