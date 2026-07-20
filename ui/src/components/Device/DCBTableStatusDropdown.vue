<template>
  <span
    class="pointer dcb-table-status-trigger"
    menu-trigger
    aria-haspopup="true"
    tabindex="0"
    @keydown.enter.prevent="toggleMenu"
    @keydown.space.prevent="toggleMenu"
    @click="toggleMenu"
  >
    Backup Status
    <FeatherIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
  </span>
  <PMenu ref="menu" :model="menuItems" :popup="true" class="dcb-table-status-dropdown">
    <template #item="{ item, props }">
      <a v-bind="props.action">
        <div class="option" :class="item.statusClass">{{ item.label }}</div>
      </a>
    </template>
  </PMenu>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import Menu from 'primevue/menu'
import { FeatherIcon } from '@featherds/icon'
import ArrowDown from '@featherds/icon/navigation/ArrowDropDown'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigQueryParams, status } from '@/types/deviceConfig'

const PMenu = Menu

const deviceStore = useDeviceStore()
const menu = ref()

const menuItems = computed(() => deviceStore.backupStatusOptions.map((option: status) => ({
  label: option === 'NONE' ? 'No Backup' : option.toLowerCase(),
  statusClass: option.toLowerCase(),
  command: () => filterByStatus(option)
})))

const toggleMenu = (event: Event) => menu.value.toggle(event)

const filterByStatus = (value: status) => {
  const newQueryParams: DeviceConfigQueryParams = {
    limit: 20,
    offset: 0,
    status: value
  }

  deviceStore.updateDeviceConfigBackupQueryParams(newQueryParams)
  deviceStore.getDeviceConfigBackups()
}
</script>

<style scoped lang="scss">
.dcb-table-status-trigger {
  display: inline-flex;
  align-items: center;
}
</style>

<!-- Menu teleports to body, so the option status-bar styling must be global -->
<style lang="scss">
@mixin status-bar($color) {
  background: $color;
  background: linear-gradient(90deg, $color 1%, rgba(255, 255, 255, 0) 9%);
}

.dcb-table-status-dropdown {
  .option {
    height: 36px;
    line-height: 2.5;
    padding-left: 15px;
    text-transform: capitalize;

    &.success {
      @include status-bar(var(--p-green-500));
    }
    &.failed {
      @include status-bar(var(--p-red-500));
    }
    &.none {
      @include status-bar(var(--p-datatable-header-cell-background));
    }
  }
}
</style>
