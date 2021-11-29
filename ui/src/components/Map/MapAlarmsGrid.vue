<template>
  <div id="flashMessage" v-if="GStore.flashMessage">{{ GStore.flashMessage }}</div>
  <div class="map-alarms">
    <div style="display: flex; justify-content: flex-end;">
      <FeatherSelect
        class="select"
        name="alarmOptions"
        id="alarmOptions"
        v-model="alarmOption"
        :disabled="!hasAlarmSelected"
        :options="alarmOptions"
        text-prop="option"
        @update:modelValue="selectAlarmAck"
        :hideLabel="true"
        label=""
      />
      <FeatherButton primary @click="clearFilters()">Clear Filters</FeatherButton>
      <FeatherButton primary @click="applyFilters()">Filter Map</FeatherButton>
      <FeatherButton primary @click="reset()">Reset</FeatherButton>
    </div>
    <div class="map-alarms-grid">
      <ag-grid-vue
        style="width: 100%; height: 100%"
        class="ag-theme-alpine"
        rowSelection="multiple"
        @grid-ready="onGridReady"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :pagination="true"
        @selection-changed="onSelectionChanged"
      ></ag-grid-vue>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, inject, onDeactivated } from "vue"
import "ag-grid-community/dist/styles/ag-grid.css"
import "ag-grid-community/dist/styles/ag-theme-alpine.css"
import { AgGridVue } from "ag-grid-vue3"
import { useStore } from "vuex"
import { computed, watch } from 'vue'
import { Alarm, Node, AlarmQueryParameters } from "@/types"
import SeverityFloatingFilter from "./SeverityFloatingFilter.vue"
import { FeatherButton } from "@featherds/button"
import { FeatherSelect } from '@featherds/select'
import { ColumnApi, GridApi, GridReadyEvent } from 'ag-grid-community'

const store = useStore()
const interestedNodesID = computed<string[]>(() => store.state.mapModule.interestedNodesID)
const alarms = computed(() => store.getters['mapModule/getAlarmsFromSelectedNodes'])
const rowData = ref(getAlarmsFromSelectedNodes())
const alarmOptions = [
  { id: 1, option: "Not Selected" },
  { id: 2, option: "Acknowledge" },
  { id: 3, option: "Unacknowledge" },
  { id: 4, option: "Escalate" },
  { id: 5, option: "Clear" }
]
const alarmOption = ref(alarmOptions[0])

let gridApi: GridApi
let gridColumnApi: ColumnApi

const onGridReady = (params: GridReadyEvent) => {
  gridApi = params.api
  gridColumnApi = params.columnApi
  autoSizeAll(false)
}

const autoSizeAll = (skipHeader: boolean) => {
  const columns = gridColumnApi.getAllColumns() || []
  const allColumnIds = columns.map((column) => column.getColId())
  gridColumnApi.autoSizeColumns(allColumnIds, skipHeader)
}

watch(
  () => [interestedNodesID.value, alarms.value],
  () => {
    if (gridApi.setRowData != undefined && gridApi.setRowData != null) {
      gridApi.setRowData(
        getAlarmsFromSelectedNodes()
      )
    }
  }
)

function getAlarmsFromSelectedNodes() {
  const alarms: Alarm[] = store.getters['mapModule/getAlarmsFromSelectedNodes']
  return alarms.map((alarm: Alarm) => ({
    id: +alarm.id,
    severity: alarm.severity,
    node: alarm.nodeLabel,
    uei: alarm.uei,
    count: +alarm.count,
    lastEventTime: alarm.lastEvent.time,
    logMessage: alarm.logMessage,
  }))
}

const selectAlarmAck = () => {
  let alarmQueryParameters: AlarmQueryParameters
  switch (alarmOption.value.option) {
    case alarmOptions[0].option:
      break
    case alarmOptions[1].option: { // "Acknowledge"
      alarmQueryParameters = { ack: true }
      break
    }
    case alarmOptions[2].option: { // "Unacknowledge"
      alarmQueryParameters = { ack: false }
      break
    }
    case alarmOptions[3].option: { // "Escalate"
      alarmQueryParameters = { escalate: true }
      break
    }
    case alarmOptions[4].option: { // "Clear"
      alarmQueryParameters = { clear: true }
      break
    }
    default:
      console.log("No such alarm option exists: " + alarmOption.value.option)
      break
  }

  let numFail: number = 0
  const respCollection: any = []
  selectedAlarmIds.value.forEach((alarmId: string) => {
    const resp = store.dispatch("mapModule/modifyAlarm", {
      pathVariable: alarmId, queryParameters: alarmQueryParameters
    })
    respCollection.push(resp)
  })
  Promise.all(respCollection).then(function (result) {
    result.forEach(r => {
      if (r === false) {
        numFail = numFail + 1
      }
    })

    // TODO: Fix message display
    GStore.flashMessage = (selectedAlarmIds.value.length - numFail) + " success, " + numFail + ' failed.'

    // TODO: Update alarm display without page refresh
    setTimeout(() => {
      GStore.flashMessage = ''
      window.location.reload()
    }, 4000)
  })
}

const GStore = inject<any>('GStore')

const selectedAlarmIds = ref<string[]>([])

const hasAlarmSelected = ref<boolean>(false)

const onSelectionChanged = () => {
  const selectedRows = gridApi.getSelectedNodes().map((node: any) => node.data)
  selectedAlarmIds.value = selectedRows.map((alarm: any) => alarm.id)
  hasAlarmSelected.value = selectedAlarmIds.value.length > 0
}

const clearFilters = () => gridApi.setFilterModel(null)

const applyFilters = () => {
  const nodesLabel: string[] = []
  gridApi.forEachNodeAfterFilter((node: any) => {
    nodesLabel.push(node.data.node)
  })
  const distictNodesLabel = [...new Set(nodesLabel)]
  const ids = store.getters['mapModule/getInterestedNodes']
    .filter((node: Node) => distictNodesLabel.includes(node.label))
    .map((node: Node) => node.id)
  store.dispatch("mapModule/setInterestedNodesId", ids)
}

const reset = () => store.dispatch("mapModule/resetInterestedNodesID")

const defaultColDef = {
  floatingFilter: true,
  resizable: true,
  enableBrowserTooltips: true,
  filter: "agTextColumnFilter",
  sortable: true,
  suppressMenu: true
}

const columnDefs = [
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
      return valueA - valueB
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
        const filterTextUpperCase = filterText.toUpperCase()
        const valueUpperCase = value.toString().toUpperCase()
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
        return true
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
      return valueA - valueB
    },
  },
  {
    headerName: "LAST EVENT TIME",
    field: "lastEventTime",
    headerTooltip: "Last Event Time",
    width: 120,
    filter: "agDateColumnFilter",
    cellRenderer: (data: any) => {
      return data.value ? new Date(data.value).toLocaleDateString() : ""
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
      const newData = `<style type = "text/css">
            p
            {
                margin: 0px;
            }
        </style> ${data.value}`
      return newData
    }
  }
]
onDeactivated(() => reset())
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
.map-alarms {
  background: var(--feather-background);
  color: var(--feather-primary-text-on-surface);
  height: calc(100% - 54px);
}
.map-alarms-grid {
  width: 100%;
  height: 100%;
}
.select {
  width: 200px;
  padding: 0px;
  margin-top: -14px;
  margin-bottom: 10px;
  margin-right: 17px;
}
.btn {
  margin: 0px;
}
</style>
