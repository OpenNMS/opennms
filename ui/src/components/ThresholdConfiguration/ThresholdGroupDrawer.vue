<template>
  <OnmsDrawer
    :visible="visible"
    position="right"
    width="34rem"
    :header="isCreate ? 'New threshold group' : 'Edit threshold group'"
    data-test="threshold-group-drawer"
    @update:visible="onVisibleChange"
  >
    <div class="drawer-body">
      <FormField label="Name" for="threshold-group-name" required :error="errors.name">
        <OnmsInputText
          inputId="threshold-group-name"
          data-test="threshold-group-drawer-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>

      <FormField
        label="RRD repository"
        for="threshold-group-repository"
        required
        :error="errors.rrdRepository"
        :hint="THRESHOLD_FIELD_HINTS.rrdRepository"
      >
        <OnmsInputText
          inputId="threshold-group-repository"
          data-test="threshold-group-drawer-repository"
          :modelValue="draft.rrdRepository"
          :invalid="!!errors.rrdRepository"
          fluid
          @update:modelValue="draft.rrdRepository = String($event ?? '')"
        />
      </FormField>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <OnmsButton variant="text" data-test="threshold-group-drawer-cancel" @click="emit('cancel')">
          Cancel
        </OnmsButton>
        <OnmsButton data-test="threshold-group-drawer-save" :disabled="hasErrors(errors)" @click="onSave">
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
import { hasErrors, validateThresholdGroup } from '@/lib/thresholdValidator'
import { getDefaultThresholdGroup } from '@/stores/thresholdGroupStore'
import type { ThresholdGroup } from '@/types/thresholdConfig'
import { OnmsButton, OnmsDrawer, OnmsInputText } from '@opennms/onms-ui'

const props = defineProps<{
  visible: boolean
  isCreate: boolean
  group?: ThresholdGroup | null
  existingNames: string[]
}>()

const emit = defineEmits<{
  cancel: []
  save: [group: ThresholdGroup]
}>()

const draft = ref<ThresholdGroup>(getDefaultThresholdGroup())

const errors = computed(() =>
  validateThresholdGroup(
    draft.value,
    // On rename the group's own name is obviously taken; exclude it so the form does not fight itself.
    props.existingNames.filter(name => props.isCreate || name !== props.group?.name),
    true
  )
)

watch(
  () => [props.visible, props.group],
  () => {
    if (props.visible) {
      // A deep-ish copy: the drawer must not mutate the loaded group before the user saves.
      draft.value = props.group ? { ...props.group } : getDefaultThresholdGroup()
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

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5em;
}
</style>
