<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <div class="feather-row">
          <!-- <div class="feather-col-2">
            <NodeStructurePanel />
          </div> -->
          <div :class="`feather-col-12`">
            <NodesTable />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
<script setup lang="ts">
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import NodesTable from '@/components/Nodes/NodesTable.vue'
import { loadNodePreferences } from '@/services/localStorageService'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { BreadCrumb, NodePreferences } from '@/types'
import { useRoute, useRouter } from 'vue-router'

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const { buildNodeQueryFilterFromQueryString, queryStringHasTrackedValues } = useNodeQuery()

const route = useRoute()
const router = useRouter()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Structured Node List', to: '#', position: 'last' }
  ]
})

const handleQuery = (prefs: NodePreferences | null) => {
  if (queryStringHasTrackedValues(route.query)) {
    const nodeFilter = buildNodeQueryFilterFromQueryString(route.query, nodeStructureStore.categories, nodeStructureStore.monitoringLocations)

    const newPrefs = {
      nodeColumns: prefs?.nodeColumns || [],
      nodeFilter
    } as NodePreferences

    nodeStructureStore.setFromNodePreferences(newPrefs)

    // TODO: Save prefs???
    router.replace({ name: 'Nodes' })
    return true
  }

  return false
}

onMounted(() => {
  // load any saved preferences
  const prefs = loadNodePreferences()

  if (handleQuery(prefs)) {
    return
  }

  if (prefs) {
    nodeStructureStore.setFromNodePreferences(prefs)
  }
})

watch (() => route.query, () => {
  const prefs = loadNodePreferences()
  handleQuery(prefs)
})
</script>
  
<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

</style>
