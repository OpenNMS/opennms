<template>
  <Calendar :id="id" :showTime="showTime" :timeOnly="timeOnly" :hourFormat="hourFormat" />
</template>

<script setup lang="ts">

import Calendar from 'primevue/calendar'

interface TimePickerProps {
  id?: string
  showTime?: boolean
  timeOnly?: boolean
  hourFormat?: string
}

const props = withDefaults(defineProps<TimePickerProps>(), {
  showTime: true,
  timeOnly: true,
  hourFormat: "24", // value - 12 or 24 supported 
})

</script>
