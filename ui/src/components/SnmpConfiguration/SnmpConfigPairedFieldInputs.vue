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

        <div class="feather-row" v-if="!field.isSelect && field.scvEnabled">
          <div class="feather-col-9">
            <FeatherInput
              label=""
              class="scv-enabled-input"
              :data-test="field.dataTest"
              v-model.trim="(props.config as any)[field.key]"
              :hint="field.hint"
              :error="(props.validationErrors as any)[field.key]"
              :type="field.isNumeric ? 'number' : 'text'"
              @update:modelValue="val => handleFormInputUpdate(String(field.key), String(val ?? ''), field.isNumeric)"
            >
            </FeatherInput>
          </div>
          <div class="feather-col-3">
            <div class="scv-icon-container">
              <ScvInputIcon @click="() => scvButtonClick(String(field.key))"></ScvInputIcon>
            </div>
          </div>
        </div>

        <FeatherInput
          v-if="!field.scvEnabled && !field.isSelect"
          label=""
          :data-test="field.dataTest"
          v-model.trim="(props.config as any)[field.key]"
          :hint="field.hint"
          :error="(props.validationErrors as any)[field.key]"
          :type="field.isNumeric ? 'number' : 'text'"
          @update:modelValue="val => handleFormInputUpdate(String(field.key), String(val ?? ''), field.isNumeric)"
        >
        </FeatherInput>

        <FeatherSelect
          v-if="field.isSelect"
          :label="field.label"
          :data-test="field.dataTest"
          :hint="field.hint"
          :options="field.selectOptions"
          :modelValue="(selectModel as any)[field.key]"
          @update:modelValue="(val: any) => handleFormSelectUpdate(String(field.key), val, field.isNumeric)"
        >
        </FeatherSelect>
     </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo } from '@/types/snmpConfig'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect } from '@featherds/select'
import ScvInputIcon from '@/components/SCV/ScvInputIcon.vue'

const props = defineProps<{
  fieldInfo: SnmpFieldInfo[]
  config: SnmpBaseConfiguration
  validationErrors: SnmpConfigFormErrors
}>()

// key: ISelectItemType for FeatherSelect component models
const selectModel = ref<Record<string, any>>({})

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

const handleFormSelectUpdate = (key: string, val?: any, isNumeric?: boolean) => {
  // get the newly selected option's value as either text or numeric
  const value = String(`${val?._value ?? ''}`)
  const numericValue = isNumeric ? Number(value) : 0

  const field = props.fieldInfo.find(f => f.key === key)
  const selectedOption = field?.selectOptions?.find(option => option._value === value)

  if (value?.length > 0) {
    // update the FeatherSelect model value
    selectModel.value = {
      ...selectModel.value,
      [key]: selectedOption
    }

    // update the config and emit
    const updatedConfig = {
      ...(props.config as any),
      [key]: isNumeric ? numericValue : value
    }

    emit('update', updatedConfig)
  }
}

const updateSelectValues = () => {
  let newModel = {}

  props.fieldInfo?.filter(field => field.isSelect).forEach(field => {
    // Get the string value from the current config for the current field
    const value = String((props.config as any)[field.key])

    // from the field's selectOptions, find the option that matches the current value
    const selectedOption = field.selectOptions?.find(option => option._value === value)

    // update the model for the corresponding FeatherSelect
    newModel = {
      ...newModel,
      [field.key]: {
        ...selectedOption
      }
    }
  })

  selectModel.value = {
    ...newModel
  }
}

watch([props], () => {
  updateSelectValues()
}, { deep: true })

onMounted(() => {
  updateSelectValues()
})
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

  .scv-icon-container {
    padding: 0.2em;
    margin-left: -1em;
  }
}
</style>
