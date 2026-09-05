<template>
  <TableCard class="daemons-table">
    <div class="header">
      <div class="card-title" data-test="daemon-title">Daemons</div>
      <AboutDialogButton title="Daemon Management">
        <DaemonManagementAbout />
      </AboutDialogButton>
    </div>
    <p class="subtitle">
      Reload a daemon's configuration without restarting OpenNMS. The reload runs asynchronously;
      the daemon reports the outcome as a <em>reloadDaemonConfigSuccessful</em> or
      <em>reloadDaemonConfigFailed</em> event.
    </p>

    <OnmsTable :value="RELOADABLE_DAEMONS" dataKey="name" data-test="daemons-table">
      <OnmsColumn field="label" header="Daemon" />
      <OnmsColumn field="description" header="Reload applies" />
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsButton
            variant="outlined"
            label="Reload"
            :disabled="reloading === data.name"
            :data-test="`reload-${data.name}`"
            @click="askReload(data)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>
  </TableCard>

  <OnmsConfirmationDialog
    :visible="showReloadConfirmation"
    title="Reload Daemon Configuration"
    actionButtonText="Reload"
    @ok="confirmReload"
    @cancel="cancelReload"
  >
    <template #content>
      <p data-test="reload-confirm-text">
        Reload the configuration of <strong>{{ daemonToReload?.label }}</strong>?
        The daemon re-reads its configuration and rebuilds its schedules, which can
        briefly delay its regular work on large installations.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsTable, useOnmsToast } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import TableCard from '@/components/Common/TableCard.vue'
import DaemonManagementAbout from '@/components/DaemonManagement/DaemonManagementAbout.vue'
import { RELOADABLE_DAEMONS, ReloadableDaemon, reloadDaemon } from '@/services/daemonService'

const { showToast } = useOnmsToast()

const reloading = ref('')
const showReloadConfirmation = ref(false)
const daemonToReload = ref<ReloadableDaemon | null>(null)

const askReload = (daemon: ReloadableDaemon) => {
  daemonToReload.value = daemon
  showReloadConfirmation.value = true
}

const cancelReload = () => {
  showReloadConfirmation.value = false
  daemonToReload.value = null
}

// a 400 from POST /rest/events carries the validation reason as a plain-text
// body — the only actionable detail a failed request has
const serverMessage = (err: unknown): string => {
  const data = (err as { response?: { data?: unknown }})?.response?.data
  return typeof data === 'string' && data.length ? ` Server said: ${data.split('\n')[0]}` : ''
}

const confirmReload = async () => {
  const daemon = daemonToReload.value
  showReloadConfirmation.value = false
  daemonToReload.value = null
  if (!daemon) {
    return
  }
  reloading.value = daemon.name
  try {
    await reloadDaemon(daemon.name)
    showToast({
      message: `Reload requested for ${daemon.label}. The daemon reports the outcome as an event.`,
      severity: 'success',
      timeout: 8000
    })
  } catch (err) {
    showToast({
      message: `Failed to request a reload for ${daemon.label}.${serverMessage(err)}`,
      severity: 'error'
    })
  } finally {
    reloading.value = ''
  }
}
</script>

<style scoped lang="scss">
.daemons-table {
  padding: 25px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.subtitle {
  margin: 0.25rem 0 1rem 0;
  color: var(--p-text-muted-color);
}
</style>
