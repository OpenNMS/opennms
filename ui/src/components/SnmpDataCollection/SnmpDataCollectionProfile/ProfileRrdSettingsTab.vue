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
        <OnmsInputNumber
          :inputId="rrdStepId"
          :modelValue="rrdSettings.rrdStep === '' ? null : Number(rrdSettings.rrdStep)"
          @update:modelValue="update('rrdStep', $event == null ? '' : String($event))"
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
        <OnmsButton
          variant="outlined"
          data-test="add-rra-button"
          class="add-rra-button"
          @click="addRRA"
        >
          <OnmsIcon :icon="Add" />
          Add RRA
        </OnmsButton>
      </div>
      <OnmsTable
        v-model:editingRows="editingRows"
        :value="rrdSettings.rras"
        editMode="row"
        dataKey="_id"
        @row-edit-save="onRowEditSave"
        data-test="rra-table"
      >
        <OnmsColumn
          header="RRA"
          style="width: 4rem"
        >
          <template #body>
            <span>RRA</span>
          </template>
          <template #editor>
            <span>RRA</span>
          </template>
        </OnmsColumn>
        <OnmsColumn
          field="cf"
          header="Consolidation Function"
        >
          <template #editor="{ data }">
            <OnmsSelect
              v-model="data.cf"
              :options="cfOptions"
              optionLabel="label"
              optionValue="value"
            />
          </template>
        </OnmsColumn>
        <OnmsColumn
          field="xff"
          header="XFF"
        >
          <template #editor="{ data }">
            <OnmsInputNumber
              v-model="data.xff"
              :min="0"
              :maxFractionDigits="6"
            />
          </template>
        </OnmsColumn>
        <OnmsColumn
          field="steps"
          header="Step"
        >
          <template #editor="{ data }">
            <OnmsInputNumber
              v-model="data.steps"
              :min="1"
              :step="1"
            />
          </template>
        </OnmsColumn>
        <OnmsColumn
          field="rows"
          header="Rows"
        >
          <template #editor="{ data }">
            <OnmsInputNumber
              v-model="data.rows"
              :min="1"
              :step="1"
            />
          </template>
        </OnmsColumn>
        <OnmsColumn
          header=""
          style="width: 4rem"
        >
          <template #body="{ data }">
            <OnmsIconButton
              title="Delete RRA"
              data-test="delete-rra-button"
              :icon="Delete"
              @click="deleteRRA(data._id)"
            />
          </template>
        </OnmsColumn>
        <OnmsColumn
          :rowEditor="true"
          style="width: 8rem"
          bodyStyle="text-align: center"
          :pt="{
            pcRowEditorInit: {
              root: { title: 'Edit' }
            }
          }"
        />
      </OnmsTable>
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
import {
  OnmsButton,
  OnmsColumn,
  OnmsIcon,
  OnmsIconButton,
  OnmsInputNumber,
  OnmsSelect,
  OnmsTable,
  type OnmsTableRowEditSaveEvent
} from '@opennms/onms-ui'
import Add from '@/components/icons/action/Add.vue'
import Delete from '@/components/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'

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

const onRowEditSave = (event: OnmsTableRowEditSaveEvent) => {
  const rras = [...props.rrdSettings.rras]
  rras[event.index] = { ...event.newData } as EditableRRA
  emit('update:rrdSettings', { ...props.rrdSettings, rras })
}
</script>

<style lang="scss" scoped>
@import '@/styles/onms-typography';
@import "@/styles/onms-tokens";

.rrd-settings-box {
  padding: 20px 0;
}

.section-header {
  @include onms-headline3;
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
      @include onms-headline4;
      color: var(--onms-secondary-text-on-surface);
    }
  }
}
</style>
