<template>
  <div
    class="onms-search-control-wrapper"
    :id="props.searchId"
  >
    <div class="onms-search-input-wrapper">
      <div class="search-icon">
        <OnmsIcon :icon="SearchIcon" />
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
      <div class="search-results-toolbar">
        <OnmsIcon
          :icon="CancelIcon"
          class="search-results-close"
          role="button"
          tabindex="0"
          title="Close"
          @click="closeResults"
          @keydown.enter.space.prevent="closeResults"
        />
      </div>
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
              :contextLabel="searchResultByContext?.label"
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
import { OnmsIcon } from '@opennms/onms-ui'
import SearchIcon from '@/components/icons/action/Search.vue'
import CancelIcon from '@/components/icons/navigation/Cancel.vue'
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
      () => true
      // Allow all contexts for now
      // ([, value]: any) => value?.label === 'Action'
    )
  )
})

const hasResults = computed(() => {
  return searchStore.searchResultsByContext &&
    Object.keys(searchStore.searchResultsByContext).length > 0
})

const createResultKey = (contextKey: string | number, subContextKey: string | number, itemIndex: number) => {
  return `${contextKey}-${subContextKey}-${itemIndex}`
}

const setSearchResultRef = (el: any, contextKey: string | number, subContextKey: string | number, itemIndex: number) => {
  if (el) {
    const key = createResultKey(contextKey, subContextKey, itemIndex)
    searchResultRefs.value.set(key, el)
  }
}

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
  const stringValue = String(event.target?.value || '')
  searchValue.value = stringValue

  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  const trimmedStringValue = stringValue.trim()

  if (trimmedStringValue.length > 0) {
    showResults.value = true
    searchTimeout = setTimeout(() => {
      searchStore.search(trimmedStringValue)
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
  if (selectedIndex.value === -1) {
    return false
  }

  const currentResult = flatResults.value[selectedIndex.value]

  return currentResult &&
         currentResult.contextKey === String(contextKey) &&
         currentResult.subContextKey === String(subContextKey) &&
         currentResult.itemIndex === itemIndex
}

const focusSelectedResult = async () => {
  if (selectedIndex.value >= 0 && selectedIndex.value < flatResults.value.length) {
    await nextTick()
  }
}

const selectCurrentItem = () => {
  if (selectedIndex.value >= 0 && selectedIndex.value < flatResults.value.length) {
    const selectedResult = flatResults.value[selectedIndex.value]
    handleItemClick(selectedResult.item)
  }
}

const closeResults = () => {
  showResults.value = false
  selectedIndex.value = -1
}

const onKeyDown = async (event: KeyboardEvent) => {
  if (!showResults.value || !hasResults.value) {
    return
  }

  if (['ArrowDown', 'ArrowUp', 'Enter', 'Escape'].includes(event.key)) {
    event.preventDefault()
    // prevent Escape from closing side menu
    event.stopPropagation()
    event.stopImmediatePropagation()

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
        closeResults()
        break
    }
  }
}
</script>

<style lang="scss" scoped>
.onms-search-control-wrapper {
  position: relative;
  // Fluid width: 24em on wide viewports, shrinking down to a 10em floor so
  // the menubar degrades gracefully as the window narrows (NMS-20201).
  min-width: clamp(10em, 28vw, 24em);
}

.search-results-dropdown {
  position: absolute;
  top: 100%;
  // Anchor to the input's right edge: when min-width exceeds the input's
  // width the dropdown grows leftward (over the logo area, still on-screen)
  // instead of rightward past the viewport edge, where the fixed header
  // provides no scrollbar to reach it.
  left: auto;
  right: 0;
  width: 100%;
  // Keep results readable even when the input has shrunk to its floor width.
  min-width: 24em;
  background: var(--p-content-background);
  color: var(--p-text-muted-color);
  border: 1px solid var(--p-content-border-color);
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .search-results-toolbar {
    display: flex;
    justify-content: flex-end;
    padding: 0.25em 0.5em;
    border-bottom: 1px solid var(--p-content-border-color);

    .search-results-close {
      cursor: pointer;
      color: var(--p-text-muted-color);
      font-size: 1.25rem;

      &:hover {
        color: var(--p-text-color);
      }

      &:focus-visible {
        outline: 2px solid var(--p-primary-color);
        outline-offset: 2px;
        border-radius: 2px;
      }
    }
  }

  .search-category {
    background-color: var(--p-content-background);
    padding: 0.5em 0.75em;
    border-bottom: 1px solid var(--p-content-border-color);
    font-weight: 800;
  }

  .search-result-item {
    border-bottom: 1px solid var(--p-content-border-color);
    transition: background-color 0.15s ease;
    padding-left: 0.5em;
    color: var(--p-text-muted-color);

    &:hover {
      background-color: var(--p-highlight-background);
    }

    &.keyboard-selected {
      background-color: var(--p-highlight-background);
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
  background-color: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: 4px;

  .search-icon {
    position: absolute;
    left: 8px;
    z-index: 1;
    color: var(--p-text-muted-color);
    pointer-events: none;
  }

  .search-input {
    width: 100%;
    padding: 8px 12px 8px 36px;
    border: none;
    background: transparent;
    outline: none;
    font-size: 14px;
    color: var(--p-text-color);

    &::placeholder {
      color: var(--p-text-muted-color);
    }

    &:focus {
      box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
    }
  }
}
</style>
