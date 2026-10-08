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

    <p
      v-if="note"
      class="column-note"
      :data-test="`${dataTest}-note`"
    >{{ note }}</p>

    <!--
      An error with items still listed (selected nodes survive a rule the engine
      rejects) is a note above them, not a replacement for them: the list is what
      lets the user change the selection that is still driving the graph.
    -->
    <p
      v-if="errorMessage && options.length && !loading"
      class="column-note column-error"
      :data-test="`${dataTest}-error`"
    >{{ errorMessage }}</p>

    <div class="column-actions">
      <OnmsButton
        variant="ghost"
        :disabled="!options.length"
        :data-test="`${dataTest}-select-all`"
        @click="selectAll"
      >Select all</OnmsButton>
      <OnmsButton
        variant="ghost"
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
      v-else-if="errorMessage && !options.length"
      class="column-status column-error"
      :data-test="`${dataTest}-error`"
    >{{ errorMessage }}</p>
    <p
      v-else-if="!options.length"
      class="column-status column-empty"
      :data-test="`${dataTest}-empty`"
    >{{ emptyMessage }}</p>
    <OnmsListbox
      v-else
      multiple
      checkmark
      :options="options"
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
import { computed, ref, watch } from 'vue'

/**
 * One column of the ad-hoc picker: a filter box, and the list of what it matches.
 * Used three times over three different shapes, so options are opaque here and the
 * parent supplies the key/label/description accessors.
 *
 * The column does no matching of its own. The box's text goes up through the
 * `filter` event and the parent (the store) decides what it means: a filter rule
 * for nodes, a wildcard pattern for resources and datasources. `options` is
 * whatever that produced, with any selected items pinned on top. Selecting is
 * optional: with nothing selected, everything the filter matches is in the graph,
 * which is what the count label says ("all 12 matching"); selecting narrows it.
 *
 * The list is `OnmsListbox` in multiple mode, windowed by its virtual scroller: a
 * switch with 400 interfaces would otherwise put 400 rows in the DOM.
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
  /** The box's text; the parent owns it so a restored link can put it back. */
  filterTerm: string
  /** Whether the filter is non-empty, which is what makes "no picks" mean "all". */
  filterActive?: boolean
  loading?: boolean
  emptyMessage?: string
  /** A server-side problem with the filter; replaces an empty list, sits above a non-empty one. */
  errorMessage?: string
  /** A quiet line under the box, such as "showing the first 100 of 2,310". */
  note?: string
  filterPlaceholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  descriptionOf: undefined,
  filterActive: false,
  loading: false,
  emptyMessage: 'Nothing to show yet.',
  errorMessage: '',
  note: '',
  filterPlaceholder: 'Filter'
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

// Typed text is echoed locally so the box never lags the keyboard while the
// parent decides what to do with it; the parent's value wins whenever it changes.
const filterTerm = ref(props.filterTerm)

watch(() => props.filterTerm, (value) => {
  filterTerm.value = value
})

/** Secondary line for an option; optional, so columns without one get ''. */
const describe = (option: unknown): string => props.descriptionOf?.(option) ?? ''

const selectedKeys = computed<Set<string>>(() => new Set(props.modelValue.map(props.keyOf)))

/**
 * How many of what are in the graph. "Matching" counts what the filter produced,
 * excluding pinned picks the filter no longer matches, so the number means the
 * same thing whether or not anything is picked.
 */
const countLabel = computed<string>(() => {
  const matching = props.options.filter(option => !selectedKeys.value.has(props.keyOf(option))).length +
    props.modelValue.length

  if (props.modelValue.length) {
    return `${props.modelValue.length} of ${matching} selected`
  }

  if (props.filterActive) {
    return `all ${matching} matching`
  }

  return `${matching} available`
})

const onFilter = (value: string | undefined) => {
  filterTerm.value = value ?? ''
  emit('filter', filterTerm.value)
}

/** Select everything listed, so individual items can then be deselected. */
const selectAll = () => {
  emit('update:modelValue', [...props.options])
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

.column-note {
  @include onms-body-small;
  color: var(--p-text-muted-color);
  margin: -0.25rem 0 0;
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

.column-error {
  color: var(--onms-error);
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
