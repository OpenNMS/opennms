<template>
  <div
    class="onms-search-control-wrapper"
    :id="props.searchId"
  >
    <div class="onms-search-input-wrapper">
      <FeatherInput
        ref="searchInputRef"
        label="Search..."
        @update:modelValue="handleSearch"
        :modelValue="searchValue"
        @keydown="onKeyDown"
      >
        <template v-slot:pre>
          <FeatherIcon :icon="SearchIcon" />
        </template>
      </FeatherInput>
    </div>
    <!-- Replace FeatherDropdown with simple div -->
    <div
      v-if="showResults && hasResults"
      class="search-results-dropdown"
      @mousedown.prevent
    >
      <template
        v-for="(searchResultByContext, searchResultByContextKey) in filteredResults"
        :key="searchResultByContextKey"
      >
        <div
          v-if="searchResultByContext?.results"
          class="search-category"
        >
          <SearchHeader>{{ searchResultByContext?.label }}</SearchHeader>
        </div>
        <template
          v-for="contextSearchResults, contextSearchResultsKey in searchResultByContext?.results"
          :key="contextSearchResultsKey"
        >
          <div
            v-for="searchResultItem, searchResultItemKey in contextSearchResults?.results"
            :key="searchResultItemKey"
            class="search-result-item"
            @click="handleItemClick(searchResultItem)"
            @mousedown.prevent
          >
            <SearchResult
              :item="searchResultItem"
              :iconClass="iconClasses?.[searchResultByContextKey]?.[searchResultItemKey]"
              :itemClicked="handleItemClick"
            />
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import SearchIcon from '@featherds/icon/action/Search'
import { FeatherInput } from '@featherds/input'
import SearchHeader from './SearchHeader.vue'
import SearchResult from './SearchResult.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSearchStore } from '@/stores/searchStore'

const menuStore = useMenuStore()
const searchStore = useSearchStore()
const iconClasses = ref<any>([[]])
const searchInputRef = ref<any>(null)

const props = defineProps({
  searchId: {
    type: String,
    required: false
  }
})

const searchValue = ref('')
const showResults = ref(false)

let searchTimeout: any = null

const hasResults = computed(() => {
  return searchStore.searchResultsByContext &&
    Object.keys(searchStore.searchResultsByContext).length > 0
})

const handleSearch = (value: any) => {
  const stringValue = String(value || '').trim()
  searchValue.value = stringValue
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  if (stringValue.length > 0) {
    showResults.value = true
    searchTimeout = setTimeout(() => {
      searchStore.search(stringValue)
    }, 300)
  } else {
    showResults.value = false
  }
}

const handleItemClick = (item: any) => {
  showResults.value = false
  if (item && (item.url || item.value)) {
    const baseHref = menuStore.mainMenu?.baseHref || ''
    const itemUrl = item.url || item.value || ''
    const fullPath = `${baseHref}${itemUrl}`
    window.location.href = fullPath
  }
}

const onKeyDown = (event: KeyboardEvent) => {
  if (['ArrowDown', 'ArrowUp', 'Enter', 'Escape'].includes(event.key)) {
    if (event.key === 'Escape') {
      showResults.value = false
    }
  }
}

const filteredResults = computed(() => {
  return Object.fromEntries(
    Object.entries(searchStore.searchResultsByContext).filter(
      ([, value]: any) => value?.label === 'Action'
    )
  )
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/themes/variables";

.onms-search-control-wrapper {
  position: relative;
  min-width: 30em;
}

.search-results-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: var($surface);
  border: 1px solid var($secondary);
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);


  .search-category {
    background-color: #f8f9fa;
    padding: 8px 12px;
    border-bottom: 1px solid #dee2e6;
    font-weight: 500;
  }

  .search-result-item {
    padding: 8px 12px;
    cursor: pointer;
    border-bottom: 1px solid #f1f3f4;

    &:hover {
      background-color: #f8f9fa;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}

.onms-search-input-wrapper {
  display: flex;
  position: relative;
  align-items: center;
  width: 100%;
  background-color: #f8f9fa;

  :deep(.feather-input-container) {
    width: 100%;
  }

  :deep(.feather-input-label) {
    padding-left: 32px;
    top: 10px;
  }

  :deep(.feather-input) {
    padding-left: 32px;
  }
}

:deep(.feather-input-wrapper-container .feather-input-border .pre-border) {
  border-radius: 0 !important;
}

:deep(.feather-input-wrapper-container .feather-input-border .post-border) {
  border-radius: 0 !important;
}

:deep(.feather-input-border) {
  background: var(--surface);
}

:deep(.feather-input-sub-text) {
  display: none !important;
}

:deep(.feather-input-wrapper-container.raised .feather-input-label) {
  display: none !important;
}
</style>

