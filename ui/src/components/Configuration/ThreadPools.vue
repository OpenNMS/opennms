<template>
  <br />
    <label class="p-col-9" for="import">Import</label>
    <br />
    <InputNumber
      id="import"
      class="p-col-9"
      mode="decimal"
      v-model="validationVar.import.$model" 
      :class="{ 'p-invalid' : validationVar.import?.$errors[0]}"
    />
    <p class="p-error">{{ validationVar.import?.$errors[0]?.$message }}</p>
    
    <br />

    <label class="p-col-9" for="scan">Scan</label>
    <br />
    <InputNumber
      id="scan"
      class="p-col-9"
      mode="decimal"
      v-model="validationVar.scan.$model"
      :class="{ 'p-invalid' : validationVar.scan?.$errors[0]}"
    />
    <p class="p-error">{{ validationVar.scan?.$errors[0]?.$message }}</p>
   
    <br />

    <label class="p-col-9" for="rescan">Rescan</label>
    <br />
    <InputNumber
    class="p-col-9"
      mode="decimal"
      v-model="validationVar.rescan.$model" 
      :class="{ 'p-invalid' : validationVar.rescan?.$errors[0]}"
    />
    <p class="p-error">{{ validationVar.rescan?.$errors[0]?.$message }}</p>
    
    <br />

    <label class="p-col-9" for="write">Write</label>
    <br />
    <InputNumber
    class="p-col-9"
      mode="decimal"      
      v-model="validationVar.write.$model" 
      :class="{ 'p-invalid' : validationVar.write?.$errors[0]}"
    />
    <p  class="p-error">{{ validationVar.write?.$errors[0]?.$message }}</p>
    
    <br />
  <br />
  <Button label="Save" :disabled="validationVar.$invalid" @click="onchange()"></Button>
</template>

<script setup lang="ts">
import useVuelidate from '@vuelidate/core'
import { required, minValue,  numeric, maxValue} from '@vuelidate/validators'
import {reactive, toRef} from 'vue'
import InputNumber from '../Common/InputNumber.vue'
import Button from '../Common/Button.vue'

interface ThreadPoolProps {
  import?: number,
  scan?: number,
  rescan?: number,
  write?: number
}

const threadpool = reactive({
  import: 0,
  scan: 0,
  rescan: 0,
  write: 0
});
const rules = {
  import: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) },
  scan: { required, numeric,minValue: minValue(0), maxValue: maxValue(10) },
  rescan: { required, numeric, minValue: minValue(0), maxValue: maxValue(10)},
  write: { required, numeric, minValue: minValue(0), maxValue: maxValue(10)}
};

const validationVar = useVuelidate(rules, {
  import: toRef(threadpool, "import"),
  scan: toRef(threadpool, "scan"),
  rescan: toRef(threadpool, "rescan"),
  write: toRef(threadpool, "write")
});


const onchange =() => {
  validationVar.value.$touch();
  if (validationVar.value.$invalid) return;
}
</script>