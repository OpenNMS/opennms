<template>
  <Row first><h3>HTTP/S</h3></Row>
  <Row label="Port"><InputText v-model="port" class="input" @input="setValues"/></Row>
  <Row label="URL"><InputText v-model="url" class="input" @input="setValues" /></Row>
  <Row label="Use HTTPS"><Checkbox v-model="useHttps" :binary="true" class="input" @change="setValues" /></Row>
  <Row label="Timeout"><InputText v-model="timeout" class="input" @input="setValues" /></Row>
  <Row label="Retry"><InputText v-model="retry" class="input" @input="setValues" /></Row>

  <ShowHideBox label="Add return code">
    <Row label="Return code"><InputText v-model="returnCode" class="input" @input="setValues" /></Row>
  </ShowHideBox>

  <ServiceFilter @setValues="setValues" />
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import InputText from 'primevue/inputtext'
import Checkbox from 'primevue/checkbox'
import Row from '@/components/Common/Row.vue'
import ShowHideBox from '@/components/Common/ShowHideBox.vue'
import ServiceFilter from './ServiceFilter.vue'

export default defineComponent({
  components: {
    Row,
    Checkbox,
    InputText,
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

<style scoped lang="scss"></style>
