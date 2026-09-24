<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Delete monitoring location ${name}?`"
    class="location-delete-dialog"
    width="min(640px, 95vw)"
    data-test="location-delete-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="body">
      <p class="subtitle">Here is what this changes. It cannot be undone.</p>

      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <div v-if="loading" class="loading" data-test="loading">
        <OnmsSpinner size="1.5rem" strokeWidth="6" />
        <span>Checking what depends on <code>{{ name }}</code>…</span>
      </div>

      <template v-else>
        <OnmsMessage v-if="blocked" severity="error" data-test="nodes-callout">
          <strong>{{ count(nodeCount ?? 0, 'node') }} and {{ count(minionCount, 'Minion') }} are still here</strong> —
          a monitoring location with nodes cannot be deleted, and that includes each running Minion's own node;
          this page also refuses while a Minion is registered here.
        </OnmsMessage>
        <OnmsMessage v-else-if="nodeCount === null" severity="warn" data-test="nodes-callout">
          <strong>The node count could not be checked</strong> — a monitoring location with nodes cannot be
          deleted, and that includes each running Minion's own node. The delete fails if any node is still here.
        </OnmsMessage>
        <OnmsMessage v-else severity="success" data-test="nodes-callout">
          <strong>No nodes or Minions here</strong> — a monitoring location with nodes cannot be deleted, and
          that includes each running Minion's own node.
        </OnmsMessage>

        <OnmsMessage v-if="applications === null" severity="info" data-test="perspective-callout">
          <strong>Applications could not be checked</strong> — any application that polls from this monitoring
          location as a perspective stops doing so, and nothing asks you to replace it.
          <a :href="applicationsUrl" target="_self" data-test="applications-link">Open Manage Applications</a>
        </OnmsMessage>
        <OnmsMessage v-else-if="applications.length" severity="warn" data-test="perspective-callout">
          <strong>Removed from {{ count(applications.length, 'application') }} as a perspective</strong> —
          {{ applicationNames }} {{ applications.length === 1 ? 'stops' : 'stop' }} being polled from this
          monitoring location, and nothing asks you to replace it.
          <a :href="applicationsUrl" target="_self" data-test="applications-link">Open Manage Applications</a>
        </OnmsMessage>

        <OnmsMessage v-if="outageCount === null" severity="info" data-test="outages-callout">
          Perspective outage history could not be counted; any outages recorded from this perspective are
          removed with the location.
        </OnmsMessage>
        <OnmsMessage v-else-if="outageCount > 0" severity="error" data-test="outages-callout">
          <strong>Perspective outage history is deleted</strong> — {{ count(outageCount, 'outage') }} recorded
          from this monitoring location's perspective {{ outageCount === 1 ? 'is' : 'are' }} removed with it.
        </OnmsMessage>
      </template>

      <FormField for="confirm-name" hint="The name is case-sensitive.">
        <template #label-suffix>
          <label for="confirm-name" class="confirm-label">Type <code>{{ name }}</code> to confirm</label>
        </template>
        <OnmsInputText
          id="confirm-name"
          ref="confirmInput"
          v-model.trim="confirmText"
          :disabled="blocked || loading"
          :autofocus="!blocked || undefined"
          autocomplete="off"
          fluid
          data-test="confirm-input"
          @keydown.enter="confirmDelete"
        />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton
        ref="cancelButton"
        variant="ghost"
        label="Cancel"
        :autofocus="blocked || undefined"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <OnmsButton
        severity="danger"
        label="Delete monitoring location"
        :disabled="!canDelete"
        data-test="delete-button"
        @click="confirmDelete"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText, OnmsMessage, OnmsSpinner } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { legacyUrl } from '@/lib/legacyUrl'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { MonitoringLocation } from '@/types'

export interface LocationDeletedSummary {
  name: string
  applications: { id: number; name: string }[] | null
  outageCount: number | null
}

const props = defineProps<{
  visible: boolean
  location: MonitoringLocation | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  deleted: [summary: LocationDeletedSummary]
}>()

const store = useMonitoringLocationAdminStore()
const minionStore = useMinionAdminStore()

const applicationsUrl = legacyUrl('admin/applications.htm')

const name = computed(() => props.location?.['location-name'] ?? '')

const confirmInput = ref<{ $el?: HTMLElement } | null>(null)
const cancelButton = ref<{ $el?: HTMLElement } | null>(null)

const loading = ref(false)
const deleting = ref(false)
const errorText = ref('')
const confirmText = ref('')
const nodeCount = ref<number | null>(null)
const minionCount = ref(0)
const applications = ref<{ id: number; name: string }[] | null>(null)
const outageCount = ref<number | null>(null)

const count = (n: number, noun: string) => `${n} ${noun}${n === 1 ? '' : 's'}`
const applicationNames = computed(() => (applications.value ?? []).map(app => app.name).join(', '))

// a running Minion's own node is a node, so either count blocks the delete
const blocked = computed(() => (nodeCount.value ?? 0) > 0 || minionCount.value > 0)
const canDelete = computed(() =>
  !loading.value && !deleting.value && !blocked.value && confirmText.value === name.value && name.value !== '')

// only the newest open commits its results, so a slow lookup from an earlier
// open cannot describe the wrong location
let openRequest = 0

// the tab's counts and Minion list may be a paused refresh old, so every open
// (and every refusal) reads them again
const load = async () => {
  const request = ++openRequest
  const target = name.value
  loading.value = true
  const [nodes, apps, outages] = await Promise.all([
    store.getNodeCount(target),
    store.getApplicationsUsingPerspective(target),
    store.getPerspectiveOutageCount(target),
    minionStore.getMinions()
  ])
  if (request !== openRequest) {
    return
  }
  minionCount.value = minionStore.byLocation[target]?.length ?? 0
  nodeCount.value = nodes
  applications.value = apps
  outageCount.value = outages
  loading.value = false
  await nextTick()
  ;(blocked.value ? cancelButton.value : confirmInput.value)?.$el?.focus?.()
}

watch(() => props.visible, (isVisible) => {
  if (!isVisible || !props.location) {
    return
  }
  errorText.value = ''
  confirmText.value = ''
  nodeCount.value = null
  minionCount.value = 0
  applications.value = null
  outageCount.value = null
  load()
})

const confirmDelete = async () => {
  if (!canDelete.value) {
    return
  }
  deleting.value = true
  errorText.value = ''
  try {
    const summary: LocationDeletedSummary = { name: name.value, applications: applications.value, outageCount: outageCount.value }
    const result = await store.deleteLocation(summary.name)
    if (result.success) {
      emit('update:visible', false)
      emit('deleted', summary)
    } else {
      errorText.value = result.message
      await load()
    }
  } finally {
    deleting.value = false
  }
}
</script>

<style lang="scss" scoped>
.body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 0.25rem;
}

.subtitle {
  margin: 0;
  color: var(--p-text-muted-color);
}

.loading {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.confirm-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--p-text-color);
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

a {
  color: var(--p-primary-color);
  text-decoration: underline;
}
</style>
