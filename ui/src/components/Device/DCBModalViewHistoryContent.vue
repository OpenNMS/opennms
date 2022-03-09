<template>
  <FeatherButton class="dwnld-btn" icon="Download config" @click="onDownload">
    <FeatherIcon :icon="Download" />
  </FeatherButton>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
import { useStore } from 'vuex'

const store = useStore()

const onDownload = () => store.dispatch('deviceModule/downloadSelectedDevices')
</script>

<style scoped lang="scss">
.dwnld-btn {
  position: absolute;
  right: 36px;
  top: -77px;
}
</style>
