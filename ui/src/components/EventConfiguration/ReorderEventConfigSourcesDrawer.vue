<template>
  <OnmsDrawer
    v-model:visible="drawerVisible"
    header="Reorder Sources"
    width="46em"
  >
    <div class="drawer-content">
      <p class="intro">
        Sources at the top are evaluated first when matching events. Drag rows or use the arrows;
        nothing changes until you save.
      </p>

      <div class="filter-row">
        <FormField class="filter-field">
          <OnmsSearchInput
            :input-id="filterId"
            :modelValue="filterTerm"
            @update:modelValue="onFilterChange"
            data-test="reorder-filter"
            placeholder="Filter by name or vendor — several terms match any"
            :aria-label="'Filter sources by name or vendor'"
          />
        </FormField>
        <div class="filter-meta">
          <span data-test="filter-count">Showing {{ visibleSources.length }} of {{ workingSources.length }} sources</span>
          <OnmsButton
            v-if="isFiltering && visibleSources.length"
            variant="text"
            size="small"
            data-test="select-all-shown"
            @click="selectAllShown"
          >Select all shown</OnmsButton>
        </div>
      </div>

      <Draggable
        :modelValue="visibleSources"
        @update:modelValue="onVisibleReorder"
        @start="onDragStart"
        item-key="id"
        filter="button, input, a, .p-checkbox"
        :prevent-on-filter="false"
        class="sources-drag-container"
        data-test="reorder-list"
      >
        <template #item="{ element }">
          <div
            class="source-row"
            :class="{ selected: isSelected(element.id) }"
            :data-test="`source-row-${element.id}`"
          >
            <OnmsCheckbox
              :input-id="`select-${element.id}`"
              :modelValue="isSelected(element.id)"
              @update:modelValue="toggleSelected(element.id)"
              :aria-label="`Select ${element.name}`"
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
            <div class="source-info">
              <span class="source-name">{{ element.name }}</span>
              <span class="source-meta">{{ element.vendor }} &middot; {{ element.eventCount }} events</span>
            </div>
            <OnmsTag
              :class="element.enabled ? 'enabled-tag' : 'disabled-tag'"
              :value="element.enabled ? 'Enabled' : 'Disabled'"
            />
            <OnmsIconButton
              :title="`Move ${element.name} up`"
              data-test="move-up-button"
              :icon="ArrowUp"
              :disabled="rankOf(element.id) === 1"
              @click="moveStep(element, -1)"
            />
            <OnmsIconButton
              :title="`Move ${element.name} down`"
              data-test="move-down-button"
              :icon="ArrowDown"
              :disabled="rankOf(element.id) === workingSources.length"
              @click="moveStep(element, 1)"
            />
            <OnmsIconButton
              aria-haspopup="true"
              aria-controls="reorder-source-row-menu"
              :title="`More actions for ${element.name}`"
              data-test="row-menu-button"
              :icon="MenuIcon"
              @click="toggleRowMenu($event, element)"
            />
          </div>
        </template>
      </Draggable>

      <div
        v-if="catchAllSource"
        class="catch-all-row"
        data-test="catch-all-row"
      >
        <OnmsIcon
          :icon="Lock"
          aria-hidden="true"
          focusable="false"
        />
        <div class="source-info">
          <span class="source-name">{{ catchAllSource.name }}</span>
          <span class="source-meta">Always evaluated last (pinned)</span>
        </div>
        <OnmsTag
          :class="catchAllSource.enabled ? 'enabled-tag' : 'disabled-tag'"
          :value="catchAllSource.enabled ? 'Enabled' : 'Disabled'"
        />
      </div>

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
          data-test="selection-above"
          @click="openMoveDialog('above', selectedIdList)"
        >Move Above Source&hellip;</OnmsButton>
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
        >{{ movedCount === 0 ? 'No changes yet' : `${movedCount} sources moved` }}</span>
        <OnmsButton
          :disabled="!isDirty || store.isSavingSourceOrder"
          :loading="store.isSavingSourceOrder"
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
          data-test="close-button"
          @click="requestClose"
        >Close</OnmsButton>
      </div>

      <span
        class="visually-hidden"
        aria-live="polite"
        data-test="move-announcement"
      >{{ announcement }}</span>
    </div>

    <OnmsMenu
      id="reorder-source-row-menu"
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <OnmsConfirmationDialog
      :visible="moveDialogState.visible"
      :title="moveDialogState.mode === 'position' ? 'Move to Position' : 'Move Above Source'"
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
            :hint="`1 = evaluated first · currently at position ${moveDialogState.currentRank} of ${workingSources.length}`"
          >
            <OnmsInputNumber
              v-model="positionValue"
              :input-id="positionInputId"
              :min="1"
              :max="workingSources.length"
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
              placeholder="Search sources..."
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
      @cancel="discardConfirmVisible = false"
    >
      <template #content>
        <p>The new source order has not been saved. Closing the drawer discards it.</p>
      </template>
    </OnmsConfirmationDialog>
  </OnmsDrawer>
