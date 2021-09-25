<template>
    <div>
        <DataTable :value="nodeDataValue" :customData="customData" :rowsPerPageOptions="[5, 3, 1]" />
    </div>
</template>

<script setup lang="ts">

import { onMounted, ref } from 'vue';
import { nodeData } from '../Demo/apiService';
import DataTable from '../DataTable.vue';

let nodeDataValue: any = ref([]);
let customData = ['edit', 'delete']; //custom data

onMounted(() => {
    //service call for data
    nodeData.then((response: any) => {
        //data come form api
        let dataLen = response.data.response.length;
        if (dataLen > 0) {
            nodeDataValue.value = response.data.response;
        }
    }).catch((err) => {
        console.error("error ==>", err);
    });
})

</script>