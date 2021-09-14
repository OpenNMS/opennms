<template>
    <div class="container">
        <p class="title">New Requisition Definition</p>
        <div class="form">
            <div class="p-fluid">
                <div class="p-field">
                    <label for="name" class="required">Name</label>
                    <InputText id="name" v-model="nameVal" />
                </div>

                <div class="p-field">
                    <label for="type" class="required">Type</label>
                    <DropDown
                        v-model="selectedType"
                        :options="types"
                        optionLabel="name"
                        optionValue="value"
                        :filter="true"
                    ></DropDown>
                </div>

                <div class="p-field">
                    <label for="host" class="required">Host</label>
                    <InputText id="host" v-model="hostVal" />
                </div>

                <div class="p-field">
                    <label for="foreignSource" class="required">Foreign Source</label>
                    <InputText id="foreignSource" v-model="foreignSourceVal" />
                </div>

                <div class="p-field">
                    <label for="advOps">Advanced Options</label>
                    <div class="inline-display" v-for="add in addAnotherArr">
                        <span
                            v-if="add.id !== 0"
                            class="pi pi-times width100 align-R"
                            style="font-size: 12px;"
                            @click="closeIcon(add.id)"
                        ></span>
                        <DropDown
                            :id="add.id"
                            v-model="add.dropdownVal"
                            :options="advancedDropdown"
                            optionLabel="name"
                            optionValue="value"
                            :filter="true"
                        ></DropDown>
                        <p>
                            <InputText
                                v-model="add.advTextVal"
                                @focusout="advParam(add.dropdownVal, add.advTextVal)"
                            />
                        </p>
                    </div>
                    <label class="width100">
                        <span @click="addAnother">
                            <i class="pi pi-plus" style="font-size: 12px;margin: 2px;"></i>Add Another
                        </span>
                        <a class="viewDoc">View Documentation</a>
                    </label>
                </div>

                <div class="p-field">
                    <p class="generatedUrl">URL : {{ generatedURL }}</p>
                    <Button label="Generate URL" @click="generateURL" :disabled="disableBtn"></Button>
                </div>

                <div class="p-field">
                    <label for="type" class="required">Schedule Period</label>
                    <DropDown
                        v-model="selectedPeriod"
                        :options="schedulePeriod"
                        optionLabel="name"
                        optionValue="value"
                        :filter="true"
                    ></DropDown>
                    <p>
                        Every
                        <span>
                            <InputNumber
                                class="inputNumberSection"
                                showButtons
                                v-model="selectedInputNumber"
                                :min="minVal"
                            />
                        </span>
                        {{ selectedPeriod }}
                    </p>
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
import InputNumber from '../Common/InputNumber.vue'

const minVal = ref(1);
const disableBtn = ref(true);

const nameVal = ref('');
const hostVal = ref('');
const foreignSourceVal = ref('');

const selectedType = ref('');
const selectedPeriod = ref('');
const selectedInputNumber = ref('');

const types: any = ref([]);
const schedulePeriod: any = ref([]);
const advancedDropdown: any = ref([]);
const count = ref(0);
const addAnotherArr: any = ref([{ "id": count.value, "dropdownVal": `selectedAdvDropdown${count.value}`, "advTextVal": '' }]);

const paramUrl = ref('');
const generatedURL = ref('');

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

const addAnother = () => {
    console.log('add another clicked');
    if (addAnotherArr.value.length < 5) {
        let counterVal = ++count.value;
        let addObj = { "id": counterVal, "dropdownVal": `selectedAdvDropdown${counterVal}`, "advTextVal": `Enter param ${counterVal}` };
        addAnotherArr.value.push(addObj);
        console.log("Add ID addAnotherArr", addAnotherArr.value);
    } else {
        alert(`Max allowed limit is ${count.value}`);
    }

}

const closeIcon = (id: any) => {
    const findIndex = addAnotherArr.value.findIndex((index: any) => index.id === id);
    console.log("Remove ID ::", id, findIndex);
    addAnotherArr.value.splice(findIndex, 1);
    console.log("Remove ID from addAnotherArr ::", addAnotherArr.value);
}

const advParam = (paramDrop: any, paramText: any) => {
    console.log(paramDrop, paramText);
    let text = paramText;
    if (text === '') {
        paramUrl.value += `?${paramDrop}=${text}`;
    } else {
        console.log(text)
        paramUrl.value += `?${paramDrop}=${text}`;
    }
}

const generateURL = () => {
    let mandateCondition = nameVal.value !== '' && hostVal.value !== '' && foreignSourceVal.value !== '' && selectedType.value !== '';
    if (mandateCondition) {
        disableBtn.value = false;
    } else {
        disableBtn.value = true;
    }
}

const onSave = () => {
    console.log("add another array value", addAnotherArr.value);
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

.inputNumberSection {
    width: 30%;
    height: 30%;
    margin: 1%;
}

.required:after {
    content: " *";
    color: red;
}
</style>