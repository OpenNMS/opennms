<template>
  <OnmsTable
    :value="series"
    dataKey="key"
    size="small"
    stripedRows
    tableStyle="min-width: 60rem"
    data-test="series-grid"
  >
    <template #empty>
      <p
        class="series-empty"
        data-test="series-empty"
      >
        Pick one or more datasources above and they will appear here, ready to label,
        recolour and reference from an expression.
      </p>
    </template>

    <OnmsColumn
      header="Colour"
      style="width: 5rem"
    >
      <template #body="{ data }">
        <input
          type="color"
          class="color-swatch"
          :value="data.color"
          :aria-label="`Colour for ${data.label}`"
          :data-test="`series-color-${data.key}`"
          @input="patch(data.key, { color: ($event.target as HTMLInputElement).value })"
        >
      </template>
    </OnmsColumn>

    <OnmsColumn header="Label">
      <template #body="{ data }">
        <FormField
          :for="`series-label-${data.key}`"
          :error="labelErrors[data.key]"
        >
          <OnmsInputText
            :id="`series-label-${data.key}`"
            :modelValue="data.label"
            :aria-label="`Label for ${data.attribute}`"
            :invalid="Boolean(labelErrors[data.key])"
            :data-test="`series-label-${data.key}`"
            @update:modelValue="value => patch(data.key, { label: (value ?? '') as string })"
          />
        </FormField>
      </template>
    </OnmsColumn>

    <OnmsColumn header="Source">
      <template #body="{ data }">
        <span class="source-attribute">{{ data.attribute }}</span>
        <span
          class="source-resource"
          :title="data.resourceId"
        >{{ data.resourceId }}</span>
      </template>
    </OnmsColumn>

    <OnmsColumn
      header="Consolidation"
      style="width: 10rem"
    >
      <template #body="{ data }">
        <OnmsSelect
          :modelValue="data.aggregation"
          :options="aggregations"
          :aria-label="`Consolidation function for ${data.label}`"
          :data-test="`series-aggregation-${data.key}`"
          @update:modelValue="value => patch(data.key, { aggregation: value as ConsolidationFunctionType })"
        />
      </template>
    </OnmsColumn>

    <OnmsColumn
      header="Style"
      style="width: 10rem"
    >
      <template #body="{ data }">
        <OnmsSelect
          :modelValue="data.style"
          :options="styles"
          :aria-label="`Style for ${data.label}`"
          :data-test="`series-style-${data.key}`"
          @update:modelValue="value => patch(data.key, { style: value as AdhocSeriesStyle })"
        />
      </template>
    </OnmsColumn>

    <OnmsColumn
      header="Hide raw"
      style="width: 10rem"
    >
      <template #body="{ data }">
        <OnmsToggleSwitch
          :modelValue="data.hidden"
          :inputId="`series-hidden-${data.key}`"
          :data-test="`series-hidden-${data.key}`"
          @update:modelValue="value => patch(data.key, { hidden: value })"
        />
        <small
          v-if="data.hidden && !consumedKeys.has(data.key)"
          class="hide-raw-hint"
        >Not used by any expression — still plotted.</small>
      </template>
    </OnmsColumn>

    <OnmsColumn
      header="Actions"
      style="width: 6rem"
    >
      <template #body="{ data }">
        <OnmsIconButton
          variant="text"
          :icon="DeleteIcon"
          :title="`Remove ${data.label}`"
          :data-test="`series-remove-${data.key}`"
          @click="emit('remove', data.key)"
        />
      </template>
    </OnmsColumn>
  </OnmsTable>
</template>

<script setup lang="ts">
import {
  OnmsColumn,
  OnmsIconButton,
  OnmsInputText,
  OnmsSelect,
  OnmsTable,
  OnmsToggleSwitch
} from '@opennms/onms-ui'
import { computed } from 'vue'

import DeleteIcon from '@/components/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import { AdhocExpression, AdhocGraphConfig, AdhocSeries, AdhocSeriesStyle } from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { expressionReferences, seriesLabelIssues } from './utils/adhocQuery'

const props = defineProps<{
  series: AdhocSeries[]
  expressions: AdhocExpression[]
}>()

const emit = defineEmits<{
  update: [key: string, patch: Partial<AdhocSeries>]
  remove: [key: string]
}>()

const aggregations = Object.values(ConsolidationFunctionType)
const styles: AdhocSeriesStyle[] = ['line', 'line2', 'line3', 'area', 'stack']

/** Series labels an expression actually consumes — "hide raw" only bites for these. */
const consumedKeys = computed<Set<string>>(() => {
  const consumed = new Set<string>()

  for (const entry of props.series) {
    if (props.expressions.some(expression => expressionReferences(expression.value, entry.label))) {
      consumed.add(entry.key)
    }
  }

  return consumed
})

const labelErrors = computed<Record<string, string>>(() =>
  seriesLabelIssues({ series: props.series, expressions: props.expressions } as AdhocGraphConfig))

const patch = (key: string, changes: Partial<AdhocSeries>) => emit('update', key, changes)
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

.series-empty {
  @include onms-body-small;
  margin: 0;
  color: var(--p-text-muted-color);
}

.color-swatch {
  width: 2.25rem;
  height: 2.25rem;
  padding: 0;
  border: 1px solid var(--p-content-border-color);
  border-radius: var(--p-content-border-radius);
  background: none;
  cursor: pointer;
}

.source-attribute {
  display: block;
  font-weight: 700;
  overflow-wrap: anywhere;
}

.source-resource {
  @include onms-body-small;
  display: block;
  color: var(--p-text-muted-color);
  overflow-wrap: anywhere;
}

.hide-raw-hint {
  @include onms-body-small;
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>
