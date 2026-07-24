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
            <OnmsCheckbox v-model="all" aria-label="All" />
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
            <OnmsCheckbox
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

import { Alarm, AlarmQueryParameters, ISortObject } from '@/types'
import Select from 'primevue/select'
import { OnmsCheckbox } from '@opennms/onms-ui'
import { SORT } from '@/types'
import FormField from '@/components/Common/FormField.vue'
import SortableTh from './SortableTh.vue'
import { useMapStore } from '@/stores/mapStore'

const PSelect = Select

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

const sortChanged = (sortObj: ISortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[`${sortObj.property}`] = sortObj.value
  mapStore.setAlarmSortObject(sortObj)
}
</script>

<style lang="scss" scoped>
@import "@/styles/onms-table";
@import "@/styles/onms-tokens";
#wrap {
  // Height accounts for the 15px top gap below so the pane keeps its footprint.
  height: calc(100% - 44px);
  overflow: auto;
  background: var(--p-content-background);
  // Gap above the table lives on the scroll container (outside the scrolled
  // content) so it doesn't make the sticky header travel before locking.
  margin-top: 15px;
}
table {
  @include onms-table;
  @include onms-table-condensed;
  background: var(--p-content-background);
  color: var(--p-text-color);
  // No top margin/padding: any space above thead inside the scroll container
  // makes the sticky header travel that distance before it locks at top:0.
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
// "Alarm Action" control: lay the label and dropdown out on a single line
// (overriding FormField's default stacked `column`) so the label and select sit
// inline with the tab headers, anchored right with room for both. Uses
// `.select-ack.form-field` + :deep() to outrank FormField's own scoped rules.
.select-ack.form-field {
  z-index: var($zindex-dropdown);
  position: absolute;
  right: 30px;
  top: 7px;
  width: 360px;
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;

  :deep(.form-field__label) {
    margin-bottom: 0;
    white-space: nowrap;
  }

  :deep(.p-select) {
    flex: 1 1 auto;
    width: auto;
  }
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
