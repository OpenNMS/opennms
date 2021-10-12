<template>
  <div class="feather-row">
    <div class="title">When do you want to schedule this scan?</div>
  </div>
  <div class="feather-row">
    <div class="title" for="calendar">Date/Time</div>
  </div>
  <div class="feather-row">
    <FeatherDateInput id="calendar" v-model="calendarDate" />
  </div>
  <div class="feather-row">
    <div class="title" for="calendar">Re-run this scan?</div>
  </div>
  <div class="feather-row">
    <FeatherSelect :options="reRunOptions" v-model="selectedReRunOption" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { FeatherDateInput } from '@featherds/date-input'
import { FeatherSelect } from '@featherds/select'

export default defineComponent({
  components: {
    FeatherDateInput,
    FeatherSelect
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

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.title {
  @include headline3();
}
</style>
