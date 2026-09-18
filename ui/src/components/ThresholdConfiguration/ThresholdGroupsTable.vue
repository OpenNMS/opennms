<template>
  <TableCard>
    <div class="table-header">
      <OnmsSearchInput
        :modelValue="search"
        placeholder="Search groups"
        ariaLabel="Search threshold groups"
        dataTest="threshold-group-search"
        @update:modelValue="search = $event ?? ''"
      />
      <OnmsButton data-test="threshold-group-create" @click="onCreate">New group</OnmsButton>
    </div>

    <OnmsTable :value="filteredGroups" data-test="threshold-groups-table">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <router-link :to="groupRoute(data.name)" data-test="threshold-group-link">{{ data.name }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="RRD repository" field="rrdRepository" />
      <OnmsColumn header="Thresholds" field="thresholdCount" />
      <OnmsColumn header="Expressions" field="expressionCount" />
      <OnmsColumn header="Used by">
        <template #body="{ data }">
          <span data-test="threshold-group-used-by">{{ usedBy(data.name) }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="actions">
            <OnmsTag v-if="data.readOnly" value="Read only" data-test="threshold-group-readonly-tag" />
            <OnmsIconButton
              v-else
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Delete group"
              aria-label="Delete group"
              data-test="threshold-group-delete"
              @click="pendingDelete = data.name"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!filteredGroups.length"
      :content="{ msg: search ? 'No threshold groups match your search.' : 'No threshold groups configured yet.' }"
      data-test="threshold-groups-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this threshold group?"
      actionButtonText="Delete"
      data-test="threshold-group-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = ''"
    >
      <template #content>
        <p>{{ deleteMessage }}</p>
      </template>
    </OnmsConfirmationDialog>

    <ThresholdGroupDrawer
      :visible="isDrawerVisible"
      :isCreate="true"
      :existingNames="store.groupNames"
      @cancel="isDrawerVisible = false"
      @save="onSaveNewGroup"
    />
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ThresholdGroupDrawer from '@/components/ThresholdConfiguration/ThresholdGroupDrawer.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ThresholdGroup } from '@/types/thresholdConfig'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIconButton,
  OnmsSearchInput,
  OnmsTable,
  OnmsTag
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const store = useThresholdGroupStore()
const threshdStore = useThreshdConfigurationStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const search = ref('')
const pendingDelete = ref('')
const isDrawerVisible = ref(false)

const filteredGroups = computed(() => {
  const term = search.value.trim().toLowerCase()

  if (!term) {
    return store.groups
  }

  return store.groups.filter(
    group =>
      group.name.toLowerCase().includes(term) || (group.rrdRepository ?? '').toLowerCase().includes(term)
  )
})

const groupRoute = (name: string) => `/threshold-config/group/${encodeURIComponent(name)}`

const usedBy = (name: string) => {
  const packages = threshdStore.packagesUsingGroup(name)
  return packages.length ? packages.join(', ') : '--'
}

const deleteMessage = computed(() => {
  const packages = threshdStore.packagesUsingGroup(pendingDelete.value)

  if (packages.length) {
    return `'${pendingDelete.value}' is still applied by ${packages.join(', ')}. Those packages will reference a group that no longer exists.`
  }

  return `'${pendingDelete.value}' and all of its thresholds will be removed.`
})

const onCreate = () => {
  isDrawerVisible.value = true
}

const onSaveNewGroup = async (group: ThresholdGroup) => {
  isDrawerVisible.value = false

  const result = await store.createGroup(group)

  showSnackBar({ msg: result.success ? 'Threshold group created.' : result.message, error: !result.success })

  if (result.success) {
    router.push(groupRoute(group.name))
  }
}

const onConfirmDelete = async () => {
  const name = pendingDelete.value
  pendingDelete.value = ''

  const result = await store.deleteGroup(name)

  showSnackBar({ msg: result.success ? 'Threshold group deleted.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1em;
  padding: 0 1em 0.75em 1em;
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.25em;
}
</style>
