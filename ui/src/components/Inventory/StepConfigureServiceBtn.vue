<template>
  <FeatherButton 
    class="service"
    :class="[
      //@ts-ignore
      {'feather-secondary-variant' : (selectedServices.includes(serviceName) && !completedServices.includes(serviceName))}, 
      //@ts-ignore
      {'feather-secondary' : completedServices.includes(serviceName)}
    ]"
    :disabled="disableService"
    @click="$emit('select-service', serviceName)"
  >{{ serviceName }}</FeatherButton>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import { FeatherButton } from '@featherds/button'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    FeatherButton
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
