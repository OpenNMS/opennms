<template>
  <div class="p-col-9">
    <div class="p-fluid">
      <div>
        <label for="import" class="required">Import</label>
        <FeatherInput
          :error="getErrorMessage(validationVar.threadpool.importThreads.$errors)"
          v-model="validationVar.threadpool.importThreads.$model"
          type="number"
          background
        />
      </div>
      <div>
        <label for="scan" class="required">Scan</label>
        <FeatherInput
          :error="getErrorMessage(validationVar.threadpool.scanThreads.$errors)"
          v-model="validationVar.threadpool.scanThreads.$model"
          type="number"
          background
        />
      </div>

      <div>
        <label for="rescan" class="required">Rescan</label>
        <FeatherInput
          :error="getErrorMessage(validationVar.threadpool.rescanThreads.$errors)"
          v-model="validationVar.threadpool.rescanThreads.$model"
          type="number"
          background
        />
      </div>
      <div>
        <label for="write" class="required">Write</label>
        <FeatherInput
          :error="getErrorMessage(validationVar.threadpool.writeThreads.$errors)"
          v-model="validationVar.threadpool.writeThreads.$model"
          type="number"
          background
        />
      </div>
      <div class="p-field p-col-2">
        <FeatherButton 
          primary
          :disabled="
            validationVar.threadpool.writeThreads.$invalid ||
            validationVar.threadpool.scanThreads.$invalid ||
            validationVar.threadpool.rescanThreads.$invalid ||
            validationVar.threadpool.importThreads.$invalid
            "
          @click="onSave()"
        >
          Save
        </FeatherButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import State from './formState'
import { useStore } from 'vuex'
import { notify } from "@kyvg/vue3-notification"
import { putProvisionDService } from "./../../services/configurationService"

const store = useStore()
const validationVar = State.toModel()

const getErrorMessage = (err:any) =>{
 return err.length > 0 ? err[0].$message: '';
}

const onSave = () => {
  const provisionDService = store.state.configuration.provisionDService
  provisionDService.importThreads = validationVar.value.threadpool.importThreads.$model,
    provisionDService.scanThreads = validationVar.value.threadpool.scanThreads.$model,
    provisionDService.rescanThreads = validationVar.value.threadpool.rescanThreads.$model,
    provisionDService.writeThreads = validationVar.value.threadpool.writeThreads.$model
  // await store.commit('configuration/PUT_ProvisionDService', provisionDService);
  // await store.dispatch('configuration/putProvisionDService');
  let response = putProvisionDService(provisionDService)
  console.log(response)
  try {
    if (response != null) {
      notify({
        title: "Notification",
        text: 'Threadpool data successfully updated',
        type: 'success',
      })
    }
  } catch {
    notify({
      title: "Notification",
      text: 'ProvisionDService PUT API Error',
      type: 'error',
    })
  }
}

onMounted(async () => {
  await store.dispatch('configuration/getProvisionDService')
  try {
    const provisionDService = store.state.configuration.provisionDService
    if (provisionDService != null) {
      validationVar.value.threadpool.importThreads.$model = provisionDService.importThreads
      validationVar.value.threadpool.scanThreads.$model = provisionDService.scanThreads
      validationVar.value.threadpool.rescanThreads.$model = provisionDService.rescanThreads
      validationVar.value.threadpool.writeThreads.$model = provisionDService.writeThreads
    }
  } catch {
    console.error("Error in API")
  }
})

</script>

<style lang="scss" scoped>
@import "../Common/common.scss";
.notification-font {
  font-style: normal;
  font-size: 14px;
}
.feather-input-container {
  padding-top: 0px;
}
</style>
