<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherButton
        class="button"
        :class="deviceEntryOption === enterDevice ? 'feather-secondary-variant' : 'feather-shade3'"
        @click="selectEnterDeviceData"
      >I have formatted device data</FeatherButton>

      <FeatherButton
        class="button"
        :class="deviceEntryOption === findDevice ? 'feather-secondary-variant' : 'feather-shade3'"
        @click="selectFindDevice"
      >Let OpenNMS find devices for me</FeatherButton>
    </div>
  </div>

  <div v-if="deviceEntryOption === enterDevice">
    <div class="feather-row">
      <div class="feather-col-12 title">How would you like to import devices on your network?</div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <FeatherButton
          class="button"
          :class="deviceImportOption === byManual ? 'feather-secondary-variant' : 'feather-shade3'"
          @click="selectManually"
        >Manually</FeatherButton>

        <FeatherButton
          class="button"
          :class="deviceImportOption === byImport ? 'feather-secondary-variant' : 'feather-shade3'"
          @click="selectImport"
        >DNS Import</FeatherButton>

        <FeatherButton
          class="button"
          :class="deviceImportOption === byController ? 'feather-secondary-variant' : 'feather-shade3'"
          @click="selectController"
        >Controller API</FeatherButton>
      </div>
    </div>

    <div v-if="deviceImportOption === byManual">
      <StepAddContentManual />
    </div>

    <div v-else-if="deviceImportOption === byImport">
      <StepAddContentContainer contains="StepAddContentDNS" />
    </div>

    <div v-else-if="deviceImportOption === byController">
      <StepAddContentContainer contains="StepAddContentCtrl" />
    </div>
  </div>

  <div v-else-if="deviceEntryOption === findDevice">
    <div class="feather-row">
      <div class="feather-col-12 title">How would you like to discover devices on your network?</div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <FeatherButton
          class="button"
          :class="deviceFindOption === byIPRange ? 'feather-secondary-variant' : 'feather-shade3'"
          @click="selectIpRange"
        >By IP Range</FeatherButton>

        <FeatherButton
          class="button"
          :class="deviceFindOption === byPassiveDiscovery ? 'feather-secondary-variant' : 'feather-shade3'"
          @click="selectPassiveDiscovery"
        >Use Passive Discovery</FeatherButton>
      </div>
    </div>

    <div v-if="deviceFindOption === byIPRange">
      <StepAddContentContainer contains="StepAddContentIpRange" />
    </div>

    <div v-if="deviceFindOption === byPassiveDiscovery">
      <StepAddContentPassiveDiscovery />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { FeatherButton } from '@featherds/button'
import StepAddContentManual from './StepAddContentManual.vue'
import StepAddContentContainer from './StepAddContentContainer.vue'
import StepAddContentPassiveDiscovery from './StepAddContentPassiveDiscovery.vue'

const enterDevice = 'enter'
const findDevice = 'find'
const byManual = 'manual'
const byImport = 'import'
const byController = 'controller'
const byIPRange = 'ip'
const byPassiveDiscovery = 'discovery'

const deviceEntryOption = ref()
const deviceImportOption = ref()
const deviceFindOption = ref()

const selectEnterDeviceData = () => deviceEntryOption.value = enterDevice
const selectFindDevice = () => deviceEntryOption.value = findDevice
const selectManually = () => deviceImportOption.value = byManual
const selectImport = () => deviceImportOption.value = byImport
const selectController = () => deviceImportOption.value = byController
const selectIpRange = () => deviceFindOption.value = byIPRange
const selectPassiveDiscovery = () => deviceFindOption.value = byPassiveDiscovery
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.title {
  @include headline3();
}
.button {
  margin-right: 10px;
  height: 100px;
}
</style>

