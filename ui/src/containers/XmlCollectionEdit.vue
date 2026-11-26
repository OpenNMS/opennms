<template>
  <ThresholdEditPage
    v-if="isLoaded"
    :title="isCreate ? 'Create New XML Data Collection' : 'Edit XML Data Collection'"
    sectionTitle="Basic Information"
    :saveLabel="isCreate ? 'Create Collection' : 'Save Changes'"
    :saveDisabled="hasErrors(errors) || isSaving"
    :notFoundMessage="notFoundMessage"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField label="Name" for="xml-collection-name" required :error="errors.name" :info="XML_FIELD_HINTS.collectionName">
      <OnmsInputText
        inputId="xml-collection-name"
        data-test="xml-collection-edit-name"
        :modelValue="draft.name"
        :invalid="!!errors.name"
        fluid
        @update:modelValue="draft.name = String($event ?? '')"
      />
    </FormField>
    <small v-if="isRenamed" class="field-hint warning" data-test="xml-collection-rename-warning">
      Collectd services that use '{{ collectionName }}' are not updated; change their collection parameter as well.
    </small>
    <div class="spacer"></div>
    <FormField label="Step (seconds)" for="xml-collection-step" required :error="errors.step" :info="XML_FIELD_HINTS.step">
      <OnmsInputNumber
        inputId="xml-collection-step"
        data-test="xml-collection-edit-step"
        :modelValue="draft.rrd.step"
        :min="1"
        :useGrouping="false"
        :invalid="!!errors.step"
        fluid
        @update:modelValue="draft.rrd.step = $event === null || $event === undefined ? null : Number($event)"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField label="RRAs" required :error="errors.rras" :info="XML_FIELD_HINTS.rras">
      <StringListEditor
        :modelValue="draft.rrd.rras"
        itemLabel="RRA"
        idPrefix="xml-collection-rra"
        @update:modelValue="draft.rrd.rras = $event"
      />
    </FormField>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import StringListEditor from '@/components/Common/StringListEditor.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { cloneState } from '@/lib/utils'
import { XML_FIELD_HINTS } from '@/lib/xmlCollectionHelpText'
import { COLLECTIONS_TAB_PATH, collectionPath, nameFromParam } from '@/lib/xmlCollectionRoutes'
import { hasErrors, validateCollectionBasics } from '@/lib/xmlCollectionValidator'
import { getDefaultCollection, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import type { XmlCollection } from '@/types/xmlCollectionConfig'
import { OnmsInputNumber, OnmsInputText } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const store = useXmlCollectionConfigStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<XmlCollection>(getDefaultCollection())

const isCreate = computed(() => route.name === 'XML Collection Create')
const collectionName = computed(() => nameFromParam(route.params.name))
const isRenamed = computed(() => !isCreate.value && draft.value.name.trim() !== collectionName.value)

const notFoundMessage = computed(() =>
  !isCreate.value && !store.currentCollection ? `No XML data collection named '${collectionName.value}' exists.` : undefined
)

const errors = computed(() =>
  validateCollectionBasics(
    draft.value.name,
    draft.value.rrd,
    // On edit the collection's own name is obviously taken; exclude it so the form does not fight itself.
    store.collectionNames.filter(name => isCreate.value || name !== collectionName.value)
  )
)

onMounted(async () => {
  if (isCreate.value) {
    await store.fetchCollections()
  } else {
    await Promise.all([store.fetchCollection(collectionName.value), store.fetchCollections()])

    if (store.currentCollection) {
      const loaded = cloneState(store.currentCollection)
      draft.value = { ...loaded, rrd: loaded.rrd ?? { step: null, rras: [] }}
    }
  }

  isLoaded.value = true
})

const goBack = () => {
  router.push(isCreate.value ? COLLECTIONS_TAB_PATH : collectionPath(collectionName.value))
}

const onSave = async () => {
  const collection: XmlCollection = {
    ...draft.value,
    name: draft.value.name.trim(),
    rrd: { step: draft.value.rrd.step, rras: draft.value.rrd.rras.map(rra => rra.trim()) }
  }

  isSaving.value = true
  const result = isCreate.value
    ? await store.createCollection(collection)
    // Only the basics are edited here; the sources and version are those the store holds.
    : await store.updateCollection(collectionName.value, {
      ...(store.currentCollection as XmlCollection),
      name: collection.name,
      rrd: collection.rrd
    })
  isSaving.value = false

  if (report(result, isCreate.value ? 'XML data collection created.' : 'XML data collection saved.')) {
    router.push(collectionPath(collection.name))
  }
}
</script>

<style lang="scss" scoped>
.warning {
  color: var(--p-orange-500);
}
</style>
