<template>
  <OnmsTable
    lazy
    :value="nodeStore.snmpInterfaces"
    paginator
    :rows="pageSize"
    :first="first"
    :totalRecords="nodeStore.snmpInterfacesTotalCount"
    :rowsPerPageOptions="[5, 10, 20, 50]"
    data-test="snmp-interfaces-table"
    @page="onPage"
  >
    <OnmsColumn field="ifIndex" header="SNMP ifIndex">
      <template #body="{ data }">
        <a :href="snmpInterfaceLink(baseHref, nodeId, data.ifIndex)" data-test="if-index-link">{{ data.ifIndex }}</a>
      </template>
    </OnmsColumn>
    <OnmsColumn header="Status">
      <template #body="{ data }">
        <OnmsTag
          v-onms-tooltip.top="snmpInterfaceStatusTooltip(data)"
          :value="snmpInterfaceStatus(data)"
          :severity="statusSeverity[snmpInterfaceStatus(data) as SnmpInterfaceStatus]"
          class="tooltip-target"
          data-test="status-tag"
        />
      </template>
    </OnmsColumn>
    <OnmsColumn field="ifName" header="SNMP ifName">
      <template #body="{ data }">
        <span
          v-onms-tooltip.top="snmpInterfaceNameTooltip(data)"
          class="tooltip-target"
          data-test="if-name"
        >{{ data.ifName || 'N/A' }}</span>
      </template>
    </OnmsColumn>
    <OnmsColumn field="ifAlias" header="SNMP ifAlias">
      <template #body="{ data }">{{ data.ifAlias || 'N/A' }}</template>
    </OnmsColumn>
    <OnmsColumn field="ifSpeed" header="SNMP ifSpeed">
      <template #body="{ data }"><span v-html="data.ifSpeed" /></template>
    </OnmsColumn>
    <template #empty>
      <EmptyList :content="emptyListContent" data-test="empty-list" />
    </template>
  </OnmsTable>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { snmpInterfaceLink } from '@/lib/linkUtils'
import {
  snmpInterfaceNameTooltip,
  snmpInterfaceStatus,
  snmpInterfaceStatusTooltip,
  type SnmpInterfaceStatus
} from '@/components/Nodes/utils'
import { SORT } from '@/types'

const menuStore = useMenuStore()
const nodeStore = useNodeStore()
const route = useRoute()

const nodeId = computed(() => route.params.id as string)
const baseHref = computed(() => menuStore.mainMenu.baseHref)

const DEFAULT_PAGE_SIZE = 5

const pageSize = ref(DEFAULT_PAGE_SIZE)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const statusSeverity: Record<SnmpInterfaceStatus, OnmsTagSeverity> = {
  UP: 'success',
  DOWN: 'danger',
  UNKNOWN: 'info'
}

// Ordered by ifIndex ascending. The table is lazy -- each page is a separate request -- so the
// order has to be asked of the API; sorting the rows client-side would only order the page in
// hand and leave which rows land on which page up to the server's default (unordered) sequence.
// ifIndex is an INTEGER search property on OnmsSnmpInterface, so this sorts numerically.
const queryParameters = ref({
  limit: DEFAULT_PAGE_SIZE,
  offset: 0,
  orderBy: 'ifIndex',
  order: SORT.ASCENDING
})

const fetchInterfaces = () => {
  nodeStore.getNodeSnmpInterfaces({ id: nodeId.value, queryParameters: queryParameters.value })
}

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = { ...queryParameters.value, offset: event.first, limit: event.rows }
  fetchInterfaces()
}

onMounted(fetchInterfaces)

// The details page keeps one instance of this table across node ids, so the id has to be
// followed rather than read once, or the tab keeps showing the SNMP interfaces of the node the user
// navigated away from. Back to the first page, since the page the user was on says nothing
// about the new node.
watch(nodeId, () => {
  first.value = 0
  queryParameters.value = { ...queryParameters.value, offset: 0 }
  fetchInterfaces()
})

defineExpose({ onPage })
</script>

<style lang="scss" scoped>
// Both tooltip hosts are hover-only affordances with nothing to click, so the
// cursor is the only cue that there is more behind them.
.tooltip-target {
  cursor: pointer;
}
</style>
