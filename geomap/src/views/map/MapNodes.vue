<template>
  <div class="mapnodes">
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
})

const interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

const rowData = ref(getGridRowDataFromInterestedNodes());

function getGridRowDataFromInterestedNodes() {
  return store.getters['mapModule/getInterestedNodes'].map((node: any) => ({
    id: node.id,
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

function onGridReady(params) {
  gridApi = params.api
}

watch(
  () => interestedNodesID.value,
  (newValue, oldValue) => {
    console.log("I'm changed from " + oldValue + " to " + newValue)
    gridApi.setRowData(
      getGridRowDataFromInterestedNodes()
    );
  }
)

const columnDefs = ref([
  {
    headerName: "ID",
    field: "id",
    sortable: true,
    headerTooltip: "ID",
    filter: "agNumberColumnFilter",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "FOREIGN SOURCE",
    field: "foreignSource",
    sortable: true,
    headerTooltip: "Foreign Source",
  },
  {
    headerName: "FOREIGN ID",
    field: "foreignId",
    sortable: true,
    headerTooltip: "Foreign ID",
  },
  {
    headerName: "LABLE",
    field: "lable",
    sortable: true,
    headerTooltip: "Lable",
  },
  {
    headerName: "LABLE SOURCE",
    field: "lableSource",
    sortable: true,
    headerTooltip: "Lable Source",
  },
  {
    headerName: "LAST CAPABILITIES SCAN",
    field: "lastCapabilitiesScan",
    sortable: true,
    headerTooltip: "Last Capabilities Scan",
    filter: "agDateColumnFilter",
    cellRenderer: (data) => {
      return data.value ? new Date(data.value).toLocaleDateString() : "";
    },
  },
  {
    headerName: "PRIMARY INTERFACE",
    field: "primaryInterface",
    sortable: true,
    headerTooltip: "Primary Interface",
  },
  {
    headerName: "SYSOBJECTID",
    field: "sysObjectid",
    sortable: true,
    headerTooltip: "Sys Object ID",
  },
  {
    headerName: "SYSNAME",
    field: "sysName",
    sortable: true,
    headerTooltip: "Sys Name",
  },
  {
    headerName: "SYSDESCRIPTION",
    field: "sysDescription",
    sortable: true,
    headerTooltip: "Sys Description",
  },
  {
    headerName: "SYSCONTACT",
    field: "sysContact",
    sortable: true,
    headerTooltip: "Sys Contact",
  },
  {
    headerName: "SYSLOCATION",
    field: "sysLocation",
    sortable: true,
    headerTooltip: "Sys Location",
  },
]);
</script>
<style scoped>
</style>
