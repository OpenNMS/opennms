<template>
  <div class="mapnodes">
    <div class="button-group">
      <span class="buttons">
        <button v-on:click="clearFilters()">Clear Filters</button>
        <button v-on:click="confirmFilters()">Apply filter</button>
        <button v-on:click="reset()">Reset</button>
      </span>
    </div>
    <div class="map-nodes-grid">
      <ag-grid-vue
        style="width: 100%; height: 600px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        :columnDefs="columnDefs"
        @grid-ready="onGridReady"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true"
        @rowDoubleClicked="rowDoubleClicked"
      ></ag-grid-vue>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";
import { useStore } from "vuex";
import { computed, watch } from 'vue'
import { Coordinates } from "@/types";

const store = useStore();

const gridOptions = ref({})

const defaultColDef = ref({
  floatingFilter: true,
  resizable: true,
  enableBrowserTooltips: true,
  filter: "agTextColumnFilter",
  sortable: true,
  suppressMenu: true
})

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

let rowData = ref(getGridRowDataFromInterestedNodes());

function getGridRowDataFromInterestedNodes() {
  return store.getters['mapModule/getInterestedNodes'].map((node: any) => ({
    id: +node.id,
    foreignSource: node.foreignSource,
    foreignId: node.foreignId,
    label: node.label,
    labelSource: node.labelSource,
    lastCapabilitiesScan: node.lastCapsdPoll,
    primaryInterface: node.primaryInterface,
    sysObjectid: node.sysObjectId,
    sysName: node.sysName,
    sysDescription: node.sysDescription,
    sysContact: node.sysContact,
    sysLocation: node.sysLocation
  }));
}

let gridApi = ref({});
let gridColumnApi = ref({});

function onGridReady(params: any) {
  gridApi = params.api
  gridColumnApi = params.columnApi
  autoSizeAll(false);
}

function autoSizeAll(skipHeader: boolean) {
  var allColumnIds: string[] = [];
  gridColumnApi.getAllColumns().forEach(function (column) {
    allColumnIds.push(column.colId);
  });
  gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
}

watch(
  () => interestedNodesID.value,
  () => {
    gridApi.setRowData(
      getGridRowDataFromInterestedNodes()
    );
  }
)

function clearFilters() {
  gridApi.setFilterModel(null);
}

function confirmFilters() {
  let ids: string[] = [];
  gridApi.forEachNodeAfterFilter((node: any) => ids.push(node.data.id.toString()));
  store.dispatch("mapModule/setInterestedNodesId", ids);
}

function reset() {
  store.dispatch("mapModule/resetInterestedNodesID");
}

function rowDoubleClicked() {
  const id = gridApi.getSelectedNodes().map(node => node.data)[0].id;
  const node = store.getters['mapModule/getInterestedNodes'].filter((n: Node) => n.id == id);

  let coordinate: Coordinates;
  coordinate = { latitude: node[0].assetRecord.latitude, longitude: node[0].assetRecord.longitude }
  store.dispatch("mapModule/setMapCenter", coordinate)
}

const columnDefs = ref([
  {
    headerName: "ID",
    field: "id",
    headerTooltip: "ID",
    filter: "agNumberColumnFilter",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "FOREIGN SOURCE",
    field: "foreignSource",
    headerTooltip: "Foreign Source",
  },
  {
    headerName: "FOREIGN ID",
    field: "foreignId",
    headerTooltip: "Foreign ID",
  },
  {
    headerName: "LABEL",
    field: "label",
    headerTooltip: "Label",
    sort: "asc"
  },
  {
    headerName: "LABEL SOURCE",
    field: "labelSource",
    headerTooltip: "Label Source",
  },
  {
    headerName: "LAST CAPABILITIES SCAN",
    field: "lastCapabilitiesScan",
    headerTooltip: "Last Capabilities Scan",
    filter: "agDateColumnFilter",
    cellRenderer: (data) => {
      return data.value ? new Date(data.value).toLocaleDateString() : "";
    },
  },
  {
    headerName: "PRIMARY INTERFACE",
    field: "primaryInterface",
    headerTooltip: "Primary Interface",
  },
  {
    headerName: "SYSOBJECTID",
    field: "sysObjectid",
    headerTooltip: "Sys Object ID",
  },
  {
    headerName: "SYSNAME",
    field: "sysName",
    headerTooltip: "Sys Name",
  },
  {
    headerName: "SYSDESCRIPTION",
    field: "sysDescription",
    headerTooltip: "Sys Description",
  },
  {
    headerName: "SYSCONTACT",
    field: "sysContact",
    headerTooltip: "Sys Contact",
  },
  {
    headerName: "SYSLOCATION",
    field: "sysLocation",
    headerTooltip: "Sys Location",
  },
]);
</script>
<style scoped>
.button-group {
  width: 100%;
  height: 25px;
}
.map-nodes-grid {
  width: 100%;
  height: 700px;
}
.buttons {
  float: right;
}
button {
  margin-right: 5px;
}
</style>
