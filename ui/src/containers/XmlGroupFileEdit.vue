<template>
  <div class="xml-group-file-edit">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <ThresholdEditPage
      v-if="isLoaded"
      :title="isCreate ? 'Create New Group File' : `Group File '${groupFileId}'`"
      sectionTitle="Groups"
      :saveLabel="isCreate ? 'Create Group File' : 'Save Changes'"
      :saveDisabled="!!error || isSaving"
      :notFoundMessage="notFoundMessage"
      wide
      @back="goBack"
      @cancel="goBack"
      @save="onSave"
    >
      <FormField
        v-if="isCreate"
        label="Id"
        for="xml-group-file-id"
        required
        :error="idError"
        :info="XML_FIELD_HINTS.groupFileId"
      >
        <OnmsInputText
          inputId="xml-group-file-id"
          data-test="xml-group-file-id"
          :modelValue="draft.id"
          :invalid="!!idError"
          fluid
          @update:modelValue="draft.id = String($event ?? '')"
        />
      </FormField>
      <p v-else class="used-by" data-test="xml-group-file-used-by">
        {{ usedBy.length ? `Imported by ${usedBy.join(', ')}.` : 'Not imported by any collection yet.' }}
      </p>
      <div class="spacer"></div>
      <small v-if="groupsError" class="field-error" role="alert" data-test="xml-group-file-groups-error">{{ groupsError }}</small>
      <XmlGroupListEditor
        v-model="draft.groups"
        idPrefix="xml-group-file"
        :resourceTypes="store.metadata.resourceTypes"
        :attributeTypes="store.metadata.attributeTypes"
      />
    </ThresholdEditPage>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import XmlGroupListEditor from '@/components/XmlCollectionConfiguration/XmlGroupListEditor.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { cloneState } from '@/lib/utils'
import { XML_FIELD_HINTS } from '@/lib/xmlCollectionHelpText'
import { GROUP_FILES_TAB_PATH, groupFilePath, nameFromParam, xmlBreadcrumbs } from '@/lib/xmlCollectionRoutes'
import { validateGroupFileId, validateGroups } from '@/lib/xmlCollectionValidator'
import { useMenuStore } from '@/stores/menuStore'
import { getDefaultGroupFile, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { isPreconditionFailed } from '@/types/validation'
import type { XmlGroupFile } from '@/types/xmlCollectionConfig'
import { OnmsInputText } from '@opennms/onms-ui'

const GROUP_FILE_ROUTE_NAMES = ['XML Group File Create', 'XML Group File']

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useXmlCollectionConfigStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<XmlGroupFile>(getDefaultGroupFile())

const isCreate = computed(() => route.name === 'XML Group File Create')
const groupFileId = computed(() => nameFromParam(route.params.id))

const breadcrumbs = computed(() =>
  xmlBreadcrumbs(menuStore.mainMenu, [
    { label: 'Group files', to: GROUP_FILES_TAB_PATH },
    { label: isCreate.value ? 'New group file' : groupFileId.value }
  ])
)

const notFoundMessage = computed(() =>
  !isCreate.value && !store.currentGroupFile ? `No group file with id '${groupFileId.value}' exists.` : undefined
)

const usedBy = computed(() => store.groupFiles.find(groupFile => groupFile.id === groupFileId.value)?.usedBy ?? [])
const idError = computed(() => (isCreate.value ? validateGroupFileId(draft.value.id, store.groupFileIds) : undefined))
const groupsError = computed(() => validateGroups(draft.value.groups))
const error = computed(() => idError.value || groupsError.value)

const load = async () => {
  isLoaded.value = false
  const loads: Promise<unknown>[] = [store.fetchGroupFiles(), store.fetchMetadata()]
  if (!isCreate.value) {
    loads.push(store.fetchGroupFile(groupFileId.value))
  }
  await Promise.all(loads)

  draft.value = !isCreate.value && store.currentGroupFile ? cloneState(store.currentGroupFile) : getDefaultGroupFile()
  isLoaded.value = true
}

onMounted(load)
// Creating moves on to the page of the new group file, which reuses this component. Leaving the page also
// changes the route before this component is gone, so only react to routes of this page.
watch(() => route.fullPath, () => {
  if (GROUP_FILE_ROUTE_NAMES.includes(String(route.name))) {
    load()
  }
})

const goBack = () => {
  router.push(GROUP_FILES_TAB_PATH)
}

const onSave = async () => {
  const groupFile: XmlGroupFile = { ...draft.value, id: isCreate.value ? draft.value.id.trim() : groupFileId.value }

  isSaving.value = true
  const result = isCreate.value ? await store.createGroupFile(groupFile) : await store.updateGroupFile(groupFile)
  isSaving.value = false

  if (!report(result, isCreate.value ? 'Group file created.' : 'Group file saved.')) {
    // After a 412 the store holds the current version; show it rather than the stale edit. Other failures keep
    // the edit, so the user can fix it.
    if (isPreconditionFailed(result) && store.currentGroupFile) {
      draft.value = cloneState(store.currentGroupFile)
    }
    return
  }

  if (isCreate.value) {
    router.push(groupFilePath(groupFile.id))
  } else if (store.currentGroupFile) {
    // Carry the new version, so the next save is not refused as stale.
    draft.value = cloneState(store.currentGroupFile)
  }
}
</script>

<style lang="scss" scoped>
.used-by {
  margin: 0;
  color: var(--p-text-muted-color);
}

.field-error {
  display: block;
  margin-bottom: 0.5em;
  font-size: 0.875rem;
  color: var(--p-red-500);
}
</style>
