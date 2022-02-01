<template>
  <FeatherPagination
    v-if="totalCount"
    class="pagination"
    v-model="page"
    :pageSize="pageSize"
    :total="totalCount"
    @update:pageSize="updatePageSize"
    @update:modelValue="updatePage"
  ></FeatherPagination>
</template>
  
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { FeatherPagination } from '@featherds/pagination'
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
const pageSize = ref(props.parameters.limit)
const page = ref(1)

onMounted(() => store.dispatch(`${props.moduleName}/${props.functionName}`, props.payload || props.parameters))

const totalCount = computed(() => {
  const totalCount = store.state[props.moduleName][props.totalCountStateName]
  if (totalCount && !isNaN(totalCount)) return totalCount
  return 0
})

const updatePage = () => {
  const updatedParameters = { ...props.parameters, limit: pageSize.value, offset: (page.value - 1) * pageSize.value }
  emit('update-query-parameters', updatedParameters)
  if (props.payload) {
    store.dispatch(`${props.moduleName}/${props.functionName}`, { ...props.payload, queryParameters: updatedParameters })
    return
  }
  store.dispatch(`${props.moduleName}/${props.functionName}`, updatedParameters)
}

const updatePageSize = (v: number) => {
  pageSize.value = v
  updatePage()
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.pagination {
  @include body-small;
  background: var($surface);
  color: var($primary-text-on-surface);
}
</style>
