<template>
  <Card>
    <template v-slot:title>
      What services do you want to find/monitor?
    </template>
    <template v-slot:content>
      <StepConfigureServiceBtnContainer @configureServices="configureServices"/>
      <StepConfigureServiceContainer :services="selectedServices" />
    </template>
    <template v-slot:footer>
      <div class="p-grid p-nogutter p-justify-between">
        <Button 
          class="p-button-primary" 
          label="Back" 
          @click="$emit('prev-page', { pageIndex: 1 })" 
          icon="pi pi-angle-left" 
        />
        <Button 
          v-if="showNextBtn" 
          class="p-button-primary" 
          label="This looks good" 
          @click="$emit('next-page', { pageIndex: 1 })" 
          icon="pi pi-angle-right" 
          iconPos="right" 
        />
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import Card from 'primevue/card'
import Button from 'primevue/button'
import StepConfigureServiceContainer from './StepConfigureServiceContainer.vue'
import StepConfigureServiceBtnContainer from './StepConfigureServiceBtnContainer.vue'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    Card,
    Button,
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

<style scoped lang="scss"></style>
