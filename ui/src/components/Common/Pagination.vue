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
import { FeatherPagination } from '@featherds/pagination'
import { PropType } from 'vue'
import { QueryParameters } from '@/types'

const props = defineProps({
  query: {
    required: true,
    type: Function as PropType<(params: QueryParameters) => void>
  },
  getTotalCount: {
    required: true,
    type: Function as PropType<() => number>
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
const pageSize = ref(props.parameters.limit)
const page = ref(1)

onMounted(() => props.query(props.payload || props.parameters))

const totalCount = computed(() => {
  const totalCount = props.getTotalCount()

  if (totalCount && !isNaN(totalCount)) {
    return totalCount
  }

  return 0
})

const updatePage = () => {
  const updatedParameters = { ...props.parameters, limit: pageSize.value, offset: (page.value - 1) * pageSize.value }
  emit('update-query-parameters', updatedParameters)

  if (props.payload) {
    props.query({ ...props.payload, queryParameters: updatedParameters })
    return
  }

  props.query(updatedParameters)
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
