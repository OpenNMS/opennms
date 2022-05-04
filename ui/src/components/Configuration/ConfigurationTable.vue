<template>
  <div class="main-wrapper">
    <table class="condensed">
      <thead>
        <tr class="tr">
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            :property="RequisitionData.ImportName"
            :sort="sorts[RequisitionData.ImportName]"
            v-on:sort-changed="sortChanged"
            >Name</FeatherSortHeader
          >
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            :property="RequisitionData.ImportURL"
            :sort="sorts[RequisitionData.ImportURL]"
            v-on:sort-changed="sortChanged"
            >URL</FeatherSortHeader
          >
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            :property="RequisitionData.CronSchedule"
            :sort="sorts[RequisitionData.CronSchedule]"
            v-on:sort-changed="sortChanged"
            >Schedule Frequency</FeatherSortHeader
          >
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            :property="RequisitionData.RescanExisting"
            :sort="sorts[RequisitionData.RescanExisting]"
            v-on:sort-changed="sortChanged"
            >Rescan Behavior</FeatherSortHeader
          >
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-bind:key="key"
          v-for="(item, key) in filteredItems"
        >
          <td>
            <ConfigurationCopyPasteDisplay :text="item[RequisitionData.ImportName]" />
          </td>
          <td>
            <ConfigurationCopyPasteDisplay :text="item[RequisitionData.ImportURL]" />
          </td>
          <td>
            <ConfigurationCopyPasteDisplay
              :showCopyBtn="false"
              :text="ConfigurationHelper.cronToEnglish(item[RequisitionData.CronSchedule])"
            />
          </td>
          <td>
            {{ rescanToEnglish(item[RequisitionData.RescanExisting]) }}
          </td>
          <td>
            <div class="flex">
              <FeatherButton
                icon="Edit"
                @click="() => props.editClicked(item.originalIndex)"
              >
                <FeatherIcon
                  :icon="Edit"
                  class="edit-icon"
                ></FeatherIcon>
              </FeatherButton>
              <FeatherButton
                icon="Delete"
                @click="() => props.deleteClicked(item.originalIndex)"
              >
                <FeatherIcon
                  class="delete-icon"
                  :icon="Delete"
                ></FeatherIcon>
              </FeatherButton>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <FeatherPagination
      :total="pageVals.total"
      :page-size="pageVals.pageSize"
      :modelValue="pageVals.page"
      @update:modelValue="pageUpdate"
      @update:pageSize="pageSizeUpdate"
    />
  </div>
</template>

<script
  setup
  lang="ts"
>
import { ComputedRef, PropType } from 'vue'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'

import Edit from '@featherds/icon/action/Edit'
import Delete from '@featherds/icon/action/Delete'

import { RequisitionData } from './copy/requisitionTypes'
import { ConfigurationHelper } from './ConfigurationHelper'
import ConfigurationCopyPasteDisplay from './ConfigurationCopyPasteDisplay.vue'
import { ConfigurationPageVals, ConfigurationTableSort, ProvisionDServerConfiguration } from './configuration.types'
import { rescanCopy } from './copy/rescanItems'

/**
 * Props
 */
const props = defineProps({
  itemList: { required: true, type: Array as PropType<Array<ProvisionDServerConfiguration>> },
  editClicked: { type: Function, required: true },
  deleteClicked: { type: Function, required: true },
  setNewPage: { type: Function, required: true }
})

/**
 * Local State
 */
const sorts = reactive<ProvisionDServerConfiguration>({
  [RequisitionData.ImportName]: SORT.NONE,
  [RequisitionData.CronSchedule]: SORT.NONE,
  [RequisitionData.ImportURL]: SORT.NONE,
  [RequisitionData.RescanExisting]: SORT.NONE,
  currentSort: { property: RequisitionData.ImportName, value: SORT.NONE },
  originalIndex: 0
})

const itemList = computed(() => props.itemList)

const pageVals: ComputedRef<ConfigurationPageVals> = computed(() => {
  return reactive({
    total: itemList?.value?.length || 0,
    page: pageVals?.value?.page || 1,
    pageSize: pageVals?.value?.pageSize || 10
  })
})

/**
 * Sorts and filters all of the given items by the current state of the table.
 */
const filteredItems = computed(() => {
  const currentTablePage = pageVals.value.pageSize * (pageVals.value.page - 1)
  const currentSortKey = sorts.currentSort?.property || ''

  let myItems: Array<ProvisionDServerConfiguration> = [...itemList.value]

  // Determine Sort Order
  let sortOrderValues = [0, 0]
  if (sorts.currentSort?.value === SORT.ASCENDING) {
    sortOrderValues = [-1, 1]
  } else if (sorts.currentSort?.value === SORT.DESCENDING) {
    sortOrderValues = [1, -1]
  }

  // Sort the Items
  const sortedItemsTotal = myItems.sort((a, b) => {
    if (a[currentSortKey] > b[currentSortKey]) {
      return sortOrderValues[0]
    } else if (a[currentSortKey] < b[currentSortKey]) {
      return sortOrderValues[1]
    } else {
      return 0
    }
  })
  // Keep only the current page.
  return sortedItemsTotal?.slice(currentTablePage, currentTablePage + pageVals.value.pageSize)
})

/**
 * When the user changes which column is sorted.
 */
const sortChanged = (sortVal: ConfigurationTableSort) => {
  sorts.currentSort = sortVal
  sorts[sortVal.property] = sortVal.value
}

/**
 * When the user changes the page number.
 */
const pageUpdate = (newPage: number) => {
  pageVals.value.page = newPage
  if (props.setNewPage) {
    props.setNewPage(newPage)
  }
}

/**
 * When the user updates the page size.
 */
const pageSizeUpdate = (newPageSize: number) => {
  pageVals.value.pageSize = newPageSize
}

/**
 * Convert our Rescan Existing value to something more understandable by Humans.
 */
const rescanToEnglish = (rescanVal: string) => {
  return rescanCopy[rescanVal]
}
</script>
<style lang="scss">
@import "@featherds/table/scss/table";

table {
  @include table();
  @include table-condensed();
}
</style>
<style
  lang="scss"
  scoped
>
@import "@featherds/table/scss/table";
@import "@featherds/styles/themes/variables";

.main-wrapper {
  /* table {
    @include table();
    @include table-condensed();
  } */
  /* :deep(table) {
    &.condensed {
      .onms-sort-header {
        > .header-flex-container {
          justify-content: flex-start;
        }
      }
    }
  } */
  table.condensed {
    :deep(.onms-sort-header) {
      > .header-flex-container {
        justify-content: flex-start;
      }
    }
  }
}
.flex {
  display: flex;
}
.tr {
  background-color: var($background);
  .th {
    color: var($primary);
  }
}
.edit-icon {
  color: var($primary);
}
.delete-icon {
  color: var($error);
}
/* .condensed {
  @include table();
  @include table-condensed();
} */
.main-wrapper {
  padding: 16px 24px;
}
.cron {
  max-width: 260px;
}
</style>

