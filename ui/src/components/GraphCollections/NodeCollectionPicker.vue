<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  The "Node and Domain Interface Reports" of the legacy index page: pick a node or a domain to
  see a collection built on the fly from its SNMP interfaces. Nothing is stored until the user
  saves it as a collection from the view page.

  Nodes are searched on the server one bounded page at a time. Listing every top-level resource
  instead walks the whole resource tree on the server and hands the browser a list it then has to
  filter and draw on every keystroke, which stops being usable somewhere above a thousand nodes.
  Domains only come from that walk, so they are listed on request and kept for the page's life.
-->
<template>
  <TableCard>
    <div class="header">
      <div class="title-block">
        <div class="card-title">Node and Domain Collections</div>
        <div class="subtitle">A collection built on the fly from the SNMP interfaces of a node or a domain. Open one and save it to customize it.</div>
      </div>
    </div>

    <OnmsSearchInput
      :modelValue="filter"
      placeholder="Find a node by label"
      dataTest="ncp-filter"
      @update:modelValue="onFilter"
    />
    <p v-if="loadError" class="error" data-test="ncp-load-error">{{ loadError }}</p>
    <p v-else-if="loading && !visibleResources.length" class="muted" data-test="ncp-loading">Searching...</p>
    <p v-else-if="!visibleResources.length" class="muted" data-test="ncp-empty">{{ emptyMessage }}</p>
    <OnmsListbox
      v-else
      :options="visibleResources"
      :modelValue="null"
      dataKey="id"
      optionLabel="label"
      scrollHeight="18rem"
      aria-label="Nodes and domains"
      data-test="ncp-resources"
      @update:modelValue="(value) => open(value as PickerEntry | null)"
    >
      <template #option="{ option }">
        <span class="option">
          <span>{{ asEntry(option).label }}</span>
          <span class="muted">{{ asEntry(option).typeLabel }}</span>
        </span>
      </template>
    </OnmsListbox>
    <p v-if="truncated" class="muted" data-test="ncp-truncated">
      Showing {{ nodes.length }} of {{ totalNodes }} matching nodes. Narrow the filter if necessary.
    </p>

    <div class="domains">
      <template v-if="domains === null">
        <span class="muted">Domains are listed on request, since finding them means walking every resource.</span>
        <OnmsButton
          variant="text"
          size="small"
          :label="loadingDomains ? 'Loading domains...' : 'Show domains'"
          :disabled="loadingDomains"
          data-test="ncp-show-domains"
          @click="loadDomains"
        />
      </template>
      <span v-else-if="!domains.length" class="muted" data-test="ncp-no-domains">No domain has performance data.</span>
      <p v-if="domainsError" class="error" data-test="ncp-domains-error">{{ domainsError }}</p>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, onMounted, onScopeDispose, ref } from 'vue'
import { useRouter } from 'vue-router'
import { debounce } from 'lodash'
import { OnmsButton, OnmsListbox, OnmsSearchInput } from '@opennms/onms-ui'
import TableCard from '@/components/Common/TableCard.vue'
import { searchNodes } from '@/services/graphCollectionsService'
import { getResources } from '@/services/resourceService'
import { syntheticCollectionRoute } from './syntheticCollectionRoute'

interface PickerEntry { id: string, label: string, typeLabel: string, route: string }

// Enough nodes to scan through, few enough that the page stays quick with ten thousand behind it.
const NODE_PAGE_SIZE = 50
// Long enough to coalesce a burst of typing, short enough that the list still feels live.
const SEARCH_DEBOUNCE_MS = 175

const router = useRouter()

const filter = ref('')
const nodes = ref<PickerEntry[]>([])
const totalNodes = ref(0)
const loading = ref(true)
const loadError = ref('')
// null until the user asks for them
const domains = ref<PickerEntry[] | null>(null)
const loadingDomains = ref(false)
const domainsError = ref('')

// the listbox option slot is untyped
const asEntry = (option: unknown) => option as PickerEntry

const term = computed(() => filter.value.trim())
const visibleDomains = computed(() => {
  const needle = term.value.toLowerCase()
  return (domains.value ?? []).filter(d => !needle || d.label.toLowerCase().includes(needle))
})
// domains first, as the legacy page listed them; the nodes arrive sorted by label
const visibleResources = computed(() => [...visibleDomains.value, ...nodes.value])
const truncated = computed(() => totalNodes.value > nodes.value.length)
const emptyMessage = computed(() => {
  if (!term.value) {
    return 'No node has been provisioned yet.'
  }
  return domains.value ? 'No node or domain matches the filter.' : 'No node matches the filter.'
})

// a search that started before a later one answers must not overwrite the later one's result
let generation = 0

const search = async () => {
  const mine = ++generation
  loading.value = true
  const result = await searchNodes(term.value, NODE_PAGE_SIZE)
  if (mine !== generation) {
    return
  }
  loading.value = false
  if (result === null) {
    loadError.value = 'Failed to search the nodes.'
    return
  }
  loadError.value = ''
  nodes.value = result.nodes.map(n => ({
    id: `node[${n.id}]`,
    label: n.label,
    typeLabel: 'Node',
    route: `/graph-collections/node/${n.id}`
  }))
  totalNodes.value = result.totalCount
}

const debouncedSearch = debounce(search, SEARCH_DEBOUNCE_MS)

const onFilter = (value: unknown) => {
  filter.value = String(value ?? '')
  debouncedSearch()
}

// a keystroke close to unmount would otherwise search after the component is gone
onScopeDispose(() => debouncedSearch.cancel())

const loadDomains = async () => {
  loadingDomains.value = true
  domainsError.value = ''
  const response = await getResources()
  loadingDomains.value = false
  if (response === null) {
    domainsError.value = 'Failed to load the domains.'
    return
  }
  domains.value = (response.resource ?? [])
    .filter(r => r.id.startsWith('domain['))
    .map(r => ({ id: r.id, label: r.label, typeLabel: r.typeLabel, route: syntheticCollectionRoute(r.id) }))
    .filter((r): r is PickerEntry => r.route !== null)
    .sort((a, b) => a.label.localeCompare(b.label))
}

const open = (entry: PickerEntry | null) => {
  if (entry) {
    router.push(entry.route)
  }
}

onMounted(search)
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
}

.subtitle,
.muted {
  color: var(--p-text-muted-color);
}

.error {
  color: var(--p-red-500, #d32f2f);
}

.option {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
}

.domains {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
}
</style>
