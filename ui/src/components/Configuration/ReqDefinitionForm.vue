<template>
    <p class="title">{{ title }} Requisition Definition</p>
    <div class="p-col-9">
        <form @submit.prevent="onSave">
            <div class="p-fluid">
                <div class="p-field">
                    <label for="name" class="required">Name</label>
                    <InputText
                        id="name"
                        v-model="model.reqDef.name.$model"
                        :class="{ 'p-invalid': model.reqDef.name.$error }"
                    />
                    <ValidationMessage :model="model.reqDef.name"></ValidationMessage>
                </div>

                <div class="p-field">
                    <label for="type" class="required">Type</label>
                    <DropDown
                        v-model="model.reqDef.type.$model"
                        :options="stateTypes"
                        optionLabel="name"
                        optionValue="value"
                        :filter="true"
                    ></DropDown>
                </div>

                <div class="p-field">
                    <label for="host" class="required">Host</label>
                    <InputText
                        id="host"
                        v-model="model.reqDef.host.$model"
                        :class="{ 'p-invalid': model.reqDef.host.$error }"
                        :placeholder="hostPlaceholder"
                    />
                    <ValidationMessage :model="model.reqDef.host"></ValidationMessage>
                </div>

                <div class="p-field">
                    <label for="foreignSource" class="required">Foreign Source</label>
                    <InputText
                        id="foreignSource"
                        v-model="model.reqDef.foreignSource.$model"
                        :class="{ 'p-invalid': model.reqDef.foreignSource.$error }"
                    />
                    <ValidationMessage :model="model.reqDef.foreignSource"></ValidationMessage>
                </div>

                <div class="p-field">
                    <label for="advOps">Advanced Options</label>
                    <div class v-for="add in addAnotherArr">
                        <p class="closeBtn">
                            <Button
                                v-if="add.id !== 0"
                                icon="pi pi-times"
                                style="font-size: 12px;"
                                @click="closeIcon(add.id)"
                                label
                            ></Button>
                        </p>
                        <DropDown
                            v-model="add.dropdownVal"
                            :options="stateAdvancedDropdown"
                            optionLabel="name"
                            optionValue="value"
                        ></DropDown>
                        <p class="inputText-margin">
                            <InputText
                                v-model="add.advTextVal"
                                placeholder="please enter parameter"
                            />
                        </p>
                    </div>
                    <div class="width100">
                        <Button
                            @click="addAnother"
                            label="Add Another"
                            icon="pi pi-plus"
                            style="font-size: 12px;
                             width: fit-content;"
                        ></Button>
                        <a class="viewDoc">View Documentation</a>
                    </div>
                </div>

                <div class="p-field">
                    <p>
                        <b>URL :</b>
                        {{ generatedURL }}
                    </p>
                    <Button
                        :icon="urlIcon"
                        :label="urlBtnTitle"
                        @click="generateURL"
                        :disabled="model.reqDef.type.$invalid || model.reqDef.host.$invalid || model.reqDef.foreignSource.$invalid"
                    ></Button>
                </div>

                <div class="p-field">
                    <label for="type" class="required">Schedule Period</label>
                    <DropDown
                        v-model="model.reqDef.schedulePeriod.$model"
                        :options="stateSchedulePeriod"
                        optionLabel="name"
                        optionValue="value"
                        :filter="true"
                    ></DropDown>
                    <p class="p-field p-col-6">
                        Every
                        <span>
                            <InputNumber
                                class="inputNumberSection"
                                showButtons
                                :min="minVal"
                                v-model="model.reqDef.schedulePeriodNumber.$model"
                            />
                        </span>
                        {{ model.reqDef.schedulePeriod.$model }}
                    </p>
                </div>

                <div class="p-field p-col-2">
                    <Button
                        label="Save"
                        icon="pi pi-save"
                        type="submit"
                        :disabled="model.reqDef.$invalid"
                    ></Button>
                </div>
            </div>
        </form>
    </div>
</template>

<script setup lang="ts">

import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import InputText from '../Common/InputText.vue'
import DropDown from '../Common/DropDown.vue'
import Button from '../Common/Button.vue'
import InputNumber from '../Common/InputNumber.vue'
import State from './formState'
import ValidationMessage from '../Common/ValidationMessage.vue'
import router from '@/router'

