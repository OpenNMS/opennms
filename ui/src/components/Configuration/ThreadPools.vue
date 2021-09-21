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
          mode="decimal"
          v-model="validationVar.rescan.$model"
          :class="{ 'p-invalid': validationVar.rescan?.$errors[0] }"
        />
        <p class="p-error">{{ validationVar.rescan?.$errors[0]?.$message }}</p>
      </div>
      <div class="p-field">
        <label for="write" class="required">Write</label>
        <InputNumber
          mode="decimal"
          v-model="validationVar.write.$model"
          :class="{ 'p-invalid': validationVar.write?.$errors[0] }"
        />
        <p class="p-error">{{ validationVar.write?.$errors[0]?.$message }}</p>
      </div>
      <div class="p-field p-col-2">
        <Button
          label="Save"
          icon="pi pi-save"
          :disabled="validationVar.$invalid"
          @click="onchange()"
        ></Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, toRef } from 'vue'
import InputNumber from '../Common/InputNumber.vue'
import Button from '../Common/Button.vue'
import useVuelidate from '@vuelidate/core'
import { required, minValue, numeric, maxValue } from '@vuelidate/validators'

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
  scan: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) },
  rescan: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) },
  write: { required, numeric, minValue: minValue(0), maxValue: maxValue(10) }
};

const validationVar = useVuelidate(rules, {
  import: toRef(threadpool, "import"),
  scan: toRef(threadpool, "scan"),
  rescan: toRef(threadpool, "rescan"),
  write: toRef(threadpool, "write")
});

const onchange = () => {
  validationVar.value.$touch();
  if (validationVar.value.$invalid) return;
}

</script>

<style lang="scss" scoped>
@import "../Common/common.scss";
</style>
