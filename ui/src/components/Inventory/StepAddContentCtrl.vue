<template>
  <Row label="Location" first><LocationsDropdown @setLocation="setLocation" /></Row>
  <Row label="Type"><TypesDropdown @setType="setType" /></Row>
  <Row label="Endpoint"><InputText v-model="endpoint" class="input" @input="setValues" /></Row>
  <Row label="Key"><InputText v-model="key" class="input" @input="setValues" /></Row>
  <Row label="Secret"><InputText v-model="secret" class="input" @input="setValues" /></Row>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import InputText from 'primevue/inputtext'
import TypesDropdown from './TypeDropdown.vue'
import LocationsDropdown from './LocationsDropdown.vue'
import { MonitoringLocation } from '@/types'
import Row from '@/components/Common/Row.vue'

export default defineComponent({
  components: {
    Row,
    InputText,
    TypesDropdown,
    LocationsDropdown
  },
  emits: ['set-values'],
  setup(_, context) {
    const endpoint = ref()
    const secret = ref()
    const key = ref()
    const type = ref()
    const location = ref()

    const setLocation = (selectedLocation: MonitoringLocation) => {
      location.value = selectedLocation
      setValues()
    }

    const setType = (selectedType: { id: string, name: string }) => type.value = selectedType.name
    const setValues = () => context.emit('set-values', { endpoint, secret, key, type })

    return {
      setLocation,
      setValues,
      setType,
      endpoint,
      secret,
      key
    }
  }
})

</script>

<style scoped lang="scss">
</style>
