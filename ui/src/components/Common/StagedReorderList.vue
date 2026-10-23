<template>
  <div class="staged-reorder">
    <p class="intro">{{ intro }}</p>

    <div class="filter-row">
      <FormField class="filter-field">
        <OnmsSearchInput
          :input-id="filterId"
          :modelValue="filterTerm"
          @update:modelValue="onFilterChange"
          data-test="reorder-filter"
          :placeholder="filterPlaceholder"
          :aria-label="`Filter ${itemNoun}s`"
        />
      </FormField>
      <div class="filter-meta">
        <span data-test="filter-count">Showing {{ renderedItems.length }} of {{ workingItems.length }} {{ itemNoun }}s</span>
        <OnmsButton
          v-if="isFiltering && visibleItems.length"
          variant="text"
          size="small"
          data-test="select-all-shown"
          @click="selectAllShown"
        >Select all shown</OnmsButton>
      </div>
    </div>

    <Draggable
      :modelValue="renderedItems"
      @update:modelValue="onVisibleReorder"
      @start="onDragStart"
      item-key="id"
      filter="button, input, a, .p-checkbox"
      :prevent-on-filter="false"
      class="reorder-drag-container"
      data-test="reorder-list"
    >
      <template #item="{ element }">
        <div
          class="reorder-row"
          :class="{ selected: isSelected(element.id) }"
          :data-test="`reorder-row-${element.id}`"
        >
          <OnmsCheckbox
            :input-id="`select-${listId}-${element.id}`"
            :modelValue="isSelected(element.id)"
            @update:modelValue="toggleSelected(element.id)"
            :aria-label="`Select ${itemLabel(element)}`"
            data-test="row-checkbox"
          />
          <span
            class="rank-badge"
            :class="{ changed: hasMoved(element.id) }"
            data-test="rank-badge"
          >{{ rankOf(element.id) }}</span>
          <OnmsIcon
            class="drag-hint"
            :icon="Apps"
            v-onms-tooltip="'Drag anywhere on the row to reorder'"
            aria-hidden="true"
            focusable="false"
          />
          <slot
            name="item"
            :item="element"
          />
          <OnmsIconButton
            :title="`Move ${itemLabel(element)} up`"
            data-test="move-up-button"
            :icon="ArrowUp"
            :disabled="rankOf(element.id) === 1"
            @click="moveStep(element, -1)"
          />
          <OnmsIconButton
            :title="`Move ${itemLabel(element)} down`"
            data-test="move-down-button"
            :icon="ArrowDown"
            :disabled="rankOf(element.id) === workingItems.length"
            @click="moveStep(element, 1)"
          />
          <OnmsIconButton
            aria-haspopup="true"
            :aria-controls="`reorder-row-menu-${listId}`"
            :title="`More actions for ${itemLabel(element)}`"
            data-test="row-menu-button"
            :icon="MenuIcon"
            @click="toggleRowMenu($event, element)"
          />
        </div>
      </template>
    </Draggable>

    <div
      v-if="hiddenRenderCount > 0"
      class="show-more-row"
      data-test="show-more-row"
    >
      <span>{{ hiddenRenderCount }} more {{ itemNoun }}s below &mdash; filter to jump to one, or</span>
      <OnmsButton
        variant="text"
        size="small"
        data-test="show-more-button"
        @click="renderLimit += renderChunkSize"
      >Show {{ Math.min(renderChunkSize, hiddenRenderCount) }} more</OnmsButton>
    </div>

    <slot name="pinned" />

    <div class="sticky-actions">
    <div
      v-if="selectedIdList.length"
      class="selection-bar"
      data-test="selection-bar"
    >
      <span class="selection-count">{{ selectedIdList.length }} selected</span>
      <OnmsButton
        variant="outlined"
        size="small"
        data-test="selection-top"
        @click="moveBlockEdge(selectedIdList, true)"
      >Move to Top</OnmsButton>
      <OnmsButton
        variant="outlined"
        size="small"
        data-test="selection-bottom"
        @click="moveBlockEdge(selectedIdList, false)"
      >Move to Bottom</OnmsButton>
      <OnmsButton
        variant="outlined"
        size="small"
        data-test="selection-position"
        @click="openMoveDialog('position', selectedIdList)"
      >Insert at Position&hellip;</OnmsButton>
      <OnmsButton
        variant="outlined"
        size="small"
        data-test="selection-above"
        @click="openMoveDialog('above', selectedIdList)"
      >Move Above {{ capitalNoun }}&hellip;</OnmsButton>
      <OnmsButton
        variant="text"
        size="small"
        data-test="selection-clear"
        @click="clearSelection"
      >Clear</OnmsButton>
    </div>

    <div class="button-row">
      <span
        class="moved-count"
        data-test="moved-count"
      >{{ movedCount === 0 ? 'No changes yet' : `${movedCount} ${itemNoun}s moved` }}</span>
      <OnmsButton
        :disabled="!isDirty || saving"
        :loading="saving"
        data-test="save-order-button"
        @click="saveOrder"
      >Save Order</OnmsButton>
      <OnmsButton
        variant="outlined"
        :disabled="!isDirty"
        data-test="reset-button"
        @click="resetOrder"
      >Reset</OnmsButton>
      <OnmsButton
        variant="outlined"
        data-test="cancel-button"
        @click="requestClose"
      >Cancel</OnmsButton>
    </div>
    </div>

    <span
      class="visually-hidden"
      aria-live="polite"
      data-test="move-announcement"
    >{{ announcement }}</span>

    <OnmsMenu
      :id="`reorder-row-menu-${listId}`"
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <OnmsConfirmationDialog
      :visible="moveDialogState.visible"
      :title="moveDialogState.mode === 'position' ? 'Move to Position' : `Move Above ${capitalNoun}`"
      actionButtonText="Move"
      cancelButtonText="Cancel"
      @ok="applyMoveDialog"
      @cancel="closeMoveDialog"
    >
      <template #content>
        <div
          v-if="moveDialogState.mode === 'position'"
          class="move-dialog-content"
        >
          <FormField
            label="Position"
            :for="positionInputId"
            :hint="positionHint"
          >
            <OnmsInputNumber
              v-model="positionValue"
              :input-id="positionInputId"
              :min="1"
              :max="workingItems.length"
              showButtons
              data-test="position-input"
            />
          </FormField>
        </div>
        <div
          v-else
          class="move-dialog-content"
        >
          <FormField
            label="Place directly above"
            :for="aboveInputId"
          >
            <OnmsAutoComplete
              v-model="aboveTarget"
              :input-id="aboveInputId"
              :suggestions="aboveSuggestions"
              optionLabel="name"
              :placeholder="`Search ${itemNoun}s...`"
              :forceSelection="true"
              dropdown
              completeOnFocus
              fluid
              data-test="above-autocomplete"
              @complete="onAboveSearch"
            />
          </FormField>
        </div>
      </template>
    </OnmsConfirmationDialog>

    <OnmsConfirmationDialog
      :visible="discardConfirmVisible"
      title="Discard order changes?"
      actionButtonText="Discard"
      cancelButtonText="Keep Editing"
      @ok="discardAndClose"
      @cancel="keepEditing"
    >
      <template #content>
        <p>The new {{ itemNoun }} order has not been saved. Leaving discards it.</p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script lang="ts" setup generic="T extends { id: number }">
