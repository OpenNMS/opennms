<template>
  <div
    class="rrd-settings-box"
    data-test="rrd-settings-box"
  >
    <div class="section-header">RRD Settings</div>
    <div class="input-row">
      <FormField
        label="RRD Step"
        :for="rrdStepId"
        :error="errors.rrdStep"
        hint="RRD step size in seconds"
      >
        <PInputNumber
          :id="rrdStepId"
          :modelValue="rrdSettings.rrdStep === '' ? null : Number(rrdSettings.rrdStep)"
          @update:modelValue="update('rrdStep', $event == null ? '' : String($event))"
          :useGrouping="false"
          :min="1"
          :invalid="!!errors.rrdStep"
          data-test="rrd-step"
          fluid
        />
      </FormField>
    </div>
    <div class="rra-section">
      <div class="rra-header">
        <span class="rra-title">RRAs</span>
        <PButton
          outlined
          data-test="add-rra-button"
          class="add-rra-button"
          @click="addRRA"
        >
          <FeatherIcon :icon="Add" />
          Add RRA
        </PButton>
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
            <PButton
              text
              data-test="delete-rra-button"
              @click="deleteRRA(data._id)"
            >
              <FeatherIcon :icon="Delete" />
            </PButton>
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
import { ref, useId, watch } from 'vue'

import type { EditableRRA, ProfileFormErrors, RrdSettingsModel } from '@/types/snmpDataCollection'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'
import ButtonComponent from 'primevue/button'
import DataTableComponent from 'primevue/datatable'
import type { DataTableRowEditSaveEvent } from 'primevue/datatable'
import ColumnComponent from 'primevue/column'
import InputNumberComponent from 'primevue/inputnumber'
import SelectComponent from 'primevue/select'
import FormField from '@/components/Common/FormField.vue'

const PButton = ButtonComponent
const PDataTable = DataTableComponent
const PColumn = ColumnComponent
const PInputNumber = InputNumberComponent
const PSelect = SelectComponent

const rrdStepId = useId()

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
  display: block;
  color: var(--p-red-500);
  font-size: 0.8em;
  margin-top: 0.25em;
}

.rra-section {
  margin-top: 20px;

  .rra-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .rra-title {
      @include headline4;
      color: var(--feather-secondary-text-on-surface);
    }
  }
}
</style>
