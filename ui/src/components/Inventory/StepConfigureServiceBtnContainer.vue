<template>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <template v-for="service of services">
      <component 
        is="StepConfigureServiceBtn" 
        :serviceName="service"
        :selectedServices="selectedServices"
        :disableService="disableServiceSelection"
        @selectService="selectService(service)"
      />
    </template>
    <!-- <i class="pi pi-replay pointer" v-if="showReset" @click="resetServiceSelection" /> -->
  </div>
  <div class="p-flex-row first" v-if="showConfigureServicesBtn">
    <Button
      class="p-button-secondary" 
      label="Configure" 
      @click="configureServices"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import StepConfigureServiceBtn from './StepConfigureServiceBtn.vue';
import Button from 'primevue/button'

export default defineComponent({
  components: {
    StepConfigureServiceBtn,
    Button
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

<style lang="scss">
 .pi-replay {
   margin-top: 10px;
   font-weight: bold;
 }
</style>
