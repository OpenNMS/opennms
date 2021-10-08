<template>
  <Button 
    :label="serviceName"
    class="p-button-raised p-button-text service"
    :class="[
      //@ts-ignore
      {'bg-tertiaty-sky-blue' : (selectedServices.includes(serviceName) && !completedServices.includes(serviceName))}, 
      //@ts-ignore
      {'bg-primary-green' : completedServices.includes(serviceName)}
    ]"
    :disabled="disableService"
    @click="$emit('select-service', serviceName)"
  />
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import Button from 'primevue/button'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    Button
  },
  emits:['select-service'],
  props: {
    serviceName: {
      type: String,
      required: true
    },
    selectedServices: {
      type: Array,
      required: true,
      default: []
    },
    disableService: {
      type: Boolean,
      required: true
    }
  },
  setup() {
    const store = useStore()
    const completedServices = computed(() => store.state.inventoryModule.completedServices)

    return {
      completedServices
    }
  }
})
</script>

<style scoped lang="scss">
  .service {
    margin-right: 10px;
  }
</style>
