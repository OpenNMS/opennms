<template>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <h3>Name this batch/set</h3>
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <InputText id="batch-name" v-model="batchName" />
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <h3>When do you want to run this batch?</h3>
  </div>
  <div class="p-d-flex p-flex-column p-flex-md-row">
    <Button 
      label="Now"
      class="p-button-raised p-button-text now-btn"
      :class="selected === now ? 'bg-tertiaty-sky-blue' : ''"
      @click="showNowForm"
    />
    <Button 
      label="Schedule for later"
      class="p-button-raised p-button-text"
      :class="selected === later ? 'bg-tertiaty-sky-blue' : ''"
      @click="showLaterForm"
    />
  </div>
  <div v-if="selected === now">
    <StepScheduleContentNow />
  </div>
  <div v-if="selected === later">
    <StepScheduleContentLater :data="data" />
  </div>
</template>

<script lang="ts">
import dayjs from 'dayjs'
import { defineComponent, ref, computed } from 'vue'
import { useStore } from 'vuex'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import StepScheduleContentNow from './StepScheduleContentNow.vue'
import StepScheduleContentLater from './StepScheduleContentLater.vue'

export default defineComponent({
  components: {
    Button,
    InputText,
    StepScheduleContentNow,
    StepScheduleContentLater
  },
  setup() {
    const dayjsNow = dayjs()
    const store = useStore()
    const now = 'now'
    const later = 'later'
    const selected = ref()
    const batchName = ref('defaultName')

    const ipRanges = computed(() => store.state.inventoryModule.ipRanges)
    const snmpDetectRequest = computed(() => store.state.inventoryModule.snmpDetectRequest)

    const data = computed(() => ({
      batchName: batchName.value,
      discoverIPRanges: ipRanges.value,
      snmpConfigList: snmpDetectRequest.value,
    }))

    const showNowForm = async () => {
      selected.value = now
      const req = { scheduleTime: dayjsNow.unix(), ...data.value}
      const success = await store.dispatch('inventoryModule/provision', req)
      
      if (success) {
        store.dispatch('inventoryModule/setShowCompleteButton', 'now')
      }
    }

    const showLaterForm = () => {
      selected.value = later
    }

    return {
      now,
      data,
      later,
      selected,
      batchName,
      showNowForm,
      showLaterForm
    }
  }
})

</script>

<style scoped lang="scss">
  .now-btn {
    margin-right: 10px;
  }
</style>