</template>

<script lang="ts" setup>
import { computed, ref, useId, watch } from 'vue'

import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
import {
  OnmsAutoComplete,
  OnmsButton,
  OnmsCheckbox,
  OnmsConfirmationDialog,
  OnmsDrawer,
  OnmsIcon,
  OnmsIconButton,
  OnmsInputNumber,
  OnmsMenu,
  OnmsMenuItem,
  OnmsSearchInput,
  OnmsTag
} from '@opennms/onms-ui'
import Lock from '@opennms/onms-ui/icons/action/Lock.vue'
import KeyboardArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import KeyboardArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'
import Apps from '@opennms/onms-ui/icons/navigation/Apps.vue'
import MenuIcon from '@opennms/onms-ui/icons/navigation/MoreHoriz.vue'
import Draggable from 'vuedraggable'

const ArrowUp = KeyboardArrowUp
const ArrowDown = KeyboardArrowDown

const store = useEventConfigStore()
const { showSnackBar } = useSnackbar()
const filterId = useId()
const positionInputId = useId()
const aboveInputId = useId()

// The working copy the drawer edits: every mechanism (drag, arrows, menu moves, block moves)
// rearranges this array; Save sends it as one complete order.
const workingSources = ref<EventConfigSource[]>([])
const originalIds = ref<number[]>([])
const filterTerm = ref('')
const selectedIds = ref<Record<number, boolean>>({})
const announcement = ref('')
const discardConfirmVisible = ref(false)

const rowMenu = ref()
const rowMenuTarget = ref<EventConfigSource | null>(null)

const moveDialogState = ref<{
  visible: boolean
  mode: 'position' | 'above'
  ids: number[]
  currentRank: number
}>({ visible: false, mode: 'position', ids: [], currentRank: 1 })
const positionValue = ref<number>(1)
const aboveTarget = ref<EventConfigSource | string | null>(null)
const aboveSuggestions = ref<EventConfigSource[]>([])

const catchAllSource = computed(() => store.catchAllSource)

const drawerVisible = computed({
  get: () => store.reorderSourcesDrawerState.visible,
  set: (val: boolean) => {
    if (!val) {
      requestClose()
    }
  }
})

const initWorkingCopy = () => {
  workingSources.value = [...store.orderedSources]
  originalIds.value = store.orderedSources.map(source => source.id)
  selectedIds.value = {}
  filterTerm.value = ''
  announcement.value = ''
}

watch(() => store.orderedSources, initWorkingCopy, { immediate: true })

const isDirty = computed(() => workingSources.value.map(source => source.id).join(',') !== originalIds.value.join(','))
const movedCount = computed(() =>
  workingSources.value.filter((source, index) => originalIds.value[index] !== source.id).length)

const rankOf = (id: number) => workingSources.value.findIndex(source => source.id === id) + 1
const hasMoved = (id: number) => originalIds.value[rankOf(id) - 1] !== id

