<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-8">
        <span class="title">Nodes</span>
      </div>
      <div class="feather-col-4">
        <div class="feather-row">
          <div class="feather-col-1">
            <FeatherButton
              icon="Open Preferences"
              @click="openPreferences"
            >
              <FeatherIcon :icon="settingsIcon" class="node-actions-icon" />
            </FeatherButton>
          </div>
          <div class="feather-col-11">
            <FeatherInput
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
                <template v-for="column in columns" :key="column.id">
                  <FeatherSortHeader
                    scope="col"
                    :property="column.id"
                    :sort="sortStateForId(column.id)"
                    v-on:sort-changed="sortChanged"
                    v-if="column.selected"
                  >{{ column.label }}</FeatherSortHeader>
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

                <template v-for="column in columns" :key="column.id">
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
    <Pagination
      :parameters="queryParameters"
      @update-query-parameters="updateQueryParameters"
      moduleName="nodesModule"
      functionName="getNodes"
      totalCountStateName="totalCount"
    />
  </div>
  <NodeDetailsDialog
    :computeNodeLink="computeNodeLink"
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
import { useStore } from 'vuex'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Settings from '@featherds/icon/action/Settings'
import { FeatherInput } from '@featherds/input'
import { FeatherSortHeader, SORT } from '@featherds/table'
import FlowTooltipCell from './FlowTooltipCell.vue'
import NodeActionsDropdown from './NodeActionsDropdown.vue'
import NodeDetailsDialog from './NodeDetailsDialog.vue'
import NodePreferencesDialog from './NodePreferencesDialog.vue'
import NodeTooltipCell from './NodeTooltipCell.vue'
import Pagination from '../Common/Pagination.vue'
import { buildNodeStructureQuery } from './utils'
import useQueryParameters from '@/composables/useQueryParams'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import {
  Category,
  FeatherSortObject,
  MonitoringLocation,
  Node,
  NodeColumnSelectionItem,
  QueryParameters,
  SetOperator,
  UpdateModelFunction } from '@/types'
import { MainMenu } from '@/types/mainMenu'

const store = useStore()
const nodeStructureStore = useNodeStructureStore()
const menuStore = useMenuStore()
const settingsIcon = markRaw(Settings)

const sortStates: any = reactive({
  id: SORT.NONE,
  label: SORT.ASCENDING,
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

const currentSearch = ref('')
const nodes = computed(() => store.state.nodesModule.nodes)
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const selectedCategories = computed<Category[]>(() => nodeStructureStore.selectedCategories)
const selectedFlows = computed<string[]>(() => nodeStructureStore.selectedFlows)
const selectedLocations = computed<MonitoringLocation[]>(() => nodeStructureStore.selectedMonitoringLocations)
const categoryMode = computed<SetOperator>(() => nodeStructureStore.categoryMode)
const dialogVisible = ref(false)
const dialogNode = ref<Node>()
const preferencesVisible = ref(false)
const columns = computed<NodeColumnSelectionItem[]>(() => nodeStructureStore.columns)

const tableCssClasses = computed<string[]>(() => {
  const classes: string[] = columns.value.filter(col => col.selected).map((col, i) => {
    let t = 'tl'

    if (col.id === 'id') {
      t = 'tr'
    } else if (col.id === 'flows') {
      t = 'tc'
    }

    // +2 : one since Feather table column classes are 1-based, one for the first action column which isn't in 'columns'
    return `${t}${i + 2}`
  })

  // add 'action' column
  return ['tl1', ...classes]
})

const isSelectedColumn = (column: NodeColumnSelectionItem, id: string) => {
  return column.selected && column.id === id
}

const { queryParameters, updateQueryParameters, sort } = useQueryParameters({
  limit: 10,
  offset: 0,
  orderBy: 'label'
}, 'nodesModule/getNodes')

const sortChanged = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }
  sortStates[`${sortObj.property}`] = sortObj.value
  sort(sortObj)
}

const buildQueryParams = (searchVal: string) => {
  const params = {
    searchVal,
    selectedCategories: selectedCategories.value,
    categoryMode: categoryMode.value,
    selectedFlows: selectedFlows.value,
    selectedLocations: selectedLocations.value
  }

  return buildNodeStructureQuery(params)
}

const searchFilterHandler: UpdateModelFunction = (val = '') => {
  currentSearch.value = val
  updateQuery(val)
}

const onNodeInfo = (node: Node) => {
  dialogNode.value = node
  dialogVisible.value = true
}

const openPreferences = () => {
  preferencesVisible.value = true
}

const updateQuery = (val?: string) => {
  const searchQuery = buildQueryParams(val || currentSearch.value)
  const searchQueryParam: QueryParameters = { _s: searchQuery }
  const updatedParams = { ...queryParameters.value, ...searchQueryParam }
  // if there is no search query, remove the '_s' property entirely so it doesn't
  // get put into the API request query string
  if (!searchQuery) {
    delete updatedParams._s
  }

  store.dispatch('nodesModule/getNodes', updatedParams)
  queryParameters.value = updatedParams
}

const computeNodeLink = (nodeId: number | string) => {
  return `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}${nodeId}`
}

const onNodeLinkClick = (nodeId: number) => {
  window.location.assign(computeNodeLink(nodeId))
}

watch([categoryMode, selectedCategories, selectedFlows, selectedLocations], () => {
  updateQuery()
})
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

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
</style>
