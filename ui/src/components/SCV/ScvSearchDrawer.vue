<template>
  <PDrawer
    data-test="scv-drawer"
    v-model:visible="drawerOpen"
    position="right"
    header="Use an Existing Credential"
    :style="{ width: '40em' }"
    @hide="emit('hidden')"
  >
    <div class="drawer-content">
      <h3>Use an existing credential</h3>
      <div class="large-spacer"></div>
      <h4>Find existing credentials, searching by alias or key.</h4>
      <div class="large-spacer"></div>

      <FormField
        class="search-field"
        label="Search for credentials"
        for="scv-search"
      >
        <IconField>
          <PInputText
            id="scv-search"
            :modelValue="searchValue"
            @update:modelValue="val => onSearch(val as string)"
          />
          <InputIcon>
            <FeatherIcon :icon="SearchIcon" />
          </InputIcon>
        </IconField>
      </FormField>

      <div class="large-spacer"></div>

      <div class="results-table-container">
        <PDataTable
          :value="filteredResults"
          aria-label="SCV Search Results Table"
        >
          <PColumn header="Alias">
            <template #body="{ data }">
              {{ data.type === 'alias' ? data.alias : '' }}
            </template>
          </PColumn>
          <PColumn header="Key">
            <template #body="{ data }">
              <a
                v-if="data.type === 'key' && data.key"
                href="#"
                class="key-link"
                @click.prevent="onItemSelected(data)"
              >{{ data.key }}</a>
            </template>
          </PColumn>
          <template #empty>
            <div class="empty-results">No results found.</div>
          </template>
        </PDataTable>
      </div>

      <div class="large-spacer"></div>
      <div class="scv-drawer-button-container">
        <PButton
          text
          :disabled="credentialsLoading"
          data-test="scv-drawer-cancel-button"
          label="Cancel"
          @click="drawerOpen = false"
        />
      </div>
    </div>
  </PDrawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Drawer from 'primevue/drawer'
import FormField from '@/components/Common/FormField.vue'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import { FeatherIcon } from '@featherds/icon'
import SearchIcon from '@featherds/icon/action/Search'
import { debounce } from 'lodash'
import { useScvStore } from '@/stores/scvStore'
import { ScvSearchItem } from '@/types/scv'

const PButton = Button
const PColumn = Column
const PDataTable = DataTable
const PDrawer = Drawer
const PInputText = InputText

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'hidden'): void
  (e: 'item-selected', item: ScvSearchItem): void
}>()

const DEBOUNCE_DELAY = 300
const scvStore = useScvStore()
const drawerOpen = ref(props.isOpen)
const credentialsLoading = ref(false)
const filteredResults = ref<ScvSearchItem[]>([])
const searchValue = ref<string>('')

const runQuery = (query: string) => {
  credentialsLoading.value = true
  searchValue.value = query
  filteredResults.value = scvStore.queryCredentials(query)
  credentialsLoading.value = false
}

const onSearch = debounce((query: string) => {
  runQuery(query)
}, DEBOUNCE_DELAY)

const onItemSelected = (item: ScvSearchItem) => {
  emit('item-selected', item)
}

watch(() => props.isOpen, (newVal) => {
  drawerOpen.value = newVal

  if (newVal) {
    // When the drawer opens, show all current results (empty search => all
    // credentials) so the user sees existing entries without having to type first.
    runQuery(searchValue.value)
  } else {
    // Reset the search on close so the next open starts fresh; otherwise the
    // drawer (kept mounted by the parent) would reopen showing the prior query.
    searchValue.value = ''
  }
})
</script>

<style lang="scss" scoped>
// Let the PrimeVue drawer body (.p-drawer-content) be the only scroll
// container — no nested height/overflow constraints, so the whole drawer
// scrolls as one.
.drawer-content {
  margin-top: 1em;

  .large-spacer {
    min-height: 1em;
  }

  .search-field {
    // vertical spacing above the search field
    margin-top: 1em;

    :deep(.p-inputtext),
    :deep(.p-iconfield) {
      width: 100%;
    }
  }

  .key-link {
    cursor: pointer;
    color: var(--p-primary-color);
  }

  .empty-results {
    text-align: center;
    font-style: italic;
  }

  .scv-drawer-button-container {
    display: flex;
    justify-content: flex-start;
  }
}
</style>