import { computed, ref, useId, watch } from 'vue'

import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import { EventConfigMutationResult } from '@/types/eventConfig'
import {
  OnmsAutoComplete,
  OnmsButton,
  OnmsCheckbox,
  OnmsConfirmationDialog,
  OnmsIcon,
  OnmsIconButton,
  OnmsInputNumber,
  OnmsMenu,
  OnmsMenuItem,
  OnmsSearchInput
} from '@opennms/onms-ui'
import KeyboardArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import KeyboardArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'
import Apps from '@opennms/onms-ui/icons/navigation/Apps.vue'
import MenuIcon from '@opennms/onms-ui/icons/navigation/MoreHoriz.vue'
import Draggable from 'vuedraggable'

const ArrowUp = KeyboardArrowUp
const ArrowDown = KeyboardArrowDown

const props = defineProps<{
  /** The items in evaluation order (first entry evaluated first) */
  items: T[]
  itemLabel: (item: T) => string
  itemSearchText: (item: T) => string
  /** Lower-case singular noun for copy, e.g. 'source' or 'event' */
  itemNoun: string
  intro: string
  filterPlaceholder: string
  saving: boolean
  /** Sends the complete order; the component rebases or closes on the outcome */
  save: (ids: number[]) => Promise<EventConfigMutationResult>
  /** Re-fetches `items` after the server rejected the list as out of date */
  refetch: () => Promise<unknown>
  /** How many rows render at once; "Show more" extends by this much (default 250) */
  renderChunk?: number
}>()

