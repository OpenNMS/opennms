<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <rapi-doc
        ref="doc"
        render-style="read"
        style="height:100vh; width:100%"
        fetch-credentials="include"
        update-route="false"
        allow-authentication="false"
        show-header="false"
        theme="dark"
      />
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import 'rapidoc'

const store = useStore()
const doc = ref()

const theme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'dark'
  return 'light'
})

onMounted(async () => {
  const http = 'http', https = 'https'
  let openApiSpec = await store.dispatch('helpModule/getOpenApi')
  const protocol = window.location.protocol.slice(0, -1)

  if (protocol === https) {
    const openApiSpecString = JSON.stringify(openApiSpec)
    const modifiedOpenApiSpecString = openApiSpecString.replaceAll(http, https)
    openApiSpec = JSON.parse(modifiedOpenApiSpecString)
  }

  doc.value.loadSpec(openApiSpec)
})
</script>
  
<style scoped>
</style>
  