<template>
  <div class="map-alarms">
    <div class="button-group">
      <span class="map-alarm-buttons">
        <select name="subject" id="subject">
          <option value="Acknowledge" selected="selected">Acknowledge</option>
          <option value="Unacknowledge">Unacknowledge</option>
          <option value="Escalate">Escalate</option>
          <option value="Clear">Clear</option>
        </select>
        <button v-on:click="submit()">Submit</button>
        <button v-on:click="clearFilters()">Clear Filters</button>
        <button v-on:click="confirmFilters()">Apply filter</button>
        <button v-on:click="reset()">Reset</button>
      </span>
    </div>
    <div class="map-alarms-grid">
      <ag-grid-vue
        style="width: 100%; height: 700px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        @grid-ready="onGridReady"
        :columnDefs="columnDefs"
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
import { Alarm, Node } from "@/types";
import SeverityFloatingFilter from "./SeverityFloatingFilter.vue"
import NumberFloatingFilterComponent from "./NumberFloatingFilterComponent.vue"

const store = useStore();

const gridOptions = ref({})

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

let rowData = ref(getAlarmsFromSelectedNodes());

let gridApi = ref({});

function onGridReady(params: any) {
  gridApi = params.api
}

watch(
  () => interestedNodesID.value,
  (newValue, oldValue) => {
    console.log("I'm changed from " + oldValue + " to " + newValue)
    gridApi.setRowData(
      getAlarmsFromSelectedNodes()
    );
  }
)

function getAlarmsFromSelectedNodes() {
  let alarms = store.getters['mapModule/getAlarmsFromSelectedNodes'];
  return alarms.map((alarm: Alarm) => ({
    id: +alarm.id,
    severity: alarm.severity,
    node: alarm.nodeLabel,
    uei: alarm.uei,
    count: +alarm.count,
    lastEventTime: alarm.lastEvent.time,
    logMessage: alarm.logMessage,
  }));
}

function clearFilters() {
  gridApi.setFilterModel(null);
}

function confirmFilters() {
  let nodesLable: any = [];
  gridApi.forEachNodeAfterFilter((node: any) => {
    nodesLable.push(node.data.node);
  });
  let distictNodesLable = [...new Set(nodesLable)];
  let ids = [];
  ids = store.getters['mapModule/getInterestedNodes']
    .filter((node: Node) => distictNodesLable.includes(node.label))
    .map((node: Node) => node.id);
  store.dispatch("mapModule/setInterestedNodesId", ids);
}

function reset() {
  store.dispatch("mapModule/resetInterestedNodesID");
}

const defaultColDef = ref({
  floatingFilter: true,
  resizable: true,
  enableBrowserTooltips: true,
  filter: "agTextColumnFilter",
  sortable: true,
})

const columnDefs = ref([
  {
    headerName: "ID",
    field: "id",
    headerTooltip: "ID",
    headerCheckboxSelection: true,
    checkboxSelection: true,
    headerCheckboxSelectionFilteredOnly: true,
    filter: "agNumberColumnFilter",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "SEVERITY",
    field: "severity",
    headerTooltip: "Severity",
    floatingFilterComponentFramework: SeverityFloatingFilter,
    floatingFilterComponentParams: {
      suppressFilterButton: true,
    },
    comparator: (valueA: string, valueB: string) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "Node",
    field: "node",
    headerTooltip: "Node",
  },
  {
    headerName: "UEI",
    field: "lable",
    headerTooltip: "Lable",
  },
  {
    headerName: "LABLE SOURCE",
    field: "uei",
    headerTooltip: "UEI",
  },
  {
    headerName: "COUNT",
    field: "count",
    headerTooltip: "Count",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "LAST EVENT TIME",
    field: "lastEventTime",
    headerTooltip: "Last Event Time",
    filter: "agDateColumnFilter",
    cellRenderer: (data: any) => {
      return data.value ? new Date(data.value).toLocaleDateString() : "";
    },
  },
  {
    headerName: "LOG MESSAGE",
    field: "logMessage",
    headerTooltip: "Log Message",
  },
]
)
</script>

<style scoped>
.button-group {
  width: 100%;
  height: 25px;
}
.map-alarms-grid {
  width: 100%;
  height: 700px;
}
.map-alarm-buttons {
  float: right;
}
button {
  margin-left: 4px;
  margin-right: 6px;
}
</style>
