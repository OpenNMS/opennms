<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? `Add Source to '${collectionName}'` : `Edit Source of '${collectionName}'`"
    sectionTitle="Source"
    :saveLabel="isCreate ? 'Add Source' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    wide
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField label="URL" for="xml-source-url" required :error="errors.url" :info="XML_FIELD_HINTS.url">
      <OnmsInputText
        inputId="xml-source-url"
        data-test="xml-source-url"
        :modelValue="draft.url"
        :invalid="!!errors.url"
        placeholder="http://{ipaddr}:8080/status"
        fluid
        @update:modelValue="draft.url = String($event ?? '')"
      />
    </FormField>

    <div class="spacer"></div>
    <OnmsToggleSwitch
      inputId="xml-source-custom-request"
      data-test="xml-source-custom-request"
      :modelValue="!!draft.request"
      @update:modelValue="onToggleRequest"
    />
    <label for="xml-source-custom-request" class="toggle-label">Custom request (method, parameters, headers, body)</label>

    <div v-if="draft.request" class="request">
      <div class="grid">
        <FormField label="Method" for="xml-source-method" :error="errors.method" :info="XML_FIELD_HINTS.method">
          <OnmsSelect
            inputId="xml-source-method"
            data-test="xml-source-method"
            :options="methodOptions"
            :modelValue="draft.request.method"
            :invalid="!!errors.method"
            @update:modelValue="draft.request.method = String($event ?? 'GET')"
          />
        </FormField>
        <FormField label="Content type" for="xml-source-content-type" :error="errors.contentType">
          <OnmsInputText
            inputId="xml-source-content-type"
            data-test="xml-source-content-type"
            :modelValue="draft.request.contentType ?? ''"
            :invalid="!!errors.contentType"
            placeholder="application/json"
            fluid
            @update:modelValue="draft.request.contentType = String($event ?? '')"
          />
        </FormField>
      </div>
      <FormField label="Content" for="xml-source-content" :info="XML_FIELD_HINTS.content">
        <OnmsTextarea
          id="xml-source-content"
          data-test="xml-source-content"
          :modelValue="draft.request.content ?? ''"
          rows="4"
          fluid
          @update:modelValue="draft.request.content = String($event ?? '')"
        />
      </FormField>
      <FormField label="Parameters" :info="XML_FIELD_HINTS.parameters">
        <KeyValueListEditor
          v-model="draft.request.parameters"
          idPrefix="xml-source-parameter"
          itemLabel="parameter"
          :error="errors.parameters"
        />
      </FormField>
      <FormField label="Headers" :info="XML_FIELD_HINTS.headers">
        <KeyValueListEditor
          v-model="draft.request.headers"
          idPrefix="xml-source-header"
          itemLabel="header"
          :error="errors.headers"
        />
      </FormField>
    </div>

    <div class="spacer"></div>
    <FormField label="Imported group files" for="xml-source-imports" :error="errors.importGroups" :info="XML_FIELD_HINTS.importGroups">
      <OnmsMultiSelect
        inputId="xml-source-imports"
        data-test="xml-source-imports"
        :options="importOptions"
        :modelValue="draft.importGroups"
        display="chip"
        filter
        placeholder="No group files imported"
        @update:modelValue="draft.importGroups = ($event ?? []) as string[]"
      />
    </FormField>

    <div class="spacer"></div>
    <h4>Groups of this source</h4>
    <small v-if="errors.groups" class="field-error" role="alert" data-test="xml-source-groups-error">{{ errors.groups }}</small>
    <XmlGroupListEditor
      v-model="draft.groups"
      idPrefix="xml-source"
      :resourceTypes="store.metadata.resourceTypes"
      :attributeTypes="store.metadata.attributeTypes"
      emptyMessage="No groups of its own; the source collects the groups of the imported group files only."
    />
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import KeyValueListEditor from '@/components/XmlCollectionConfiguration/KeyValueListEditor.vue'
import XmlGroupListEditor from '@/components/XmlCollectionConfiguration/XmlGroupListEditor.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { XML_FIELD_HINTS } from '@/lib/xmlCollectionHelpText'
import { collectionPath, indexFromParam, nameFromParam } from '@/lib/xmlCollectionRoutes'
import { hasErrors, validateSource } from '@/lib/xmlCollectionValidator'
import { getDefaultRequest, getDefaultSource, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import type { XmlSource } from '@/types/xmlCollectionConfig'
import { OnmsInputText, OnmsMultiSelect, OnmsSelect, OnmsTextarea, OnmsToggleSwitch } from '@opennms/onms-ui'

const METHODS = ['GET', 'POST', 'PUT']

const route = useRoute()
const router = useRouter()
const store = useXmlCollectionConfigStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<XmlSource>(getDefaultSource())

const collectionName = computed(() => nameFromParam(route.params.name))
const index = computed(() => indexFromParam(route.params.index))
const isCreate = computed(() => index.value < 0)

const notFoundMessage = computed(() => {
  if (!store.currentCollection) {
    return `No XML data collection named '${collectionName.value}' exists.`
  }
  return !isCreate.value && index.value >= store.currentCollection.sources.length
    ? `Collection '${collectionName.value}' has no source ${index.value + 1}.`
    : undefined
})

const methodOptions = computed(() => {
  const current = draft.value.request?.method
  return current && !METHODS.includes(current) ? [current, ...METHODS] : METHODS
})

// A stored import of a group file that is gone stays visible, so the user can see and remove it.
const importOptions = computed(() => [...new Set([...store.metadata.groupFiles, ...draft.value.importGroups])].sort())

const errors = computed(() => {
  const otherUrls = (store.currentCollection?.sources ?? [])
    .filter((_, i) => i !== index.value)
    .map(source => source.url)
  return validateSource(draft.value, otherUrls, store.metadata.groupFiles)
})

onMounted(async () => {
  await Promise.all([store.fetchCollection(collectionName.value), store.fetchMetadata()])

  const source = store.sourceAt(isCreate.value ? null : index.value)
  if (source) {
    draft.value = { ...source, request: source.request ? { ...getDefaultRequest(), ...source.request } : null }
  }
  isLoaded.value = true
})

const onToggleRequest = (enabled: boolean) => {
  draft.value.request = enabled ? getDefaultRequest() : null
}

const goBack = () => {
  router.push(collectionPath(collectionName.value))
}

const onSave = async () => {
  const source: XmlSource = { ...draft.value, url: draft.value.url.trim() }

  isSaving.value = true
  const result = await store.saveSource(isCreate.value ? null : index.value, source)
  isSaving.value = false

  if (report(result, isCreate.value ? 'Source added.' : 'Source saved.')) {
    goBack()
  }
}
</script>

<style lang="scss" scoped>
.toggle-label {
  margin-left: 0.5em;
  vertical-align: super;
}

.request {
  display: flex;
  flex-direction: column;
  gap: 1em;
  margin-top: 1em;
  padding: 1em;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(18em, 1fr));
  gap: 1em;
}

h4 {
  margin: 0.5em 0;
}

.field-error {
  display: block;
  margin-bottom: 0.5em;
  font-size: 0.875rem;
  color: var(--p-red-500);
}
</style>
