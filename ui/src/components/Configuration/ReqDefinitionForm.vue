<template>
  <p class="title">{{ title }} Requisition Definition</p>
  <div class="p-col-9">
    <form @submit.prevent="onSave">
      <div class="p-fluid">
        <div class="p-field">
          <label for="name" class="required">Name</label>
          <FeatherInput
            id="name"
            v-model="model.reqDef.name.$model"
            :error="nameValidation"
            @keyup="getErrorMessage('name', model.reqDef.name.$errors)"
          >
          </FeatherInput>
        </div>

        <div class="p-field">
          <label for="type" class="required">Type</label>
          <div>
            <FeatherDropdown>
              <template v-slot:trigger :right="right" :cover="cover" :standard="standard">
                <FeatherButton link href="#" menu-trigger>
                  {{typeDropDownValue==="" ? "Select type": typeDropDownValue}}
                  <!-- {{model.reqDef.type.$model.name}} -->
                </FeatherButton>
              </template>
              <FeatherDropdownItem v-for="state in stateTypes" :key="state" @click="typeDropdownChange(state)">
                {{ state.name }}
              </FeatherDropdownItem>
            </FeatherDropdown>
          </div>
          <!-- <DropDown
            v-model="model.reqDef.type.$model"
            :options="stateTypes"
            optionLabel="name"
            optionValue="value"
            :filter="true"
            @change="generateURL"
          ></DropDown> -->
        </div>

        <div class="p-field">
          <label for="host" class="required">Host</label>

          <FeatherInput
            id="host"
            v-model="model.reqDef.host.$model"
            :error="hostValidation"
            @keyup="getErrorMessage('host', model.reqDef.host.$errors)"
            @change="generateURL"
          >
          </FeatherInput>
        </div>

        <div class="p-field">
          <label for="foreignSource" class="required">Foreign Source</label>
          <FeatherInput
            id="foreignSource"
            v-model="model.reqDef.foreignSource.$model"
            :error="foreignSourceValidation"
            @keyup="getErrorMessage('foreignSource', model.reqDef.foreignSource.$errors)"
          >
          </FeatherInput>
        </div>

        <div class="p-field">
          <label for="advOps">Advanced Options</label>
          <div class v-for="add in addAnotherArr">
            <p class="closeBtn">
              <FeatherButton
                primary
                v-if="add.id !== 0"
                icon="Cancel"
                @click="closeIcon(add.id)"
                @change="generateURL"
              >
                <FeatherIcon :icon="navigationCancelIcon"> </FeatherIcon>
              </FeatherButton>
            </p>
            <!-- <DropDown
              v-model="add.dropdownVal"
              :options="stateAdvancedDropdown"
              optionLabel="name"
              optionValue="value"
              @change="generateURL"
            ></DropDown> -->

            <FeatherDropdown>
              <template v-slot:trigger :right="right" :cover="cover" :standard="standard">
                <FeatherButton link href="#" menu-trigger>
                  {{advanceOptionDropDownValue==="" ? "Select type": advanceOptionDropDownValue}}
                  <!-- {{model.reqDef.type.$model.name}} -->
                </FeatherButton>
              </template>
              <FeatherDropdownItem 
                v-for="advanceOption in stateAdvancedDropdown" 
                :key="advanceOption" 
                @click="advanceOptionDropdownChange(advanceOption)"
              >
                {{ advanceOption.name }}
              </FeatherDropdownItem>
            </FeatherDropdown>

            <p class="inputText-margin">
              <FeatherInput
                id="name"
                v-model="add.advTextVal"
                @change="generateURL"
              >
              </FeatherInput>
            </p>
          </div>
          <div class="width100">
            <FeatherButton primary @click="addAnother">
              <template v-slot:icon>
                <FeatherIcon :icon="actionsAddIcon" aria-hidden="true" focusable="false"></FeatherIcon>
              </template>
              Add Another
            </FeatherButton>
            <a class="viewDoc">View Documentation</a>
          </div>
        </div>

        <div class="p-field">
          <p>
            <b>URL :</b>
            {{ generatedURL }}
          </p>
        </div>

        <div class="p-field">
          <label for="type" class="required">Schedule Period</label>
          <!-- <DropDown
            v-model="model.reqDef.schedulePeriod.$model"
            :options="stateSchedulePeriod"
            optionLabel="name"
            optionValue="value"
            :filter="true"
          ></DropDown> -->
          <div>
            <FeatherDropdown>
              <template v-slot:trigger :right="right" :cover="cover" :standard="standard">
                <FeatherButton link href="#" menu-trigger>
                  {{schedulePeriodDropDownValue==="" ? "Select Period": schedulePeriodDropDownValue}}
                </FeatherButton>
              </template>
              <FeatherDropdownItem 
                v-for="schedulePeriod in stateSchedulePeriod" 
                :key="schedulePeriod" 
                @click="schedulePeriodDropdownChange(schedulePeriod)">
                {{ schedulePeriod.name }}
              </FeatherDropdownItem>
            </FeatherDropdown>
          </div>
          <p class="p-field p-col-6">
            Every
            <span>
              <FeatherInput
                v-model="model.reqDef.schedulePeriodNumber.$model"
                type="number"
                :min="minVal"
              />
            </span>
            {{ schedulePeriodDropDownValue }}
          </p>
        </div>

        <div class="p-field p-col-2">
          <FeatherButton
            primary
            :disabled="model.reqDef.$invalid"
            type="submit"
            @disabled-click="disabledClick"
          >Save
          </FeatherButton>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">

