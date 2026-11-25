<template>
  <TableCard class="resource-types-card">
    <div class="table-header">
      <OnmsSearchInput
        class="config-search"
        :modelValue="search"
        placeholder="Search configurations or resource types"
        ariaLabel="Search resource types configurations"
        dataTest="resource-types-config-search"
        @update:modelValue="search = $event ?? ''"
      />
      <OnmsButton class="create-button" data-test="resource-types-config-create" @click="router.push(CONFIG_CREATE_PATH)">New configuration</OnmsButton>
    </div>

    <OnmsTable :value="filteredConfigs" data-test="resource-types-configs-table">
      <OnmsColumn header="Configuration">
        <template #body="{ data }">
          <router-link :to="configPath(data.id)" data-test="resource-types-config-link">{{ data.id }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Resource types">
        <template #body="{ data }">
          <span data-test="resource-types-config-types">{{ data.resourceTypeNames.length ? data.resourceTypeNames.join(', ') : '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Delete configuration"
            aria-label="Delete configuration"
            data-test="resource-types-config-delete"
            @click="pendingDelete = data.id"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!filteredConfigs.length"
      :content="{ msg: search ? 'No configurations match your search.' : 'No resource types configurations stored yet.' }"
      data-test="resource-types-configs-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this configuration?"
      actionButtonText="Delete"
      data-test="resource-types-config-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = ''"
    >
      <template #content>
        <p>{{ deleteMessage(pendingDelete) }}</p>
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
import { CONFIG_CREATE_PATH, configPath } from '@/lib/resourceTypeRoutes'
import { configDeleteMessage } from '@/lib/resourceTypeHelpText'
import { useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIconButton,
  OnmsSearchInput,
  OnmsTable
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const store = useResourceTypesConfigStore()
const router = useRouter()
const { report } = useActionFeedback()

const search = ref('')
const pendingDelete = ref('')

// Matches the configuration and the names of its types, so a type can be found without knowing its file.
const filteredConfigs = computed(() => {
  const term = search.value.trim().toLowerCase()

  if (!term) {
    return store.configs
  }

  return store.configs.filter(config =>
    config.id.toLowerCase().includes(term) || config.resourceTypeNames.some(name => name.toLowerCase().includes(term))
  )
})

const deleteMessage = (id: string) =>
  configDeleteMessage(id, store.configs.find(config => config.id === id)?.resourceTypeNames ?? [])

const onConfirmDelete = async () => {
  const id = pendingDelete.value
  pendingDelete.value = ''

  report(await store.deleteConfig(id), 'Resource types configuration deleted.')
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
}

.config-search {
  flex: 0 1 24em;
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
