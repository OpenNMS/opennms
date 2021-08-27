<template>
  <div class="mapnodes">
    <div class="map-nodes-grid">
      <ag-grid-vue
        style="width: 100%; height: 600px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true"
      >
      </ag-grid-vue>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";




const gridOptions = ref({})

const defaultColDef = ref({
      floatingFilter: true,
      resizable: true,
      enableBrowserTooltips: true,
      filter: "agTextColumnFilter",
    })

const columnDefs = ref([
      {
        headerName: "ID",
        field: "id",
        sortable: true,
        headerTooltip: "ID",
        filter: "agNumberColumnFilter",
        comparator: (valueA, valueB) => {
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
const rowData = ref([
  { id: 1, foreignSource: "fs1" },
  { id: 2, foreignSource: "fs2" },
]);
</script>
<style scoped></style>
