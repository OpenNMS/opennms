<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <Graph
        v-for="definition in props.resource.definitions"
        :definition="definition"
        :resourceId="props.resource.id"
        :key="definition"
      />
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { PropType } from 'vue'
import Graph from './Graph.vue'

const props = defineProps({
  resource: {
    required: true,
    type: Object as PropType<{ id: string, definitions: string[] }>
  }
})
</script>
  
<style scoped lang="scss">
</style>
  