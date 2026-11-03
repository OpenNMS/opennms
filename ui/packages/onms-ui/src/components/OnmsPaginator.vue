<template>
  <Paginator
    :first="first"
    :rows="rows"
    :totalRecords="totalRecords"
    :rowsPerPageOptions="rowsPerPageOptions"
    :pageLinkSize="pageLinkSize"
    :alwaysShow="alwaysShow"
    :pt="unsafePt as never"
    @page="onPage"
  />
</template>

<script setup lang="ts">
import Paginator from 'primevue/paginator'
import { OnmsTablePageEvent } from '../types'

// Seam wrapper (NMS-20303) around PrimeVue Paginator: paging controls on their own, for content
// that is not an OnmsTable -- which has its own paginator built in. `page` emits the same
// OnmsTablePageEvent shape OnmsTable does, so a handler serves either. `alwaysShow` keeps PrimeVue's
// default (true); set it false to hide the controls when everything fits on one page.
withDefaults(defineProps<{
  first?: number
  rows?: number
  totalRecords?: number
  rowsPerPageOptions?: number[]
  pageLinkSize?: number
  alwaysShow?: boolean
  unsafePt?: unknown
}>(), {
  first: 0,
  rows: 10,
  totalRecords: 0,
  rowsPerPageOptions: undefined,
  pageLinkSize: 5,
  alwaysShow: true,
  unsafePt: undefined
})

const emit = defineEmits<{
  page: [event: OnmsTablePageEvent]
}>()

const onPage = (event: { page: number, first: number, rows: number, pageCount?: number }) => {
  emit('page', { page: event.page, first: event.first, rows: event.rows, pageCount: event.pageCount })
}
</script>
