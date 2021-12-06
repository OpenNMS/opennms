<template>
  <FeatherAutocomplete
    v-model="searchStr"
    type="single"
    :results="results"
    label="Search"
    class="map-search"
    @search="search"
    :loading="loading"
    :hideLabel="true"
    text-prop="label"
    @update:modelValue="() => console.log('test')"
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

const selectItem = (value: { url: string }) => {
  console.log('RAN', value)
  if (!value) return
  console.log(value)
}

const search = debounce(async (value: string) => {
  console.log('RAN1', value)
  const searchVal = value || 'node'
  loading.value = true
  await store.dispatch('searchModule/search', searchVal)
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
  .feather-menu {
    z-index: 400 !important;
    .feather-menu-dropdown {
      display: block !important;
      z-index: 400;
      ul, li {
              z-index: 400 !important;
              display: block;
      }
    }
  }
}
</style>

