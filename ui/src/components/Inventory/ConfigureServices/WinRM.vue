<template>
  <Row><span class="headline3">WinRM / WSMan</span></Row>
  <Row><FeatherInput label="Username" v-model="username"  @update:modelValue="setValues" /></Row>
  <Row><FeatherInput label="Password" v-model="password"  @update:modelValue="setValues" /></Row>
  <Row><FeatherInput label="Timeout" v-model="timeout"  @update:modelValue="setValues" /></Row>
  <Row><FeatherInput label="Retry" v-model="retry"  @update:modelValue="setValues" /></Row>

  <!-- advanced options -->
  <ShowHideBox label="Advanced options">
    <Row><FeatherInput label="Port" v-model="port"  @update:modelValue="setValues"/></Row>
    <Row><FeatherCheckbox label="Use TLS" v-model="useTLS" @update:modelValue="setValues" /></Row>
    <Row><FeatherInput label="Object" v-model="object"  @update:modelValue="setValues"/></Row>
    <Row><FeatherInput label="WQL Query" v-model="wqlQuery"  @update:modelValue="setValues"/></Row>

    <!-- add filter -->
    <ServiceFilter @setValues="setValues" />
  </ShowHideBox>
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
    FeatherInput,
    FeatherCheckbox,
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
    const showAdvancedOptions = ref(false)
    const useTLS = ref()
    const object = ref()
    const port = ref()
    const wqlQuery = ref()

    // form
    const timeout = ref()
    const retry = ref()
    const username = ref()
    const password = ref()

    const data = computed(() => ({ 
      timeout: timeout.value, 
      retry: retry.value, 
      username: username.value, 
      password: password.value, 
      port: port.value,
      useTLS: useTLS.value,
      object: object.value,
      wqlQuery: wqlQuery.value
    }))

    const setValues = (filterValues: any) => context.emit('set-values', { index: props.index, data: {...data.value, ...filterValues} })

    return {
      port,
      retry,
      object,
      useTLS,
      timeout,
      username,
      wqlQuery,
      password,
      showAdvancedOptions,
      setValues
    }
  }
})
</script>
