<template>
  <Row first ><h3>ICMP</h3></Row>
  <Row label="Timeout"><FeatherInput type="text" v-model="timeout" class="input" @update:modelValue="setValues" /></Row>
  <Row label="Retry"><FeatherInput type="text" v-model="retry" class="input" @update:modelValue="setValues" /></Row>

  <ShowHideBox label="Advanced options">
    <!-- Advanced options -->
    <Row first label="DSCP"><FeatherInput type="text" v-model="dscp" class="input" @update:modelValue="setValues" /></Row>

    <!-- add filter -->
    <ServiceFilter @setValues="setValues" />
  </ShowHideBox>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { FeatherInput } from '@featherds/input'
import Row from '@/components/Common/Row.vue'
import ShowHideBox from '@/components/Common/ShowHideBox.vue'
import ServiceFilter from './ServiceFilter.vue'

export default defineComponent({
  components: {
    Row,
    FeatherInput,
    ShowHideBox,
    ServiceFilter
  },
  emits: ['set-values'],
  props: {
    index: {
      type: Number,
      required: true
    }
  },
  setup(props, context) {
    // advanced options
    const dscp = ref()

    // form
    const timeout = ref()
    const retry = ref()

    const data = computed(() => ({
      timeout: timeout.value, 
      retry: retry.value, 
      dscp: dscp.value
    }))

    const setValues = (filterValues: any) => context.emit('set-values', { index: props.index, data: {...data.value, ...filterValues} })

    return {
      dscp,
      retry,
      timeout,
      setValues
    }
  }
})
</script>
