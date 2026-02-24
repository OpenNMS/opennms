<template>
  <TableCard
    class="resource-type-table-card"
    v-if="store.resourceTypeDrawerState.isEditMode === CreateEditMode.None"
  >
    <div class="header">
      <div class="title-container">
        <h3 class="title">Resource Types</h3>
      </div>
      <div class="action-container">
        <FeatherButton
          primary
          data-test="add-resource-type-button"
          @click="onAddResourceTypeClicked"
        >
          Add Resource Type
        </FeatherButton>
      </div>
    </div>
    <div class="container">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Label</th>
            <th>Resource Label</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <template
            v-for="(resourceType, index) in store.configForm.resourceType"
            :key="index"
          >
            <tr>
              <td>{{ resourceType.name }}</td>
              <td>{{ resourceType.label }}</td>
              <td>{{ resourceType.resourceLabel }}</td>
              <td>{{ resourceType.enabled ? 'Enabled' : 'Disabled' }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit"
                    :title="`Edit ${resourceType.name}`"
                    data-test="edit-button"
                    @click="onResourceTypeEditClicked(resourceType)"
                  >
                    <FeatherIcon :icon="Edit" />
                  </FeatherButton>
                  <FeatherButton
                    icon="Delete"
                    :title="`Delete ${resourceType.name}`"
                    data-test="delete-button"
                    @click="onResourceTypeDeleteClicked(resourceType, index)"
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
                <h6>Storage Strategy:</h6>
                <p class="description">{{ resourceType.storageStrategy }}</p>
                <h6>Persistence Selector Strategy:</h6>
                <p class="description">{{ resourceType.persistenceSelectorStrategy }}</p>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div v-if="!store.configForm.resourceType.length">
        <EmptyList :content="{ msg: 'No Resource Types found.' }" />
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
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

const onAddResourceTypeClicked = () => {
  store.resourceTypeDrawerState = ({
    isEditMode: CreateEditMode.Create,
    visible: true,
    resourceTypeIndex: -1
  })
}

const onResourceTypeEditClicked = (resourceType: any) => {
  store.resourceTypeDrawerState = ({
    isEditMode: CreateEditMode.Edit,
    visible: true,
    resourceTypeIndex: store.configForm.resourceType.findIndex(def => def.id === resourceType.id)
  })
}

const onResourceTypeDeleteClicked = (resourceType: any, index: number) => {
  store.configForm.resourceType.splice(index, 1)
}
</script>

<style lang="scss" scoped>
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';
@import '@featherds/table/scss/table';
@import '@/styles/_transitionDataTable';

.resource-type-table-card {
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

