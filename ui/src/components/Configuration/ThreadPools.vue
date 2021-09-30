<template>
  <div class="p-col-9">
    <div class="p-fluid">
      <div class="p-field">
        <label for="import" class="required">Import</label>
        <InputNumber
          id="import"
          mode="decimal"
          v-model="validationVar.import.$model"
          :class="{ 'p-invalid': validationVar.import?.$errors[0] }"
        />
        <p class="p-error">{{ validationVar.import?.$errors[0]?.$message }}</p>
      </div>
      <div class="p-field">
        <label for="scan" class="required">Scan</label>
        <InputNumber
          id="scan"
          mode="decimal"
          v-model="validationVar.scan.$model"
          :class="{ 'p-invalid': validationVar.scan?.$errors[0] }"
        />
        <p class="p-error">{{ validationVar.scan?.$errors[0]?.$message }}</p>
      </div>

      <div class="p-field">
        <label for="rescan" class="required">Rescan</label>
        <InputNumber
          id="rescan"
          mode="decimal"
          v-model="validationVar.rescan.$model"
          :class="{ 'p-invalid': validationVar.rescan?.$errors[0] }"
        />
        <p class="p-error">{{ validationVar.rescan?.$errors[0]?.$message }}</p>
      </div>
      <div class="p-field">
        <label for="write" class="required">Write</label>
        <InputNumber
          id="write"
          mode="decimal"
          v-model="validationVar.write.$model"
          :class="{ 'p-invalid': validationVar.write?.$errors[0] }"
        />
        <p class="p-error">{{ validationVar.write?.$errors[0]?.$message }}</p>
      </div>
      <div class="p-field p-col-2">
        <Button icon="pi pi-save" label="Save" :disabled="validationVar.$invalid" @click="onSave()"></Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, toRef, ref } from 'vue'
import InputNumber from '../Common/InputNumber.vue'
import Button from '../Common/Button.vue'
import useVuelidate from '@vuelidate/core'
import { required, minValue, numeric, maxValue } from '@vuelidate/validators'
import { apigetProvisionD } from '../Common/Demo/apiService';


const threadpool = reactive({
  import: 0,
  scan: 0,
  rescan: 0,
  write: 0
});

const rules = {
  import: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) },
  scan: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) },
  rescan: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) },
  write: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) }
};

let validationVar = useVuelidate(rules, {
  import: toRef(threadpool, "import"),
  scan: toRef(threadpool, "scan"),
  rescan: toRef(threadpool, "rescan"),
  write: toRef(threadpool, "write")
});
let mainObj= ref('');
const onSave = () => {  
    threadpool.import = validationVar.value.import.$model;
    threadpool.scan = validationVar.value.scan.$model;
    threadpool.rescan = validationVar.value.rescan.$model;
    threadpool.write = validationVar.value.write.$model; 
}
onMounted(async () => {
    //service call for data
    await apigetProvisionD.then((response: any) => {
        //data come form api
        if (response.status == 200) {
            validationVar.value.import.$model = response.data.importThreads;
            validationVar.value.scan.$model = response.data.scanThreads;
            validationVar.value.rescan.$model = response.data.rescanThreads;
            validationVar.value.write.$model = response.data.writeThreads; 
            console.log('ProvisionD API data', response);
        }
    }).catch((err) => {
        console.error("error ==>", err);
    });
})
</script>

<style lang="scss" scoped>
@import "../Common/common.scss";
</style>
