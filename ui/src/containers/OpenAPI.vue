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
      <!-- show-info="false" so focused mode opens on the first operation rather than
           the info section, which is also served with the document itself. -->
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <rapi-doc
            id="thedoc"
            ref="doc"
            class="doc"
            render-style="focused"
            load-fonts="false"
            regular-font="OpenSans, Helvetica, Arial, sans-serif"
            mono-font="ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"
            fetch-credentials="include"
            update-route="false"
            allow-authentication="false"
            show-header="false"
            show-info="false"
          />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <rapi-doc
            id="thedocV1"
            ref="docV1"
            class="doc"
            render-style="focused"
            load-fonts="false"
            regular-font="OpenSans, Helvetica, Arial, sans-serif"
            mono-font="ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"
            fetch-credentials="include"
            update-route="false"
            allow-authentication="false"
            show-header="false"
            show-info="false"
          />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script lang="ts">
import API from '@/services'

type Specs = [Record<string, unknown>, Record<string, unknown>]

// Module scope, unlike `<script setup>` bindings which are created per component
// instance: the specs are large and the REST responses are deliberately
// uncacheable, so they are fetched once and kept for the lifetime of the SPA session.
// The pending promise is what is held, so a tab click during the mount-time fetch
// joins it instead of starting a second pair of requests.
let cachedSpecs: Promise<Specs> | null = null

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

const loadSpecs = async (): Promise<Specs> => {
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

  return [sanitizeSpec(modifiedOpenApiSpec), sanitizeSpec(modifiedOpenApiV1Spec)]
}

// Only drops the entry it created, so a retry already in flight is left alone.
const forget = (pending: Promise<Specs>) => {
  if (cachedSpecs === pending) {
    cachedSpecs = null
  }
}

const fetchSpecs = (): Promise<Specs> => {
  if (!cachedSpecs) {
    const pending: Promise<Specs> = loadSpecs()
      .then((specs) => {
        // an empty object means the fetch failed; let the next call retry instead of caching it
        if (specs.some(spec => Object.keys(spec).length === 0)) {
          forget(pending)
        }
        return specs
      })
      .catch((error) => {
        forget(pending)
        throw error
      })

    cachedSpecs = pending
  }

  return cachedSpecs
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
    { label: 'OpenAPI Documentation', to: '#', position: 'last' }
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

  lockPageScroll()
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

// The column already takes exactly the space left over, so the page has nothing
// to scroll to, but it stays a scroll container and shows a second, dead scrollbar
// next to the one RapiDoc renders. Take the overflow away rather than pinning the
// offset, which also stops the tab strip scrolling the document as it takes focus.
// Released while the viewport is too short for the column, where scrolling is the
// only way to reach the rest of the page.
// The app shell overflows the viewport by a little regardless of this column, and
// that is enough for RapiDoc's scrollIntoView to drag the whole layout under a
// scrollbar nothing else uses. Released while the viewport is too short, where
// scrolling is the only way to reach the rest of the page.
const PAGE_SCROLL_CLASS = 'openapi-no-page-scroll'

const lockPageScroll = () => {
  document.documentElement.classList.toggle(PAGE_SCROLL_CLASS, columnFits)
}

const releasePageScroll = () => {
  document.documentElement.classList.remove(PAGE_SCROLL_CLASS)
}

// clip hides the scrollbar but the viewport still scrolls programmatically, and
// RapiDoc calls scrollIntoView on the nav entry as the content passes a section.
const pinPageScroll = () => {
  const scroller = document.scrollingElement
  if (columnFits && scroller && scroller.scrollTop !== 0) {
    scroller.scrollTop = 0
  }
}

// Rendering a couple of hundred operations is not cheap, so the V1 doc waits
// until its tab is first opened.
let v1Rendered = false

const renderV1 = async () => {
  const [, openApiSpecV1] = await fetchSpecs()

  // RapiDoc sizes its scroll pane when it is handed the spec, so wait for the
  // panel to be shown; loaded while hidden it measures zero and never scrolls.
  await nextTick()

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
  window.addEventListener('scroll', pinPageScroll, true)

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
  window.removeEventListener('scroll', pinPageScroll, true)
  watchedColumn = null
  releasePageScroll()
})
</script>

<!-- On <html>, so it cannot be scoped. Both axes, or the overflow-x:hidden in
     App.vue coerces clip back to hidden, which still scrolls programmatically. -->
<style lang="scss">
html.openapi-no-page-scroll {
  overflow: clip;
}
</style>

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

  // RapiDoc puts an inline width:100vw/height:100vh on the panel holding it,
  // which overflows the clipped column and takes its scrollbar below the fold.
  :deep(.p-tabpanel) {
    height: 100% !important;
    width: 100% !important;
    overflow: clip;
  }

  .doc {
    height: 100%;
    width: 100%;
  }
}
</style>
