<template>
  <div id="wrap">
    <table class="tl1 tl2 tl3" summary="Nodes">
      <thead>
        <tr>
          <SortableTh
            scope="col"
            property="id"
            :sort="sortStates.id"
            @sort-changed="sortChanged"
          >ID</SortableTh>

          <SortableTh
            scope="col"
            property="foreignSource"
            :sort="sortStates.foreignSource"
            @sort-changed="sortChanged"
          >FOREIGN SOURCE</SortableTh>

          <SortableTh
            scope="col"
            property="foreignId"
            :sort="sortStates.foreignId"
            @sort-changed="sortChanged"
          >FOREIGN ID</SortableTh>

          <SortableTh
            scope="col"
            property="label"
            :sort="sortStates.label"
            @sort-changed="sortChanged"
          >LABEL</SortableTh>

          <SortableTh
            scope="col"
            property="labelSource"
            :sort="sortStates.labelSource"
            @sort-changed="sortChanged"
          >LABEL SOURCE</SortableTh>

          <SortableTh
            scope="col"
            property="lastCapabilitiesScan"
            :sort="sortStates.lastCapabilitiesScan"
            @sort-changed="sortChanged"
          >LAST CAP SCAN</SortableTh>

          <SortableTh
            scope="col"
            property="primaryInterface"
            :sort="sortStates.primaryInterface"
            @sort-changed="sortChanged"
          >PRIMARY INTERFACE</SortableTh>

          <SortableTh
            scope="col"
            property="sysObjectId"
            :sort="sortStates.sysObjectId"
            @sort-changed="sortChanged"
          >SYSOBJECTID</SortableTh>

          <SortableTh
            scope="col"
            property="sysName"
            :sort="sortStates.sysName"
            @sort-changed="sortChanged"
          >SYSNAME</SortableTh>

          <SortableTh
            scope="col"
            property="sysDescription"
            :sort="sortStates.sysDescription"
            @sort-changed="sortChanged"
          >SYSDESCRIPTION</SortableTh>

          <SortableTh
            scope="col"
            property="sysContact"
            :sort="sortStates.sysContact"
            @sort-changed="sortChanged"
          >SYSCONTACT</SortableTh>

          <SortableTh
            scope="col"
            property="sysLocation"
            :sort="sortStates.sysLocation"
            @sort-changed="sortChanged"
          >SYSLOCATION</SortableTh>
        </tr>
      </thead>
      <tbody>
        <tr v-for="node in nodes" :key="node.id" @dblclick="doubleClickHandler(node)">
          <td class="first-td" :class="nodeLabelAlarmSeverityMap[node.label]">
            <a href="#" @click.prevent="onNodeIdClick(node.id)">{{ node.id }}</a>
          </td>
          <td>{{ node.foreignSource }}</td>
          <td>{{ node.foreignId }}</td>
          <td>
            <a href="#" @click.prevent="onNodeLabelClick(node.label)">{{ node.label }}</a>
          </td>
          <td>{{ node.labelSource }}</td>
          <td v-date>{{ node.lastCapabilitiesScan }}</td>
          <td>{{ node.primaryInterface }}</td>
          <td>{{ node.sysObjectId }}</td>
          <td>{{ node.sysName }}</td>
          <td>{{ node.sysDescription }}</td>
          <td>{{ node.sysContact }}</td>
          <td>{{ node.sysLocation }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive } from 'vue'

import { useMapStore } from '@/stores/mapStore'
import { Coordinates, Node, FeatherSortObject } from '@/types'
import { SORT } from '@/types'
import SortableTh from './SortableTh.vue'

const mapStore = useMapStore()
const nodes = computed<Node[]>(() => mapStore.getNodes())
const nodeLabelAlarmSeverityMap = computed(() => mapStore.getNodeAlarmSeverityMap())

const doubleClickHandler = (node: Node) => {
  const coordinate: Coordinates = { latitude: node.assetRecord.latitude, longitude: node.assetRecord.longitude }
  mapStore.setMapCenter(coordinate)
}

const sortStates: any = reactive({
  label: SORT.ASCENDING,
  id: SORT.NONE,
  foreignSource: SORT.NONE,
  foreignId: SORT.NONE,
  labelSource: SORT.NONE,
  lastCapabilitiesScan: SORT.NONE,
  primaryInterface: SORT.NONE,
  sysObjectId: SORT.NONE,
  sysName: SORT.NONE,
  sysDescription: SORT.NONE,
  sysContact: SORT.NONE,
  sysLocation: SORT.NONE
})

const sortChanged = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[`${sortObj.property}`] = sortObj.value
  mapStore.setNodeSortObject(sortObj)
}

const onNodeIdClick = (nodeId: string) => {
  const searchTerm = `nodeid == ${nodeId}`
  mapStore.setNodeSearchTerm(searchTerm)
}

const onNodeLabelClick = (label: string) => {
  mapStore.setNodeSearchTerm(label)
}
</script>

<style lang="scss" scoped>
@import "@/styles/onms-table";
@import "@featherds/styles/themes/variables";

#wrap {
  // Height accounts for the 15px top gap below so the pane keeps its footprint.
  height: calc(100% - 44px);
  overflow: auto;
  background: var(--p-content-background);
  // Gap above the table lives on the scroll container (outside the scrolled
  // content) so it doesn't make the sticky header travel before locking.
  margin-top: 15px;
}
table {
  @include onms-table;
  @include onms-table-condensed;
  background: var(--p-content-background);
  color: var(--p-text-color);
  // No top margin/padding: any space above thead inside the scroll container
  // makes the sticky header travel that distance before it locks at top:0.
}
// CSS sticky header (replaces a JS scroll-transform hack that jittered and let
// rows bleed above the header). Sticky lives on the cells (thead sticky has
// spotty support) and needs an opaque background so body rows don't show through.
thead th {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--p-content-background);
}
.first-td {
  padding-left: 12px;
  border-left: 4px solid var($success);
}
.WARNING,
.MINOR,
.MAJOR {
  border-left: 4px solid var($warning);
}

.CRITICAL {
  border-left: 4px solid var($error);
}
</style>
