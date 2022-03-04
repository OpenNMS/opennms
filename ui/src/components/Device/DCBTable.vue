<template>
  <div class="select-search">
    <div class="checkbox-with-caption">
      <FeatherCheckbox v-model="all">{{ `Select All Devices (${totalCountOfDeviceConfigBackups})` }}</FeatherCheckbox>
      <span class="caption">{{ `Devices selected: ${numberOfSelectedDevices}` }}</span>
    </div>
    <DCBSearch class="dcb-search" />
  </div>

  <div ref="tableWrap" id="wrap">
    <table class="tl1 tl2 tl3 tl4 tl5 tl6 tl7 tc8" summary="Device Config Backup">
      <thead>
        <tr>
          <FeatherSortHeader
            scope="col"
            property="name"
            :sort="sortStates.name"
            v-on:sort-changed="sortByColumnHandler"
          >Device Name</FeatherSortHeader>

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
            <router-link :to="`/device-config-backup/${config.id}`">{{ config.name }}</router-link>

            <FeatherDropdown>
              <template v-slot:trigger>
                <FeatherButton link href="#" menu-trigger icon="Actions">
                  <FeatherIcon :icon="MoreVert" />
                </FeatherButton>
              </template>

              <FeatherDropdownItem :disabled="numberOfSelectedDevices > 1">View Last Backup</FeatherDropdownItem>

              <FeatherDropdownItem
                @click="downloadConfigurationHandler(config.id)"
              >Download Configuration</FeatherDropdownItem>

              <FeatherDropdownItem :disabled="numberOfSelectedDevices === 0" @click="backupNowHandler">Backup Now</FeatherDropdownItem>
            </FeatherDropdown>
          </td>
          <td>{{ config.ipAddress }}</td>
          <td>{{ config.location }}</td>
          <td v-date>{{ config.lastBackup }}</td>
          <td v-date>{{ config.lastAttempted }}</td>
          <td>{{ config.backupStatus }}</td>
          <td v-date>{{ config.scheduleDate }}</td>
          <td>{{ config.scheduleInterval }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, ref, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useScroll } from '@vueuse/core'
import { DeviceConfigBackup, QueryParameters } from '@/types'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import DCBSearch from '@/components/Device/DCBSearch.vue'

const store = useStore()
const all = ref(false)
const tableWrap = ref<HTMLElement | null>(null)
const defaultQuerySize = 20
const selectedDeviceConfigBackups = ref<Record<string, boolean>>({})
const sortStates: any = reactive({
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

const deviceConfigBackups = computed(() => store.state.deviceModule.deviceConfigBackups)

const totalCountOfDeviceConfigBackups = computed(() => 2) // TODO: which endpoint prop will return this?

const deviceConfigBackupQueryParams = computed<QueryParameters>(() => store.state.deviceModule.deviceConfigBackupQueryParams)

const numberOfSelectedDevices = computed(() => {
  if (all.value) {
    return totalCountOfDeviceConfigBackups.value
  }

  return Object.values(selectedDeviceConfigBackups.value)
    .filter((isSelected) => isSelected)
    .length
})

const sortByColumnHandler = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[`${sortObj.property}`] = sortObj.value

  const newQueryParams: QueryParameters = {
    limit: defaultQuerySize,
    offset: 0,
    order: sortObj.value,
    orderBy: sortObj.property
  }

  store.dispatch('deviceModule/updateDeviceConfigBackupQueryParams', newQueryParams)
  store.dispatch('deviceModule/getDeviceConfigBackups')
}

const backupNowHandler = () => {
  const ids = Object.keys(selectedDeviceConfigBackups.value)
    .filter((id) => selectedDeviceConfigBackups.value[id])

  store.dispatch('deviceModule/backupDeviceConfigByIds', ids)
}

const getMoreDeviceConfigBackups = () => {
  const newQueryParams: QueryParameters = {
    limit: (deviceConfigBackupQueryParams.value.limit || 0) + defaultQuerySize,
    offset: (deviceConfigBackupQueryParams.value.offset || 0) + defaultQuerySize
  }

  store.dispatch('deviceModule/updateDeviceConfigBackupQueryParams', newQueryParams)
  store.dispatch('deviceModule/getAndMergeDeviceConfigBackups')
}

const downloadConfigurationHandler = async (id: string) => store.dispatch('deviceModule/downloadDeviceConfigById', id)
const selectCheckbox = (config: DeviceConfigBackup) => selectedDeviceConfigBackups.value[config.id] = !selectedDeviceConfigBackups.value[config.id]

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
  }

  thead {
    z-index: 2;
    position: relative;
    background: var(--feather-surface);
  }
}

.select-search {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;

  .checkbox-with-caption {
    .layout-container {
      margin-bottom: 0px;
    }
    .caption {
      font-family: var($font-family);
      @include caption;
      display: block;
      margin-left: 32px;
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
