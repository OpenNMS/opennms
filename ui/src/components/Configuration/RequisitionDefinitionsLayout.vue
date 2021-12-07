<template>
  <div class="container">
    <div class="btnAction">
      <FeatherButton primary @click="clickAction(index)">
        <template v-slot:icon>
          <FeatherIcon :icon="icon" aria-hidden="true" focusable="false"></FeatherIcon>
        </template>
        {{ buttonAction[index] }}
      </FeatherButton>
    </div>
    <div class="reqDef">
      <div class="dataTable" v-if="index === 0">
        <table id="feather-table">
          <thead>
            <tr>
              <FeatherSortHeader
                v-for="node in tableHeaders"
                :property="node"
                scope="col"
                v-on:sort-changed="sortChanged"
              >
                {{node.toLocaleUpperCase()}}
              </FeatherSortHeader>
              <th scope="col"></th>
              <th scope="col"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(dataRow, index) in nodeDataValue">
                <td v-for="node in tableHeaders">
                  {{ dataRow[node]}}
                </td>
                <td v-for="columnName of customData">
                    <FeatherButton 
                      primary 
                      @click="onClickHandle(columnName, dataRow, index)"
                    >{{ columnName }}
                    </FeatherButton>
                </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else>
        <ReqDefinitionForm></ReqDefinitionForm>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { computed, onMounted, ref, watchEffect } from 'vue'
import { FeatherButton }   from '@featherds/button'
import { FeatherIcon }   from '@featherds/icon'
import { FeatherSortHeader, SORT } from "@featherds/table";
import actionsAdd from "@featherds/icon/action/Add";
import navigationArrowBack from "@featherds/icon/navigation/ArrowBack";
import { markRaw } from "vue";
import ReqDefinitionForm from './ReqDefinitionForm.vue'
import { useStore } from 'vuex'
import router from '@/router';
import { notify } from "@kyvg/vue3-notification"
import { putProvisionDService } from "./../../services/configurationService"
import { FeatherSortObject } from '@/types'

const store = useStore()
const buttonAction = ref(['ADD NEW', 'BACK'])
const index = ref(0)
const icon = ref(markRaw(actionsAdd));
const isData = ref(false)
let customData: any = ref([])
const provisionDService = computed(() => { return store.state.configuration.provisionDService })

const tableHeaders = computed(() => {
  if (provisionDService.value) {
    return Object.keys(provisionDService.value["requisition-def"][0]);
  }
})

let nodeDataValue = computed(() => {
  if (provisionDService.value) {

    let copyState = [], cronScheduleType: string[], valuePos: number, ele: number
    cronScheduleType = ['minute', 'hour', 'day of month', 'month', 'day of week']
    copyState = JSON.parse(JSON.stringify(provisionDService.value))

    let data = (copyState as any)["requisition-def"]
    if (data && data.length > 1) {
      customData.value = ['edit', 'delete']
      isData.value = true //show pagination 

      // cron-schedule expression changed to human readable format 
      const copydata = data.filter((rowData: any) => {
        let items = rowData['cron-schedule'].split(' ')
        items.forEach((element: any) => {
          if (!isNaN(element)) {
            ele = element
            valuePos = items.indexOf(element)
          }
        })
        return rowData['cron-schedule'] = `Every ${ele} ${cronScheduleType[valuePos]}`
      })
      //return updated data
      console.log('copydata');
      console.log(copydata);
      return copydata
    }
  }
  return []
})

onMounted(async () => {
  try {
    await store.dispatch('configuration/getProvisionDService')
  } catch {
    console.error("Error in API - Inside datatableDemo")
  }
})

const sortChanged = (sortObj: FeatherSortObject) => { 
  if(sortObj.value === 'asc'){
    computed(() => nodeDataValue.value.sort((a: any, b: any) => (a[`${sortObj.property}`] > b[`${sortObj.property}`] ? 1 : -1)));
  }
  console.log(nodeDataValue.value);
}
const onClickHandle = (selectedName: any, data: any, index: any) => {
  //Added one dyanamic property to data for identitify the table position - helps in edit put call
  data['tablePosition'] = index

  switch (selectedName) {
    case "edit":
      //edit click data state store
      store.commit('configuration/SEND_MODIFIED_DATA', data)
      //route to edit node component
      router.push({ path: `/${selectedName}/${data['import-name']}` })
      break
    case "delete":
      const confirmResponse = confirm(`Please confirm delete ${data['import-name']}?`)
      deleteAction(confirmResponse, data['tablePosition'])
      break
    default:
      alert(`please add logic for ${selectedName}`)
  }
}

const deleteAction = (response: boolean, removePosition: number) => {
  if (response == true) {
    try {
      const provisionData = store.state.configuration.provisionDService['requisition-def']
      let copyState = [...provisionData]
      copyState.splice(removePosition, 1)
      const requestPayload = { 'requisition-def': copyState }
      const response = putProvisionDService(requestPayload)
      notification(response)
    } catch {
      notify({
        title: "Notification",
        text: 'ProvisionDService PUT API Error',
        type: 'error',
      })
    }
  }
}

const notification = (response: any) => {
  if (response != null) {
    notify({
      title: "Notification",
      text: `Requisition definition data successfully deleted !`,
      type: 'success',
    })

    //Route to table and refresh the data
    router.push({ name: 'requisitionDefinitionsLayout' })
    setTimeout(() => {
      location.reload()
    }, 1000)
  }
}



const clickAction = (val: any) => {
  switch (val) {
    case 0:
      index.value = 1
      icon.value = markRaw(navigationArrowBack);
      break
    case 1:
      index.value = 0
      icon.value = markRaw(actionsAdd);
      break
  }
}

</script>

<style scoped>
@import "../Common/common.scss";
.btnAction {
  margin: 1% 0;
  float: right;
}

.reqDef {
  display: inline-block;
  width: 100%;
}
</style>
