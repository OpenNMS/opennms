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
import ResourceList from '@/components/Resources/ResourceList.vue'
import NodeResourceList from '@/components/Resources/NodeResourceList.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useResourceStore } from '@/stores/resourceStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const resourceStore = useResourceStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Resource Graphs', to: '#', position: 'last' }
  ]
})

onMounted(() => {
  resourceStore.getResources()
})
</script>
