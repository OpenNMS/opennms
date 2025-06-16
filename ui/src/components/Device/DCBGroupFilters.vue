<template>
  <div class="group-filters-container">
    <p class="title">Group By</p>

    <FeatherDropdown class="dropdown">
      <template v-slot:trigger>
        <FeatherButton secondary link href="#" menu-trigger>
          <template v-slot:icon>
            Vendor
            <FeatherIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
          </template>
        </FeatherButton>
      </template>
      <FeatherDropdownItem
        v-for="option of deviceStore.vendorOptions"
        :key="option"
        @click="onGroupByOptionClick('vendor', option)"
      >{{ option }}</FeatherDropdownItem>
    </FeatherDropdown>

    <FeatherDropdown class="dropdown dcb-group-filters-status-dropdown">
      <template v-slot:trigger>
        <FeatherButton secondary link href="#" menu-trigger>
          <template v-slot:icon>
            Backup Status
            <FeatherIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
          </template>
        </FeatherButton>
      </template>
      <FeatherDropdownItem
        v-for="option of deviceStore.backupStatusOptions"
        :key="option"
        @click="onGroupByOptionClick('status', option)"
      >
        <div class="option" :class="option.replace(' ', '').toLowerCase()">{{ option }}</div>
      </FeatherDropdownItem>
    </FeatherDropdown>

    <FeatherDropdown class="dropdown">
      <template v-slot:trigger>
        <FeatherButton secondary link href="#" menu-trigger>
          <template v-slot:icon>
            OS Image
            <FeatherIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
          </template>
        </FeatherButton>
      </template>
      <FeatherDropdownItem
        v-for="option of deviceStore.osImageOptions"
        :key="option"
        @click="onGroupByOptionClick('osImage', option)"
      >{{ option }}</FeatherDropdownItem>
    </FeatherDropdown>
  </div>
</template>

<script lang="ts" setup>
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import ArrowDown from '@featherds/icon/navigation/ArrowDropDown'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigQueryParams } from '@/types/deviceConfig'

const deviceStore = useDeviceStore()

const onGroupByOptionClick = (groupBy: string, value: string) => {
  const newQueryParams: DeviceConfigQueryParams = {
    limit: 20,
    offset: 0,
    groupBy: groupBy,
    groupByValue: value
  }

  deviceStore.updateDeviceConfigBackupQueryParams(newQueryParams)
  deviceStore.getDeviceConfigBackups()
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

.group-filters-container {
  display: flex;
  flex-direction: column;
  margin-left: 20px;
  margin-top: 63px;
  border: 1px solid var($shade-4);
  border-radius: 5px;
  padding: 15px;

  .title {
    @include headline4;
    margin-top: 0px;
  }

  .dropdown {
    margin-bottom: 15px;

    .option {
      height: 36px;
      line-height: 2.5;
      padding-left: 15px;
      text-transform: capitalize;
    }
    .btn {
      width: 100%;
    }
  }
}
</style>

<style lang="scss">
.dcb-group-filters-status-dropdown {
  .feather-dropdown {
    li {
      a {
        padding-left: 5px;
      }
    }
  }
}
</style>
