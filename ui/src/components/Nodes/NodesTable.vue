<template>
  <div class="card">
    <div>
      <div class="feather-row title-bar">
        <span class="title">Node List</span>
        <div class="action-buttons-container">
          <NodeDownloadDropdown
            :onCsvDownload="onCsvDownload"
            :onJsonDownload="onJsonDownload"
          />
          <FeatherButton
            primary
            @click="() => nodeStructureStore.openColumnsDrawerModal()"
          >
            Customize Columns
          </FeatherButton>
          <FeatherButton
            secondary
            @click="() => nodeStructureStore.clearAllFiltersAndSelections()"
          >
            Clear Filters
          </FeatherButton>
        </div>
      </div>
      <div class="spacer-large"></div>
      <div class="spacer-large"></div>
      <div class="search-container feather-col-12">
        <div class="feather-row">
          <div class="filter">
            <div class="search-filter-column">
              <FeatherInput
                v-model="currentSearch"
                @update:modelValue="searchFilterHandler"
                label="Search node label or full IP address"
              >
                <template #pre>
                  <FeatherIcon :icon="Search" />
                </template>
              </FeatherInput>
            </div>
            <div>
              <FeatherButton
                icon="FilterAlt"
                @click="() => nodeStructureStore.openInstancesDrawerModal()"
              >
                <FeatherIcon :icon="FilterAlt" />
              </FeatherButton>
            </div>
          </div>
          <div class="chip-container">
            <FeatherChipList label="SearchParams">
              <FeatherChip
                v-for="(cat, index) in nodeStructureStore.selectedCategories"
                :key="`cat-${index}`"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeItem(cat, FilterTypeEnum.Category)"
                  />
                </template>
                {{ `Category: ${cat._text}` }}
              </FeatherChip>

              <FeatherChip
                v-for="(flow, index) in nodeStructureStore.selectedFlows"
                :key="`flow-${index}`"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeItem(flow, FilterTypeEnum.Flow)"
                  />
                </template>
                {{ `Flow: ${flow._text}` }}
              </FeatherChip>

              <FeatherChip
                v-for="loc in nodeStructureStore.queryFilter.selectedMonitoringLocations"
                :key="loc.name"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeItem(loc, FilterTypeEnum.MonitoringLocation)"
                  />
                </template>
                {{ `Location: ${loc.name}` }}
              </FeatherChip>

              <FeatherChip
                v-if="hasExtendedSearchParams"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeExtendedSearchItem"
                  />
                </template>
                {{ 'Extended Search' }}
              </FeatherChip>
            </FeatherChipList>
          </div>
        </div>
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <div
          id="wrap"
          class="node-table"
        >
          <table
            :class="tableCssClasses"
            summary="Nodes"
            v-if="nodes.length > 0"
          >
            <thead>
              <tr>
                <th
                  v-if="canNavigateLeft"
                  class="navigation-cell"
                >
                  <div @click="navigateColumns(Direction.Left)">
                    <FeatherButton icon="Shift Left">
                      <FeatherIcon
                        :icon="ChevronLeft"
                        class="navigation-icon"
                      />
                    </FeatherButton>
                  </div>
                </th>

                <template
                  v-for="column in visibleColumns.sort((a: NodeColumnSelectionItem, b: NodeColumnSelectionItem) => a.order - b.order)"
                  :key="column.id"
                >
                  <FeatherSortHeader
                    v-if="column.id !== 'ipaddress'"
                    scope="col"
                    :property="column.id"
                    :sort="sortStateForId(column.id)"
                    @sort-changed="sortChanged"
                  >
                    {{ column.label }}
                  </FeatherSortHeader>
                  <th v-else>{{ column.label }}</th>
                </template>
                <th
                  v-if="canNavigateRight"
                  class="navigation-cell"
                >
                  <div
                    class="icon-container"
                    @click="navigateColumns(Direction.Right)"
                  >
                    <FeatherButton icon="Shift Right">
                      <FeatherIcon
                        :icon="ChevronRight"
                        class="navigation-icon"
                      />
                    </FeatherButton>
                  </div>
                </th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="node in nodes"
                :key="node.id"
              >
                <td
                  v-if="canNavigateLeft"
                  class="navigation-cell"
                ></td>
                <template
                  v-for="column in visibleColumns.sort((a: NodeColumnSelectionItem, b: NodeColumnSelectionItem) => a.order - b.order)"
                  :key="column.id"
                >
                  <td v-if="isSelectedColumn(column, 'id')">
                    <a
                      :href="computeNodeLink(node.id)"
                      @click="onNodeLinkClick(node.id)"
                      target="_blank"
                    >
                      {{ node.id }}
                    </a>
                  </td>
                  <td v-if="isSelectedColumn(column, 'label')">
                    <a
                      :href="computeNodeLink(node.id)"
                      @click="onNodeLinkClick(node.id)"
                      target="_blank"
                    >
                      {{ node.label }}
                    </a>
                  </td>

                  <ManagementIPTooltipCell
                    v-if="isSelectedColumn(column, 'ipaddress')"
                    :computeNodeIpInterfaceLink="computeNodeIpInterfaceLink"
                    :node="node"
                    :nodeToIpInterfaceMap="nodeStore.nodeToIpInterfaceMap"
                  />

                  <td v-if="isSelectedColumn(column, 'location')">{{ node.location }}</td>

                  <NodeTooltipCell
                    v-if="isSelectedColumn(column, 'foreignSource')"
                    :text="node.foreignSource"
                  />
                  <NodeTooltipCell
                    v-if="isSelectedColumn(column, 'foreignId')"
                    :text="node.foreignId"
                  />
                  <NodeTooltipCell
                    v-if="isSelectedColumn(column, 'sysContact')"
                    :text="node.sysContact"
                  />
                  <NodeTooltipCell
                    v-if="isSelectedColumn(column, 'sysLocation')"
                    :text="node.sysLocation"
                  />
                  <NodeTooltipCell
                    v-if="isSelectedColumn(column, 'sysDescription')"
                    :text="node.sysDescription"
                  />

                  <td v-if="isSelectedColumn(column, 'flows')">
                    <FlowTooltipCell :node="node" />
                  </td>
                </template>

                <td
                  v-if="canNavigateRight"
                  class="navigation-cell"
                ></td>
                <td class="actions-cell">
                  <FeatherButton
                    icon="Edit"
                    class="edit-icon"
                    @click="() => onNodeLinkClick(node.id)"
                  >
                    <FeatherIcon
                      :icon="Edit"
                      title="Edit"
                    />
                  </FeatherButton>

                  <NodeActionsDropdown
                    :baseHref="mainMenu.baseHref"
                    :node="node"
                    :triggerNodeInfo="onNodeInfo"
                    class="triple-icon"
                  />
                </td>
              </tr>
            </tbody>
          </table>
          <EmptyList
            v-else
            :content="emptyListContent"
            data-test="empty-list"
          />
        </div>
      </div>
    </div>
    <FeatherPagination
      v-if="nodeStore.totalCount > 0"
      v-model="pageNumber"
      :pageSizes="[10, 20, 50, 100, 200]"
      :total="nodeStore.totalCount"
      @update:modelValue="updatePageNumber"
      @update:pageSize="updatePageSize"
    />
  </div>
  <NodeDetailsDialog
    :computeNodeLink="computeNodeLink"
    :computeNodeIpInterfaceLink="computeNodeIpInterfaceLink"
    @close="dialogVisible = false"
    :visible="dialogVisible"
    :node="dialogNode"
  >
  </NodeDetailsDialog>
  <NodeAdvancedFiltersDrawer />
  <ColumnSelectionDrawer />
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import {
  Direction,
  FeatherSortObject,
  FilterTypeEnum,
  Node,
  NodeColumnSelectionItem,
  QueryParameters,
  UpdateModelFunction
} from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherChip, FeatherChipList } from '@featherds/chips'
import { FeatherIcon } from '@featherds/icon'
import Edit from '@featherds/icon/action/Edit'
import FilterAlt from '@featherds/icon/action/FilterAlt'
import Search from '@featherds/icon/action/Search'
import Cancel from '@featherds/icon/navigation/Cancel'
import ChevronLeft from '@featherds/icon/navigation/ChevronLeft'
import ChevronRight from '@featherds/icon/navigation/ChevronRight'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { computed, nextTick, reactive, ref, watch } from 'vue'
import ColumnSelectionDrawer from './ColumnSelectionDrawer.vue'
import FlowTooltipCell from './FlowTooltipCell.vue'
import ManagementIPTooltipCell from './ManagementIPTooltipCell.vue'
import NodeActionsDropdown from './NodeActionsDropdown.vue'
import NodeAdvancedFiltersDrawer from './NodeAdvancedFiltersDrawer.vue'
import NodeDetailsDialog from './NodeDetailsDialog.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import NodeTooltipCell from './NodeTooltipCell.vue'
import { useNodeExport } from './hooks/useNodeExport'
import { useNodeQuery } from './hooks/useNodeQuery'
import { getTableCssClasses } from './utils'
import EmptyList from '../Common/EmptyList.vue'

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const nodeStore = useNodeStore()
const { showSnackBar } = useSnackbar()
const { generateBlob, generateDownload, getExportData } = useNodeExport()
const { buildUpdatedNodeStructureQueryParameters, hasAnyExtendedSearchValues } = useNodeQuery()
const visibleColumnStart = ref(0)
const visibleColumnsCount = 5

