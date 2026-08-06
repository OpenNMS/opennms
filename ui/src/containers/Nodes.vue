<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="onms-row">
    <div class="onms-col-12">
      <div class="card">
        <NodesTable />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { buildNodeDetailUrl, parseNodeIdQueryParam, useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import NodesTable from '@/components/Nodes/NodesTable.vue'
import { loadNodePreferences, saveNodeQueryFilter } from '@/services/localStorageService'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { BreadCrumb, NodePreferences } from '@/types'
import { LocationQuery, useRoute, useRouter } from 'vue-router'

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const { buildNodeQueryFilterFromQueryString, queryStringHasTrackedValues } = useNodeQuery()

const route = useRoute()
const router = useRouter()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Nodes', to: '#', position: 'last' }
  ]
})

// Holds query params that arrived before categories/locations had loaded.
// Cleared once the deferred filter is applied.
const pendingRouteQuery = ref<LocationQuery | null>(null)

// Holds a legacy `?nodeId=<n>` id that arrived before menuStore.mainMenu (source of
// baseHref/baseNodeUrl) had loaded. Cleared once the deferred redirect fires.
const pendingNodeIdRedirect = ref<number | null>(null)

// Legacy bookmarks like `#/nodes?nodeId=42` should land on the node detail page — the same
// place the node-label column links to (see computeNodeLink in NodesTable.vue) — rather than
// being treated as a node-list filter. Returns true if the query was handled here (either
// redirected immediately or deferred), meaning normal query handling should be skipped.
const handleNodeIdRedirect = (query: LocationQuery): boolean => {
  const id = parseNodeIdQueryParam(query)
  if (id === null) {
    return false
  }

  const url = buildNodeDetailUrl(menuStore.mainMenu, id)
  if (url) {
    // replace (not assign): this is a redirect away from a legacy bookmark, not real in-app
    // navigation — using replace keeps the ?nodeId=<n> URL out of history so Back doesn't return
    // to it and immediately re-redirect.
    window.location.replace(url)
  } else {
    // mainMenu hasn't loaded yet — defer until the watch below sees it arrive.
    pendingNodeIdRedirect.value = id
  }
  return true
}

// Completes a deferred nodeId redirect once menuStore.mainMenu (baseHref/baseNodeUrl) loads.
watch(
  () => menuStore.mainMenu?.baseHref,
  () => {
    if (pendingNodeIdRedirect.value !== null) {
      const url = buildNodeDetailUrl(menuStore.mainMenu, pendingNodeIdRedirect.value)
      if (url) {
        pendingNodeIdRedirect.value = null
        window.location.replace(url)
      }
    }
  }
)

const applyQueryFilter = (query: LocationQuery, prefs: NodePreferences | null) => {
  const nodeFilter = buildNodeQueryFilterFromQueryString(
    query,
    nodeStructureStore.categories,
    nodeStructureStore.monitoringLocations,
    nodeStructureStore.allServiceTypes
  )
  const newPrefs = {
    nodeColumns: prefs?.nodeColumns || [],
    nodeFilter
  } as NodePreferences

  // listInterfaces is a display flag (legacy ?listInterfaces=true), not part of the filter —
  // applied directly on the store here rather than persisted via NodePreferences.
  if (String(query.listInterfaces).toLowerCase() === 'true') {
    nodeStructureStore.setShowInterfaces(true)
  }

  nodeStructureStore.setFromNodePreferences(newPrefs)
}

const handleQuery = (prefs: NodePreferences | null) => {
  if (queryStringHasTrackedValues(route.query)) {
    if (!nodeStructureStore.categoriesLoaded || !nodeStructureStore.monitoringLocationsLoaded || !nodeStructureStore.serviceTypesLoaded) {
      // Lists not finished loading yet — save and defer.
      pendingRouteQuery.value = { ...route.query }
    } else {
      applyQueryFilter(route.query, prefs)
    }
    // Always clear the URL regardless of whether we deferred.
    router.replace({ name: 'Nodes' })
    return true
  }
  return false
}

// Re-apply any deferred query once both lists have finished loading.
// Handles the race between App.vue's async getCategories/getMonitoringLocations
// and Nodes.vue mounting with query params already in the URL.
// Note: lists may be empty (e.g. no categories configured) — loaded flags handle this correctly.
watch(
  [() => nodeStructureStore.categoriesLoaded, () => nodeStructureStore.monitoringLocationsLoaded, () => nodeStructureStore.serviceTypesLoaded],
  ([catsLoaded, locsLoaded, svcTypesLoaded]) => {
    if (pendingRouteQuery.value && catsLoaded && locsLoaded && svcTypesLoaded) {
      applyQueryFilter(pendingRouteQuery.value, loadNodePreferences())
      pendingRouteQuery.value = null
    }
  }
)

let saveFilterTimeout: number | undefined

watch(
  () => nodeStructureStore.queryFilter,
  (filter) => {
    if (saveFilterTimeout !== undefined) {
      clearTimeout(saveFilterTimeout)
    }
    saveFilterTimeout = window.setTimeout(() => saveNodeQueryFilter(filter), 250)
  },
  { deep: true }
)

onMounted(() => {
  if (handleNodeIdRedirect(route.query)) {
    return
  }
  const prefs = loadNodePreferences()
  if (handleQuery(prefs)) {
    return
  }
  if (prefs) {
    nodeStructureStore.setFromNodePreferences(prefs)
  }
})

watch(() => route.query, () => {
  if (handleNodeIdRedirect(route.query)) {
    return
  }
  handleQuery(loadNodePreferences())
})
</script>

<style lang="scss" scoped>
@import "@/styles/onms-tokens";

</style>
