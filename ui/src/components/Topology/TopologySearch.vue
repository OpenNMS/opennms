<template>
  <FeatherAutocomplete
    class="search-bar"
    :modelValue="focusedSearchBarNodes"
    type="multi"
    :results="results"
    label="Focused Nodes"
    @search="resetLabelsAndSearch"
    :loading="loading"
    text-prop="label"
    @update:modelValue="selectItem"
    :labels="labels"
  ></FeatherAutocomplete>
</template>
  
<script setup lang="ts">
import { ref, computed } from 'vue'
import { debounce } from 'lodash'
import { useStore } from 'vuex'
import { FeatherAutocomplete } from '@featherds/autocomplete'

const store = useStore()
const loading = ref(false)
const defaultLabels = { noResults: 'Searching...' }
const labels = ref(defaultLabels)

const selectItem = (items: { url: string }[]) => {
  const ids = items.map((item) => item.url.split('=')[1])
  store.dispatch('topologyModule/addFocusedNodeIds', ids)
  store.dispatch('topologyModule/setFocusedSearchBarNodes', items)
}

const resetLabelsAndSearch = (value: string) => {
  labels.value = defaultLabels
  search(value)
}

const search = debounce(async (value: string) => {
  if (!value) return
  loading.value = true
  await store.dispatch('searchModule/search', value)
  labels.value = { noResults: 'No results found' }
  loading.value = false
}, 1000)

const results = computed(() => {
  if (store.state.searchModule.searchResults[0]) {
    return store.state.searchModule.searchResults[0].results
  }
  return []
})

const focusedSearchBarNodes = computed(() => store.state.topologyModule.focusedSearchBarNodes)
</script>
