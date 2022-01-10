<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <GraphContainer v-for="resource in resources" :resource="resource" :key="resource.id" />
    </div>
  </div>
</template>
  
<script setup lang=ts>
import { computed } from 'vue'
import { useStore } from 'vuex'
import GraphContainer from './GraphContainer.vue'
import { Chart, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'

Chart.register(...registerables)
Chart.register(zoomPlugin)

const store = useStore()
const resources = computed<{ id: string, definitions: string[] }[]>(() => store.state.graphModule.definitions)
</script>
  
<style scoped lang="scss">
</style>
  