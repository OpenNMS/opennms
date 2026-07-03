<template>
  <div
    class="sources-box"
    data-test="sources-box"
  >
    <div class="section-header">Sources</div>
    <div>Add or remove sources from this profile.</div>
    <div class="autocomplete-row">
      <PAutoComplete
        v-model="autocompleteQuery"
        :suggestions="sourceSearchResults"
        optionLabel="name"
        @complete="onSourceSearch"
        @option-select="onSourceSelected($event.value)"
        placeholder="Add Source"
        :forceSelection="true"
        data-test="add-source-autocomplete"
        dropdown
        completeOnFocus
        fluid
      />
    </div>
    <div class="sources-card">
      <PDataTable
        :value="sortedSources"
        scrollable
        scrollHeight="400px"
        :size="'small'"
        :virtualScrollerOptions="{ itemSize: 44 }"
        tableStyle="min-width: 50rem"
      >
        <PColumn field="name" style="width: 20%; height: 44px"></PColumn>
        <PColumn style="width: 4rem">
          <template #body="{ data }">
            <PButton
              text
              data-test="delete-source-button"
              @click="removeSource(data.name)"
            >
              <FeatherIcon :icon="Delete" />
            </PButton>
          </template>
        </PColumn>
      </PDataTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import AutoCompleteComponent from 'primevue/autocomplete'
import ButtonComponent from 'primevue/button'
import DataTableComponent from 'primevue/datatable'
import ColumnComponent from 'primevue/column'

const PAutoComplete = AutoCompleteComponent
const PButton = ButtonComponent
const PDataTable = DataTableComponent
const PColumn = ColumnComponent

interface SourceItem {
  name: string
  id: number
}

const props = defineProps<{
  sources: string[]
}>()

const emit = defineEmits<{
  'update:sources': [value: string[]]
}>()

const store = useSnmpDataCollectionStore()
const autocompleteQuery = ref<string | SourceItem>('')
const sourceSearchResults = ref<SourceItem[]>([])

const sortedSources = computed(() =>
  [...props.sources].sort((a, b) => a.localeCompare(b)).map(name => ({ name }))
)

const onSourceSearch = (event: { query: string }) => {
  const q = event.query.toLowerCase()
  sourceSearchResults.value = store.uploadedSourceNames
    .filter(s => !props.sources.includes(s.name))
    .filter(s => s.name.toLowerCase().includes(q))
    .map(s => ({ name: s.name, id: s.id }))
}

const onSourceSelected = (source: SourceItem | undefined) => {
  if (source) {
    emit('update:sources', [...props.sources, source.name])
    autocompleteQuery.value = ''
    sourceSearchResults.value = []
  }
}

const removeSource = (name: string) => {
  emit('update:sources', props.sources.filter(s => s !== name))
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.sources-box {
  padding: 20px 0;
}

.section-header {
  @include headline3;
  margin-bottom: 16px;
}

.autocomplete-row {
  max-width: 300px;
  margin-top: 16px;
}

.sources-card {
  :deep(.p-datatable-thead) {
    display: none;
  }
}
</style>
