<template>
  <FeatherSelect
    @update:modelValue="$emit('set-location', location)"
    v-model="location"
    :options="locations"
    text-prop="location-name"
    class="locations-dropdown"
    label="Location"
  />
</template>

<script lang="ts">
import { defineComponent, computed, ref, watchEffect } from 'vue'
import { useStore } from 'vuex'
import { FeatherSelect } from '@featherds/select'

export default defineComponent({
  components: {
    FeatherSelect
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

