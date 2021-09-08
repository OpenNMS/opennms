<template>
    <div>
        <PrimeVueDataTable
            :showGridlines="showGridlines"
            :loading="loading"
            :value="dataValue"
            :data-key="datakey"
            :responsiveLayout="responsiveLayout"
            :paginator="paginator"
            :rows="rows"
            :rowHover="rowHover"
            v-model:selection="selectedRows"
            :rowsPerPageOptions="rowsPerPage"
        >
            <template #header v-if="tableHeader">{{ tableHeader }}</template>
            <template #empty>No records found.</template>
            <template #loading>Loading data. Please wait.</template>

            <!-- Below Column is for radio button OR checkbox for rows :: props can be use -> selectionMode -->
            <Column v-if="selectionMode" :selectionMode="selectionMode"></Column>
            <Column
                v-for="col of columnDef"
                :field="col.field"
                :header="col.header"
                :key="col.field"
                :sortable="columnSortable"
            ></Column>
            <!-- custom data column added -->
            <Column v-for="columnName of props.customData">
                <template #body="{ data }">
                    <Button :label="columnName" @click="onClickHandle(columnName, data.id)"></Button>
                </template>
            </Column>
        </PrimeVueDataTable>
    </div>
</template>

<script setup lang="ts">

import { computed, ref } from 'vue';
import PrimeVueDataTable from 'primevue/datatable';
import Column from 'primevue/column';
import router from '@/router';
import Button from "./Button.vue";

const selectedRows: any = ref();
const tableData: any = ref();
const loading: any = ref(true);
const columnDef: any = ref([]);

interface DataTableProps {
    tableHeader?: any
    showGridlines?: boolean //this is for gridline in table
    value: any
    datakey?: string
    responsiveLayout?: string //valid options are "stack" and "scroll".
    paginator?: boolean
    rows?: any
    columnSortable?: boolean
    rowHover?: boolean
    selectionMode?: string  //valid options are "multiple" OR "single"
    rowsPerPageOptions?: any
    customData?: any
};

//default values for props
const props = withDefaults(defineProps<DataTableProps>(), {
    showGridlines: false,
    datakey: "id",
    responsiveLayout: "scroll",
    paginator: true,
    rows: 5,
    columnSortable: true,
    rowHover: true,
});

//computed for value - display object
const dataValue = computed(() => {
    tableData.value = props.value;
    findColumn(tableData.value);
    return props.value;
});

const findColumn = (data: any) => {
    let responseData = data[0];
    //responseData can not be null | undefined
    if (responseData) {
        let columnName = Object.keys(responseData);
        columnName.forEach((colData) => {
            //column def values
            const Obj = { "field": colData, "header": colData.toUpperCase(), "key": colData };
            columnDef.value.push(Obj);
        });
        loading.value = false; //loading stops
    }
};

//computed for rowsPerPage
const rowsPerPage = computed(() => {
    return props.rowsPerPageOptions;
});

//decision for routing
const onClickHandle = (selectedName: any, paramId: any) => {
    switch (selectedName) {
        case "edit":
            router.push({ path: `/dataTableDemo/${selectedName}/${paramId}` });
            break;
        case "delete":
            confirm(`Please confirm delete id ${paramId}?`);
            break;
        default:
            alert(`please add logic for ${selectedName}`);
    }
};

</script>