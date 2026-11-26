<template>
  <div class="xml-collection-detail">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <template v-if="collection">
      <TableCard class="xml-card header-card">
        <div class="header">
          <div>
            <h2 data-test="xml-collection-name">{{ collection.name }}</h2>
            <p class="meta" data-test="xml-collection-rrd">
              Step {{ collection.rrd?.step ?? '--' }} s · {{ plural(collection.rrd?.rras.length ?? 0, 'RRA') }}
            </p>
            <ul class="rras">
              <li v-for="rra in collection.rrd?.rras ?? []" :key="rra"><code>{{ rra }}</code></li>
            </ul>
          </div>
          <div class="header-actions">
            <OnmsButton variant="outlined" data-test="xml-collection-edit" @click="router.push(collectionEditPath(collectionName))">
              Edit
            </OnmsButton>
            <OnmsButton variant="outlined" severity="danger" data-test="xml-collection-detail-delete" @click="isDeleteVisible = true">
              Delete
            </OnmsButton>
          </div>
        </div>
      </TableCard>

      <TableCard class="xml-card">
        <div class="table-header">
          <h3>Sources</h3>
          <OnmsButton data-test="xml-source-create" @click="router.push(sourceCreatePath(collectionName))">Add source</OnmsButton>
        </div>
        <OnmsTable :value="sourceRows" data-test="xml-sources-table">
          <OnmsColumn header="URL">
            <template #body="{ data }">
              <router-link :to="sourceEditPath(collectionName, data.index)" data-test="xml-source-link">
                {{ data.source.url }}
              </router-link>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Method">
            <template #body="{ data }">{{ data.source.request?.method || 'GET' }}</template>
          </OnmsColumn>
          <OnmsColumn header="Groups">
            <template #body="{ data }">{{ data.source.groups.length }}</template>
          </OnmsColumn>
          <OnmsColumn header="Imported group files">
            <template #body="{ data }">
              <template v-if="data.source.importGroups.length">
                <span v-for="id in data.source.importGroups" :key="id" class="import-link">
                  <router-link :to="groupFilePath(id)">{{ id }}</router-link>
                </span>
              </template>
              <template v-else>--</template>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ data }">
              <div class="actions">
                <OnmsIconButton
                  :icon="EditIcon"
                  tooltip="Edit source"
                  aria-label="Edit source"
                  data-test="xml-source-edit"
                  @click="router.push(sourceEditPath(collectionName, data.index))"
                />
                <OnmsIconButton
                  :icon="DeleteIcon"
                  severity="danger"
                  tooltip="Delete source"
                  aria-label="Delete source"
                  data-test="xml-source-delete"
                  @click="pendingSourceDelete = data.index"
                />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>
        <EmptyList
          v-if="!sourceRows.length"
          :content="{ msg: 'This collection has no sources yet, so it collects nothing.' }"
          data-test="xml-sources-empty"
        />
      </TableCard>

      <OnmsConfirmationDialog
        v-if="isDeleteVisible"
        :visible="true"
        title="Delete this XML data collection?"
        actionButtonText="Delete"
        data-test="xml-collection-detail-delete-confirm"
        @ok="onDeleteCollection"
        @cancel="isDeleteVisible = false"
      >
        <template #content>
          <p>'{{ collectionName }}' and all of its sources will be removed.</p>
        </template>
      </OnmsConfirmationDialog>

      <OnmsConfirmationDialog
        v-if="pendingSourceDelete !== null"
        :visible="true"
        title="Delete this source?"
        actionButtonText="Delete"
        data-test="xml-source-delete-confirm"
        @ok="onDeleteSource"
        @cancel="pendingSourceDelete = null"
      >
        <template #content>
          <p>'{{ collection.sources[pendingSourceDelete]?.url }}' and its groups will be removed.</p>
        </template>
      </OnmsConfirmationDialog>
    </template>

    <EmptyList
      v-else-if="isLoaded"
      :content="{ msg: `No XML data collection named '${collectionName}' exists.` }"
      data-test="xml-collection-not-found"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { plural } from '@/lib/utils'
import {
  COLLECTIONS_TAB_PATH,
  collectionEditPath,
  groupFilePath,
  nameFromParam,
  sourceCreatePath,
  sourceEditPath,
  xmlBreadcrumbs
} from '@/lib/xmlCollectionRoutes'
import { useMenuStore } from '@/stores/menuStore'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useXmlCollectionConfigStore()
const { report } = useActionFeedback()

const isLoaded = ref(false)
const isDeleteVisible = ref(false)
const pendingSourceDelete = ref<number | null>(null)

const collectionName = computed(() => nameFromParam(route.params.name))
// Only show the loaded collection when it is the one the route asks for, not a leftover of another page.
const collection = computed(() => (store.currentCollection?.name === collectionName.value ? store.currentCollection : null))
const sourceRows = computed(() => (collection.value?.sources ?? []).map((source, index) => ({ source, index })))

const breadcrumbs = computed(() => xmlBreadcrumbs(menuStore.mainMenu, [{ label: collectionName.value }]))

const load = async () => {
  isLoaded.value = false
  report(await store.fetchCollection(collectionName.value))
  isLoaded.value = true
}

onMounted(load)
// Leaving the page changes the route before this component is gone, so only react to this page's route.
watch(collectionName, () => {
  if (route.name === 'XML Collection Detail') {
    load()
  }
})

const onDeleteCollection = async () => {
  isDeleteVisible.value = false

  if (report(await store.deleteCollection(collectionName.value, collection.value?.version), 'XML data collection deleted.')) {
    router.push(COLLECTIONS_TAB_PATH)
  }
}

const onDeleteSource = async () => {
  const index = pendingSourceDelete.value
  pendingSourceDelete.value = null

  if (index !== null) {
    report(await store.deleteSource(index), 'Source deleted.')
  }
}
</script>

<style lang="scss" scoped>
.xml-collection-detail {
  padding: 1.5em;
  display: flex;
  flex-direction: column;
  gap: 1.5em;

  .xml-card {
    padding: 25px;
    border: 1px solid var(--p-content-border-color);
    border-radius: 5px;
  }

  .header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1em;

    h2 {
      margin: 0;
    }

    .meta {
      margin: 0.25em 0;
      color: var(--p-text-muted-color);
    }

    .rras {
      margin: 0;
      padding-left: 1.25em;
    }
  }

  .import-link + .import-link::before {
    content: ', ';
  }

  .header-actions,
  .actions {
    display: flex;
    gap: 0.5em;
  }

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 0.75em;

    h3 {
      margin: 0;
    }
  }
}
</style>
