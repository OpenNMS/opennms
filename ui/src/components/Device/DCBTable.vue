<template>
  <div class="select-search">
    <div class="config-header">
      <div class="config-column">
        <div>Devices:</div>
        <div class="config-number">100</div>
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
            @click="onDownload"
            :disabled="selectedDeviceConfigIds.length === 0 && !all"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="Download" />
            </template>
            Download
          </FeatherButton>

          <FeatherButton
            @click="onBackupNow"
            :disabled="selectedDeviceConfigIds.length === 0 && !all"
            text
          >
            <template v-slot:icon>
              <FeatherIcon :icon="Backup" />
            </template>
            Backup Now
          </FeatherButton>
        </div>
      </div>
    </div>
    <DCBSearch class="dcb-search" />
  </div>

  <div ref="tableWrap" id="wrap">
    <table class="tl1 tl2 tl3 tl4 tl5 tl6 tl7 tc8" summary="Device Config Backup">
      <thead>
        <tr>
          <th>
            <FeatherCheckbox v-model="all" @update:modelValue="selectAll" />
          </th>
          <FeatherSortHeader
            scope="col"
            property="name"
            :sort="sortStates.name"
            v-on:sort-changed="sortByColumnHandler"
          >Node Name</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="ipAddress"
            :sort="sortStates.ipAddress"
            v-on:sort-changed="sortByColumnHandler"
          >IP Address</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="location"
            :sort="sortStates.location"
            v-on:sort-changed="sortByColumnHandler"
          >Location</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="lastBackup"
            :sort="sortStates.lastBackup"
            v-on:sort-changed="sortByColumnHandler"
          >Last Backup Date</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="lastAttempted"
            :sort="sortStates.lastAttempted"
            v-on:sort-changed="sortByColumnHandler"
          >Last Attempted</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="backupStatus"
            :sort="sortStates.backupStatus"
            v-on:sort-changed="sortByColumnHandler"
          >Backup Status</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="scheduleDate"
            :sort="sortStates.scheduleDate"
            v-on:sort-changed="sortByColumnHandler"
          >Scheduled Date</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="scheduleInterval"
            :sort="sortStates.scheduleInterval"
            v-on:sort-changed="sortByColumnHandler"
          >Scheduled Interval</FeatherSortHeader>
        </tr>
      </thead>
      <tbody>
        <tr v-for="config in deviceConfigBackups" :key="config.id">
          <td>
            <FeatherCheckbox
              class="device-config-checkbox"
              @update:modelValue="selectCheckbox(config)"
              :modelValue="all || selectedDeviceConfigBackups[config.id]"
            />
          </td>
          <td>
            <router-link :to="`/node/${config.id}`">{{ config.deviceName }}</router-link>
          </td>
          <td>{{ config.ipAddress }}</td>
          <td>{{ config.location }}</td>
          <td v-date>{{ config.lastSucceeded }}</td>
          <td
            v-date
            class="last-backup-date pointer"
            @click="onLastBackupDateClick(config)"
          >{{ config.lastUpdated }}</td>
          <td>{{ config.backupStatus }}</td>
          <td v-date>{{ config.scheduleDate }}</td>
          <td>{{ config.scheduleInterval }}</td>
        </tr>
      </tbody>
    </table>
  </div>
  <DCBModal @close="dcbModalVisible = false" :visible="dcbModalVisible">
    <template v-slot:content>
      <DCBModalLastBackupContent
        v-if="dcbModalContentComponentName === DCBModalContentComponentNames.DCBModalLastBackupContent"
      />
      <DCBModalViewHistoryContentVue v-else />
    </template>
  </DCBModal>
</template>

<script setup lang="ts">
import { reactive, computed, ref, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useScroll } from '@vueuse/core'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import History from '@featherds/icon/action/Restore'
import Download from '@featherds/icon/action/DownloadFile'
import Backup from '@featherds/icon/action/Cycle'
import DCBSearch from '@/components/Device/DCBSearch.vue'
import DCBModal from './DCBModal.vue'
import DCBModalLastBackupContent from './DCBModalLastBackupContent.vue'
import DCBModalViewHistoryContentVue from './DCBModalViewHistoryContent.vue'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

enum DCBModalContentComponentNames {
  DCBModalLastBackupContent = 'DCBModalLastBackupContent',
  DCBModalViewHistoryContent = 'DCBModalViewHistoryContent'
}

const store = useStore()
const dcbModalVisible = ref(false)
const dcbModalContentComponentName = ref('')
const all = ref(false)
const tableWrap = ref<HTMLElement | null>(null)
const defaultQuerySize = 20
const selectedDeviceConfigBackups = ref<Record<string, boolean>>({})
const sortStates: DeviceConfigQueryParams = reactive({
  name: SORT.ASCENDING,
  ipAddress: SORT.NONE,
  location: SORT.NONE,
  lastBackup: SORT.NONE,
  lastAttempted: SORT.NONE,
  backupStatus: SORT.NONE,
  scheduleDate: SORT.NONE,
  scheduleInterval: SORT.NONE
})
const { arrivedState } = useScroll(tableWrap, {
  offset: { bottom: 300 }
})

watch(arrivedState, () => {
  if (arrivedState.bottom) {
    getMoreDeviceConfigBackups()
  }
})

const deviceConfigBackups = computed<DeviceConfigBackup[]>(() => store.state.deviceModule.deviceConfigBackups)
const totalCountOfDeviceConfigBackups = computed(() => 2) // TODO: which endpoint prop will return this?
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
    store.dispatch('deviceModule/setSelectedIds', selectedDeviceConfigIds.value)
  }
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

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

#wrap {
  height: calc(100vh - 250px);
  overflow: auto;

  table {
    width: 100%;
    margin-top: 0px !important;
    @include table;
    @include table-condensed;

    .last-backup-date {
      color: var($primary);
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
    .layout-container {
      margin-bottom: 0px;
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
</style>
