<template>
  <Row first><h3>WinRM / WSMan</h3></Row>
  <Row label="Username"><InputText v-model="username" class="input" @input="setValues" /></Row>
  <Row label="Password"><InputText v-model="password" class="input" @input="setValues" /></Row>
  <Row label="Timeout"><InputText v-model="timeout" class="input" @input="setValues" /></Row>
  <Row label="Retry"><InputText v-model="retry" class="input" @input="setValues" /></Row>

  <!-- advanced options -->
  <ShowHideBox label="Advanced options">
    <Row first label="Port"><InputText v-model="port" class="input" @input="setValues"/></Row>
    <Row label="Use TLS"><Checkbox v-model="useTLS" @change="setValues" :binary="true"/></Row>
    <Row label="Object"><InputText v-model="object" class="input" @input="setValues"/></Row>
    <Row label="WQL Query"><InputText v-model="wqlQuery" class="input" @input="setValues"/></Row>

    <!-- add filter -->
    <ServiceFilter @setValues="setValues" />
  </ShowHideBox>
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
    InputText,
    Checkbox,
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

<style scoped lang="scss"></style>
