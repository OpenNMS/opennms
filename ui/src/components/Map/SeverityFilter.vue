<template>
  <FeatherSelect
    class="severity-select"
    v-model="selectedSeverity"
    :options="options"
    text-prop="option"
    @update:modelValue="onSeveritySelect"
    label="Show Severity >="
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherSelect } from '@featherds/select'

const store = useStore()

const options = [
  { id: 'NORMAL', option: 'Normal' },
  { id: 'WARNING', option: 'Warning' },
  { id: 'MINOR', option: 'Minor' },
  { id: 'MAJOR', option: 'Major' },
  { id: 'CRITICAL', option: 'Critical' }
]
const selectedSeverity = ref(options[0])

const onSeveritySelect = () => store.dispatch('mapModule/setSelectedSeverity', selectedSeverity.value.id)
</script>

<style lang="scss">
@import "@featherds/styles/themes/variables";
.severity-select {
  position: absolute;
  width: 250px;
  right: 51px;
  top: -12px;
  z-index: 1000;
  .feather-input-wrapper {
    background: var($primary-text-on-color);
    border: 2px solid var($secondary);
  }
  .feather-input-label {
    border: 2px solid var($secondary);
    border-bottom: none;
  }
}
</style>
