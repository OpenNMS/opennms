<template>
  <FeatherSelect
    label="Location"
    data-test="snmp-monitoring-location-select"
    hint="Select the location"
    :options="monitoringLocations"
    :modelValue="selectedLocation"
    @update:modelValue="handleUpdate"
  >
    <FeatherIcon :icon="MoreVert" />
  </FeatherSelect>
</template>

<script setup lang="ts">
import { FeatherIcon } from '@featherds/icon'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'

const props = defineProps<{
  monitoringLocation?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ISelectItemType | undefined]
}>()

const store = useSnmpConfigStore()

const monitoringLocations = computed<ISelectItemType[]>(() => {
  return store.monitoringLocations.map(loc => {
    return {
      _text: loc.name,
      _value: loc.name
    }
  })
})

const selectedLocation = computed<ISelectItemType | undefined>(() => {
  if (!props.monitoringLocation) return undefined
  return monitoringLocations.value.find(loc => loc._value === props.monitoringLocation)
})

const handleUpdate = (value: ISelectItemType | undefined) => {
  emit('update:modelValue', value)
}
</script>
