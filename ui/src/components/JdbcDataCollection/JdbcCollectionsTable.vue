<template>
  <TableCard class="jdbc-card">
    <div class="table-header">
      <h3>Collections</h3>
      <OnmsButton class="create-button" data-test="jdbc-collection-create" @click="router.push(collectionCreatePath(source.id))">
        New collection
      </OnmsButton>
    </div>

    <OnmsTable :value="source.collections" data-test="jdbc-collections-table">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <router-link :to="collectionEditPath(source.id, data.name)" data-test="jdbc-collection-link">{{ data.name }}</router-link>
          <OnmsTag
            v-if="data.shadowedBy"
            class="shadowed"
            severity="warn"
            :value="`Ignored: also defined in '${data.shadowedBy}'`"
            data-test="jdbc-collection-shadowed"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="RRD step">
        <template #body="{ data }">{{ data.rrdStep }} s</template>
      </OnmsColumn>
      <OnmsColumn header="Queries">
        <template #body="{ data }">{{ data.queries.length }}</template>
      </OnmsColumn>
      <OnmsColumn header="Used by collectd service">
        <template #body="{ data }">
          <span data-test="jdbc-collection-used-by">{{ data.usedBy?.length ? data.usedBy.join(', ') : '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Delete collection"
            aria-label="Delete collection"
            data-test="jdbc-collection-delete"
            @click="pendingDelete = data"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!source.collections.length"
      :content="{ msg: 'This source has no collections yet.' }"
      data-test="jdbc-collections-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this collection?"
      actionButtonText="Delete"
      data-test="jdbc-collection-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = null"
    >
      <template #content>
        <p v-if="pendingDelete.usedBy?.length">
          '{{ pendingDelete.name }}' is used by {{ pendingDelete.usedBy.join(', ') }}. Those services stop collecting.
        </p>
        <p v-else>'{{ pendingDelete.name }}' and all of its queries will be removed.</p>
      </template>
    </OnmsConfirmationDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { collectionCreatePath, collectionEditPath } from '@/lib/jdbcDataCollectionRoutes'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcCollection, JdbcSource } from '@/types/jdbcDataCollection'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable, OnmsTag } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

defineProps<{
  source: JdbcSource
}>()

const store = useJdbcDataCollectionStore()
const router = useRouter()
const { report } = useActionFeedback()

const pendingDelete = ref<JdbcCollection | null>(null)

const onConfirmDelete = async () => {
  const name = pendingDelete.value?.name
  pendingDelete.value = null

  if (name) {
    report(await store.deleteCollection(name), 'Collection deleted.')
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

.shadowed {
  margin-left: 0.5em;
}
</style>
