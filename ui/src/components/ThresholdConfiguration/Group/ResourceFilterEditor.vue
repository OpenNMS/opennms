<template>
  <div class="resource-filter-editor">
    <div class="section-header">
      <h4>Resource filters</h4>
      <OnmsIcon
        :icon="InfoIcon"
        class="info-icon"
        data-test="resource-filter-info-icon"
        role="button"
        tabindex="0"
        aria-label="About resource filters"
        @click="isHelpVisible = true"
        @keyup.enter="isHelpVisible = true"
      />
    </div>

    <FormField
      label="Filter operator"
      for="resource-filter-operator"
      :hint="THRESHOLD_FIELD_HINTS.filterOperator"
    >
      <OnmsSelect
        inputId="resource-filter-operator"
        data-test="resource-filter-operator"
        optionLabel="_text"
        optionValue="_value"
        :options="FILTER_OPERATOR_OPTIONS"
        :modelValue="filterOperator"
        @update:modelValue="emit('update:filterOperator', String($event ?? FilterOperator.Or))"
      />
    </FormField>

    <p class="order-hint">Filters are applied in the order listed.</p>

    <OnmsTable :value="modelValue" data-test="resource-filter-table">
      <OnmsColumn header="#">
        <template #body="{ index }">{{ index + 1 }}</template>
      </OnmsColumn>
      <OnmsColumn header="Field name">
        <template #body="{ index }">
          <OnmsInputText
            v-if="editingIndex === index"
            :modelValue="draft.field"
            :inputId="`resource-filter-field-${index}`"
            data-test="resource-filter-edit-field"
            :invalid="!!draftErrors.field"
            fluid
            @update:modelValue="draft.field = String($event ?? '')"
          />
          <span v-else data-test="resource-filter-field">{{ modelValue[index].field }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Regular expression">
        <template #body="{ index }">
          <OnmsInputText
            v-if="editingIndex === index"
            :modelValue="draft.content"
            :inputId="`resource-filter-content-${index}`"
            data-test="resource-filter-edit-content"
            fluid
            @update:modelValue="draft.content = String($event ?? '')"
          />
          <span v-else data-test="resource-filter-content">{{ modelValue[index].content || '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ index }">
          <div class="actions">
            <template v-if="editingIndex === index">
              <OnmsButton
                size="small"
                data-test="resource-filter-save"
                :disabled="!!draftErrors.field"
                @click="onSaveEdit(index)"
              >
                Save
              </OnmsButton>
              <OnmsButton variant="text" size="small" data-test="resource-filter-cancel" @click="onCancelEdit">
                Cancel
              </OnmsButton>
            </template>
            <template v-else>
              <OnmsIconButton
                :icon="EditIcon"
                tooltip="Edit filter"
                aria-label="Edit filter"
                data-test="resource-filter-edit"
                @click="onStartEdit(index)"
              />
              <OnmsIconButton
                :icon="DeleteIcon"
                severity="danger"
                tooltip="Delete filter"
                aria-label="Delete filter"
                data-test="resource-filter-delete"
                @click="onDelete(index)"
              />
              <OnmsIconButton
                :icon="MoveUpIcon"
                tooltip="Move filter up"
                aria-label="Move filter up"
                data-test="resource-filter-move-up"
                :disabled="index === 0"
                @click="onMove(index, -1)"
              />
              <OnmsIconButton
                :icon="MoveDownIcon"
                tooltip="Move filter down"
                aria-label="Move filter down"
                data-test="resource-filter-move-down"
                :disabled="index === modelValue.length - 1"
                @click="onMove(index, 1)"
              />
            </template>
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <p v-if="!modelValue.length" class="empty">No resource filters. This threshold applies to every resource.</p>

    <div class="add-row">
      <FormField label="Field name" for="resource-filter-new-field" :error="newFilterErrors.field">
        <OnmsInputText
          inputId="resource-filter-new-field"
          data-test="resource-filter-new-field"
          :modelValue="newFilter.field"
          fluid
          @update:modelValue="newFilter.field = String($event ?? '')"
        />
      </FormField>
      <FormField label="Regular expression" for="resource-filter-new-content" :hint="newFilterErrors.content">
        <OnmsInputText
          inputId="resource-filter-new-content"
          data-test="resource-filter-new-content"
          :modelValue="newFilter.content"
          fluid
          @update:modelValue="newFilter.content = String($event ?? '')"
        />
      </FormField>
      <OnmsButton data-test="resource-filter-add" :disabled="!newFilter.field.trim()" @click="onAdd">
        Add
      </OnmsButton>
    </div>

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Resource filters"
      :text="RESOURCE_FILTER_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import { RESOURCE_FILTER_HELP, THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import { FILTER_OPERATOR_OPTIONS, FilterOperator, validateResourceFilter } from '@/lib/thresholdValidator'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ResourceFilter } from '@/types/thresholdConfig'
import { OnmsButton, OnmsColumn, OnmsIcon, OnmsIconButton, OnmsInputText, OnmsSelect, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import MoveDownIcon from '@opennms/onms-ui/icons/navigation/ExpandMore.vue'
import MoveUpIcon from '@opennms/onms-ui/icons/navigation/ExpandLess.vue'

const props = defineProps<{
  modelValue: ResourceFilter[]
  filterOperator: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ResourceFilter[]]
  'update:filterOperator': [value: string]
}>()

const store = useThresholdGroupStore()

const isHelpVisible = ref(false)
const editingIndex = ref(-1)
const draft = ref<ResourceFilter>({ field: '', content: '' })
const newFilter = ref<ResourceFilter>({ field: '', content: '' })

const draftErrors = computed(() => validateResourceFilter(draft.value))
const newFilterErrors = computed(() =>
  newFilter.value.field || newFilter.value.content ? validateResourceFilter(newFilter.value) : {}
)

const onStartEdit = (index: number) => {
  editingIndex.value = index
  draft.value = { ...props.modelValue[index] }
}

const onCancelEdit = () => {
  editingIndex.value = -1
}

const onSaveEdit = (index: number) => {
  const next = [...props.modelValue]
  next.splice(index, 1, { ...draft.value })
  emit('update:modelValue', next)
  editingIndex.value = -1
}

const onDelete = (index: number) => {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
  editingIndex.value = -1
}

const onMove = (index: number, direction: -1 | 1) => {
  emit('update:modelValue', store.moveResourceFilter(props.modelValue, index, direction))
  editingIndex.value = -1
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, { ...newFilter.value }])
  newFilter.value = { field: '', content: '' }
}
</script>

<style lang="scss" scoped>
.resource-filter-editor {
  .section-header {
    display: flex;
    align-items: center;
    gap: 0.5em;

    h4 {
      margin: 0;
    }
  }

  .info-icon {
    cursor: pointer;
    color: var(--p-text-muted-color);
  }

  .order-hint {
    color: var(--p-text-muted-color);
    margin: 0 0 0.5em 0;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 0.25em;
  }

  .empty {
    color: var(--p-text-muted-color);
  }

  .add-row {
    display: flex;
    align-items: flex-start;
    gap: 0.75em;
    margin-top: 0.75em;
  }
}
</style>
