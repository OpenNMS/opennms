<template>
  <div class="timezone-picker" data-test="timezone-picker">
    <FormField label="Region" :for="`${idBase}-region`">
      <OnmsSelect
        v-model="region"
        :inputId="`${idBase}-region`"
        :options="regionOptions"
        filter
        showClear
        fluid
        placeholder="Server default"
        data-test="timezone-region-select"
      />
    </FormField>
    <FormField label="City / Area" :for="`${idBase}-city`">
      <OnmsSelect
        v-model="city"
        :inputId="`${idBase}-city`"
        :options="cityOptions"
        optionLabel="label"
        optionValue="value"
        :disabled="!region || cityOptions.length === 0"
        filter
        fluid
        :placeholder="region ? 'Select a city or area' : '—'"
        data-test="timezone-city-select"
      />
    </FormField>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'

// Split time-zone picker (NMS-20281): region ("America") then city/area
// ("New York", underscores shown as spaces), both with the seam Select's
// case-insensitive filter. The value stays a full IANA id.

const props = defineProps<{
  modelValue: string | null
  idBase: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const allZones: string[] = (() => {
  try {
    return (Intl as unknown as { supportedValuesOf(key: string): string[] }).supportedValuesOf('timeZone')
  } catch {
    return []
  }
})()

// a stored zone the browser's list doesn't know (hand-edited users.xml, e.g.
// a legacy alias) must still render and round-trip instead of vanishing
const zones = computed(() => {
  const stored = props.modelValue
  return stored && !allZones.includes(stored) ? [...allZones, stored] : allZones
})

const regionOf = (zone: string) => (zone.includes('/') ? zone.slice(0, zone.indexOf('/')) : zone)

const prettify = (zone: string) =>
  zone.slice(zone.indexOf('/') + 1).replaceAll('_', ' ').replaceAll('/', ' / ')

const regionOptions = computed(() => [...new Set(zones.value.map(regionOf))].sort())

const region = ref<string | null>(null)
const city = ref<string | null>(null)

const cityOptions = computed(() => {
  if (!region.value) {
    return []
  }
  return zones.value
    .filter(zone => zone.includes('/') && regionOf(zone) === region.value)
    .sort()
    .map(zone => ({ label: prettify(zone), value: zone }))
})

// region-only zones (UTC and friends) are complete on their own; a region with
// cities needs one picked before a value exists
watch([region, city], () => {
  const next = region.value === null
    ? null
    : cityOptions.value.length === 0
      ? region.value
      : city.value && regionOf(city.value) === region.value ? city.value : null
  if (next !== props.modelValue) {
    emit('update:modelValue', next)
  }
})

watch(() => props.modelValue, (zone) => {
  if (!zone) {
    region.value = null
    city.value = null
    return
  }
  region.value = regionOf(zone)
  city.value = zone.includes('/') ? zone : null
}, { immediate: true })
</script>

<style lang="scss" scoped>
.timezone-picker {
  display: grid;
  // match the parent grid's column rhythm; capped so a full-width row does
  // not stretch two selects across the whole page
  grid-template-columns: repeat(auto-fit, minmax(16rem, 24rem));
  gap: 0.75rem 1.25rem;
}
</style>
