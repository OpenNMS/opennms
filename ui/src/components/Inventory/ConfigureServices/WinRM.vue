<template>
  <Row first><h3>WinRM / WSMan</h3></Row>
  <Row label="Username"><FeatherInput v-model="username" class="input" @update:modelValue="setValues" /></Row>
  <Row label="Password"><FeatherInput v-model="password" class="input" @update:modelValue="setValues" /></Row>
  <Row label="Timeout"><FeatherInput v-model="timeout" class="input" @update:modelValue="setValues" /></Row>
  <Row label="Retry"><FeatherInput v-model="retry" class="input" @update:modelValue="setValues" /></Row>

  <!-- advanced options -->
  <ShowHideBox label="Advanced options">
    <Row first label="Port"><FeatherInput v-model="port" class="input" @update:modelValue="setValues"/></Row>
    <Row label="Use TLS"><FeatherCheckbox v-model="useTLS" @update:modelValue="setValues" /></Row>
    <Row label="Object"><FeatherInput v-model="object" class="input" @update:modelValue="setValues"/></Row>
    <Row label="WQL Query"><FeatherInput v-model="wqlQuery" class="input" @update:modelValue="setValues"/></Row>

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
