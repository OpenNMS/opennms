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
      <div class="search-row">
        <OnmsAutoComplete
          v-model="selection"
          :inputId="`picker-${mode}-input`"
          :suggestions="suggestions"
          optionLabel="label"
          :placeholder="mode === 'node' ? 'Search by node label' : 'Search by IP address or host name'"
          forceSelection
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
    <small class="hint">Up to 200 matches are listed.</small>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
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

const onComplete = async (query: string) => {
  if (props.mode === 'node') {
    const nodes = await searchOutageNodes(query)
    suggestions.value = nodes.map(n => ({ label: `${n.label} (id ${n.id})`, id: n.id, nodeLabel: n.label }))
  } else {
    const interfaces = await searchOutageInterfaces(query)
    suggestions.value = interfaces.map(i => ({
      label: i.nodeLabel ? `${i.address} — ${i.nodeLabel}` : i.address,
      address: i.address
    }))
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
  selection.value = null
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
  }

  .none {
    font-style: italic;
    color: var(--p-text-muted-color);
  }

  .hint {
    color: var(--p-text-muted-color);
  }
}
</style>
