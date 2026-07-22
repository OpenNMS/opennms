<template>
  <div class="main-wrapper">
    <PDataTable
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
      <PColumn :field="RequisitionData.ImportName" header="Name" sortable :pt="columnHeaderPt">
        <template #body="{ data }">
          <ConfigurationCopyPasteDisplay :text="data[RequisitionData.ImportName]" />
        </template>
      </PColumn>
      <PColumn :field="RequisitionData.ImportURL" header="URL" sortable :pt="columnHeaderPt">
        <template #body="{ data }">
          <ConfigurationCopyPasteDisplay :text="data[RequisitionData.ImportURL]" />
        </template>
      </PColumn>
      <PColumn header="Schedule Frequency" :pt="columnHeaderPt">
        <template #body="{ data }">
          <ConfigurationCopyPasteDisplay
            :showCopyBtn="false"
            :text="ConfigurationHelper.cronToEnglish(data[RequisitionData.CronSchedule])"
          />
        </template>
      </PColumn>
      <PColumn :field="RequisitionData.RescanExisting" header="Rescan Behavior" sortable :pt="columnHeaderPt">
        <template #body="{ data }">
          {{ rescanToEnglish(data[RequisitionData.RescanExisting]) }}
        </template>
      </PColumn>
      <PColumn :pt="columnHeaderPt">
        <template #body="{ data }">
          <div class="flex">
            <PButton
              aria-label="Edit"
              v-tooltip="'Edit'"
              @click="() => props.editClicked(data.originalIndex)"
              :disabled="Boolean(data[RequisitionData.ImportURL].startsWith('requisition://'))"
              data-test="edit-btn"
            >
              <OnmsIcon :icon="Edit" />
            </PButton>
            <PButton
              text
              aria-label="Delete"
              v-tooltip="'Delete'"
              @click="() => props.deleteClicked(data.originalIndex)"
            >
              <OnmsIcon
                class="delete-icon"
                :icon="Delete"
              />
            </PButton>
          </div>
        </template>
      </PColumn>
    </PDataTable>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { computed, PropType } from 'vue'
import DataTable, { DataTablePageEvent } from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'

import Edit from '@/components/icons/action/Edit.vue'
import Delete from '@/components/icons/action/Delete.vue'

import { RequisitionData } from './copy/requisitionTypes'
import { ConfigurationHelper } from './ConfigurationHelper'
import ConfigurationCopyPasteDisplay from './ConfigurationCopyPasteDisplay.vue'
import { ProvisionDServerConfiguration } from './configuration.types'
import { rescanCopy } from './copy/rescanItems'

const PDataTable = DataTable
const PColumn = Column
const PButton = Button

// PrimeVue Column doesn't emit scope="col" on the header <th>; restore it via the
// passthrough so header cells stay associated with their columns for screen readers.
const columnHeaderPt = { headerCell: { scope: 'col' }}

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
const onPage = (event: DataTablePageEvent) => {
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

  // Enlarge the edit/delete glyphs (OnmsIcon scales with font-size)
  :deep(svg) {
    font-size: 1.25em;
  }
}
.delete-icon {
  color: var(--p-red-500);
}
.cron {
  max-width: 260px;
}
</style>