const emit = defineEmits<{
  close: []
}>()

const { showSnackBar } = useSnackbar()
const listId = useId()
const filterId = useId()
const positionInputId = useId()
const aboveInputId = useId()

const capitalNoun = computed(() => props.itemNoun.charAt(0).toUpperCase() + props.itemNoun.slice(1))

// The working copy every mechanism (drag, arrows, menu moves, block moves) rearranges;
// Save sends it as one complete order.
const workingItems = ref<T[]>([]) as import('vue').Ref<T[]>
const originalIds = ref<number[]>([])
const filterTerm = ref('')
const selectedIds = ref<Record<number, boolean>>({})
const announcement = ref('')
const discardConfirmVisible = ref(false)

const rowMenu = ref()
const rowMenuTarget = ref<T | null>(null) as import('vue').Ref<T | null>

const moveDialogState = ref<{
  visible: boolean
  mode: 'position' | 'above'
  ids: number[]
  currentRank: number
}>({ visible: false, mode: 'position', ids: [], currentRank: 1 })
const positionValue = ref<number>(1)
const aboveTarget = ref<{ id: number; name: string } | string | null>(null)
const aboveSuggestions = ref<{ id: number; name: string }[]>([])

const renderChunkSize = computed(() => props.renderChunk ?? 250)
const renderLimit = ref(250)

const initWorkingCopy = () => {
  workingItems.value = [...props.items]
  originalIds.value = props.items.map(item => item.id)
  selectedIds.value = {}
  filterTerm.value = ''
  announcement.value = ''
  renderLimit.value = renderChunkSize.value
}

watch(() => props.items, initWorkingCopy, { immediate: true })

const isDirty = computed(() => workingItems.value.map(item => item.id).join(',') !== originalIds.value.join(','))
const movedCount = computed(() =>
  workingItems.value.filter((item, index) => originalIds.value[index] !== item.id).length)

// Every row reads its rank several times per render, so ranks come from a map rebuilt
// once per move rather than a linear scan per read.
const rankById = computed(() => {
  const map = new Map<number, number>()
  workingItems.value.forEach((item, index) => map.set(item.id, index + 1))
  return map
})
const rankOf = (id: number) => rankById.value.get(id) ?? 0
const hasMoved = (id: number) => originalIds.value[rankOf(id) - 1] !== id

const terms = computed(() =>
  filterTerm.value.toLowerCase().split(/[\s,]+/).filter(term => term.length > 0))
const isFiltering = computed(() => terms.value.length > 0)

const matchesFilter = (item: T) => {
  if (!terms.value.length) {
    return true
  }
  const haystack = props.itemSearchText(item).toLowerCase()
  return terms.value.some(term => haystack.includes(term))
}

const visibleItems = computed(() => workingItems.value.filter(matchesFilter))

// Only a window of the matches is rendered: with thousands of rows, re-rendering
// them all made every move take seconds.
const renderedItems = computed(() => visibleItems.value.slice(0, renderLimit.value))
const hiddenRenderCount = computed(() => visibleItems.value.length - renderedItems.value.length)

const onFilterChange = (value: string | undefined) => {
  filterTerm.value = value ?? ''
  renderLimit.value = renderChunkSize.value
}

