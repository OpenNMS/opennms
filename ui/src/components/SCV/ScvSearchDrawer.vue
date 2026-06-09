<template>
  <FeatherDrawer
    data-test="scv-drawer"
    @hidden="emit('hidden')"
    v-model="drawerOpen"
    :labels="{ close: 'close', title: 'Use an Existing Credential' }"
    width="40em"
  >
    <div class="drawer-content">
      <h3>Use an existing credential</h3>
      <div class="large-spacer"></div>
      <h4>Find existing credentials, searching by alias or key.</h4>
      <div class="large-spacer"></div>

      <FeatherInput
        :modelValue="searchValue"
        @update:modelValue="val => onSearch(val as string)"
        label="Search for credentials"
      >
        <template v-slot:post>
          <FeatherIcon :icon="SearchIcon" />
        </template>
      </FeatherInput>

      <div class="large-spacer"></div>

      <div class="results-table-container">
        <table
          class="data-table"
          aria-label="SCV Search Results Table"
        >
          <thead>
            <tr>
              <th>Alias</th>
              <th>Key</th>
            </tr>
          </thead>
          <tbody v-if="filteredResults.length > 0">
            <tr
              v-for="(item, index) of filteredResults"
              :key="`${item.alias}-${item.key}-${index}`"
              style="cursor: pointer;"
            >
              <td>
                {{ item.type === 'alias' ? item.alias : '' }}
              </td>
              <td v-if="item.type === 'key' && item.key">
                <a @click.prevent="onItemSelected(item)">{{ item.type === 'key' ? item.key : '' }}</a>
              </td>
              <td v-else></td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td
                colspan="2"
                style="text-align: center; font-style: italic;"
              >
                No results found.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="large-spacer"></div>
      <div class="scv-drawer-button-container">
        <FeatherButton
          secondary
          :disabled="credentialsLoading"
          data-test="scv-drawer-cancel-button"
          @click="drawerOpen = false"
          >Cancel</FeatherButton
        >
      </div>
    </div>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import SearchIcon from '@featherds/icon/action/Search'
import { debounce } from 'lodash'
import { useScvStore } from '@/stores/scvStore'
import { ScvSearchItem } from '@/types/scv'

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

const onSearch = debounce((query: string) => {
  credentialsLoading.value = true
  searchValue.value = query
  filteredResults.value = scvStore.queryCredentials(query)
  credentialsLoading.value = false
}, DEBOUNCE_DELAY)

const onItemSelected = (item: ScvSearchItem) => {
  emit('item-selected', item)
}

watch(() => props.isOpen, (newVal) => {
  drawerOpen.value = newVal
})
</script>

<style lang="scss" scoped>
@use "@featherds/table/scss/table" as table;
@use "@featherds/styles/mixins/typography";
@use "@featherds/styles/themes/variables";

.drawer-content {
  height: 100%;
  margin-top: 1em;
  padding: 1em 1.5em 1em 1.5em;
  overflow-y: auto;

  .large-spacer {
    min-height: 1em;
  }

  .results-table-container {
    max-height: 30em;
    overflow-y: auto;

    table.data-table {
      width: 100%;
      @include table.table;
      @include table.table-condensed;
      border: 1px solid var(variables.$border-on-surface);

      >thead {
        background-color: var(variables.$border-on-surface);
        padding: 1em;
        text-transform: uppercase;

        th {
          padding: 0.5em 1em 0.5em 1em;
          text-align: left;
        }
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        border-bottom: 1px solid var(variables.$border-on-surface);
      }
    }
  }

  .scv-drawer-button-container {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
