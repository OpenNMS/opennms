<template>
  <OnmsDrawer
    :visible="store.definitionDrawer.visible"
    position="right"
    width="52rem"
    :header="header"
    data-test="threshold-definition-drawer"
    @update:visible="onVisibleChange"
  >
    <div class="drawer-body">
      <div class="drawer-header">
        <OnmsIcon
          :icon="InfoIcon"
          class="info-icon"
          data-test="threshold-definition-info-icon"
          role="button"
          tabindex="0"
          aria-label="About thresholds"
          @click="isHelpVisible = true"
          @keyup.enter="isHelpVisible = true"
        />
      </div>

      <ThresholdDefinitionForm
        v-model="definition"
        :kind="store.definitionDrawer.kind"
        :errors="errors"
        :dsTypes="store.dsTypes"
      />

      <ResourceFilterEditor
        :modelValue="definition.resourceFilters"
        :filterOperator="definition.filterOperator || FilterOperator.Or"
        @update:modelValue="definition.resourceFilters = $event"
        @update:filterOperator="definition.filterOperator = $event"
      />
    </div>

    <template #footer>
      <div class="drawer-footer">
        <OnmsButton variant="text" data-test="threshold-definition-cancel" @click="onCancel">Cancel</OnmsButton>
        <OnmsButton data-test="threshold-definition-save" :disabled="hasErrors(errors)" @click="onSave">
          Save
        </OnmsButton>
      </div>
    </template>

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Thresholds"
      :text="THRESHOLD_DEFINITION_HELP"
      @close="isHelpVisible = false"
    />
  </OnmsDrawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import ResourceFilterEditor from '@/components/ThresholdConfiguration/Group/ResourceFilterEditor.vue'
import ThresholdDefinitionForm from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionForm.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLD_DEFINITION_HELP } from '@/lib/thresholdHelpText'
import {
  FilterOperator,
  ThresholdDefinitionKind,
  hasErrors,
  validateThresholdDefinition
} from '@/lib/thresholdValidator'
import { getDefaultThreshold, useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { CreateEditMode } from '@/types'
import type { ThresholdDefinition } from '@/types/thresholdConfig'
import { OnmsButton, OnmsDrawer, OnmsIcon } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const store = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const isHelpVisible = ref(false)
// A deep clone of the definition being edited, so Cancel really discards and the table does not update
// under the user as they type.
const definition = ref<ThresholdDefinition>(getDefaultThreshold())

const errors = computed(() => validateThresholdDefinition(definition.value, store.definitionDrawer.kind))

const header = computed(() => {
  const noun = store.definitionDrawer.kind === ThresholdDefinitionKind.Threshold ? 'threshold' : 'expression threshold'
  return store.definitionDrawer.mode === CreateEditMode.Create ? `New ${noun}` : `Edit ${noun}`
})

watch(
  () => store.definitionDrawer.visible,
  (visible) => {
    if (visible) {
      definition.value = store.drawerDefinition()
    }
  },
  { immediate: true }
)

const onVisibleChange = (visible: boolean) => {
  if (!visible) {
    store.closeDefinitionDrawer()
  }
}

const onCancel = () => {
  store.closeDefinitionDrawer()
}

const onSave = async () => {
  const { kind, mode, index } = store.definitionDrawer

  store.upsertDefinition(kind, mode === CreateEditMode.Edit ? index : null, definition.value)
  store.closeDefinitionDrawer()

  const result = await store.saveCurrentGroup()

  showSnackBar({
    msg: result.success ? 'Threshold saved.' : result.message,
    error: !result.success
  })
}
</script>

<style lang="scss" scoped>
.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 1.5em;
  padding: 0 0 1em 0;
}

.drawer-header {
  display: flex;
  justify-content: flex-end;
}

.info-icon {
  cursor: pointer;
  color: var(--p-text-muted-color);
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5em;
}
</style>
