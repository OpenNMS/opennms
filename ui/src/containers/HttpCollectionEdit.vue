<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New HTTP Collection' : 'Edit HTTP Collection'"
    sectionTitle="Basic Information"
    :saveLabel="isCreate ? 'Create HTTP Collection' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField
      label="Name"
      for="http-collection-name"
      required
      :error="errors.name"
      hint="Selected by a collectd package service through its collection parameter."
    >
      <OnmsInputText
        inputId="http-collection-name"
        data-test="http-collection-edit-name"
        :modelValue="draft.name"
        :invalid="!!errors.name"
        fluid
        @update:modelValue="draft.name = String($event ?? '')"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField label="RRD step (seconds)" for="http-collection-step" required :error="errors.step">
      <OnmsInputNumber
        inputId="http-collection-step"
        data-test="http-collection-edit-step"
        :modelValue="draft.rrd.step"
        :min="1"
        :invalid="!!errors.step"
        fluid
        @update:modelValue="draft.rrd.step = $event ?? null"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField
      label="Round robin archives"
      for="http-collection-rra-0"
      required
      :error="errors.rras"
      hint="Each in the form RRA:(AVERAGE|MIN|MAX|LAST):xff:steps:rows."
    >
      <StringListEditor
        :modelValue="draft.rrd.rras"
        itemLabel="RRA"
        idPrefix="http-collection-rra"
        @update:modelValue="draft.rrd.rras = $event"
      />
    </FormField>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import StringListEditor from '@/components/ThresholdConfiguration/Common/StringListEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { COLLECTIONS_TAB_PATH, collectionPath, nameFromParam } from '@/lib/httpCollectionRoutes'
import { hasErrors, sameCollectionName, validateCollection } from '@/lib/httpCollectionValidator'
import { getDefaultCollection, useHttpCollectionStore } from '@/stores/httpCollectionStore'
import type { HttpCollection } from '@/types/httpCollectionConfig'
import { OnmsInputNumber, OnmsInputText } from '@opennms/onms-ui'
import { cloneDeep } from 'lodash'

const route = useRoute()
const router = useRouter()
const store = useHttpCollectionStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<HttpCollection>(getDefaultCollection())

const isCreate = computed(() => route.name === 'HTTP Collection Create')
const collectionName = computed(() => nameFromParam(route.params.name))

const notFoundMessage = computed(() =>
  !isCreate.value && !store.currentCollection ? `No HTTP collection named '${collectionName.value}' exists.` : undefined
)

const errors = computed(() =>
  validateCollection(
    draft.value,
    // On rename the collection's own name is obviously taken; exclude it so the form does not fight itself.
    store.collectionNames.filter(name => isCreate.value || !sameCollectionName(name, collectionName.value))
  )
)

onMounted(async () => {
  if (isCreate.value) {
    await Promise.all([store.fetchCollections(), store.fetchMetadata()])
    draft.value = getDefaultCollection(store.defaultRrd)
  } else {
    await Promise.all([store.fetchCollection(collectionName.value), store.fetchCollections()])

    if (store.currentCollection) {
      draft.value = cloneDeep(store.currentCollection)
    }
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(isCreate.value ? COLLECTIONS_TAB_PATH : collectionPath(collectionName.value))
}

const onSave = async () => {
  const collection: HttpCollection = {
    ...draft.value,
    name: draft.value.name.trim(),
    rrd: { ...draft.value.rrd, rras: draft.value.rrd.rras.map(rra => rra.trim()) }
  }

  isSaving.value = true

  const result = isCreate.value
    ? await store.createCollection(collection)
    // Only the name and the RRD settings are edited here; keep the URIs and version the store holds.
    : await store.saveCollection(collectionName.value, {
      ...(store.currentCollection as HttpCollection),
      name: collection.name,
      rrd: collection.rrd
    })

  isSaving.value = false

  if (report(result, isCreate.value ? 'HTTP collection created.' : 'HTTP collection saved.')) {
    router.push(collectionPath(collection.name))
  }
}
</script>
