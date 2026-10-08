<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New Service' : 'Edit Service Details'"
    sectionTitle="Basic Information"
    :saveLabel="isCreate ? 'Create Service' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
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
    <div class="spacer"></div>
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
    <div class="spacer"></div>
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
    <div class="spacer"></div>
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
    <div class="spacer"></div>
    <FormField label="User defined" for="package-service-user-defined">
      <OnmsToggleSwitch
        inputId="package-service-user-defined"
        data-test="package-service-user-defined"
        :modelValue="!!draft.userDefined"
        @update:modelValue="draft.userDefined = $event"
      />
    </FormField>
    <div class="spacer"></div>
    <ParameterListEditor
      :modelValue="draft.parameters"
      idPrefix="package-service-parameter"
      :lockedKeys="[THRESHOLDING_GROUP_PARAMETER]"
      @update:modelValue="draft.parameters = $event"
    />
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import ParameterListEditor from '@/components/ThresholdConfiguration/Common/ParameterListEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import { indexFromParam, isPackagePageOf, nameFromParam, packagePath } from '@/lib/thresholdRoutes'
import {
  SERVICE_STATUS_OPTIONS,
  THRESHOLDING_GROUP_PARAMETER,
  ThreshdServiceStatus,
  hasErrors,
  validateThreshdService
} from '@/lib/thresholdValidator'
import { getDefaultThreshdService, useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ISelectItemType } from '@/types'
import type { ThreshdService } from '@/types/thresholdConfig'
import { cloneDeep } from 'lodash'
import { OnmsInputNumber, OnmsInputText, OnmsSelect, OnmsToggleSwitch } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const store = useThreshdConfigurationStore()
const groupStore = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const isLoaded = ref(false)
const isSaving = ref(false)
const hasService = ref(false)
const draft = ref<ThreshdService>(getDefaultThreshdService())

const packageName = computed(() => nameFromParam(route.params.name))
const index = computed(() => indexFromParam(route.params.index))
const isCreate = computed(() => index.value < 0)

const notFoundMessage = computed(() => {
  if (!store.currentPackage) {
    return `No threshd package named '${packageName.value}' exists.`
  }

  return hasService.value ? undefined : `No such service in '${packageName.value}'.`
})

const groupOptions = computed<ISelectItemType[]>(() =>
  groupStore.groupNames.map(name => ({ _text: name, _value: name }))
)

const thresholdingGroup = computed(
  () => draft.value.parameters.find(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER)?.value ?? ''
)

const errors = computed(() => validateThreshdService(draft.value, groupStore.groupNames))

onMounted(async () => {
  // Coming from the package page, keep its package: it may carry edits that are not saved yet, and a service
  // save writes those along. Only a reload or a direct link loads it.
  await Promise.all([
    store.loadedPackageName === packageName.value && store.currentPackage ? null : store.fetchPackage(packageName.value),
    groupStore.fetchGroups()
  ])

  const services = store.currentPackage?.services ?? []

  if (isCreate.value) {
    hasService.value = true
  } else if (index.value < services.length) {
    draft.value = cloneDeep(services[index.value])
    hasService.value = true
  }

  isLoaded.value = true
})

// The package page keeps unsaved edits only for the way back; anywhere else they are dropped.
onBeforeRouteLeave((to) => {
  if (!isPackagePageOf(to, store.loadedPackageName)) {
    store.clearCurrentPackage()
  }
})

const goBack = () => {
  // A service save also writes a pending package rename, after which the package lives under its new name.
  router.push(packagePath(store.loadedPackageName ?? packageName.value))
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
  isSaving.value = true
  const result = await store.saveService(isCreate.value ? null : index.value, draft.value)
  isSaving.value = false

  if (result.success) {
    showSnackBar({ msg: 'Service saved.' })
    goBack()
  } else {
    showSnackBar({ msg: result.message, error: true })
  }
}
</script>
