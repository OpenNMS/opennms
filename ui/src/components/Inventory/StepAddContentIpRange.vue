<template>
<div class="feather-row">
	<div class="feather-col-6">
    <Row label="Location" first><LocationsDropdown @setLocation="setLocation" /></Row>
    <Row label="Start"><FeatherInput v-model="start" class="input" @update:modelValue="setValues" /></Row>
    <Row label="End"><FeatherInput v-model="end" class="input" @update:modelValue="setValues" /></Row>
  </div>
	<div class="feather-col-5">
    <StepAddResponseTables />
  </div>
</div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { FeatherInput } from '@featherds/input'
import LocationsDropdown from './LocationsDropdown.vue'
import { MonitoringLocation } from '@/types'
import Row from '@/components/Common/Row.vue'
import StepAddResponseTables from './StepAddResponseTables.vue'

export default defineComponent({
  components: {
    Row,
    FeatherInput,
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

