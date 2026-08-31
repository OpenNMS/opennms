<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="daemon-management">
    <TableCard>
      <div class="header">
        <div class="card-title" data-test="daemon-title">Daemon Management</div>
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
              @click="reload(data)"
            />
          </template>
        </OnmsColumn>
      </OnmsTable>
    </TableCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsTable } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import TableCard from '@/components/Common/TableCard.vue'
import DaemonManagementAbout from '@/components/DaemonManagement/DaemonManagementAbout.vue'
import useSnackbar from '@/composables/useSnackbar'
import { RELOADABLE_DAEMONS, ReloadableDaemon, reloadDaemon } from '@/services/daemonService'
import { BreadCrumb } from '@/types'

const { showSnackBar } = useSnackbar()

const breadcrumbs: BreadCrumb[] = [
  { label: 'Home', to: '/' },
  { label: 'Daemon Management', to: '/daemon-management' }
]

const reloading = ref('')

const reload = async (daemon: ReloadableDaemon) => {
  reloading.value = daemon.name
  try {
    await reloadDaemon(daemon.name)
    showSnackBar({ msg: `Reload requested for ${daemon.label}. The daemon reports the outcome as an event.`, timeout: 8000 })
  } catch (_err) {
    showSnackBar({ msg: `Failed to request a reload for ${daemon.label}.`, error: true })
  } finally {
    reloading.value = ''
  }
}
</script>

<style scoped lang="scss">
.daemon-management {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .subtitle {
    margin: 0.25rem 0 1rem 0;
    color: var(--p-text-muted-color);
  }
}
</style>
