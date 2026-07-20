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
import { computed, onMounted, reactive } from 'vue'

import { useMapStore } from '@/stores/mapStore'
import { Coordinates, Node, FeatherSortObject } from '@/types'
import { SORT } from '@featherds/table'
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

onMounted(() => {
  const wrap = document.getElementById('wrap')
  const thead = document.querySelector('thead')

  if (wrap && thead) {
    wrap.addEventListener('scroll', function () {
      const translate = `translate(0, ${this.scrollTop}px)`
      thead.style.transform = translate
    })
  }
})
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/themes/variables";

#wrap {
  height: calc(100% - 29px);
  overflow: auto;
  background: var(--p-content-background);
}
table {
  @include table;
  @include table-condensed;
  background: var(--p-content-background);
  color: var(--p-text-color);
  padding-top: 4px;
  margin-top: 15px;
}
thead {
  z-index: 2;
  position: relative;
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
