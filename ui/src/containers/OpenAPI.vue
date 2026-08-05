<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div
    ref="docColumn"
    class="doc-tabs"
  >
    <OnmsTabs
      :value="activeTab"
      @update:value="onTabChange"
    >
      <OnmsTabList>
        <OnmsTab :value="0">V2 API</OnmsTab>
        <OnmsTab :value="1">V1 API</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
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
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
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
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
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
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

import 'rapidoc'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

const appStore = useAppStore()
const menuStore = useMenuStore()
const doc = ref()
const docV1 = ref()
const docColumn = ref<HTMLElement | null>(null)
const activeTab = ref(0)

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

// Any page overflow lets scrollIntoView scroll the document, so take the space
// actually left over rather than guessing at the chrome heights.
const fitColumnToViewport = () => {
  const column = docColumn.value
  if (!column) {
    return
  }

  // clientHeight rather than innerHeight so a horizontal scrollbar is excluded
  const viewport = document.documentElement.clientHeight
  const topInDocument = column.getBoundingClientRect().top + window.scrollY
  const footer = document.querySelector('.app-footer')
  const below = footer ? footer.getBoundingClientRect().height : 0

  const available = viewport - topInDocument - below
  columnFits = available >= MIN_COLUMN_HEIGHT
  const next = `${Math.max(MIN_COLUMN_HEIGHT, available)}px`

  // guard the write so observing body size cannot feed back into itself
  if (column.style.height !== next) {
    column.style.height = next
  }
}

// Enough to stay usable if the measurement lands somewhere unexpected.
const MIN_COLUMN_HEIGHT = 320

let bodyObserver: ResizeObserver | null = null
// false when the viewport is too short, where the page does need to scroll
let columnFits = true
// held separately: the template ref is already cleared by the time onUnmounted runs
let watchedColumn: HTMLElement | null = null

// RapiDoc and PrimeVue's Tab both call scrollIntoView, which scrolls every
// scrollable ancestor. PrimeVue's theme is generated at runtime, so which boxes
// those are is not knowable here; undo them instead. querySelectorAll stops at the
// shadow root, leaving RapiDoc's own panes to scroll. Vertical only: the tab strip
// is legitimately scrollable sideways.
const isScrollable = (element: Element) => {
  const style = getComputedStyle(element)
  const scrolls = /^(auto|scroll|hidden)$/
  return scrolls.test(style.overflowY) || scrolls.test(style.overflowX)
}

const preserveLayoutScroll = () => {
  const column = docColumn.value
  if (!column) {
    return
  }

  const snapshots: { element: Element, top: number }[] = []

  for (const element of [column, ...Array.from(column.querySelectorAll('*'))]) {
    if (isScrollable(element)) {
      snapshots.push({ element, top: element.scrollTop })
    }
  }

  for (let element = column.parentElement; element; element = element.parentElement) {
    if (isScrollable(element)) {
      snapshots.push({ element, top: element.scrollTop })
    }
  }

  const scrollingElement = document.scrollingElement
  if (scrollingElement) {
    snapshots.push({ element: scrollingElement, top: scrollingElement.scrollTop })
  }

  const restore = () => {
    for (const { element, top } of snapshots) {
      if (element.scrollTop !== top) {
        element.scrollTop = top
      }
    }
  }

  // both callers scroll after an await of their own, so one frame is too early
  requestAnimationFrame(() => {
    restore()
    requestAnimationFrame(restore)
  })
}

// Focusing the newly shown tab scrolls it into view on its own, after the frames
// the click handler watches. The column is clipped, so the document is all that is
// left, and it has nowhere to go while the column fits.
const pinDocumentScroll = () => {
  if (!columnFits) {
    return
  }

  const scroller = document.scrollingElement
  if (scroller && scroller.scrollTop !== 0) {
    scroller.scrollTop = 0
  }
}

// Rendering a couple of hundred operations is not cheap, so the V1 doc waits
// until its tab is first opened.
let v1Rendered = false

const renderV1 = async () => {
  const [, openApiSpecV1] = await fetchSpecs()

  if (!docV1.value) {
    return
  }

  v1Rendered = true
  docV1.value.loadSpec(cloneSpec(openApiSpecV1))
}

const onTabChange = (value: string | number) => {
  activeTab.value = Number(value)

  if (activeTab.value === 1 && !v1Rendered) {
    renderV1()
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

const setup = async () => {
  const [openApiSpec] = await fetchSpecs()

  if (!doc.value) {
    // the component was unmounted while the specs were being fetched
    return
  }

  doc.value.loadSpec(cloneSpec(openApiSpec))
  setTheme(document.getElementById('thedoc'))
  setTheme(document.getElementById('thedocV1'))

  if (v1Rendered) {
    // re-running for a theme change; loadSpec is what makes RapiDoc pick the colours up
    await renderV1()
  }
}

watch(getTheme, () => setup())

onMounted(async () => {
  setup()

  // after the breadcrumbs and tab strip have laid out, since the height depends on them
  await nextTick()
  fitColumnToViewport()
  window.addEventListener('resize', fitColumnToViewport)

  // capture, so it is queued before RapiDoc's and PrimeVue's own handlers run
  watchedColumn = docColumn.value
  watchedColumn?.addEventListener('click', preserveLayoutScroll, true)
  watchedColumn?.addEventListener('keyup', preserveLayoutScroll, true)
  // capture, since scroll events do not bubble
  window.addEventListener('scroll', pinDocumentScroll, true)

  // App.vue fetches the menu in onMounted, so the header grows after this point
  // and pushes the column down.
  if (typeof ResizeObserver !== 'undefined') {
    bodyObserver = new ResizeObserver(() => fitColumnToViewport())
    bodyObserver.observe(document.body)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', fitColumnToViewport)
  bodyObserver?.disconnect()
  bodyObserver = null
  watchedColumn?.removeEventListener('click', preserveLayoutScroll, true)
  watchedColumn?.removeEventListener('keyup', preserveLayoutScroll, true)
  window.removeEventListener('scroll', pinDocumentScroll, true)
  watchedColumn = null
})
</script>

<style scoped lang="scss">
rapi-doc::part(section-tag) {
  display: none;
}

// height is a fallback only; fitColumnToViewport sets the real one. clip, not
// hidden: a hidden box is still programmatically scrollable.
.doc-tabs {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
  overflow: clip;
  margin-top: -10px;

  // clip, so the theme cannot leave these scrollable
  :deep(.p-tabs) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow: clip;
  }

  :deep(.p-tabpanels) {
    flex: 1;
    min-height: 0;
    padding: 0;
    overflow: clip;
  }

  :deep(.p-tabpanel) {
    height: 100%;
    overflow: clip;
  }

  .doc {
    height: 100%;
    width: 100%;
  }
}
</style>
