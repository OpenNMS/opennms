<template>
  <div class="container">
    <div class="btnAction">
      <FeatherButton primary @click="clickAction(index)">
        <template v-slot:icon>
          <FeatherIcon :icon="icon" aria-hidden="true" focusable="false"></FeatherIcon>
        </template>
        {{ buttonAction[index] }}
      </FeatherButton>
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
import { FeatherButton }   from '@featherds/button'
import { FeatherIcon }   from '@featherds/icon'
import actionsAdd from "@featherds/icon/actions/Add";
import navigationArrowBack from "@featherds/icon/navigation/ArrowBack";
import { markRaw } from "vue";
import DataTableDemo from '../Common/Demo/DataTableDemo.vue'
import ReqDefinitionForm from './ReqDefinitionForm.vue'

const buttonAction = ref(['ADD NEW', 'BACK'])
const index = ref(0)
const icon = ref(markRaw(actionsAdd));

const clickAction = (val: any) => {
  switch (val) {
    case 0:
      index.value = 1
      icon.value = markRaw(navigationArrowBack);
      break
    case 1:
      index.value = 0
      icon.value = markRaw(actionsAdd);
      break
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
