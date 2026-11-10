<template>
  <div class="jdbc-source-detail">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <template v-if="source">
      <div class="source-header">
        <div class="left">
          <OnmsButton variant="text" data-test="jdbc-source-back" @click="router.push(JDBC_SOURCES_TAB_PATH)">
            <OnmsIcon :icon="ArrowBack" />
            Go Back
          </OnmsButton>
          <h2 data-test="jdbc-source-id">{{ source.id }}</h2>
        </div>
        <div class="right">
          <OnmsButton variant="outlined" data-test="jdbc-source-download-xml" @click="onDownload(true)">Download XML</OnmsButton>
          <OnmsButton variant="outlined" data-test="jdbc-source-download-json" @click="onDownload(false)">Download JSON</OnmsButton>
          <OnmsButton
            v-if="!isDefault"
            variant="outlined"
            severity="danger"
            data-test="jdbc-source-delete"
            @click="isDeleteConfirmVisible = true"
          >
            Delete source
          </OnmsButton>
        </div>
      </div>

      <TableCard class="jdbc-card repository">
        <FormField label="RRD repository" for="jdbc-source-repository" :info="JDBC_FIELD_HINTS.rrdRepository" :hint="repositoryHint">
          <div class="repository-row">
            <OnmsInputText
              inputId="jdbc-source-repository"
              data-test="jdbc-source-repository"
              :modelValue="repository"
              fluid
              @update:modelValue="repository = String($event ?? '')"
            />
            <OnmsButton
              data-test="jdbc-source-repository-save"
              :disabled="repository.trim() === (source.rrdRepository ?? '') || isSaving"
              @click="onSaveRepository"
            >
              Save
            </OnmsButton>
          </div>
        </FormField>
      </TableCard>

      <JdbcCollectionsTable :source="source" />

      <OnmsConfirmationDialog
        v-if="isDeleteConfirmVisible"
        :visible="true"
        title="Delete this source?"
        actionButtonText="Delete"
        data-test="jdbc-source-delete-confirm"
        @ok="onDelete"
        @cancel="isDeleteConfirmVisible = false"
      >
        <template #content>
          <p>
            '{{ source.id }}' and all of its collections will be removed. Collectd services using one of them
            stop collecting.
          </p>
        </template>
      </OnmsConfirmationDialog>
    </template>

    <EmptyList
      v-else-if="isLoaded"
      :content="{ msg: `No JDBC data collection source '${sourceId}' exists.` }"
      data-test="jdbc-source-not-found"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import JdbcCollectionsTable from '@/components/JdbcDataCollection/JdbcCollectionsTable.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useDownload from '@/composables/useDownload'
import { JDBC_FIELD_HINTS } from '@/lib/jdbcDataCollectionHelpText'
import { JDBC_HOME_PATH, JDBC_SOURCES_TAB_PATH, valueFromParam } from '@/lib/jdbcDataCollectionRoutes'
import { DEFAULT_SOURCE_ID } from '@/lib/jdbcDataCollectionValidator'
import API from '@/services'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { OnmsButton, OnmsConfirmationDialog, OnmsIcon, OnmsInputText } from '@opennms/onms-ui'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useJdbcDataCollectionStore()
const { report, showError } = useActionFeedback()
const { downloadFile } = useDownload()

const isLoaded = ref(false)
const isSaving = ref(false)
const isDeleteConfirmVisible = ref(false)
const repository = ref('')

const sourceId = computed(() => valueFromParam(route.params.id))
const source = computed(() => (store.currentSource?.id === sourceId.value ? store.currentSource : null))
const isDefault = computed(() => sourceId.value === DEFAULT_SOURCE_ID)
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'JDBC Data Collection', to: JDBC_HOME_PATH },
  { label: sourceId.value, to: '#', position: 'last' }
]))

/** The collector uses one repository for all collections; say so when it is not this one. */
const repositoryHint = computed(() => {
  const effective = source.value?.effectiveRrdRepository
  return effective && effective !== (source.value?.rrdRepository ?? '')
    ? `The collector currently uses '${effective}' for all collections.`
    : undefined
})

watch(source, (value) => {
  repository.value = value?.rrdRepository ?? ''
}, { immediate: true })

const load = async () => {
  isLoaded.value = false
  const [result] = await Promise.all([store.fetchSource(sourceId.value), store.fetchSources()])
  isLoaded.value = true

  // a 404 shows the empty state, anything else the server's message
  if (!result.success && result.status !== 404) {
    report(result)
  }
}

onMounted(load)

watch(sourceId, load)

const onSaveRepository = async () => {
  isSaving.value = true
  const result = await store.updateRepository(repository.value.trim())
  isSaving.value = false

  report(result, 'RRD repository saved.')
}

const onDownload = async (isXml: boolean) => {
  const response = await API.downloadJdbcSource(sourceId.value, isXml)

  if (!response) {
    showError(`Failed to download source '${sourceId.value}'.`)
    return
  }

  downloadFile(response as never, true)
}

const onDelete = async () => {
  isDeleteConfirmVisible.value = false

  const result = await store.deleteSource(sourceId.value, store.currentSource?.version)

  if (report(result, 'Source deleted.')) {
    router.push(JDBC_SOURCES_TAB_PATH)
  }
}
</script>

<style lang="scss" scoped>
.jdbc-source-detail {
  padding: 1.5em;
  display: flex;
  flex-direction: column;
  gap: 1.25em;

  .source-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;

    .left {
      display: flex;
      align-items: center;
      gap: 20px;

      h2 {
        margin: 0;
      }
    }

    .right {
      display: flex;
      gap: 0.5em;
    }
  }

  .jdbc-card {
    padding: 25px;
    border: 1px solid var(--p-content-border-color);
    border-radius: 5px;
  }

  .repository-row {
    display: flex;
    align-items: center;
    gap: 0.5em;
    max-width: 50em;
  }
}
</style>