const visibleColumns = computed(() => {
  return nodeStructureStore.columns
    .filter(col => col.selected)
    .slice(visibleColumnStart.value, visibleColumnStart.value + visibleColumnsCount)
})

const canNavigateLeft = computed(() => visibleColumnStart.value > 0)
const canNavigateRight = computed(() =>
  visibleColumnStart.value + visibleColumnsCount <
  nodeStructureStore.columns.filter(col => col.selected).length
)

const navigateColumns = (direction: Direction) => {
  if (direction === Direction.Left && canNavigateLeft.value) {
    visibleColumnStart.value -= visibleColumnsCount
  } else if (direction === Direction.Right && canNavigateRight.value) {
    visibleColumnStart.value += visibleColumnsCount
  }
}

const sortStates: any = reactive({
  id: SORT.NONE,
  label: SORT.ASCENDING,
  ipaddress: SORT.NONE, // note, cannot sort by this at the moment
  location: SORT.NONE,
  foreignSource: SORT.NONE,
  foreignId: SORT.NONE,
  sysContact: SORT.NONE,
  sysLocation: SORT.NONE,
  sysDescription: SORT.NONE,
  flows: SORT.NONE
})

const sortStateForId = (label: string) => {
  switch (label) {
    case 'id': return sortStates.id
    case 'label': return sortStates.label
    case 'ipaddress': return sortStates.ipaddress
    case 'location': return sortStates.location
    case 'foreignSource': return sortStates.foreignSource
    case 'foreignId': return sortStates.foreignId
    case 'sysContact': return sortStates.sysContact
    case 'sysLocation': return sortStates.sysLocation
    case 'sysDescription': return sortStates.sysDescription
    case 'flows': return sortStates.flows
  }

  return SORT.NONE
}

