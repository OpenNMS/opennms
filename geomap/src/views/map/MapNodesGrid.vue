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
        rowSelection="single"
        :columnDefs="columnDefs"
        @grid-ready="onGridReady"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true"
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

const store = useStore();

const gridOptions = ref({})

const defaultColDef = ref({
  floatingFilter: true,
  resizable: true,
  enableBrowserTooltips: true,
  filter: "agTextColumnFilter",
  sortable: true,
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
    lable: node.label,
    lableSource: node.labelSource,
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

function onGridReady(params: any) {
  gridApi = params.api
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
  console.log("ids = " + ids)
  store.dispatch("mapModule/setInterestedNodesId", ids);
}

function reset() {
  store.dispatch("mapModule/resetInterestedNodesID");
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
    headerName: "LABLE",
    field: "lable",
    headerTooltip: "Lable",
  },
  {
    headerName: "LABLE SOURCE",
    field: "lableSource",
    headerTooltip: "Lable Source",
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
