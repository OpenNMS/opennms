<template>
  <FeatherAutocomplete
    v-model="searchStr"
    type="single"
    :results="results"
    label="Search"
    class="menubar-search"
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
import { useRouter } from 'vue-router'
import { FeatherAutocomplete } from "@featherds/autocomplete"

const router = useRouter()
const store = useStore()

// data
const searchStr = ref()
const loading = ref(false)

// methods
const selectItem = (value: { url: string }) => {
  // parse selected item url and redirect
  const path = value.url.split('?')[1].split('=')
  router.push(`/${path[0]}/${path[1]}`)
}
const search = debounce(async (value: string) => {
  const searchVal = value || 'node'
  loading.value = true
  await store.dispatch('searchModule/search', searchVal)
  loading.value = false
}, 600)

// computed
const results = computed(() => {
  if (store.state.searchModule.searchResults[0]) {
    return store.state.searchModule.searchResults[0].results
  }
  return []
})
</script>
  
<style lang="scss">
.menubar-search {
  width: 400px !important;
  margin-right: 20px;
  .feather-input-border {
    background: var(--feather-surface) !important;
  }
  &.feather-autocomplete-container {
    padding-top: 0px !important;
  }
}
</style>

