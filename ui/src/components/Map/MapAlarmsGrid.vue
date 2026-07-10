<template>
  <FormField label="Alarm Action" class="select-ack">
    <PSelect
      name="alarmOptions"
      id="alarmOptions"
      v-model="alarmOption"
      :disabled="disableAckSelect"
      :options="alarmOptions"
      optionLabel="option"
      @update:modelValue="selectAlarmAck"
    />
  </FormField>
  <div id="wrap">
    <table class="tl1 tl2 tl3" summary="Alarms">
      <thead>
        <tr>
          <th class="first-th">
            <PCheckbox binary v-model="all" aria-label="All" />
          </th>

          <SortableTh
            scope="col"
            property="id"
            :sort="sortStates.id"
            @sort-changed="sortChanged"
          >ID</SortableTh>

          <SortableTh
            scope="col"
            property="severity"
            :sort="sortStates.severity"
            @sort-changed="sortChanged"
          >SEVERITY</SortableTh>

          <SortableTh
            scope="col"
            property="nodeLabel"
            :sort="sortStates.nodeLabel"
            @sort-changed="sortChanged"
          >NODE LABEL</SortableTh>

          <SortableTh
            scope="col"
            property="uei"
            :sort="sortStates.uei"
            @sort-changed="sortChanged"
          >UEI</SortableTh>

          <SortableTh
            scope="col"
            property="count"
            :sort="sortStates.count"
            @sort-changed="sortChanged"
          >COUNT</SortableTh>

          <SortableTh
            scope="col"
            property="lastEvent"
            :sort="sortStates.lastEventTime"
            @sort-changed="sortChanged"
          >LAST EVENT</SortableTh>

          <SortableTh
            scope="col"
            property="logMessage"
            :sort="sortStates.logMessage"
            @sort-changed="sortChanged"
          >LOG MESSAGE</SortableTh>
        </tr>
      </thead>
      <tbody>
        <tr v-for="alarm in alarms" :key="alarm.id">
          <td :class="alarm.severity" class="first-td">
            <PCheckbox
              binary
              aria-label="Alarm"
              @update:modelValue="selectCheckbox(alarm)"
              :modelValue="all || alarmCheckboxes[alarm.id]"
            />
          </td>
          <td>{{ alarm.id }}</td>
          <td>{{ alarm.severity }}</td>
          <td>{{ alarm.nodeLabel }}</td>
          <td>{{ alarm.uei }}</td>
          <td>{{ alarm.count }}</td>
          <td v-date>{{ alarm.lastEventTime }}</td>
          <td>{{ alarm.logMessage }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'

import { Alarm, AlarmQueryParameters, FeatherSortObject } from '@/types'
import Select from 'primevue/select'
import Checkbox from 'primevue/checkbox'
import { SORT } from '@/types'
import FormField from '@/components/Common/FormField.vue'
import SortableTh from './SortableTh.vue'
import { useMapStore } from '@/stores/mapStore'

const PSelect = Select
const PCheckbox = Checkbox

const mapStore = useMapStore()
const alarms = computed<Alarm[]>(() => mapStore.getAlarms())
const alarmOptions = [
  { id: 1, option: 'Not Selected' },
  { id: 2, option: 'Acknowledge' },
  { id: 3, option: 'Unacknowledge' },
  { id: 4, option: 'Escalate' },
  { id: 5, option: 'Clear' }
]
const alarmOption = ref(alarmOptions[0])
const all = ref(false)
const alarmCheckboxes = ref<{ [x: string]: boolean }>({})

const disableAckSelect = computed(() => {
  let hasSelectedCheckbox = false

  for (const key in alarmCheckboxes.value) {
    if (alarmCheckboxes.value[key]) {
      hasSelectedCheckbox = true
      break
    }
  }
  return !all.value && !hasSelectedCheckbox
})

const selectCheckbox = (alarm: Alarm) => {
  alarmCheckboxes.value[alarm.id] = !alarmCheckboxes.value[alarm.id]
}

const selectAlarmAck = async () => {
  let alarmQueryParameters: AlarmQueryParameters = {} as AlarmQueryParameters

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
      break
  }

  const selectedAlarms = alarms.value.filter(alarm => all.value || alarmCheckboxes.value[alarm.id])

  let numFail = 0
  const respCollection: any = []

  for (const alarm of selectedAlarms) {
    const resp = await mapStore.modifyAlarm({
      pathVariable: alarm.id, queryParameters: alarmQueryParameters
    })

    respCollection.push(resp)
  }

  const result = await Promise.all(respCollection)
  result.forEach((r) => {
    if (r === false) {
      numFail = numFail + 1
    }
  })

  // update and reset selections
  mapStore.getAlarms()
  all.value = false
  alarmCheckboxes.value = {}
}

const sortStates: any = reactive({
  id: SORT.DESCENDING,
  severity: SORT.NONE,
  nodeLabel: SORT.NONE,
  uei: SORT.NONE,
  count: SORT.NONE,
  lastEventTime: SORT.NONE,
  logMessage: SORT.NONE
})

const sortChanged = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[`${sortObj.property}`] = sortObj.value
  mapStore.setAlarmSortObject(sortObj)
}
</script>

<style lang="scss" scoped>
@import "@/styles/onms-table";
@import "@featherds/styles/themes/variables";
#wrap {
  height: calc(100% - 29px);
  overflow: auto;
  background: var(--p-content-background);
}
table {
  @include onms-table;
  @include onms-table-condensed;
  background: var(--p-content-background);
  color: var(--p-text-color);
  padding-top: 4px;
  margin-top: 15px;
}
// CSS sticky header (replaces a JS scroll-transform hack that jittered and let
// rows bleed above the header). Sticky lives on the cells (thead sticky has
// spotty support) and needs an opaque background so body rows don't show through.
thead th {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--p-content-background);
}
.select-ack {
  z-index: var($zindex-dropdown);
  width: 300px;
  position: absolute;
  right: 30px;
  top: 7px;
}
.first-th {
  padding-left: 20px;
}
.first-td {
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
