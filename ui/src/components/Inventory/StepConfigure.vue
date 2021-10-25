<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12 title">What services do you want to find/monitor?</div>
    </div>

    <div class="feather-row">
      <div class="feather-col-12">
        <StepConfigureServiceBtnContainer @configureServices="configureServices" />
        <StepConfigureServiceContainer :services="selectedServices" />
      </div>
    </div>

    <div class="feather-row">
      <div class="feather-col-12 space-between">
        <FeatherButton primary @click="$emit('prev-page', { pageIndex: 1 })">Back</FeatherButton>
        <FeatherButton
          v-if="showNextBtn"
          primary
          @click="$emit('next-page', { pageIndex: 1 })"
        >This looks good</FeatherButton>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { FeatherButton } from '@featherds/button'
import StepConfigureServiceContainer from './StepConfigureServiceContainer.vue'
import StepConfigureServiceBtnContainer from './StepConfigureServiceBtnContainer.vue'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    FeatherButton,
    StepConfigureServiceContainer,
    StepConfigureServiceBtnContainer
  },
  emits: ['next-page', 'prev-page'],
  setup() {
    const store = useStore()
    const selectedServices = ref([] as string[])
    const configureServices = (services: string[]) => selectedServices.value = services
    const showNextBtn = computed(() => store.state.inventoryModule.showConfigureServiceStepNextButton)

    return {
      configureServices,
      selectedServices,
      showNextBtn
    }
  }
})

</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  .title {
    @include headline3();
  }
}
</style>
