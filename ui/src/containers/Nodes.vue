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
          <div class="feather-col-2">
            <NodeStructurePanel />
          </div>
          <div :class="`feather-col-10`">
            <NodesTable />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
<script setup lang="ts">
import NodesTable from '@/components/Nodes/NodesTable.vue'
import NodeStructurePanel from '@/components/Nodes/NodeStructurePanel.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { loadNodePreferences } from '@/services/localStorageService'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Nodes', to: '#', position: 'last' }
  ]
})

onMounted(() => {
  // load any saved preferences
  const prefs = loadNodePreferences()

  if (prefs) {
    nodeStructureStore.setFromNodePreferences(prefs)
  }
})
</script>
  
<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

</style>
