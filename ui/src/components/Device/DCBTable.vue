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
          <FeatherButton
            data-test="view-history-btn"
            @click="onViewHistory"
            :disabled="!singleConfigSelected"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="History" />
            </template>
            View History
          </FeatherButton>

          <FeatherButton
            data-test="download-btn"
            @click="onDownload"
            :disabled="noConfigsSelected"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="Download" />
            </template>
            Download
          </FeatherButton>

          <FeatherButton
            data-test="backup-now-btn"
            @click="onBackupNow"
            :disabled="noConfigsSelected || singleConfigSelectedHasNoServiceName"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="Backup" />
            </template>
            Backup
          </FeatherButton>

          <FeatherButton
            data-test="compare-btn"
            @click="onCompare"
            :disabled="!singleConfigSelected"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="Compare" />
            </template>
            Compare
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>

  <div
    ref="tableWrap"
    id="wrap"
    class="dcb-table"
  >
    <table summary="Device Config Backup">
      <thead>
        <tr>
          <th>
            <FeatherCheckbox
              v-model="all"
              @update:modelValue="selectAll"
              data-test="all-checkbox"
              class="dcb-all-checkbox"
            />
          </th>
          <FeatherSortHeader
            scope="col"
            property="deviceName"
            :sort="sortStates.deviceName"
            v-on:sort-changed="sortByColumnHandler"
            >Node Name</FeatherSortHeader
          >

          <FeatherSortHeader
            scope="col"
            property="ipAddress"
            :sort="sortStates.ipAddress"
            v-on:sort-changed="sortByColumnHandler"
            >IP Address</FeatherSortHeader
          >

          <FeatherSortHeader
            scope="col"
            property="location"
            :sort="sortStates.location"
            v-on:sort-changed="sortByColumnHandler"
            >Location</FeatherSortHeader
          >

          <FeatherSortHeader
            scope="col"
            property="lastBackup"
            :sort="sortStates.lastBackup"
            v-on:sort-changed="sortByColumnHandler"
            >Last Backup Date</FeatherSortHeader
          >

          <FeatherSortHeader
            scope="col"
            property="lastUpdated"
            :sort="sortStates.lastUpdated"
            v-on:sort-changed="sortByColumnHandler"
            >Last Attempted</FeatherSortHeader
          >

          <th>
            <DCBTableStatusDropdown />
          </th>

          <th>Schedule Date</th>
          <th>Schedule Interval</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="config in deviceStore.deviceConfigBackups"
          :key="config.id"
        >
          <td>
            <FeatherCheckbox
              class="dcb-config-checkbox"
              @update:modelValue="selectCheckbox(config)"
              :modelValue="all || selectedDeviceConfigBackups[config.id]"
            />
          </td>
          <td>
            <a
              :href="computeNodeLink(config.nodeId)"
              @click="onNodeLinkClick(config.nodeId)"
              target="_blank">
            <!--
            <router-link
              :to="`/node/${config.nodeId}`"
              target="_blank"
            >
            -->
              {{ config.deviceName }}
              <FeatherTooltip
                :title="config.configName"
                v-slot="{ attrs, on }">
                <FeatherIcon
                  v-bind="attrs" v-on="on"
                  v-if="config.configType !== 'default'"
                  :icon="Speed"
                />
              </FeatherTooltip>
            </a>
          </td>
          <td>{{ config.ipAddress }}</td>
          <td>{{ config.location }}</td>
          <td
            class="last-backup-date pointer"
            @click="onLastBackupDateClick(config)"
          >
            <FeatherTooltip
              title="View config"
              v-slot="{ attrs, on }">
              <span
                v-bind="attrs" 
                v-on="on"
                v-date
                >{{ config.lastBackupDate }}
              </span>
            </FeatherTooltip>
          </td>
          <td v-date>{{ config.lastUpdatedDate }}</td>
          <td>
            <div
              :class="config.backupStatus"
              class="option"
            >
              {{ config.backupStatus === 'none' ? 'No Backup' : config.backupStatus }}
            </div>
          </td>
          <td v-date>{{ config.nextScheduledBackupDate }}</td>
          <td>{{ Object.values(config.scheduledInterval)[0] }}</td>
        </tr>
      </tbody>
    </table>
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
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherTooltip } from '@featherds/tooltip'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import History from '@featherds/icon/action/Restore'
import Download from '@featherds/icon/action/DownloadFile'
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
const tableWrap = ref<HTMLElement | null>(null)
const defaultQuerySize = 20
const selectedDeviceConfigBackups = ref<Record<string, boolean>>({})
const sortStates: Record<string, SORT> = reactive({
  deviceName: SORT.ASCENDING,
  ipAddress: SORT.NONE,
  location: SORT.NONE,
  lastBackup: SORT.NONE,
  lastUpdated: SORT.NONE
})
const { arrivedState, directions } = useScroll(tableWrap.value as HTMLElement, {
  offset: { bottom: 300 }
})