const store = useStore();
const reqDefinition = reactive(State);

const minVal = ref(1);
const count = ref(0);
const addAnotherArr = ref([{ "id": count.value, "dropdownVal": '', "advTextVal": '' }]);

const urlBtnTitle = ref('Generate URL');
const generatedURL = ref('');
const advString: any = ref([]);

const urlIcon = ref('pi pi-check-circle');

const hostPlaceholder = ref('(0-255).(0-255).(0-255).(0-255)');

const model = State.toModel();

const props = defineProps({
    title: {
        type: String,
        default: "New"
    }
});

// Dropdown API Data
onMounted(async () => {
    try {
        // Types
        await store.dispatch('configuration/getDropdownTypes');
        // Schedule Period
        await store.dispatch('configuration/getSchedulePeriod');
        // Advanced Dropdown
        await store.dispatch('configuration/getAdvancedDropdown');

        // Edit Operation logic
        if (router.currentRoute.value.name === 'reqDefEdit') {

            let data = store.state.configuration.sendModifiedData;
            let url = data['import-url-resource'].split('/');

            //temp logic to patch schedule period value
            let cron_schedule = data['cron-schedule'];
            reqDefinition.reqDef.schedulePeriodNumber = parseInt(cron_schedule.match(/\d+/)[0]);
            reqDefinition.reqDef.schedulePeriod = "minute"
            //temp logic ends

            reqDefinition.reqDef.name = data['import-name'];
            reqDefinition.reqDef.type = url[0].split(':')[0];
            reqDefinition.reqDef.host = url[2];
            generatedURL.value = data['import-url-resource'];

            let patchVal = url[3].split('?');
            reqDefinition.reqDef.foreignSource = patchVal[0];

            //add edit data value to advance dropdown
            const dropVal = (dropdownVal: any, advTextVal: any, index: any) => {
                if (index == 1) {
                    addAnotherArr.value[0]['dropdownVal'] = dropdownVal;
                    addAnotherArr.value[0]['advTextVal'] = advTextVal;
                } else {
                    let addObj = { "id": index - 1, "dropdownVal": dropdownVal, "advTextVal": advTextVal };
                    addAnotherArr.value.push(addObj);
                }
            }

            //Identify how many advance parameter
            for (let i = 1; i < patchVal.length; i++) {
                let val = patchVal[i].split('=');
                dropVal(val[0], val[1], i);
            }

        } else {
            reqDefinition.reqDef.name = '';
            reqDefinition.reqDef.type = '';
            reqDefinition.reqDef.host = '';
            reqDefinition.reqDef.foreignSource = '';
        }
    } catch {
        console.error("Error in API/Logic");
    }
});

const stateTypes = computed(() => {
    return store.state.configuration.types
});

const stateSchedulePeriod = computed(() => {
    return store.state.configuration.schedulePeriod
});

const stateAdvancedDropdown = computed(() => {
    return store.state.configuration.advancedDropdown
});

//Add another parameter
const addAnother = () => {
    let addObj = { "id": ++count.value, "dropdownVal": '', "advTextVal": "" };
    addAnotherArr.value.push(addObj);
};

//Dismiss dropdown
const closeIcon = (id: any) => {
    const findIndex = addAnotherArr.value.findIndex((index: any) => index.id === id);
    addAnotherArr.value.splice(findIndex, 1);
};

//Show Generated URL
const generateURL = () => {
    urlIcon.value = 'pi pi-refresh';
    urlBtnTitle.value = 'Refresh URL';

    if (addAnotherArr.value[0].dropdownVal != '') {
        advString.value = [];
        addAnotherArr.value.forEach((ele: any) => {
            let param = "?" + ele.dropdownVal + "=" + ele.advTextVal;
            advString.value.push(param);
        });
        generatedURL.value =
            reqDefinition.reqDef.type + "://"
            + reqDefinition.reqDef.host + "/"
            + reqDefinition.reqDef.foreignSource
            + advString.value.join('');
    } else {
        generatedURL.value =
            reqDefinition.reqDef.type + "://"
            + reqDefinition.reqDef.host + "/"
            + reqDefinition.reqDef.foreignSource
    }
}

//Save 
const onSave = () => {
    console.log("generatedURL", generatedURL.value);
    console.log('onSave Obj', JSON.stringify(reqDefinition));
};

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
</style>