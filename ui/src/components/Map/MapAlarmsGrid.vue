<template>
  <FeatherSelect
    class="select-ack"
    name="alarmOptions"
    id="alarmOptions"
    v-model="alarmOption"
    :disabled="disableAckSelect"
    :options="alarmOptions"
    text-prop="option"
    @update:modelValue="selectAlarmAck"
    label="Alarm Action"
  />
  <div id="wrap">
    <table class="tl1 tl2 tl3" summary="Alarms">
      <thead>
        <tr>
          <th class="first-th">
            <FeatherCheckbox v-model="all" label="All" />
          </th>

          <FeatherSortHeader
            scope="col"
            property="id"
            :sort="sortStates.id"
            @sort-changed="sortChanged"
          >ID</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="severity"
            :sort="sortStates.severity"
            @sort-changed="sortChanged"
          >SEVERITY</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="nodeLabel"
            :sort="sortStates.nodeLabel"
            @sort-changed="sortChanged"
          >NODE LABEL</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="uei"
            :sort="sortStates.uei"
            @sort-changed="sortChanged"
          >UEI</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="count"
            :sort="sortStates.count"
            @sort-changed="sortChanged"
          >COUNT</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="lastEvent"
            :sort="sortStates.lastEventTime"
            @sort-changed="sortChanged"
          >LAST EVENT</FeatherSortHeader>

          <FeatherSortHeader
            scope="col"
            property="logMessage"
            :sort="sortStates.logMessage"
            @sort-changed="sortChanged"
          >LOG MESSAGE</FeatherSortHeader>
        </tr>
      </thead>
      <tbody>
        <tr v-for="alarm in alarms" :key="alarm.id">
          <td :class="alarm.severity" class="first-td">
            <FeatherCheckbox
              @update:modelValue="selectCheckbox(alarm)"
              :modelValue="all || alarmCheckboxes[alarm.id]"
              label="Alarm"
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
import { Alarm, AlarmQueryParameters, FeatherSortObject } from '@/types'
import { FeatherSelect } from '@featherds/select'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { useMapStore } from '@/stores/mapStore'

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

  const selectedAlarms = alarms.value.filter((alarm) => all.value || alarmCheckboxes.value[alarm.id])

  let numFail = 0
  const respCollection: any = []

  for (const alarm of selectedAlarms) {
    const resp = await mapStore.modifyAlarm({
      pathVariable: alarm.id, queryParameters: alarmQueryParameters
    })

    respCollection.push(resp)
  }

  const result = await Promise.all(respCollection)
  result.forEach(r => {
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

onMounted(() => {
  const wrap = document.getElementById('wrap')
  const thead = document.querySelector('thead')

  if (wrap && thead) {
    wrap.addEventListener('scroll', function () {
      let translate = 'translate(0,' + this.scrollTop + 'px)'
      thead.style.transform = translate
    })
  }
})
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/themes/variables";
#wrap {
  height: calc(100% - 29px);
  overflow: auto;
  background: var($surface);
}
table {
  @include table;
  @include table-condensed;
  background: var($surface);
  color: var($primary-text-on-surface);
  padding-top: 4px;
  margin-top: 15px;
}
thead {
  z-index: 2;
  position: relative;
  background: var($surface);
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
