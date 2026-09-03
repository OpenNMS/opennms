<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-wsman-container">
    <h1 class="page-title">Manage WS-Man</h1>
    <WsmanHelpPanel />

    <p v-if="store.loadError" class="error" data-test="load-error">
      Failed to load the WS-Man configuration. Check that <code>wsman-config.xml</code> is readable, then reload the page.
    </p>
    <p v-if="actionError" class="error" data-test="action-error">{{ actionError }}</p>

    <OnmsTabs v-if="store.config" v-model:value="activeTab">
      <OnmsTabList>
        <OnmsTab :value="0" data-test="tab-definitions">Server Definitions ({{ store.config.definitions.length }})</OnmsTab>
        <OnmsTab :value="1" data-test="tab-data-collection">Data Collection</OnmsTab>
        <OnmsTab :value="2" data-test="tab-defaults">Agent Defaults</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <WsmanDefinitionsTable
            :definitions="store.config.definitions"
            @add="openDefinition(null)"
            @edit="openDefinition"
            @delete="askDelete"
            @move="moveDefinition"
          />
        </OnmsTabPanel>
        <OnmsTabPanel :value="2">
          <WsmanDefaultsCard :settings="store.config.defaults" @edit="showDefaultsDialog = true" />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <p v-if="store.dataCollectionError" class="error" data-test="data-collection-error">
            Failed to load the WS-Man data collection configuration. Check <code>wsman-datacollection-config.xml</code>
            and the files in <code>wsman-datacollection.d/</code>, then reload the page.
          </p>
          <WsmanDataCollectionPanel
            v-else-if="store.dataCollection"
            :dataCollection="store.dataCollection"
            @add="openDataCollectionEditor"
            @edit="openDataCollectionEditor"
            @delete="askDeleteDataCollection"
          />
          <p v-else class="placeholder" data-test="data-collection-loading">Loading…</p>
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>

    <template v-if="store.dataCollection">
      <WsmanCollectionDialog v-model:visible="showCollectionDialog" :dataCollection="store.dataCollection" :original="editingCollection" />
      <WsmanSystemDefinitionDialog v-model:visible="showSystemDefinitionDialog" :dataCollection="store.dataCollection" :original="editingSystemDefinition" />
      <WsmanGroupDialog v-model:visible="showGroupDialog" :dataCollection="store.dataCollection" :original="editingGroup" />
      <OnmsConfirmationDialog
        :visible="dataCollectionToDelete !== null"
        title="Delete from Data Collection"
        actionButtonText="Delete"
        @ok="confirmDeleteDataCollection"
        @cancel="dataCollectionToDelete = null"
      >
        <template #content>
          <p data-test="delete-dc-confirm-text">
            Delete {{ dataCollectionToDelete?.kind === 'systemDefinition' ? 'system definition' : dataCollectionToDelete?.kind }}
            <strong>{{ dataCollectionToDelete?.item.name }}</strong> from {{ dataCollectionToDelete?.item.source }}?
            The save is refused if something else still references it.
          </p>
        </template>
      </OnmsConfirmationDialog>
    </template>

    <template v-if="store.config">
      <WsmanDefaultsDialog v-model:visible="showDefaultsDialog" :config="store.config" />
      <WsmanDefinitionDialog v-model:visible="showDefinitionDialog" :config="store.config" :index="editingIndex" />
      <OnmsConfirmationDialog
        :visible="deleteIndex !== null"
        title="Delete Server Definition"
        actionButtonText="Delete"
        @ok="confirmDelete"
        @cancel="deleteIndex = null"
      >
        <template #content>
          <p data-test="delete-confirm-text">
            Delete server definition {{ (deleteIndex ?? 0) + 1 }}? Servers it matched will use the next matching definition or the defaults.
          </p>
        </template>
      </OnmsConfirmationDialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { OnmsConfirmationDialog, OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import WsmanDefaultsCard from '@/components/ManageWsman/WsmanDefaultsCard.vue'
import WsmanDefaultsDialog from '@/components/ManageWsman/WsmanDefaultsDialog.vue'
import WsmanDefinitionDialog from '@/components/ManageWsman/WsmanDefinitionDialog.vue'
import WsmanCollectionDialog from '@/components/ManageWsman/WsmanCollectionDialog.vue'
import WsmanDataCollectionPanel from '@/components/ManageWsman/WsmanDataCollectionPanel.vue'
import WsmanGroupDialog from '@/components/ManageWsman/WsmanGroupDialog.vue'
import WsmanSystemDefinitionDialog from '@/components/ManageWsman/WsmanSystemDefinitionDialog.vue'
import { DataCollectionKind, EditableObject, fileInput, remove } from '@/components/ManageWsman/wsmanDataCollectionForm'
import WsmanDefinitionsTable from '@/components/ManageWsman/WsmanDefinitionsTable.vue'
import { configToInput } from '@/components/ManageWsman/wsmanForm'
import WsmanHelpPanel from '@/components/ManageWsman/WsmanHelpPanel.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { BreadCrumb } from '@/types'
import { WsmanCollectionInfo, WsmanGroupInfo, WsmanSystemDefinitionInfo } from '@/types/wsmanAdmin'

const menuStore = useMenuStore()
const store = useWsmanAdminStore()
const activeTab = ref(0)
const showDefaultsDialog = ref(false)
const showDefinitionDialog = ref(false)
const editingIndex = ref<number | null>(null)
const deleteIndex = ref<number | null>(null)
const actionError = ref('')
const showCollectionDialog = ref(false)
const showSystemDefinitionDialog = ref(false)
const showGroupDialog = ref(false)
const editingCollection = ref<WsmanCollectionInfo | null>(null)
const editingSystemDefinition = ref<WsmanSystemDefinitionInfo | null>(null)
const editingGroup = ref<WsmanGroupInfo | null>(null)
const dataCollectionToDelete = ref<{ kind: DataCollectionKind, item: EditableObject } | null>(null)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Manage WS-Man', to: '#', position: 'last' }
])

