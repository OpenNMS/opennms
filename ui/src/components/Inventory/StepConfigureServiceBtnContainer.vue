<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <template v-for="service of services">
        <component
          is="StepConfigureServiceBtn"
          :serviceName="service"
          :selectedServices="selectedServices"
          :disableService="disableServiceSelection"
          @selectService="selectService(service)"
        />
      </template>
      <!-- <i class="pointer" v-if="showReset" @click="resetServiceSelection" /> -->
    </div>
  </div>
  <div class="feather-row first" v-if="showConfigureServicesBtn">
    <FeatherButton secondary @click="configureServices">Configure</FeatherButton>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import StepConfigureServiceBtn from './StepConfigureServiceBtn.vue'
import { FeatherButton } from '@featherds/button'

export default defineComponent({
  components: {
    StepConfigureServiceBtn,
    FeatherButton
  },
  emits: ['configure-services'],
  setup(_, context) {
    const showReset = ref(false)
    const showConfigureServicesBtn = ref(false)
    const disableServiceSelection = ref(false)
    const selectedServices = ref([] as string[])
    const services = [
      'SNMP',
      'HTTPS',
      'SSH',
      'ICMP',
      'WinRM'
    ]

    const selectService = (service: string) => {
      const idx = selectedServices.value.indexOf(service)

      if (idx !== -1) {
        selectedServices.value.splice(idx, 1)
      } else {
        selectedServices.value.push(service)
      }

      showConfigureServicesBtn.value = Boolean(selectedServices.value.length)
    }

    const configureServices = () => {
      context.emit('configure-services', selectedServices.value)
      showConfigureServicesBtn.value = false
      disableServiceSelection.value = true
      showReset.value = true
    }

    const resetServiceSelection = () => {
      // reset services
      selectedServices.value = []
      context.emit('configure-services', selectedServices.value)

      // hide btns
      showReset.value = false
      showConfigureServicesBtn.value = false
      disableServiceSelection.value = false
    }

    return {
      services,
      showReset,
      selectedServices,
      showConfigureServicesBtn,
      disableServiceSelection,
      resetServiceSelection,
      configureServices,
      selectService
    }
  }
})
</script>
