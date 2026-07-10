<template>
  <div class="alarm-data-info">
    <div>
      <h3>Alarm Information</h3>
    </div>
    <div class="spacer"></div>
    <div class="alarm-check">
      <Checkbox
        :modelValue="enableAlarmData"
        binary
        inputId="add-alarm-data-checkbox"
        @update:model-value="$emit('setAlarmData', 'addAlarmData', $event)"
      />
      <label for="add-alarm-data-checkbox">Add Alarm Data</label>
    </div>
    <div class="spacer"></div>
    <div v-if="enableAlarmData">
      <div class="dropdown">
        <FormField
          label="Alarm Type"
          :for="alarmTypeId"
          :error="errors.alarmType"
          hint="Select the alarm type."
        >
          <Select
            :inputId="alarmTypeId"
            :options="AlarmTypeOptions"
            optionLabel="_text"
            showClear
            data-test="alarm-type"
            :invalid="!!errors.alarmType"
            :modelValue="selectedEventAlarmType?._value ? selectedEventAlarmType : null"
            @update:model-value="$emit('setAlarmData', 'alarmType', $event)"
            fluid
          />
        </FormField>
      </div>
      <div class="spacer"></div>
      <div class="alarm-check">
        <Checkbox
          :modelValue="autoClean"
          binary
          inputId="auto-clean-checkbox"
          @update:model-value="$emit('setAlarmData', 'autoClean', $event)"
        />
        <label for="auto-clean-checkbox">Auto Clean</label>
      </div>
      <div class="spacer"></div>
      <FormField
        label="Alarm Reduction Key"
        :for="reductionKeyId"
        :error="errors?.reductionKey"
        hint="Provide the reduction key for the alarm."
      >
        <InputText
          :id="reductionKeyId"
          :modelValue="alarmReductionKey"
          data-test="alarm-reduction-key"
          :invalid="!!errors?.reductionKey"
          fluid
          @update:model-value="$emit('setAlarmData', 'reductionKey', $event)"
        />
      </FormField>
      <div class="spacer"></div>
      <FormField
        label="Alarm Clear Key"
        :for="clearKeyId"
        :error="errors.clearKey"
        hint="Provide the clear key for the alarm."
      >
        <InputText
          :id="clearKeyId"
          :modelValue="alarmClearKey"
          data-test="alarm-clear-key"
          :invalid="!!errors.clearKey"
          fluid
          @update:model-value="$emit('setAlarmData', 'clearKey', $event)"
        />
      </FormField>
      <div class="spacer"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, useId, watch } from 'vue'

import { EventFormErrors } from '@/types/eventConfig'
import { ISelectItemType } from '@featherds/select'
import Checkbox from 'primevue/checkbox'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import FormField from '@/components/Common/FormField.vue'
import { AlarmTypeOptions } from './constants'

defineEmits<{ (e: 'setAlarmData', key: string, value: any): void }>()
const props = defineProps<{
  addAlarmData: boolean,
  reductionKey: string,
  alarmType: ISelectItemType
  autoClean: boolean,
  clearKey: string,
  errors: EventFormErrors
}>()
const alarmTypeId = useId()
const reductionKeyId = useId()
const clearKeyId = useId()
const enableAlarmData = ref(false)
const alarmReductionKey = ref('')
const alarmClearKey = ref('')
const selectedEventAlarmType = ref<ISelectItemType>({ _text: '', _value: '' })

watch(() => props, (newVal) => {
  enableAlarmData.value = newVal.addAlarmData
  alarmReductionKey.value = newVal.reductionKey
  alarmClearKey.value = newVal.clearKey
  selectedEventAlarmType.value = {
    _text: newVal.alarmType._text,
    _value: newVal.alarmType._value
  }
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.alarm-data-info {
  .label {
    font-weight: 600;
  }

  .spacer {
    min-height: 0.5em;
  }

  .dropdown {
    width: 50%;
  }

  .alarm-check {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
}
</style>
