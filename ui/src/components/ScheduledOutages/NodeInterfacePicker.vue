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
      <p v-if="!items.length" class="none" :data-test="`picker-${mode}-empty`">
        {{ mode === 'node' ? 'No specific nodes selected' : 'No specific interfaces selected' }}
      </p>
      <ul v-else class="chips">
        <li v-for="(item, index) in items" :key="index" class="chip">
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
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsAutoComplete, OnmsButton, OnmsIconButton } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import { OutageInterface, OutageNode } from '@/types/scheduledOutage'
import { searchOutageInterfaces, searchOutageNodes } from '@/services/scheduledOutagesService'

interface NodeSuggestion { label: string, id: number }
interface InterfaceSuggestion { label: string, address: string }
type Suggestion = NodeSuggestion | InterfaceSuggestion

const props = defineProps<{
  mode: 'node' | 'interface'
  items: (OutageNode | OutageInterface)[]
}>()

const emit = defineEmits<{
  add: [value: OutageNode | OutageInterface]
  remove: [index: number]
}>()

const selection = ref<Suggestion | null>(null)
const suggestions = ref<Suggestion[]>([])

const onComplete = async (query: string) => {
  if (props.mode === 'node') {
    const nodes = await searchOutageNodes(query)
    suggestions.value = nodes.map(n => ({ label: `${n.label} (id ${n.id})`, id: n.id }))
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
    emit('add', { id: sel.id })
  } else if (props.mode === 'interface' && 'address' in sel) {
    emit('add', { address: sel.address })
  }
  selection.value = null
}

const labelFor = (item: OutageNode | OutageInterface): string =>
  'id' in item ? `Node id ${item.id}` : item.address
</script>

<style scoped lang="scss">
.picker {
  .picker-title {
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .search-row {
    display: flex;
    align-items: flex-start;
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
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .chip {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    padding: 0.15rem 0.25rem;
    border-radius: 4px;
    background: var(--p-content-hover-background, rgba(127, 127, 127, 0.08));
  }
}
</style>
