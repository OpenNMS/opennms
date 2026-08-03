<template>
  <FormField label="Show Severity" class="severity-select">
    <OnmsSelect
      v-model="selectedSeverity"
      :options="options"
      optionLabel="option"
      @update:modelValue="onSeveritySelect"
    />
  </FormField>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { useMapStore } from '@/stores/mapStore'

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
  top: 2em;
  /* below the app bar's z-index (1030) */
  z-index: 1020;
  /* Translucent panel so the control reads clearly over the map. Follows the
     theme (light panel in light mode, dark in dark mode) like the rest of the
     UI — the select itself just uses the default --p-* theme tokens. */
  padding: 0.5em;
  background-color: rgba(211, 211, 211, 0.8);
  border-radius: 4px;
}

.open-dark .severity-select {
  background-color: rgba(30, 30, 40, 0.8);
}
</style>
