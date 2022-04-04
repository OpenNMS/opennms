<template>
  <div class="feather-row doc-row">
    <div class="feather-col-12">
      <rapi-doc
        id="thedoc"
        ref="doc"
        class="doc"
        render-style="read"
        fetch-credentials="include"
        update-route="false"
        allow-authentication="false"
        show-header="false"
      />
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { useStore } from 'vuex'
import 'rapidoc'

const store = useStore()
const doc = ref()

const getTheme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'dark'
  return 'light'
})

const setup = async () => {
  const theme = getTheme.value
  const docEl = document.getElementById('thedoc')
  const http = 'http', https = 'https'
  let openApiSpec = await store.dispatch('helpModule/getOpenApi')
  const protocol = window.location.protocol.slice(0, -1)

  if (protocol === https) {
    const openApiSpecString = JSON.stringify(openApiSpec)
    const modifiedOpenApiSpecString = openApiSpecString.replaceAll(http, https)
    openApiSpec = JSON.parse(modifiedOpenApiSpecString)
  }

  doc.value.loadSpec(openApiSpec)

  if (docEl) {
    if (theme === 'light') {
      docEl.setAttribute('theme', 'light')
      docEl.setAttribute('bg-color', '#fff')
      docEl.setAttribute('nav-bg-color', '#f4f7fc')
      docEl.setAttribute('nav-text-color', '#131736')
      docEl.setAttribute('nav-hover-bg-color', '#fff')
      docEl.setAttribute('nav-hover-text-color', '#00BFCB')
      docEl.setAttribute('nav-accent-color', '#00BFCB')
      docEl.setAttribute('primary-color', '#00BFCB')
    } else {
      docEl.setAttribute('theme', 'dark')
      docEl.setAttribute('bg-color', '#15182B')
      docEl.setAttribute('nav-bg-color', '#0a0c1b')
      docEl.setAttribute('nav-text-color', '#fff')
      docEl.setAttribute('nav-hover-bg-color', '#3a3d4d')
      docEl.setAttribute('nav-hover-text-color', '#fff')
      docEl.setAttribute('nav-accent-color', '#b5eff3')
      docEl.setAttribute('primary-color', '#00BFCB')
    }
  }
}

watch(getTheme, () => setup())
onMounted(() => {
  store.dispatch('appModule/setNavRailOpen', false)
  setup()
})
onUnmounted(() => store.dispatch('appModule/setNavRailOpen', true))
</script>

<style scoped lang="scss">
rapi-doc::part(section-tag) {
  display: none;
}

.doc-row {
  margin-top: -10px;
  .doc {
    height: 100vh;
    width: 100%;
    max-height: calc(100vh - 70px);
  }
}
</style>
