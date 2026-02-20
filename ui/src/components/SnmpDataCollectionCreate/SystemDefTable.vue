<template>
  <TableCard
    class="system-def-table-card"
    v-if="store.systemDefDrawerState.isEditMode === CreateEditMode.None"
  >
    <div class="header">
      <div class="title-container">
        <h3 class="title">System Definitions</h3>
      </div>
      <div class="action-container">
        <FeatherButton
          primary
          data-test="add-system-def-button"
          @click="onAddSystemDefClicked"
        >
          Add System Definition
        </FeatherButton>
      </div>
    </div>
    <div class="container">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>SysOID</th>
            <th>SysOID Mask</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(systemDef, index) in store.configForm.systemDef"
            :key="systemDef.id"
          >
            <td>{{ systemDef.name }}</td>
            <td>{{ systemDef.sysoid }}</td>
            <td>{{ systemDef.sysoidMask }}</td>
            <td>{{ systemDef.enabled ? 'Enabled' : 'Disabled' }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  :title="`Edit ${systemDef.name}`"
                  data-test="edit-button"
                  @click="onSystemDefEditClicked(systemDef)"
                >
                  <FeatherIcon :icon="Edit" />
                </FeatherButton>
                <FeatherButton
                  icon="Delete"
                  :title="`Delete ${systemDef.name}`"
                  data-test="delete-button"
                  @click="onSystemDefDeleteClicked(systemDef, index)"
                >
                  <FeatherIcon :icon="Delete" />
                </FeatherButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!store.configForm.systemDef.length">
        <EmptyList :content="{ msg: 'No System Definitions found.' }" />
      </div>
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { SnmpCollectionSystemDefPayload } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { CreateEditMode } from '@/types'

const store = useSnmpDataCollectionCreationStore()

const onSystemDefEditClicked = (systemDef: SnmpCollectionSystemDefPayload) => {
  store.systemDefDrawerState = {
    visible: true,
    isEditMode: CreateEditMode.Edit,
    systemDefIndex: store.configForm.systemDef.findIndex(def => def.id === systemDef.id)
  }
}

const onSystemDefDeleteClicked = (systemDef: SnmpCollectionSystemDefPayload, index: number) => {
  store.configForm.systemDef.splice(index, 1)
}

const onAddSystemDefClicked = () => {
  store.systemDefDrawerState = {
    visible: true,
    isEditMode: CreateEditMode.Create,
    systemDefIndex: -1
  }
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.system-def-table-card {
  padding: 20px;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .title-container {
      .title {
        @include typography.headline3;
        margin: 0;
      }
    }

    .action-container {
      display: flex;
      align-items: center;
      gap: 10px;

      button {
        margin: 0;
      }
    }
  }

  .container {
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

        .description {
          margin: 0;
          white-space: normal;
        }
      }
    }

  }
}
</style>

