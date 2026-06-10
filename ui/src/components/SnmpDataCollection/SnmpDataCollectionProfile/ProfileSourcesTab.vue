<template>
  <div
    class="sources-box"
    data-test="sources-box"
  >
    <div class="section-header">Sources</div>
    <div>Add or remove sources from this profile.</div>
    <div class="autocomplete-row">
      <FeatherAutocomplete
        type="single"
        label="Add Source"
        textProp="name"
        :modelValue="selectedAutoSource"
        @update:modelValue="onSourceSelected"
        @search="onSourceSearch"
        :results="sourceSearchResults"
        data-test="add-source-autocomplete"
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
            <FeatherButton
              icon="Delete"
              data-test="delete-source-button"
              @click="removeSource(data.name)"
            >
              <FeatherIcon :icon="Delete" />
            </FeatherButton>
          </template>
        </PColumn>
      </PDataTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import DataTableComponent from 'primevue/datatable'
import ColumnComponent from 'primevue/column'

const PDataTable = DataTableComponent
const PColumn = ColumnComponent

interface SourceItem extends IAutocompleteItemType {
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
const selectedAutoSource = ref<SourceItem | undefined>(undefined)
const sourceSearchResults = ref<SourceItem[]>([])

const sortedSources = computed(() =>
  [...props.sources].sort((a, b) => a.localeCompare(b)).map(name => ({ name }))
)

const onSourceSearch = (query: string) => {
  const q = query.toLowerCase()
  sourceSearchResults.value = store.uploadedSourceNames
    .filter(s => !props.sources.includes(s.name))
    .filter(s => s.name.toLowerCase().includes(q))
    .map(s => ({ name: s.name, id: s.id }))
}

const onSourceSelected = (item: IAutocompleteItemType | IAutocompleteItemType[] | undefined) => {
  const source = item as SourceItem | undefined
  if (source && !Array.isArray(source)) {
    emit('update:sources', [...props.sources, source.name])
    selectedAutoSource.value = undefined
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

  :deep(.p-datatable-tbody > tr) {
    background-color: var(--feather-surface);
    color: var(--feather-primary-text-on-surface);
  }

  :deep(.p-datatable-tbody > tr > td) {
    border-color: var(--feather-border-on-surface);
    color: var(--feather-primary-text-on-surface);
  }
}
</style>
