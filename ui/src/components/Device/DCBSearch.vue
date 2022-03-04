<template>
  <FeatherInput
    :modelValue="searchVal"
    @update:modelValue="searchFilterHandler"
    label="Search device"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { useDebounceFn } from '@vueuse/core'
import { QueryParameters } from '@/types'

const store = useStore()
const searchVal = ref<string | undefined>(undefined)

const searchFilterHandler = (val = '') => {
  if (searchVal.value === undefined && val === '') return // prevents dup mounted call from feather
  searchVal.value = val

  const newQueryParams: QueryParameters = {
    limit: 20,
    offset: 0,
    search: val
  }

  store.dispatch('deviceModule/updateDeviceConfigBackupQueryParams', newQueryParams)
  getDeviceConfigBackupsOnDebounce()
}


// TODO: return scroll bar to top before running, so infinite scroll won't trigger after search
const getDeviceConfigBackupsOnDebounce = useDebounceFn(() => store.dispatch('deviceModule/getDeviceConfigBackups'), 1000)
</script>

<style scoped lang="scss">
</style>
