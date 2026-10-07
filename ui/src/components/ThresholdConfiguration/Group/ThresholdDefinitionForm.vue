<template>
  <div class="threshold-definition-form">
    <div class="field-grid">
      <FormField label="Type" for="threshold-type" required :error="errors.type" :hint="typeHint">
        <OnmsSelect
          inputId="threshold-type"
          data-test="threshold-type"
          optionLabel="_text"
          optionValue="_value"
          :options="THRESHOLD_TYPE_OPTIONS"
          :modelValue="modelValue.type"
          :invalid="!!errors.type"
          @update:modelValue="update('type', $event)"
        />
      </FormField>

      <FormField
        v-if="kind === ThresholdDefinitionKind.Threshold"
        label="Datasource"
        for="threshold-ds-name"
        required
        :error="errors.dsName"
        :hint="THRESHOLD_FIELD_HINTS.dsName"
      >
        <OnmsInputText
          inputId="threshold-ds-name"
          data-test="threshold-ds-name"
          :modelValue="(modelValue as Threshold).dsName"
          :invalid="!!errors.dsName"
          fluid
          @update:modelValue="update('dsName', $event)"
        />
      </FormField>

      <FormField
        v-else
        label="Expression"
        for="threshold-expression"
        required
        :error="errors.expression"
        :hint="THRESHOLD_FIELD_HINTS.expression"
      >
        <OnmsInputText
          inputId="threshold-expression"
          data-test="threshold-expression"
          :modelValue="(modelValue as Expression).expression"
          :invalid="!!errors.expression"
          fluid
          @update:modelValue="update('expression', $event)"
        />
      </FormField>

      <FormField
        label="Datasource type"
        for="threshold-ds-type"
        required
        :error="errors.dsType"
        :hint="THRESHOLD_FIELD_HINTS.dsType"
      >
        <OnmsSelect
          inputId="threshold-ds-type"
          data-test="threshold-ds-type"
          optionLabel="label"
          optionValue="name"
          :options="dsTypes"
          :modelValue="modelValue.dsType"
          :invalid="!!errors.dsType"
          @update:modelValue="update('dsType', $event)"
        />
      </FormField>

      <FormField label="Datasource label" for="threshold-ds-label" :hint="THRESHOLD_FIELD_HINTS.dsLabel">
        <OnmsInputText
          inputId="threshold-ds-label"
          data-test="threshold-ds-label"
          :modelValue="modelValue.dsLabel"
          fluid
          @update:modelValue="update('dsLabel', $event)"
        />
      </FormField>

      <FormField
        v-if="kind === ThresholdDefinitionKind.Expression"
        label="Expression label"
        for="threshold-expr-label"
        :hint="THRESHOLD_FIELD_HINTS.exprLabel"
      >
        <OnmsInputText
          inputId="threshold-expr-label"
          data-test="threshold-expr-label"
          :modelValue="modelValue.exprLabel"
          fluid
          @update:modelValue="update('exprLabel', $event)"
        />
      </FormField>

      <FormField label="Value" for="threshold-value" required :error="errors.value" :hint="THRESHOLD_FIELD_HINTS.value">
        <OnmsInputText
          inputId="threshold-value"
          data-test="threshold-value"
          :modelValue="modelValue.value"
          :invalid="!!errors.value"
          fluid
          @update:modelValue="update('value', $event)"
        />
      </FormField>

      <FormField label="Re-arm" for="threshold-rearm" required :error="errors.rearm" :hint="rearmHint">
        <OnmsInputText
          inputId="threshold-rearm"
          data-test="threshold-rearm"
          :modelValue="modelValue.rearm"
          :invalid="!!errors.rearm"
          fluid
          @update:modelValue="update('rearm', $event)"
        />
      </FormField>

      <FormField label="Trigger" for="threshold-trigger" required :error="errors.trigger" :hint="triggerHint">
        <OnmsInputText
          inputId="threshold-trigger"
          data-test="threshold-trigger"
          :modelValue="modelValue.trigger"
          :invalid="!!errors.trigger"
          fluid
          @update:modelValue="update('trigger', $event)"
        />
      </FormField>

      <FormField label="Description" for="threshold-description" class="span-two">
        <OnmsInputText
          inputId="threshold-description"
          data-test="threshold-description"
          :modelValue="modelValue.description"
          fluid
          @update:modelValue="update('description', $event)"
        />
      </FormField>

      <FormField label="Triggered UEI" for="threshold-triggered-uei" :hint="THRESHOLD_FIELD_HINTS.triggeredUEI">
        <OnmsInputText
          inputId="threshold-triggered-uei"
          data-test="threshold-triggered-uei"
          :modelValue="modelValue.triggeredUEI"
          fluid
          @update:modelValue="update('triggeredUEI', $event)"
        />
      </FormField>

      <FormField
        label="Re-armed UEI"
        for="threshold-rearmed-uei"
        :hint="rearmedUeiHint"
      >
        <OnmsInputText
          inputId="threshold-rearmed-uei"
          data-test="threshold-rearmed-uei"
          :modelValue="modelValue.rearmedUEI"
          :disabled="!supportsRearmedUei"
          fluid
          @update:modelValue="update('rearmedUEI', $event)"
        />
      </FormField>

      <FormField
        label="Relaxed"
        for="threshold-relaxed"
        hint="Evaluate the threshold even when some referenced values are unknown."
      >
        <OnmsToggleSwitch
          inputId="threshold-relaxed"
          data-test="threshold-relaxed"
          :modelValue="!!modelValue.relaxed"
          @update:modelValue="update('relaxed', $event)"
        />
      </FormField>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import { THRESHOLD_FIELD_HINTS, THRESHOLD_TYPE_HELP } from '@/lib/thresholdHelpText'