const computeNodeLink = (nodeId: number) => {
  return `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}${nodeId}`
}

const onNodeLinkClick = (nodeId: number) => {
  window.location.assign(computeNodeLink(nodeId))
}

watch(() => directions.bottom, () => {
  if (!directions.bottom && arrivedState.bottom) {
    getMoreDeviceConfigBackups()
  }
})

const selectedDeviceConfigIds = computed<number[]>(() => {
  return Object.keys(selectedDeviceConfigBackups.value)
    .filter((id) => selectedDeviceConfigBackups.value[id])
    .map((id) => parseInt(id))
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
const singleConfigSelectedHasNoServiceName = computed<boolean>(() => singleConfigSelected.value && !getDeviceConfigBackupById(selectedDeviceConfigIds.value[0]).serviceName)
const getDeviceConfigBackupById = (id: number) => deviceStore.deviceConfigBackups.filter((backup) => backup.id === id)[0]

const sortByColumnHandler = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[`${sortObj.property}`] = sortObj.value

  const newQueryParams: DeviceConfigQueryParams = {
    limit: defaultQuerySize,
    offset: 0,
    order: sortObj.value,
    orderBy: sortObj.property
  }

  deviceStore.updateDeviceConfigBackupQueryParams(newQueryParams)
  deviceStore.getDeviceConfigBackups()
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

const getMoreDeviceConfigBackups = () => {
  const newQueryParams: DeviceConfigQueryParams = {
    limit: (deviceStore.deviceConfigBackupQueryParams.limit || 0) + defaultQuerySize,
    offset: (deviceStore.deviceConfigBackupQueryParams.offset || 0) + defaultQuerySize
  }

  deviceStore.updateDeviceConfigBackupQueryParams(newQueryParams)
  deviceStore.getAndMergeDeviceConfigBackups()
}

onMounted(() => {
  const wrap = document.getElementById('wrap')
  const thead = document.querySelector('thead')

  if (wrap && thead) {
    wrap.addEventListener('scroll', function () {
      const translate = 'translate(0,' + this.scrollTop + 'px)'
      thead.style.transform = translate
    })
  }
})
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

#wrap {
  height: calc(100vh - 310px);
  overflow: auto;
  white-space: nowrap;

  table {
    margin-top: 0px !important;
    font-size: 12px !important;
    @include table;
    @include table-condensed;
    @include row-striped;

    .last-backup-date {
      color: var($primary);
      
      span:hover {
        font-weight: 600;
      }
    }

    .option {
      margin-left: 8px;
      height: 43px;
      line-height: 3.5;
      padding-left: 15px;
      text-transform: capitalize;
    }
  }

  thead {
    z-index: 2;
    position: relative;
    background: var($surface);
  }
}

.select-search {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;

  .config-header {
    display: flex;
    flex-direction: row;
    font-family: var($font-family);
    @include subtitle2;

    .config-column {
      display: flex;
      flex-direction: column;
      margin-left: 20px;

      .config-number {
        margin-top: 5px;
      }

      .btn-container {
        .btn {
          margin-top: 0px;
        }
      }
    }

    .divider {
      height: 46px;
      margin: 0px 13px 0px 35px;
      border-left: 1px solid var($shade-4);
    }
  }
}
a:visited {
  color: var($clickable-normal) !important;
}
</style>

<style lang="scss">
.dcb-config-checkbox, 
.dcb-all-checkbox {
  margin-bottom: 0px !important;
}
.dcb-config-checkbox {
  label {
    display: none;
  }
}
.dcb-table {
  .feather-checkbox {
    width: 20px;
  }
}
</style>
