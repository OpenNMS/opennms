<template>
  <Dropdown 
    @change="$emit('set-type', type)" 
    v-model="type" 
    :options="types" 
    optionLabel="name"
    class="types-dropdown"/>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
  components: {
    Dropdown
  },
  emits: ['set-type'],
  setup() {
    const types = [
      {id: 1, name: 'VMware vSphere'}, 
      {id: 2, name: 'OpenDaylight'}
    ]

    const type = ref(types[0])

    return {
      type,
      types
    }
  }
})
</script>

<style scoped lang="scss">
  .types-dropdown {
    width: 100%;
  }
</style>

