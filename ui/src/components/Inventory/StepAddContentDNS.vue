<template>
  <Row label="Location" first><LocationsDropdown @setLocation="setLocation"/></Row>
  <Row label="Host"><InputText v-model="host" class="input" @input="setValues"/></Row>
  <Row label="Zone"><InputText v-model="zone" class="input" @input="setValues" /></Row>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import InputText from 'primevue/inputtext'
import LocationsDropdown from './LocationsDropdown.vue'
import { MonitoringLocation } from '@/types'
import Row from '@/components/Common/Row.vue'

export default defineComponent({
  components: {
    Row,
    InputText,
    LocationsDropdown
  },
  emits: ['set-values'],
  setup(_, context) {
    const host = ref()
    const zone = ref()
    const location = ref()

    const setLocation = (selectedLocation: MonitoringLocation) => {
      location.value = selectedLocation
      setValues()
    }

    const setValues = () => context.emit('set-values', { host, zone, location })

    return {
      host,
      zone,
      setValues,
      setLocation
    }
  }
})

</script>

<style scoped lang="scss">
</style>
