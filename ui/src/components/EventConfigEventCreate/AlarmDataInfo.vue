<template>
  <div class="alarm-data-info">
    <div>
      <h3>Alarm Information</h3>
    </div>
    <div class="spacer"></div>
    <div class="alarm-check">
      <FeatherCheckbox
        :model-value="enableAlarmData"
        @update:model-value="$emit('setAlarmData', 'addAlarmData', $event)"
      >
        Add Alarm Data
      </FeatherCheckbox>
    </div>
    <div class="spacer"></div>
    <div v-if="enableAlarmData">
      <div class="dropdown">
        <label class="label">Alarm Type:</label>
        <div class="spacer"></div>
        <FeatherSelect
          label="Alarm Type"
          hint="Select the alarm type."
          data-test="alarm-type"
          :error="errors.alarmType"
          :options="AlarmTypeOptions"
          :model-value="selectedEventAlarmType"
          @update:model-value="$emit('setAlarmData', 'alarmType', $event)"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
      </div>
      <div class="spacer"></div>
      <FeatherCheckbox
        :model-value="autoClean"
        @update:model-value="$emit('setAlarmData', 'autoClean', $event)"
      >
        Auto Clean
      </FeatherCheckbox>
      <div class="spacer"></div>
      <div class="label">Alarm Reduction Key:</div>
      <div class="spacer"></div>
      <FeatherInput
        label=""
        hint="Provide the reduction key for the alarm."
        :model-value="alarmReductionKey"
        data-test="alarm-reduction-key"
        @update:model-value="$emit('setAlarmData', 'reductionKey', $event)"
        :error="errors?.reductionKey"
      />
      <div class="spacer"></div>
      <div class="label">Alarm Clear Key:</div>
      <div class="spacer"></div>
      <FeatherInput
        label=""
        hint="Provide the clear key for the alarm."
        :model-value="alarmClearKey"
        data-test="alarm-clear-key"
        @update:model-value="$emit('setAlarmData', 'clearKey', $event)"
        :error="errors.clearKey"
      />
      <div class="spacer"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { EventFormErrors } from '@/types/eventConfig'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
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
}
</style>

