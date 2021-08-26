<template>
  <div class="mapnodes">
    <h4>This is the nodes page</h4>

    <div class="map-nodes-grid">
      <ag-grid-vue
        style="width: 100%; height: 600px"
        class="ag-theme-alpine"
        :columnDefs="columnDefs"
        :rowData="rowData"
      >
      </ag-grid-vue>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from "vue";
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";
const columnDefs = ref([
  { headerName: "ID", field: "id" },
  { headerName: "FOREIGN SOURCE", field: "foreignSource" },
]);
const rowData = ref([
  { id: 1, foreignSource: "fs1" },
  { id: 2, foreignSource: "fs2" },
]);
</script>
<style scoped></style>
