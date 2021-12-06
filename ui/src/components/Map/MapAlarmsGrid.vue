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
  <table class="tl1 tl2 tl3" summary="Alarms">
    <thead>
      <tr>
        <th class="checkbox-cell">
          <FeatherCheckbox v-model="all" label="All" />
        </th>
        <th scope="col">ID</th>
        <th scope="col">SEVERITY</th>
        <th scope="col">NODE LABEL</th>
        <th scope="col">UEI</th>
        <th scope="col">COUNT</th>
        <th scope="col">LAST EVENT</th>
        <th scope="col">LOG MESSAGE</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="alarm in alarms" :key="alarm.id">
        <td class="checkbox-cell">
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
        <td>{{ alarm.lastEventTime }}</td>
        <td>{{ alarm.logMessage }}</td>
      </tr>
    </tbody>
  </table>
</template>
<script setup lang="ts">
import { ref } from "vue"
import { useStore } from "vuex"
import { computed } from 'vue'
import { Alarm, AlarmQueryParameters } from "@/types"
import { FeatherSelect } from '@featherds/select'
import { FeatherCheckbox } from '@featherds/checkbox'

const store = useStore()
const alarms = computed<Alarm[]>(() => store.getters['mapModule/getAlarms'])
const alarmOptions = [
  { id: 1, option: "Not Selected" },
  { id: 2, option: "Acknowledge" },
  { id: 3, option: "Unacknowledge" },
  { id: 4, option: "Escalate" },
  { id: 5, option: "Clear" }
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
      break
  }

  const selectedAlarms = alarms.value.filter((alarm) => all.value || alarmCheckboxes.value[alarm.id])

  let numFail: number = 0
  const respCollection: any = []
  selectedAlarms.forEach((alarm: Alarm) => {
    const resp = store.dispatch("mapModule/modifyAlarm", {
      pathVariable: alarm.id, queryParameters: alarmQueryParameters
    })
    respCollection.push(resp)
  })
  const result = await Promise.all(respCollection)
  result.forEach(r => {
    if (r === false) {
      numFail = numFail + 1
    }
  })
  // update and reset selections
  store.dispatch("mapModule/getAlarms")
  all.value = false
  alarmCheckboxes.value = {}
}
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
table {
  @include table();
  @include table-condensed();
  background: var(--feather-surface);
  color: var(--feather-primary-text-on-surface);
  display: block;
  height: calc(100% - 58px);
  width: 100%;
  overflow-y: scroll;
  padding-top: 4px;
  margin-top: 15px;
}
.select-ack {
  width: 300px;
  position: absolute;
  right: 30px;
  top: -15px;
}
</style>
