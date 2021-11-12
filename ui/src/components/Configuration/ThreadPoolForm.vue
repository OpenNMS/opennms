<template>
  <div class="p-col-9">
    <div class="p-fluid">
      <div class="p-field">
        <label for="import" class="required">Import</label>
        <InputNumber
          id="import"
          mode="decimal"
          v-model="validationVar.threadpool.importThreads.$model"
          :class="{ 'p-invalid': validationVar.threadpool.importThreads.$error }"
        />
        <ValidationMessage :model="validationVar.threadpool.importThreads"></ValidationMessage>
      </div>
      <div class="p-field">
        <label for="scan" class="required">Scan</label>
        <InputNumber
          id="scan"
          mode="decimal"
          v-model="validationVar.threadpool.scanThreads.$model"
          :class="{ 'p-invalid': validationVar.threadpool.scanThreads.$error }"
        />
        <ValidationMessage :model="validationVar.threadpool.scanThreads"></ValidationMessage>
      </div>

      <div class="p-field">
        <label for="rescan" class="required">Rescan</label>
        <InputNumber
          id="rescan"
          mode="decimal"
          v-model="validationVar.threadpool.rescanThreads.$model"
          :class="{ 'p-invalid': validationVar.threadpool.rescanThreads.$error }"
        />
        <ValidationMessage :model="validationVar.threadpool.rescanThreads"></ValidationMessage>
      </div>
      <div class="p-field">
        <label for="write" class="required">Write</label>
        <InputNumber
          id="write"
          mode="decimal"
          v-model="validationVar.threadpool.writeThreads.$model"
          :class="{ 'p-invalid': validationVar.threadpool.writeThreads.$error }"
        />
        <ValidationMessage :model="validationVar.threadpool.writeThreads"></ValidationMessage>
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
import InputNumber from '../Common/InputNumber.vue'
import { FeatherButton } from '@featherds/button'
import State from './formState'
import ValidationMessage from '../Common/ValidationMessage.vue'
import { useStore } from 'vuex'
import { notify } from "@kyvg/vue3-notification"
import { putProvisionDService } from "./../../services/configurationService"

const store = useStore()
const validationVar = State.toModel()

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
</style>
