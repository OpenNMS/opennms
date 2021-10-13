<template>
  <Row col="3"><LocationsDropdown @setLocation="setLocation" /></Row>
  <Row col="3"><TypesDropdown @setType="setType" /></Row>
  <Row col="3"><FeatherInput label="Endpoint" v-model="endpoint" @update:modelValue="setValues" /></Row>
  <Row col="3"><FeatherInput label="Key" v-model="key" @update:modelValue="setValues" /></Row>
  <Row col="3"><FeatherInput label="Secret" v-model="secret" @update:modelValue="setValues" /></Row>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import { FeatherInput } from '@featherds/input'
import TypesDropdown from './TypeDropdown.vue'
import LocationsDropdown from './LocationsDropdown.vue'
import { MonitoringLocation } from '@/types'
import Row from '@/components/Common/Row.vue'

export default defineComponent({
  components: {
    Row,
    FeatherInput,
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
