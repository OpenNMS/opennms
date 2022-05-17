<template>
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

const store = useStore()

onMounted(() => {
  store.dispatch('resourceModule/getResources')
})
</script>
