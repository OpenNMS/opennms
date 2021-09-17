<template>
    <div class="container">
        <div class="btnAction">
            <Button :icon="icon" :label="buttonAction[index]" @click="clickAction(index)"></Button>
        </div>
        <div class="reqDef">
            <div class="dataTable" v-if="index === 0">
                <DataTableDemo></DataTableDemo>
            </div>
            <div v-else>
                <ReqDefinitionForm></ReqDefinitionForm>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">

import { ref } from 'vue'
import Button from '../Common/Button.vue'
import DataTableDemo from '../Common/Demo/DataTableDemo.vue'
import ReqDefinitionForm from './ReqDefinitionForm.vue'

const buttonAction = ref(['ADD NEW', 'BACK']);
const index = ref(0);
const icon = ref('pi pi-plus');

const clickAction = (val: any) => {
    switch (val) {
        case 0:
            index.value = 1;
            icon.value = 'pi pi-arrow-left';
            break;
        case 1:
            index.value = 0;
            icon.value = 'pi pi-plus';
            break;
    }
}


</script>

<style scoped>
.btnAction {
    margin: 1% 0;
    float: right;
}

.reqDef {
    display: inline-block;
    width: 100%;
}
</style>