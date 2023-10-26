<template>
  <div class="dropdown-wrapper">
    <div class="onms-search-icon-wrapper">
      <font-awesome-icon color="rgb(73, 80, 87)" class="onms-search-icon" icon="fa fa-search" />
      <FeatherInput @update:modelValue="search" label="Search..." :modelValue="searchState.currentSearch" />
    </div>
    <div class="dropdown">
      <FeatherDropdown v-model="searchState.dropdownOpen">
        <template
          v-for="searchResultByContext, searchResultByContextKey in searchStore.searchResultsByContext"
          :key="searchResultByContextKey">
          <FeatherDropdownItem v-if="searchResultByContext?.results">
            <SearchHeader>{{ searchResultByContext?.label }}</SearchHeader>
          </FeatherDropdownItem>
          <template v-for="contextSearchResults, contextSearchResultsKey in searchResultByContext?.results"
            :key="contextSearchResultsKey">
            <FeatherDropdownItem v-for="searchResultItem, searchResultItemKey in contextSearchResults?.results"
              :key="searchResultItemKey" :style="{ padding: '3px 5px' }">
              <!-- FeatherDropdownItem does not accept a class, so our extra padding has to be an inline style -->
              <SearchResult :item="searchResultItem"
                :iconClass="iconClasses?.[searchResultByContextKey]?.[searchResultItemKey]"
                :itemClicked="itemClicked" />
            </FeatherDropdownItem>
          </template>
        </template>
      </FeatherDropdown>
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { reactive } from 'vue'
import { FeatherInput } from '@featherds/input'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import SearchHeader from './SearchHeader.vue'
import SearchResult from './SearchResult.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSearchStore } from '@/stores/searchStore'
import { SearchResultItem } from '@/types'

const menuStore = useMenuStore()
const searchStore = useSearchStore()

const iconClasses = ref<string[][]>([[]])

interface SearchState {
  pausedSearch: string | number;
  currentSearch: string | number | undefined;
  dropdownOpen: boolean;
}

const searchState = reactive<SearchState>({
  pausedSearch: '',
  dropdownOpen: false,
  currentSearch: ''
})

const itemClicked = (item: SearchResultItem) => {
  if (item && item.url) {
    const baseHref = menuStore.mainMenu.baseHref
    const fullPath = `${baseHref}${item.url}`

    window.location.href = fullPath
  }
}

const loading = computed(() => searchStore.loading)

const search = (userInput: string | number | undefined) => {
  searchState.currentSearch = userInput

  if (userInput && !loading.value) {
    searchState.dropdownOpen = true
    searchStore.search('' + userInput)
  } else if (!userInput) {
    searchState.dropdownOpen = false
  } else if (userInput && loading.value) {
    searchState.pausedSearch = userInput
  } 
}

watchEffect(() => {
  if (!loading.value && searchState.pausedSearch) {
    search(searchState.pausedSearch)
    searchState.pausedSearch = ''
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
}

.dropdown-wrapper {
  position: relative;
  min-width: 250px;

  .dropdown {
    position: absolute;
    min-width: 278px;
    max-width: 278px;
    height: 0;

    :deep(.feather-dropdown) {
      padding: 0;
      border: #343a40 solid 1px;
      border-radius: 4px;
      max-height: none;
    }

    :deep(.feather-menu) {
      max-width: 278px;
      width: 100%;
    }

    :deep(.feather-menu-dropdown) {
      min-width: 100%;
      max-width: 278px;
      position: absolute !important;
      bottom: unset !important;
      left: unset !important;
      top: unset !important;
      right: unset !important;
      width: auto !important;
      transform: translateY(-20px);
    }
  }

  :deep(.feather-input-border) {
    background: var($surface);
  }

  :deep(.feather-input-sub-text) {
    display: none;
  }

  :deep(.feather-input-wrapper-container.raised .feather-input-label) {
    display: none;
  }
}

.label-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: '#e9ecef';
  color: '#495057';
  font-weight: 500;
  white-space: break-spaces;
  padding: 0 3px;

  .visible {
    display: block;
  }

  .short {
    font-size: 0.8em;
    margin-left: 3px;
  }

  span {
    opacity: 0;
  }

  &:hover {
    span {
      opacity: 1;
    }
  }
}

.onms-search-icon-wrapper {
  display:flex;
  position:relative;
  align-items:center;
  width:100%;
  .onms-search-icon {
    left:18px;
    position: absolute;
    z-index: 3;
  }
  :deep(.feather-input-container){
    width:100%;
  }
  :deep(.feather-input-label) {
    padding-left:32px;
    top:10px;
  }
  :deep(.feather-input){
    padding-left:32px;
  }
}
</style>
