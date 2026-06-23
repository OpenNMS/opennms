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
        <label class="label">Alarm Type:</label>
        <div class="spacer"></div>
        <IftaLabel>
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
          <label :for="alarmTypeId">Alarm Type</label>
        </IftaLabel>
        <small
          v-if="errors.alarmType"
          class="field-error"
        >
          {{ errors.alarmType }}
        </small>
        <small
          v-else
          class="field-hint"
        >
          Select the alarm type.
        </small>
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
      <div class="label">Alarm Reduction Key:</div>
      <div class="spacer"></div>
      <IftaLabel>
        <InputText
          :id="reductionKeyId"
          :modelValue="alarmReductionKey"
          data-test="alarm-reduction-key"
          :invalid="!!errors?.reductionKey"
          fluid
          @update:model-value="$emit('setAlarmData', 'reductionKey', $event)"
        />
        <label :for="reductionKeyId">Alarm Reduction Key</label>
      </IftaLabel>
      <small
        v-if="errors?.reductionKey"
        class="field-error"
      >
        {{ errors.reductionKey }}
      </small>
      <small
        v-else
        class="field-hint"
      >
        Provide the reduction key for the alarm.
      </small>
      <div class="spacer"></div>
      <div class="label">Alarm Clear Key:</div>
      <div class="spacer"></div>
      <IftaLabel>
        <InputText
          :id="clearKeyId"
          :modelValue="alarmClearKey"
          data-test="alarm-clear-key"
          :invalid="!!errors.clearKey"
          fluid
          @update:model-value="$emit('setAlarmData', 'clearKey', $event)"
        />
        <label :for="clearKeyId">Alarm Clear Key</label>
      </IftaLabel>
      <small
        v-if="errors.clearKey"
        class="field-error"
      >
        {{ errors.clearKey }}
      </small>
      <small
        v-else
        class="field-hint"
      >
        Provide the clear key for the alarm.
      </small>
      <div class="spacer"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, useId, watch } from 'vue'

import { EventFormErrors } from '@/types/eventConfig'
import { ISelectItemType } from '@featherds/select'
import Checkbox from 'primevue/checkbox'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
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
const enableAutoClean = ref(false)
const alarmReductionKey = ref('')
const alarmClearKey = ref('')
const selectedEventAlarmType = ref<ISelectItemType>({ _text: '', _value: '' })

watch(() => props, (newVal) => {
  enableAlarmData.value = newVal.addAlarmData
  alarmReductionKey.value = newVal.reductionKey
  enableAutoClean.value = newVal.autoClean
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

  .field-error {
    display: block;
    margin-top: 0.25rem;
    color: var(--p-red-500);
  }

  .field-hint {
    display: block;
    margin-top: 0.25rem;
    color: var(--p-text-muted-color);
  }
}
</style>
