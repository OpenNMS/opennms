<template>
  <DataTable
    :value="value"
    :dataKey="dataKey"
    :lazy="lazy"
    :paginator="paginator"
    :rows="rows"
    :rowsPerPageOptions="rowsPerPageOptions"
    :first="first"
    :totalRecords="totalRecords"
    :sortField="sortField ?? undefined"
    :sortOrder="sortOrder ?? undefined"
    :stripedRows="stripedRows"
    :size="size"
    :scrollable="scrollable"
    :selectionMode="selectionMode"
    :scrollHeight="scrollHeight"
    :tableStyle="tableStyle"
    :editMode="editMode"
    :editingRows="editingRows"
    :expandedRows="expandedRows"
    :virtualScrollerOptions="virtualScrollerOptions"
    :pt="pt as never"
    @page="emit('page', $event)"
    @sort="emit('sort', $event)"
    @row-edit-save="emit('row-edit-save', $event)"
    @row-click="emit('row-click', $event)"
    @update:first="emit('update:first', $event)"
    @update:rows="emit('update:rows', $event)"
    @update:expandedRows="emit('update:expandedRows', $event as any)"
    @update:editingRows="emit('update:editingRows', $event as any)"
  >
    <slot />
    <template
      v-if="$slots.empty"
      #empty
    >
      <slot name="empty" />
    </template>
    <template
      v-if="$slots.expansion"
      #expansion="slotProps"
    >
      <slot
        name="expansion"
        v-bind="slotProps"
      />
    </template>
  </DataTable>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DataTable from 'primevue/datatable'
import type { OnmsTablePageEvent, OnmsTableRowClickEvent, OnmsTableRowEditSaveEvent, OnmsTableSortEvent } from '../types'

// Seam wrapper (NMS-20081) around PrimeVue DataTable. Columns are declared
// with OnmsColumn children in the default slot (OnmsColumn is a typed
// re-export — see OnmsColumn.ts for why). Event payloads are forwarded
// verbatim; the Onms*Event types mirror PrimeVue's field names so handlers
// written against PrimeVue keep compiling. DOM attrs (aria-label, data-test,
// class) fall through to the root element.
//
// Baked default: every column's header cell gets scope="col" (PrimeVue omits
// it), applied via table-level pt { column: { headerCell } } and deep-merged
// under unsafePt so a consumer's own column.headerCell keys — including an
// explicit scope — win on collision.
const props = withDefaults(defineProps<{
  value?: any[]
  dataKey?: string
  lazy?: boolean
  paginator?: boolean
  rows?: number
  rowsPerPageOptions?: number[]
  first?: number
  totalRecords?: number
  sortField?: string | ((item: any) => string) | null
  sortOrder?: number | null
  stripedRows?: boolean
  size?: 'small' | 'large'
  scrollable?: boolean
  selectionMode?: 'single' | 'multiple'
  scrollHeight?: string
  tableStyle?: string | Record<string, string>
  editMode?: 'row'
  editingRows?: any[]
  // PrimeVue accepts either an array of row instances or an object keyed by
  // dataKey (its DataTableExpandedRows shape) when a table uses dataKey.
  expandedRows?: any[] | Record<string, boolean>
  // Narrowed from PrimeVue's virtualScrollerOptions object: row height in px
  virtualScrollItemSize?: number
  unsafePt?: unknown
}>(), {
  value: undefined,
  dataKey: undefined,
  lazy: false,
  paginator: false,
  rows: undefined,
  rowsPerPageOptions: undefined,
  first: undefined,
  totalRecords: undefined,
  sortField: undefined,
  sortOrder: undefined,
  stripedRows: false,
  size: undefined,
  scrollable: false,
  selectionMode: undefined,
  scrollHeight: undefined,
  tableStyle: undefined,
  editMode: undefined,
  editingRows: undefined,
  expandedRows: undefined,
  virtualScrollItemSize: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  page: [event: OnmsTablePageEvent]
  sort: [event: OnmsTableSortEvent]
  'row-edit-save': [event: OnmsTableRowEditSaveEvent]
  'row-click': [event: OnmsTableRowClickEvent]
  'update:first': [value: number]
  'update:rows': [value: number]
  'update:expandedRows': [value: any[] | Record<string, boolean>]
  'update:editingRows': [value: any[]]
}>()

const virtualScrollerOptions = computed(() =>
  props.virtualScrollItemSize !== undefined ? { itemSize: props.virtualScrollItemSize } : undefined)

const pt = computed(() => {
  const base = props.unsafePt as Record<string, unknown> | undefined
  const column = base?.column as Record<string, unknown> | undefined
  const headerCell = column?.headerCell as Record<string, unknown> | undefined
  return {
    ...base,
    column: {
      ...column,
      headerCell: { scope: 'col', ...headerCell }
    }
  }
})
</script>
