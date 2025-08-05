<template>
  <div
    class="onms-search-control-wrapper"
    :id="props.searchId"
  >
    <div class="onms-search-input-wrapper">
      <div class="search-icon">
        <FeatherIcon :icon="SearchIcon" />
      </div>
      <input
        ref="searchInputRef"
        type="text"
        placeholder="Search..."
        v-model="searchValue"
        @input="handleSearch"
        @keydown="onKeyDown"
        class="search-input"
      />
    </div>
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
            :class="{ 'keyboard-selected': isSelected(searchResultByContextKey, contextSearchResultsKey, searchResultItemKey) }"
            @mousedown.prevent
            @mouseenter="setSelectedIndex(searchResultByContextKey, contextSearchResultsKey, searchResultItemKey)"
          >
            <SearchResult
              :ref="(el:any) => setSearchResultRef(el, searchResultByContextKey, contextSearchResultsKey, searchResultItemKey)"
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
import { ref, computed, watch, nextTick } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import SearchIcon from '@featherds/icon/action/Search'
import SearchHeader from './SearchHeader.vue'
import SearchResult from './SearchResult.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSearchStore } from '@/stores/searchStore'

const menuStore = useMenuStore()
const searchStore = useSearchStore()
const iconClasses = ref<any>([[]])
const searchInputRef = ref<any>(null)
const searchResultRefs = ref<Map<string, any>>(new Map())

const props = defineProps({
  searchId: {
    type: String,
    required: false
  }
})

const searchValue = ref('')
const showResults = ref(false)
const selectedIndex = ref(-1)
const flatResults = ref<any[]>([])

let searchTimeout: any = null

const filteredResults = computed(() => {
  return Object.fromEntries(
    Object.entries(searchStore.searchResultsByContext).filter(
      ([, value]: any) => value?.label === 'Action'
    )
  )
})

const hasResults = computed(() => {
  return searchStore.searchResultsByContext &&
    Object.keys(searchStore.searchResultsByContext).length > 0
})

// Create a unique key for each search result
const createResultKey = (contextKey: string | number, subContextKey: string | number, itemIndex: number) => {
  return `${contextKey}-${subContextKey}-${itemIndex}`
}

// Set ref for each SearchResult component
const setSearchResultRef = (el: any, contextKey: string | number, subContextKey: string | number, itemIndex: number) => {
  if (el) {
    const key = createResultKey(contextKey, subContextKey, itemIndex)
    searchResultRefs.value.set(key, el)
  }
}

// Flatten results for easier navigation
const updateFlatResults = () => {
  const results: any[] = []
  searchResultRefs.value.clear()
  
  Object.entries(filteredResults.value).forEach(([contextKey, searchResultByContext]: any) => {
    if (searchResultByContext?.results) {
      Object.entries(searchResultByContext.results).forEach(([subContextKey, contextSearchResults]: any) => {
        if (contextSearchResults?.results) {
          contextSearchResults.results.forEach((item: any, itemIndex: number) => {
            const resultKey = createResultKey(contextKey, subContextKey, itemIndex)
            results.push({
              item,
              contextKey: String(contextKey),
              subContextKey: String(subContextKey),
              itemIndex,
              resultKey,
              flatIndex: results.length
            })
          })
        }
      })
    }
  })
  
  flatResults.value = results
}

// Watch for changes in filtered results to update flat results
watch(filteredResults, () => {
  updateFlatResults()
  selectedIndex.value = -1 // Reset selection when results change
}, { deep: true })

const handleSearch = (event: any) => {
  const stringValue = String(event.target?.value || '').trim()
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
    selectedIndex.value = -1
  }
}

const handleItemClick = (item: any) => {
  showResults.value = false
  selectedIndex.value = -1
  if (item && (item.url || item.value)) {
    const baseHref = menuStore.mainMenu?.baseHref || ''
    const itemUrl = item.url || item.value || ''
    const fullPath = `${baseHref}${itemUrl}`
    window.location.href = fullPath
  }
}

const setSelectedIndex = (contextKey: string | number, subContextKey: string | number, itemIndex: number) => {
  const foundIndex = flatResults.value.findIndex(result => 
    result.contextKey === String(contextKey) && 
    result.subContextKey === String(subContextKey) && 
    result.itemIndex === itemIndex
  )
  if (foundIndex !== -1) {
    selectedIndex.value = foundIndex
  }
}

const isSelected = (contextKey: string | number, subContextKey: string | number, itemIndex: number) => {
  if (selectedIndex.value === -1) return false
  
  const currentResult = flatResults.value[selectedIndex.value]
  return currentResult && 
         currentResult.contextKey === String(contextKey) && 
         currentResult.subContextKey === String(subContextKey) && 
         currentResult.itemIndex === itemIndex
}

const focusSelectedResult = async () => {
  if (selectedIndex.value >= 0 && selectedIndex.value < flatResults.value.length) {
    await nextTick()
    // Just update the visual state, don't actually focus the button
    // The visual feedback is handled by the CSS class
  }
}

const selectCurrentItem = () => {
  if (selectedIndex.value >= 0 && selectedIndex.value < flatResults.value.length) {
    const selectedResult = flatResults.value[selectedIndex.value]
    handleItemClick(selectedResult.item)
  }
}

const onKeyDown = async (event: KeyboardEvent) => {
  // Only handle navigation keys when dropdown is visible
  if (!showResults.value || !hasResults.value) return
  
  if (['ArrowDown', 'ArrowUp', 'Enter', 'Escape'].includes(event.key)) {
    event.preventDefault()
    
    switch (event.key) {
      case 'ArrowDown':
        if (selectedIndex.value < flatResults.value.length - 1) {
          selectedIndex.value++
        } else {
          selectedIndex.value = 0
        }
        await focusSelectedResult()
        break
        
      case 'ArrowUp':
        if (selectedIndex.value > 0) {
          selectedIndex.value--
        } else {
          selectedIndex.value = flatResults.value.length - 1
        }
        await focusSelectedResult()
        break
        
      case 'Enter':
        selectCurrentItem()
        break
        
      case 'Escape':
        showResults.value = false
        selectedIndex.value = -1
        break
    }
  }
}
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
    border-bottom: 1px solid #f1f3f4;
    transition: background-color 0.15s ease;

    &:hover {
      background-color: #f8f9fa;
    }

    &.keyboard-selected {
      background-color: #e9ecef;
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
  border: 1px solid #dee2e6;
  border-radius: 4px;
  
  .search-icon {
    position: absolute;
    left: 8px;
    z-index: 1;
    color: #6c757d;
    pointer-events: none;
  }
  
  .search-input {
    width: 100%;
    padding: 8px 12px 8px 36px;
    border: none;
    background: transparent;
    outline: none;
    font-size: 14px;
    color: black;
    
    &::placeholder {
      color: #0c0d0e;
    }
    
    &:focus {
      box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
    }
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