const terms = computed(() =>
  filterTerm.value.toLowerCase().split(/[\s,]+/).filter(term => term.length > 0))
const isFiltering = computed(() => terms.value.length > 0)

const matchesFilter = (source: EventConfigSource) => {
  if (!terms.value.length) {
    return true
  }
  const haystack = `${source.name} ${source.vendor}`.toLowerCase()
  return terms.value.some(term => haystack.includes(term))
}

const visibleSources = computed(() => workingSources.value.filter(matchesFilter))

const onFilterChange = (value: string | undefined) => {
  filterTerm.value = value ?? ''
}

const announceMove = (source: EventConfigSource) => {
  announcement.value = `Moved ${source.name} to position ${rankOf(source.id)}`
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
  draggedId.value = visibleSources.value[event.oldIndex]?.id ?? null
}

const onVisibleReorder = (newVisible: EventConfigSource[]) => {
  const oldVisible = visibleSources.value
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
  let moved: EventConfigSource
  let movedIndex: number
  const grabbed = grabbedId != null ? newVisible.find(source => source.id === grabbedId) : undefined
  if (grabbed) {
    moved = grabbed
    movedIndex = newVisible.findIndex(source => source.id === grabbed.id)
  } else if (firstDiff + 1 < oldVisible.length && oldVisible[firstDiff + 1].id === newVisible[firstDiff].id) {
    // the row at firstDiff moved down; everything below shifted up into its place
    moved = oldVisible[firstDiff]
    movedIndex = newVisible.findIndex(source => source.id === moved.id)
  } else {
    moved = newVisible[firstDiff]
    movedIndex = firstDiff
  }
  const successor = newVisible[movedIndex + 1]
  const predecessor = newVisible[movedIndex - 1]
  const rest = workingSources.value.filter(source => source.id !== moved.id)
  let insertAt: number
  if (successor) {
    insertAt = rest.findIndex(source => source.id === successor.id)
  } else if (predecessor) {
    insertAt = rest.findIndex(source => source.id === predecessor.id) + 1
  } else {
    return
  }
  rest.splice(insertAt, 0, moved)
  workingSources.value = rest
  announceMove(moved)
}

const moveToIndex = (source: EventConfigSource, index: number) => {
  const rest = workingSources.value.filter(entry => entry.id !== source.id)
  const clamped = Math.max(0, Math.min(index, rest.length))
  rest.splice(clamped, 0, source)
  workingSources.value = rest
  announceMove(source)
}

const moveStep = (source: EventConfigSource, delta: number) => {
  const index = rankOf(source.id) - 1
  const target = index + delta
  if (target < 0 || target >= workingSources.value.length) {
    return
  }
  moveToIndex(source, target)
}

const moveBlockEdge = (ids: number[], top: boolean) => {
  const inSelection = new Set(ids)
  const selection = workingSources.value.filter(source => inSelection.has(source.id))
  const rest = workingSources.value.filter(source => !inSelection.has(source.id))
  workingSources.value = top ? [...selection, ...rest] : [...rest, ...selection]
  announcement.value = `Moved ${selection.length} sources to the ${top ? 'top' : 'bottom'}`
}

const moveBlockAbove = (ids: number[], targetId: number) => {
  const inSelection = new Set(ids)
  if (inSelection.has(targetId)) {
    return
  }
  const selection = workingSources.value.filter(source => inSelection.has(source.id))
  const rest = workingSources.value.filter(source => !inSelection.has(source.id))
  const insertAt = rest.findIndex(source => source.id === targetId)
  if (insertAt < 0) {
    return
  }
  rest.splice(insertAt, 0, ...selection)
  workingSources.value = rest
  announcement.value = `Moved ${selection.length === 1 ? selection[0].name : `${selection.length} sources`} above ${rest[insertAt + selection.length].name}`
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
  visibleSources.value.forEach((source) => {
    next[source.id] = true
  })
  selectedIds.value = next
}
const selectedIdList = computed(() =>
  workingSources.value.filter(source => selectedIds.value[source.id]).map(source => source.id))

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
      command: () => moveToIndex(target, workingSources.value.length - 1)
    },
    {
      label: 'Move to Position...',
      command: () => openMoveDialog('position', [target.id])
    },
    {
      label: 'Move Above Source...',
      command: () => openMoveDialog('above', [target.id])
    }
  ]
})

