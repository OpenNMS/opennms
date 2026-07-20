<template>
  <div class="snmp-config-paired-fields" v-for="fieldPair in pairedFields" :key="fieldPair.field1.key">
    <div class="onms-row">
      <div class="onms-col-6" v-for="field in [fieldPair.field1, fieldPair.field2]" :key="field.key">

        <!-- select -->
        <FormField
          v-if="field.isSelect"
          :label="field.label"
          :for="fieldId(field.key)"
          :error="(props.validationErrors as any)[field.key]"
          :hint="field.hint"
        >
          <PSelect
            :inputId="fieldId(field.key)"
            class="paired-input"
            :data-test="field.dataTest"
            optionLabel="_text"
            :disabled="field.disabled"
            :options="field.selectOptions"
            :modelValue="(selectModel as any)[field.key]"
            @update:modelValue="(val: any) => handleFormSelectUpdate(String(field.key), val, field.isNumeric)"
          />
        </FormField>

        <!-- SCV-enabled text input with vault search icon -->
        <div class="scv-input-row" v-else-if="field.scvEnabled">
          <FormField
            class="scv-input-grow"
            :label="field.label"
            :for="fieldId(field.key)"
            :error="(props.validationErrors as any)[field.key]"
            :hint="field.hint"
          >
            <PInputText
              :id="fieldId(field.key)"
              class="paired-input scv-enabled-input"
              :data-test="field.dataTest"
              :disabled="field.disabled"
              v-model.trim="(props.config as any)[field.key]"
              :invalid="!!(props.validationErrors as any)[field.key]"
              @update:modelValue="val => handleFormInputUpdate(String(field.key), String(val ?? ''), field.isNumeric)"
            />
          </FormField>
          <div class="scv-icon-container">
            <ScvInputIcon
              :disabled="field.disabled"
              @click="() => scvButtonClick(String(field.key))"
            ></ScvInputIcon>
          </div>
        </div>

        <!-- numeric input -->
        <FormField
          v-else-if="field.isNumeric"
          :label="field.label"
          :for="fieldId(field.key)"
          :error="(props.validationErrors as any)[field.key]"
          :hint="field.hint"
        >
          <PInputNumber
            :inputId="fieldId(field.key)"
            class="paired-input"
            :inputProps="{ 'data-test': field.dataTest }"
            :disabled="field.disabled"
            :modelValue="(props.config as any)[field.key]"
            :useGrouping="false"
            :invalid="!!(props.validationErrors as any)[field.key]"
            @update:modelValue="val => handleFormInputUpdate(String(field.key), String(val ?? ''), true)"
          />
        </FormField>

        <!-- plain text input -->
        <FormField
          v-else
          :label="field.label"
          :for="fieldId(field.key)"
          :error="(props.validationErrors as any)[field.key]"
          :hint="field.hint"
        >
          <PInputText
            :id="fieldId(field.key)"
            class="paired-input"
            :data-test="field.dataTest"
            :disabled="field.disabled"
            v-model.trim="(props.config as any)[field.key]"
            :invalid="!!(props.validationErrors as any)[field.key]"
            @update:modelValue="val => handleFormInputUpdate(String(field.key), String(val ?? ''), field.isNumeric)"
          />
        </FormField>
     </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, useId, watch } from 'vue'

import { SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo } from '@/types/snmpConfig'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import FormField from '@/components/Common/FormField.vue'
import ScvInputIcon from '@/components/SCV/ScvInputIcon.vue'

const PInputText = InputText
const PInputNumber = InputNumber
const PSelect = Select

// Unique per-instance prefix so label `for`/input `id` pairs don't collide
// across the several PairedFieldInputs instances (and tab panels) that PrimeVue
// keeps mounted in the DOM at once.
const uid = useId()
const fieldId = (key: string | number) => `${uid}-${key}`

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

  props.fieldInfo?.filter(field => field.isSelect).forEach((field) => {
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
.snmp-config-paired-fields {
  .onms-row {
    margin-bottom: 0.75rem;
  }

  .paired-input {
    width: 100%;

    :deep(.p-inputtext) {
      width: 100%;
    }
  }

  // SCV-enabled field: input grows, vault icon sits beside it, centered against
  // the taller FormField input
  .scv-input-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    .scv-input-grow {
      flex: 1;
    }
  }

  .scv-icon-container {
    padding: 0.2em;
  }
}
</style>
