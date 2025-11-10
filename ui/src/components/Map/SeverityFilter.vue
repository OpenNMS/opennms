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
import { FeatherSelect } from '@featherds/select'
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
@import "@featherds/styles/themes/variables";

.severity-select {
  position: absolute;
  width: 250px;
  right: 60px;
  top: 80px;
  /* z-index needs to be below $zindex-fixed (1030) which is the z-index of the FeatherAppBar component */
  z-index: var($zindex-sticky);

  .feather-input-wrapper {
    background: var($primary-text-on-color);
    border: 2px solid var($secondary);
  }

  .feather-input-label {
    border: 2px solid var($secondary);
    border-bottom: none;

    // fix placement of severity dropdown label
    // so it is aligned over top left of select dropdown
    left: -0.025rem !important;
    top: -1.25rem !important;
  }
}
</style>
