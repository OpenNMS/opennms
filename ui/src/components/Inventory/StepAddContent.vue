<template>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <Button 
      class="p-button-raised p-button-text button"
      :class="deviceEntryOption === enterDevice ? 'bg-tertiaty-sky-blue' : ''"
      label="I have formatted device data" 
      @click="selectEnterDeviceData"/>
        
    <Button 
      class="p-button-raised p-button-text button"
      :class="deviceEntryOption === findDevice ? 'bg-tertiaty-sky-blue' : ''"
      label="Let OpenNMS find devices for me"
      @click="selectFindDevice"/>
  </div>

  <div v-if="deviceEntryOption === enterDevice">
    <div class="p-d-flex p-flex-column p-flex-md-row">
      <h3>How would you like to import devices on your network?</h3>
    </div>
    <div class="p-d-flex p-flex-column p-flex-md-row">
      <Button
        class="p-button-raised p-button-text button"
        :class="deviceImportOption === byManual ? 'bg-tertiaty-sky-blue' : ''"
        label="Manually" 
        @click="selectManually"/>
          
      <Button
        class="p-button-raised p-button-text button"
        :class="deviceImportOption === byImport ? 'bg-tertiaty-sky-blue' : ''"
        label="DNS Import"
        @click="selectImport"/>

      <Button
        class="p-button-raised p-button-text button"
        :class="deviceImportOption === byController ? 'bg-tertiaty-sky-blue' : ''"
        label="Controller API"
        @click="selectController"/>
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
    <div class="p-d-flex p-flex-column p-flex-md-row">
      <h3>How would you like to discover devices on your network?</h3>
    </div>
    <div class="p-d-flex p-flex-column p-flex-md-row">
      <Button
        class="p-button-raised p-button-text button"
        :class="deviceFindOption === byIPRange ? 'bg-tertiaty-sky-blue' : ''"
        label="By IP Range"
        @click="selectIpRange"/>

      <Button
        class="p-button-raised p-button-text button"
        :class="deviceFindOption === byPassiveDiscovery ? 'bg-tertiaty-sky-blue' : ''"
        label="Use Passive Discovery"
        @click="selectPassiveDiscovery"/>
    </div>

    <div v-if="deviceFindOption === byIPRange">
      <StepAddContentContainer contains="StepAddContentIpRange" />
    </div>

    <div v-if="deviceFindOption === byPassiveDiscovery">
      <StepAddContentPassiveDiscovery />
    </div>
  </div>

</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import Button from 'primevue/button'
import StepAddContentManual from './StepAddContentManual.vue'
import StepAddContentContainer from './StepAddContentContainer.vue'
import StepAddContentPassiveDiscovery from './StepAddContentPassiveDiscovery.vue'

export default defineComponent({
  components: {
    Button,
    StepAddContentManual,
    StepAddContentContainer,
    StepAddContentPassiveDiscovery
  },
  setup() {
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

    return {
      selectManually,
      selectImport,
      selectController,
      selectIpRange,
      selectPassiveDiscovery,
      selectFindDevice,
      selectEnterDeviceData,
      findDevice,
      enterDevice,
      byManual,
      byImport,
      byController,
      byIPRange,
      byPassiveDiscovery,
      deviceEntryOption,
      deviceImportOption,
      deviceFindOption
    }
  }
})

</script>

<style scoped lang="scss">
  .button {
    margin-right: 10px;
    height: 100px;
  }
</style>

