<template>
<div class="feather-row">
  <div class="feather-col-6">
    <Row><span class="headline3">SNMP</span></Row>
    <Row col="6"><FeatherInput label="v1/v2c community string" type="text" v-model="v1v2"  @update:modelValue="setValues"/></Row>
    <Row col="6"><FeatherInput label="Timeout" type="number" v-model="timeout"  @update:modelValue="setValues" /></Row>
    <Row col="6"><FeatherInput label="Retry" type="number" v-model="retry"  @update:modelValue="setValues" /></Row>

    <ShowHideBox label="Advanced options">
      <Row>
        <FeatherSelect 
          v-model="securityLevel" 
          @update:modelValue="setValues" 
           
          :options="options"
          text-prop="label"
          label="Security level"
        />
      </Row>

      <!-- add filter -->
      <ServiceFilter @setValues="setValues" />
    </ShowHideBox>
  </div>
  <div class="feather-col-6">
    <ResponseTable />
  </div>
</div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { FeatherSelect } from '@featherds/select'
import { FeatherInput } from '@featherds/input'
import Row from '@/components/Common/Row.vue'
import ShowHideBox from '@/components/Common/ShowHideBox.vue'
import ServiceFilter from './ServiceFilter.vue'
import ResponseTable from './ResponseTable.vue'

export default defineComponent({
  components: {
    Row,
    FeatherSelect,
    FeatherInput,
    ShowHideBox,
    ServiceFilter,
    ResponseTable
  },
  emits: ['set-values'],
  props: {
    index: {
      type: Number,
      required: true
    }
  },
  setup(props, context) {

    const options = [
      { label: 'noAuthNoPriv', value: 1 },
      { label: 'authNoPriv', value: 2 },
      { label: 'authPriv', value: 3 }
    ]

    // advanced options
    const securityLevel = ref(1)

    // form
    const v1v2 = ref()
    const timeout = ref()
    const retry = ref()

    const data = computed(() => ({ 
      timeout: Number(timeout.value), 
      retry: Number(retry.value), 
      communityString: v1v2.value,
      securityLevel: securityLevel.value
    }))

    const setValues = () => context.emit('set-values', { index: props.index, data: {...data.value } })

    return {
      v1v2,
      retry,
      timeout,
      securityLevel,
      setValues,
      options
    }
  }
})
</script>
