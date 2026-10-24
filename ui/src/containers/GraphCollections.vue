<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="graph-collections">
    <TableCard>
      <div class="header">
        <div class="title-block">
          <div class="card-title">Graph Collections</div>
          <div class="subtitle">Saved sets of performance graphs, each drawn for one resource over a named timespan.</div>
        </div>
        <OnmsButton
          v-if="canEdit"
          icon="pi pi-plus"
          label="Create collection"
          data-test="create-collection"
          @click="router.push('/graph-collections/new')"
        />
      </div>

      <p v-if="loadError" class="error" data-test="load-error">{{ loadError }}</p>

      <OnmsTable
        v-if="collections.length"
        :value="collections"
        paginator
        dataKey="id"
        :rows="20"
        :rowsPerPageOptions="[20, 50, 100]"
        data-test="collections-table"
      >
        <OnmsColumn field="title" header="Title" sortable>
          <template #body="{ data }">
            <router-link :to="viewRoute(data)" class="wrap-cell" data-test="collection-title">{{ data.title }}</router-link>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Graphs">
          <template #body="{ data }"><span data-test="collection-graphs">{{ data.graphs.length }}</span></template>
        </OnmsColumn>
        <OnmsColumn header="Per row">
          <template #body="{ data }">{{ data.graphsPerLine || 'Default' }}</template>
        </OnmsColumn>
        <OnmsColumn header="Overrides">
          <template #body="{ data }">
            <span class="overrides" data-test="collection-overrides">{{ overridesSummary(data) }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Actions">
          <template #body="{ data }">
            <div class="action-container">
              <OnmsIconButton
                :icon="View"
                :tooltip="`View ${data.title}`"
                :aria-label="`View ${data.title}`"
                data-test="view-collection"
                @click="router.push(viewRoute(data))"
              />
              <OnmsIconButton
                v-if="canEdit"
                :icon="Edit"
                :tooltip="`Edit ${data.title}`"
                :aria-label="`Edit ${data.title}`"
                data-test="edit-collection"
                @click="router.push(`/graph-collections/${data.id}/edit`)"
              />
              <OnmsIconButton
                v-if="canEdit"
                :icon="ContentCopy"
                :tooltip="`Duplicate ${data.title}`"
                :aria-label="`Duplicate ${data.title}`"
                data-test="duplicate-collection"
                @click="router.push({ path: '/graph-collections/new', query: { from: String(data.id) }})"
              />
              <OnmsIconButton
                v-if="canEdit"
                :icon="Delete"
                severity="danger"
                :tooltip="`Delete ${data.title}`"
                :aria-label="`Delete ${data.title}`"
                data-test="delete-collection"
                @click="collectionToDelete = data"
              />
            </div>
          </template>
        </OnmsColumn>
      </OnmsTable>

      <div v-else-if="!loading && !loadError">
        <EmptyList :content="emptyContent" data-test="empty-list" />
      </div>
    </TableCard>

    <NodeCollectionPicker class="node-picker" />

    <OnmsConfirmationDialog
      :visible="!!collectionToDelete"
      title="Delete Graph Collection"
      actionButtonText="Delete"
      @ok="confirmDelete"
      @cancel="collectionToDelete = null"
    >
      <template #content>
        <p>
          Are you sure you want to delete the graph collection <strong>{{ collectionToDelete?.title }}</strong>
          and its {{ collectionToDelete?.graphs.length ?? 0 }} graph{{ collectionToDelete?.graphs.length === 1 ? '' : 's' }}?
          This action cannot be undone.
        </p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import ContentCopy from '@opennms/onms-ui/icons/action/ContentCopy.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import View from '@opennms/onms-ui/icons/action/View.vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import TableCard from '@/components/Common/TableCard.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import NodeCollectionPicker from '@/components/GraphCollections/NodeCollectionPicker.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useRole from '@/composables/useRole'
import { deleteGraphCollection, getGraphCollections } from '@/services/graphCollectionsService'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { GraphCollection } from '@/types/graphCollection'

const router = useRouter()
const menuStore = useMenuStore()
const { readOnlyRole, rolesAreLoaded } = useRole()
const { withSpinner, report } = useActionFeedback()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu.homeUrl, isAbsoluteLink: true },
  { label: 'Graph Collections', to: '/graph-collections', position: 'last' }
])

// Any logged-in user may change collections except a read-only one, as on the legacy pages;
// the server enforces the same rule.
const canEdit = computed(() => rolesAreLoaded.value && !readOnlyRole.value)

const collections = ref<GraphCollection[]>([])
const loading = ref(true)
const loadError = ref('')
const collectionToDelete = ref<GraphCollection | null>(null)

const emptyContent = computed(() => ({
  title: 'No graph collections',
  msg: 'Create one with the Create collection button, or add a graph to a new collection from any resource graph.',
  btn: canEdit.value ? { label: 'Create collection', action: () => router.push('/graph-collections/new') } : undefined
}))

const viewRoute = (collection: GraphCollection) => `/graph-collections/${collection.id}`

const overridesSummary = (collection: GraphCollection): string => {
  const parts = []
  if (collection.showTimespanButton) {
    parts.push('Timespan')
  }
  if (collection.showGraphtypeButton) {
    parts.push('Graph type')
  }
  return parts.length ? parts.join(', ') : 'None'
}

const load = async () => {
  loading.value = true
  const result = await getGraphCollections()
  // keep any rows already on screen when a refresh fails
  if (result.success) {
    collections.value = result.payload ?? []
    loadError.value = ''
  } else {
    loadError.value = collections.value.length
      ? `${result.message} Showing the last known list.`
      : result.message
  }
  loading.value = false
}

onMounted(load)

const confirmDelete = async () => {
  const collection = collectionToDelete.value
  collectionToDelete.value = null
  const id = collection?.id
  // ids start at 0, so this is a null check and not a truthiness check
  if (!collection || id === undefined || id === null) {
    return
  }
  await withSpinner(async () => {
    const result = await deleteGraphCollection(id)
    if (report(result, `Deleted graph collection "${collection.title}".`)) {
      await load()
    }
  })
}
</script>

<style scoped lang="scss">
.graph-collections {
  .node-picker {
    margin-top: 1.5rem;
  }

  padding: 0 2px 2rem 2px;

  :deep(.table-card) {
    padding: 1.5rem;
  }

  .header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .subtitle {
    color: var(--p-text-muted-color);
  }

  .wrap-cell {
    display: inline-block;
    max-width: 28rem;
    overflow-wrap: anywhere;
  }

  .overrides {
    white-space: nowrap;
  }

  .action-container {
    display: flex;
    gap: 0.25rem;
  }

  .error {
    color: var(--p-red-500, #d32f2f);
  }
}
</style>
