<template>
  <Card>
    <template v-slot:content>
      <StepScheduleContent />
    </template>
    <template v-slot:footer>
      <div class="p-grid p-nogutter p-justify-between">
        <Button 
          class="p-button-primary" 
          label="Back" 
          @click="$emit('prev-page', { pageIndex: 2 })" 
          icon="pi pi-angle-left" 
        />
        <Button
          v-if="showCompleteButton"
          label="Complete" 
          @click="$emit('complete')" 
          icon="pi pi-check" 
          iconPos="right" 
          class="p-button-success"
        />
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import { useStore } from 'vuex'
import Card from 'primevue/card'
import Button from 'primevue/button'
import StepScheduleContent from './StepScheduleContent.vue'

export default defineComponent({
  components: {
    Card,
    Button,
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

<style scoped lang="scss"></style>
