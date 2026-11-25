<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New Resource Type' : 'Edit Resource Type'"
    :sectionTitle="`Resource type in '${configId}'`"
    :saveLabel="isCreate ? 'Create Resource Type' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    wide
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <template #section-actions>
      <OnmsIconButton
        :icon="InfoIcon"
        iconSize="1.2em"
        class="info-icon-button"
        title="About resource types"
        aria-label="About resource types"
        data-test="resource-type-info-icon"
        @click="isHelpVisible = true"
      />
    </template>

    <div class="form-grid">
      <FormField
        label="Name"
        for="resource-type-name"
        required
        :error="errors.name"
        :hint="warning"
        :info="RESOURCE_TYPE_FIELD_HINTS.name"
      >
        <OnmsInputText
          inputId="resource-type-name"
          data-test="resource-type-edit-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>
      <FormField label="Label" for="resource-type-label" required :error="errors.label" :info="RESOURCE_TYPE_FIELD_HINTS.label">
        <OnmsInputText
          inputId="resource-type-label"
          data-test="resource-type-edit-label"
          :modelValue="draft.label"
          :invalid="!!errors.label"
          fluid
          @update:modelValue="draft.label = String($event ?? '')"
        />
      </FormField>
      <FormField label="Resource label" for="resource-type-resource-label" :info="RESOURCE_TYPE_FIELD_HINTS.resourceLabel">
        <OnmsInputText
          inputId="resource-type-resource-label"
          data-test="resource-type-edit-resource-label"
          :modelValue="draft.resourceLabel ?? ''"
          fluid
          @update:modelValue="draft.resourceLabel = String($event ?? '')"
        />
      </FormField>
    </div>

    <div class="spacer"></div>
    <div class="strategies">
      <StrategyEditor
        v-model="draft.persistenceSelectorStrategy"
        label="Persistence selector strategy"
        idPrefix="resource-type-persistence"
        info="Decides which resources of this type are stored."
        :options="persistenceOptions"
        :classError="errors.persistenceSelectorStrategy"
        :parametersError="errors.persistenceSelectorParameters"
      />
      <StrategyEditor
        v-model="draft.storageStrategy"
        label="Storage strategy"
        idPrefix="resource-type-storage"
        info="Decides where the data of a resource is stored and how it is named."
        :options="storageOptions"
        :classError="errors.storageStrategy"
        :parametersError="errors.storageParameters"
      />
    </div>

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Resource types"
      :text="RESOURCE_TYPE_HELP"
      @close="isHelpVisible = false"
    />
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import StrategyEditor from '@/components/ResourceTypesConfiguration/StrategyEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { RESOURCE_TYPE_FIELD_HINTS, RESOURCE_TYPE_HELP } from '@/lib/resourceTypeHelpText'
import { configPath, idFromParam, indexFromParam } from '@/lib/resourceTypeRoutes'
import { hasErrors, overrideWarning, validateResourceType } from '@/lib/resourceTypeValidator'
import { getDefaultResourceType, useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import type { ResourceTypeDefinition } from '@/types/resourceTypesConfig'
import { isPreconditionFailed } from '@/types/validation'
import { OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const route = useRoute()
const router = useRouter()
const store = useResourceTypesConfigStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const isHelpVisible = ref(false)
const hasType = ref(false)
// A deep clone of the resource type being edited, so Cancel really discards.
const draft = ref<ResourceTypeDefinition>(getDefaultResourceType())

const configId = computed(() => idFromParam(route.params.id))
const index = computed(() => indexFromParam(route.params.index))
const isCreate = computed(() => index.value < 0)

const notFoundMessage = computed(() => {
  if (!store.currentConfig) {
    return `No resource types configuration named '${configId.value}' exists.`
  }
  return isCreate.value || hasType.value ? undefined : `No such resource type in '${configId.value}'.`
})

// The names of the other types of this configuration and of every other configuration.
const takenNames = computed(() => [
  ...(store.currentConfig?.resourceTypes ?? []).filter((_, i) => i !== index.value).map(type => type.name),
  ...store.typeNamesOutside(configId.value)
])

const errors = computed(() => validateResourceType(draft.value, takenNames.value))
const warning = computed(() => overrideWarning(draft.value.name, store.metadata))

const persistenceOptions = computed(() =>
  store.metadata?.persistenceSelectorStrategies?.length ? store.metadata.persistenceSelectorStrategies : PERSISTENCE_SELECTOR_STRATEGY_OPTIONS
)
const storageOptions = computed(() =>
  store.metadata?.storageStrategies?.length ? store.metadata.storageStrategies : STORAGE_STRATEGY_OPTIONS
)

onMounted(async () => {
  await Promise.all([store.fetchConfig(configId.value), store.fetchConfigs()])

  if (!store.metadata) {
    await store.fetchMetadata()
  }

  if (!isCreate.value) {
    const existing = store.resourceTypeAt(index.value)

    if (existing) {
      draft.value = existing
      hasType.value = true
    }
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(configPath(configId.value))
}

const onSave = async () => {
  const type: ResourceTypeDefinition = {
    ...draft.value,
    name: draft.value.name.trim(),
    label: draft.value.label.trim(),
    resourceLabel: draft.value.resourceLabel?.trim() || undefined,
    persistenceSelectorStrategy: { ...draft.value.persistenceSelectorStrategy, className: draft.value.persistenceSelectorStrategy.className.trim() },
    storageStrategy: { ...draft.value.storageStrategy, className: draft.value.storageStrategy.className.trim() }
  }

  isSaving.value = true
  const result = await store.saveResourceType(isCreate.value ? null : index.value, type)
  isSaving.value = false

  // After a 412 the store has loaded the current configuration, in which the index may address another type.
  // Saving the draft again would overwrite that one, so the user reselects from the configuration page instead.
  if (report(result, 'Resource type saved.') || (!isCreate.value && isPreconditionFailed(result))) {
    goBack()
  }
}
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;

.info-icon-button {
  @include info-icon-button;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1em;
}

.strategies {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1.5em;
}

@media (max-width: 900px) {
  .form-grid,
  .strategies {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
