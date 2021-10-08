<template>
  <div class="p-flex-row input first">
    <h4 class="pointer" @click="showFilter = !showFilter">Add a filter</h4>
  </div>

  <div v-if="showFilter">
    <div class="p-flex-row input">
      <label>If </label>
      <Dropdown :options="['', 'any', 'all']" v-model="dropdown1" @change="setValues" />
      <Dropdown :options="['IP address', 'Hostname']" v-model="dropdown2" @change="setValues" />
    </div>
    <div class="p-flex-row input">
      <label>matches </label>
      <InputText placeholder="Value" v-model="value" @input="setValues" />
    </div>
    <div class="p-flex-row input">
      <label>then perform </label>
      <Dropdown :options="['Don\'t add node', 'Don\'t collect data']" v-model="dropdown3" @change="setValues" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'

export default defineComponent({
  components: {
    InputText,
    Dropdown
  },
  emits: ['set-values'],
  setup(_, context) {
    const showFilter = ref(false)
    const value = ref()
    const dropdown1 = ref()
    const dropdown2 = ref('IP address')
    const dropdown3 = ref('Don\'t add node')

    const data = computed(() => ({ value: value.value }))
    const setValues = () => context.emit('set-values', data.value)

    return {
      showFilter,
      value,
      dropdown1,
      dropdown2,
      dropdown3,
      setValues
    }
  }
})

</script>

<style scoped lang="scss"></style>
