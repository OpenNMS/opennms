<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Delete ${minion?.id ?? ''}?`"
    class="minion-delete-dialog"
    width="min(640px, 95vw)"
    data-test="minion-delete-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="body">
      <p class="subtitle">Use this for Minions you have decommissioned.</p>

      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <OnmsMessage v-if="recent" severity="error" data-test="heartbeat-callout">
        <strong>Last heartbeat {{ heartbeat }}</strong> — this Minion is still running. Stop the Minion process
        first, then delete it here; otherwise it registers again within 30 seconds.
      </OnmsMessage>
      <OnmsMessage v-else severity="success" data-test="heartbeat-callout">
        <strong>Last heartbeat {{ heartbeat }}</strong> — it looks stopped. A Minion that is still running
        registers again within 30 seconds, with its label and properties cleared.
      </OnmsMessage>

      <div v-if="loading" class="loading" data-test="loading">
        <OnmsSpinner size="1.5rem" strokeWidth="6" />
        <span>Counting its alarms…</span>
      </div>
      <OnmsMessage v-else-if="alarmCount === null" severity="info" data-test="alarms-callout">
        Its alarms could not be counted; any alarms raised through this Minion are removed with it.
      </OnmsMessage>
      <OnmsMessage v-else-if="alarmCount > 0" severity="error" data-test="alarms-callout">
        <strong>Its alarms are deleted</strong> — {{ alarmCount }} {{ alarmCount === 1 ? 'alarm' : 'alarms' }}
        raised through this Minion, such as traps and syslog it received, {{ alarmCount === 1 ? 'is' : 'are' }}
        removed with it.
      </OnmsMessage>
      <OnmsMessage v-else severity="info" data-test="alarms-callout">
        No alarms were raised through this Minion.
      </OnmsMessage>

      <OnmsMessage severity="info" data-test="requisition-callout">
        <strong>Its node stays until the Minions requisition is synchronized</strong> — the node is removed
        from the pending Minions requisition only, so it keeps being monitored until that requisition is
        synchronized.
        <a :href="requisitionUrl" target="_self" data-test="requisition-link">Open the Minions requisition</a>
      </OnmsMessage>
    </div>

    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
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
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsMessage, OnmsSpinner, useOnmsToast } from '@opennms/onms-ui'

import { legacyUrl } from '@/lib/legacyUrl'
import { minionState } from '@/lib/minionStatus'
import { relativeTimeSince, toMillis } from '@/lib/relativeTime'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { Minion } from '@/types/minionAdmin'

// heartbeats arrive every 30 s, so anything within two minutes is a live Minion
const RECENT_MS = 2 * 60 * 1000

const props = defineProps<{
  visible: boolean
  minion: Minion | null
  now: number
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const store = useMinionAdminStore()
const { showToast } = useOnmsToast()

const requisitionUrl = legacyUrl('admin/ng-requisitions/index.jsp#/requisitions/Minions')

const loading = ref(false)
const deleting = ref(false)
const errorText = ref('')
const alarmCount = ref<number | null>(null)

const heartbeat = computed(() => relativeTimeSince(props.minion?.date, props.now) ?? 'unknown')

// with no usable heartbeat time, an UP status is the only sign it is still running
const recent = computed(() => {
  const ms = toMillis(props.minion?.date)
  return ms === null ? minionState(props.minion?.status) === 'up' : props.now - ms < RECENT_MS
})

const canDelete = computed(() => !loading.value && !deleting.value && !recent.value && !!props.minion)

let openRequest = 0

watch(() => props.visible, async (isVisible) => {
  if (!isVisible || !props.minion) {
    return
  }
  const request = ++openRequest
  errorText.value = ''
  alarmCount.value = null
  loading.value = true
  const alarms = await store.getAlarmCount(props.minion.id)
  if (request !== openRequest) {
    return
  }
  alarmCount.value = alarms
  loading.value = false
})

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

.loading {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
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
