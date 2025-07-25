<template>
  <div class="card">
    <div>
      <div class="feather-row title-bar">
        <span class="title">Structured Node List</span>
        <div class="action-buttons-container">
          <NodeDownloadDropdown
            :onCsvDownload="onCsvDownload"
            :onJsonDownload="onJsonDownload"
          />
          <FeatherButton
            secondary
            @click="() => nodeStructureStore.openColumnsDrawerModal()"
          >
            Customize
          </FeatherButton>
        </div>
      </div>
      <div class="spacer-large"></div>
      <div class="spacer-large"></div>
      <div class="search-container feather-col-12">
        <div class="feather-row">
          <div class="search-filter-column">
            <FeatherInput
              v-model="currentSearch"
              @update:modelValue="searchFilterHandler"
              label="Search node label"
            >
              <template #pre>
                <FeatherIcon :icon="icons.Search" />
              </template>
            </FeatherInput>
          </div>
          <div class="filter-icon-wrapper">
            <FeatherIcon
              :icon="FilterAlt"
              @click="() => nodeStructureStore.openInstancesDrawerModal()"
            />
          </div>
          <div class="chip-container">
            <FeatherChipList label="Tags">
              <FeatherChip
                v-for="cat in nodeStructureStore.selectedCategories"
                :key="cat._value as string"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeItem(cat, FilterTypeEnum.Category)"
                  />
                </template>
                {{ cat._text }}
              </FeatherChip>

              <FeatherChip
                v-for="flow in nodeStructureStore.selectedFlows"
                :key="flow._value as string"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeItem(flow, FilterTypeEnum.Flow)"
                  />
                </template>
                {{ flow._text }}
              </FeatherChip>

              <FeatherChip
                v-for="loc in nodeStructureStore.queryFilter.selectedMonitoringLocations"
                :key="loc.name"
              >
                <template #icon>
                  <FeatherIcon
                    :icon="cancelIcon"
                    class="icon"
                    @click="removeItem(loc, FilterTypeEnum.Location)"
                  />
                </template>
                {{ loc.name }}
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
          >
            <thead>
              <tr>
                <th v-if="canNavigateLeft">
                  <div @click="navigateColumns(Direction.Left)">
                    <FeatherIcon
                      :icon="ChevronLeft"
                      class="edit-icon"
                    />
                    <FeatherIcon
                      :icon="ChevronRight"
                      class="edit-icon"
                    />
                  </div>
                </th>

                <template
                  v-for="column in visibleColumns"
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
                    <FeatherIcon
                      :icon="ChevronLeft"
                      class="edit-icon"
                    />
                    <FeatherIcon
                      :icon="ChevronRight"
                      class="edit-icon"
                    />
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
                  v-for="column in visibleColumns"
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
                <td>
                  <FeatherButton @click="() => onNodeLinkClick(node.id)">
                    <FeatherIcon
                      :icon="Edit"
                      class="edit-icon"
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
        </div>
      </div>
    </div>
    <FeatherPagination
      v-model="pageNumber"
      :pageSize="queryParameters.limit"
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
import { markRaw, ref, reactive, computed, watch, nextTick } from 'vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Settings from '@featherds/icon/action/Settings'
import ChevronLeft from '@featherds/icon/navigation/ChevronLeft'
import ChevronRight from '@featherds/icon/navigation/ChevronRight'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import Edit from '@featherds/icon/action/Edit'
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

import FlowTooltipCell from './FlowTooltipCell.vue'
import ManagementIPTooltipCell from './ManagementIPTooltipCell.vue'
import NodeActionsDropdown from './NodeActionsDropdown.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import NodeDetailsDialog from './NodeDetailsDialog.vue'
import NodePreferencesDialog from './NodePreferencesDialog.vue'
import NodeTooltipCell from './NodeTooltipCell.vue'
import { useNodeQuery } from './hooks/useNodeQuery'
import { useNodeExport } from './hooks/useNodeExport'
import { getTableCssClasses } from './utils'
import Search from '@featherds/icon/action/Search'
import FilterAlt from '@featherds/icon/action/FilterAlt'
import Cancel from '@featherds/icon/navigation/Cancel'
import { FeatherChip, FeatherChipList } from '@featherds/chips'
import NodeAdvancedFiltersDrawer from './NodeAdvancedFiltersDrawer.vue'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import ColumnSelectionDrawer from './ColumnSelectionDrawer.vue'

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const nodeStore = useNodeStore()
const { showSnackBar } = useSnackbar()
const settingsIcon = markRaw(Settings)
const { generateBlob, generateDownload, getExportData } = useNodeExport()
const { buildUpdatedNodeStructureQueryParameters } = useNodeQuery()
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

const icons = markRaw({
  Search,
})
const cancelIcon = computed(() => Cancel)

const currentSearch = ref(nodeStructureStore.queryFilter.searchTerm || '')
const nodes = computed(() => nodeStore.nodes)
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const dialogVisible = ref(false)
const dialogNode = ref<Node>()
const preferencesVisible = ref(false)
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

const openPreferences = () => {
  preferencesVisible.value = true
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

const removeItem = (item: IAutocompleteItemType, type: FilterTypeEnum) => {
  switch (type) {
    case FilterTypeEnum.Category:
      nodeStructureStore.removeCategory(item);
      break;
    case FilterTypeEnum.Flow:
      nodeStructureStore.removeFlow(item);
      break;
    case FilterTypeEnum.Location:
      nodeStructureStore.removeLocation(item);
      break;
    default:
      console.warn(`Unknown filter type: ${type}`);
  }
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

#wrap {
  overflow: auto;
  white-space: nowrap;
}

.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
}

table {
  @include table;
  @include table-condensed;
  @include row-select();
  @include row-hover();
}

.title {
  @include headline1;
  display: block;
}

.feather-col-11.search-filter-column {
  padding-left: 1rem;
  max-width: 60% !important;
}

.action-buttons-column {
  text-align: left;
}

.search-filter-column {
  :deep(.feather-input-sub-text) {
    display: none !important;
  }

  .feather-input-container {
    width: 450px !important;
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

.filter-icon-wrapper {
  display: flex;
  align-items: center;
  margin-left: 10px;
  padding: 0 0.5rem;
  font-size: 1.5rem;
  cursor: pointer;
  border: 2px solid var($border-on-surface);
  color: var($primary);
  border-radius: 3px;
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
  display: inline-block;
  gap: 0.5rem;
  align-items: center;
}

.edit-icon {
  font-size: 20px;
}

.triple-icon {
  margin-left: 7px;
}

.navigation-cell {
  width: 10px;
}

.icon-container {
  display: flex;
}
</style>

