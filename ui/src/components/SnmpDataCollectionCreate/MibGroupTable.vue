<template>
  <TableCard
    class="mib-group-table-card"
    v-if="store.mibGroupDrawerState.isEditMode === CreateEditMode.None"
  >
    <div class="header">
      <div class="title-container">
        <h3 class="title">MIB Groups</h3>
      </div>
      <div class="action-container">
        <FeatherButton
          primary
          data-test="add-mib-group-button"
          @click="onAddMibGroupClicked"
        >
          Add MIB Group
        </FeatherButton>
      </div>
    </div>
    <div class="container">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Interface Type</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <template
            v-for="(mibGroup, index) in store.configForm.mibGroup"
            :key="index"
          >
            <tr>
              <td>{{ mibGroup.name }}</td>
              <td>{{ mibGroup.ifType }}</td>
              <td>{{ mibGroup.enabled ? 'Enabled' : 'Disabled' }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit"
                    :title="`Edit ${mibGroup.name}`"
                    data-test="edit-button"
                    @click="onMibGroupEditClicked(mibGroup)"
                  >
                    <FeatherIcon :icon="Edit" />
                  </FeatherButton>
                  <FeatherButton
                    icon="Delete"
                    :title="`Delete ${mibGroup.name}`"
                    data-test="delete-button"
                    @click="onMibGroupDeleteClicked(mibGroup, index)"
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
                <h5>Mib Group Names</h5>
                <p class="description">{{ JSON.parse(mibGroup.mibGroupNames || '[]').join(', ') }}</p>
                <div v-if="JSON.parse(mibGroup.mibObjects || '[]').length > 0">
                  <h5>Mib Objects:</h5>
                  <div
                    v-for="(value, objIndex) in JSON.parse(mibGroup.mibObjects || '[]')"
                    :key="value.alias"
                  >
                    <h6>Object {{ Number(objIndex) + 1 }}</h6>
                    <div>
                      <strong>Alias:</strong> {{ value.alias }} <br />
                      <strong>OID:</strong> {{ value.oid }} <br />
                      <strong>Instance:</strong> {{ value.instance }} <br />
                      <strong>Data Type:</strong> {{ value.type }}
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div v-if="!store.configForm.mibGroup.length">
        <EmptyList :content="{ msg: 'No MIB Groups found.' }" />
      </div>
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { CreateEditMode } from '@/types'
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

const onAddMibGroupClicked = () => {
  store.mibGroupDrawerState = ({
    isEditMode: CreateEditMode.Create,
    visible: true,
    mibGroupIndex: -1
  })
}

const onMibGroupEditClicked = (mibGroup: any) => {
  store.mibGroupDrawerState = ({
    isEditMode: CreateEditMode.Edit,
    visible: true,
    mibGroupIndex: store.configForm.mibGroup.findIndex(group => group === mibGroup)
  })
}

const onMibGroupDeleteClicked = (mibGroup: any, index: number) => {
  store.configForm.mibGroup.splice(index, 1)
}
</script>

<style lang="scss" scoped>
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';
@import '@featherds/table/scss/table';
@import '@/styles/_transitionDataTable';

.mib-group-table-card {
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

