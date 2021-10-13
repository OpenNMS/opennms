<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title">Name this batch/set</div>
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherInput id="batch-name" v-model="batchName" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title">When do you want to run this batch?</div>
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherButton
        class="now-btn"
        :class="selected === now ? 'feather-secondary-variant' : 'feather-shade3'"
        @click="showNowForm"
      >Now</FeatherButton>
      <FeatherButton
        :class="selected === later ? 'feather-secondary-variant' : 'feather-shade3'"
        @click="showLaterForm"
      >Schedule for later</FeatherButton>
    </div>
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
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import StepScheduleContentNow from './StepScheduleContentNow.vue'
import StepScheduleContentLater from './StepScheduleContentLater.vue'

export default defineComponent({
  components: {
    FeatherButton,
    FeatherInput,
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
      const req = { scheduleTime: dayjsNow.unix(), ...data.value }
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
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.now-btn {
  margin-right: 10px;
}
.title {
  @include headline3();
}
</style>
