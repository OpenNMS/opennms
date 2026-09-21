<template>
  <div class="interfaces-table">
    <OnmsSearchInput
      class="interfaces-search"
      placeholder="Filter IP interfaces"
      aria-label="Filter IP interfaces"
      data-test="ip-interfaces-search"
      :modelValue="searchTerm"
      @update:modelValue="onSearch"
      @clear="clearSearch"
    />
    <OnmsTable
      :value="rows"
      paginator
      :rows="pageSize"
      :first="first"
      :rowsPerPageOptions="[5, 10, 20, 50]"
      :loading="showSpinner"
      scrollable
      :tableStyle="`min-width: ${TABLE_MIN_WIDTH_PX}px`"
      sortField="ipAddress"
      :sortOrder="1"
      data-test="ip-interfaces-table"
      @page="onPage"
    >
      <OnmsColumn style="min-width: 150px" field="ipAddress" header="IP Address" sortable>
        <template #body="{ data }">
          <a v-if="data.ipAddress" :href="interfaceLink(baseHref, nodeId, data.ipAddress)" data-test="ip-address-link">{{ data.ipAddress }}</a>
          <span v-else>N/A</span>
        </template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 200px" field="hostName" header="IP Host Name" sortable>
        <template #body="{ data }">{{ data.hostName || 'N/A' }}</template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 110px" field="ifIndex" header="SNMP ifIndex" sortable>
        <template #body="{ data }">{{ data.ifIndex || 'N/A' }}</template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 170px" field="status" header="Status" sortable>
        <template #body="{ data }">
          <OnmsTag
            :value="data.status"
            :severity="statusSeverity(data.isManaged)"
            data-test="status-tag"
          />
        </template>
      </OnmsColumn>
      <template #empty>
        <EmptyList v-if="!isFetching" :content="emptyListContent" data-test="empty-list" />
      </template>
    </OnmsTable>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsSearchInput, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { interfaceLink } from '@/lib/linkUtils'
import { useDebouncedSearch } from './hooks/useDebouncedSearch'
import { useDelayedLoading } from './hooks/useDelayedLoading'
import { ipInterfaceStatus, matchesSearchTerm } from './utils'

const props = withDefaults(defineProps<{
  // Whether this table's tab is the one showing. Default true so the table still works mounted
  // on its own; InterfacesTabs passes the real value.
  active?: boolean
}>(), { active: true })

const menuStore = useMenuStore()
const nodeStore = useNodeStore()
const route = useRoute()

const nodeId = computed(() => route.params.id as string)
const baseHref = computed(() => menuStore.mainMenu.baseHref)

const DEFAULT_PAGE_SIZE = 5

// The sum of the columns' own min-widths: the table scrolls horizontally rather than squeezing
// four columns, one of them a spelled-out status, into the half-page panel. Status is 170px
// because 'Forced Unmanaged' needs 147px plus the cell's padding to stay on one line -- measured;
// at 150px the tag wrapped and made those rows half again as tall as the others.
const TABLE_MIN_WIDTH_PX = 630

// Unmanaged states are a warning rather than a danger -- an interface nobody is polling is
// usually somebody's decision, not a fault. 'D' is danger because a deleted interface is on its
// way out of the database entirely, and 'N' is secondary because nothing writes it any more.
// Any code the product does not define reads as a warning: it is a data problem worth noticing.
const STATUS_SEVERITIES: Record<string, OnmsTagSeverity> = {
  M: 'success',
  U: 'warn',
  F: 'warn',
  N: 'secondary',
  D: 'danger',
  A: 'warn'
}

const statusSeverity = (isManaged?: string | null): OnmsTagSeverity =>
  (isManaged && STATUS_SEVERITIES[isManaged]) || 'warn'

const pageSize = ref(DEFAULT_PAGE_SIZE)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const { searchTerm, appliedTerm, onSearch, clearSearch } = useDebouncedSearch()
const { isFetching, showSpinner, start: startLoading, stop: stopLoading } = useDelayedLoading()

// Every interface for the node in one request (limit 0 is "no limit"), the way the legacy
// interfaces page fetched them -- see the same note in SnmpInterfacesTable.
//
// No _s narrowing any more. It read isManaged==U,P,N,M, which asked for 'P' -- an isSnmpPrimary
// code (Polled), not an interface status -- while leaving out 'F', the state an operator puts an
// interface into by unmanaging it. So every explicitly unmanaged interface was missing from this
// table. The legacy page fetched them all and said which was which, which is what the Status
// column does now.
const queryParameters = { limit: 0 }

// status is derived rather than stored, so it is decorated on here -- which is what lets the
// column sort on the label and the filter match it. Spelling the code out is the point: 'managed'
// now finds the managed interfaces, where the raw column only ever answered to 'M'.
const decorated = computed(() =>
  nodeStore.ipInterfaces.map(ipInterface => ({
    ...ipInterface,
    status: ipInterfaceStatus(ipInterface.isManaged)
  })))

// Filters on what the columns actually show.
const rows = computed(() => decorated.value.filter(row => matchesSearchTerm(appliedTerm.value, [
  row.ipAddress,
  row.hostName,
  row.ifIndex,
  row.status
])))

// The store action resolves once it has assigned the rows, so awaiting it is all the loading
// state needs -- no callback handed into the store, and no watch that could not tell a fetch
// that is still running from one that finished with nothing.
const fetchInterfaces = async () => {
  startLoading()

  try {
    await nodeStore.getNodeIpInterfaces({ id: nodeId.value, queryParameters })
  } finally {
    stopLoading()
  }
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
}

// The details page mounts BOTH tabs' tables at once -- PrimeVue 4 has no lazy TabPanel -- so
// fetching on mount pulled every IP interface for the node even when the user never opened
// this tab. On a switch with thousands of them that is the expensive request, paid for nothing.
// Fetch when the tab is first shown instead, and again if the node changes while it is showing.
// A node changed behind a hidden tab is picked up when the user comes back to it, and the store
// clears the rows as that fetch starts, so what they see is never the previous node's.
const fetchedNodeId = ref<string | undefined>(undefined)

const fetchInterfacesIfShown = () => {
  if (!props.active || fetchedNodeId.value === nodeId.value) {
    return
  }

  fetchedNodeId.value = nodeId.value
  fetchInterfaces()
}

// Paging and the filter belong to the node that was on screen, so they reset whether or not this
// tab is the one showing.
watch(nodeId, () => {
  first.value = 0
  clearSearch()
})

watch([() => props.active, nodeId], fetchInterfacesIfShown, { immediate: true })

defineExpose({ onPage })
</script>

<style lang="scss" scoped>
// Scrolls horizontally like the SNMP table on the neighbouring tab: each column carries its own
// min-width and the table a min-width that is their sum. Spelling the status out is what tipped
// it -- 'Forced Unmanaged' is far wider than the single letter the column used to hold, and the
// panel is only half the page.
//
// Horizontal padding stays tightened to 8px a side (the stock is 16px), matching the SNMP table
// so the two tabs read at the same density.
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
  padding-inline: 8px;
}

.interfaces-search {
  margin-bottom: 15px;
}
</style>
