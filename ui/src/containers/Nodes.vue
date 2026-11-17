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
import { parseNodeIdQueryParam, useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import { nodeDetailsRoute } from '@/components/Nodes/nodeDetailsRoute'
import NodesTable from '@/components/Nodes/NodesTable.vue'
import { loadNodePreferences, saveNodeQueryFilter } from '@/services/localStorageService'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeListStore } from '@/stores/nodeListStore'
import { BreadCrumb, NodePreferences } from '@/types'
import { LocationQuery, useRoute, useRouter } from 'vue-router'

const menuStore = useMenuStore()
const nodeListStore = useNodeListStore()
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

// Legacy bookmarks like `#/nodes?nodeId=42` land on Node Details -- where the node-label column
// links -- rather than being read as a node-list filter. Returns true when it redirected, so normal
// query handling is skipped. replace, not push: Back should not return to the bookmark URL and
// redirect again.
const handleNodeIdRedirect = (query: LocationQuery): boolean => {
  const id = parseNodeIdQueryParam(query)
  if (id === null) {
    return false
  }
  router.replace(nodeDetailsRoute(id))
  return true
}

const applyQueryFilter = (query: LocationQuery, prefs: NodePreferences | null) => {
  const nodeFilter = buildNodeQueryFilterFromQueryString(
    query,
    nodeListStore.categories,
    nodeListStore.monitoringLocations,
    nodeListStore.allServiceTypes
  )
  const newPrefs = {
    nodeColumns: prefs?.nodeColumns || [],
    nodeFilter
  } as NodePreferences

  // listInterfaces is a display flag (legacy ?listInterfaces=true), not part of the filter —
  // applied directly on the store here rather than persisted via NodePreferences.
  if (String(query.listInterfaces).toLowerCase() === 'true') {
    nodeListStore.setShowInterfaces(true)
  }

  nodeListStore.setFromNodePreferences(newPrefs)
}

const handleQuery = (prefs: NodePreferences | null) => {
  if (queryStringHasTrackedValues(route.query)) {
    if (!nodeListStore.categoriesLoaded || !nodeListStore.monitoringLocationsLoaded || !nodeListStore.serviceTypesLoaded) {
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
  [() => nodeListStore.categoriesLoaded, () => nodeListStore.monitoringLocationsLoaded, () => nodeListStore.serviceTypesLoaded],
  ([catsLoaded, locsLoaded, svcTypesLoaded]) => {
    if (pendingRouteQuery.value && catsLoaded && locsLoaded && svcTypesLoaded) {
      applyQueryFilter(pendingRouteQuery.value, loadNodePreferences())
      pendingRouteQuery.value = null
    }
  }
)

let saveFilterTimeout: number | undefined

watch(
  () => nodeListStore.queryFilter,
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
    nodeListStore.setFromNodePreferences(prefs)
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
