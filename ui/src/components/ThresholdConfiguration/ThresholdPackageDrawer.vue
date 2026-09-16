<template>
  <OnmsDrawer
    :visible="visible"
    position="right"
    width="34rem"
    header="New threshd package"
    data-test="threshd-package-drawer"
    @update:visible="onVisibleChange"
  >
    <div class="drawer-body">
      <FormField label="Name" for="threshd-package-name" required :error="errors.name">
        <OnmsInputText
          inputId="threshd-package-name"
          data-test="threshd-package-drawer-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>

      <FormField
        label="Filter"
        for="threshd-package-filter"
        required
        :error="errors.filter"
        :hint="THRESHOLD_FIELD_HINTS.packageFilter"
      >
        <OnmsInputText
          inputId="threshd-package-filter"
          data-test="threshd-package-drawer-filter"
          :modelValue="draft.filter"
          :invalid="!!errors.filter"
          fluid
          @update:modelValue="draft.filter = String($event ?? '')"
        />
      </FormField>

      <p class="hint">Services, address ranges and outage calendars are configured after the package exists.</p>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <OnmsButton variant="text" data-test="threshd-package-drawer-cancel" @click="emit('cancel')">
          Cancel
        </OnmsButton>
        <OnmsButton data-test="threshd-package-drawer-save" :disabled="hasErrors(errors)" @click="onSave">
          Save
        </OnmsButton>
      </div>
    </template>
  </OnmsDrawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import { THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import { hasErrors, validateThreshdPackage } from '@/lib/thresholdValidator'
import { getDefaultThreshdPackage } from '@/stores/threshdConfigurationStore'
import type { ThreshdPackage } from '@/types/thresholdConfig'
import { OnmsButton, OnmsDrawer, OnmsInputText } from '@opennms/onms-ui'

const props = defineProps<{
  visible: boolean
  existingNames: string[]
}>()

const emit = defineEmits<{
  cancel: []
  save: [pkg: ThreshdPackage]
}>()

const draft = ref<ThreshdPackage>(getDefaultThreshdPackage())

const errors = computed(() => validateThreshdPackage(draft.value, props.existingNames, true))

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      draft.value = getDefaultThreshdPackage()
    }
  },
  { immediate: true }
)

const onVisibleChange = (visible: boolean) => {
  if (!visible) {
    emit('cancel')
  }
}

const onSave = () => {
  emit('save', { ...draft.value, name: draft.value.name.trim() })
}
</script>

<style lang="scss" scoped>
.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 0.75em;
}

.hint {
  color: var(--p-text-muted-color);
  margin: 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5em;
}
</style>
