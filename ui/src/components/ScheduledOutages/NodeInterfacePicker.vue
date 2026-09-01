<template>
  <div class="picker" :data-test="`picker-${mode}`">
    <div class="picker-title">{{ mode === 'node' ? 'Nodes' : 'Interfaces' }}</div>
    <FormField :label="`Search (max 200 results)`" :for="`picker-${mode}-input`">
      <div class="search-row">
        <OnmsAutoComplete
          v-model="selection"
          :inputId="`picker-${mode}-input`"
          :suggestions="suggestions"
          optionLabel="label"
          :placeholder="mode === 'node' ? 'Node label' : 'IP address'"
          forceSelection
          fluid
          :data-test="`picker-${mode}-search`"
          @complete="onComplete"
        />
        <OnmsButton
          label="Add"
          icon="pi pi-plus"
          :disabled="!selection"
          :data-test="`picker-${mode}-add`"
          @click="addSelection"
        />
      </div>
    </FormField>

    <div class="current">
      <div class="current-title">Current selection:</div>
      <!-- match-any lives in the interface list; the node picker mirrors it as
           a display-only chip so both read as "everything selected" -->
      <div v-if="mode === 'node' && matchAny" class="chips">
        <OnmsChip label="All Nodes" :data-test="`picker-${mode}-all`" />
      </div>
      <p v-else-if="!items.length" class="none" :data-test="`picker-${mode}-empty`">
        {{ mode === 'node' ? 'No specific nodes selected' : 'No specific interfaces selected' }}
      </p>
      <div v-else class="chips">
        <OnmsChip
          v-for="(item, index) in items"
          :key="index"
          :label="labelFor(item)"
          removable
          :data-test="`picker-${mode}-chip`"
          @remove="emit('remove', index)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsAutoComplete, OnmsButton, OnmsChip } from '@opennms/onms-ui'
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
  // the outage applies to everything; the node picker shows an All Nodes chip
  matchAny?: boolean
}>()

const emit = defineEmits<{
  add: [value: OutageNode | OutageInterface, label?: string]
  remove: [index: number]
}>()

const selection = ref<Suggestion | null>(null)
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

const addSelection = () => {
  const sel = selection.value
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

  .current {
    margin-top: 0.75rem;
  }

  .current-title {
    font-weight: 600;
    margin-bottom: 0.25rem;
  }

  .none {
    margin: 0;
    font-style: italic;
    color: var(--p-text-muted-color);
  }

  .chips {
    display: flex;
    flex-wrap: wrap;
    gap: 0.35rem;
  }
}
</style>