import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherButton }   from '@featherds/button'
import { FeatherIcon }   from '@featherds/icon'
import { FeatherInput }   from '@featherds/input'
// import { FeatherDropdown, FeatherDropdownItem }   from '@featherds/dropdown'
import actionsAdd from "@featherds/icon/action/Add";
import navigationCancel from "@featherds/icon/navigation/Cancel";
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
// import InputNumber from '../Common/InputNumber.vue'
import State from './formState'
import router from '@/router'
import {
  GET_TYPES_DROPDOWN,
  GET_SCHEDULE_PERIOD_DROPDOWN,
  GET_ADVANCED_DROPDOWN
} from '../../store/configuration/actions'
import { notify } from "@kyvg/vue3-notification"
import { putProvisionDService } from "./../../services/configurationService"

const store = useStore()
const reqDefinition = reactive(State)

const minVal = ref(1)
const count = ref(0)
const actionsAddIcon = ref(actionsAdd);
const navigationCancelIcon = ref(navigationCancel);
const addAnotherArr = ref([{ "id": count.value, "dropdownVal": '', "advTextVal": '' }])
const generatedURL = ref('')
const advString: any = ref([])
const putDataPosition = ref()
const model = State.toModel()
const right = ref(false)
const cover = ref(false)
const standard = ref(false)
const typeDropDownValue = ref('')
const advanceOptionDropDownValue = ref('')
const schedulePeriodDropDownValue = ref('')
const props = defineProps({
  title: {
    type: String,
    default: "New"
  }
})

const nameValidation = ref('');
const hostValidation = ref('');
const foreignSourceValidation = ref('');

const disabledClick = (e:any)=>{
  e.preventDefault();
}

const getErrorMessage = (field:string, err:any) => {
  switch (field) {
    case 'name':
      nameValidation.value = err.length > 0 ? err[0].$message: '';
      break;

    case 'host':
      hostValidation.value = err.length > 0 ? err[0].$message: '';
      break;

    case 'foreignSource':
      foreignSourceValidation.value = err.length > 0 ? err[0].$message: '';
      break;
  }
}

onMounted(async () => {
  try {
    // Types
    await store.dispatch(GET_TYPES_DROPDOWN)
    // Schedule Period
    await store.dispatch(GET_SCHEDULE_PERIOD_DROPDOWN)
    // Advanced Dropdown
    await store.dispatch(GET_ADVANCED_DROPDOWN)

    // Edit Operation logic
    if (router.currentRoute.value.name === 'reqDefEdit') {

      let data = store.state.configuration.sendModifiedData
      let url = data['import-url-resource'].split('/')
      putDataPosition.value = data['tablePosition']   //Helps in edit save
      reqDefinition.reqDef.name = data['import-name']
      reqDefinition.reqDef.type = url[0].split(':')[0]
      typeDropDownValue.value = reqDefinition.reqDef.type
      reqDefinition.reqDef.host = url[2]
      generatedURL.value = data['import-url-resource']

      //temp logic to patch/set schedule period value
      setCronSchedule(data['cron-schedule'])

      let patchVal = url[3].split('?')
      reqDefinition.reqDef.foreignSource = patchVal[0]
      //On edit patch/set data - for advance dropdown
      setAdvDropDowndata(patchVal)

    } else {
      reqDefinition.reqDef.name = ''
      reqDefinition.reqDef.type = ''
      reqDefinition.reqDef.host = ''
      reqDefinition.reqDef.foreignSource = ''
    }
  } catch {
    console.error("Error in API/Logic")
  }
})

const stateTypes = computed(() => {
  return store.state.configuration.types
})

const stateSchedulePeriod = computed(() => {
  return store.state.configuration.schedulePeriod
})

const stateAdvancedDropdown = computed(() => {
  return store.state.configuration.advancedDropdown
})

const setCronSchedule = (data: any) => {
  let values = data.split(' ')
  reqDefinition.reqDef.schedulePeriodNumber = parseInt(values[1])
  if (values.length > 3) {
    reqDefinition.reqDef.schedulePeriod = values.slice(2, values.length + 1).join(' ')
    schedulePeriodDropDownValue.value = values.slice(2, values.length + 1).join(' ')
  } else {
    reqDefinition.reqDef.schedulePeriod = values[2]
    schedulePeriodDropDownValue.value = values[2]
  }
}

const setAdvDropDowndata = (patchVal: any) => {
  //add edit data value to advance dropdown
  const dropVal = (dropdownVal: any, advTextVal: any, index: any) => {
    if (index == 1) {
      addAnotherArr.value[0]['dropdownVal'] = dropdownVal
      advanceOptionDropDownValue.value = dropdownVal
      addAnotherArr.value[0]['advTextVal'] = advTextVal
    } else {
      let addObj = { "id": index - 1, "dropdownVal": dropdownVal, "advTextVal": advTextVal }
      addAnotherArr.value.push(addObj)
    }
  }

  //Identify how many advance parameter
  for (let i = 1;i < patchVal.length;i++) {
    let val = patchVal[i].split('=')
    dropVal(val[0], val[1], i)
  }
}