const announceMove = (item: T) => {
  announcement.value = `Moved ${props.itemLabel(item)} to position ${rankOf(item.id)}`
}

/**
 * A drag rearranged the visible (possibly filtered) list. Translate the single moved row back
 * onto the full order: it lands immediately above the visible row now below it (or right after
 * the visible row above it when dropped last), so hidden rows keep their relative order.
 */
// The rearranged visible array alone is ambiguous ([1,2,4] -> [1,4,2] reads as "4 up" or
// "2 down", which land differently in the full order), so the grabbed row is captured here.
const draggedId = ref<number | null>(null)
const onDragStart = (event: { oldIndex: number }) => {
  draggedId.value = renderedItems.value[event.oldIndex]?.id ?? null
}

const onVisibleReorder = (newVisible: T[]) => {
  const oldVisible = renderedItems.value
  const grabbedId = draggedId.value
  draggedId.value = null
  if (newVisible.length !== oldVisible.length) {
    return
  }
  let firstDiff = 0
  while (firstDiff < oldVisible.length && oldVisible[firstDiff].id === newVisible[firstDiff].id) {
    firstDiff++
  }
  if (firstDiff === oldVisible.length) {
    return
  }
  let moved: T
  let movedIndex: number
  const grabbed = grabbedId != null ? newVisible.find(item => item.id === grabbedId) : undefined
  if (grabbed) {
    moved = grabbed
    movedIndex = newVisible.findIndex(item => item.id === grabbed.id)
  } else if (firstDiff + 1 < oldVisible.length && oldVisible[firstDiff + 1].id === newVisible[firstDiff].id) {
    // the row at firstDiff moved down; everything below shifted up into its place
    moved = oldVisible[firstDiff]
    movedIndex = newVisible.findIndex(item => item.id === moved.id)
  } else {
    moved = newVisible[firstDiff]
    movedIndex = firstDiff
  }
  const successor = newVisible[movedIndex + 1]
  const predecessor = newVisible[movedIndex - 1]
  const rest = workingItems.value.filter(item => item.id !== moved.id)
  let insertAt: number
  if (successor) {
    insertAt = rest.findIndex(item => item.id === successor.id)
  } else if (predecessor) {
    insertAt = rest.findIndex(item => item.id === predecessor.id) + 1
  } else {
    return
  }
  rest.splice(insertAt, 0, moved)
  workingItems.value = rest
  announceMove(moved)
}

const moveToIndex = (item: T, index: number) => {
  const rest = workingItems.value.filter(entry => entry.id !== item.id)
  const clamped = Math.max(0, Math.min(index, rest.length))
  rest.splice(clamped, 0, item)
  workingItems.value = rest
  announceMove(item)
}

const moveStep = (item: T, delta: number) => {
  const index = rankOf(item.id) - 1
  const target = index + delta
  if (target < 0 || target >= workingItems.value.length) {
    return
  }
  moveToIndex(item, target)
}

const moveBlockEdge = (ids: number[], top: boolean) => {
  const inSelection = new Set(ids)
  const selection = workingItems.value.filter(item => inSelection.has(item.id))
  const rest = workingItems.value.filter(item => !inSelection.has(item.id))
  workingItems.value = top ? [...selection, ...rest] : [...rest, ...selection]
  announcement.value = `Moved ${selection.length} ${props.itemNoun}s to the ${top ? 'top' : 'bottom'}`
}

const moveBlockToIndex = (ids: number[], index: number) => {
  const inSelection = new Set(ids)
  const selection = workingItems.value.filter(item => inSelection.has(item.id))
  const rest = workingItems.value.filter(item => !inSelection.has(item.id))
  const clamped = Math.max(0, Math.min(index, rest.length))
  rest.splice(clamped, 0, ...selection)
  workingItems.value = rest
  announcement.value = `Moved ${selection.length} ${props.itemNoun}s to positions ${clamped + 1}+`
}