onMounted(async () => {
  await Promise.all([store.getConfig(), store.getDataCollection()])
})

const openDefinition = (index: number | null) => {
  editingIndex.value = index
  showDefinitionDialog.value = true
}

const askDelete = (index: number) => {
  deleteIndex.value = index
}

// Reordering and deleting resend the document; every surviving definition
// keeps its sourceIndex so its stored password follows it.
const confirmDelete = async () => {
  const index = deleteIndex.value
  deleteIndex.value = null
  if (index === null || !store.config) {
    return
  }
  const input = configToInput(store.config)
  input.definitions.splice(index, 1)
  actionError.value = (await store.saveConfig(input)) ?? ''
}

const openDataCollectionEditor = (kind: DataCollectionKind, item: EditableObject | null = null) => {
  if (kind === 'collection') {
    editingCollection.value = item as WsmanCollectionInfo | null
    showCollectionDialog.value = true
  } else if (kind === 'systemDefinition') {
    editingSystemDefinition.value = item as WsmanSystemDefinitionInfo | null
    showSystemDefinitionDialog.value = true
  } else {
    editingGroup.value = item as WsmanGroupInfo | null
    showGroupDialog.value = true
  }
}

const askDeleteDataCollection = (kind: DataCollectionKind, item: EditableObject) => {
  dataCollectionToDelete.value = { kind, item }
}

// The object is removed from the file it lives in; the server refuses the
// save if a system definition or collection elsewhere still references it.
const confirmDeleteDataCollection = async () => {
  const target = dataCollectionToDelete.value
  dataCollectionToDelete.value = null
  if (!target || !store.dataCollection) {
    return
  }
  const input = fileInput(store.dataCollection, target.item.source)
  if (target.kind === 'collection') {
    input.collections = remove(input.collections, target.item.name)
  } else if (target.kind === 'systemDefinition') {
    input.systemDefinitions = remove(input.systemDefinitions, target.item.name)
  } else {
    input.groups = remove(input.groups, target.item.name)
  }
  actionError.value = (await store.saveDataCollectionFile(target.item.source, input)) ?? ''
}

const moveDefinition = async (index: number, delta: number) => {
  if (!store.config) {
    return
  }
  const input = configToInput(store.config)
  const target = index + delta
  if (target < 0 || target >= input.definitions.length) {
    return
  }
  const [moved] = input.definitions.splice(index, 1)
  input.definitions.splice(target, 0, moved)
  actionError.value = (await store.saveConfig(input)) ?? ''
}
</script>

<style lang="scss" scoped>
.manage-wsman-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.error {
  color: var(--p-red-500, #c62828);
  margin: 0;
}

.placeholder {
  margin: 0;
  font-size: 0.95rem;
  color: var(--p-text-muted-color);
}
</style>
