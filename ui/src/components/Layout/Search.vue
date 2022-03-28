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
import { FeatherAutocomplete } from '@featherds/autocomplete'

const router = useRouter()
const store = useStore()
const searchStr = ref()
const loading = ref(false)

const selectItem: any = (value: { url: string }) => {
  if (!value) return
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

const results = computed(() => {
  if (store.state.searchModule.searchResults[0]) {
    return store.state.searchModule.searchResults[0].results
  }
  return []
})
</script>
  
<style lang="scss">
@import "@featherds/styles/themes/variables";
.menubar-search {
  width: 350px !important;
  margin-right: 20px;
  .feather-input-border {
    background: var($surface);
  }
  &.feather-autocomplete-container {
    padding-top: 0px;
  }
  .feather-autocomplete-input {
    height: 25px !important;
  }
}
</style>

