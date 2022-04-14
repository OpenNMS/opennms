<template>
  <FeatherAutocomplete
    class="search-bar"
    type="single"
    :results="results"
    label="Focused Nodes"
    @search="resetLabelsAndSearch"
    :loading="loading"
    text-prop="label"
    @update:modelValue="selectItem"
    :labels="labels"
  ></FeatherAutocomplete>

  <FeatherChipList condensed label="List of focus objects">
    <FeatherChip v-for="item of focusObjects" :key="item.id" @click="removeFocusObjectsByIds([item.id])">
      {{ item.label }}
      <template v-slot:icon> <FeatherIcon :icon="Close" /> </template>
    </FeatherChip>
  </FeatherChipList>
</template>
  
<script setup lang="ts">
import { debounce } from 'lodash'
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import Close from '@featherds/icon/navigation/Cancel'
import { FeatherAutocomplete } from '@featherds/autocomplete'
import { FeatherChip, FeatherChipList } from '@featherds/chips'
import { IdLabelProps } from '@/types'
import { useTopologyFocus } from './topology.composables'

const store = useStore()
const { addFocusObject, removeFocusObjectsByIds } = useTopologyFocus()
const loading = ref(false)
const defaultLabels = { noResults: 'Searching...' }
const labels = ref(defaultLabels)

// add any here to fix feather TS issue
const selectItem: any = (item: { url: string, label: string }) => {
  const label = item.label
  const id = item.url.split('=')[1]
  addFocusObject({ id, label })
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

const focusObjects = computed<IdLabelProps[]>(() => store.state.topologyModule.focusObjects)
</script>
