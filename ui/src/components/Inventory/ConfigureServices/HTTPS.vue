<template>
  <Row><span class="headline3">HTTP/S</span></Row>
  <Row col="3"><FeatherInput label="Port" v-model="port"   @update:modelValue="setValues"/></Row>
  <Row col="3"><FeatherInput label="URL" v-model="url"   @update:modelValue="setValues" /></Row>
  <Row col="3"><FeatherCheckbox v-model="useHttps"  @update:modelValue="setValues">Use HTTPS</FeatherCheckbox></Row>
  <Row col="3"><FeatherInput label="Timeout" v-model="timeout"   @update:modelValue="setValues" /></Row>
  <Row col="3"><FeatherInput label="Retry" v-model="retry"   @update:modelValue="setValues" /></Row>

  <ShowHideBox label="Add return code">
    <Row><FeatherInput label="Return code" v-model="returnCode"   @update:modelValue="setValues" /></Row>
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
