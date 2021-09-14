<template>
    <div class="container">
        <p class="title">New Requisition Definition</p>
        <div class="card">
            <div class="p-fluid">
                <div class="p-field">
                    <label for="name">Name</label>
                    <InputText id="name" v-model="nameVal" />
                </div>
                <div class="p-field">
                    <label for="type">Type</label>
                    <DropDown
                        v-model="selectedType"
                        :options="types"
                        optionLabel="name"
                        optionValue="value"
                        :filter="true"
                    ></DropDown>
                </div>
                <div class="p-field">
                    <label for="host">Host</label>
                    <InputText id="host" v-model="hostVal" />
                </div>
                <div class="p-field">
                    <label for="foreignSource">Foreign Source</label>
                    <InputText id="foreignSource" v-model="foreignSourceVal" />
                </div>
                <div class="p-field">
                    <label for="advOps">Advanced Options</label>

                    <div class="inline-display" v-for="(add,index) in addAnotherArr">
                        <span
                            v-if="add.id !== 0"
                            class="pi pi-times width100 align-R"
                            @click="closeIcon(add.id)"
                        ></span>
                        <DropDown
                            :id="add.id"
                            v-model="add.name"
                            :options="advancedDropdown"
                            optionLabel="name"
                            optionValue="value"
                            :filter="true"
                        ></DropDown>
                        <p>
                            <InputText v-model="add.advText" />
                        </p>
                    </div>

                    <!-- <DropDown
                        id="0"
                        v-model="selectedAdvDropdown"
                        :options="advancedDropdown"
                        optionLabel="name"
                        optionValue="value"
                        :filter="true"
                    ></DropDown>
                    <p>
                        <InputText id="advDropdownText" v-model="advDropdownText" />
                    </p>
                    <div
                        id="addDrop"
                        ref="addDrop"
                        v-for="(add, index) in addAnotherArr"
                        :key="add.id"
                    >
                        <div v-html="add.value"></div>
                    </div>-->

                    <label class="width100">
                        <span @click="addAnother">Add Another</span>
                        <span class="viewDoc">View Documentation</span>
                    </label>
                </div>
                <div class="p-field">
                    <label for="url">URL</label>
                    <p
                        class="generatedUrl"
                    >{{ selectedType + '://' + hostVal + '/' + foreignSourceVal }}</p>
                </div>
                <div class="p-field">
                    <label for="type">Schedule Period</label>
                    <DropDown
                        v-model="selectedPeriod"
                        :options="schedulePeriod"
                        optionLabel="name"
                        optionValue="name"
                        :filter="true"
                    ></DropDown>
                    <p>Display selection</p>
                </div>
                <div class="p-field">
                    <Button label="Save" icon="pi pi-save" @click="onSave"></Button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">

import { onMounted, ref } from 'vue'
import { apiTypes, apiPeriod, apiAdvDropdown } from '../Common/Demo/apiService'
import InputText from '../Common/InputText.vue'
import DropDown from '../Common/DropDown.vue'
import Button from '../Common/Button.vue'

const addDrop = ref('');
const runTimeHtml = ref('');

const nameVal = ref('');
const hostVal = ref('');
const foreignSourceVal = ref('');

// const url = ref(`${nameVal.value}://${hostVal.value}/${foreignSourceVal.value}`);
// const url = ref('');
// url.value = nameVal.value + '://' + hostVal.value + '/' + foreignSourceVal.value

const selectedType = ref('');
const selectedAdvDropdown0 = ref('');
const selectedAdvDropdown1 = ref('');
const advDropdownText = ref('');
const selectedPeriod = ref('');
const types: any = ref([]);
const schedulePeriod: any = ref([]);
const advancedDropdown: any = ref([]);
const count = ref(0);
const addAnotherArr: any = ref([{ "id": count.value, "name": `selectedAdvDropdown${count.value}`, "advText": '' }]);

onMounted(() => {
    //service call for data
    try {

        //Types
        apiTypes.then((response: any) => {
            //data come form api
            let dataLen = response.data.length;
            if (dataLen > 0) {
                types.value = response.data;
            }
        })
            .catch((err: any) => {
                console.error("apiTypes Error ==>", err);
            });

        // Schedule Period
        apiPeriod.then((response: any) => {
            //data come form api
            let dataLen = response.data.length;
            if (dataLen > 0) {
                schedulePeriod.value = response.data;
            }
        })
            .catch((err: any) => {
                console.error("apiPeriod Error ==>", err);
            });

        // Advanced Dropdown
        apiAdvDropdown.then((response: any) => {
            //data come form api
            let dataLen = response.data.length;
            if (dataLen > 0) {
                advancedDropdown.value = response.data;
            }
        })
            .catch((err: any) => {
                console.error("apiAdvDropdown Error ==>", err);
            });

    } catch {
        console.error("Error in API")
    }
});

const advDropdownText0 = ref('');
const advDropdownText1 = ref('');

const addAnother = () => {
    console.log('add another clicked');
    let counterVal = ++count.value;
    let addObj = { "id": counterVal, "name": `selectedAdvDropdown${counterVal}`, "advText": `Enter param ${counterVal}` };
    addAnotherArr.value.push(addObj);
    console.log("Add ID addAnotherArr", addAnotherArr.value);

    // console.log("v-model", selectedAdvDropdown0.value, "ref", advDropdownText0.value);
    // console.log("v-model", selectedAdvDropdown1.value, "ref", advDropdownText1.value);
    // const runTimeHtml =
    //     `<span class="pi pi-times width100 align-R" @click="closeIcon(${counterVal})"></span>
    //  <DropDown
    //  :id="${counterVal}"
    //  v-model=selectedAdvDropdown${counterVal}
    //  :options="advancedDropdown"
    //   optionLabel="name"
    //  optionValue="value"
    // :filter="true"
    // ></DropDown>
    // <p> <InputText id="advDropdownText" v-model="advDropdownText${counterVal}" /> </p>`

    // console.log(runTimeHtml);
    // addAnotherArr.value.push({ "id": counterVal, "value": runTimeHtml });
    // console.log("Add ID addAnotherArr", addAnotherArr.value);
    // addDrop[innerHTML] = runTimeHtml;
}

const closeIcon = (id: any) => {
    const findIndex = addAnotherArr.value.findIndex((index: any) => index.id === id);
    console.log("Remove ID ::", id, findIndex);
    addAnotherArr.value.splice(findIndex, 1);
    console.log("Remove ID from addAnotherArr ::", addAnotherArr.value);
}

const onSave = () => {
    // console.log(
    //     `{
    //     Name: ${nameVal.value},
    //     Type:${selectedType.value}
    //     Host: ${hostVal.value},
    //     Foreign Source:${foreignSourceVal.value},
    //     advDropdownVal:{
    //         Dropdwon: ${selectedAdvDropdown.value},
    //         DropdownText:${advDropdownText.value}
    //     },
    //     Schedule Period:${selectedPeriod.value}
    //     }
    //     `
    // )
}

</script>

<style lang="scss" scoped>
$title-font: 18px;
.title {
    font-size: $title-font;
    font-weight: bold;
    text-align: left;
}

.p-dropdown {
    width: inherit;
}

.width100 {
    width: 100%;
}

.viewDoc {
    float: right;
}

.generatedUrl {
    margin-top: 0;
}

.inline-display {
    display: inline;
}

.align-R {
    text-align: right;
}
</style>