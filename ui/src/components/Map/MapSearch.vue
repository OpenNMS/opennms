<template>
  <div class="map-search">
    <OnmsIcon
      :icon="IconSearch"
      class="map-search__icon"
    />
    <OnmsAutoComplete
      ref="autoCompleteRef"
      v-model="searchStr"
      multiple
      :suggestions="results"
      optionLabel="label"
      class="map-search__input"
      aria-label="Search"
      placeholder="Search"
      @complete="resetLabelsAndSearch"
      @update:modelValue="selectItem"
    >
      <template #empty>
        <div class="autocomplete-empty">{{ labels.noResults }}</div>
      </template>
    </OnmsAutoComplete>
    <OnmsIcon
      :icon="IconCancel"
      class="map-search__clear"
      role="button"
      tabindex="0"
      title="Clear search"
      @click="clearSearch"
      @keydown.enter.space.prevent="clearSearch"
    />
  </div>
</template>

<script
  setup
  lang="ts"
>
import { computed, ref, watch, watchEffect } from 'vue'

import { debounce } from 'lodash'
import { OnmsAutoComplete, OnmsIcon } from '@opennms/onms-ui'
import IconCancel from '@opennms/onms-ui/icons/navigation/Cancel.vue'
import IconSearch from '@opennms/onms-ui/icons/action/Search.vue'
import { useMapStore } from '@/stores/mapStore'
import { useSearchStore } from '@/stores/searchStore'

const emit = defineEmits(['fly-to-node', 'set-bounding-box'])

const mapStore = useMapStore()
const searchStore = useSearchStore()
const autoCompleteRef = ref<InstanceType<typeof OnmsAutoComplete> | null>(null)
const searchStr = ref()
const loading = ref(false)
const outsideSearch = ref(false)
const defaultLabels = { noResults: 'Searching...' }
const labels = ref(defaultLabels)

const selectItem: any = (items: { label: string }[]) => {
  const nodeLabels = items.map(item => item.label)
  mapStore.setSearchedNodeLabels(nodeLabels)

  if (nodeLabels.length) {
    if (nodeLabels.length === 1) {
      // fly to last selected node
      emit('fly-to-node', nodeLabels[0])
    } else {
      // set bounding box for all searched nodes
      emit('set-bounding-box', nodeLabels)
    }
  }
}

const resetLabelsAndSearch = (value: string) => {
  labels.value = defaultLabels
  search(value)
}

const search = debounce(async (value: string) => {
  if (!value) {
    return
  }

  loading.value = true

  await searchStore.search(value)

  labels.value = { noResults: 'No results found' }
  loading.value = false
}, 1000)

/**
 * Resets the field and everything a search left behind: the selected chips, the
 * map's node filter, and the text typed but not yet selected (which lives in the
 * DOM, not the model — see OnmsAutoComplete.clearInput).
 */
const clearSearch = () => {
  search.cancel()
  autoCompleteRef.value?.clearInput()
  searchStr.value = []
  labels.value = defaultLabels
  outsideSearch.value = false
  mapStore.setSearchedNodeLabels([])
  // Also clear the term an outside component (MapNodesGrid) may have set:
  // leaving it in place would stop a repeat click on that same node from
  // re-running the search, since the watchEffect below only reacts to changes.
  mapStore.setNodeSearchTerm('')
}

const results = computed(() => {
  if (searchStore.searchResults.length > 0 && searchStore.searchResults[0]) {
    return searchStore.searchResults[0].results
  }

  return []
})

// search term set by an outside component rather than from user text input, i.e. from MapNodesGrid
const nodeSearchTerm = computed<string>(() => mapStore.nodeSearchTerm)

// when results are updated as a result of a search initiated from an outside component,
// select the item (which may also perform the fly-to-node behavior)
const selectItemFromOutsideSearch = (searchResults: any) => {
  if (outsideSearch.value) {
    outsideSearch.value = false
    const label = searchResults?.[0].label

    if (label) {
      selectItem([{ label }])
    }
  }
}

// when an outside component modifies nodeSearchTerm, launch a search with this term,
// similar to 'search()' above
// search term may be a node label or else an expression (e.g. 'nodeid == 10')
// when results are received and outsideSearch is true, this will trigger watch(results)
watchEffect(async () => {
  if (nodeSearchTerm.value) {
    labels.value = defaultLabels
    searchStr.value = [{ label: nodeSearchTerm.value }]

    loading.value = true
    outsideSearch.value = true
    await searchStore.search(nodeSearchTerm.value)
    labels.value = { noResults: 'No results found' }
    loading.value = false
  }
})

watch(results, (newResults) => {
  selectItemFromOutsideSearch(newResults)
})
</script>

<style
  lang="scss"
  scoped
>
.map-search {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  /* Translucent panel + padding, matching the Show Severity control; follows
     the theme (light in light mode, dark in dark mode). */
  padding: 0.5em;
  background-color: rgba(211, 211, 211, 0.8);
  border-radius: 4px;
}
.map-search__icon,
.map-search__clear {
  flex: 0 0 auto;
  color: var(--p-text-color);
  font-size: 1.1rem;
}
.map-search__clear {
  cursor: pointer;

  &:hover {
    color: var(--p-primary-color);
  }

  &:focus-visible {
    outline: 2px solid var(--p-primary-color);
    outline-offset: 2px;
    border-radius: 2px;
  }
}
.map-search__input {
  width: 290px !important;
}
.autocomplete-empty {
  padding: 0.5rem 0.75rem;
}
</style>

<style lang="scss">
/* Dark-mode panel tint, matching the Show Severity control. Kept in a separate
   unscoped block because .open-dark lives on <html>, outside this component's
   scope; html.open-dark raises specificity above the scoped base .map-search
   rule so it reliably wins. */
html.open-dark .map-search {
  background-color: rgba(30, 30, 40, 0.8);
}
</style>
