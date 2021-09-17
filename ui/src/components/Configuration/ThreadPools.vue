<template>
  <div class="p-col-9">
    <div class="p-fluid">
      <div class="p-field">
        <label for="import" class="required">Import</label>
        <InputNumber id="import" mode="decimal" />
      </div>
      <div class="p-field">
        <label for="scan" class="required">Scan</label>
        <InputNumber id="scan" mode="decimal" />
      </div>
      <div class="p-field">
        <label for="rescan" class="required">Rescan</label>
        <InputNumber mode="decimal" />
      </div>
      <div class="p-field">
        <label for="write" class="required">Write</label>
        <InputNumber mode="decimal" />
      </div>
      <div class="p-field p-col-2">
        <Button label="Save" icon="pi pi-save"></Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, toRef } from 'vue'
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

</script>

<style lang="scss" scoped>
@import "../Common/common.scss";
</style>