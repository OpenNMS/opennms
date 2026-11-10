<template>
  <TableCard class="jdbc-card">
    <div class="table-header">
      <OnmsSearchInput
        class="source-search"
        :modelValue="search"
        placeholder="Search sources and collections"
        ariaLabel="Search JDBC data collection sources"
        dataTest="jdbc-source-search"
        @update:modelValue="search = $event ?? ''"
      />
      <OnmsButton class="create-button" data-test="jdbc-source-create" @click="router.push(JDBC_SOURCE_CREATE_PATH)">New source</OnmsButton>
    </div>

    <OnmsTable :value="filteredSources" data-test="jdbc-sources-table">
      <OnmsColumn header="Source">
        <template #body="{ data }">
          <router-link :to="sourcePath(data.id)" data-test="jdbc-source-link">{{ data.id }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Collections">
        <template #body="{ data }">
          <span data-test="jdbc-source-collections">{{ data.collectionNames.length ? data.collectionNames.join(', ') : '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="RRD repository">
        <template #body="{ data }">
          <span>{{ data.rrdRepository || '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            v-if="data.id !== DEFAULT_SOURCE_ID"
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Delete source"
            aria-label="Delete source"
            data-test="jdbc-source-delete"
            @click="pendingDelete = data"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!filteredSources.length"
      :content="{ msg: search ? 'No sources match your search.' : 'No JDBC data collection sources stored yet.' }"
      data-test="jdbc-sources-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this source?"
      actionButtonText="Delete"
      data-test="jdbc-source-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = null"
    >
      <template #content>
        <p>
          '{{ pendingDelete.id }}' and its collections will be removed<template v-if="pendingDelete.collectionNames.length">:
            {{ pendingDelete.collectionNames.join(', ') }}</template>.
          Collectd services using one of them stop collecting.
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
import { JDBC_SOURCE_CREATE_PATH, sourcePath } from '@/lib/jdbcDataCollectionRoutes'
import { DEFAULT_SOURCE_ID } from '@/lib/jdbcDataCollectionValidator'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcSourceSummary } from '@/types/jdbcDataCollection'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIconButton,
  OnmsSearchInput,
  OnmsTable
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const store = useJdbcDataCollectionStore()
const router = useRouter()
const { report } = useActionFeedback()

const search = ref('')
const pendingDelete = ref<JdbcSourceSummary | null>(null)

const filteredSources = computed(() => {
  const term = search.value.trim().toLowerCase()

  if (!term) {
    return store.sources
  }

  return store.sources.filter(source =>
    source.id.toLowerCase().includes(term) ||
    source.collectionNames.some(name => name.toLowerCase().includes(term)))
})

const onConfirmDelete = async () => {
  const id = pendingDelete.value?.id
  pendingDelete.value = null

  if (id) {
    report(await store.deleteSource(id), 'Source deleted.')
  }
}
</script>

<style lang="scss" scoped>
.jdbc-card {
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

.source-search {
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
</style>
