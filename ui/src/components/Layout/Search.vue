<template>
  <FeatherAutocomplete
    v-model="searchModel"
    type="single"
    :results="results"
    label="Search..."
    class="menubar-search"
    @search="search"
    :loading="loading"
    text-prop="_text"
    @update:modelValue="selectItem"
  ></FeatherAutocomplete>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherAutocomplete } from '@featherds/autocomplete'
import { SearchResultResponse } from '@/types'

const store = useStore()
const searchStr = ref()
const pausedSearch = ref()
const loading = ref(false)
const baseHref = computed<string>(() => store.state.menuModule.mainMenu?.baseHref)
const results = ref([{}])
const searchModel = ref({})

const selectItem: any = (value: any) => {
  if (!value || !value.url) return

  const absPath = `${baseHref.value}${value.url}`
  window.location.assign(absPath)
}

const search = async (value: string) => {
  if (value || searchStr.value && !loading.value){
    loading.value = true
    await store.dispatch('searchModule/search', value)

    const searchResults : (SearchResultResponse[] | null) = store.state?.searchModule?.searchResults || null

    if (searchResults) {
      const allResults : any[] = []

      searchResults.forEach(sr => {
        const context = sr.context?.name || ''

        if (sr.results) {
          sr.results.forEach(r => {
            const text = context ? `${context}: ${r.label}` : r.label
            const obj = { _text: text, ...r }
            allResults.push(obj)
          })
        }
      })

      results.value = allResults
    }

    loading.value = false
  } else {
    pausedSearch.value = value
    searchStr.value = value
  }
}

watchEffect(() => {
  if (!loading.value && pausedSearch){
    search(pausedSearch.value)
    pausedSearch.value = ''
  }
})
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/styles/themes/variables";

.menubar-search {
  width: 250px !important;
  margin-right: 20px;
  :deep(.feather-input-border) {
    background: var($surface);
  }
  :deep(.feather-input-sub-text){
    display:none;
  }
  :deep(.feather-input-wrapper-container.raised .feather-input-label){
    display:none;
  }
}
</style>

