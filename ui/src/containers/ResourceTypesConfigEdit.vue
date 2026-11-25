<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New Resource Types Configuration' : 'Rename Resource Types Configuration'"
    sectionTitle="Basic Information"
    :saveLabel="isCreate ? 'Create Configuration' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField
      label="Name"
      for="resource-types-config-id"
      required
      :error="errors.id"
      info="Used like the name of a former file in etc/resource-types.d, e.g. postgresql-resource."
    >
      <OnmsInputText
        inputId="resource-types-config-id"
        data-test="resource-types-config-edit-id"
        :modelValue="draftId"
        :invalid="!!errors.id"
        fluid
        @update:modelValue="draftId = String($event ?? '')"
      />
    </FormField>
    <template v-if="isCreate">
      <div class="spacer"></div>
      <FormField
        label="Start from a file (optional)"
        for="resource-types-config-file"
        :hint="file ? `The resource types of '${file.name}' are imported.` : 'A former resource-types.d XML file, or a JSON download.'"
      >
        <input
          id="resource-types-config-file"
          type="file"
          accept=".json,.xml,application/json,application/xml,text/xml"
          data-test="resource-types-config-edit-file"
          @change="onFile"
        >
      </FormField>
    </template>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { configPath, idFromParam, RESOURCE_TYPES_PATH } from '@/lib/resourceTypeRoutes'
import { hasErrors, validateConfigId } from '@/lib/resourceTypeValidator'
import { getDefaultResourceTypesConfig, useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { OnmsInputText } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const store = useResourceTypesConfigStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const draftId = ref('')
const file = ref<File | null>(null)

const isCreate = computed(() => route.name === 'Resource Types Config Create')
const configId = computed(() => idFromParam(route.params.id))

const notFoundMessage = computed(() =>
  !isCreate.value && !store.currentConfig ? `No resource types configuration named '${configId.value}' exists.` : undefined
)

// On rename the configuration's own id is obviously taken; leave it out so the form does not fight itself.
const errors = computed(() =>
  validateConfigId(draftId.value, store.configIds.filter(id => isCreate.value || id !== configId.value))
)

onMounted(async () => {
  if (isCreate.value) {
    await store.fetchConfigs()
  } else {
    await Promise.all([store.fetchConfig(configId.value), store.fetchConfigs()])
    draftId.value = store.currentConfig?.id ?? ''
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(isCreate.value ? RESOURCE_TYPES_PATH : configPath(configId.value))
}

const onFile = (event: Event) => {
  file.value = (event.target as HTMLInputElement).files?.[0] ?? null
}

const onSave = async () => {
  const id = draftId.value.trim()

  isSaving.value = true

  let result
  if (!isCreate.value) {
    result = await store.renameConfig(configId.value, id)
  } else if (file.value) {
    // The upload creates the configuration when it does not exist yet.
    result = await store.uploadConfig(id, file.value, file.value.name.toLowerCase().endsWith('.xml'))
  } else {
    result = await store.createConfig({ ...getDefaultResourceTypesConfig(), id })
  }

  isSaving.value = false

  if (report(result, isCreate.value ? 'Resource types configuration created.' : 'Resource types configuration renamed.')) {
    router.push(configPath(id))
  }
}
</script>
