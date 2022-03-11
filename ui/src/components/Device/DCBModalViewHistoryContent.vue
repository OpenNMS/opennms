<template>
  <FeatherButton class="dwnld-btn" icon="Download config" @click="onDownload">
    <FeatherIcon :icon="Download" />
  </FeatherButton>

  <div class="flex-container">
    <div class="row-of-dates">

    </div>

    <div v-if="modalDeviceConfigBackup.configType === 'default'">{{ modalDeviceConfigBackup.config }}</div>

    <FeatherTabContainer class="tabs" v-else>
      <template v-slot:tabs>
        <FeatherTab @click="getListOfDates">Startup Configuration</FeatherTab>
        <FeatherTab @click="getListOfDates">Running Configuration</FeatherTab>
      </template>
    </FeatherTabContainer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherTab, FeatherTabContainer } from '@featherds/tabs'
import Download from '@featherds/icon/action/DownloadFile'
import { useStore } from 'vuex'
import { DeviceConfigBackup } from '@/types/deviceConfig'

const store = useStore()

const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)

const onDownload = () => store.dispatch('deviceModule/downloadSelectedDevices')
const getListOfDates = () => { console.log('call api to get dates') }
</script>

<style scoped lang="scss">
.flex-container {
  display: flex;

  .row-of-dates {
    display: flex;
    flex-direction: column;
    width: 150px;
  }
}
.dwnld-btn {
  position: absolute;
  right: 36px;
  top: -77px;
}
</style>
