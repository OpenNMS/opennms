<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-2">
        <ResourceList />
      </div>
      <div class="feather-col-9">
        <NodeResourceList />
      </div>
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import ResourceList from '@/components/Resources/ResourceList.vue'
import NodeResourceList from '@/components/Resources/NodeResourceList.vue'

const store = useStore()

onMounted(() => {
  store.dispatch('resourceModule/getResources')
})
</script>
  
<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  background: var(--feather-surface);
  padding: 15px;
  height: calc(100vh - 110px);
  overflow-y: scroll;
}
</style>
  