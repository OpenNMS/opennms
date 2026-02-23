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
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <template
            v-for="(systemDef, index) in store.configForm.systemDef"
            :key="index"
          >
            <tr>
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
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(index)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(index)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(index)"
                    />
                    <FeatherIcon
                      :icon="ExpandMore"
                      v-else
                    />
                  </FeatherButton>
                </div>
              </td>
            </tr>
            <tr
              v-if="expandedRows.includes(index)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h6>Mib Group Names:</h6>
                <p
                  class="description"
                  v-html="JSON.parse(systemDef.mibGroupNames || '[]').join(', ')"
                ></p>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div v-if="!store.configForm.systemDef.length">
        <EmptyList :content="{ msg: 'No System Definitions found.' }" />
      </div>
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDefPayload } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import ExpandLess from '@featherds/icon/navigation/ExpandLess'
import ExpandMore from '@featherds/icon/navigation/ExpandMore'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

const store = useSnmpDataCollectionCreationStore()
const expandedRows = ref<number[]>([])

const toggleExpand = (id: number) => {
  const index = expandedRows.value.indexOf(id)
  if (index === -1) {
    expandedRows.value.push(id)
  } else {
    expandedRows.value.splice(index, 1)
  }
}

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
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';
@import '@featherds/table/scss/table';
@import '@/styles/_transitionDataTable';

.system-def-table-card {
  padding: 20px;
  margin-bottom: 20px;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .title-container {
      .title {
        @include headline3;
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
      @include table;

      thead {
        background: var($background);
        text-transform: uppercase;
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        border-bottom: 1px solid var($border-on-surface);

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

