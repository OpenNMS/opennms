<template>
  <div class="selection-column">
    <div class="column-header">
      <span class="column-title">{{ title }}</span>
      <span
        class="column-count"
        :data-test="`${dataTest}-count`"
      >{{ countLabel }}</span>
    </div>

    <OnmsSearchInput
      :modelValue="filterTerm"
      :placeholder="filterPlaceholder"
      :ariaLabel="`Filter ${title}`"
      :dataTest="`${dataTest}-filter`"
      @update:modelValue="onFilter"
    />

    <div class="column-actions">
      <OnmsButton
        variant="text"
        :disabled="!visibleOptions.length"
        :data-test="`${dataTest}-select-all`"
        @click="selectAllVisible"
      >Select all</OnmsButton>
      <OnmsButton
        variant="text"
        :disabled="!modelValue.length"
        :data-test="`${dataTest}-clear`"
        @click="emit('update:modelValue', [])"
      >Clear</OnmsButton>
    </div>

    <div
      v-if="loading"
      class="column-status"
      :data-test="`${dataTest}-loading`"
    >
      <OnmsSpinner size="1.75rem" />
    </div>
    <p
      v-else-if="!visibleOptions.length"
      class="column-status column-empty"
      :data-test="`${dataTest}-empty`"
    >{{ emptyMessage }}</p>
    <OnmsListbox
      v-else
      multiple
      checkmark
      :options="visibleOptions"
      :modelValue="modelValue"
      :dataKey="dataKey"
      :optionLabel="optionLabel"
      :scrollHeight="SCROLL_HEIGHT"
      :virtualScrollerOptions="{ itemSize: ROW_HEIGHT }"
      :aria-label="title"
      :data-test="`${dataTest}-list`"
      @update:modelValue="value => emit('update:modelValue', (value ?? []) as unknown[])"
    >
      <template #option="{ option }">
        <span class="option-body">
          <span class="option-primary">{{ labelOf(option) }}</span>
          <span class="option-secondary">{{ describe(option) }}</span>
        </span>
      </template>
    </OnmsListbox>
  </div>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsListbox, OnmsSearchInput, OnmsSpinner } from '@opennms/onms-ui'
import { computed, ref } from 'vue'

/**
 * One filterable, multi-select column of the ad-hoc picker. Used three times over
 * three different shapes, so options are opaque here and the parent supplies the
 * key/label/description accessors.
 *
 * Filtering is either local (`serverFilter` false — the whole option set is already
 * in memory) or delegated upward via the `filter` event, which is how the node
 * column searches server-side instead of downloading every node.
 *
 * The list itself is `OnmsListbox` in multiple mode, windowed by its virtual
 * scroller — a switch with 400 interfaces would otherwise put 400 rows in the DOM.
 * The filter field stays a separate `OnmsSearchInput` rather than the listbox's own
 * `filter` prop, both because it is the search control the rest of the app uses and
 * because the node column's filter has to reach the server.
 */
interface Props {
  title: string
  dataTest: string
  options: unknown[]
  modelValue: unknown[]
  /** Property holding each option's stable identity, for selection equality. */
  dataKey: string
  /** Property rendered as the primary line; also what the option slot echoes. */
  optionLabel: string
  keyOf: (option: unknown) => string
  labelOf: (option: unknown) => string
  descriptionOf?: (option: unknown) => string
  loading?: boolean
  emptyMessage?: string
  filterPlaceholder?: string
  serverFilter?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  descriptionOf: undefined,
  loading: false,
  emptyMessage: 'Nothing to show yet.',
  filterPlaceholder: 'Filter',
  serverFilter: false
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown[]]
  filter: [term: string]
}>()

// Virtual scrolling needs a fixed row height, so every option renders the same
// two-line block — which is why each column supplies a description even when it
// is only echoing an id.
const ROW_HEIGHT = 52
const SCROLL_HEIGHT = '22rem'

const filterTerm = ref('')

/** Secondary line for an option; optional, so columns without one get ''. */
const describe = (option: unknown): string => props.descriptionOf?.(option) ?? ''

const matchesFilter = (option: unknown): boolean => {
  const term = filterTerm.value.trim().toLowerCase()

  if (!term) {
    return true
  }

  return `${props.labelOf(option)} ${describe(option)}`.toLowerCase().includes(term)
}

const visibleOptions = computed<unknown[]>(() =>
  (props.serverFilter ? props.options : props.options.filter(matchesFilter)))

const selectedKeys = computed<Set<string>>(() => new Set(props.modelValue.map(props.keyOf)))

const countLabel = computed<string>(() => `${props.modelValue.length} of ${props.options.length} selected`)

const onFilter = (value: string | undefined) => {
  filterTerm.value = value ?? ''

  if (props.serverFilter) {
    emit('filter', filterTerm.value)
  }
}

/**
 * Adds what is currently visible to the selection rather than replacing it, so
 * "filter, select all, refine filter, select all" accumulates the way it reads.
 */
const selectAllVisible = () => {
  const additions = visibleOptions.value.filter(option => !selectedKeys.value.has(props.keyOf(option)))
  emit('update:modelValue', [...props.modelValue, ...additions])
}
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

.selection-column {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid var(--p-content-border-color);
  border-radius: var(--p-content-border-radius);
  padding: 0.75rem;
  gap: 0.5rem;
}

.column-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.5rem;

  .column-title {
    @include onms-headline4();
  }

  .column-count {
    @include onms-body-small;
    color: var(--p-text-muted-color);
    white-space: nowrap;
  }
}

.column-actions {
  display: flex;
  gap: 0.25rem;
  margin: -0.25rem 0;
}

.column-status {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 12rem;
  margin: 0;
  padding: 1rem;
  text-align: center;
}

.column-empty {
  color: var(--p-text-muted-color);
}

.option-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;

  .option-primary,
  .option-secondary {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .option-secondary {
    @include onms-body-small;
    color: var(--p-text-muted-color);
  }
}
</style>
