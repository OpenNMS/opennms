<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="scheduled-outages">
    <TableCard>
      <div class="header">
        <div class="title-block">
          <div class="card-title">Scheduled Outages</div>
          <div class="subtitle-row">
            <span class="subtitle">View and create scheduled outages.</span>
            <AboutDialogButton title="Scheduled Outages">
              <ScheduledOutagesAbout />
            </AboutDialogButton>
          </div>
        </div>
        <OnmsButton
          icon="pi pi-plus"
          label="Create New Outage"
          data-test="create-outage"
          @click="openCreateDialog"
        />
      </div>

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
          <template #body="{ data }">
            <span :title="selectionTitle(data)">{{ selectionSummary(data) }}</span>
          </template>
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

    <OnmsDialog
      :visible="showCreateDialog"
      modal
      header="Create New Outage"
      width="28rem"
      data-test="create-dialog"
      @update:visible="(value: boolean) => { if (!value) closeCreateDialog() }"
    >
      <FormField label="Name" for="new-outage-name" :error="createError">
        <OnmsInputText
          id="new-outage-name"
          v-model="newName"
          placeholder="Outage name"
          fluid
          data-test="new-name"
          @keyup.enter="createOutage"
        />
      </FormField>
      <div class="dialog-actions">
        <OnmsButton variant="text" label="Cancel" data-test="create-cancel" @click="closeCreateDialog" />
        <OnmsButton label="Create" :disabled="!newName.trim()" data-test="create-confirm" @click="createOutage" />
      </div>
    </OnmsDialog>

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

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsDialog, OnmsIconButton, OnmsInputText, OnmsTable } from '@opennms/onms-ui'
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
  getNodeLabels,
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
  h('span', {
    class: props.on ? 'mark on' : 'mark off',
    'data-test': 'applied-mark',
    role: 'img',
    'aria-label': props.on ? 'applied' : 'not applied'
  }, props.on ? '✓' : '—')

const router = useRouter()

const breadcrumbs: BreadCrumb[] = [
  { label: 'Home', to: '/' },
  { label: 'Scheduled Outages', to: '/scheduled-outages' }
]

const outages = ref<OutageRow[]>([])
const nodeLabels = ref<Record<number, string>>({})
const loading = ref(true)
const newName = ref('')
const createError = ref('')
const loadError = ref('')
const outageToDelete = ref('')

const emptyContent = computed(() => ({
  title: 'No scheduled outages',
  msg: 'Create one with the Create New Outage button above.'
}))

const load = async () => {
  loading.value = true
  loadError.value = ''
  const list = await getScheduledOutages()
  // null signals a fetch failure — keep any rows already on screen (service contract)
  if (list === null) {
    loadError.value = outages.value.length
      ? 'Failed to load scheduled outages. Showing the last known list.'
      : 'Failed to load scheduled outages.'
    loading.value = false
    return
  }
  const rows: OutageRow[] = list.map(o => ({ ...o }))
  // One name-less applies-to call exposes every package's outage calendars, so
  // per-row membership is derived here instead of one request per outage.
  const appl = await getOutageApplicability()
  const referencedBy = (packages: { calendars: string[] }[] | undefined, outageName: string) =>
    !!packages?.some(p => p.calendars.includes(outageName))
  for (const row of rows) {
    row.applies = {
      notifications: !!appl?.notificationCalendars.includes(row.name),
      polling: referencedBy(appl?.pollers, row.name),
      thresholds: referencedBy(appl?.thresholders, row.name),
      collection: referencedBy(appl?.collectors, row.name)
    }
  }
  // resolve node labels for the selection column in a single OR query
  const nodeIds = [...new Set(rows.flatMap(row => (row.node ?? []).map(n => n.id)))]
  nodeLabels.value = await getNodeLabels(nodeIds)
  outages.value = rows
  loading.value = false
}

onMounted(load)

// Node labels and interface addresses, as the legacy list showed them; nodes
// no longer in inventory are flagged, long selections truncated with a title.
const selectionSummary = (o: ScheduledOutage): string => {
  if ((o.interface ?? []).some(i => i.address === 'match-any')) {
    return 'All nodes and interfaces'
  }
  const parts = [
    ...(o.node ?? []).map(n => nodeLabels.value[n.id] ?? `Node ${n.id} (not found)`),
    ...(o.interface ?? []).map(i => i.address)
  ]
  if (!parts.length) {
    return '--'
  }
  return parts.length > 4 ? `${parts.slice(0, 4).join(', ')}, +${parts.length - 4} more` : parts.join(', ')
}

const selectionTitle = (o: ScheduledOutage): string => [
  ...(o.node ?? []).map(n => nodeLabels.value[n.id] ?? `Node ${n.id} (not found)`),
  ...(o.interface ?? []).map(i => i.address)
].join(', ')

const showCreateDialog = ref(false)

const openCreateDialog = () => {
  newName.value = ''
  createError.value = ''
  showCreateDialog.value = true
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
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
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .subtitle-row {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }

  .subtitle {
    color: var(--p-text-muted-color);
  }

  .dialog-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    margin-top: 1rem;
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
