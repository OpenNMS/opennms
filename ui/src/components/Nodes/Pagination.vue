<template>
  <Paginator
    :first="offset"
    :rows="limit"
    :total-records="totalCount"
    @page="onPage($event)"
    :rowsPerPageOptions="[5, 20, 30]"
  />
</template>
  
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Paginator, { PageState } from 'primevue/paginator'
import { useStore } from 'vuex'

const props = defineProps({
  moduleName: {
    type: String,
    required: true
  },
  functionName: {
    type: String,
    required: true
  },
  totalCountStateName: {
    type: String,
    required: true
  },
  parameters: {
    type: Object,
    required: true
  },
  payload: {
    type: Object,
    required: false
  }
})
const emit = defineEmits(['update-query-parameters'])
const store = useStore()
const limit = ref(props.parameters.limit)
const offset = ref(props.parameters.offset)

onMounted(() => store.dispatch(`${props.moduleName}/${props.functionName}`, props.payload || props.parameters))

const totalCount = computed(() => store.state[props.moduleName][props.totalCountStateName])

const onPage = (event: PageState) => {
  limit.value = event.rows
  offset.value = event.rows * event.page
  const updatedParameters = { ...props.parameters, limit: limit.value, offset: offset.value }
  emit('update-query-parameters', updatedParameters)
  if (props.payload) {
    store.dispatch(`${props.moduleName}/${props.functionName}`, { ...props.payload, queryParameters: updatedParameters })
    return
  }
  store.dispatch(`${props.moduleName}/${props.functionName}`, updatedParameters)
}
</script>