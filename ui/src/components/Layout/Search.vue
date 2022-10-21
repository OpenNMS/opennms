<template>
  <FeatherAutocomplete
    v-model="searchStr"
    type="single"
    :results="results"
    label="Search..."
    class="menubar-search"
    @search="search"
    :loading="loading"
    text-prop="label"
    @update:modelValue="selectItem"
  ></FeatherAutocomplete>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherAutocomplete } from '@featherds/autocomplete'

const router = useRouter()
const store = useStore()
const searchStr = ref()
const pausedSearch = ref()
const loading = ref(false)

const selectItem: any = (value: { url: string }) => {
  if (!value) return
  // parse selected item url and redirect
  const path = value.url.split('?')[1].split('=')
  router.push(`/${path[0]}/${path[1]}`)
}


const search = async (value: string) => {
  if (value || searchStr.value && !loading.value){
    loading.value = true
    await store.dispatch('searchModule/search', value)
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

const results = computed(() => {
  return store.state?.searchModule?.searchResults?.[0]?.results || null
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

