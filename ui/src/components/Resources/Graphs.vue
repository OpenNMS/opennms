<template>
  <div class="feather-row">
    <div class="feather-col-12">

    </div>
  </div>
</template>
  
<script setup lang=ts>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const definitions = computed<string[]>(() => store.state.graphModule.definitions)
</script>
  
<style scoped lang="scss">
</style>
  