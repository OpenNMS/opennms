<template>
  <TableCard
    class="system-def-table-card"
    v-if="!showSystemDefForm"
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
            v-for="(systemDef, index) in systemDefs"
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
      <div v-if="!systemDefs.length">
        <EmptyList :content="{ msg: 'No System Definitions found.' }" />
      </div>
    </div>
  </TableCard>
  <SystemDefForm
    v-if="showSystemDefForm"
    :system-def="selectedSystemDef"
    @cancel="handleCancel"
    @save="handleSystemDefSave"
  />
</template>

<script lang="ts" setup>
import { SnmpCollectionSystemDefPayload } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import SystemDefForm from './SystemDefForm.vue'

const systemDefs = ref<SnmpCollectionSystemDefPayload[]>([])
const showSystemDefForm = ref(false)
const selectedSystemDef = ref<SnmpCollectionSystemDefPayload | null>(null)

const onSystemDefEditClicked = (systemDef: SnmpCollectionSystemDefPayload) => {
  selectedSystemDef.value = systemDef
  showSystemDefForm.value = true
}

const onSystemDefDeleteClicked = (systemDef: SnmpCollectionSystemDefPayload, index: number) => {
  systemDefs.value.splice(index, 1)
}

const onAddSystemDefClicked = () => {
  selectedSystemDef.value = null
  showSystemDefForm.value = true
}

const handleCancel = () => {
  selectedSystemDef.value = null
  showSystemDefForm.value = false
}

const handleSystemDefSave = (systemDef: SnmpCollectionSystemDefPayload) => {
  if (selectedSystemDef.value) {
    // Edit existing system definition
    const index = systemDefs.value.findIndex(def => def.id === systemDef.id)
    if (index !== -1) {
      systemDefs.value[index] = systemDef
    }
  } else {
    // Add new system definition
    systemDefs.value.push(systemDef)
  }

  selectedSystemDef.value = null
  showSystemDefForm.value = false
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

