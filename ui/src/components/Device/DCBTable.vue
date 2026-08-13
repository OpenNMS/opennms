<template>
  <div class="select-search">
    <div class="config-header">
      <div class="config-column">
        <div>Devices:</div>
        <div class="config-number">{{ deviceStore.deviceConfigTotal || 'N/A' }}</div>
      </div>
      <div class="divider"></div>
      <div class="config-column">
        <div>Selected:</div>
        <div class="config-number">{{ numberOfSelectedDevices }}</div>
      </div>
      <div class="divider"></div>
      <div class="config-column">
        <div>Configurations:</div>
        <div class="btn-container">
          <OnmsButton
            variant="text"
            class="dcb-action-btn"
            data-test="view-history-btn"
            @click="onViewHistory"
            :disabled="!singleConfigSelected"
          >
            <OnmsIcon :icon="History" class="btn-icon" />
            View History
          </OnmsButton>

          <OnmsButton
            variant="text"
            class="dcb-action-btn"
            data-test="download-btn"
            @click="onDownload"
            :disabled="noConfigsSelected"
          >
            <OnmsIcon :icon="Download" class="btn-icon" />
            Download
          </OnmsButton>

          <OnmsButton
            variant="text"
            class="dcb-action-btn"
            data-test="backup-now-btn"
            @click="onBackupNow"
            :disabled="noConfigsSelected || singleConfigSelectedHasNoServiceName"
          >
            <OnmsIcon :icon="Backup" class="btn-icon" />
            Backup
          </OnmsButton>

          <OnmsButton
            variant="text"
            class="dcb-action-btn"
            data-test="compare-btn"
            @click="onCompare"
            :disabled="!singleConfigSelected"
          >
            <OnmsIcon :icon="Compare" class="btn-icon" />
            Compare
          </OnmsButton>
        </div>
      </div>
    </div>
  </div>

  <div class="dcb-table">
    <OnmsTable
      :value="deviceStore.deviceConfigBackups"
      dataKey="id"
      lazy
      paginator
      :rows="rows"
      :rowsPerPageOptions="[20, 50, 100, 200]"
      :first="first"
      :totalRecords="deviceStore.deviceConfigTotal"
      stripedRows
      size="small"
      :sortField="sortField"
      :sortOrder="sortOrder"
      @page="onPage"
      @sort="onSort"
      aria-label="Device Config Backup"
    >
      <OnmsColumn>
        <template #header>
          <OnmsCheckbox
            inputId="dcb-select-all"
            aria-label="Select all configurations"
            :modelValue="all"
            @update:modelValue="onSelectAll"
            data-test="all-checkbox"
            class="dcb-all-checkbox"
          />
        </template>
        <template #body="{ data }">
          <OnmsCheckbox
            class="dcb-config-checkbox"
            :disabled="all"
            :aria-label="`Select ${data.deviceName}`"
            :modelValue="all || selectedDeviceConfigBackups[data.id]"
            @update:modelValue="() => selectCheckbox(data)"
          />
        </template>
      </OnmsColumn>

      <OnmsColumn field="deviceName" header="Node Name" sortable>
        <template #body="{ data }">
          <a
            :href="computeNodeLink(data.nodeId)"
            @click="onNodeLinkClick(data.nodeId)"
          >
            {{ data.deviceName }}
            <OnmsIcon
              v-if="data.configType !== 'default'"
              :icon="Speed"
              v-onms-tooltip="data.configName"
            />
          </a>
        </template>
      </OnmsColumn>

      <OnmsColumn field="ipAddress" header="IP Address" sortable />
      <OnmsColumn field="location" header="Location" sortable />

      <OnmsColumn field="lastBackup" header="Last Backup Date" sortable>
        <template #body="{ data }">
          <span
            class="last-backup-date pointer"
            @click="onLastBackupDateClick(data)"
            v-onms-tooltip="'View config'"
          >
            <span v-date>{{ data.lastBackupDate }}</span>
          </span>
        </template>
      </OnmsColumn>

      <OnmsColumn field="lastUpdated" header="Last Attempted" sortable>
        <template #body="{ data }">
          <span v-date>{{ data.lastUpdatedDate }}</span>
        </template>
      </OnmsColumn>

      <OnmsColumn>
        <template #header>
          <DCBTableStatusDropdown />
        </template>
        <template #body="{ data }">
          <div :class="data.backupStatus" class="option">
            {{ data.backupStatus === 'none' ? 'No Backup' : data.backupStatus }}
          </div>
        </template>
      </OnmsColumn>

      <OnmsColumn header="Schedule Date">
        <template #body="{ data }">
          <span v-date>{{ data.nextScheduledBackupDate }}</span>
        </template>
      </OnmsColumn>

      <OnmsColumn header="Schedule Interval">
        <template #body="{ data }">
          {{ Object.values(data.scheduledInterval)[0] }}
        </template>
      </OnmsColumn>
    </OnmsTable>
  </div>

  <DCBModal
    @close="dcbModalVisible = false"
    :visible="dcbModalVisible"
  >
    <template v-slot:content>
      <DCBModalViewHistoryContentVue
        @onCompare="onCompare"
        v-if="dcbModalVisible && dcbModalContentComponentName === DCBModalContentComponentNames.DCBModalViewHistoryContent"
      />
      <DCBModalLastBackupContent
        v-if="dcbModalVisible && dcbModalContentComponentName === DCBModalContentComponentNames.DCBModalLastBackupContent"
      />
      <DCBModalConfigDiffContent
        v-if="dcbModalVisible && dcbModalContentComponentName === DCBModalContentComponentNames.DCBModalConfigDiffContent"
      />
    </template>
  </DCBModal>