const cancelIcon = computed(() => Cancel)

const currentSearch = ref(nodeStructureStore.queryFilter.searchTerm || '')
const nodes = computed(() => nodeStore.nodes)
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const dialogVisible = ref(false)
const dialogNode = ref<Node>()
const tableCssClasses = computed<string[]>(() => getTableCssClasses(nodeStructureStore.columns))
const queryParameters = ref<QueryParameters>(nodeStore.nodeQueryParameters)
const pageNumber = ref(1)

const isSelectedColumn = (column: NodeColumnSelectionItem, id: string) => {
  return column.selected && column.id === id
}

const updatePageNumber = (page: number) => {
  pageNumber.value = page
  const pageSize = queryParameters.value.limit || 0
  queryParameters.value = { ...queryParameters.value, offset: Math.max((page - 1) * pageSize, 0) }
  nodeStore.setNodeQueryParameters(queryParameters.value)

  updateQuery()
}

const updatePageSize = (size: number) => {
  queryParameters.value = { ...queryParameters.value, limit: size }
  nodeStore.setNodeQueryParameters(queryParameters.value)

  updateQuery()
}

const sortChanged = (sortObj: FeatherSortObject) => {
  if (sortObj.property === 'ipaddress') {
    return
  }

  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[`${sortObj.property}`] = sortObj.value

  queryParameters.value = {
    ...queryParameters.value,
    orderBy: sortObj.property,
    order: sortObj.value
  }

  updateQuery({ orderBy: sortObj.property, order: sortObj.value })
}

const searchFilterHandler: UpdateModelFunction = (val = '') => {
  if (val !== nodeStructureStore.queryFilter.searchTerm) {
    nodeStructureStore.setSearchTerm(val)
  }
}

