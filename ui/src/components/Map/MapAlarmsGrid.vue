<template>
  <div id="flashMessage" v-if="GStore.flashMessage">{{ GStore.flashMessage }}</div>
  <div class="map-alarms">
    <div class="button-group">
      <span class="map-alarm-buttons">
        <select
          name="alarmOptions"
          id="alarmOptions"
          v-model="alarmOption"
          :disabled="!hasAlarmSelected"
        >
          <option v-for="option in alarmOptions" :value="option" :key="option">{{ option }}</option>
        </select>
        <!-- <section>
          <FeatherSelect
            class="my-select"
            label="Alarm Action"
            :options="alarmOptions"
            v-model="alarmOption"
            :disabled="!hasAlarmSelected"
          ></FeatherSelect>
        </section>-->
        <feather-button primary @click="clearFilters()">Clear Filters</feather-button>
        <feather-button primary @click="applyFilters()">Filter Map</feather-button>
        <feather-button primary @click="reset()">Reset</feather-button>
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
import SeverityFloatingFilter from "./SeverityFloatingFilter.vue"
// import { FeatherSelect } from "@featherds/select";
import { FeatherButton } from "@featherds/button";

const store = useStore();

const gridOptions = ref({})

const interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

const alarms = computed(() => {
  return store.getters['mapModule/getAlarmsFromSelectedNodes'];
})

const rowData = ref(getAlarmsFromSelectedNodes());

let gridApi: any = ref({});

let gridColumnApi: any = ref({});

function onGridReady(params: any) {
  gridApi = params.api
  gridColumnApi = params.columnApi;
  autoSizeAll(false);
}

function autoSizeAll(skipHeader: boolean) {
  const allColumnIds: string[] = [];
  gridColumnApi.getAllColumns().forEach(function (column: any) {
    allColumnIds.push(column.colId);
  });
  gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
}

watch(
  () => [interestedNodesID.value, alarms.value],
  () => {
    if (gridApi.setRowData != undefined && gridApi.setRowData != null) {
      gridApi.setRowData(
        getAlarmsFromSelectedNodes()
      );
    }
  }
)

function getAlarmsFromSelectedNodes() {
  const alarms: Alarm[] = store.getters['mapModule/getAlarmsFromSelectedNodes'];
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

const alarmOptions = ref<string[]>(["Not Selected", "Acknowledge", "Unacknowledge", "Escalate", "Clear"]);

const alarmOption = ref<string>(alarmOptions.value[0]);

watch(
  () => [alarmOption.value],
  () => {
    let alarmQueryParameters: AlarmQueryParameters;
    switch (alarmOption.value) {
      case alarmOptions.value[0]:
        break;
      case alarmOptions.value[1]: { // "Acknowledge"
        alarmQueryParameters = { ack: true };
        break;
      }
      case alarmOptions.value[2]: { // "Unacknowledge"
        alarmQueryParameters = { ack: false };
        break;
      }
      case alarmOptions.value[3]: { // "Escalate"
        alarmQueryParameters = { escalate: true };
        break;
      }
      case alarmOptions.value[4]: { // "Clear"
        alarmQueryParameters = { clear: true };
        break;
      }
      default:
        console.log("No such alarm option exists: " + alarmOption.value);
        break;
    }

    let numFail: number = 0;
    const respCollection: any = [];
    selectedAlarmIds.value.forEach((alarmId: string) => {
      const resp = store.dispatch("mapModule/modifyAlarm", {
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
      GStore.flashMessage = (selectedAlarmIds.value.length - numFail) + " success, " + numFail + ' failed.'

      setTimeout(() => {
        GStore.flashMessage = ''
        window.location.reload()
      }, 4000)
    })
  }
)

const GStore = inject<any>('GStore');

const selectedAlarmIds = ref<string[]>([]);

const hasAlarmSelected = ref<boolean>(false);

function onSelectionChanged() {
  let selectedRows = gridApi.getSelectedNodes().map((node: any) => node.data);
  selectedAlarmIds.value = selectedRows.map((alarm: any) => alarm.id);
  hasAlarmSelected.value = selectedAlarmIds.value.length > 0;
}

function clearFilters() {
  gridApi.setFilterModel(null);
}

function applyFilters() {
  const nodesLabel: string[] = [];
  gridApi.forEachNodeAfterFilter((node: any) => {
    nodesLabel.push(node.data.node);
  });
  const distictNodesLabel = [...new Set(nodesLabel)];
  const ids = store.getters['mapModule/getInterestedNodes']
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
      textCustomComparator: (filter: string, value: any, filterText: any) => {
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
    filter: "agNumberColumnFilter",
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
      //This is a temporary solution. Currently the settings user set make the "Log Message" saved in database in html style. But the style may not 
      //fit our new Vue UI. That's the reason we add css to change it. This part of code will eventually be removed in future project design.   
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

<style lang="scss" scoped>
@keyframes bluefade {
  from {
    background: var(--feather-primary);
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
  height: 40px;
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
.btn {
  margin-top: 0px;
  margin-bottom: 0px;
  margin-right: 10px;
}
.my-select {
  width: 200px;
}
</style>
