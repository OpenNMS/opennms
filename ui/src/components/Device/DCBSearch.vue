<template>
  <FormField class="dcb-search-field">
    <OnmsSearchInput
      placeholder="Search device"
      aria-label="Search device"
      :modelValue="searchVal"
      @update:modelValue="(val) => searchFilterHandler(val as string)"
    />
  </FormField>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsSearchInput } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { useDebounceFn } from '@vueuse/core'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigQueryParams } from '@/types/deviceConfig'

const deviceStore = useDeviceStore()
const searchVal = ref<string | undefined>(undefined)

const searchFilterHandler = (val = '') => {
  if (searchVal.value === undefined && val === '') {
    return
  } // prevents dup mounted call
  searchVal.value = val

  // omit limit: updateDeviceConfigBackupQueryParams merges, so the store's
  // current page size (set by the paginator, or the 20 default) survives.
  const newQueryParams: DeviceConfigQueryParams = {
    offset: 0,
    search: val
  }

  deviceStore.updateDeviceConfigBackupQueryParams(newQueryParams)
  getDeviceConfigBackupsOnDebounce()
}

// TODO: return scroll bar to top before running, so infinite scroll won't trigger after search
const getDeviceConfigBackupsOnDebounce = useDebounceFn(() => deviceStore.getDeviceConfigBackups(), 1000)
</script>

<style scoped lang="scss">
.dcb-search-field {
  width: 100%;
}
</style>
