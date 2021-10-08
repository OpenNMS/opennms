<template>
  <Dropdown 
    @change="$emit('set-location', location)" 
    v-model="location" 
    :options="locations" 
    optionLabel="location-name"
    class="locations-dropdown"/>
</template>

<script lang="ts">
import { defineComponent, computed, ref, watchEffect } from 'vue'
import { useStore } from 'vuex'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
  components: {
    Dropdown
  },
  emits: ['set-location'],
  setup(_, context) {
    const store = useStore()
    const locations = computed(() => [...store.state.locationsModule.locations, { 'location-name': 'All' }])
    const location = ref(locations.value[0])

    watchEffect(() => context.emit('set-location', locations.value[0]))

    return {
      location,
      locations
    }
  }
})
</script>

<style scoped lang="scss">
  .locations-dropdown {
    width: 100%;
  }
</style>

