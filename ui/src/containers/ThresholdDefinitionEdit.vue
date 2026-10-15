<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? `Create New ${noun}` : `Edit ${noun} Details`"
    sectionTitle="Basic Information"
    :saveLabel="isCreate ? `Create ${noun}` : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    wide
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <template #section-actions>
      <InfoIconButton
        ariaLabel="About thresholds"
        data-test="threshold-definition-info-icon"
        @click="isHelpVisible = true"
      />
    </template>

    <ThresholdDefinitionForm
      v-model="definition"
      :kind="kind"
      :errors="errors"
      :dsTypes="store.dsTypes"
    />
    <div class="spacer"></div>
    <div class="spacer"></div>
    <ResourceFilterEditor
      :modelValue="definition.resourceFilters"
      :filterOperator="definition.filterOperator || FilterOperator.Or"
      @update:modelValue="definition.resourceFilters = $event"
      @update:filterOperator="definition.filterOperator = $event"
    />
  </ThresholdEditPage>

  <ThresholdHelpDialog
    :visible="isHelpVisible"
    title="Thresholds"
    :text="THRESHOLD_DEFINITION_HELP"
    @close="isHelpVisible = false"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import InfoIconButton from '@/components/Common/InfoIconButton.vue'
import ResourceFilterEditor from '@/components/ThresholdConfiguration/Group/ResourceFilterEditor.vue'
import ThresholdDefinitionForm from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionForm.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLD_DEFINITION_HELP } from '@/lib/thresholdHelpText'
import { groupPath, indexFromParam, nameFromParam } from '@/lib/thresholdRoutes'
import {
  FilterOperator,
  ThresholdDefinitionKind,
  hasErrors,
  validateThresholdDefinition
} from '@/lib/thresholdValidator'
import { getDefaultThreshold, useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ThresholdDefinition } from '@/types/thresholdConfig'

const route = useRoute()
const router = useRouter()
const store = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const isLoaded = ref(false)
const isSaving = ref(false)
const isHelpVisible = ref(false)
const hasDefinition = ref(false)
// A deep clone of the definition being edited, so Cancel really discards.
const definition = ref<ThresholdDefinition>(getDefaultThreshold())

const groupName = computed(() => nameFromParam(route.params.name))
const kind = computed(() =>
  route.params.kind === ThresholdDefinitionKind.Expression ? ThresholdDefinitionKind.Expression : ThresholdDefinitionKind.Threshold
)
const index = computed(() => indexFromParam(route.params.index))
const isCreate = computed(() => index.value < 0)
const noun = computed(() => (kind.value === ThresholdDefinitionKind.Threshold ? 'Threshold' : 'Expression Threshold'))

const notFoundMessage = computed(() => {
  if (!store.currentGroup) {
    return `No threshold group named '${groupName.value}' exists.`
  }

  if (store.currentGroup.readOnly) {
    return `Threshold group '${groupName.value}' is read only.`
  }

  return hasDefinition.value ? undefined : `No such ${noun.value.toLowerCase()} in '${groupName.value}'.`
})

const errors = computed(() => validateThresholdDefinition(definition.value, kind.value))

onMounted(async () => {
  await store.fetchGroup(groupName.value)

  if (!store.dsTypes.length) {
    await store.fetchMetadata()
  }

  const existing = store.definitionAt(kind.value, isCreate.value ? null : index.value)

  if (existing) {
    definition.value = existing
    hasDefinition.value = true
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(groupPath(groupName.value))
}

const onSave = async () => {
  isSaving.value = true
  const result = await store.saveDefinition(kind.value, isCreate.value ? null : index.value, definition.value)
  isSaving.value = false

  if (result.success) {
    showSnackBar({ msg: 'Threshold saved.' })
    goBack()
  } else {
    showSnackBar({ msg: result.message, error: true })
  }
}
</script>
