<template>
  <div v-if="isLoaded" class="jdbc-collection-edit">
    <ThresholdEditPage
      :title="isCreate ? 'Create New JDBC Collection' : 'Edit JDBC Collection'"
      :sectionTitle="`Collection settings (source '${sourceId}')`"
      :saveLabel="isCreate ? 'Create Collection' : 'Save Changes'"
      testIdPrefix="jdbc-edit"
      :saveDisabled="hasErrors(errors) || isSaving || !isDirty"
      :notFoundMessage="notFoundMessage"
      @back="goBack"
      @cancel="goBack"
      @save="onSave"
    >
      <OnmsMessage
        v-if="stored?.shadowedBy"
        severity="warn"
        data-test="jdbc-collection-shadowed-message"
      >
        Source '{{ stored.shadowedBy }}' also defines a collection named '{{ stored.name }}', and the collector uses
        that one. Rename this collection or delete one of the two.
      </OnmsMessage>
      <FormField label="Name" for="jdbc-collection-name" required :error="errors.name" :info="JDBC_FIELD_HINTS.collectionName"
        :hint="renameHint">
        <OnmsInputText
          inputId="jdbc-collection-name"
          data-test="jdbc-collection-edit-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>
      <div class="spacer"></div>
      <FormField label="RRD step (seconds)" for="jdbc-collection-step" required :error="errors.rrdStep" :info="JDBC_FIELD_HINTS.rrdStep">
        <OnmsInputNumber
          inputId="jdbc-collection-step"
          data-test="jdbc-collection-edit-step"
          :modelValue="draft.rrdStep"
          :min="1"
          :useGrouping="false"
          :invalid="!!errors.rrdStep"
          @update:modelValue="draft.rrdStep = $event ?? null"
        />
      </FormField>
      <div class="spacer"></div>
      <FormField label="RRAs" for="jdbc-collection-rra-0" required :error="errors.rras" :info="JDBC_FIELD_HINTS.rras">
        <StringListEditor
          :modelValue="draft.rras"
          itemLabel="RRA"
          idPrefix="jdbc-collection-rra"
          @update:modelValue="draft.rras = $event"
        />
      </FormField>
      <p v-if="isCreate" class="note">Queries can be added once the collection is created.</p>
    </ThresholdEditPage>

    <JdbcQueriesTable
      v-if="!isCreate && stored"
      class="queries"
      :sourceId="sourceId"
      :collection="stored"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { isEqual } from 'lodash'
import FormField from '@/components/Common/FormField.vue'
import JdbcQueriesTable from '@/components/JdbcDataCollection/JdbcQueriesTable.vue'
import StringListEditor from '@/components/ThresholdConfiguration/Common/StringListEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { JDBC_FIELD_HINTS } from '@/lib/jdbcDataCollectionHelpText'
import { collectionEditPath, sourcePath, valueFromParam } from '@/lib/jdbcDataCollectionRoutes'
import { hasErrors, validateCollection } from '@/lib/jdbcDataCollectionValidator'
import { getDefaultCollection, useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcCollection } from '@/types/jdbcDataCollection'
import { OnmsInputNumber, OnmsInputText, OnmsMessage } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const store = useJdbcDataCollectionStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<JdbcCollection>(getDefaultCollection())

const isCreate = computed(() => route.name === 'JDBC Collection Create')
const sourceId = computed(() => valueFromParam(route.params.id))
const collectionName = computed(() => valueFromParam(route.params.name))

/** The stored collection, which also holds the queries; null on create or when it does not exist. */
const stored = computed(() =>
  isCreate.value ? null : (store.currentSource?.collections.find(collection => collection.name === collectionName.value) ?? null))

const notFoundMessage = computed(() => {
  if (!store.currentSource) {
    return `No JDBC data collection source '${sourceId.value}' exists.`
  }
  if (!isCreate.value && !stored.value) {
    return `Source '${sourceId.value}' has no collection named '${collectionName.value}'.`
  }
  return undefined
})

const errors = computed(() =>
  validateCollection(draft.value, store.takenCollectionNames(sourceId.value, isCreate.value ? undefined : collectionName.value)))

const isDirty = computed(() => isCreate.value || !stored.value ||
  draft.value.name.trim() !== stored.value.name ||
  draft.value.rrdStep !== stored.value.rrdStep ||
  !isEqual(draft.value.rras.map(rra => rra.trim()), stored.value.rras))

const renameHint = computed(() =>
  !isCreate.value && stored.value?.usedBy?.length && draft.value.name.trim() !== stored.value.name
    ? `Used by ${stored.value.usedBy.join(', ')}; their 'collection' parameter is not renamed.`
    : undefined)

onMounted(async () => {
  await Promise.all([store.fetchSource(sourceId.value), store.fetchSources(), store.fetchMetadata()])

  draft.value = (isCreate.value ? null : store.collectionAt(collectionName.value)) ?? getDefaultCollection(store.metadata)
  isLoaded.value = true
})

const goBack = () => {
  router.push(sourcePath(sourceId.value))
}

const onSave = async () => {
  const name = draft.value.name.trim()
  const settings = { name, rrdStep: draft.value.rrdStep, rras: draft.value.rras.map(rra => rra.trim()) }

  isSaving.value = true
  const result = isCreate.value
    ? await store.createCollection({ ...draft.value, ...settings, queries: [] })
    // Only the settings are edited here; keep the queries the store holds.
    : await store.saveCollection(collectionName.value, { ...(stored.value as JdbcCollection), ...settings })
  isSaving.value = false

  if (report(result, isCreate.value ? 'Collection created.' : 'Collection saved.')) {
    if (isCreate.value || name !== collectionName.value) {
      // a new collection continues with its queries; a renamed one lives under its new path
      router.replace(collectionEditPath(sourceId.value, name))
    }
    draft.value = store.collectionAt(name) ?? draft.value
  }
}
</script>

<style lang="scss" scoped>
.jdbc-collection-edit {
  .queries {
    margin: 0 30px 30px 30px;
  }

  .note {
    color: var(--p-text-muted-color);
  }
}
</style>
