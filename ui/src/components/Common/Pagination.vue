<template>
  <PPaginator
    v-if="totalCount"
    class="pagination"
    :first="first"
    :rows="rows"
    :totalRecords="totalCount"
    :rowsPerPageOptions="rowsPerPageOptions"
    @page="onPage"
  />
</template>

<script setup lang="ts">
import Paginator from 'primevue/paginator'
import { PropType, computed, onMounted, ref } from 'vue'
import { QueryParameters } from '@/types'

const PPaginator = Paginator

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

// Paginator is offset-based (`first`/`rows`) rather than page-based.
const rows = ref<number>(props.parameters.limit)
const first = ref<number>(props.parameters.offset ?? 0)

// Match FeatherPagination's default page sizes, but always include the caller's
// initial page size so the rows-per-page dropdown has a valid selection.
const rowsPerPageOptions = computed(() => {
  const base = [10, 20, 50]
  const initial = props.parameters.limit
  if (initial && !base.includes(initial)) {
    return [...base, initial].sort((a, b) => a - b)
  }
  return base
})

onMounted(() => props.query(props.payload || props.parameters))

const totalCount = computed(() => {
  const totalCount = props.getTotalCount()

  if (totalCount && !isNaN(totalCount)) {
    return totalCount
  }

  return 0
})

const onPage = (event: { first: number, rows: number, page: number, pageCount?: number }) => {
  first.value = event.first
  rows.value = event.rows

  const updatedParameters = { ...props.parameters, limit: rows.value, offset: first.value }
  emit('update-query-parameters', updatedParameters)

  if (props.payload) {
    props.query({ ...props.payload, queryParameters: updatedParameters })
    return
  }

  props.query(updatedParameters)
}
</script>

<style scoped lang="scss">
.pagination {
  font-size: 0.875rem;
  background: var(--p-content-background);
  color: var(--p-content-color);
}
</style>
