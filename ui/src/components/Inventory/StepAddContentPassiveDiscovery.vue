<template>
  <Row label="Location" first><LocationsDropdown @setLocation="setLocation" /></Row>
  <Row label="Send device traps and syslogs to 192.168.1.1 in order for OpenNMS to discover." />
  
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import { MonitoringLocation } from '@/types'
import LocationsDropdown from './LocationsDropdown.vue'
import { useStore } from 'vuex'
import Row from '@/components/Common/Row.vue'

export default defineComponent({
  components: {
    Row,
    LocationsDropdown
  },
  setup() {
    const store = useStore()
    const location = ref()

    const setLocation = (selectedLocation: MonitoringLocation) => {
      location.value = selectedLocation
      // display next btn if testing successful
      store.dispatch('inventoryModule/showAddStepNextButton', true)
    }

    return {
      setLocation
    }
  }
})
</script>
