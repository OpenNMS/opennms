<template>
    <DataTable
        :loading="loading"
        :value="dataValue"
        :data-key="datakey"
        :responsiveLayout="responsiveLayout"
        :paginator="paginator"
        :rows="rows"
        :rowHover="rowHover"
        v-model:selection="selectedRows"
        :rowsPerPageOptions = "rowsPerPage"
    >
        <template #header v-if="tableHeader">{{ tableHeader }}</template>
        <template #empty>No records found.</template>
        <template #loading>Loading data. Please wait.</template>

        <!-- Below Column is for radio button OR checkbox for rows :: props use selectionMode -->
        <Column v-if="selectionMode" :selectionMode="selectionMode"></Column>
        <Column
            v-for="col of columnDef"
            :field="col.field"
            :header="col.header"
            :key="col.field"
            :sortable="columnSortable"
        ></Column>
        <!-- custom data column  added -->
        <Column v-for="columnName of props.customData">
            <template #body="{ data }">
                <button class="p-button" @click="onClickHandle(columnName, data.id)">{{ columnName }}</button>
            </template>
        </Column>
    </DataTable>
</template>

<script setup lang="ts">

import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import router from '@/router';

const selectedRows: any = ref();
const tableData: any = ref();
const loading: any = ref(true);
const columnDef: any = ref([]);

interface DataTableProps {
    tableHeader?: any
    value: any
    datakey?: string
    responsiveLayout?: string
    paginator?: boolean
    rows?: any
    columnSortable?: boolean
    rowHover?: boolean
    selectionMode?: string  //it can be "multiple" OR "single"
    rowsPerPageOptions?: any
    customData?: any
};

//default values for props
const props = withDefaults(defineProps<DataTableProps>(), {
    datakey: "id",
    responsiveLayout: "scroll",
    paginator: true,
    rows: 10,
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
const onClickHandle = (selectedName: any, id: any) => {
    switch (selectedName) {
        case "edit":
            router.push(`/${selectedName}/${id}`);
            break;
        case "delete":
            confirm(`Please confirm delete id ${id}?`);
            break;
        default:
            alert(`please add logic for ${selectedName}`);
    }
};

</script>