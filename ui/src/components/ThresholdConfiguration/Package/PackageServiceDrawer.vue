<template>
  <OnmsDrawer
    :visible="store.serviceDrawer.visible"
    position="right"
    width="40rem"
    :header="isCreate ? 'New service' : 'Edit service'"
    data-test="package-service-drawer"
    @update:visible="onVisibleChange"
  >
    <div class="drawer-body">
      <FormField label="Service" for="package-service-name" required :error="errors.name">
        <OnmsInputText
          inputId="package-service-name"
          data-test="package-service-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>

      <FormField
        label="Interval (ms)"
        for="package-service-interval"
        required
        :error="errors.interval"
        :hint="THRESHOLD_FIELD_HINTS.serviceInterval"
      >
        <OnmsInputNumber
          inputId="package-service-interval"
          data-test="package-service-interval"
          :modelValue="draft.interval"
          :invalid="!!errors.interval"
          @update:modelValue="onIntervalChange"
        />
      </FormField>

      <FormField
        label="Threshold group"
        for="package-service-group"
        :error="errors.thresholdingGroup"
        :hint="THRESHOLD_FIELD_HINTS.thresholdingGroup"
      >
        <OnmsSelect
          inputId="package-service-group"
          data-test="package-service-group"
          optionLabel="_text"
          optionValue="_value"
          showClear
          :options="groupOptions"
          :modelValue="thresholdingGroup"
          :invalid="!!errors.thresholdingGroup"
          @update:modelValue="onThresholdingGroupChange"
        />
      </FormField>

      <FormField label="Status" for="package-service-status">
        <OnmsSelect
          inputId="package-service-status"
          data-test="package-service-status"
          optionLabel="_text"
          optionValue="_value"
          :options="SERVICE_STATUS_OPTIONS"
          :modelValue="draft.status"
          @update:modelValue="draft.status = String($event ?? ThreshdServiceStatus.On)"
        />
      </FormField>

      <FormField label="User defined" for="package-service-user-defined">
        <OnmsToggleSwitch
          inputId="package-service-user-defined"
          data-test="package-service-user-defined"
          :modelValue="!!draft.userDefined"
          @update:modelValue="draft.userDefined = $event"
        />
      </FormField>

      <ParameterListEditor
        :modelValue="draft.parameters"
        idPrefix="package-service-parameter"
        :lockedKeys="[THRESHOLDING_GROUP_PARAMETER]"
        @update:modelValue="draft.parameters = $event"
      />
    </div>

    <template #footer>
      <div class="drawer-footer">
        <OnmsButton variant="text" data-test="package-service-cancel" @click="store.closeServiceDrawer()">
          Cancel
        </OnmsButton>
        <OnmsButton data-test="package-service-save" :disabled="hasErrors(errors)" @click="onSave">Save</OnmsButton>
      </div>
    </template>
  </OnmsDrawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import ParameterListEditor from '@/components/ThresholdConfiguration/Common/ParameterListEditor.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import {
  SERVICE_STATUS_OPTIONS,
  THRESHOLDING_GROUP_PARAMETER,
  ThreshdServiceStatus,
  hasErrors,
  validateThreshdService
} from '@/lib/thresholdValidator'
import { getDefaultThreshdService, useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { CreateEditMode, type ISelectItemType } from '@/types'
import type { ThreshdService } from '@/types/thresholdConfig'
import { cloneDeep } from 'lodash'
import { OnmsButton, OnmsDrawer, OnmsInputNumber, OnmsInputText, OnmsSelect, OnmsToggleSwitch } from '@opennms/onms-ui'

const store = useThreshdConfigurationStore()
const groupStore = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const draft = ref<ThreshdService>(getDefaultThreshdService())

const isCreate = computed(() => store.serviceDrawer.mode === CreateEditMode.Create)

const groupOptions = computed<ISelectItemType[]>(() =>
  groupStore.groupNames.map(name => ({ _text: name, _value: name }))
)

const thresholdingGroup = computed(
  () => draft.value.parameters.find(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER)?.value ?? ''
)

const errors = computed(() => validateThreshdService(draft.value, groupStore.groupNames))

watch(
  () => store.serviceDrawer.visible,
  (visible) => {
    if (!visible) {
      return
    }

    const { mode, index } = store.serviceDrawer
    const existing = store.currentPackage?.services?.[index]

    draft.value = mode === CreateEditMode.Edit && existing ? cloneDeep(existing) : getDefaultThreshdService()
  },
  { immediate: true }
)

const onVisibleChange = (visible: boolean) => {
  if (!visible) {
    store.closeServiceDrawer()
  }
}

// OnmsInputNumber emits number | null; NaN would make the validation message unhelpful.
const onIntervalChange = (value: number | null) => {
  draft.value.interval = value === null ? Number.NaN : value
}

const onThresholdingGroupChange = (value: unknown) => {
  const groupName = String(value ?? '')
  const existing = draft.value.parameters.find(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER)

  if (existing) {
    existing.value = groupName
  } else {
    draft.value.parameters.push({ key: THRESHOLDING_GROUP_PARAMETER, value: groupName })
  }
}

const onSave = async () => {
  const { mode, index } = store.serviceDrawer
  store.closeServiceDrawer()

  store.upsertService(mode === CreateEditMode.Edit ? index : null, draft.value)

  const result = await store.saveCurrentPackage()

  showSnackBar({ msg: result.success ? 'Service saved.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 0.75em;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5em;
}
</style>
