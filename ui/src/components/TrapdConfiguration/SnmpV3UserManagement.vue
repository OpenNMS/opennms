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
        <FeatherButton
          primary
          data-test="add-user-button"
          @click="store.openCreateUserDrawer(CreateEditMode.Create, -1)"
        >
          Add User
        </FeatherButton>
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
      <table
        class="data-table"
        aria-label="SNMPv3 Users Table"
      >
        <thead>
          <tr>
            <FeatherSortHeader
              v-for="col of columns"
              :key="col.label"
              scope="col"
              :property="col.id"
              :sort="(sort as any)[col.id]"
              v-on:sort-changed="sortChanged"
            >
              {{ col.label }}
            </FeatherSortHeader>
            <th>Action</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="(user, index) in tableRecords"
            :key="index"
          >
            <td>{{ user.securityName }}</td>
            <td>{{ displaySecurityLevel(user.securityLevel) }}</td>
            <td>{{ user.authProtocol }}</td>
            <td>{{ user.privacyProtocol }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit User"
                  data-test="edit-user-button"
                  @click="store.openCreateUserDrawer(CreateEditMode.Edit, index)"
                >
                  <FeatherIcon :icon="Edit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  icon="Delete User"
                  data-test="delete-user-button"
                  @click="openDeleteUserDialog(index)"
                >
                  <FeatherIcon :icon="Delete"> </FeatherIcon>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div v-if="!tableRecords.length">
        <EmptyList :content="{ msg: 'No SNMPv3 users found' }" />
      </div>
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
import { computed, reactive, ref, watch } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import InfoIcon from '@featherds/icon/action/Info'
import { FeatherSortHeader, SORT } from '@featherds/table'
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

const store = useTrapdConfigStore()
const { showSnackBar } = useSnackbar()
const tableRecords = ref<SnmpV3User[]>([])
const deleteUserIndex = ref<number | null>(null)
const deleteDialogVisible = ref<boolean>(false)
const isDeleting = ref(false)
const isMessageDialogVisible = ref(false)

const columns = computed(() => [
  { id: 'securityName', label: 'Security Name' },
  { id: 'securityLevel', label: 'Security Level' },
  { id: 'authenticationProtocol', label: 'Authentication Protocol' },
  { id: 'privacyProtocol', label: 'Privacy Protocol' }
])

const sort = reactive({
  securityName: SORT.NONE,
  securityLevel: SORT.NONE,
  authenticationProtocol: SORT.NONE,
  privacyProtocol: SORT.NONE
}) as any

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.snmp-v3-user-management {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      h3 {
        @include typography.headline3;
        color: var(--feather-text-primary);
      }

      p {
        @include typography.body-large;
        color: var(--feather-text-secondary);
      }
    }
  }

  .info-section {
    margin-bottom: 1em;

    .label {
      color: var(variables.$primary-text-on-surface);
    }

    .info-icon {
      cursor: pointer;
      font-size: 1.5em;
      margin-left: 0.5em;
      color: var(variables.$primary);

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .spacer {
    height: 0.5em;
  }

  .table-container {
    table {
      width: 100%;
      @include table.table;

      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        border-bottom: 1px solid var(variables.$border-on-surface);

        .action-container {
          display: flex;
          align-items: center;
          gap: 5px;

          button {
            margin: 0px;
          }

          :deep(.feather-menu-dropdown) {
            .feather-dropdown {
              li {
                a {
                  padding: 8px 16px !important;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
