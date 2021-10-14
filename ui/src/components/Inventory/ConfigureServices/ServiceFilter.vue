<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="pointer headline4" @click="showFilter = !showFilter">Add a filter</div>
    </div>
  </div>

  <div v-if="showFilter">
    <div class="feather-row">
      <div class="feather-col-3">
        <label>If</label>
        <FeatherSelect
          :options="[{ id: 1, value: 'any' }, { id: 2, value: 'all' }]"
          text-prop="value"
          v-model="dropdown1"
          @update:modelValue="setValues"
        />
        <FeatherSelect
          text-prop="value"
          :options="[{ id: 1, value: 'IP address' }, { id: 2, value: 'Hostname' }]"
          v-model="dropdown2"
          @update:modelValue="setValues"
        />
        <label>matches</label>
        <FeatherInput placeholder="Value" v-model="value" @input="setValues" />
        <label>then perform</label>
        <FeatherSelect
          text-prop="value"
          :options="[{ id: 1, value: 'Don\'t add node' }, { id: 1, value: 'Don\'t collect data' }]"
          v-model="dropdown3"
          @update:modelValue="setValues"
        />
      </div>
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
