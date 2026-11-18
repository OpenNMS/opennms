<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New URI' : 'Edit URI'"
    :sectionTitle="`URI of collection '${collectionName}'`"
    :saveLabel="isCreate ? 'Create URI' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    wide
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <div class="field-grid">
      <FormField label="Name" for="http-uri-name" required :error="errors.name" class="span-two">
        <OnmsInputText
          inputId="http-uri-name"
          data-test="http-uri-name"
          :modelValue="draft.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="draft.name = String($event ?? '')"
        />
      </FormField>
    </div>

    <h4>Request</h4>
    <div class="field-grid">
      <FormField label="Method" for="http-uri-method">
        <OnmsSelect
          inputId="http-uri-method"
          data-test="http-uri-method"
          :options="store.methods"
          :modelValue="url.method"
          fluid
          @update:modelValue="url.method = String($event ?? 'GET')"
        />
      </FormField>
      <FormField label="HTTP version" for="http-uri-http-version">
        <OnmsSelect
          inputId="http-uri-http-version"
          data-test="http-uri-http-version"
          :options="store.httpVersions"
          :modelValue="url.httpVersion"
          fluid
          @update:modelValue="url.httpVersion = String($event ?? '1.1')"
        />
      </FormField>
      <FormField label="Scheme" for="http-uri-scheme">
        <OnmsInputText
          inputId="http-uri-scheme"
          data-test="http-uri-scheme"
          :modelValue="url.scheme ?? ''"
          fluid
          @update:modelValue="url.scheme = optional($event)"
        />
      </FormField>
      <FormField label="Host" for="http-uri-host" hint="${ipaddr} is the address of the interface being collected.">
        <OnmsInputText
          inputId="http-uri-host"
          data-test="http-uri-host"
          :modelValue="url.host ?? ''"
          fluid
          @update:modelValue="url.host = optional($event)"
        />
      </FormField>
      <FormField label="Port" for="http-uri-port" :error="errors.port">
        <OnmsInputNumber
          inputId="http-uri-port"
          data-test="http-uri-port"
          :modelValue="url.port"
          :min="1"
          :max="65535"
          :invalid="!!errors.port"
          fluid
          @update:modelValue="url.port = $event ?? null"
        />
      </FormField>
      <FormField label="Path" for="http-uri-path" required :error="errors.path">
        <OnmsInputText
          inputId="http-uri-path"
          data-test="http-uri-path"
          :modelValue="url.path"
          :invalid="!!errors.path"
          fluid
          @update:modelValue="url.path = String($event ?? '')"
        />
      </FormField>
      <FormField label="Query" for="http-uri-query" hint="Without the leading ?.">
        <OnmsInputText
          inputId="http-uri-query"
          data-test="http-uri-query"
          :modelValue="url.query ?? ''"
          fluid
          @update:modelValue="url.query = optional($event)"
        />
      </FormField>
      <FormField label="Fragment" for="http-uri-fragment">
        <OnmsInputText
          inputId="http-uri-fragment"
          data-test="http-uri-fragment"
          :modelValue="url.fragment ?? ''"
          fluid
          @update:modelValue="url.fragment = optional($event)"
        />
      </FormField>
      <FormField label="User info" for="http-uri-user-info" hint="user:password for basic authentication.">
        <OnmsInputText
          inputId="http-uri-user-info"
          data-test="http-uri-user-info"
          :modelValue="url.userInfo ?? ''"
          fluid
          @update:modelValue="url.userInfo = optional($event)"
        />
      </FormField>
      <FormField label="Virtual host" for="http-uri-virtual-host" hint="Host header for name-based virtual hosts.">
        <OnmsInputText
          inputId="http-uri-virtual-host"
          data-test="http-uri-virtual-host"
          :modelValue="url.virtualHost ?? ''"
          fluid
          @update:modelValue="url.virtualHost = optional($event)"
        />
      </FormField>
      <FormField label="User agent" for="http-uri-user-agent" class="span-two">
        <OnmsInputText
          inputId="http-uri-user-agent"
          data-test="http-uri-user-agent"
          :modelValue="url.userAgent ?? ''"
          fluid
          @update:modelValue="url.userAgent = optional($event)"
        />
      </FormField>
    </div>

    <h4>Headers</h4>
    <FormField label="" for="http-uri-header-key-0" :error="errors.headers">
      <KeyValueListEditor
        :modelValue="url.headers ?? []"
        keyField="name"
        keyLabel="Name"
        itemLabel="header"
        idPrefix="http-uri-header"
        @update:modelValue="url.headers = $event"
      />
    </FormField>

    <h4>Parameters</h4>
    <FormField label="" for="http-uri-parameter-key-0" :error="errors.parameters" hint="Form parameters sent with a POST request.">
      <KeyValueListEditor
        :modelValue="url.parameters ?? []"
        keyField="key"
        keyLabel="Key"
        itemLabel="parameter"
        idPrefix="http-uri-parameter"
        @update:modelValue="url.parameters = $event"
      />
    </FormField>

    <h4>Response</h4>
    <div class="field-grid">
      <FormField
        label="Matches"
        for="http-uri-matches"
        :error="errors.matches"
        hint="Java regular expression applied to the response body; its groups provide the attribute values."
        class="span-two"
      >
        <OnmsInputText
          inputId="http-uri-matches"
          data-test="http-uri-matches"
          :modelValue="url.matches ?? ''"
          fluid
          @update:modelValue="url.matches = optional($event)"
        />
      </FormField>
      <FormField label="Response range" for="http-uri-response-range" :error="errors.responseRange">
        <OnmsInputText
          inputId="http-uri-response-range"
          data-test="http-uri-response-range"
          :modelValue="url.responseRange ?? ''"
          :invalid="!!errors.responseRange"
          fluid
          @update:modelValue="url.responseRange = optional($event)"
        />
      </FormField>
    </div>
    <div class="flags" data-test="http-uri-flags">
      <div v-for="flag in REGEX_FLAGS" :key="flag.field" class="flag">
        <OnmsCheckbox
          :inputId="`http-uri-flag-${flag.field}`"
          :data-test="`http-uri-flag-${flag.field}`"
          :modelValue="!!url[flag.field]"
          @update:modelValue="url[flag.field] = !!$event"
        />
        <label :for="`http-uri-flag-${flag.field}`">{{ flag.label }}</label>
      </div>
    </div>

    <h4>Attributes</h4>
    <FormField label="" for="http-attrib-alias-0" :error="errors.attributes">
      <HttpAttributeListEditor
        :modelValue="draft.attributes ?? []"
        :types="store.attributeTypes"
        @update:modelValue="draft.attributes = $event"
      />
    </FormField>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import HttpAttributeListEditor from '@/components/HttpCollection/HttpAttributeListEditor.vue'
