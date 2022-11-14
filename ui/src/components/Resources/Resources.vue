<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-3">
      <ResourceList />
    </div>
    <div class="feather-col-8">
      <NodeResourceList />
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { useStore } from 'vuex'
import ResourceList from '@/components/Resources/ResourceList.vue'
import NodeResourceList from '@/components/Resources/NodeResourceList.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { BreadCrumb } from '@/types'

const store = useStore()

const homeUrl = computed<string>(() => store.state.menuModule.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Resource Graphs', to: '#', position: 'last' }
  ]
})

onMounted(() => {
  store.dispatch('resourceModule/getResources')
})
</script>
