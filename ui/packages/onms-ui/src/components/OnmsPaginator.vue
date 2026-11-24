<template>
  <Paginator
    :first="first"
    :rows="rows"
    :totalRecords="totalRecords"
    :rowsPerPageOptions="rowsPerPageOptions"
    :pageLinkSize="pageLinkSize"
    :alwaysShow="alwaysShow"
    :template="pageReport ? TEMPLATE_WITH_REPORT : undefined"
    :currentPageReportTemplate="pageReport"
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
//
// `pageReport` adds a line of text after the rows-per-page dropdown, so a page can say what it is
// paging -- 'Interfaces {first}–{last} of {totalRecords}' -- when that is not one row each. The
// placeholders are PrimeVue's: {first}, {last}, {totalRecords}, {currentPage}, {totalPages} and
// {rows}. Left out, the controls are PrimeVue's default set, with no text.
withDefaults(defineProps<{
  first?: number
  rows?: number
  totalRecords?: number
  rowsPerPageOptions?: number[]
  pageLinkSize?: number
  alwaysShow?: boolean
  pageReport?: string
  unsafePt?: unknown
}>(), {
  first: 0,
  rows: 10,
  totalRecords: 0,
  rowsPerPageOptions: undefined,
  pageLinkSize: 5,
  alwaysShow: true,
  pageReport: undefined,
  unsafePt: undefined
})

// PrimeVue's default template with the report added at the end.
const TEMPLATE_WITH_REPORT = 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown CurrentPageReport'

const emit = defineEmits<{
  page: [event: OnmsTablePageEvent]
}>()

const onPage = (event: { page: number, first: number, rows: number, pageCount?: number }) => {
  emit('page', { page: event.page, first: event.first, rows: event.rows, pageCount: event.pageCount })
}
</script>

<style lang="scss" scoped>
// PrimeVue gives the page report no spacing of its own -- only the paginator's 0.25rem gap, which
// suits the buttons but sets the text hard against the rows-per-page dropdown. Space just the report
// rather than the paginator.gap token, which would loosen every control in every paginator.
:deep(.p-paginator-current) {
  margin-inline-start: var(--onms-spacing-xxs);
}
</style>
