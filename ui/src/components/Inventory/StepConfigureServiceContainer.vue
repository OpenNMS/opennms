<template>
  <template v-for="service in services">
    <component 
      v-if="service === services[currentServiceIndex]" 
      is="ServiceContainer" 
      :service="service"
      :lastService="services[currentServiceIndex] === services[services.length - 1]"
      @completeService="completeService"
    />
  </template>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import ServiceContainer from './ConfigureServices/ServiceContainer.vue'

export default defineComponent({
  components: {
    ServiceContainer
  },
  props: {
    services: {
      type: Array,
      required: true
    }
  },
  setup(props) {
    const currentServiceIndex = ref(0)

    const completeService = () => {
      const nextServiceIndex = currentServiceIndex.value + 1
      if (props.services[nextServiceIndex]) {
        currentServiceIndex.value = nextServiceIndex
      }
    }

    return {
      completeService,
      currentServiceIndex
    }
  }
})
</script>

<style scoped lang="scss"></style>
