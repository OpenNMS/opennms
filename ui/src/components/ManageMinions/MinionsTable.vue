<template>
  <TableCard class="minions-table">
    <p v-if="store.loadError && store.minions.length" class="stale-note" data-test="stale-note">
      Showing the last loaded data — the most recent refresh failed.
    </p>
    <p v-if="store.truncated" class="truncation-note" data-test="truncation-note">
      Showing the first {{ store.minions.length }} of {{ store.totalCount }} minions. Use search to narrow the list.
    </p>

    <!-- the quick filters, the monitoring location chip and the search box narrow the same list together -->
    <div v-if="store.minions.length" class="toolbar">
      <div class="filters">
        <OnmsSelectButton
          v-model="quickFilter"
          :options="quickFilterOptions"
          optionLabel="label"
          optionValue="value"
          aria-label="Quick filter"
          data-test="quick-filters"
        />
        <!-- the chip's own remove icon is not focusable, so a real button clears it -->
        <template v-if="locationFilter">
          <OnmsChip
            :label="`Monitoring location: ${locationFilter}`"
            data-test="location-filter-chip"
          />
          <OnmsButton
            variant="text"
            size="small"
            label="Clear"
            aria-label="Clear monitoring location filter"
            data-test="clear-location-filter"
            @click="emit('update:locationFilter', null)"
          />
        </template>
      </div>
      <!-- one box does both: the text narrows the rows as it is typed, and the Minions and monitoring
           locations it matches are offered as "Minion: id" / "Monitoring location: name" to pick one exactly;
           showEmptyMessage falls through to the PrimeVue root so no overlay opens for plain text -->
      <OnmsAutoComplete
        ref="searchBox"
        :modelValue="search"
        :suggestions="locationSuggestions"
        optionLabel="label"
        placeholder="Minions, Monitoring locations"
        :showEmptyMessage="false"
        :unsafePt="{ pcInputText: { root: { 'aria-label': 'Search minions, or choose a monitoring location to filter by', 'data-test': 'minion-search' }}}"
        class="search"
        data-test="minion-search-box"
        @update:modelValue="onSearchInput"
        @complete="suggestLocations"
        @optionSelect="chooseLocation"
      />
    </div>

    <OnmsTable
      :value="visibleMinions"
      v-model:filters="filters"
      :globalFilterFields="['id', 'location', 'status', 'version']"
      :paginator="visibleMinions.length > PAGE_SIZE"
      dataKey="id"
      sortField="id"
      :sortOrder="1"
      :rows="PAGE_SIZE"
      :rowsPerPageOptions="[PAGE_SIZE, 50, 100]"
      class="data-table"
      data-test="minions-table"
    >
      <template #empty>
        <EmptyList v-if="!store.isLoading" :content="emptyContent" data-test="empty-list" />
      </template>
      <OnmsColumn field="id" header="Minion" sortable>
        <template #body="{ data }">
          <a
            v-if="store.nodeIdFor(data)"
            :href="nodeUrl(store.nodeIdFor(data))"
            data-test="minion-node-link"
          >{{ data.id }}</a>
          <span v-else>{{ data.id }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="location" header="Monitoring location" sortable>
        <template #body="{ data }">
          <button
            v-if="data.location"
            type="button"
            class="link-button"
            :title="`Show monitoring location ${data.location}`"
            data-test="minion-location-link"
            @click="emit('showLocation', data.location)"
          >{{ data.location }}</button>
          <span v-else>-</span>
        </template>
      </OnmsColumn>
      <!-- the API only reports up / down / unknown; there is no degraded state or reason to show -->
      <OnmsColumn field="status" header="Status" sortable>
        <template #body="{ data }">
          <OnmsTag
            :value="(data.status ?? 'unknown').toUpperCase()"
            :severity="statusTagSeverity(data.status)"
            data-test="status-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn field="version" :sortField="versionSortField" header="Version" sortable>
        <template #body="{ data }">
          <OnmsTag
            :value="versionLabel(data.version)"
            :severity="versionTagSeverity(data.version)"
            :title="versionTitle(data.version)"
            data-test="version-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn field="date" header="Last heartbeat" sortable>
        <template #body="{ data }">
          <OnmsTag
            :value="relativeTimeSince(data.date, now) ?? '-'"
            :severity="ageSeverity(data.date, now)"
            :title="formatAbsolute(data.date)"
            data-test="heartbeat-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <!-- only a Minion that is not up can be deleted, so the others show no icon -->
            <span
              v-if="!isUp(data)"
              v-onms-tooltip.top="`Delete ${data.id}`"
              class="delete-wrap"
              data-test="delete-minion-wrap"
            >
              <OnmsIconButton
                :icon="Delete"
                severity="danger"
                :aria-label="`Delete ${data.id}`"
                data-test="delete-minion-button"
                @click="askDelete(data)"
              />
            </span>
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
  </TableCard>

  <MinionDeleteDialog
    v-model:visible="showDeleteDialog"
    :minion="minionToDelete"
  />
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'

import { OnmsAutoComplete, OnmsButton, OnmsChip, OnmsColumn, OnmsIconButton, OnmsSelectButton, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import TableCard from '@/components/Common/TableCard.vue'
import MinionDeleteDialog from '@/components/ManageMinions/MinionDeleteDialog.vue'
import { legacyUrl } from '@/lib/legacyUrl'
import { minionState, minionStateSeverity } from '@/lib/minionStatus'
import { ageSeverity, formatAbsolute, isOlderThan, relativeTimeSince } from '@/lib/relativeTime'
import { normalizeVersion, sameVersion, versionSortKey } from '@/lib/version'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { Minion } from '@/types/minionAdmin'

// `now` is the page's shared clock: the container ticks it once a second so every
// relative heartbeat on the page is computed against the same instant
const props = withDefaults(defineProps<{
  now: number
  locationFilter?: string | null
}>(), {
  locationFilter: null
})

const emit = defineEmits<{
  'update:locationFilter': [name: string | null]
  showLocation: [name: string]
  dialogOpen: [open: boolean]
}>()

const store = useMinionAdminStore()

const nodeUrl = (id?: number) => legacyUrl(`element/node.jsp?node=${id}`)

const showDeleteDialog = ref(false)
const minionToDelete = ref<Minion | null>(null)

watch(showDeleteDialog, open => emit('dialogOpen', open))

// the paginator only appears once the rows fill more than one page
const PAGE_SIZE = 25

const emptyListContent = { msg: 'No minions found.' }
const filteredOutContent = { msg: 'No minions match the current filter.' }
const errorListContent = { msg: 'Failed to load minions. Check your connection or session and reload.' }
const emptyContent = computed(() =>
  store.loadError ? errorListContent : store.minions.length ? filteredOutContent : emptyListContent)

const search = ref('')
const filters = ref({ global: { value: null as string | null, matchMode: 'contains' }})
watch(search, (value) => {
  filters.value.global.value = value || null
})

interface SearchSuggestion {
  kind: 'minion' | 'location'
  name: string
  label: string
}

const searchBox = ref<InstanceType<typeof OnmsAutoComplete> | null>(null)
const locationSuggestions = ref<SearchSuggestion[]>([])

// a chosen suggestion arrives as the object; only typed text is a search
const onSearchInput = (value: unknown) => {
  if (typeof value === 'string') {
    search.value = value
  }
}

// Minions first, then locations, as the tabs are ordered
const suggestLocations = (query: string) => {
  const needle = query.trim().toLowerCase()
  if (needle === '') {
    locationSuggestions.value = []
    return
  }
  const minions = [...new Set(store.minions.map(minion => minion.id))]
    .filter(id => id.toLowerCase().includes(needle))
    .sort((a, b) => a.localeCompare(b))
    .map(name => ({ kind: 'minion' as const, name, label: `Minion: ${name}` }))
  const locations = locationOptions.value
    .filter(name => name.toLowerCase().includes(needle))
    .map(name => ({ kind: 'location' as const, name, label: `Monitoring location: ${name}` }))
  locationSuggestions.value = [...minions, ...locations]
}

// a Minion becomes the search text, so the table shows that row; a location
// becomes the exact filter the chip shows, and the box is emptied
const chooseLocation = async (value: unknown) => {
  const suggestion = value as SearchSuggestion
  if (!suggestion?.name) {
    return
  }
  locationSuggestions.value = []
  if (suggestion.kind === 'minion') {
    search.value = suggestion.name
    await nextTick()
    searchBox.value?.clearInput()
    search.value = suggestion.name
    return
  }
  emit('update:locationFilter', suggestion.name)
  search.value = ''
  await nextTick()
  searchBox.value?.clearInput()
}

const DAY_MS = 24 * 60 * 60 * 1000

type QuickFilter = 'all' | 'downOrUnknown' | 'notSeen24h' | 'versionDiffers'
const quickFilter = ref<QuickFilter>('all')

// a location hand-off from the other tab starts from the full list of that location
watch(() => props.locationFilter, (name) => {
  if (name) {
    quickFilter.value = 'all'
  }
})

const coreVersion = computed(() => normalizeVersion(store.coreVersion))

type VersionState = 'coreUnknown' | 'unknown' | 'same' | 'differs'
const versionState = (version?: string | null): VersionState => {
  if (coreVersion.value === null) {
    return 'coreUnknown'
  }
  if (normalizeVersion(version) === null) {
    return 'unknown'
  }
  return sameVersion(version, coreVersion.value) ? 'same' : 'differs'
}

const isUp = (minion: Minion) => minionState(minion.status) === 'up'
const versionDiffers = (minion: Minion) => versionState(minion.version) === 'differs'
const versionSortField = (minion: Minion) => versionSortKey(minion.version)

const matchers: Record<QuickFilter, (minion: Minion) => boolean> = {
  all: () => true,
  downOrUnknown: minion => !isUp(minion),
  notSeen24h: minion => isOlderThan(minion.date, DAY_MS, props.now),
  versionDiffers
}

const locationScoped = computed(() =>
  props.locationFilter ? store.minions.filter(minion => minion.location === props.locationFilter) : store.minions)

const locationOptions = computed(() =>
  [...new Set(store.minions.map(minion => minion.location).filter((name): name is string => !!name))]
    .sort((a, b) => a.localeCompare(b)))

const quickFilterOptions = computed(() => {
  const count = (filter: QuickFilter) => locationScoped.value.filter(matchers[filter]).length
  return [
    { label: `All (${count('all')})`, value: 'all' },
    { label: `Down or unknown (${count('downOrUnknown')})`, value: 'downOrUnknown' },
    { label: `Not seen in 24 h (${count('notSeen24h')})`, value: 'notSeen24h' },
    { label: `Version differs from core (${count('versionDiffers')})`, value: 'versionDiffers' }
  ]
})

const visibleMinions = computed(() => locationScoped.value.filter(matchers[quickFilter.value]))

const statusTagSeverity = minionStateSeverity

// secondary while either version is unknown, so nothing is flagged on a guess
const versionTagSeverity = (version?: string | null) => {
  const state = versionState(version)
  return state === 'same' ? 'success' : state === 'differs' ? 'danger' : 'secondary'
}

// a mismatch is spelled out in the tag text, not only in its colour
const versionLabel = (version?: string | null) =>
  versionState(version) === 'differs' ? `${version} · differs from core` : version ?? '-'

const versionTitle = (version?: string | null) => {
  switch (versionState(version)) {
    case 'coreUnknown': return 'The core version could not be determined'
    case 'unknown': return 'Version format not recognised'
    case 'same': return `Matches the core version ${store.coreVersion}`
    default: return `The core runs ${store.coreVersion}`
  }
}

const askDelete = (minion: Minion) => {
  minionToDelete.value = minion
  showDeleteDialog.value = true
}
</script>

<style lang="scss" scoped>
.minions-table {
  padding: 25px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.search {
  width: 18rem;

  :deep(input) {
    width: 100%;
  }
}

.truncation-note {
  margin: 0 0 0.75rem 0;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.stale-note {
  margin: 0 0 0.75rem 0;
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
  border-radius: 4px;
  background: var(--p-yellow-50, #fffbeb);
  border: 1px solid var(--p-yellow-200, #fde68a);
  color: var(--p-yellow-800, #854d0e);
}

.link-button {
  padding: 0;
  border: 0;
  background: none;
  font: inherit;
  color: var(--p-primary-color);
  cursor: pointer;
  text-decoration: underline;
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}

.delete-wrap {
  display: inline-flex;
}
</style>
