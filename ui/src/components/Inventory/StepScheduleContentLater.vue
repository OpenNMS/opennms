<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title">When do you want to schedule this scan?</div>
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title" for="calendar">Date/Time</div>
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherDateInput class="headline4" disabled id="calendar" v-model="calendarDate" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title" for="calendar">Re-run this scan?</div>
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherSelect text-prop="value" :options="reRunOptions" v-model="selectedReRunOption" />
    </div>
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
    const reRunOptions = [{ id: 1, value: 'Never' }, { id: 2, value: 'Weekly' }, { id: 3, value: 'Daily' }]
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