const moveBlockAbove = (ids: number[], targetId: number) => {
  const inSelection = new Set(ids)
  if (inSelection.has(targetId)) {
    return
  }
  const selection = workingItems.value.filter(item => inSelection.has(item.id))
  const rest = workingItems.value.filter(item => !inSelection.has(item.id))
  const insertAt = rest.findIndex(item => item.id === targetId)
  if (insertAt < 0) {
    return
  }
  rest.splice(insertAt, 0, ...selection)
  workingItems.value = rest
  announcement.value = `Moved ${selection.length === 1 ? props.itemLabel(selection[0]) : `${selection.length} ${props.itemNoun}s`} above ${props.itemLabel(rest[insertAt + selection.length])}`
}

const isSelected = (id: number) => !!selectedIds.value[id]
const toggleSelected = (id: number) => {
  selectedIds.value = { ...selectedIds.value, [id]: !selectedIds.value[id] }
}
const clearSelection = () => {
  selectedIds.value = {}
}
const selectAllShown = () => {
  const next = { ...selectedIds.value }
  renderedItems.value.forEach((item) => {
    next[item.id] = true
  })
  selectedIds.value = next
}
const selectedIdList = computed(() =>
  workingItems.value.filter(item => selectedIds.value[item.id]).map(item => item.id))

const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    {
      label: 'Move to Top',
      command: () => moveToIndex(target, 0)
    },
    {
      label: 'Move to Bottom',
      command: () => moveToIndex(target, workingItems.value.length - 1)
    },
    {
      label: 'Move to Position...',
      command: () => openMoveDialog('position', [target.id])
    },
    {
      label: `Move Above ${capitalNoun.value}...`,
      command: () => openMoveDialog('above', [target.id])
    }
  ]
})

const toggleRowMenu = (event: Event, item: T) => {
  rowMenuTarget.value = item
  rowMenu.value?.toggle(event)
}

const openMoveDialog = (mode: 'position' | 'above', ids: number[]) => {
  moveDialogState.value = {
    visible: true,
    mode,
    ids,
    currentRank: ids.length === 1 ? rankOf(ids[0]) : 1
  }
  positionValue.value = ids.length === 1 ? rankOf(ids[0]) : 1
  aboveTarget.value = null
  aboveSuggestions.value = []
}

const positionHint = computed(() => {
  const { ids, currentRank } = moveDialogState.value
  if (ids.length > 1) {
    return `1 = evaluated first · the ${ids.length} selected ${props.itemNoun}s are inserted as one block`
  }
  return `1 = evaluated first · currently at position ${currentRank} of ${workingItems.value.length}`
})

const closeMoveDialog = () => {
  moveDialogState.value = { ...moveDialogState.value, visible: false }
}

const onAboveSearch = (query: string) => {
  const needle = (query ?? '').toLowerCase()
  const excluded = new Set(moveDialogState.value.ids)
  aboveSuggestions.value = workingItems.value
    .filter(item => !excluded.has(item.id))
    .filter(item => !needle || props.itemLabel(item).toLowerCase().includes(needle))
    .map(item => ({ id: item.id, name: props.itemLabel(item) }))
}

const applyMoveDialog = () => {
  const { mode, ids } = moveDialogState.value
  if (mode === 'position') {
    if (positionValue.value) {
      if (ids.length === 1) {
        const item = workingItems.value.find(entry => entry.id === ids[0])
        if (item) {
          moveToIndex(item, positionValue.value - 1)
        }
      } else {
        moveBlockToIndex(ids, positionValue.value - 1)
        clearSelection()
      }
    }
  } else {
    const target = aboveTarget.value
    if (target && typeof target === 'object') {
      moveBlockAbove(ids, target.id)
      clearSelection()
    }
  }
  closeMoveDialog()
}

