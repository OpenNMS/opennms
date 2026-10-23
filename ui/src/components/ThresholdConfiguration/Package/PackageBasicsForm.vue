<template>
  <TableCard class="package-basics-card">
    <div class="basics-form">
      <FormField label="Name" for="package-name" required :error="errors.name">
        <OnmsInputText
          inputId="package-name"
          data-test="package-name"
          :modelValue="modelValue.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="update('name', $event)"
        />
      </FormField>

      <FormField
        label="Filter"
        for="package-filter"
        required
        :error="errors.filter"
        :info="THRESHOLD_FIELD_HINTS.packageFilter"
      >
        <OnmsInputText
          inputId="package-filter"
          data-test="package-filter"
          :modelValue="modelValue.filter"
          :invalid="!!errors.filter"
          fluid
          @update:modelValue="update('filter', $event)"
        />
      </FormField>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import { THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import type { ThreshdPackage, ThreshdPackageErrors } from '@/types/thresholdConfig'
import { OnmsInputText } from '@opennms/onms-ui'

const props = defineProps<{
  modelValue: ThreshdPackage
  errors: ThreshdPackageErrors
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ThreshdPackage]
}>()

const update = (field: string, value: unknown) => {
  emit('update:modelValue', { ...props.modelValue, [field]: String(value ?? '') })
}
</script>

<style lang="scss" scoped>
// Padded like the event configuration table and framed like the SNMP configuration tabs.
.package-basics-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.basics-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr);
  gap: 0.75em 1.25em;

  @media (max-width: 768px) {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