const onDownload = async (format: string) => {
  const updatedParams = buildUpdatedNodeStructureQueryParameters(queryParameters.value, nodeStructureStore.queryFilter)
  const data = await getExportData(format, updatedParams, nodeStructureStore.columns)

  if (!data) {
    showSnackBar({
      msg: `No data found for '${format}' download with the given search and filter configuration`,
      error: true
    })

    return
  }

  const contentType = format === 'json' ? 'application/json' : format === 'csv' ? 'text/csv' : ''

  const blob = generateBlob(data, contentType)
  generateDownload(blob, `Nodes.${format}`)
}

const onCsvDownload = async () => { return onDownload('csv') }
const onJsonDownload = async () => { return onDownload('json') }

const onNodeInfo = (node: Node) => {
  dialogNode.value = node
  dialogVisible.value = true
}

const computeNodeLink = (nodeId: number | string) => {
  return `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}${nodeId}`
}

const computeNodeIpInterfaceLink = (nodeId: number | string, ipAddress: string) => {
  return `${mainMenu.value.baseHref}element/interface.jsp?node=${nodeId}&intf=${ipAddress}`
}

const onNodeLinkClick = (nodeId: number | string) => {
  window.location.assign(computeNodeLink(nodeId))
}

const hasExtendedSearchParams = computed(() => {
  return hasAnyExtendedSearchValues(nodeStructureStore.queryFilter.extendedSearch)
})

const removeItem = (item: IAutocompleteItemType, type: FilterTypeEnum) => {
  switch (type) {
    case FilterTypeEnum.Category:
      nodeStructureStore.removeCategory(item)
      break
    case FilterTypeEnum.Flow:
      nodeStructureStore.removeFlow(item)
      break
    case FilterTypeEnum.MonitoringLocation:
      nodeStructureStore.removeMonitoringLocation(item)
      break
    default:
      console.warn(`Unknown filter type: ${type}`)
  }
}

const removeExtendedSearchItem = () => {
  nodeStructureStore.removeExtendedSearch()
}

const updateQuery = (options?: { orderBy?: string, order?: SORT }) => {
  // make sure anything setting nodeStore.nodeQueryParameters has been processed
  nextTick()

  const queryParamsToUse =
    options?.orderBy ?
      {
        ...nodeStore.nodeQueryParameters,
        orderBy: options.orderBy,
        order: options.order || SORT.ASCENDING
      }
      : nodeStore.nodeQueryParameters

  const updatedParams = buildUpdatedNodeStructureQueryParameters(queryParamsToUse, nodeStructureStore.queryFilter)
  queryParameters.value = updatedParams

  nodeStore.getNodes(updatedParams, true)
}

const emptyListContent = {
  msg: 'No results found.'
}

watch([() => nodeStructureStore.queryFilter], () => {
  if (nodeStructureStore.queryFilter.searchTerm !== currentSearch.value) {
    currentSearch.value = nodeStructureStore.queryFilter.searchTerm
  }

  updateQuery()
},
{ deep: true }
)
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.node-table {
  margin-top: 1rem;
}

#wrap {
  overflow: auto;
  white-space: nowrap;
}

.card {
  @include elevation(2);
  background: var($surface);
  padding: 30px;
}

table {
  @include table;
  @include table-condensed;
  @include row-select();
  @include row-hover();

  tbody {
    tr {
      td {
        padding: 12px 1rem;
      }
    }
  }
}

.title {
  @include headline1;
  display: block;
}

.action-buttons-column {
  text-align: left;
}

.filter {
  display: flex;
  align-items: center;
  gap: 10px;

  .search-filter-column {
    :deep(.feather-input-sub-text) {
      display: none !important;
    }

    .feather-input-container {
      width: 450px !important;
    }
  }

  .btn.btn-icon{
    border: 2px solid var($border-on-surface);
    border-radius: 3px;
    padding: 0 0.5rem;
    height: 3rem;
    width: 3rem;
  }
}

.chip-container {
  padding-left: 10px;

  :deep(.chip) {
    margin-bottom: 0 !important;
  }

  :deep(.chip-list) {
    margin-top: 0.25rem !important;
  }
}

.spacer-large {
  margin-bottom: 2rem;
}

.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 1rem;
  padding-left: 1rem;
}

.action-buttons-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.actions-cell {
  .edit-icon {
    svg {
      font-size: 1rem !important;
    }
  }
}

.triple-icon {
  margin-left: 7px;
}

.navigation-cell {
  width: 10px;

  .btn.btn-icon-table {
    width: 2.25rem;
    height: 2.25rem;
    border-radius: 100%;
  }
}
</style>

