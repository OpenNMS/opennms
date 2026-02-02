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
      <div class="feather-col-6" v-for="field in [fieldPair.field1, fieldPair.field2]" :key="field.key">
        <FeatherInput
          v-if="field"
          label=""
          :class="field.scvEnabled ? 'scv-enabled-input' : ''"
          :data-test="field.dataTest"
          v-model.trim="(props.config as any)[field.key]"
          :hint="field.hint"
          :error="(props.validationErrors as any)[field.key]"
          :type="field.isNumeric ? 'number' : 'text'"
          @update:modelValue="val => handleFormInputUpdate(String(field.key), String(val ?? ''), field.isNumeric)"
        >
          <template v-if="field.scvEnabled" v-slot:post>
            <ScvInputIcon @click="() => scvButtonClick(String(field.key))"></ScvInputIcon>
          </template>
        </FeatherInput>
     </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo } from '@/types/snmpConfig'
import { FeatherInput } from '@featherds/input'
import ScvInputIcon from '@/components/SCV/ScvInputIcon.vue'

const props = defineProps<{
  fieldInfo: SnmpFieldInfo[]
  config: SnmpBaseConfiguration
  validationErrors: SnmpConfigFormErrors
}>()

const emit = defineEmits<{
  (e: 'update', config: SnmpBaseConfiguration): void
  (e: 'scv-search', value: string): void
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

const scvButtonClick = (key: string) => {
  emit('scv-search', key)
}

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
