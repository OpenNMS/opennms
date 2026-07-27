<template>
  <FormField class="dcb-search-field">
    <IconField>
      <PInputText
        placeholder="Search device"
        aria-label="Search device"
        :modelValue="searchVal"
        @update:modelValue="(val) => searchFilterHandler(val as string)"
      />
      <InputIcon>
        <OnmsIcon :icon="SearchIcon" />
      </InputIcon>
    </IconField>
  </FormField>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import InputText from 'primevue/inputtext'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import SearchIcon from '@/components/icons/action/Search.vue'
import FormField from '@/components/Common/FormField.vue'
import { useDebounceFn } from '@vueuse/core'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigQueryParams } from '@/types/deviceConfig'

const PInputText = InputText

const deviceStore = useDeviceStore()
const searchVal = ref<string | undefined>(undefined)

const searchFilterHandler = (val = '') => {
  if (searchVal.value === undefined && val === '') {
    return
  } // prevents dup mounted call
  searchVal.value = val

  const newQueryParams: DeviceConfigQueryParams = {
    limit: 20,
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

  // make the input (and its IconField wrapper) fill the field so the
  // search icon sits at the input's right edge
  :deep(.p-iconfield) {
    display: block;
    width: 100%;
  }

  :deep(.p-inputtext) {
    width: 100%;
    padding-right: 2.75rem;
  }
}
</style>
