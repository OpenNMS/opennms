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
      scrollable
      :rows="pageSize"
      :first="first"
      :rowsPerPageOptions="[5, 10, 20, 50]"
      :loading="showSpinner"
      :tableStyle="`min-width: ${TABLE_MIN_WIDTH_PX}px`"
      sortField="ifIndex"
      :sortOrder="1"
      data-test="snmp-interfaces-table"
      @page="onPage"
    >
      <OnmsColumn style="min-width: 90px" field="ifIndex" header="Index" sortable>
        <template #body="{ data }">
          <a :href="snmpInterfaceLink(baseHref, nodeId, data.ifIndex)" data-test="if-index-link">{{ data.ifIndex }}</a>
        </template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 110px" field="ifName" header="Name" sortable>
        <template #body="{ data }"><span data-test="if-name">{{ data.ifName || 'N/A' }}</span></template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 110px" field="status" header="Status" sortable>
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
      <OnmsColumn style="min-width: 140px" field="ifAlias" header="Alias" sortable>
        <template #body="{ data }">{{ data.ifAlias || 'N/A' }}</template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 110px" field="ifSpeed" header="Speed" sortable>
        <template #body="{ data }"><span data-test="if-speed">{{ data.speedLabel }}</span></template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 200px" field="ifDescr" header="Descr" sortable>
        <template #body="{ data }"><span data-test="if-descr">{{ data.ifDescr || 'N/A' }}</span></template>
      </OnmsColumn>
      <OnmsColumn style="min-width: 130px" field="flows" header="Flows" sortable>
        <template #body="{ data }">
          <span v-if="data.flows" class="flows-cell" data-test="flows">
            <OnmsTag
              v-if="data.hasIngressFlows"
              v-onms-tooltip.top="snmpInterfaceFlowsTooltip(data)"
              value="I"
              severity="success"
              class="tooltip-target"
              data-test="flows-ingress-tag"
            />
            <OnmsTag
              v-if="data.hasEgressFlows"
              v-onms-tooltip.top="snmpInterfaceFlowsTooltip(data)"
              value="E"
              severity="success"
              class="tooltip-target"
              data-test="flows-egress-tag"
            />
            <OnmsIconButton
              :icon="ViewDetails"
              iconSize="1.25rem"
              :tooltip="snmpInterfaceFlowGraphsTooltip(data)"
              :disabled="pendingFlowsIfIndex === data.ifIndex"
              data-test="flows-button"
              @click="openFlowGraphs(data)"
            />
          </span>
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
import { OnmsColumn, OnmsIconButton, OnmsSearchInput, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { snmpInterfaceLink } from '@/lib/linkUtils'
import { getFlowGraphUrl } from '@/services/flowService'
import useSnackbar from '@/composables/useSnackbar'
import { useDebouncedSearch } from './hooks/useDebouncedSearch'
import { useDelayedLoading } from './hooks/useDelayedLoading'
import {
  formatIfSpeed,
  matchesSearchTerm,
  snmpInterfaceFlowGraphsTooltip,
  snmpInterfaceFlows,
  snmpInterfaceFlowsTooltip,
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
const { showSnackBar } = useSnackbar()
const route = useRoute()

const nodeId = computed(() => route.params.id as string)
const baseHref = computed(() => menuStore.mainMenu.baseHref)

const DEFAULT_PAGE_SIZE = 5

// The sum of the columns' own min-widths. The table is wider than the half-page panel it sits
// in, deliberately: it scrolls horizontally rather than squeezing seven columns into ~463px.
// Keep in step with the styles on the columns below.
const TABLE_MIN_WIDTH_PX = 890

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
    speedLabel: formatIfSpeed(snmpInterface.ifSpeed),
    flows: snmpInterfaceFlows(snmpInterface)
  })))

// Filters on what the columns actually show -- so the formatted speed label rather than the raw
// ifSpeed. Flows is the one exception: the cells show 'I' and 'E', but the term is matched
// against the full 'Ingress/Egress' key behind them, so typing 'ingress' narrows the table to
// the interfaces carrying flows the same way sorting that column groups them.
const rows = computed(() => decorated.value.filter(row => matchesSearchTerm(appliedTerm.value, [
  row.ifIndex,
  row.ifName,
  row.status,
  row.ifAlias,
  row.speedLabel,
  row.ifDescr,
  row.flows
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

// Resolving a flow graph URL is one request per interface, and the server runs a flow count for
// each one, so the URLs are fetched when a user asks for one rather than for every row up front.
// Nothing is kept afterwards: this is the whole of the flows state.
const pendingFlowsIfIndex = ref<number | undefined>(undefined)

const openFlowGraphs = async (row: { ifIndex: number }) => {
  if (pendingFlowsIfIndex.value !== undefined) {
    return
  }

  // Opened here rather than once the URL arrives: by then the click that authorised it has
  // expired and the browser blocks the tab. Deliberately without 'noopener', which would sever
  // the handle needed to navigate it (window.open returns null with that flag) -- the opener is
  // dropped below instead, while the tab is still about:blank and same-origin.
  const tab = window.open('', '_blank')

  if (tab) {
    tab.opener = null
  }

  pendingFlowsIfIndex.value = row.ifIndex

  try {
    const url = await getFlowGraphUrl(nodeId.value, row.ifIndex)

    if (!url) {
      tab?.close()
      showSnackBar({ msg: 'No \'flowGraphUrl\' was configured.', error: true })
      return
    }

    if (!tab) {
      showSnackBar({
        msg: 'The browser blocked the new tab. Allow pop-ups for this site to open flow graphs.',
        error: true
      })
      return
    }

    // An absolute URL into a separate tool (Grafana, typically), not an OpenNMS page.
    tab.location.href = url
  } catch {
    tab?.close()
    showSnackBar({ msg: 'Could not look up the flow graph URL.', error: true })
  } finally {
    pendingFlowsIfIndex.value = undefined
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

defineExpose({ onPage, openFlowGraphs })
</script>

<style lang="scss" scoped>
// Seven columns do not fit the half-width panel (~463px of table), so this one scrolls
// horizontally instead of compressing them: each column carries its own min-width and the table
// a min-width that is their sum. Nothing is frozen -- pinning Index and Name left only ~253px of
// scrolling region, which was worse than scrolling past them.
//
// The horizontal padding stays tightened to 8px a side (the stock 16px is what made five columns
// overflow before scrolling was an option). It is density now rather than necessity, and it
// matches the IP interfaces table on the neighbouring tab.
//
// Wrapping stays at word boundaries (no overflow-wrap: anywhere): forcing mid-word breaks at
// these widths shredded the headers a character at a time and split the UNKNOWN tag across two
// lines.
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
  padding-inline: 8px;
}

.interfaces-search {
  margin-bottom: 15px;
}

// Tags and the graphs button on one line, and the button pulled in: an icon button's own padding
// is sized for a toolbar, not for sitting beside a pair of one-letter tags.
.flows-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  :deep(.p-button) {
    padding: 2px;
  }
}

// The status tag is a hover-only affordance with nothing to click, so the cursor is the only cue
// that there is more behind it.
.tooltip-target {
  cursor: pointer;
}
</style>
