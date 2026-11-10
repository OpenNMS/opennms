<template>
  <TableCard class="jdbc-card">
    <div class="table-header">
      <h3>Queries</h3>
      <OnmsButton
        class="create-button"
        data-test="jdbc-query-create"
        @click="router.push(queryCreatePath(sourceId, collection.name))"
      >
        New query
      </OnmsButton>
    </div>

    <OnmsTable :value="rows" data-test="jdbc-queries-table">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <router-link :to="queryEditPath(sourceId, collection.name, data.index)" data-test="jdbc-query-link">{{ data.query.name }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Interface type">
        <template #body="{ data }">{{ data.query.ifType }}</template>
      </OnmsColumn>
      <OnmsColumn header="Resource type">
        <template #body="{ data }">{{ data.query.resourceType || 'node' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Columns">
        <template #body="{ data }">
          <span data-test="jdbc-query-columns">{{ data.query.columns.map((column: JdbcColumn) => column.alias).join(', ') }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Delete query"
            aria-label="Delete query"
            data-test="jdbc-query-delete"
            @click="pendingDelete = data.index"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!collection.queries.length"
      :content="{ msg: 'This collection has no queries yet.' }"
      data-test="jdbc-queries-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDelete !== null"
      :visible="true"
      title="Delete this query?"
      actionButtonText="Delete"
      data-test="jdbc-query-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = null"
    >
      <template #content>
        <p>'{{ collection.queries[pendingDelete]?.name }}' will be removed from '{{ collection.name }}'.</p>
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
import { queryCreatePath, queryEditPath } from '@/lib/jdbcDataCollectionRoutes'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcCollection, JdbcColumn } from '@/types/jdbcDataCollection'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = defineProps<{
  sourceId: string
  collection: JdbcCollection
}>()

const store = useJdbcDataCollectionStore()
const router = useRouter()
const { report } = useActionFeedback()

const pendingDelete = ref<number | null>(null)

// Queries are addressed by position, so the rows carry it.
const rows = computed(() => props.collection.queries.map((query, index) => ({ query, index })))

const onConfirmDelete = async () => {
  const index = pendingDelete.value
  pendingDelete.value = null

  if (index !== null) {
    report(await store.deleteQuery(props.collection.name, index), 'Query deleted.')
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

  h3 {
    margin: 0;
  }
}

.create-button {
  flex-shrink: 0;
  white-space: nowrap;
}
</style>
