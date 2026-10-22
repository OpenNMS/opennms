<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Delete Minion?"
    class="minion-delete-dialog"
    width="min(640px, 95vw)"
    data-test="minion-delete-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="body">
      <p class="subtitle">Use this for Minions you have decommissioned. It cannot be undone.</p>

      <dl class="details">
        <dt>Minion</dt>
        <dd><code data-test="minion-id">{{ minion?.id }}</code></dd>
        <dt>Last heartbeat</dt>
        <dd :title="formatAbsolute(lastDate)" data-test="last-heartbeat">{{ relativeTimeSince(lastDate, checkedAt) ?? '-' }}</dd>
      </dl>

      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <div v-if="loading" class="loading" data-test="loading">
        <OnmsSpinner size="1.5rem" strokeWidth="6" />
        <span>Checking the status of this Minion…</span>
      </div>

      <OnmsMessage v-else-if="state === 'up'" severity="error" data-test="status-callout">
        <strong>The Minion is <OnmsTag value="UP" severity="success" class="status-tag" /></strong>
        — a running Minion cannot be deleted.
      </OnmsMessage>
      <OnmsMessage v-else-if="state === 'down'" severity="info" data-test="status-callout">
        <strong>The Minion is <OnmsTag value="DOWN" severity="danger" class="status-tag" /></strong>
        — it can be deleted.
      </OnmsMessage>
      <OnmsMessage v-else severity="info" data-test="status-callout">
        <strong>The Minion is <OnmsTag value="UNKNOWN" severity="secondary" class="status-tag" /></strong>
        — it can be deleted.
      </OnmsMessage>
    </div>

    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" autofocus data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton
        severity="danger"
        label="Delete Minion"
        :disabled="!canDelete"
        data-test="delete-button"
        @click="confirmDelete"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsMessage, OnmsSpinner, OnmsTag, useOnmsToast } from '@opennms/onms-ui'

import { MinionState, minionState } from '@/lib/minionStatus'
import { formatAbsolute, relativeTimeSince } from '@/lib/relativeTime'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { Minion } from '@/types/minionAdmin'

// the list's auto-refresh is paused behind this dialog, so it re-reads the row itself
const RECHECK_MS = 30 * 1000

const props = defineProps<{
  visible: boolean
  minion: Minion | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const store = useMinionAdminStore()
const { showToast } = useOnmsToast()

const loading = ref(false)
const deleting = ref(false)
const errorText = ref('')
// the status actually read for this open, never the paused list
const lastStatus = ref<Minion['status']>(null)
// shown for information only; the decision follows the status
const lastDate = ref<Minion['date']>(null)
const checkedAt = ref(Date.now())

// the same up / down / unknown the table shows; only UP refuses. Unknown means the
// Minion has no node in the Minions requisition, so OpenNMS does not monitor it
const state = computed<MinionState>(() => minionState(lastStatus.value))

const canDelete = computed(() => !loading.value && !deleting.value && state.value !== 'up' && !!props.minion)

let openRequest = 0
let recheck: ReturnType<typeof setInterval> | undefined

const stopRecheck = () => {
  clearInterval(recheck)
  recheck = undefined
}

// a read that fails keeps the last decision, so a network blip never unblocks a running Minion
const readMinion = async (id: string, request: number) => {
  const fresh = await store.getMinion(id)
  if (request !== openRequest || !fresh) {
    return
  }
  lastStatus.value = fresh.status ?? null
  lastDate.value = fresh.date ?? null
  checkedAt.value = Date.now()
}

watch(() => props.visible, async (isVisible) => {
  stopRecheck()
  if (!isVisible || !props.minion) {
    return
  }
  const request = ++openRequest
  const id = props.minion.id
  errorText.value = ''
  lastStatus.value = props.minion.status ?? null
  lastDate.value = props.minion.date ?? null
  checkedAt.value = Date.now()
  loading.value = true
  await readMinion(id, request)
  if (request !== openRequest) {
    return
  }
  loading.value = false
  recheck = setInterval(() => readMinion(id, request), RECHECK_MS)
})

onBeforeUnmount(stopRecheck)

const confirmDelete = async () => {
  const minion = props.minion
  if (!canDelete.value || !minion) {
    return
  }
  deleting.value = true
  errorText.value = ''
  try {
    const result = await store.deleteMinion(minion.id)
    if (result.success) {
      showToast({ message: `Minion '${minion.id}' deleted.`, severity: 'success' })
      emit('update:visible', false)
    } else {
      errorText.value = result.message
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

.details {
  display: grid;
  grid-template-columns: max-content 1fr;
  gap: 0.25rem 1rem;
  margin: 0;

  dt {
    color: var(--p-text-muted-color);
  }

  dd {
    margin: 0;
    overflow-wrap: anywhere;
  }
}

.loading {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.status-tag {
  vertical-align: middle;
  margin: 0 0.15rem;
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}
</style>