import { THRESHOLD_TYPE_OPTIONS, ThresholdDefinitionKind, ThresholdType } from '@/lib/thresholdValidator'
import type {
  Expression,
  Threshold,
  ThresholdDefinition,
  ThresholdDefinitionErrors,
  ThresholdDsType
} from '@/types/thresholdConfig'
import { OnmsInputText, OnmsSelect, OnmsToggleSwitch } from '@opennms/onms-ui'

const props = defineProps<{
  modelValue: ThresholdDefinition
  kind: ThresholdDefinitionKind
  errors: ThresholdDefinitionErrors
  dsTypes: ThresholdDsType[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ThresholdDefinition]
}>()

// Types that never re-arm, so the daemon ignores a re-armed UEI and the server stores it as null anyway.
const NON_REARMING_TYPES: string[] = [ThresholdType.RelativeChange, ThresholdType.AbsoluteChange]

const supportsRearmedUei = computed(() => !NON_REARMING_TYPES.includes(props.modelValue.type))

// One sentence at the moment it matters, rather than the five-paragraph wall the JSP editor showed.
const typeHint = computed(() => THRESHOLD_TYPE_HELP[props.modelValue.type as ThresholdType] ?? undefined)

const rearmHint = computed(() =>
  props.modelValue.type === ThresholdType.RelativeChange
    ? 'Not used for relativeChange thresholds, but still required by the schema.'
    : THRESHOLD_FIELD_HINTS.rearm
)

const triggerHint = computed(() =>
  props.modelValue.type === ThresholdType.RelativeChange
    ? 'Not used for relativeChange thresholds, but still required by the schema.'
    : THRESHOLD_FIELD_HINTS.trigger
)

const rearmedUeiHint = computed(() =>
  supportsRearmedUei.value
    ? THRESHOLD_FIELD_HINTS.rearmedUEI
    : `A ${props.modelValue.type} threshold never re-arms, so this is ignored.`
)

const update = (field: string, value: unknown) => {
  emit('update:modelValue', { ...props.modelValue, [field]: value } as ThresholdDefinition)
}
</script>

<style lang="scss" scoped>
.threshold-definition-form {
  .field-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.75em 1.25em;
  }

  .span-two {
    grid-column: span 2;
  }

  @media (max-width: 768px) {
    .field-grid {
      grid-template-columns: minmax(0, 1fr);
    }

    .span-two {
      grid-column: span 1;
    }
  }
}
</style>
