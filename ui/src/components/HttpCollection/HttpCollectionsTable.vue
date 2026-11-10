<template>
  <TableCard class="http-collections-card">
    <div class="table-header">
      <OnmsSearchInput
        class="collection-search"
        :modelValue="search"
        placeholder="Search collections"
        ariaLabel="Search HTTP collections"
        dataTest="http-collection-search"
        @update:modelValue="search = $event ?? ''"
      />
      <OnmsButton class="create-button" data-test="http-collection-create" @click="router.push(COLLECTION_CREATE_PATH)">
        New collection
      </OnmsButton>
    </div>

    <OnmsTable :value="filteredCollections" data-test="http-collections-table">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <router-link :to="collectionPath(data.name)" data-test="http-collection-link">{{ data.name }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Step (s)" field="rrdStep" />
      <OnmsColumn header="URIs" field="uriCount" />
      <OnmsColumn header="Attributes" field="attributeCount" />
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="actions">
            <OnmsIconButton
              :icon="EditIcon"
              tooltip="Edit collection"
              aria-label="Edit collection"
              data-test="http-collection-edit"
              @click="router.push(collectionEditPath(data.name))"
            />
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Delete collection"
              aria-label="Delete collection"
              data-test="http-collection-delete"
              @click="pendingDelete = data.name"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!filteredCollections.length"
      :content="{ msg: search ? 'No HTTP collections match your search.' : 'No HTTP collections configured yet.' }"
      data-test="http-collections-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this HTTP collection?"
      actionButtonText="Delete"
      data-test="http-collection-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = ''"
    >
      <template #content>
        <p>
          '{{ pendingDelete }}' and all of its URIs will be removed. A collection that a collectd package still
          uses cannot be deleted.
        </p>
      </template>
    </OnmsConfirmationDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { COLLECTION_CREATE_PATH, collectionEditPath, collectionPath } from '@/lib/httpCollectionRoutes'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIconButton,
  OnmsSearchInput,
  OnmsTable
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'

const store = useHttpCollectionStore()
const router = useRouter()
const { report } = useActionFeedback()

const search = ref('')
const pendingDelete = ref('')

const filteredCollections = computed(() => {
  const term = search.value.trim().toLowerCase()

  if (!term) {
    return store.collections
  }

  return store.collections.filter(collection => collection.name.toLowerCase().includes(term))
})

const onConfirmDelete = async () => {
  const name = pendingDelete.value
  pendingDelete.value = ''

  const result = await store.deleteCollection(name)

  report(result, 'HTTP collection deleted.')
}
</script>

<style lang="scss" scoped>
.http-collections-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1em;
  padding: 0 0 0.75em 0;
}

.collection-search {
  flex: 0 1 auto;
  min-width: 0;

  :deep(input) {
    width: 100%;
  }
}

.create-button {
  flex-shrink: 0;
  white-space: nowrap;
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.25em;
}
</style>
