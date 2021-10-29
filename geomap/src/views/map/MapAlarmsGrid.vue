<template>
  <div id="flashMessage" v-if="GStore.flashMessage" style>{{ GStore.flashMessage }}</div>
  <div class="map-alarms">
    <div class="button-group">
      <span class="map-alarm-buttons">
        <select
          name="alarmOptions"
          id="alarmOptions"
          v-model="currentAlarmOption"
          :disabled="!hasAlarmSelected"
        >
          <option v-for="option in alarmOptions" :value="option" :key="option">{{ option }}</option>
        </select>
        <button type="button" :disabled="!hasAlarmSelected" @click="submit()">Submit</button>
        <button @click="clearFilters()">Clear Filters</button>
        <button @click="applyFilters()">Apply filter</button>
        <button @click="reset()">Reset</button>
      </span>
    </div>
    <div class="map-alarms-grid">
      <ag-grid-vue
        style="width: 100%; height: 600px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        @grid-ready="onGridReady"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true"
        @selection-changed="onSelectionChanged"
      ></ag-grid-vue>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, inject } from "vue";
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";
import { useStore } from "vuex";
import { computed, watch } from 'vue'
import { Alarm, Node, AlarmQueryParameters } from "@/types";
import SeverityFloatingFilter from "../../components/SeverityFloatingFilter.vue"

const store = useStore();

const gridOptions = ref({})

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

let rowData = ref(getAlarmsFromSelectedNodes());

let gridApi = ref({});

let gridColumnApi = ref({});

function onGridReady(params: any) {
  gridApi = params.api
  gridColumnApi = params.columnApi;
  sizeToFit()
}

function sizeToFit() {
  gridApi.sizeColumnsToFit();
}

watch(
  () => interestedNodesID.value,
  () => {
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

let alarmOptions = ref(["Acknowledge", "Unacknowledge", "Escalate", "Clear"]);

let currentAlarmOption = ref(alarmOptions.value[0]);

const GStore = inject('GStore');

let selectedAlarmIds = ref([]);

let hasAlarmSelected = ref(false);

function onSelectionChanged() {
  let selectedRows = gridApi.getSelectedNodes().map(node => node.data);
  selectedAlarmIds = selectedRows.map(alarm => alarm.id);
  hasAlarmSelected.value = selectedAlarmIds.length > 0;
}

function submit() {
  let alarmQueryParameters: AlarmQueryParameters;
  switch (currentAlarmOption.value) {
    case alarmOptions.value[0]: { // "Acknowledge"
      alarmQueryParameters = { ack: true };
      break;
    }
    case alarmOptions.value[1]: { // "Unacknowledge"
      alarmQueryParameters = { ack: false };
      break;
    }
    case alarmOptions.value[2]: { // "Escalate"
      alarmQueryParameters = { escalate: true };
      break;
    }
    case alarmOptions.value[3]: { // "Clear"
      alarmQueryParameters = { clear: true };
      break;
    }
    default:
      console.log("No such alarm option exists: " + currentAlarmOption.value);
      break;
  }

  let numFail = 0;
  let respCollection = [];
  selectedAlarmIds.forEach((alarmId: string) => {
    let resp = store.dispatch("mapModule/modifyAlarm", {
      pathVariable: alarmId, queryParameters: alarmQueryParameters
    })
    respCollection.push(resp)
  })
  Promise.all(respCollection).then(function (result) {
    result.forEach(r => {
      if (r === false) {
        numFail = numFail + 1;
      }
    })
    GStore.flashMessage = (selectedAlarmIds.length - numFail) + " success, " + numFail + ' failed.'

    setTimeout(() => {
      GStore.flashMessage = ''
      window.location.reload()
    }, 4000)  
  })
}

function clearFilters() {
  gridApi.setFilterModel(null);
}

function applyFilters() {
  let nodesLabel: any = [];
  gridApi.forEachNodeAfterFilter((node: any) => {
    nodesLabel.push(node.data.node);
  });
  let distictNodesLabel = [...new Set(nodesLabel)];
  let ids = [];
  ids = store.getters['mapModule/getInterestedNodes']
    .filter((node: Node) => distictNodesLabel.includes(node.label))
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
  suppressMenu: true
})

const columnDefs = ref([
  {
    headerName: "ID",
    field: "id",
    headerTooltip: "ID",
    width: 50,
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
    width: 100,
    floatingFilterComponentFramework: SeverityFloatingFilter,
    floatingFilterComponentParams: {
      suppressFilterButton: true,
    },
    filterParams: {
      textCustomComparator: (filter, value, filterText) => {
        const filterTextUpperCase = filterText.toUpperCase();
        const valueUpperCase = value.toString().toUpperCase();
        enum ALARM_SEVERITY {
          'INDETERMINATE',
          'CLEARED',
          'NORMAL',
          'WARNING',
          'MINOR',
          'MAJOR',
          'CRITICAL'
        }
        if (filter === 'contains') {
          return ALARM_SEVERITY[valueUpperCase] >= ALARM_SEVERITY[filterTextUpperCase]
        }
        return true;
      }
    }
  },
  {
    headerName: "NODE",
    field: "node",
    headerTooltip: "Node",
    width: 100,
  },
  {
    headerName: "UEI",
    field: "uei",
    headerTooltip: "UEI",
  },
  {
    headerName: "COUNT",
    field: "count",
    width: 50,
    headerTooltip: "Count",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "LAST EVENT TIME",
    field: "lastEventTime",
    headerTooltip: "Last Event Time",
    width: 120,
    filter: "agDateColumnFilter",
    cellRenderer: (data: any) => {
      return data.value ? new Date(data.value).toLocaleDateString() : "";
    },
    sort: "desc"
  },
  {
    headerName: "LOG MESSAGE",
    field: "logMessage",
    headerTooltip: "Log Message",
    cellRenderer: (data: any) => {
      let newData = `<style type = "text/css">
            p
            {
                margin: 0px;
            }
        </style> ${data.value}`;
      return newData;
      }
  },
]
)
</script>

<style scoped>
@keyframes bluefade {
  from {
    background: rgb(104, 150, 236);
  }
  to {
    background: transparent;
  }
}

#flashMessage {
  animation-name: bluefade;
  animation-duration: 4s;
  text-align: center;
}
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
