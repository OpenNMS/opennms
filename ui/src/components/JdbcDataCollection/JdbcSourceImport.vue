<template>
  <TableCard class="jdbc-card">
    <div class="import">
      <p>
        Upload a JDBC data collection file to store it as a source: an XML file in the format of the former
        jdbc-datacollection.d/*.xml files, for example from etc_archive, or a JSON file downloaded from a source.
      </p>

      <label class="upload-label">
        <input
          ref="fileInput"
          type="file"
          accept=".json,.xml,application/json,application/xml,text/xml"
          data-test="jdbc-import-file"
          @change="onFileChange"
        >
      </label>

      <div class="fields">
        <FormField label="Source id" for="jdbc-import-id" :error="idError" :info="JDBC_FIELD_HINTS.sourceId"
          hint="Taken from the file name when empty.">
          <OnmsInputText
            inputId="jdbc-import-id"
            data-test="jdbc-import-id"
            :modelValue="id"
            :invalid="!!idError"
            fluid
            @update:modelValue="id = String($event ?? '')"
          />
        </FormField>
        <FormField label="Replace an existing source" for="jdbc-import-replace">
          <OnmsToggleSwitch
            inputId="jdbc-import-replace"
            data-test="jdbc-import-replace"
            :modelValue="replace"
            @update:modelValue="replace = !!$event"
          />
        </FormField>
      </div>

      <div class="actions">
        <OnmsButton
          data-test="jdbc-import-upload"
          :disabled="!file || !!idError || isUploading"
          @click="onUpload"
        >
          Upload
        </OnmsButton>
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { JDBC_FIELD_HINTS } from '@/lib/jdbcDataCollectionHelpText'
import { sourcePath } from '@/lib/jdbcDataCollectionRoutes'
import { SOURCE_ID_PATTERN } from '@/lib/jdbcDataCollectionValidator'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import { OnmsButton, OnmsInputText, OnmsToggleSwitch } from '@opennms/onms-ui'

/** The suffixes the server strips from a file name, including the timestamp of a file from etc_archive. */
const FILE_SUFFIX = /\.(xml|json)(\.\d+)?$/i

const store = useJdbcDataCollectionStore()
const router = useRouter()
const { report } = useActionFeedback()

const fileInput = ref<HTMLInputElement | null>(null)
const file = ref<File | null>(null)
const id = ref('')
const replace = ref(false)
const isUploading = ref(false)

const idError = computed(() => {
  const value = id.value.trim()
  return value && !SOURCE_ID_PATTERN.test(value)
    ? 'Start with a letter or digit and use only letters, digits, \'.\', \'_\' and \'-\'.'
    : undefined
})

const onFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  file.value = input.files?.[0] ?? null

  if (file.value) {
    id.value = file.value.name.replace(FILE_SUFFIX, '')
  }
}

const onUpload = async () => {
  if (!file.value) {
    return
  }

  isUploading.value = true
  const result = await store.uploadSource(file.value, id.value, replace.value)
  isUploading.value = false

  if (report(result, 'Source stored.') && result.payload) {
    file.value = null
    id.value = ''
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    router.push(sourcePath(result.payload.id))
  }
}
</script>

<style lang="scss" scoped>
.jdbc-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.import {
  display: flex;
  flex-direction: column;
  gap: 1em;
  max-width: 50em;

  p {
    margin: 0;
  }

  .fields {
    display: flex;
    flex-direction: column;
    gap: 0.75em;
  }

  .actions {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
