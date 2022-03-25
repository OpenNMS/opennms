<template>
  <FeatherSelect
    class="view-select"
    v-model="selectedView"
    :options="options"
    text-prop="option"
    @update:modelValue="onViewSelect"
    label="View"
    :background="false"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherSelect } from '@featherds/select'

const store = useStore()

const options = [
  { id: 'map', option: 'Map Layout' },
  { id: 'd3', option: 'D3 Layout' },
  { id: 'circle', option: 'Circle Layout' },
]
const selectedView = ref(options[0])

const onViewSelect = () => store.dispatch('topologyModule/setSelectedView', selectedView.value.id)
</script>

<style scoped lang="scss">
.view-select {
  width: 15rem;
}
</style>
