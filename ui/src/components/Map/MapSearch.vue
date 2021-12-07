<template>
  <FeatherAutocomplete
    v-model="searchStr"
    type="multi"
    :results="results"
    label="Search"
    class="map-search"
    @search="search"
    :loading="loading"
    :hideLabel="true"
    text-prop="label"
    @update:modelValue="selectItem"
  ></FeatherAutocomplete>
</template>
  
<script setup lang="ts">
import { ref, computed } from 'vue'
import { debounce } from 'lodash'
import { useStore } from 'vuex'
import { FeatherAutocomplete } from "@featherds/autocomplete"

const store = useStore()
const searchStr = ref()
const loading = ref(false)

const selectItem = (items: { label: string }[]) => {
  const nodeLabels = items.map((item) => item.label)
  store.dispatch('mapModule/setSearchedNodeLabels', nodeLabels)
}

const search = debounce(async (value: string) => {
  if (!value) return
  loading.value = true
  await store.dispatch('searchModule/search', value)
  loading.value = false
}, 1000)

const results = computed(() => {
  if (store.state.searchModule.searchResults[0]) {
    return store.state.searchModule.searchResults[0].results
  }
  return []
})
</script>
  
<style lang="scss">
.map-search {
  z-index: 1000;
  width: 290px !important;
  .feather-input-border {
    background: var(--feather-surface);
  }
  &.feather-autocomplete-container {
    padding-top: 0px;
  }
  .feather-autocomplete-input {
    height: 25px !important;
  }
}
</style>

