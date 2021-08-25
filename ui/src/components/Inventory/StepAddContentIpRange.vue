<template>
<div class="p-grid">
	<div class="p-col">
    <Row label="Location" first><LocationsDropdown @setLocation="setLocation" /></Row>
    <Row label="Start"><InputText v-model="start" class="input" @input="setValues" /></Row>
    <Row label="End"><InputText v-model="end" class="input" @input="setValues" /></Row>
  </div>
	<div class="p-col">
    <StepAddResponseTables />
  </div>
</div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import InputText from 'primevue/inputtext'
import LocationsDropdown from './LocationsDropdown.vue'
import { IPRange, MonitoringLocation } from '@/types'
import Row from '@/components/Common/Row.vue'
import StepAddResponseTables from './StepAddResponseTables.vue'

export default defineComponent({
  components: {
    Row,
    InputText,
    LocationsDropdown,
    StepAddResponseTables
  },
  emits: ['set-values'],
  props: {
    index: {
      type: Number,
      required: true
    }
  },
  setup(props, context) {
    const start = ref()
    const end = ref()
    const location = ref()

    const setLocation = (selectedLocation: MonitoringLocation) => {
      location.value = selectedLocation['location-name']
      setValues()
    }

    const data = computed(() => ({ 
      location: location.value,
      startIP: start.value, 
      endIP: end.value 
    }))

    const setValues = () => context.emit('set-values', { 
      index: props.index, 
      data: {...data.value },
      requiredFields: ['location', 'startIP', 'endIP']
    })

    return {
      setLocation,
      setValues,
      start,
      end
    }
  }
})

</script>

<style scoped lang="scss">
</style>
