<template>
  <div class="picker" :data-test="`picker-${mode}`">
    <div class="picker-title">{{ mode === 'node' ? 'Nodes' : 'Interfaces' }}</div>

    <ul class="selection-list" :data-test="`picker-${mode}-selection`">
      <!-- match-any lives in the interface list; the node picker mirrors it as
           a display-only row so both read as "everything selected" -->
      <li v-if="mode === 'node' && matchAny" class="selection-item" :data-test="`picker-${mode}-all`">
        <span>All Nodes <span class="hint">(remove All Interfaces to pick nodes)</span></span>
      </li>
      <li v-else-if="!items.length" class="none" :data-test="`picker-${mode}-empty`">
        {{ mode === 'node' ? 'No specific nodes selected' : 'No specific interfaces selected' }}
      </li>
      <template v-else>
        <li
          v-for="(item, index) in items"
          :key="index"
          class="selection-item"
          :data-test="`picker-${mode}-item`"
        >
          <span>{{ labelFor(item) }}</span>
          <OnmsIconButton
            :icon="Delete"
            severity="danger"
            :title="`Remove ${labelFor(item)}`"
            :aria-label="`Remove ${labelFor(item)}`"
            :data-test="`picker-${mode}-remove`"
            @click="emit('remove', index)"
          />
        </li>
      </template>
    </ul>

    <FormField :label="mode === 'node' ? 'Add a node' : 'Add an interface'" :for="`picker-${mode}-input`">
      <div class="search-row" @keydown.enter="onEnter">
        <OnmsAutoComplete
          ref="searchBox"
          v-model="selection"
          :unsafePt="{ pcInputText: { root: { 'aria-describedby': `picker-${mode}-enter-miss` }}}"
          :inputId="`picker-${mode}-input`"
          :suggestions="suggestions"
          optionLabel="label"
          :placeholder="mode === 'node' ? 'Search by node label' : 'Search by IP address or host name'"
          fluid
          :data-test="`picker-${mode}-search`"
          @complete="onComplete"
        />
        <OnmsButton
          label="Add"
          icon="pi pi-plus"
          :disabled="!picked"
          :data-test="`picker-${mode}-add`"
          @click="addSelection"
        />
      </div>
    </FormField>
    <!-- always rendered, so screen readers announce the text when it changes -->
    <small :id="`picker-${mode}-enter-miss`" class="field-note" role="status" :data-test="`picker-${mode}-enter-miss`">{{ enterMiss }}</small>
    <small class="hint">Up to 200 matches are listed.</small>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { OnmsAutoComplete, OnmsButton, OnmsIconButton } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import { OutageInterface, OutageNode } from '@/types/scheduledOutage'
import { searchOutageInterfaces, searchOutageNodes } from '@/services/scheduledOutagesService'

interface NodeSuggestion { label: string, id: number, nodeLabel: string }
interface InterfaceSuggestion { label: string, address: string }
type Suggestion = NodeSuggestion | InterfaceSuggestion

const props = defineProps<{
  mode: 'node' | 'interface'
  items: (OutageNode | OutageInterface)[]
  // node id -> label, resolved by the editor; ids without an entry are shown
  // as not-found (deleted nodes still referenced by the outage config)
  nodeLabels?: Record<number, string>
  // the outage applies to everything; the node picker shows an All Nodes row
  matchAny?: boolean
}>()

const emit = defineEmits<{
  add: [value: OutageNode | OutageInterface, label?: string]
  remove: [index: number]
}>()

// typed text is a string until a suggestion is picked
const selection = ref<Suggestion | string | null>(null)
const suggestions = ref<Suggestion[]>([])
// why Enter added nothing; cleared as soon as the text or pick changes
const enterMiss = ref('')
watch(selection, () => {
  enterMiss.value = ''
})

const searchBox = ref<{ clearInput: () => void } | null>(null)
// the query the current suggestions answer; a newer search supersedes an older one
let suggestionsFor: string | null = null
let searchSeq = 0

const onComplete = async (query: string) => {
  const seq = ++searchSeq
  const results: Suggestion[] = props.mode === 'node'
    ? (await searchOutageNodes(query)).map(n => ({ label: `${n.label} (id ${n.id})`, id: n.id, nodeLabel: n.label }))
    : (await searchOutageInterfaces(query)).map(i => ({
      label: i.nodeLabel ? `${i.address} — ${i.nodeLabel}` : i.address,
      address: i.address
    }))
  if (seq === searchSeq) {
    suggestions.value = results
    suggestionsFor = query
  }
}

const picked = computed<Suggestion | null>(() =>
  selection.value && typeof selection.value === 'object' ? selection.value : null
)

const addSelection = () => {
  const sel = picked.value
  if (!sel) {
    return
  }
  if (props.mode === 'node' && 'id' in sel) {
    emit('add', { id: sel.id }, sel.nodeLabel)
  } else if (props.mode === 'interface' && 'address' in sel) {
    emit('add', { address: sel.address })
  }
  // also cancels a search still pending for the text just added
  searchBox.value?.clearInput()
  selection.value = null
}

// The text the user typed, matched against the current suggestions.
const exactMatches = (text: string): Suggestion[] => {
  const typed = text.trim().toLowerCase()
  return suggestions.value.filter(s => [s.label, 'address' in s ? s.address : s.nodeLabel]
    .some(v => v.toLowerCase() === typed))
}

// Enter adds the highlighted suggestion (the autocomplete has already picked it
// by the time the key reaches this row) or the one suggestion the typed text
// names exactly; anything else is explained instead of silently dropped.
const onEnter = async (event: KeyboardEvent) => {
  // Enter on the Add button is already a click
  if (!(event.target instanceof HTMLInputElement)) {
    return
  }
  await nextTick()
  if (!picked.value && typeof selection.value === 'string' && selection.value.trim()) {
    const text = selection.value
    // Enter can beat the debounced search; match against results for this text
    if (suggestionsFor !== text) {
      await onComplete(text)
    }
    if (selection.value !== text) {
      return
    }
    const matches = exactMatches(text)
    if (matches.length === 1) {
      selection.value = matches[0]
    } else {
      enterMiss.value = matches.length
        ? `"${text.trim()}" matches more than one entry. Pick one from the list, then press Add.`
        : `No exact match for "${text.trim()}". Pick a match from the list, then press Add.`
      return
    }
  }
  addSelection()
}

const labelFor = (item: OutageNode | OutageInterface): string => {
  if (!('id' in item)) {
    return item.address === 'match-any' ? 'All Interfaces' : item.address
  }
  const label = props.nodeLabels?.[item.id]
  return label ? `${label} (id ${item.id})` : `Node id ${item.id} (not found)`
}
</script>

<style scoped lang="scss">
.picker {
  .picker-title {
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .search-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    :deep(.p-autocomplete) {
      flex: 1 1 auto;
      min-width: 0;
    }
  }


  .selection-list {
    list-style: none;
    margin: 0 0 0.75rem 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .selection-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    min-height: 2.25rem;
    padding: 0 0.25rem 0 0.5rem;
    border-radius: 4px;
    background: var(--p-content-hover-background, rgba(127, 127, 127, 0.08));

    > span {
      min-width: 0;
      overflow-wrap: anywhere;
    }
  }

  .none {
    font-style: italic;
    color: var(--p-text-muted-color);
  }

  .hint,
  .field-note {
    color: var(--p-text-muted-color);
  }

  small.hint,
  small.field-note {
    display: block;
  }
}
</style>