const toggleRowMenu = (event: Event, source: EventConfigSource) => {
  rowMenuTarget.value = source
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

const closeMoveDialog = () => {
  moveDialogState.value = { ...moveDialogState.value, visible: false }
}

const onAboveSearch = (query: string) => {
  const needle = (query ?? '').toLowerCase()
  const excluded = new Set(moveDialogState.value.ids)
  aboveSuggestions.value = workingSources.value
    .filter(source => !excluded.has(source.id))
    .filter(source => !needle || source.name.toLowerCase().includes(needle))
}

const applyMoveDialog = () => {
  const { mode, ids } = moveDialogState.value
  if (mode === 'position') {
    const source = workingSources.value.find(entry => entry.id === ids[0])
    if (source && positionValue.value) {
      moveToIndex(source, positionValue.value - 1)
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
  const sourceIds = workingSources.value.map(source => source.id)
  const result = await store.saveSourcesOrder(sourceIds)
  if (result.ok) {
    // The server now holds the submitted snapshot; rebasing the baseline onto it keeps any
    // edits made while the request was in flight staged as dirty.
    originalIds.value = sourceIds
    if (isDirty.value) {
      showSnackBar({ msg: 'Source order saved. Changes made while saving are still unsaved.' })
    } else {
      showSnackBar({ msg: 'Source order saved.' })
      store.hideReorderSourcesDrawer()
    }
  } else {
    showSnackBar({
      msg: result.message || 'Failed to save the source order.',
      error: true
    })
    // a 400 means the list no longer matches the server (for example a source added meanwhile):
    // re-fetch so the drawer edits the current truth
    if (result.status === 400) {
      await store.fetchOrderedSources()
    }
  }
}

const resetOrder = () => {
  const byId = new Map(workingSources.value.map(source => [source.id, source]))
  workingSources.value = originalIds.value
    .map(id => byId.get(id))
    .filter((source): source is EventConfigSource => !!source)
  clearSelection()
  announcement.value = 'Order reset'
}

const requestClose = () => {
  if (isDirty.value) {
    discardConfirmVisible.value = true
  } else {
    store.hideReorderSourcesDrawer()
  }
}

const discardAndClose = () => {
  discardConfirmVisible.value = false
  resetOrder()
  store.hideReorderSourcesDrawer()
}
</script>

<style lang="scss" scoped>
// No height/overflow here on purpose: PrimeVue's own `.p-drawer-content` is the
// drawer's scroll container (see ColumnSelectionDrawer / NMS-20182).
.drawer-content {
  padding: 20px;
}

.intro {
  margin: 0 0 1rem 0;
}

.filter-row {
  margin-bottom: 1rem;

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

.source-row,
.catch-all-row {
  display: flex;
  gap: 0.6rem;
  margin-bottom: 0.5rem;
  border: 1px solid var(--p-content-border-color);
  padding: 4px 10px;
  border-radius: 5px;
  align-items: center;
}

.source-row {
  cursor: grab;
}

.source-row.selected {
  border-color: var(--p-primary-color);
}

.catch-all-row {
  border-style: dashed;
  background: var(--p-content-hover-background);
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

.source-info {
  flex-grow: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;

  .source-name {
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .source-meta {
    font-size: 0.8rem;
    color: var(--p-text-muted-color);
  }
}

.selection-bar {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin: 0.75rem 0;
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
  margin-top: 1rem;

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

.enabled-tag {
  border-radius: 4px;
  background-color: #0B720C1F;

  :deep(.p-tag-label) {
    color: #0B720C !important;
  }
}

.disabled-tag {
  border-radius: 4px;
  background-color: #7575751F;

  :deep(.p-tag-label) {
    color: #757575 !important;
  }
}
</style>
