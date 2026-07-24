<template>
  <div class="group-filters-container">
    <p class="title">Group By</p>

    <div class="dropdown">
      <OnmsButton variant="text" class="btn" menu-trigger @click="toggleMenu($event, vendorMenu)">
        Vendor
        <OnmsIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
      </OnmsButton>
      <PMenu ref="vendorMenu" :model="vendorItems" :popup="true" />
    </div>

    <div class="dropdown">
      <OnmsButton variant="text" class="btn" menu-trigger @click="toggleMenu($event, statusMenu)">
        Backup Status
        <OnmsIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
      </OnmsButton>
      <PMenu ref="statusMenu" :model="statusItems" :popup="true">
        <template #item="{ item, props }">
          <a v-bind="props.action">
            <div class="option" :class="item.statusClass">{{ item.label }}</div>
          </a>
        </template>
      </PMenu>
    </div>

    <div class="dropdown">
      <OnmsButton variant="text" class="btn" menu-trigger @click="toggleMenu($event, osImageMenu)">
        OS Image
        <OnmsIcon :icon="ArrowDown" aria-hidden="true" focusable="false" />
      </OnmsButton>
      <PMenu ref="osImageMenu" :model="osImageItems" :popup="true" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'

import { OnmsButton } from '@opennms/onms-ui'
import Menu from 'primevue/menu'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import ArrowDown from '@/components/icons/navigation/ArrowDropDown.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigQueryParams } from '@/types/deviceConfig'

const PMenu = Menu

const deviceStore = useDeviceStore()

const vendorMenu = ref()
const statusMenu = ref()
const osImageMenu = ref()

const vendorItems = computed(() => deviceStore.vendorOptions.map((option: string) => ({
  label: option,
  command: () => onGroupByOptionClick('vendor', option)
})))

const statusItems = computed(() => deviceStore.backupStatusOptions.map((option: string) => ({
  label: option,
  statusClass: option.replace(' ', '').toLowerCase(),
  command: () => onGroupByOptionClick('status', option)
})))

const osImageItems = computed(() => deviceStore.osImageOptions.map((option: string) => ({
  label: option,
  command: () => onGroupByOptionClick('osImage', option)
})))

const toggleMenu = (event: Event, menuRef: { toggle: (e: Event) => void }) => menuRef.toggle(event)

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
@import '@/styles/onms-typography';

.group-filters-container {
  display: flex;
  flex-direction: column;
  margin-left: 20px;
  margin-top: 63px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  padding: 15px;

  .title {
    @include onms-headline4;
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
