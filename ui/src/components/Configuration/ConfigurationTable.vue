<template>
  <div class="main-wrapper">
    <table class="condensed">
      <thead>
        <tr class="tr">
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            property="import-name"
            :sort="sorts['import-name']"
            v-on:sort-changed="sortChanged"
          >Name</FeatherSortHeader>
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            property="import-url-resource"
            :sort="sorts['import-url-resource']"
            v-on:sort-changed="sortChanged"
          >URL</FeatherSortHeader>
          <FeatherSortHeader
            scope="col"
            class="onms-sort-header"
            property="cron-schedule"
            :sort="sorts['cron-schedule']"
            v-on:sort-changed="sortChanged"
          >Schedule Frequency</FeatherSortHeader>
          <FeatherSortHeader
            scope="col"
            property="rescan-existing"
            class="onms-sort-header"
            :sort="sorts['rescan-existing']"
            v-on:sort-changed="sortChanged"
          >Rescan Behavior</FeatherSortHeader>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-bind:key="key" v-for="(item, key) in filteredItems">
          <td>
            <ConfigurationCopyPasteDisplay :text="item['import-name']" />
          </td>
          <td>
            <ConfigurationCopyPasteDisplay :text="item['import-url-resource']" />
          </td>
          <td>{{ cronToEnglish(item['cron-schedule']) }}</td>
          <td>{{ rescanToEnglish(item['rescan-existing']) }}</td>
          <td>
          <div class="flex">
            <FeatherButton icon="Edit" @click="() => props.editClicked(item.originalIndex)">
              <FeatherIcon :icon="editIcon" class="edit-icon"></FeatherIcon>
            </FeatherButton>
            <FeatherButton icon="Delete" @click="() => props.deleteClicked(item.originalIndex)">
              <FeatherIcon class="delete-icon" :icon="deleteIcon"></FeatherIcon>
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

<script setup lang="ts">
import { computed, ComputedRef, reactive, PropType } from 'vue'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'

import Edit from '@featherds/icon/action/Edit'
import Delete from '@featherds/icon/action/Delete'

import { ConfigurationService } from './ConfigurationService'
import ConfigurationCopyPasteDisplay from './ConfigurationCopyPasteDisplay.vue'

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
  ['import-name']: SORT.NONE,
  ['cron-schedule']: SORT.NONE,
  ['import-url-resource']: SORT.NONE,
  ['rescan-existing']: SORT.NONE,
  currentSort: { property: 'import-name', value: SORT.NONE },
  originalIndex:0
})

const itemList = computed(() => props.itemList)
const editIcon = computed(() => Edit)
const deleteIcon = computed(() => Delete)

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
  const currentSortKey = sorts.currentSort.property

  let myItems: Array<ProvisionDServerConfiguration> = [...itemList.value]
  
  // Determine Sort Order
  let sortOrderValues = [0, 0]
  if (sorts.currentSort.value === SORT.ASCENDING) {
    sortOrderValues = [-1, 1]
  } else if (sorts.currentSort.value === SORT.DESCENDING) {
    sortOrderValues = [1, -1]
  }
  
  // Sort the Items
  // TODO: Remove use of 'any' below. It kicks off a whole chain that I cant
  // get to the bottom of yet.
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
 * Convert our Cron Schedules to Human Readable String.
 */
const cronToEnglish = (cronFormatted: string) => {
  const converted = ConfigurationService.convertCronTabToLocal(cronFormatted)
  let occurance = 'Every day at '
  if (converted.occurance === 'Monthly') {
    occurance = 'Monthly at '
  } else if (converted.occurance === 'Weekly') {
    occurance = 'Weekly at '
  }
  return `${occurance}${converted.time}`
}

/**
 * Convert our Rescan Existing value to something more understandable by Humans.
 */
const rescanToEnglish = (rescanVal: string) => {
  if (rescanVal === 'true') {
    return 'Scan New Nodes'
  } else if (rescanVal === 'dbonly') {
    return 'Database Steps Only'
  } else {
    return 'No Scanning'
  }
}

</script>


<style lang="scss">
.main-wrapper {
  table.condensed {
    .onms-sort-header {
      > .header-flex-container {
        justify-content: flex-start;
      }
    }
  }
}
</style>
<style lang="scss" scoped>
@import "@featherds/table/scss/table";
.flex {
  display:flex;
}
.tr {
  background-color: #e6e8f9;
}
.edit-icon,
.delete-icon {
  color: #3a4bd3;
}
.condensed {
  @include table();
  @include table-condensed();
}
.main-wrapper {
  padding: 16px 24px;
}
</style>