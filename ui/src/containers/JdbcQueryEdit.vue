<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New JDBC Query' : 'Edit JDBC Query'"
    :sectionTitle="`Query of collection '${collectionName}'`"
    :saveLabel="isCreate ? 'Create Query' : 'Save Changes'"
    testIdPrefix="jdbc-edit"
    wide
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <div class="field-grid">
      <FormField label="Name" for="jdbc-query-name" required :error="errors.name" :info="JDBC_FIELD_HINTS.queryName">
        <OnmsInputText
          inputId="jdbc-query-name"
          data-test="jdbc-query-edit-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>
      <FormField label="Recheck interval (ms)" for="jdbc-query-recheck" required :error="errors.recheckInterval"
        :info="JDBC_FIELD_HINTS.recheckInterval">
        <OnmsInputNumber
          inputId="jdbc-query-recheck"
          data-test="jdbc-query-edit-recheck"
          :modelValue="draft.recheckInterval"
          :min="0"
          :useGrouping="false"
          :invalid="!!errors.recheckInterval"
          fluid
          @update:modelValue="draft.recheckInterval = $event ?? 0"
        />
      </FormField>
      <FormField label="Interface type" for="jdbc-query-iftype" required :error="errors.ifType" :info="JDBC_FIELD_HINTS.ifType">
        <OnmsAutoComplete
          inputId="jdbc-query-iftype"
          data-test="jdbc-query-edit-iftype"
          :modelValue="draft.ifType"
          :suggestions="ifTypeSuggestions"
          :invalid="!!errors.ifType"
          dropdown
          fluid
          @complete="ifTypeSuggestions = suggest(store.metadata.ifTypes, $event)"
          @update:modelValue="draft.ifType = String($event ?? '')"
        />
      </FormField>
      <FormField label="Resource type" for="jdbc-query-resource-type" :error="errors.resourceType" :info="JDBC_FIELD_HINTS.resourceType">
        <OnmsAutoComplete
          inputId="jdbc-query-resource-type"
          data-test="jdbc-query-edit-resource-type"
          :modelValue="draft.resourceType"
          :suggestions="resourceTypeSuggestions"
          :invalid="!!errors.resourceType"
          dropdown
          fluid
          @complete="resourceTypeSuggestions = suggest(store.metadata.resourceTypes, $event)"
          @update:modelValue="draft.resourceType = String($event ?? '')"
        />
      </FormField>
      <FormField label="Instance column" for="jdbc-query-instance-column" :error="errors.instanceColumn"
        :info="JDBC_FIELD_HINTS.instanceColumn">
        <OnmsInputText
          inputId="jdbc-query-instance-column"
          data-test="jdbc-query-edit-instance-column"
          :modelValue="draft.instanceColumn"
          :invalid="!!errors.instanceColumn"
          fluid
          @update:modelValue="draft.instanceColumn = String($event ?? '')"
        />
      </FormField>
    </div>
    <div class="spacer"></div>
    <FormField label="SQL statement" for="jdbc-query-sql" required :error="errors.queryString" :info="JDBC_FIELD_HINTS.queryString">
      <OnmsTextarea
        id="jdbc-query-sql"
        class="sql"
        data-test="jdbc-query-edit-sql"
        :modelValue="draft.queryString"
        :rows="8"
        :invalid="!!errors.queryString"
        autoResize
        fluid
        @update:modelValue="draft.queryString = String($event ?? '')"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField label="Columns" required :error="errors.columns">
      <JdbcColumnsEditor
        :modelValue="draft.columns"
        :types="store.metadata.attributeTypes"
        :errors="errors.columnErrors"
        @update:modelValue="draft.columns = $event"
      />
    </FormField>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import JdbcColumnsEditor from '@/components/JdbcDataCollection/JdbcColumnsEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { JDBC_FIELD_HINTS } from '@/lib/jdbcDataCollectionHelpText'
import { collectionEditPath, indexFromParam, valueFromParam } from '@/lib/jdbcDataCollectionRoutes'
import { hasErrors, validateQuery } from '@/lib/jdbcDataCollectionValidator'
import { getDefaultQuery, useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcQuery } from '@/types/jdbcDataCollection'
import { OnmsAutoComplete, OnmsInputNumber, OnmsInputText, OnmsTextarea } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const store = useJdbcDataCollectionStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const isMissing = ref(false)
const draft = ref<JdbcQuery>(getDefaultQuery())
const ifTypeSuggestions = ref<string[]>([])
const resourceTypeSuggestions = ref<string[]>([])

const isCreate = computed(() => route.name === 'JDBC Query Create')
const sourceId = computed(() => valueFromParam(route.params.id))
const collectionName = computed(() => valueFromParam(route.params.name))
const index = computed(() => (isCreate.value ? -1 : indexFromParam(route.params.index)))

const notFoundMessage = computed(() =>
  isMissing.value ? `Collection '${collectionName.value}' of source '${sourceId.value}' has no such query.` : undefined)

const otherQueryNames = computed(() =>
  (store.currentSource?.collections.find(collection => collection.name === collectionName.value)?.queries ?? [])
    .filter((_query, position) => position !== index.value)
    .map(query => query.name))

const errors = computed(() => validateQuery(draft.value, otherQueryNames.value, store.metadata.attributeTypes))

/** The known values containing what was typed; a free value is still allowed. */
const suggest = (values: string[], typed: string): string[] => {
  const query = (typed ?? '').trim().toLowerCase()
  return query ? values.filter(value => value.toLowerCase().includes(query)) : [...values]
}

onMounted(async () => {
  await Promise.all([store.fetchSource(sourceId.value), store.fetchMetadata()])

  const query = store.queryAt(collectionName.value, index.value)
  isMissing.value = query === null
  draft.value = query ?? getDefaultQuery()
  isLoaded.value = true
})

const goBack = () => {
  router.push(collectionEditPath(sourceId.value, collectionName.value))
}

const onSave = async () => {
  const query: JdbcQuery = {
    ...draft.value,
    name: draft.value.name.trim(),
    columns: draft.value.columns.map(column => ({ ...column, name: column.name.trim(), alias: column.alias.trim() }))
  }

  isSaving.value = true
  const result = await store.saveQuery(collectionName.value, index.value, query)
  isSaving.value = false

  if (report(result, isCreate.value ? 'Query created.' : 'Query saved.')) {
    goBack()
  }
}
</script>

<style lang="scss" scoped>
.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(18em, 1fr));
  gap: 0.75em 1.5em;
}

.sql {
  font-family: monospace;
}
</style>
