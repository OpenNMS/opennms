<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <NodesTable />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
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
    { label: 'Node List', to: '#', position: 'last' }
  ]
})

// Holds query params that arrived before categories/locations had loaded.
// Cleared once the deferred filter is applied.
const pendingRouteQuery = ref<LocationQuery | null>(null)

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
  const prefs = loadNodePreferences()
  if (handleQuery(prefs)) {
    return
  }
  if (prefs) {
    nodeStructureStore.setFromNodePreferences(prefs)
  }
})

watch(() => route.query, () => {
  handleQuery(loadNodePreferences())
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

</style>
