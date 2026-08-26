<template>
  <div class="expression-editor">
    <div
      v-if="availableLabels.length"
      class="label-tokens"
    >
      <span class="tokens-caption">Available labels:</span>
      <OnmsChip
        v-for="label in availableLabels"
        :key="label"
        :label="label"
        class="token"
        :title="`Insert ${label} into the expression being edited`"
        data-test="expression-token"
        @click="insertToken(label)"
      />
    </div>
    <p
      v-else
      class="editor-empty"
    >
      Select some datasources first — an expression needs source labels to work with.
    </p>

    <OnmsTable
      v-if="expressions.length"
      :value="expressions"
      dataKey="id"
      size="small"
      tableStyle="min-width: 50rem"
      class="expression-table"
      data-test="expression-table"
    >
      <OnmsColumn
        header="Color"
        style="width: 5rem"
      >
        <template #body="{ data }">
          <input
            type="color"
            class="color-swatch"
            :value="data.color"
            :aria-label="`Color for ${data.label}`"
            :data-test="`expression-color-${data.id}`"
            @input="patch(data.id, { color: ($event.target as HTMLInputElement).value })"
          >
        </template>
      </OnmsColumn>

      <OnmsColumn
        header="Name"
        style="width: 14rem"
      >
        <template #body="{ data }">
          <FormField
            :for="`expression-name-${data.id}`"
            :error="errorFor(data.id, 'label')"
          >
            <OnmsInputText
              :id="`expression-name-${data.id}`"
              :modelValue="data.label"
              :invalid="Boolean(errorFor(data.id, 'label'))"
              :data-test="`expression-name-${data.id}`"
              @update:modelValue="value => patch(data.id, { label: (value ?? '') as string })"
            />
          </FormField>
        </template>
      </OnmsColumn>

      <OnmsColumn header="Expression">
        <template #body="{ data }">
          <FormField
            :for="`expression-value-${data.id}`"
            :error="errorFor(data.id, 'value')"
          >
            <OnmsInputText
              :id="`expression-value-${data.id}`"
              :modelValue="data.value"
              :invalid="Boolean(errorFor(data.id, 'value'))"
              placeholder="e.g. ifHCInOctets_eth0 * 8"
              :data-test="`expression-value-${data.id}`"
              @update:modelValue="value => patch(data.id, { value: (value ?? '') as string })"
              @focus="focusedId = data.id"
            />
          </FormField>
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
            :data-test="`expression-style-${data.id}`"
            @update:modelValue="value => patch(data.id, { style: value as AdhocSeriesStyle })"
          />
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
            :data-test="`expression-remove-${data.id}`"
            @click="emit('remove', data.id)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <OnmsButton
      variant="outlined"
      :disabled="!availableLabels.length"
      data-test="expression-add"
      @click="emit('add')"
    >Add expression</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import {
  OnmsButton,
  OnmsChip,
  OnmsColumn,
  OnmsIconButton,
  OnmsInputText,
  OnmsSelect,
  OnmsTable
} from '@opennms/onms-ui'
import { computed, ref } from 'vue'

import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import { AdhocExpression, AdhocGraphConfig, AdhocSeries, AdhocSeriesStyle } from '@/types/adhocGraph'
import { expressionIssues } from './utils/adhocQuery'

const props = defineProps<{
  series: AdhocSeries[]
  expressions: AdhocExpression[]
}>()

const emit = defineEmits<{
  add: []
  update: [id: string, patch: Partial<AdhocExpression>]
  remove: [id: string]
}>()

const styles: AdhocSeriesStyle[] = ['line', 'line2', 'line3', 'area', 'stack']

/** The expression input that last had focus — where a clicked token is inserted. */
const focusedId = ref('')

const availableLabels = computed<string[]>(() => props.series.map(entry => entry.label).filter(Boolean))

// Name and expression problems come from one shared validator so the toolbar's
// "can this be queried" gate and these inline messages can never disagree.
const issues = computed(() =>
  expressionIssues({ series: props.series, expressions: props.expressions } as AdhocGraphConfig))

const errorFor = (id: string, field: 'label' | 'value'): string | undefined => {
  const issue = issues.value[id]
  return issue?.field === field ? issue.message : undefined
}

const patch = (id: string, changes: Partial<AdhocExpression>) => emit('update', id, changes)

/**
 * Append a label to the expression that was last focused. Resource and attribute
 * names are long and easy to mistype, and a typo silently becomes an unresolved
 * JEXL variable, so clicking the token is the safer path.
 */
const insertToken = (label: string) => {
  const target = props.expressions.find(expression => expression.id === focusedId.value) ??
    props.expressions[props.expressions.length - 1]

  if (!target) {
    return
  }

  const separator = target.value && !/[\s(+\-*/,]$/.test(target.value) ? ' ' : ''
  patch(target.id, { value: `${target.value}${separator}${label}` })
}
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

.expression-editor {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.75rem;
}

.editor-empty {
  @include onms-body-small;
  margin: 0;
  color: var(--p-text-muted-color);
}

.label-tokens {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.35rem;

  .tokens-caption {
    @include onms-body-small;
    color: var(--p-text-muted-color);
  }

  .token {
    cursor: pointer;
  }
}

.expression-table {
  width: 100%;
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
</style>
