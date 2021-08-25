<template>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <h3>When do you want to schedule this scan?</h3>
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <h4 for="calendar">Date/Time</h4>
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <Calendar id="calendar" v-model="calendarDate" :showTime="true" hourFormat="12" />
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <h4 for="calendar">Re-run this scan?</h4>
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <Dropdown :options="reRunOptions" v-model="selectedReRunOption" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue'
import { useStore } from 'vuex'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
  components: {
    InputText,
    Calendar,
    Dropdown,
    Button,
  },
  props: {
    data: {
      required: true,
      type: Object
    }
  },
  setup(props) {
    const store = useStore()
    const reRunOptions = ['Never', 'Weekly', 'Daily']
    const selectedReRunOption = ref('Never')
    const calendarDate = ref()

    watch(calendarDate, (calendarDate) => {
      if (calendarDate) {
        
        const date = new Date(calendarDate)
        const unix = date.valueOf()
        const req = { scheduleTime: unix, ...props.data }
        
        store.dispatch('inventoryModule/saveProvisionRequest', req)
        store.dispatch('inventoryModule/setShowCompleteButton', 'later')
      } else {
        store.dispatch('inventoryModule/setShowCompleteButton', false)
      }
    })

    return {
      reRunOptions,
      calendarDate,
      selectedReRunOption
    }
  }
})

</script>

<style scoped lang="scss"></style>
