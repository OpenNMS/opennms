<template>
  <FormField label="Show Severity >=" class="severity-select">
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
  right: 60px;
  top: 80px;
  /* below the app bar's z-index (1030) */
  z-index: 1020;

  .p-select {
    background: var(--p-content-background);
    border: 2px solid var(--p-primary-color);
  }
}
</style>