//Add another parameter
const addAnother = () => {
  let addObj = { "id": ++count.value, "dropdownVal": '', "advTextVal": "" }
  addAnotherArr.value.push(addObj)
}

//Dismiss dropdown
const closeIcon = (id: any) => {
  const findIndex = addAnotherArr.value.findIndex((index: any) => index.id === id)
  addAnotherArr.value.splice(findIndex, 1)
  generateURL()
}

//Show Generated URL
const generateURL = () => {
  if (addAnotherArr.value[0].dropdownVal != '') {
    advString.value = []
    addAnotherArr.value.forEach((ele: any) => {
      let param = "?" + ele.dropdownVal + "=" + ele.advTextVal
      advString.value.push(param)
    })
    generatedURL.value =
      reqDefinition.reqDef.type + "://"
      + reqDefinition.reqDef.host + "/"
      + reqDefinition.reqDef.foreignSource
      + advString.value.join('')
  } else {
    generatedURL.value =
      reqDefinition.reqDef.type + "://"
      + reqDefinition.reqDef.host + "/"
      + reqDefinition.reqDef.foreignSource
  }
}
//Save
const onSave = () => {
    let provisionDService = store.state.configuration.provisionDService['requisition-def'];
    if(provisionDService != undefined){
        //create copy state data
    let copyProvisionData = JSON.parse(JSON.stringify(provisionDService));
    //generate cron_expression
    const cron_expression = saveCronSchedule();
    const paylod =
    {
        'rescan-existing': 'true',
        'import-url-resource': generatedURL.value,
        'import-name': reqDefinition.reqDef.name,
        'cron-schedule': cron_expression
    }
    //Edit record operation
    if (router.currentRoute.value.name === 'reqDefEdit') {
        //Edited data replace with new payload
        copyProvisionData.splice(putDataPosition.value, 1, paylod);
    } else {
        //New record operation
        copyProvisionData.push(paylod);
    }
    const requestPayload = { 'requisition-def': copyProvisionData };
    let response = putProvisionDService(requestPayload);
    try {
        if (response != null) {
            notify({
                title: "Notification",
                text: 'Requisition definition data successfully updated !',
                type: 'success',
            });
            //Route to table and refresh the data
            router.push({ name: 'requisitionDefinitionsLayout' });
            setTimeout(() => {
                location.reload();
            }, 1000);
        }
    } catch {
        notify({
            title: "Notification",
            text: 'ProvisionDService PUT API Error',
            type: 'error',
        });
    }
    }else{
        const cron_expression = saveCronSchedule();
const paylod1 =
    [{
        'rescan-existing': 'true',
        'import-url-resource': generatedURL.value,
        'import-name': reqDefinition.reqDef.name,
        'cron-schedule': cron_expression
    }]
    const requestPayload = { 'requisition-def': paylod1};
    let response = putProvisionDService(requestPayload);
    try {
        if (response != null) {
            notify({
                title: "Notification",
                text: 'Requisition definition data successfully updated !',
                type: 'success',
            });
            //Route to table and refresh the data
            router.push({ name: 'requisitionDefinitionsLayout' });
            setTimeout(() => {
                location.reload();
            }, 1000);
        }
    } catch {
        notify({
            title: "Notification",
            text: 'ProvisionDService PUT API Error',
            type: 'error',
        });
    }
    }
};


const saveCronSchedule = () => {
  let cronSchedule = ['minute', 'hour', 'day of month', 'month', 'day of week']
  let findPosition = cronSchedule.indexOf(reqDefinition.reqDef.schedulePeriod)
  let expression = ['*', '*', '*', '*', '*']
  expression.splice(findPosition, 1, String(reqDefinition.reqDef.schedulePeriodNumber))
  return expression.join(' ')
}

const typeDropdownChange = (val: any) => {
  typeDropDownValue.value = val.name
}

const advanceOptionDropdownChange = (val: any) => {
  advanceOptionDropDownValue.value = val.name
}

const schedulePeriodDropdownChange = (val: any) => {
  schedulePeriodDropDownValue.value =  val.name
}

</script>

<style lang="scss" scoped>
@import "../Common/common.scss";
.title {
  font-size: 18px;
  font-weight: bold;
  text-align: left;
  margin-top: 0;
}
.p-dropdown {
  width: inherit;
}
.width100 {
  width: 100%;
}
.viewDoc {
  float: right;
  font-size: 14px;
  cursor: pointer;
}
.closeBtn {
  direction: rtl;
  margin: 0 0 1% 0;
}
.inputText-margin {
  margin: 2% 0 1% 0;
}
.inline-display {
  display: inline;
}
.inputNumberSection {
  width: 30%;
  height: 30%;
  margin: 1%;
}

.feather-input-container {
  padding-top: 0px;
}
</style>
