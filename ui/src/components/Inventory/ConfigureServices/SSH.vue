<template>
  <Row first><h3>SSH</h3></Row>
  <Row label="Timeout"><FeatherInput v-model="timeout" class="input" @update:modelValue="setValues" /></Row>
  <Row label="Retry"><FeatherInput v-model="retry" class="input" @update:modelValue="setValues" /></Row>

  <!-- advanced options -->
  <ShowHideBox label="Advanced options">
    <Row first label="Banner"><FeatherInput v-model="banner" @update:modelValue="setValues" class="input"/></Row>
    <Row label="Port"><FeatherInput v-model="port" @update:modelValue="setValues" class="input"/></Row>

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
    const banner = ref()
    const port = ref('22')

    // form
    const timeout = ref()
    const retry = ref()

    const data = computed(() => ({ 
      timeout: timeout.value, 
      retry: retry.value,
      banner: banner.value,
      port: Number(port.value)
    }))

    const setValues = (filterValues: any) => context.emit('set-values', { index: props.index, data: {...data.value, ...filterValues} })

    return {
      port,
      retry,
      banner,
      timeout,
      setValues
    }
  }
})
</script>
