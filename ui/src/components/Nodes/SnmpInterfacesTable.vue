<template>
  <div class="interfaces-table">
    <OnmsSearchInput
      class="interfaces-search"
      placeholder="Filter SNMP interfaces"
      aria-label="Filter SNMP interfaces"
      data-test="snmp-interfaces-search"
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
      tableStyle="table-layout: fixed; width: 100%"
      sortField="ifIndex"
      :sortOrder="1"
      data-test="snmp-interfaces-table"
      @page="onPage"
    >
      <OnmsColumn style="width: 19%" field="ifIndex" header="SNMP ifIndex" sortable>
        <template #body="{ data }">
          <a :href="snmpInterfaceLink(baseHref, nodeId, data.ifIndex)" data-test="if-index-link">{{ data.ifIndex }}</a>
        </template>
      </OnmsColumn>
      <OnmsColumn style="width: 22%" field="status" header="Status" sortable>
        <template #body="{ data }">
          <OnmsTag
            v-onms-tooltip.top="snmpInterfaceStatusTooltip(data)"
            :value="data.status"
            :severity="statusSeverity[data.status as SnmpInterfaceStatus]"
            class="tooltip-target"
            data-test="status-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn style="width: 20%" field="ifName" header="SNMP ifName" sortable>
        <template #body="{ data }">
          <span
            v-onms-tooltip.top="snmpInterfaceNameTooltip(data)"
            class="tooltip-target"
            data-test="if-name"
          >{{ data.ifName || 'N/A' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn style="width: 19%" field="ifAlias" header="SNMP ifAlias" sortable>
        <template #body="{ data }">{{ data.ifAlias || 'N/A' }}</template>
      </OnmsColumn>
      <OnmsColumn style="width: 20%" field="ifSpeed" header="SNMP ifSpeed" sortable>
        <template #body="{ data }"><span data-test="if-speed">{{ data.speedLabel }}</span></template>
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
import { snmpInterfaceLink } from '@/lib/linkUtils'
import { useDebouncedSearch } from './hooks/useDebouncedSearch'
import { useDelayedLoading } from './hooks/useDelayedLoading'
import {
  formatIfSpeed,
  matchesSearchTerm,
  snmpInterfaceNameTooltip,
  snmpInterfaceStatus,
  snmpInterfaceStatusTooltip,
  type SnmpInterfaceStatus
} from './utils'

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

const pageSize = ref(DEFAULT_PAGE_SIZE)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const { searchTerm, appliedTerm, onSearch, clearSearch } = useDebouncedSearch()
const { isFetching, showSpinner, start: startLoading, stop: stopLoading } = useDelayedLoading()

// DISABLED is a warning rather than a danger: the interface is down because somebody turned it
// off, which is not a fault. TESTING is informational for the same reason -- mid-change.
const statusSeverity: Record<SnmpInterfaceStatus, OnmsTagSeverity> = {
  UP: 'success',
  DOWN: 'danger',
  DISABLED: 'warn',
  TESTING: 'info',
  UNKNOWN: 'info'
}

// Every interface for the node in one request (limit 0 is "no limit"), the way the legacy
// interfaces page fetched them. Sorting and filtering are client-side, and neither can be
// correct over a single server page: filtering would only ever search the rows already on
// screen, so a term matching something on page 3 would read as "no results".
const queryParameters = { limit: 0 }

// status and speedLabel are derived rather than stored, so they are decorated on here. For
// status that is what lets the column sort (PrimeVue sorts by field) and the filter match
// 'up'/'down'. speedLabel is display and filtering only -- the ifSpeed column deliberately still
// sorts on the raw number, since sorting the labels would put '1 Gbps' before '100 Mbps'.
const decorated = computed(() =>
  nodeStore.snmpInterfaces.map(snmpInterface => ({
    ...snmpInterface,
    status: snmpInterfaceStatus(snmpInterface),
    speedLabel: formatIfSpeed(snmpInterface.ifSpeed)
  })))

// Filters on what the columns actually show. ifDescr is deliberately absent: it lost its column
// and only appears in the ifName tooltip.
const rows = computed(() => decorated.value.filter(row => matchesSearchTerm(appliedTerm.value, [
  row.ifIndex,
  row.status,
  row.ifName,
  row.ifAlias,
  row.speedLabel
])))

// The store action resolves once it has assigned the rows, so awaiting it is all the loading
// state needs -- no callback handed into the store, and no watch that could not tell a fetch
// that is still running from one that finished with nothing.
const fetchInterfaces = async () => {
  startLoading()

  try {
    await nodeStore.getNodeSnmpInterfaces({ id: nodeId.value, queryParameters })
  } finally {
    stopLoading()
  }
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
}

// The details page mounts BOTH tabs' tables at once -- PrimeVue 4 has no lazy TabPanel -- so
// fetching on mount pulled every SNMP interface for the node even when the user never opened
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
// The panel is half the page wide (~463px of table), and five sortable columns did not fit: each
// header needs its widest word plus the sort indicator plus the cell's horizontal padding, which
// measured ~100-108px apiece -- ~513px against 463px available. So the table overflowed into a
// horizontal scrollbar that hid ifSpeed.
//
// Percentages alone cannot solve that, since the shortfall is in the minimums. The stock 16px of
// padding per side is what gives: at 8px the five minimums come to ~433px and fit with room to
// spare. Shortening the headers would NOT have helped -- the binding word is already 'ifIndex',
// not the 'SNMP' prefix, so dropping it changes the header's height, not its width.
//
// Wrapping stays at word boundaries (no overflow-wrap: anywhere): forcing mid-word breaks at
// these widths shredded the headers a character at a time and split the UNKNOWN tag across two
// lines. The widths above are set so each column's widest atom -- that tag, an ifSpeed value --
// fits on one line.
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
  padding-inline: 8px;
}

.interfaces-search {
  margin-bottom: 15px;
}

// Both tooltip hosts are hover-only affordances with nothing to click, so the
// cursor is the only cue that there is more behind them.
.tooltip-target {
  cursor: pointer;
}
</style>
