<template>
  <TableCard>
    <div class="table-header">
      <h3>{{ title }}</h3>
      <OnmsButton
        :data-test="`threshold-create-${kind}`"
        :disabled="readOnly"
        @click="onCreate"
      >
        {{ createLabel }}
      </OnmsButton>
    </div>

    <OnmsTable :value="definitions" :data-test="`threshold-table-${kind}`">
      <OnmsColumn header="Type" field="type" />
      <OnmsColumn header="Description">
        <template #body="{ data }">{{ data.description || '--' }}</template>
      </OnmsColumn>
      <OnmsColumn v-if="isThresholdKind" header="Datasource" field="dsName" />
      <OnmsColumn v-else header="Expression" field="expression" />
      <OnmsColumn header="Datasource type" field="dsType" />
      <OnmsColumn header="Datasource label">
        <template #body="{ data }">{{ data.dsLabel || '--' }}</template>
      </OnmsColumn>
      <OnmsColumn v-if="!isThresholdKind" header="Expression label">
        <template #body="{ data }">{{ data.exprLabel || '--' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Value" field="value" />
      <OnmsColumn header="Re-arm" field="rearm" />
      <OnmsColumn header="Trigger" field="trigger" />
      <OnmsColumn header="Triggered UEI">
        <template #body="{ data }">
          <ThresholdUeiCell :uei="data.triggeredUEI" />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Re-armed UEI">
        <template #body="{ data }">
          <ThresholdUeiCell :uei="data.rearmedUEI" />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Filters">
        <template #body="{ data }">{{ data.resourceFilters?.length || 0 }}</template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ index }">
          <div class="actions">
            <OnmsIconButton
              :icon="EditIcon"
              tooltip="Edit"
              aria-label="Edit"
              :data-test="`threshold-edit-${kind}`"
              :disabled="readOnly"
              @click="onEdit(index)"
            />
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Delete"
              aria-label="Delete"
              :data-test="`threshold-delete-${kind}`"
              :disabled="readOnly"
              @click="pendingDeleteIndex = index"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <p v-if="!definitions.length" class="empty" :data-test="`threshold-empty-${kind}`">{{ emptyMessage }}</p>

    <OnmsConfirmationDialog
      v-if="pendingDeleteIndex >= 0"
      :visible="true"
      title="Delete this definition?"
      actionButtonText="Delete"
      data-test="threshold-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDeleteIndex = -1"
    >
      <template #content>
        <p>{{ deleteMessage }}</p>
      </template>
    </OnmsConfirmationDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import TableCard from '@/components/Common/TableCard.vue'
import ThresholdUeiCell from '@/components/ThresholdConfiguration/Group/ThresholdUeiCell.vue'
import useSnackbar from '@/composables/useSnackbar'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { CreateEditMode } from '@/types'
import type { Expression, Threshold } from '@/types/thresholdConfig'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'

const props = defineProps<{
  kind: ThresholdDefinitionKind
  readOnly?: boolean
}>()

const store = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const pendingDeleteIndex = ref(-1)

const isThresholdKind = computed(() => props.kind === ThresholdDefinitionKind.Threshold)

// One table for both kinds: they share every column but one, and two near-identical components were the
// main source of drift in the page this replaces.
const definitions = computed<(Threshold | Expression)[]>(() =>
  isThresholdKind.value ? store.currentThresholds : store.currentExpressions
)

const title = computed(() => (isThresholdKind.value ? 'Basic thresholds' : 'Expression-based thresholds'))

const createLabel = computed(() =>
  isThresholdKind.value ? 'Create new threshold' : 'Create new expression-based threshold'
)

const emptyMessage = computed(() =>
  isThresholdKind.value ? 'This group has no basic thresholds.' : 'This group has no expression-based thresholds.'
)

const deleteMessage = computed(() => {
  const definition = definitions.value[pendingDeleteIndex.value]

  if (!definition) {
    return ''
  }

  const subject = isThresholdKind.value
    ? (definition as Threshold).dsName
    : (definition as Expression).expression

  return `The ${definition.type} threshold on '${subject}' will be removed from this group.`
})

const onCreate = () => {
  store.openDefinitionDrawer(props.kind, CreateEditMode.Create)
}

const onEdit = (index: number) => {
  store.openDefinitionDrawer(props.kind, CreateEditMode.Edit, index)
}

const onConfirmDelete = async () => {
  const index = pendingDeleteIndex.value
  pendingDeleteIndex.value = -1

  store.removeDefinition(props.kind, index)

  const result = await store.saveCurrentGroup()

  showSnackBar({
    msg: result.success ? 'Threshold deleted.' : result.message,
    error: !result.success
  })
}
</script>

<style lang="scss" scoped>
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1em 0.75em 1em;

  h3 {
    margin: 0;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.25em;
}

.empty {
  color: var(--p-text-muted-color);
  padding: 0 1em;
}
</style>