</template>

<script
  setup
  lang="ts"
>
import { computed, ref } from 'vue'

import { OnmsButton, OnmsCheckbox, OnmsIcon, OnmsTable, OnmsColumn, type OnmsTablePageEvent, type OnmsTableSortEvent } from '@opennms/onms-ui'
import { SORT } from '@/types'
import History from '@/components/icons/action/Restore.vue'
import Download from '@/components/icons/action/DownloadFile.vue'
import Backup from '@/assets/Backup.vue'
import Compare from '@/assets/Compare.vue'
import Speed from '@/assets/Speed.vue'
import DCBModal from './DCBModal.vue'
import DCBModalLastBackupContent from './DCBModalLastBackupContent.vue'
import DCBModalViewHistoryContentVue from './DCBModalViewHistoryContent.vue'
import DCBModalConfigDiffContent from './DCBModalConfigDiffContent.vue'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'
import DCBTableStatusDropdown from './DCBTableStatusDropdown.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu } from '@/types/mainMenu'

enum DCBModalContentComponentNames {
  DCBModalLastBackupContent = 'DCBModalLastBackupContent',
  DCBModalViewHistoryContent = 'DCBModalViewHistoryContent',
  DCBModalConfigDiffContent = 'DCBModalConfigDiffContent'
}

const deviceStore = useDeviceStore()
const menuStore = useMenuStore()
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const dcbModalVisible = ref(false)
const dcbModalContentComponentName = ref('')
const all = ref(false)
const defaultQuerySize = 20
const selectedDeviceConfigBackups = ref<Record<string, boolean>>({})
const sortField = ref('deviceName')
const sortOrder = ref(1)
const rows = ref(deviceStore.deviceConfigBackupQueryParams.limit || defaultQuerySize)
const first = computed(() => deviceStore.deviceConfigBackupQueryParams.offset || 0)

const computeNodeLink = (nodeId: number) => {
  return `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}${nodeId}`
}

const onNodeLinkClick = (nodeId: number) => {
  window.location.assign(computeNodeLink(nodeId))
}

const selectedDeviceConfigIds = computed<number[]>(() => {
  return Object.keys(selectedDeviceConfigBackups.value)
    .filter(id => selectedDeviceConfigBackups.value[id])
    .map(id => parseInt(id))
})

const numberOfSelectedDevices = computed<number>(() => {
  if (all.value) {
    return deviceStore.deviceConfigTotal
  }

  return selectedDeviceConfigIds.value.length
})

// for enabling / disabling table buttons (history, backup, d/l, compare...)
const noConfigsSelected = computed<boolean>(() => (selectedDeviceConfigIds.value.length === 0 && !all.value) || (all.value && !deviceStore.deviceConfigBackups.length))
const singleConfigSelected = computed<boolean>(() => (!all.value && selectedDeviceConfigIds.value.length === 1) || (all.value && deviceStore.deviceConfigBackups.length === 1))

