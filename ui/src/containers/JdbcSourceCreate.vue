<template>
  <ThresholdEditPage
    title="Create New JDBC Data Collection Source"
    sectionTitle="Basic Information"
    saveLabel="Create Source"
    testIdPrefix="jdbc-edit"
    :saveDisabled="hasErrors(errors) || isSaving"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField label="Id" for="jdbc-source-id" required :error="errors.id" :info="JDBC_FIELD_HINTS.sourceId">
      <OnmsInputText
        inputId="jdbc-source-id"
        data-test="jdbc-source-edit-id"
        :modelValue="draft.id"
        :invalid="!!errors.id"
        fluid
        @update:modelValue="draft.id = String($event ?? '')"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField label="RRD repository" for="jdbc-source-repository" :info="JDBC_FIELD_HINTS.rrdRepository">
      <OnmsInputText
        inputId="jdbc-source-repository"
        data-test="jdbc-source-edit-repository"
        :modelValue="draft.rrdRepository"
        fluid
        @update:modelValue="draft.rrdRepository = String($event ?? '')"
      />
    </FormField>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { JDBC_FIELD_HINTS } from '@/lib/jdbcDataCollectionHelpText'
import { JDBC_SOURCES_TAB_PATH, sourcePath } from '@/lib/jdbcDataCollectionRoutes'
import { hasErrors, validateSource } from '@/lib/jdbcDataCollectionValidator'
import { getDefaultSource, useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcSource } from '@/types/jdbcDataCollection'
import { OnmsInputText } from '@opennms/onms-ui'

const router = useRouter()
const store = useJdbcDataCollectionStore()
const { report } = useActionFeedback()

const isSaving = ref(false)
const draft = ref<JdbcSource>(getDefaultSource())

const errors = computed(() => validateSource(draft.value, store.sourceIds))

onMounted(async () => {
  await Promise.all([store.fetchSources(), store.fetchMetadata()])
  draft.value = { ...getDefaultSource(store.metadata), id: draft.value.id }
})

const goBack = () => {
  router.push(JDBC_SOURCES_TAB_PATH)
}

const onSave = async () => {
  const id = draft.value.id.trim()

  isSaving.value = true
  const result = await store.createSource({ ...draft.value, id })
  isSaving.value = false

  if (report(result, 'Source created.')) {
    router.push(sourcePath(id))
  }
}
</script>