const saveOrder = async () => {
  const ids = workingItems.value.map(item => item.id)
  const result = await props.save(ids)
  if (result.ok) {
    // The server now holds the submitted snapshot; rebasing the baseline onto it keeps any
    // edits made while the request was in flight staged as dirty.
    originalIds.value = ids
    if (isDirty.value) {
      showSnackBar({ msg: `${capitalNoun.value} order saved. Changes made while saving are still unsaved.` })
    } else {
      showSnackBar({ msg: `${capitalNoun.value} order saved.` })
      emit('close')
    }
  } else {
    // 400 or 404 means the list no longer matches the server (an item added or deleted
    // meanwhile): re-fetch so the reorder mode edits the current truth. The fresh list
    // replaces the working copy, so the snackbar says the staged moves are gone.
    const outOfDate = result.status === 400 || result.status === 404
    showSnackBar({
      msg: (result.message || `Failed to save the ${props.itemNoun} order.`)
        + (outOfDate ? ' The list was reloaded and your unsaved moves were discarded.' : ''),
      error: true
    })
    if (outOfDate) {
      await props.refetch()
    }
  }
}

const resetOrder = () => {
  const byId = new Map(workingItems.value.map(item => [item.id, item]))
  workingItems.value = originalIds.value
    .map(id => byId.get(id))
    .filter((item): item is T => !!item)
  clearSelection()
  announcement.value = 'Order reset'
}

const requestClose = () => {
  if (isDirty.value) {
    discardConfirmVisible.value = true
  } else {
    emit('close')
  }
}

// Route guards in the host components call this before navigating away: unsaved edits
// raise the same discard dialog, and the promise carries the user's choice back to the guard.
let leaveResolver: ((leave: boolean) => void) | null = null

const confirmLeave = (): Promise<boolean> => {
  if (!isDirty.value) {
    return Promise.resolve(true)
  }
  discardConfirmVisible.value = true
  return new Promise((resolve) => {
    leaveResolver = resolve
  })
}

const keepEditing = () => {
  discardConfirmVisible.value = false
  leaveResolver?.(false)
  leaveResolver = null
}

const discardAndClose = () => {
  discardConfirmVisible.value = false
  resetOrder()
  emit('close')
  leaveResolver?.(true)
  leaveResolver = null
}

defineExpose({ confirmLeave })
</script>

<style lang="scss" scoped>
.staged-reorder {
  display: flex;
  flex-direction: column;
}

.intro {
  margin: 0 0 1rem 0;
}

// The list is unpaged, so the filter and the action bar stay pinned to the viewport
// while the rows in between scroll with the page.
.filter-row {
  position: sticky;
  top: var(--onms-header-height, 3.75rem);
  z-index: 2;
  background: var(--p-content-background);
  padding: 8px 0;
  margin-bottom: 0.5rem;

  .filter-field {
    width: 100%;
  }

  .filter-meta {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.8rem;
    color: var(--p-text-muted-color);
  }
}

.reorder-row {
  display: flex;
  gap: 0.6rem;
  margin-bottom: 0.5rem;
  border: 1px solid var(--p-content-border-color);
  padding: 4px 10px;
  border-radius: 5px;
  align-items: center;
  cursor: grab;
}

.reorder-row.selected {
  border-color: var(--p-primary-color);
}

.rank-badge {
  min-width: 1.8rem;
  height: 1.8rem;
  border-radius: 50%;
  border: 1.5px solid var(--p-primary-color);
  color: var(--p-primary-color);
  font-size: 0.75rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.changed {
    background: var(--p-primary-color);
    color: var(--p-primary-contrast-color);
  }
}

.drag-hint {
  color: var(--p-text-muted-color);
  flex-shrink: 0;
}

.show-more-row {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.sticky-actions {
  position: sticky;
  bottom: 0;
  z-index: 2;
  background: var(--p-content-background);
  padding: 8px 0 4px;
  box-shadow: 0 -6px 8px -8px rgba(0, 0, 0, 0.35);
}

.selection-bar {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin: 0 0 0.5rem;
  padding: 8px 12px;
  border-radius: 5px;
  background: var(--p-content-hover-background);

  .selection-count {
    font-weight: 600;
  }
}

.button-row {
  display: flex;
  flex-direction: row;
  gap: 1rem;
  align-items: center;

  .moved-count {
    flex-grow: 1;
    font-size: 0.85rem;
    color: var(--p-text-muted-color);
  }
}

.move-dialog-content {
  min-width: 22rem;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}
</style>
