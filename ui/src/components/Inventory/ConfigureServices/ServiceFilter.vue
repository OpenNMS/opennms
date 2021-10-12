<template>
  <div class="feather-row input first">
    <h4 class="pointer" @click="showFilter = !showFilter">Add a filter</h4>
  </div>

  <div v-if="showFilter">
    <div class="feather-row input">
      <label>If </label>
      <FeatherSelect :options="['', 'any', 'all']" v-model="dropdown1" @update:modelValue="setValues" />
      <FeatherSelect :options="['IP address', 'Hostname']" v-model="dropdown2" @update:modelValue="setValues" />
    </div>
    <div class="feather-row input">
      <label>matches </label>
      <FeatherInput placeholder="Value" v-model="value" @input="setValues" />
    </div>
    <div class="feather-row input">
      <label>then perform </label>
      <FeatherSelect :options="['Don\'t add node', 'Don\'t collect data']" v-model="dropdown3" @update:modelValue="setValues" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { FeatherSelect } from '@featherds/select'
import { FeatherInput } from '@featherds/input'

export default defineComponent({
  components: {
    FeatherInput,
    FeatherSelect
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
