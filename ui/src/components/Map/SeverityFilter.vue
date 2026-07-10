<template>
  <FormField label="Show Severity" class="severity-select">
    <PSelect
      v-model="selectedSeverity"
      :options="options"
      optionLabel="option"
      @update:modelValue="onSeveritySelect"
    />
  </FormField>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import Select from 'primevue/select'
import FormField from '@/components/Common/FormField.vue'
import { useMapStore } from '@/stores/mapStore'

const PSelect = Select

const mapStore = useMapStore()

const options = [
  { id: 'NORMAL', option: 'Normal' },
  { id: 'WARNING', option: 'Warning' },
  { id: 'MINOR', option: 'Minor' },
  { id: 'MAJOR', option: 'Major' },
  { id: 'CRITICAL', option: 'Critical' }
]
const selectedSeverity = ref(options[0])

const onSeveritySelect = () => mapStore.setSelectedSeverity(selectedSeverity.value.id)
</script>

<style lang="scss">
.severity-select {
  position: absolute;
  width: 250px;
  right: 80px;
  top: 80px;
  /* below the app bar's z-index (1030) */
  z-index: 1020;
  /* Translucent light panel so the control reads clearly over the map. */
  padding: 0.5em;
  background-color: rgba(211, 211, 211, 0.8);
  border-radius: 4px;

  // The map tiles are always light regardless of the app's light/dark theme,
  // so keep this overlay control readable: dark label + dark value on a light
  // select background in both themes (not the theme-aware --p-* tokens).
  .form-field__label {
    color: #1b1b1f !important;
  }

  .p-select {
    background: #ffffff;
    border: 2px solid var(--p-primary-color);
  }

  .p-select-label {
    color: #1b1b1f !important;
  }
}
</style>
