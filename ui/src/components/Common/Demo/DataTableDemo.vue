<template>
    <div>
        <DataTable tableHeader="Node Table" :value="nodeDataValue" :customData="customData" />
    </div>
</template>

<script setup lang="ts">

import { onMounted, ref } from 'vue';
import nodeData from '../Demo/apiService';
import DataTable from '../DataTable.vue';

let nodeDataValue:any = ref([]);
let customData = ['edit','delete']; //custome data

onMounted(() => {
    //service call for data
    nodeData.then((response) => {
        //data come form api
        let dataLen = response.data.response.length;
        if (dataLen > 0) {
            let data = response.data.response
            nodeDataValue.value = data;
        }
    }).catch((err) => {
        console.error("error ==>", err);
    });
})

</script>