<template>
  <Row first><h3>HTTP/S</h3></Row>
  <Row label="Port"><FeatherInput v-model="port" class="input"  @update:modelValue="setValues"/></Row>
  <Row label="URL"><FeatherInput v-model="url" class="input"  @update:modelValue="setValues" /></Row>
  <Row label="Use HTTPS"><FeatherCheckbox v-model="useHttps" class="input" @update:modelValue="setValues" /></Row>
  <Row label="Timeout"><FeatherInput v-model="timeout" class="input"  @update:modelValue="setValues" /></Row>
  <Row label="Retry"><FeatherInput v-model="retry" class="input"  @update:modelValue="setValues" /></Row>

  <ShowHideBox label="Add return code">
    <Row label="Return code"><FeatherInput v-model="returnCode" class="input"  @update:modelValue="setValues" /></Row>
  </ShowHideBox>

  <ServiceFilter @setValues="setValues" />
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { FeatherInput } from '@featherds/input'
import { FeatherCheckbox } from "@featherds/checkbox"
import Row from '@/components/Common/Row.vue'
import ShowHideBox from '@/components/Common/ShowHideBox.vue'
import ServiceFilter from './ServiceFilter.vue'

export default defineComponent({
  components: {
    Row,
    FeatherCheckbox,
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
    // advaced options
    const returnCode = ref()

    // form
    const useHttps = ref()
    const port = ref()
    const url = ref()
    const timeout = ref()
    const retry = ref()

    const data = computed(() => ({ 
      port: port.value, 
      url: url.value, 
      timeout: timeout.value, 
      retry: retry.value,
      useHttps: useHttps.value,
      returnCode: returnCode.value
    }))

    const setValues = (filterValues: any) => context.emit('set-values', { index: props.index, data: {...data.value, ...filterValues} })

    return {
      port,
      url,
      retry,
      timeout,
      useHttps,
      returnCode,
      setValues
    }
  }
})
</script>
