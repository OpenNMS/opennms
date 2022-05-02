<template>
  <div class="select-search">
    <div class="config-header">
      <div class="config-column">
        <div>Devices:</div>
        <div class="config-number">{{ totalCountOfDeviceConfigBackups }}</div>
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
            :disabled="(!all && selectedDeviceConfigIds.length !== 1) || (all && deviceConfigBackups.length !== 1)"
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
            :disabled="(selectedDeviceConfigIds.length === 0 && !all) || (all && !deviceConfigBackups.length)"
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
            :disabled="(selectedDeviceConfigIds.length === 0 && !all) || (all && !deviceConfigBackups.length)"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="Backup" />
            </template>
            Backup Now
          </FeatherButton>

          <FeatherButton
            data-test="compare-btn"
            @click="onCompare"
            :disabled="(!all && selectedDeviceConfigIds.length !== 1) || (all && deviceConfigBackups.length !== 1)"
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
    <DCBSearch class="dcb-search" />
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

          <FeatherSortHeader
            scope="col"
            property="scheduleDate"
            :sort="sortStates.scheduleDate"
            v-on:sort-changed="sortByColumnHandler"
            >Schedule Date</FeatherSortHeader
          >

          <FeatherSortHeader
            scope="col"
            property="scheduleInterval"
            :sort="sortStates.scheduleInterval"
            v-on:sort-changed="sortByColumnHandler"
            >Schedule Interval</FeatherSortHeader
          >
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="config in deviceConfigBackups"
          :key="config.id"
        >
          <td>
            <FeatherCheckbox
              class="device-config-checkbox"
              @update:modelValue="selectCheckbox(config)"
              :modelValue="all || selectedDeviceConfigBackups[config.id]"
            />
          </td>
          <td>
            <router-link
              :to="`/node/${config.nodeId}`"
              target="_blank"
            >
              {{ config.deviceName }}
              <span title="Running Configuration">
                <FeatherIcon
                  v-if="config.configType !== 'default'"
                  :icon="Speed"
                />
              </span>
            </router-link>
          </td>
          <td>{{ config.ipAddress }}</td>
          <td>{{ config.location }}</td>
          <td
            class="last-backup-date pointer"
            @click="onLastBackupDateClick(config)"
          >
            <span
              title="View config"
              v-date
              >{{ config.lastBackupDate }}</span
            >
            <span
              title="View config"
              v-if="config.lastBackupDate"
            >
              <FeatherIcon
                :icon="ViewDetails"
                class="view-config"
              />
            </span>
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
import { useStore } from 'vuex'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import History from '@featherds/icon/action/Restore'
import Download from '@featherds/icon/action/DownloadFile'
import Backup from '@featherds/icon/action/Cycle'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import Compare from '@featherds/icon/action/ContentCopy'
import Speed from './icons/Speed.vue'
import DCBSearch from '@/components/Device/DCBSearch.vue'
import DCBModal from './DCBModal.vue'
import DCBModalLastBackupContent from './DCBModalLastBackupContent.vue'
import DCBModalViewHistoryContentVue from './DCBModalViewHistoryContent.vue'
import DCBModalConfigDiffContent from './DCBModalConfigDiffContent.vue'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'
import DCBTableStatusDropdown from './DCBTableStatusDropdown.vue'

enum DCBModalContentComponentNames {
  DCBModalLastBackupContent = 'DCBModalLastBackupContent',
  DCBModalViewHistoryContent = 'DCBModalViewHistoryContent',
  DCBModalConfigDiffContent = 'DCBModalConfigDiffContent'
}

const store = useStore()
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
  lastUpdated: SORT.NONE,
  scheduleDate: SORT.NONE,
  scheduleInterval: SORT.NONE
})
const { arrivedState, directions } = useScroll(tableWrap, {
  offset: { bottom: 300 }
})

watch(() => directions.bottom, () => {
  if (!directions.bottom && arrivedState.bottom) {
    getMoreDeviceConfigBackups()
  }
})

const deviceConfigBackups = computed<DeviceConfigBackup[]>(() => store.state.deviceModule.deviceConfigBackups)
const totalCountOfDeviceConfigBackups = computed(() => store.state.deviceModule.deviceConfigTotal)
const deviceConfigBackupQueryParams = computed<DeviceConfigQueryParams>(() => store.state.deviceModule.deviceConfigBackupQueryParams)
const selectedDeviceConfigIds = computed<number[]>(() => {
  return Object.keys(selectedDeviceConfigBackups.value)
    .filter((id) => selectedDeviceConfigBackups.value[id])
    .map((id) => parseInt(id))
})

const numberOfSelectedDevices = computed(() => {
  if (all.value) {
    return totalCountOfDeviceConfigBackups.value
  }

  return selectedDeviceConfigIds.value.length
})

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

  store.dispatch('deviceModule/updateDeviceConfigBackupQueryParams', newQueryParams)
  store.dispatch('deviceModule/getDeviceConfigBackups')
}

const selectAll = () => {
  if (all.value) {
    store.dispatch('deviceModule/setSelectedIds', 'all')
  } else {
    clearAllSelectedDevices()
  }
}

const clearAllSelectedDevices = () => {
  selectedDeviceConfigBackups.value = {}
  store.dispatch('deviceModule/setSelectedIds', selectedDeviceConfigIds.value)
}

const selectCheckbox = (config: DeviceConfigBackup) => {
  selectedDeviceConfigBackups.value[config.id] = !selectedDeviceConfigBackups.value[config.id]
  store.dispatch('deviceModule/setSelectedIds', selectedDeviceConfigIds.value)
}

const onDownload = () => store.dispatch('deviceModule/downloadSelectedDevices')
const onBackupNow = () => store.dispatch('deviceModule/backupSelectedDevices')

const onViewHistory = () => {
  dcbModalContentComponentName.value = DCBModalContentComponentNames.DCBModalViewHistoryContent
  dcbModalVisible.value = true
}

const onCompare = () => {
  dcbModalContentComponentName.value = DCBModalContentComponentNames.DCBModalConfigDiffContent
  dcbModalVisible.value = true
}

const onLastBackupDateClick = (config: DeviceConfigBackup) => {
  store.dispatch('deviceModule/setModalDeviceConfigBackup', config)
  dcbModalContentComponentName.value = DCBModalContentComponentNames.DCBModalLastBackupContent
  dcbModalVisible.value = true
}

const getMoreDeviceConfigBackups = () => {
  const newQueryParams: DeviceConfigQueryParams = {
    limit: (deviceConfigBackupQueryParams.value.limit || 0) + defaultQuerySize,
    offset: (deviceConfigBackupQueryParams.value.offset || 0) + defaultQuerySize
  }

  store.dispatch('deviceModule/updateDeviceConfigBackupQueryParams', newQueryParams)
  store.dispatch('deviceModule/getAndMergeDeviceConfigBackups')
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
    }

    .option {
      height: 43px;
      line-height: 3.5;
      padding-left: 15px;
      text-transform: capitalize;
    }

    .view-config {
      margin-left: 4px;
      height: 16px;
      width: 16px;
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

  .dcb-search {
    width: 250px;
    padding: 0px;
  }
}
</style>

<style lang="scss">
.dcb-search {
  .feather-input-content {
    margin-top: 0px;
  }
}
.device-config-checkbox {
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

