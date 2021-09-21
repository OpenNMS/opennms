<template>
    <p class="title">New Requisition Definition</p>
    <div class="p-col-9">
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
                    <div class v-for="add in addAnotherArr">
                        <p style="direction: rtl;margin: 0 0 1% 0;">
                            <Button
                                v-if="add.id !== 0"
                                icon="pi pi-times"
                                style="font-size: 12px;"
                                @click="closeIcon(add.id)"
                                label
                            ></Button>
                        </p>
                        <DropDown
                            :id="add.id"
                            v-model="add.dropdownVal"
                            :options="advancedDropdown"
                            optionLabel="name"
                            optionValue="value"
                            :filter="true"
                        ></DropDown>
                        <p style="margin:2% 0 1% 0;">
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
                    <p>URL</p>
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
                    <p class="p-field p-col-6">
                        Every
                        <span>
                            <InputNumber
                                class="inputNumberSection"
                                showButtons
                                :min="minVal"
                                v-model="selectedInputNumber"
                            />
                        </span>
                        {{ selectedPeriod }}
                    </p>
                </div>

                <div class="p-field p-col-2">
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

const nameVal = ref('');
const hostVal = ref('');
const foreignSourceVal = ref('');

const selectedType = ref('');
const selectedPeriod = ref('');
const selectedInputNumber = ref();

const types: any = ref([]);
const schedulePeriod: any = ref([]);
const advancedDropdown: any = ref([]);

const minVal = ref(1);
const count = ref(0);
const addAnotherArr: any = ref([{ "id": count.value, "dropdownVal": '', "advTextVal": '' }]);

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
        console.error("Error in API");
    }
});

const addAnother = () => {
    if (addAnotherArr.value.length < 5) {
        let addObj = { "id": ++count.value, "dropdownVal": '', "advTextVal": "" };
        addAnotherArr.value.push(addObj);
    } else {
        alert(`Max allowed param is ${addAnotherArr.value.length - 1}`);
    }
}

const closeIcon = (id: any) => {
    const findIndex = addAnotherArr.value.findIndex((index: any) => index.id === id);
    addAnotherArr.value.splice(findIndex, 1);
}

const onSave = () => {

}

</script>

<style lang="scss" scoped>
@import "../Common/common.scss";

$title-font: 18px;
.title {
    font-size: $title-font;
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

.inline-display {
    display: inline;
}

.inputNumberSection {
    width: 30%;
    height: 30%;
    margin: 1%;
}
</style>