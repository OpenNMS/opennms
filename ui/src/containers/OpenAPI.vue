<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
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
   <div class="feather-row doc-row">
      <div class="feather-col-12">
        <rapi-doc
          id="thedocV1"
          ref="docV1"
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
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const store = useStore()
const menuStore = useMenuStore()
const doc = ref()
const docV1 = ref()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Endpoints', to: '#', position: 'last' }
  ]
})

const getTheme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'dark'
  return 'light'
})

const openApiSpec = computed<string>(() => store.state.helpModule?.openApi )
const openApiSpecV1 = computed<string>(() => store.state.helpModule?.openApiV1 )

const setup = async () => {
  
  const docEl = document.getElementById('thedoc')
  const docElV1 = document.getElementById('thedocV1')
  const http = 'http', https = 'https'
  let openApiSpec = await store.dispatch('helpModule/getOpenApi')
  let openApiSpecV1 = await store.dispatch('helpModule/getOpenApiV1')
  const protocol = window.location.protocol.slice(0, -1)

  let modifiedOpenApiSpec = openApiSpec
  let modifiedOpenApiV1Spec = openApiSpecV1

  if (protocol === https) {
    const openApiSpecString = JSON.stringify(openApiSpec)
    const modifiedOpenApiSpecString = openApiSpecString.replaceAll(http, https)
    modifiedOpenApiSpec = JSON.parse(modifiedOpenApiSpecString)

    const openApiSpecStringV1 = JSON.stringify(openApiSpecV1)
    const modifiedOpenApiSpecStringV1 = openApiSpecStringV1.replaceAll(http, https)
    modifiedOpenApiV1Spec = JSON.parse(modifiedOpenApiSpecStringV1)
  }

  doc.value.loadSpec(modifiedOpenApiSpec)
  docV1.value.loadSpec(modifiedOpenApiV1Spec)

  setTheme(docEl)
  setTheme(docElV1)
  
}

const setTheme = (element: HTMLElement | null) => {
  const theme = getTheme.value
  if (element) {
    if (theme === 'light') {
      element.setAttribute('theme', 'light')
      element.setAttribute('bg-color', '#fff')
      element.setAttribute('nav-bg-color', '#f4f7fc')
      element.setAttribute('nav-text-color', '#131736')
      element.setAttribute('nav-hover-bg-color', '#fff')
      element.setAttribute('nav-hover-text-color', '#00BFCB')
      element.setAttribute('nav-accent-color', '#00BFCB')
      element.setAttribute('primary-color', '#00BFCB')
    } else {
      element.setAttribute('theme', 'dark')
      element.setAttribute('bg-color', '#15182B')
      element.setAttribute('nav-bg-color', '#0a0c1b')
      element.setAttribute('nav-text-color', '#fff')
      element.setAttribute('nav-hover-bg-color', '#3a3d4d')
      element.setAttribute('nav-hover-text-color', '#fff')
      element.setAttribute('nav-accent-color', '#b5eff3')
      element.setAttribute('primary-color', '#00BFCB')
    }
  }
}

watch(getTheme, () => setup())

onMounted(() => {
  setup()
})
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
