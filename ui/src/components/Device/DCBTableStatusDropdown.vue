<template>
  <FeatherDropdown class="pointer dcb-table-status-dropdown">
    <template v-slot:trigger>
      <span secondary link href="#" menu-trigger>
        Backup Status
        <FeatherIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
      </span>
    </template>
    <FeatherDropdownItem
      v-for="option of backupStatusOptions"
      :key="option"
      @click="filterByStatus(option)"
    >
      <div class="option" :class="option">
      {{ option === 'none' ? 'No Backup' : option }}
      </div>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import ArrowDown from '@featherds/icon/navigation/ArrowDropDown'
import { useStore } from 'vuex'
import { DeviceConfigQueryParams, status } from '@/types/deviceConfig'

const store = useStore()
const backupStatusOptions = computed<status[]>(() => store.state.deviceModule.backupStatusOptions)

const filterByStatus = (value: status) => {
  const newQueryParams: DeviceConfigQueryParams = {
    limit: 20,
    offset: 0,
    status: value
  }

  store.dispatch('deviceModule/updateDeviceConfigBackupQueryParams', newQueryParams)
  store.dispatch('deviceModule/getDeviceConfigBackups')
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";

.option {
  height: 36px;
  line-height: 2.5;
  padding-left: 15px;
  text-transform: capitalize;
}
</style>

<style lang="scss">
.dcb-table-status-dropdown {
  .feather-dropdown {
    li {
      a {
        padding-left: 5px;
      }
    }
  }
}
</style>