import KeyValueListEditor from '@/components/HttpCollection/KeyValueListEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { collectionPath, indexFromParam, nameFromParam } from '@/lib/httpCollectionRoutes'
import { hasErrors, validateUri } from '@/lib/httpCollectionValidator'
import { getDefaultUri, useHttpCollectionStore } from '@/stores/httpCollectionStore'
import type { HttpUri, HttpUrl } from '@/types/httpCollectionConfig'
import { OnmsCheckbox, OnmsInputNumber, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

type FlagField = 'canonicalEquivalence' | 'caseInsensitive' | 'comments' | 'dotall' | 'literal' | 'multiline' | 'unicodeCase' | 'unixLines'

const REGEX_FLAGS: { field: FlagField, label: string }[] = [
  { field: 'caseInsensitive', label: 'Case insensitive' },
  { field: 'dotall', label: 'Dot matches line breaks (dotall)' },
  { field: 'multiline', label: 'Multiline' },
  { field: 'comments', label: 'Allow whitespace and comments' },
  { field: 'literal', label: 'Literal' },
  { field: 'unicodeCase', label: 'Unicode case' },
  { field: 'canonicalEquivalence', label: 'Canonical equivalence' },
  { field: 'unixLines', label: 'Unix lines' }
]

const route = useRoute()
const router = useRouter()
const store = useHttpCollectionStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const isMissing = ref(false)
const draft = ref<HttpUri>(getDefaultUri())

const url = computed<HttpUrl>(() => draft.value.url)

const isCreate = computed(() => route.name === 'HTTP Collection URI Create')
const collectionName = computed(() => nameFromParam(route.params.name))
const index = computed(() => (isCreate.value ? null : indexFromParam(route.params.index)))

const notFoundMessage = computed(() => {
  if (!store.currentCollection) {
    return `No HTTP collection named '${collectionName.value}' exists.`
  }
  return isMissing.value ? `HTTP collection '${collectionName.value}' has no URI at position ${index.value}.` : undefined
})

const errors = computed(() =>
  validateUri(
    draft.value,
    store.currentUris.filter((_, i) => i !== index.value).map(uri => uri.name)
  )
)

/** An empty optional field is sent as null, so the server applies the schema default. */
const optional = (value: unknown): string | null => {
  const text = String(value ?? '')
  return text.trim().length ? text : null
}

onMounted(async () => {
  await Promise.all([store.fetchCollection(collectionName.value), store.fetchMetadata()])

  const uri = store.uriAt(index.value)
  if (uri) {
    draft.value = uri
    draft.value.url.headers = draft.value.url.headers ?? []
    draft.value.url.parameters = draft.value.url.parameters ?? []
    draft.value.attributes = draft.value.attributes ?? []
  } else {
    isMissing.value = true
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(collectionPath(collectionName.value))
}

const onSave = async () => {
  const uri: HttpUri = {
    ...draft.value,
    name: draft.value.name.trim(),
    url: { ...draft.value.url, path: draft.value.url.path.trim() },
    attributes: (draft.value.attributes ?? []).map(attrib => ({ ...attrib, alias: attrib.alias.trim() }))
  }

  isSaving.value = true
  const result = await store.saveUri(index.value, uri)
  isSaving.value = false

  if (report(result, isCreate.value ? 'URI created.' : 'URI saved.')) {
    goBack()
  }
}
</script>

<style lang="scss" scoped>
h4 {
  margin: 1.5em 0 0.5em 0;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75em 1.25em;
}

.span-two {
  grid-column: span 2;
}

.flags {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.5em 1.25em;
  margin-top: 0.75em;

  .flag {
    display: flex;
    align-items: center;
    gap: 0.5em;
  }
}

@media (max-width: 768px) {
  .field-grid,
  .flags {
    grid-template-columns: minmax(0, 1fr);
  }

  .span-two {
    grid-column: span 1;
  }
}
</style>
