<template>
  <div class="snmp-config-paired-fields" v-for="fieldPair in pairedFields" :key="fieldPair.field1.key">
    <div class="feather-row">
      <div class="feather-col-6">
        <label class="label">{{ fieldPair.field1.label }}:</label>
      </div>
      <div class="feather-col-6">
        <label v-if="fieldPair.field2" class="label">{{ fieldPair.field2.label }}:</label>
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-6">
        <FeatherInput
          label=""
          :data-test="fieldPair.field1.dataTest"
          v-model.trim="(props.config as any)[fieldPair.field1.key]"
          :hint="fieldPair.field1.hint"
          :error="(props.validationErrors as any)[fieldPair.field1.key]"
          :type="fieldPair.field1.isNumeric ? 'number' : 'text'"
          @update:modelValue="val => handleFormInputUpdate(String(fieldPair.field1.key), String(val ?? ''), fieldPair.field1.isNumeric)"
        >
        </FeatherInput>
      </div>
      <div class="feather-col-6">
        <FeatherInput
          v-if="fieldPair.field2"
          label=""
          :data-test="fieldPair.field2.dataTest"
          v-model.trim="(props.config as any)[fieldPair.field2.key]"
          :hint="fieldPair.field2.hint"
          :error="(props.validationErrors as any)[fieldPair.field2.key]"
          :type="fieldPair.field2.isNumeric ? 'number' : 'text'"
          @update:modelValue="val => handleFormInputUpdate(String(fieldPair.field2.key), String(val ?? ''), fieldPair.field2.isNumeric)"
        >
        </FeatherInput>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo } from '@/types/snmpConfig';
import { FeatherInput } from '@featherds/input'

const props = defineProps<{
  fieldInfo: SnmpFieldInfo[]
  config: SnmpBaseConfiguration
  validationErrors: SnmpConfigFormErrors
}>()

const emit = defineEmits<{
  (e: 'update', config: SnmpBaseConfiguration): void
}>()

const createPairedFields = (fields: any[]) => {
  const pairs: { field1: any, field2?: any }[] = []

  for (let i = 0; i < fields.length; i += 2) {
    pairs.push({ field1: fields[i], field2: i < fields.length - 1 ? fields[i + 1] : undefined })
  }

  return pairs
}

const pairedFields = computed(() => {
  return createPairedFields(props.fieldInfo)
})

const handleFormInputUpdate = (key: string, val: string, isNumeric?: boolean) => {
  const updatedConfig = {
    ...(props.config as any),
    [key]: isNumeric ? Number(val) : val
  }

  emit('update', updatedConfig)
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';

.snmp-config-paired-fields {
  .label {
    font-weight: 600;
  }

  .feather-row {
    margin-bottom: 0.5rem;
  }
}
</style>
