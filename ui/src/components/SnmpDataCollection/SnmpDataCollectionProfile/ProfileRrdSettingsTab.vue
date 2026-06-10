<template>
  <div
    class="rrd-settings-box"
    data-test="rrd-settings-box"
  >
    <div class="section-header">RRD Settings</div>
    <div class="input-row">
      <FeatherInput
        label="RRD Step"
        :modelValue="rrdSettings.rrdStep"
        @update:modelValue="update('rrdStep', String($event))"
        type="number"
        :error="errors.rrdStep"
        hint="RRD step size in seconds"
        data-test="rrd-step"
      />
    </div>
    <div class="rra-section">
      <div class="rra-header">
        <span class="rra-title">RRAs</span>
        <FeatherButton
          secondary
          icon="Add"
          data-test="add-rra-button"
          class="add-rra-button"
          @click="addRRA"
        >
          <FeatherIcon :icon="Add" />
          Add RRA
        </FeatherButton>
      </div>
      <PDataTable
        v-model:editingRows="editingRows"
        :value="rrdSettings.rras"
        editMode="row"
        dataKey="_id"
        @row-edit-save="onRowEditSave"
        data-test="rra-table"
      >
        <PColumn
          header="RRA"
          style="width: 4rem"
        >
          <template #body>
            <span>RRA</span>
          </template>
          <template #editor>
            <span>RRA</span>
          </template>
        </PColumn>
        <PColumn
          field="cf"
          header="Consolidation Function"
        >
          <template #editor="{ data }">
            <PSelect
              v-model="data.cf"
              :options="cfOptions"
              optionLabel="label"
              optionValue="value"
            />
          </template>
        </PColumn>
        <PColumn
          field="xff"
          header="XFF"
        >
          <template #editor="{ data }">
            <PInputNumber
              v-model="data.xff"
              :min="0"
              :maxFractionDigits="6"
            />
          </template>
        </PColumn>
        <PColumn
          field="steps"
          header="Step"
        >
          <template #editor="{ data }">
            <PInputNumber
              v-model="data.steps"
              :min="1"
              :step="1"
            />
          </template>
        </PColumn>
        <PColumn
          field="rows"
          header="Rows"
        >
          <template #editor="{ data }">
            <PInputNumber
              v-model="data.rows"
              :min="1"
              :step="1"
            />
          </template>
        </PColumn>
        <PColumn
          header=""
          style="width: 4rem"
        >
          <template #body="{ data }">
            <FeatherButton
              icon="Delete"
              data-test="delete-rra-button"
              @click="deleteRRA(data._id)"
            >
              <FeatherIcon :icon="Delete" />
            </FeatherButton>
          </template>
        </PColumn>
        <PColumn
          :rowEditor="true"
          style="width: 8rem"
          bodyStyle="text-align: center"
          :pt="{
            pcRowEditorInit: {
              root: { title: 'Edit' }
            }
          }"
        />
      </PDataTable>
    </div>
    <span
      v-if="errors.rrdRras"
      class="field-error"
    >{{ errors.rrdRras }}</span>
  </div>
</template>

<script setup lang="ts">
import type { EditableRRA, ProfileFormErrors, RrdSettingsModel } from '@/types/snmpDataCollection'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'
import { FeatherInput } from '@featherds/input'
import DataTableComponent from 'primevue/datatable'
import type { DataTableRowEditSaveEvent } from 'primevue/datatable'
import ColumnComponent from 'primevue/column'
import InputNumberComponent from 'primevue/inputnumber'
import SelectComponent from 'primevue/select'

const PDataTable = DataTableComponent
const PColumn = ColumnComponent
const PInputNumber = InputNumberComponent
const PSelect = SelectComponent

const props = defineProps<{
  rrdSettings: RrdSettingsModel
  errors: ProfileFormErrors
}>()

const emit = defineEmits<{
  'update:rrdSettings': [value: RrdSettingsModel]
}>()

const editingRows = ref<EditableRRA[]>([])
let nextRRAId = 0

watch(
  () => props.rrdSettings.rras,
  (rras) => {
    if (rras.length > 0) {
      nextRRAId = Math.max(...rras.map(r => r._id)) + 1
    }
  },
  { immediate: true }
)

const cfOptions = Object.values(ConsolidationFunctionType).map(v => ({ label: v, value: v }))

const update = <K extends keyof RrdSettingsModel>(key: K, value: RrdSettingsModel[K]) => {
  emit('update:rrdSettings', { ...props.rrdSettings, [key]: value })
}

const addRRA = () => {
  emit('update:rrdSettings', {
    ...props.rrdSettings,
    rras: [...props.rrdSettings.rras, {
      _id: nextRRAId++,
      cf: ConsolidationFunctionType.AVERAGE,
      xff: 0.5,
      steps: 1,
      rows: 1
    }]
  })
}

const deleteRRA = (id: number) => {
  emit('update:rrdSettings', {
    ...props.rrdSettings,
    rras: props.rrdSettings.rras.filter(r => r._id !== id)
  })
}

const onRowEditSave = (event: DataTableRowEditSaveEvent) => {
  const rras = [...props.rrdSettings.rras]
  rras[event.index] = { ...event.newData } as EditableRRA
  emit('update:rrdSettings', { ...props.rrdSettings, rras })
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.rrd-settings-box {
  padding: 20px 0;
}

.section-header {
  @include headline3;
  margin-bottom: 16px;
}

.input-row {
  max-width: 300px;
}

.field-error {
  color: var(--feather-error);
  font-size: 0.8em;
}

.rra-section {
  margin-top: 20px;

  :deep(.p-datatable-thead > tr > th) {
    background-color: var(--feather-background);
    border-bottom: 1px solid var(--feather-border-on-surface);
    color: var(--feather-secondary-text-on-surface);
    text-transform: uppercase;
  }

  :deep(.p-datatable-tbody > tr) {
    background-color: var(--feather-surface);
    color: var(--feather-primary-text-on-surface);
  }

  :deep(.p-datatable-tbody > tr > td) {
    border-color: var(--feather-border-on-surface);
    color: var(--feather-primary-text-on-surface);
  }

  :deep(.p-select) {
    background-color: var(--feather-surface);
    border-color: var(--feather-border-on-surface);
    color: var(--feather-primary-text-on-surface);
  }

  :deep(.p-select-label) {
    color: var(--feather-primary-text-on-surface);
  }

  .rra-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .rra-title {
      @include headline4;
      color: var(--feather-secondary-text-on-surface);
    }

    .add-rra-button {
      border-radius: 0;
      border: 1px solid var(--feather-primary);
      width: auto;
      padding: 0.5em 1em;
    }
  }
}
</style>
