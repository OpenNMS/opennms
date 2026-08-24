<template>
  <div class="main-wrapper">
    <OnmsTable
      :value="tableData"
      dataKey="originalIndex"
      stripedRows
      size="small"
      paginator
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50]"
      @page="onPage"
      aria-label="External Requisitions"
    >
      <OnmsColumn :field="RequisitionData.ImportName" header="Name" sortable>
        <template #body="{ data }">
          <ConfigurationCopyPasteDisplay :text="data[RequisitionData.ImportName]" />
        </template>
      </OnmsColumn>
      <OnmsColumn :field="RequisitionData.ImportURL" header="URL" sortable>
        <template #body="{ data }">
          <ConfigurationCopyPasteDisplay :text="data[RequisitionData.ImportURL]" />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Schedule Frequency">
        <template #body="{ data }">
          <ConfigurationCopyPasteDisplay
            :showCopyBtn="false"
            :text="ConfigurationHelper.cronToEnglish(data[RequisitionData.CronSchedule])"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn :field="RequisitionData.RescanExisting" header="Rescan Behavior" sortable>
        <template #body="{ data }">
          {{ rescanToEnglish(data[RequisitionData.RescanExisting]) }}
        </template>
      </OnmsColumn>
      <OnmsColumn>
        <template #body="{ data }">
          <div class="flex">
            <OnmsIconButton
              variant="filled"
              aria-label="Edit"
              v-onms-tooltip="'Edit'"
              :disabled="Boolean(data[RequisitionData.ImportURL].startsWith('requisition://'))"
              data-test="edit-btn"
              :icon="Edit"
              @click="() => props.editClicked(data.originalIndex)"
            />
            <OnmsIconButton
              class="delete-icon"
              aria-label="Delete"
              v-onms-tooltip="'Delete'"
              :icon="Delete"
              @click="() => props.deleteClicked(data.originalIndex)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { computed, PropType } from 'vue'
import { OnmsColumn, OnmsIconButton, OnmsTable, type OnmsTablePageEvent } from '@opennms/onms-ui'

import Edit from '@/components/icons/action/Edit.vue'
import Delete from '@/components/icons/action/Delete.vue'

import { RequisitionData } from './copy/requisitionTypes'
import { ConfigurationHelper } from './ConfigurationHelper'
import ConfigurationCopyPasteDisplay from './ConfigurationCopyPasteDisplay.vue'
import { ProvisionDServerConfiguration } from './configuration.types'
import { rescanCopy } from './copy/rescanItems'

/**
 * Props
 */
const props = defineProps({
  itemList: { required: true, type: Array as PropType<Array<ProvisionDServerConfiguration>> },
  editClicked: { type: Function, required: true },
  deleteClicked: { type: Function, required: true },
  setNewPage: { type: Function, required: true }
})

/**
 * Rows for the table: obfuscate the password in the URL. Sorting and pagination
 * are handled client-side by the DataTable.
 */
const tableData = computed(() => {
  return (props.itemList || []).map(item => ({
    ...item,
    [RequisitionData.ImportURL]: ConfigurationHelper.obfuscatePassword(item[RequisitionData.ImportURL])
  }))
})

/**
 * When the user changes the page number.
 */
const onPage = (event: OnmsTablePageEvent) => {
  if (props.setNewPage) {
    props.setNewPage(event.page + 1)
  }
}

/**
 * Convert our Rescan Existing value to something more understandable by Humans.
 */
const rescanToEnglish = (rescanVal: string) => {
  return rescanCopy[rescanVal]
}
</script>
<style
  lang="scss"
  scoped
>
.main-wrapper {
  padding: 16px 24px;
}
.flex {
  display: flex;
}
.delete-icon {
  color: var(--p-red-500);
}
.cron {
  max-width: 260px;
}
</style>
