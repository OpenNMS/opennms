<template>
  <Row first><h3>SSH</h3></Row>
  <Row label="Timeout"><InputText v-model="timeout" class="input" @input="setValues" /></Row>
  <Row label="Retry"><InputText v-model="retry" class="input" @input="setValues" /></Row>

  <!-- advanced options -->
  <ShowHideBox label="Advanced options">
    <Row first label="Banner"><InputText v-model="banner" @input="setValues" class="input"/></Row>
    <Row label="Port"><InputText v-model="port" @input="setValues" class="input"/></Row>

    <!-- add filter -->
    <ServiceFilter @setValues="setValues" />
  </ShowHideBox>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import InputText from 'primevue/inputtext'
import Row from '@/components/Common/Row.vue'
import ShowHideBox from '@/components/Common/ShowHideBox.vue'
import ServiceFilter from './ServiceFilter.vue'

export default defineComponent({
  components: {
    Row,
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

<style scoped lang="scss"></style>
