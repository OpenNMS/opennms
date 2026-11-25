<template>
  <TableCard class="resource-types-card">
    <div class="table-header">
      <h3>Resource types</h3>
      <OnmsButton class="create-button" data-test="resource-type-create" @click="router.push(typeCreatePath(config.id))">New resource type</OnmsButton>
    </div>

    <OnmsTable :value="rows" data-test="resource-types-table">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <router-link :to="typeEditPath(config.id, data.index)" data-test="resource-type-link">{{ data.type.name }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Label">
        <template #body="{ data }">{{ data.type.label }}</template>
      </OnmsColumn>
      <OnmsColumn header="Persistence selector">
        <template #body="{ data }">
          <span :title="data.type.persistenceSelectorStrategy?.className" data-test="resource-type-persistence">
            {{ shortClassName(data.type.persistenceSelectorStrategy?.className) }}
          </span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Storage">
        <template #body="{ data }">
          <span :title="data.type.storageStrategy?.className" data-test="resource-type-storage">
            {{ shortClassName(data.type.storageStrategy?.className) }}
          </span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Delete resource type"
            aria-label="Delete resource type"
            data-test="resource-type-delete"
            @click="pendingDelete = data.index"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!rows.length"
      :content="{ msg: 'This configuration has no resource types yet.' }"
      data-test="resource-types-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingType"
      :visible="true"
      title="Delete this resource type?"
      actionButtonText="Delete"
      data-test="resource-type-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = null"
    >
      <template #content>
        <p>{{ typeDeleteMessage(pendingType.name) }}</p>
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
import { typeDeleteMessage } from '@/lib/resourceTypeHelpText'
import { typeCreatePath, typeEditPath } from '@/lib/resourceTypeRoutes'
import { useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import type { ResourceTypesConfig } from '@/types/resourceTypesConfig'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = defineProps<{
  config: ResourceTypesConfig
}>()

const store = useResourceTypesConfigStore()
const router = useRouter()
const { report } = useActionFeedback()

const pendingDelete = ref<number | null>(null)

// The index addresses a type in the configuration; it is all that identifies it in the edit route.
const rows = computed(() => (props.config.resourceTypes ?? []).map((type, index) => ({ type, index })))
const pendingType = computed(() => (pendingDelete.value === null ? null : props.config.resourceTypes[pendingDelete.value] ?? null))

const shortClassName = (className?: string) => className?.split('.').pop() ?? '--'

const onConfirmDelete = async () => {
  const index = pendingDelete.value
  pendingDelete.value = null

  if (index !== null) {
    report(await store.deleteResourceType(index), 'Resource type deleted.')
  }
}
</script>

<style lang="scss" scoped>
.resource-types-card {
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
