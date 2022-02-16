<template>
  <Container :script="externalJsUrl" :key="$route.fullPath" v-if="externalJsUrl" />
</template>

<script setup lang="ts">
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import * as Vue from 'vue/dist/vue.esm-bundler'
import { onMounted, watch, ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { addStylesheet } from '@/components/Plugin/utils'
import Container from '@/components/Plugin/Container.vue'
import { Plugin } from '@/types'
window.Vue = Vue

const store = useStore()
const router = useRouter()
const baseUrl = import.meta.env.VITE_BASE_REST_URL
const externalJsUrl = ref<string>('')

const enabledPlugins = computed<Plugin[]>(() => store.state.pluginModule.enabledPlugins)

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

const checkIfPluginIsEnabled = (id: string) => {
  for (const plugin of enabledPlugins.value) {
    if (plugin.extensionID === id)
      return
  }
  router.push('/plugin-management')
}

const addResources = () => {
  checkIfPluginIsEnabled(props.extensionId)

  externalJsUrl.value = `${baseUrl}/plugins/ui-extension/module/${props.extensionId}?path=${props.resourceRootPath}/${props.moduleFileName}`
  const externalCssUrl = `${baseUrl}/plugins/ui-extension/css/${props.extensionId}`
  addStylesheet(externalCssUrl)
}

watch(props, () => addResources())
onMounted(() => addResources())
</script>
