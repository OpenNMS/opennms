<template>
    <div class="container">
        <div class="btnAction">
            <Button :label="buttonAction[index]" @click="clickAction(index)"></Button>
        </div>
        <div class="reqDef">
            <div class="dataTable" v-if="index === 0">
                <dataTableDemo></dataTableDemo>
            </div>
            <div v-else>
                <reqDefinitionFrom></reqDefinitionFrom>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">

import { ref } from 'vue'
import Button from './Common/Button.vue'
import dataTableDemo from './Common/Demo/DataTableDemo.vue'
import reqDefinitionFrom from './ReqDefinitionForm.vue'

const buttonAction = ref(['ADD NEW', 'BACK']);
const index = ref(0);

const clickAction = (val: any) => {
    switch (val) {
        case 0:
            index.value = 1;
            break;
        case 1:
            index.value = 0;
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
}
</style>