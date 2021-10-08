<template>
    <div>
        <DataTable
            :value="nodeDataValue"
            :customData="customData"
            :rowsPerPageOptions="[5, 3, 1]"
            :paginator="isData"
        />
    </div>
</template>

<script setup lang="ts">

import { onMounted, ref } from 'vue';
import { nodeData, tableMockData } from '../Demo/apiService';
import DataTable from '../DataTable.vue';

const isData = ref(false);
let nodeDataValue: any = ref([]);
let customData: any = ref([]); //custom data


onMounted(() => {
    //service call for data
    // nodeData.then((response: any) => {
    //     //data come form api
    //     let dataLen = response.data.response.length;
    //     if (dataLen > 0) {
    //         nodeDataValue.value = response.data.response;
    //     }
    // }).catch((err) => {
    //     console.error("error ==>", err);
    // });

    tableMockData.then((response: any) => {
        //data come form api
        let data = response['data']["requisition-def"];
        if (data && data.length > 1) {
            nodeDataValue.value = response['data']["requisition-def"];
            customData.value = ['edit', 'delete'];
            isData.value = true;
        }

    }).catch((err) => {
        console.error("error ==>", err);
    });
})

</script>