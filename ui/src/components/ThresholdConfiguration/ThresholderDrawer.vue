<template>
  <OnmsDrawer
    :visible="store.thresholderDrawer.visible"
    position="right"
    width="40rem"
    :header="isCreate ? 'New thresholder' : 'Edit thresholder'"
    data-test="thresholder-drawer"
    @update:visible="onVisibleChange"
  >
    <div class="drawer-body">
      <FormField label="Service" for="thresholder-service" required :error="errors.service">
        <OnmsInputText
          inputId="thresholder-service"
          data-test="thresholder-service"
          :modelValue="draft.service"
          :invalid="!!errors.service"
          fluid
          @update:modelValue="draft.service = String($event ?? '')"
        />
      </FormField>

      <FormField
        label="Class name"
        for="thresholder-class-name"
        required
        :error="errors.className"
        hint="The class that evaluates thresholds for this service."
      >
        <OnmsInputText
          inputId="thresholder-class-name"
          data-test="thresholder-class-name"
          :modelValue="draft.className"
          :invalid="!!errors.className"
          fluid
          @update:modelValue="draft.className = String($event ?? '')"
        />
      </FormField>

      <ParameterListEditor
        :modelValue="draft.parameters"
        idPrefix="thresholder-parameter"
        @update:modelValue="draft.parameters = $event"
      />
    </div>

    <template #footer>
      <div class="drawer-footer">
        <OnmsButton variant="text" data-test="thresholder-cancel" @click="store.closeThresholderDrawer()">
          Cancel
        </OnmsButton>
        <OnmsButton data-test="thresholder-save" :disabled="hasErrors(errors)" @click="onSave">Save</OnmsButton>
      </div>
    </template>
  </OnmsDrawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import ParameterListEditor from '@/components/ThresholdConfiguration/Common/ParameterListEditor.vue'
import useSnackbar from '@/composables/useSnackbar'
import { hasErrors, validateThresholder } from '@/lib/thresholdValidator'
import { getDefaultThresholder, useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { CreateEditMode } from '@/types'
import type { Thresholder } from '@/types/thresholdConfig'
import { cloneDeep } from 'lodash'
import { OnmsButton, OnmsDrawer, OnmsInputText } from '@opennms/onms-ui'

const store = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

const draft = ref<Thresholder>(getDefaultThresholder())

const isCreate = computed(() => store.thresholderDrawer.mode === CreateEditMode.Create)
const errors = computed(() => validateThresholder(draft.value))

watch(
  () => store.thresholderDrawer.visible,
  (visible) => {
    if (!visible) {
      return
    }

    const { mode, index } = store.thresholderDrawer
    const existing = store.config.thresholder[index]

    draft.value = mode === CreateEditMode.Edit && existing ? cloneDeep(existing) : getDefaultThresholder()
  },
  { immediate: true }
)

const onVisibleChange = (visible: boolean) => {
  if (!visible) {
    store.closeThresholderDrawer()
  }
}

const onSave = async () => {
  const { mode, index } = store.thresholderDrawer
  store.closeThresholderDrawer()

  const result = await store.upsertThresholder(mode === CreateEditMode.Edit ? index : null, draft.value)

  showSnackBar({ msg: result.success ? 'Thresholder saved.' : result.message, error: !result.success })
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
