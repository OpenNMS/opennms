<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New Threshold Group' : 'Edit Threshold Group Details'"
    sectionTitle="Basic Information"
    :saveLabel="isCreate ? 'Create Threshold Group' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField label="Name" for="threshold-group-name" required :error="errors.name">
      <OnmsInputText
        inputId="threshold-group-name"
        data-test="threshold-group-edit-name"
        :modelValue="draft.name"
        :invalid="!!errors.name"
        fluid
        @update:modelValue="draft.name = String($event ?? '')"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField
      label="RRD repository"
      for="threshold-group-repository"
      required
      :error="errors.rrdRepository"
      :hint="THRESHOLD_FIELD_HINTS.rrdRepository"
    >
      <OnmsInputText
        inputId="threshold-group-repository"
        data-test="threshold-group-edit-repository"
        :modelValue="draft.rrdRepository"
        :invalid="!!errors.rrdRepository"
        fluid
        @update:modelValue="draft.rrdRepository = String($event ?? '')"
      />
    </FormField>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import { GROUPS_TAB_PATH, groupPath, nameFromParam } from '@/lib/thresholdRoutes'
import { hasErrors, validateThresholdGroup } from '@/lib/thresholdValidator'
import { getDefaultThresholdGroup, useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ThresholdGroup } from '@/types/thresholdConfig'
import { OnmsInputText } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const store = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<ThresholdGroup>(getDefaultThresholdGroup())

const isCreate = computed(() => route.name === 'Threshold Group Create')
const groupName = computed(() => nameFromParam(route.params.name))

const notFoundMessage = computed(() => {
  if (isCreate.value) {
    return undefined
  }

  if (!store.currentGroup) {
    return `No threshold group named '${groupName.value}' exists.`
  }

  return store.currentGroup.readOnly ? `Threshold group '${groupName.value}' is read only.` : undefined
})

const errors = computed(() =>
  validateThresholdGroup(
    draft.value,
    // On rename the group's own name is obviously taken; exclude it so the form does not fight itself.
    store.groupNames.filter(name => isCreate.value || name !== groupName.value),
    true
  )
)

onMounted(async () => {
  if (isCreate.value) {
    await store.fetchGroups()
  } else {
    await Promise.all([store.fetchGroup(groupName.value), store.fetchGroups()])

    if (store.currentGroup) {
      draft.value = { ...store.currentGroup }
    }
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(isCreate.value ? GROUPS_TAB_PATH : groupPath(groupName.value))
}

const onSave = async () => {
  const group: ThresholdGroup = { ...draft.value, name: draft.value.name.trim() }

  isSaving.value = true

  const result = isCreate.value
    ? await store.createGroup(group)
    // Only name and rrdRepository are edited here; keep the definitions and version the store holds.
    : await store.renameGroup(groupName.value, {
      ...(store.currentGroup as ThresholdGroup),
      name: group.name,
      rrdRepository: group.rrdRepository
    })

  isSaving.value = false

  if (result.success) {
    showSnackBar({ msg: isCreate.value ? 'Threshold group created.' : 'Threshold group saved.' })
    router.push(groupPath(group.name))
  } else {
    showSnackBar({ msg: result.message, error: true })
  }
}
</script>
