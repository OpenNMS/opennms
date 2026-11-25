<template>
  <div class="resource-types-config-detail">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <template v-if="store.currentConfig">
      <ResourceTypesConfigHeader
        :config="store.currentConfig"
        @download="onDownload"
        @upload="pendingUpload = $event"
        @rename="router.push(configEditPath(configId))"
        @delete="isDeleteConfirmVisible = true"
      />

      <ResourceTypesTable :config="store.currentConfig" />

      <OnmsConfirmationDialog
        v-if="isDeleteConfirmVisible"
        :visible="true"
        title="Delete this configuration?"
        actionButtonText="Delete"
        data-test="resource-types-config-detail-delete-confirm"
        @ok="onDelete"
        @cancel="isDeleteConfirmVisible = false"
      >
        <template #content>
          <p>{{ configDeleteMessage(configId, typeNames) }}</p>
        </template>
      </OnmsConfirmationDialog>

      <OnmsConfirmationDialog
        v-if="pendingUpload"
        :visible="true"
        title="Replace this configuration?"
        actionButtonText="Replace"
        data-test="resource-types-config-upload-confirm"
        @ok="onConfirmUpload"
        @cancel="pendingUpload = null"
      >
        <template #content>
          <p>
            The resource types of '{{ pendingUpload.name }}' replace those of '{{ configId }}' in full. Resource
            types not in the file are removed.
          </p>
        </template>
      </OnmsConfirmationDialog>
    </template>

    <EmptyList
      v-else-if="!store.isLoading"
      :content="{ msg: `No resource types configuration named '${configId}' exists.` }"
      data-test="resource-types-config-not-found"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import ResourceTypesConfigHeader from '@/components/ResourceTypesConfiguration/ResourceTypesConfigHeader.vue'
import ResourceTypesTable from '@/components/ResourceTypesConfiguration/ResourceTypesTable.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useDownload from '@/composables/useDownload'
import { configDeleteMessage } from '@/lib/resourceTypeHelpText'
import { configEditPath, idFromParam, RESOURCE_TYPES_PATH } from '@/lib/resourceTypeRoutes'
import API from '@/services'
import { useMenuStore } from '@/stores/menuStore'
import { useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { BreadCrumb } from '@/types'
import { OnmsConfirmationDialog } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useResourceTypesConfigStore()
const { report, showError } = useActionFeedback()
const { downloadFile } = useDownload()

const isDeleteConfirmVisible = ref(false)
const pendingUpload = ref<File | null>(null)

const configId = computed(() => idFromParam(route.params.id))
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)
const typeNames = computed(() => (store.currentConfig?.resourceTypes ?? []).map(type => type.name))

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Resource Types Configuration', to: RESOURCE_TYPES_PATH },
  { label: configId.value, to: '#', position: 'last' }
]))

const load = async () => {
  const [config] = await Promise.all([store.fetchConfig(configId.value), store.fetchConfigs()])

  report(config)
}

onMounted(load)

watch(configId, load)

const onDownload = async (isXml: boolean) => {
  const response = await API.downloadResourceTypesConfig(configId.value, isXml)

  if (!response) {
    showError(`Failed to download resource types configuration '${configId.value}'.`)
    return
  }

  downloadFile(response as never, true)
}

const onConfirmUpload = async () => {
  const file = pendingUpload.value
  pendingUpload.value = null

  if (file) {
    report(await store.uploadConfig(configId.value, file, file.name.toLowerCase().endsWith('.xml')), 'Resource types configuration replaced.')
  }
}

const onDelete = async () => {
  isDeleteConfirmVisible.value = false

  const result = await store.deleteConfig(configId.value, store.currentConfig?.version)

  if (report(result, 'Resource types configuration deleted.')) {
    router.push(RESOURCE_TYPES_PATH)
  }
}
</script>

<style lang="scss" scoped>
.resource-types-config-detail {
  padding: 1.5em;
}
</style>
