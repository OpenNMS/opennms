<template>
  <TableCard
    class="snmp-v3-user-management"
    data-test="snmpv3-user-management"
    v-if="!store.createUserDrawerState.visible"
  >
    <div class="header">
      <div class="section-left">
        <h3>SNMPv3 User Management</h3>
      </div>
      <div class="section-right">
        <PButton
          data-test="add-user-button"
          label="Add User"
          @click="store.openCreateUserDrawer(CreateEditMode.Create, -1)"
        />
      </div>
    </div>
    <div class="info-section">
      <span>Configure SNMPv3 user settings.</span>
      <FeatherIcon
        :icon="InfoIcon"
        class="info-icon"
        role="button"
        tabindex="0"
        @click="isMessageDialogVisible = true"
        @keydown.enter.space.prevent="isMessageDialogVisible = true"
        data-test="trap-config-snmpv3-users-info-icon"
      />
    </div>
    <div class="table-container">
      <PDataTable
        :value="tableRecords"
        aria-label="SNMPv3 Users Table"
      >
        <PColumn
          field="securityName"
          header="Security Name"
          sortable
        />
        <PColumn
          field="securityLevel"
          header="Security Level"
          sortable
        >
          <template #body="{ data }">
            {{ displaySecurityLevel(data.securityLevel) }}
          </template>
        </PColumn>
        <PColumn
          field="authProtocol"
          header="Authentication Protocol"
          sortable
        />
        <PColumn
          field="privacyProtocol"
          header="Privacy Protocol"
          sortable
        />
        <PColumn header="Action">
          <template #body="{ data }">
            <div class="action-container">
              <PButton
                text
                aria-label="Edit User"
                data-test="edit-user-button"
                @click="store.openCreateUserDrawer(CreateEditMode.Edit, userIndex(data))"
              >
                <FeatherIcon :icon="Edit" />
              </PButton>
              <PButton
                text
                aria-label="Delete User"
                data-test="delete-user-button"
                @click="openDeleteUserDialog(userIndex(data))"
              >
                <FeatherIcon :icon="Delete" />
              </PButton>
            </div>
          </template>
        </PColumn>
        <template #empty>
          <EmptyList :content="{ msg: 'No SNMPv3 users found' }" />
        </template>
      </PDataTable>
    </div>
    <DeleteUserConfirmationDialog
      :index="deleteUserIndex"
      :visible="deleteDialogVisible"
      @close="cancelDeleteUser"
      @confirm="confirmDeleteUser"
    />
    <MessageDialog
      :visible="isMessageDialogVisible"
      maxHeight="22em"
      maxWidth="50em"
      title="SNMPv3 User Management"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div>
          <p>Configure SNMPv3 user settings.</p>
          <p><strong>Note</strong> that the settings here apply to the OpenNMS core system as well as to any Minions or other distributed components.</p>
        </div>
      </template>
    </MessageDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import InfoIcon from '@featherds/icon/action/Info'
import useSnackbar from '@/composables/useSnackbar'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { CreateEditMode } from '@/types'
import { SnmpV3User, TrapConfig } from '@/types/trapConfig'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import DeleteUserConfirmationDialog from './Dialog/DeleteUserConfirmationDialog.vue'
import MessageDialog from '../Common/MessageDialog.vue'
import { SECURITY_LEVEL_OPTIONS } from '@/lib/trapdValidator'

const PButton = Button
const PColumn = Column
const PDataTable = DataTable

const store = useTrapdConfigStore()
const { showSnackBar } = useSnackbar()
const tableRecords = ref<SnmpV3User[]>([])
const deleteUserIndex = ref<number | null>(null)
const deleteDialogVisible = ref<boolean>(false)
const isDeleting = ref(false)
const isMessageDialogVisible = ref(false)

// Resolve a row back to its index in the store's user list. DataTable may
// reorder rows when sorting, so we can't rely on the rendered row index.
const userIndex = (user: SnmpV3User) => store.snmpV3Users.indexOf(user)

const openDeleteUserDialog = (index: number) => {
  deleteUserIndex.value = index
  deleteDialogVisible.value = true
}

const cancelDeleteUser = () => {
  deleteUserIndex.value = null
  deleteDialogVisible.value = false
}

const displaySecurityLevel = (level: number) => {
  const index = level - 1

  if (index < 0 || index > 2) {
    return '--'
  }

  return SECURITY_LEVEL_OPTIONS[index]._text
}

const confirmDeleteUser = async () => {
  if (deleteUserIndex.value === null || isDeleting.value) {
    cancelDeleteUser()
    return
  }

  if (!store.snmpV3Users[deleteUserIndex.value]) {
    showSnackBar({ msg: 'SNMPv3 user not found.', error: true })
    cancelDeleteUser()
    return
  }

  try {
    isDeleting.value = true
    const payload: TrapConfig = { ...store.trapdConfig }
    payload.snmpv3User = payload.snmpv3User.filter((_, idx) => idx !== deleteUserIndex.value)
    await updateTrapdConfiguration(payload)
    await store.fetchTrapConfig()
    cancelDeleteUser()
    showSnackBar({ msg: 'SNMPv3 user deleted successfully.' })
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to delete SNMPv3 user.'
    showSnackBar({ msg, error: true })
  } finally {
    isDeleting.value = false
  }
}

watch(
  () => store.snmpV3Users, () => {
    tableRecords.value = store.snmpV3Users || []
  }, { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';

.snmp-v3-user-management {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      h3 {
        @include typography.headline3;
        color: var(--p-text-color);
      }

      p {
        @include typography.body-large;
        color: var(--p-text-muted-color);
      }
    }
  }

  .info-section {
    margin-bottom: 1em;

    .label {
      color: var(--p-text-color);
    }

    .info-icon {
      cursor: pointer;
      font-size: 1.5em;
      margin-left: 0.5em;
      color: var(--p-primary-color);

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .spacer {
    height: 0.5em;
  }

  .table-container {
    .action-container {
      display: flex;
      align-items: center;
      gap: 5px;

      // enlarge the edit/delete icons (FeatherIcon scales with font-size)
      :deep(.p-button) {
        font-size: 1.3rem;
      }
    }
  }
}
</style>
