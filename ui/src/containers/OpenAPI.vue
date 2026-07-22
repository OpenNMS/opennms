<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="onms-row doc-row">
    <div class="onms-col-12">
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
   <div class="onms-row doc-row">
      <div class="onms-col-12">
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

<script lang="ts">
import API from '@/services'

// Module scope, unlike `<script setup>` bindings which are created per component
// instance: the specs are large and the REST responses are deliberately
// uncacheable, so they are fetched once and kept for the lifetime of the SPA session.
let cachedSpecs: [Record<string, unknown>, Record<string, unknown>] | null = null

// RapiDoc may normalize the spec it is handed in place, so never share the cached
// objects with it directly.
const cloneSpec = (spec: Record<string, unknown>): Record<string, unknown> =>
  JSON.parse(JSON.stringify(spec))

// The generated specs carry "example": null on most operations, and null members
// inside "examples" maps, both of which crash RapiDoc's example normalization
// (typeof null === 'object', so it dereferences null.value) and leave an uncaught
// TypeError per affected parameter and response panel. Drop them before rendering.
const stripNullExamples = (key: string, value: unknown): unknown => {
  if ((key === 'example' || key === 'examples') && value === null) {
    return undefined
  }
  if (key === 'examples' && typeof value === 'object' && !Array.isArray(value)) {
    return Object.fromEntries(Object.entries(value as Record<string, unknown>).filter(([, v]) => v !== null))
  }
  return value
}

const sanitizeSpec = (spec: Record<string, unknown>): Record<string, unknown> =>
  JSON.parse(JSON.stringify(spec, stripNullExamples))

const fetchSpecs = async (): Promise<[Record<string, unknown>, Record<string, unknown>]> => {
  if (cachedSpecs) {
    return cachedSpecs
  }

  const http = 'http', https = 'https'
  const protocol = window.location.protocol.slice(0, -1)

  const [openApiSpec, openApiSpecV1] = await Promise.all([API.getOpenApi(), API.getOpenApiV1()])

  let modifiedOpenApiSpec = openApiSpec
  let modifiedOpenApiV1Spec = openApiSpecV1

  if (protocol === https) {
    const openApiSpecString = JSON.stringify(openApiSpec)
    const modifiedOpenApiSpecString = openApiSpecString.includes(https)
      ? openApiSpecString
      : openApiSpecString.replaceAll(http, https)
    modifiedOpenApiSpec = JSON.parse(modifiedOpenApiSpecString)
    const openApiSpecStringV1 = JSON.stringify(openApiSpecV1)
    const modifiedOpenApiSpecStringV1 = openApiSpecStringV1.includes(https)
      ? openApiSpecStringV1
      : openApiSpecStringV1.replaceAll(http, https)
    modifiedOpenApiV1Spec = JSON.parse(modifiedOpenApiSpecStringV1)
  }

  modifiedOpenApiSpec = sanitizeSpec(modifiedOpenApiSpec)
  modifiedOpenApiV1Spec = sanitizeSpec(modifiedOpenApiV1Spec)

  // an empty object means the fetch failed; let the next mount retry instead of caching it
  if (Object.keys(modifiedOpenApiSpec).length > 0 && Object.keys(modifiedOpenApiV1Spec).length > 0) {
    cachedSpecs = [modifiedOpenApiSpec, modifiedOpenApiV1Spec]
  }

  return [modifiedOpenApiSpec, modifiedOpenApiV1Spec]
}
</script>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

import 'rapidoc'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const appStore = useAppStore()
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
  const theme = appStore.theme

  if (theme === 'open-dark') {
    return 'dark'
  }

  return 'light'
})

// The v1 doc starts below the fold, so rendering it is deferred until it is
// about to be scrolled into view.
let v1Observer: IntersectionObserver | null = null
let v1Rendered = false

const setup = async () => {
  const docEl = document.getElementById('thedoc')
  const docElV1 = document.getElementById('thedocV1')

  const [openApiSpec, openApiSpecV1] = await fetchSpecs()

  if (!doc.value) {
    // the component was unmounted while the specs were being fetched
    return
  }

  doc.value.loadSpec(cloneSpec(openApiSpec))
  setTheme(docEl)
  setTheme(docElV1)

  const renderV1 = () => {
    v1Rendered = true
    docV1.value?.loadSpec(cloneSpec(openApiSpecV1))
  }

  v1Observer?.disconnect()
  if (v1Rendered) {
    // re-running for a theme change: the v1 doc is already visible, reload it directly
    renderV1()
  } else if (docElV1) {
    v1Observer = new IntersectionObserver((entries) => {
      if (entries.some(entry => entry.isIntersecting)) {
        v1Observer?.disconnect()
        v1Observer = null
        renderV1()
      }
    }, { rootMargin: '600px 0px' })
    v1Observer.observe(docElV1)
  }
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

onMounted(async () => {
  setup()
})

onUnmounted(() => {
  v1Observer?.disconnect()
  v1Observer = null
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
