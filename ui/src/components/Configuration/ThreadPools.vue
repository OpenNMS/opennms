<template>
  <br />
    <label class="p-col-9" for="import">Import</label>
    <br />
    <InputNumber
      id="import"
      class="p-col-9"
      mode="decimal"
      />
    <br />

    <label class="p-col-9" for="scan">Scan</label>
    <br />
    <InputNumber
      id="scan"
      class="p-col-9"
      mode="decimal"
    />
    <br />

    <label class="p-col-9" for="rescan">Rescan</label>
    <br />
    <InputNumber
    class="p-col-9"
      mode="decimal"
    />
    <br />

    <label class="p-col-9" for="write">Write</label>
    <br />
    <InputNumber
    class="p-col-9"
      mode="decimal"      
    />    
    <br />
  <br />
  <Button label="Save" ></Button>
</template>

<script setup lang="ts">
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

</script>