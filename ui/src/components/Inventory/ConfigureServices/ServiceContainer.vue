<template>
  <template v-for="index in forms">
    <component :is="service" :index="index" @setValues="setValues"></component>
  </template>

  <div p-flex-row>
    <Button
      :label="`Add another ${service} from`" 
      class="p-button-raised p-button-text first input" 
      @click="addForm"
    />
  </div>

  <div p-flex-row>
    <Button
      label="Test" 
      class="p-button-primary input" 
      @click="test"
    />
  </div>

  <div p-flex-row v-if="showNextBtn">
    <Button
      label="Next" 
      class="p-button-primary input" 
      @click="next"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { SNMPDetectRequest } from '@/types'
import { useStore } from 'vuex'
import Button from 'primevue/button'
import WinRM from './WinRM.vue'
import HTTPS from './HTTPS.vue'
import ICMP from './ICMP.vue'
import SNMP from './SNMP.vue'
import SSH from './SSH.vue'

export default defineComponent({
  components: {
    Button,
    WinRM,
    HTTPS,
    ICMP,
    SNMP,
    SSH
  },
  props: {
    service: {
      type: String,
      required: true
    },
    lastService: {
      type: String,
      required: true
    }
  },
  emit:['complete-service'],
  setup(props, context) {
    const store = useStore()
    const formsValues = ref([] as any)
    const forms = ref([0])
    const showNextBtn = ref(false)

    const addForm = () => forms.value.push(forms.value.length)
    const setValues = (form: any) => {formsValues.value[form.index] = form.data; console.log(form.data)}

    const ipRangeResponses = computed(
      () => store.state.inventoryModule.ipRangeResponses
    )

    const locations = computed(() => {
      const locations = {} as any
      for (const range of ipRangeResponses.value) {
        locations[range.location] = []
        for (const result of range.scanResults) {
          locations[range.location].push(result.ipAddress)
        }
      }

      return locations
    })

    const test = async () => {
      // store.dispatch('spinnerModule/setSpinnerState', true)
      const request: SNMPDetectRequest[] = []

      for (const key in locations.value) {
        request.push({
          location: key,
          ipAddresses: locations.value[key],
          configurations: formsValues.value
        })
      }

      const success = await store.dispatch('inventoryModule/detectSNMPAvailable', request)
      // store.dispatch('spinnerModule/setSpinnerState', false)

      if (success) {
        if (props.lastService) {
          store.dispatch('inventoryModule/showConfigureServiceStepNextButton', true)
        } else {
          showNextBtn.value = true
        }

        // changes color of service btn to green
        store.dispatch('inventoryModule/addCompletedService', props.service)
      } else {
        store.dispatch('inventoryModule/showConfigureServiceStepNextButton', false)
      }
    }

    const next = () => {
      console.log(formsValues.value)
      context.emit('complete-service')
    }

    return {
      test,
      next,
      addForm,
      setValues,
      forms,
      showNextBtn
    }
  }
})
</script>

<style scoped lang="scss"></style>
