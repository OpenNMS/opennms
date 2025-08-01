<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-6">
        <span class="title">Nodes</span>
      </div>
      <div class="feather-col-6">
        <div class="feather-row">
          <div class="feather-col-3 action-buttons-column">
            <div class="action-buttons-container">
              <NodeDownloadDropdown
                :onCsvDownload="onCsvDownload"
                :onJsonDownload="onJsonDownload"
              ></NodeDownloadDropdown>
              <FeatherButton
                icon="Open Preferences"
                @click="openPreferences"
              >
                <FeatherIcon :icon="settingsIcon" class="node-actions-icon" />
              </FeatherButton>
            </div>
          </div>
          <div class="feather-col-9 search-filter-column">
            <FeatherInput
              v-model="currentSearch"
              @update:modelValue="searchFilterHandler"
              label="Search node label"
            />
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
                <th scope="column" />
                <template v-for="column in nodeStructureStore.columns" :key="column.id">
                  <FeatherSortHeader
                    scope="col"
                    :property="column.id"
                    :sort="sortStateForId(column.id)"
                    v-on:sort-changed="sortChanged"
                    v-if="column.selected && column.id !== 'ipaddress'"
                  >{{ column.label }}</FeatherSortHeader>

                  <th v-if="column.selected && column.id === 'ipaddress'">{{ column.label }}</th>
                </template>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="node in nodes"
                :key="node.id"
              >
                <td>
                  <NodeActionsDropdown
                    :baseHref="mainMenu.baseHref"
                    :node="node"
                    :triggerNodeInfo="onNodeInfo"
                  />
                </td>

                <template v-for="column in nodeStructureStore.columns" :key="column.id">
                  <td v-if="isSelectedColumn(column, 'id')">
                    <a
                      :href="computeNodeLink(node.id)"
                      @click="onNodeLinkClick(node.id)"
                      target="_blank">
                      {{ node.id }}
                    </a>
                  </td>
                  <td v-if="isSelectedColumn(column, 'label')">
                    <a
                      :href="computeNodeLink(node.id)"
                      @click="onNodeLinkClick(node.id)"
                      target="_blank">
                      {{ node.label }}
                    </a>
                  </td>

                  <ManagementIPTooltipCell v-if="isSelectedColumn(column, 'ipaddress')"
                    :computeNodeIpInterfaceLink="computeNodeIpInterfaceLink"
                    :node="node"
                    :nodeToIpInterfaceMap="nodeStore.nodeToIpInterfaceMap"
                  />

                  <td v-if="isSelectedColumn(column, 'location')">{{ node.location }}</td>

                  <NodeTooltipCell v-if="isSelectedColumn(column, 'foreignSource')" :text="node.foreignSource" />
                  <NodeTooltipCell v-if="isSelectedColumn(column, 'foreignId')" :text="node.foreignId" />
                  <NodeTooltipCell v-if="isSelectedColumn(column, 'sysContact')" :text="node.sysContact" />
                  <NodeTooltipCell v-if="isSelectedColumn(column, 'sysLocation')" :text="node.sysLocation" />
                  <NodeTooltipCell v-if="isSelectedColumn(column, 'sysDescription')" :text="node.sysDescription" />

                  <td v-if="isSelectedColumn(column, 'flows')">
                    <FlowTooltipCell :node="node" />
                  </td>
                </template>
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
    :node="dialogNode">
  </NodeDetailsDialog>

  <NodePreferencesDialog
    @close="preferencesVisible = false"
    :visible="preferencesVisible">
  </NodePreferencesDialog>
</template>

<script setup lang="ts">
import { markRaw } from 'vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Settings from '@featherds/icon/action/Settings'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'

import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import {
  FeatherSortObject,
  Node,
  NodeColumnSelectionItem,
  QueryParameters,
  UpdateModelFunction } from '@/types'
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

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const nodeStore = useNodeStore()
const { showSnackBar } = useSnackbar()
const settingsIcon = markRaw(Settings)

const { generateBlob, generateDownload, getExportData } = useNodeExport()
const { buildUpdatedNodeStructureQueryParameters } = useNodeQuery()

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
  // currently we don't support sorting by ipaddress
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
})
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
}

.action-buttons-column {
  text-align: right;
}

.search-filter-column {
}

.action-buttons-container {
  display: inline-block;
}
</style>
