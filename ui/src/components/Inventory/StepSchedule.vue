<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <StepScheduleContent />
      </div>
    </div>

    <div class="feather-row">
      <div class="feather-col-12 space-between">
        <FeatherButton primary @click="$emit('prev-page', { pageIndex: 2 })">Back</FeatherButton>
        <FeatherButton v-if="showCompleteButton" @click="$emit('complete')" primary>Complete</FeatherButton>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherButton } from '@featherds/button'
import StepScheduleContent from './StepScheduleContent.vue'

export default defineComponent({
  components: {
    FeatherButton,
    StepScheduleContent
  },
  emits: ['complete', 'prev-page'],
  setup() {
    const store = useStore()
    const showCompleteButton = computed(() => store.state.inventoryModule.showCompleteButton)

    return {
      showCompleteButton
    }
  }
})

</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
}
</style>