const singleConfigSelectedHasNoServiceName = computed<boolean>(() => {
  const backupId = selectedDeviceConfigIds.value?.length > 0 ? selectedDeviceConfigIds.value[0] : null
  return singleConfigSelected.value && backupId !== null && !getDeviceConfigBackupById(backupId)?.serviceName
})

const getDeviceConfigBackupById = (id: number) => deviceStore.deviceConfigBackups.filter(backup => backup.id === id)[0]

const onPage = (event: OnmsTablePageEvent) => {
  rows.value = event.rows
  // updateDeviceConfigBackupQueryParams merges, so orderBy/order set by
  // onSort (and any filter params set elsewhere) are preserved.
  deviceStore.updateDeviceConfigBackupQueryParams({
    limit: event.rows,
    offset: event.first
  })
  deviceStore.getDeviceConfigBackups()
}

const onSort = (event: OnmsTableSortEvent) => {
  sortField.value = event.sortField as string
  sortOrder.value = (event.sortOrder as number) ?? 1

  const newQueryParams: DeviceConfigQueryParams = {
    limit: rows.value,
    offset: 0,
    order: sortOrder.value === 1 ? SORT.ASCENDING : SORT.DESCENDING,
    orderBy: sortField.value
  }

  deviceStore.updateDeviceConfigBackupQueryParams(newQueryParams)
  deviceStore.getDeviceConfigBackups()
}

const onSelectAll = (val: boolean) => {
  all.value = val
  selectAll()
}

const selectAll = () => {
  if (all.value) {
    deviceStore.setSelectedIds('all')
  } else {
    clearAllSelectedDevices()
  }
}

const clearAllSelectedDevices = () => {
  selectedDeviceConfigBackups.value = {}
  deviceStore.setSelectedIds(selectedDeviceConfigIds.value)
}

const selectCheckbox = (config: DeviceConfigBackup) => {
  selectedDeviceConfigBackups.value[config.id] = !selectedDeviceConfigBackups.value[config.id]
  deviceStore.setSelectedIds(selectedDeviceConfigIds.value)
}

const onDownload = () => deviceStore.downloadSelectedDevices()
const onBackupNow = () => deviceStore.backupSelectedDevices()

const onViewHistory = () => {
  dcbModalContentComponentName.value = DCBModalContentComponentNames.DCBModalViewHistoryContent
  dcbModalVisible.value = true
}

const onCompare = () => {
  dcbModalContentComponentName.value = DCBModalContentComponentNames.DCBModalConfigDiffContent
  dcbModalVisible.value = true
}

const onLastBackupDateClick = (config: DeviceConfigBackup) => {
  deviceStore.setModalDeviceConfigBackup(config)
  dcbModalContentComponentName.value = DCBModalContentComponentNames.DCBModalLastBackupContent
  dcbModalVisible.value = true
}
</script>

<style
  lang="scss"
  scoped
>
@import '@/styles/onms-typography';

.dcb-table {
  :deep(.last-backup-date) {
    color: var(--p-primary-color);

    span:hover {
      font-weight: 600;
    }
  }

  :deep(.option) {
    margin-left: 8px;
    height: 43px;
    line-height: 3.5;
    padding-left: 15px;
    text-transform: capitalize;
  }
}

.btn-container {
  .dcb-action-btn {
    // PrimeVue buttons are inline-flex; keep the icon centered against its label.
    align-items: center;
  }

  // Normalize every action-button icon to 1.5em and strip the per-asset sizing
  // and margin quirks: assets/Compare.vue hardcodes 24px + margin-top: -10px and
  // assets/Backup.vue adds margin-top: -10px, which broke both size parity (the
  // Compare icon rendered 24px vs the others' ~18px) and vertical alignment.
  // Handles OnmsIcon SVGs rendered directly and the asset wrappers nesting an <svg>.
  .btn-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 1.5em;
    height: 1.5em;
    margin: 0 0.4rem 0 0;

    :deep(svg) {
      width: 1.5em;
      height: 1.5em;
      margin: 0;
    }
  }
}

.select-search {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;

  .config-header {
    display: flex;
    flex-direction: row;
    @include onms-subtitle2;

    .config-column {
      display: flex;
      flex-direction: column;
      margin-left: 20px;

      .config-number {
        margin-top: 5px;
      }
    }

    .divider {
      height: 46px;
      margin: 0px 13px 0px 35px;
      border-left: 1px solid var(--p-content-border-color);
    }
  }
}
a:visited {
  color: var(--p-primary-color) !important;
}
</style>
