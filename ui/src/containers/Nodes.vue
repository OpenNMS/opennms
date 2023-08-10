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
import { useStore } from 'vuex'
import NodesTable from '@/components/Nodes/NodesTable.vue'
import NodeStructurePanel from '@/components/Nodes/NodeStructurePanel.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { BreadCrumb } from '@/types'

const store = useStore()

const homeUrl = computed<string>(() => store.state.menuModule.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Nodes', to: '#', position: 'last' }
  ]
})

</script>
  
<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

</style>
  