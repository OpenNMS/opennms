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
        <EmptyList :content="emptyListContent" data-test="empty-list" />
      </template>
    </OnmsTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsSearchInput, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { snmpInterfaceLink } from '@/lib/linkUtils'
import { useDebouncedSearch } from './hooks/useDebouncedSearch'
import {
  formatIfSpeed,
  matchesSearchTerm,
  snmpInterfaceNameTooltip,
  snmpInterfaceStatus,
  snmpInterfaceStatusTooltip,
  type SnmpInterfaceStatus
} from './utils'

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

const statusSeverity: Record<SnmpInterfaceStatus, OnmsTagSeverity> = {
  UP: 'success',
  DOWN: 'danger',
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

const fetchInterfaces = () => {
  nodeStore.getNodeSnmpInterfaces({ id: nodeId.value, queryParameters })
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
}

onMounted(fetchInterfaces)

// A narrowed result set is shorter than the one the page number was chosen against, so staying
// on page 3 of the old set would show an empty page of the new one.
watch(appliedTerm, () => {
  first.value = 0
})

// The details page keeps one instance of this table across node ids, so the id has to be
// followed rather than read once, or the tab keeps showing the SNMP interfaces of the node the user
// navigated away from. Back to the first page, since the page the user was on says nothing
// about the new node.
watch(nodeId, () => {
  first.value = 0
  clearSearch()
  fetchInterfaces()
})

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
