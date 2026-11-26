<template>
  <div class="xml-group-files-tab">
    <p class="intro">
      A group file holds groups that several sources share; a source imports it in addition to its own groups.
      <OnmsIconButton
        :icon="InfoIcon"
        iconSize="1.2em"
        class="info-icon-button"
        title="About group files"
        aria-label="About group files"
        data-test="xml-group-files-info-icon"
        @click="isHelpVisible = true"
      />
    </p>

    <TableCard class="xml-table-card">
      <div class="table-header">
        <OnmsSearchInput
          class="search"
          :modelValue="search"
          placeholder="Search group files"
          ariaLabel="Search group files"
          dataTest="xml-group-file-search"
          @update:modelValue="search = $event ?? ''"
        />
        <OnmsButton class="create-button" data-test="xml-group-file-create" @click="router.push(GROUP_FILE_CREATE_PATH)">
          New group file
        </OnmsButton>
      </div>

      <OnmsTable :value="filtered" data-test="xml-group-files-table">
        <OnmsColumn header="Id">
          <template #body="{ data }">
            <router-link :to="groupFilePath(data.id)" data-test="xml-group-file-link">{{ data.id }}</router-link>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Groups" field="groupCount" />
        <OnmsColumn header="Imported by">
          <template #body="{ data }">
            <span data-test="xml-group-file-used-by">{{ data.usedBy.length ? data.usedBy.join(', ') : '--' }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Actions">
          <template #body="{ data }">
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              :disabled="data.usedBy.length > 0"
              :tooltip="data.usedBy.length ? 'Imported by a collection, cannot be deleted' : 'Delete group file'"
              aria-label="Delete group file"
              data-test="xml-group-file-delete"
              @click="pendingDelete = data.id"
            />
          </template>
        </OnmsColumn>
      </OnmsTable>

      <EmptyList
        v-if="!filtered.length"
        :content="{ msg: search ? 'No group files match your search.' : 'No group files configured yet.' }"
        data-test="xml-group-files-empty"
      />
    </TableCard>

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this group file?"
      actionButtonText="Delete"
      data-test="xml-group-file-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = ''"
    >
      <template #content>
        <p>'{{ pendingDelete }}' and all of its groups will be removed.</p>
      </template>
    </OnmsConfirmationDialog>

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Group files"
      :text="GROUP_FILES_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { GROUP_FILES_HELP } from '@/lib/xmlCollectionHelpText'
import { GROUP_FILE_CREATE_PATH, groupFilePath } from '@/lib/xmlCollectionRoutes'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIconButton,
  OnmsSearchInput,
  OnmsTable
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const store = useXmlCollectionConfigStore()
const router = useRouter()
const { report } = useActionFeedback()

const search = ref('')
const pendingDelete = ref('')
const isHelpVisible = ref(false)

const filtered = computed(() => {
  const term = search.value.trim().toLowerCase()
  return term ? store.groupFiles.filter(groupFile => groupFile.id.toLowerCase().includes(term)) : store.groupFiles
})

const onConfirmDelete = async () => {
  const id = pendingDelete.value
  pendingDelete.value = ''

  report(await store.deleteGroupFile(id), 'Group file deleted.')
}
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;
@use '@/components/XmlCollectionConfiguration/xml-table' as *;

.info-icon-button {
  @include info-icon-button;
}

.xml-group-files-tab {
  @include xml-tab;
}
</style>
