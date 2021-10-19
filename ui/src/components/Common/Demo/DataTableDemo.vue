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

import { onMounted, ref, computed } from 'vue';
import DataTable from '../DataTable.vue';
import { useStore } from 'vuex';
import cronstrue from 'cronstrue';

const store = useStore();
const isData = ref(false);
let customData: any = ref([]); //custom data

const provisionDService = computed(() => { return store.state.configuration.provisionDService });

const nodeDataValue = computed(() => {
    if (provisionDService.value) {
        let copyState = [];
        copyState = JSON.parse(JSON.stringify(provisionDService.value));
        let data = (copyState as any)["requisition-def"];
        if (data && data.length > 1) {
            customData.value = ['edit', 'delete'];
            isData.value = true; //show pagination 
            // cron-schedule expression changed to human readable format 
            const copydata = data.filter((rowData: any) => {
                return rowData['cron-schedule'] = cronstrue.toString(rowData['cron-schedule'], { use24HourTimeFormat: true });
            });
            return copydata;
        }
    }
    return [];
})

onMounted(async () => {
    try {
        await store.dispatch('configuration/getProvisionDService');
    } catch {
        console.error("Error in API - Inside datatableDemo");
    }
});


</script>