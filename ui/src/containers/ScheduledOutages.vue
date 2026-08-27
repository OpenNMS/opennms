<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="scheduled-outages">
    <TableCard>
      <div class="header">
        <div class="card-title">Scheduled Outages</div>
        <div class="header-actions">
          <FormField label="New outage name" for="new-outage-name">
            <div class="create-row">
              <OnmsInputText id="new-outage-name" v-model="newName" placeholder="New Name" data-test="new-name" />
              <OnmsButton
                icon="pi pi-plus"
                label="Create"
                :disabled="!newName.trim()"
                data-test="create-outage"
                @click="createOutage"
              />
            </div>
          </FormField>
          <AboutDialogButton title="Scheduled Outages">
            <ScheduledOutagesAbout />
          </AboutDialogButton>
        </div>
      </div>

      <p v-if="createError" class="error" data-test="create-error">{{ createError }}</p>
      <p v-if="loadError" class="error" data-test="load-error">{{ loadError }}</p>

      <OnmsTable
        v-if="outages.length"
        :value="outages"
        paginator
        dataKey="name"
        :rows="10"
        :rowsPerPageOptions="[10, 20, 50, 100]"
        data-test="outages-table"
      >
        <OnmsColumn field="name" header="Name" sortable />
        <OnmsColumn field="type" header="Type" sortable>
          <template #body="{ data }">{{ data.type ?? '--' }}</template>
        </OnmsColumn>
        <OnmsColumn header="Nodes/Interfaces">
          <template #body="{ data }">{{ selectionSummary(data) }}</template>
        </OnmsColumn>
        <OnmsColumn header="Times">
          <template #body="{ data }">{{ (data.time ?? []).length }}</template>
        </OnmsColumn>
        <OnmsColumn header="Notifications">
          <template #body="{ data }"><AppliedMark :on="data.applies?.notifications" /></template>
        </OnmsColumn>
        <OnmsColumn header="Polling">
          <template #body="{ data }"><AppliedMark :on="data.applies?.polling" /></template>
        </OnmsColumn>
        <OnmsColumn header="Thresholds">
          <template #body="{ data }"><AppliedMark :on="data.applies?.thresholds" /></template>
        </OnmsColumn>
        <OnmsColumn header="Data collection">
          <template #body="{ data }"><AppliedMark :on="data.applies?.collection" /></template>
        </OnmsColumn>
        <OnmsColumn header="Actions">
          <template #body="{ data }">
            <div class="action-container">
              <OnmsIconButton
                :icon="Edit"
                :title="`Edit ${data.name}`"
                :aria-label="`Edit ${data.name}`"
                data-test="edit-outage"
                @click="editOutage(data.name)"
              />
              <OnmsIconButton
                :icon="Delete"
                severity="danger"
                :title="`Delete ${data.name}`"
                :aria-label="`Delete ${data.name}`"
                data-test="delete-outage"
                @click="askDelete(data.name)"
              />
            </div>
          </template>
        </OnmsColumn>
      </OnmsTable>

      <div v-else-if="!loading">
        <EmptyList :content="emptyContent" data-test="empty-list" />
      </div>
    </TableCard>

    <OnmsConfirmationDialog
      :visible="!!outageToDelete"
      title="Delete Scheduled Outage"
      actionButtonText="Delete"
      @ok="confirmDelete"
      @cancel="outageToDelete = ''"
    >
      <template #content>
        <p>
          Are you sure you want to delete the scheduled outage <strong>{{ outageToDelete }}</strong>?
          It will also be removed from every subsystem that references it. This action cannot be undone.
        </p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsInputText, OnmsTable } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import ScheduledOutagesAbout from '@/components/ScheduledOutages/ScheduledOutagesAbout.vue'
import {
  deleteScheduledOutage,
  getOutageApplicability,
  getScheduledOutages,
  scheduledOutageErrorMessage
} from '@/services/scheduledOutagesService'
import { ScheduledOutage } from '@/types/scheduledOutage'
import { BreadCrumb } from '@/types'

interface OutageRow extends ScheduledOutage {
  applies?: { notifications: boolean, polling: boolean, thresholds: boolean, collection: boolean }
}

// small inline green-check / muted-dash cell
const AppliedMark = (props: { on?: boolean }) =>
  h('span', { class: props.on ? 'mark on' : 'mark off', 'data-test': 'applied-mark' }, props.on ? '✓' : '—')

const router = useRouter()

const breadcrumbs: BreadCrumb[] = [
  { label: 'Home', to: '/' },
  { label: 'Scheduled Outages', to: '/scheduled-outages' }
]

const outages = ref<OutageRow[]>([])
const loading = ref(true)
const newName = ref('')
const createError = ref('')
const loadError = ref('')
const outageToDelete = ref('')

const emptyContent = computed(() => ({
  title: 'No scheduled outages',
  msg: 'Create one with the New Name field above.'
}))

const load = async () => {
  loading.value = true
  loadError.value = ''
  const list = await getScheduledOutages()
  // null signals a fetch failure — keep the rows already on screen (service contract)
  if (list === null) {
    loadError.value = 'Failed to load scheduled outages. Showing the last known list.'
    loading.value = false
    return
  }
  const rows: OutageRow[] = list.map(o => ({ ...o }))
  // membership booleans come from the per-outage applies-to summary
  await Promise.all(rows.map(async (row) => {
    const appl = await getOutageApplicability(row.name)
    row.applies = {
      notifications: !!appl?.notifications,
      polling: !!appl?.pollers.some(p => p.applied),
      thresholds: !!appl?.thresholders.some(p => p.applied),
      collection: !!appl?.collectors.some(p => p.applied)
    }
  }))
  outages.value = rows
  loading.value = false
}

onMounted(load)

const selectionSummary = (o: ScheduledOutage): string => {
  if ((o.interface ?? []).some(i => i.address === 'match-any')) {
    return 'All nodes and interfaces'
  }
  const nodes = (o.node ?? []).length
  const ifaces = (o.interface ?? []).length
  if (!nodes && !ifaces) {
    return '--'
  }
  return `${nodes} node(s), ${ifaces} interface(s)`
}

const createOutage = () => {
  const name = newName.value.trim()
  createError.value = ''
  if (!name) {
    return
  }
  if (outages.value.some(o => o.name === name)) {
    createError.value = `An outage named "${name}" already exists.`
    return
  }
  router.push({ path: '/scheduled-outages/edit', query: { name, new: 'true' }})
}

const editOutage = (name: string) => {
  router.push({ path: '/scheduled-outages/edit', query: { name }})
}

const askDelete = (name: string) => {
  outageToDelete.value = name
}

const confirmDelete = async () => {
  const name = outageToDelete.value
  outageToDelete.value = ''
  loadError.value = ''
  try {
    await deleteScheduledOutage(name)
  } catch (err: any) {
    loadError.value = scheduledOutageErrorMessage(err, `Failed to delete the scheduled outage "${name}".`)
  } finally {
    await load()
  }
}
</script>

<style scoped lang="scss">
.scheduled-outages {
  .header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .header-actions {
    display: flex;
    align-items: flex-end;
    gap: 0.75rem;
  }

  .create-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .action-container {
    display: flex;
    gap: 0.25rem;
  }

  .error {
    color: var(--p-red-500, #d32f2f);
  }

  :deep(.mark.on) {
    color: var(--p-green-500, #388e3c);
    font-weight: 700;
  }

  :deep(.mark.off) {
    color: var(--p-text-muted-color);
  }
}
</style>
