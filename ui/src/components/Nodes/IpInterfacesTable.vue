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
      sortField="ipAddress"
      :sortOrder="1"
      data-test="ip-interfaces-table"
      @page="onPage"
    >
      <OnmsColumn field="ipAddress" header="IP Address" sortable>
        <template #body="{ data }">
          <a v-if="data.ipAddress" :href="interfaceLink(baseHref, nodeId, data.ipAddress)" data-test="ip-address-link">{{ data.ipAddress }}</a>
          <span v-else>N/A</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="hostName" header="IP Host Name" sortable>
        <template #body="{ data }">{{ data.hostName || 'N/A' }}</template>
      </OnmsColumn>
      <OnmsColumn field="ifIndex" header="SNMP ifIndex" sortable>
        <template #body="{ data }">{{ data.ifIndex || 'N/A' }}</template>
      </OnmsColumn>
      <OnmsColumn field="isManaged" header="Managed" sortable>
        <template #body="{ data }">{{ data.isManaged || 'N/A' }}</template>
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
import { OnmsColumn, OnmsSearchInput, OnmsTable, type OnmsTablePageEvent } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { interfaceLink } from '@/lib/linkUtils'
import { useDebouncedSearch } from './hooks/useDebouncedSearch'
import { matchesSearchTerm } from './utils'

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

// Every interface for the node in one request (limit 0 is "no limit"), the way the legacy
// interfaces page fetched them -- see the same note in SnmpInterfacesTable. The _s term is a
// different thing from the search box: it decides which interfaces belong on this panel at all.
const queryParameters = {
  limit: 0,
  _s: 'isManaged==U,isManaged==P,isManaged==N,isManaged==M'
}

// Filters on what the columns actually show.
const rows = computed(() => nodeStore.ipInterfaces.filter(row => matchesSearchTerm(appliedTerm.value, [
  row.ipAddress,
  row.hostName,
  row.ifIndex,
  row.isManaged
])))

const fetchInterfaces = () => {
  nodeStore.getNodeIpInterfaces({ id: nodeId.value, queryParameters })
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
// followed rather than read once, or the tab keeps showing the IP interfaces of the node the user
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
.interfaces-search {
  margin-bottom: 15px;
}
</style>
