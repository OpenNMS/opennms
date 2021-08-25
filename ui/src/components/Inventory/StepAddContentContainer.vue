<template>
  <template v-for="index in forms" :key="contains + index">
    <component :is="contains" :index="index" @setValues="setValues"></component>
  </template>

  <div p-flex-row>
    <Button
      label="Add another" 
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
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import { useStore } from 'vuex'
import Button from 'primevue/button'
import StepAddContentCtrl from './StepAddContentCtrl.vue'
import StepAddContentDNS from './StepAddContentDNS.vue'
import StepAddContentIpRange from './StepAddContentIpRange.vue'

export default defineComponent({
  components: {
    Button,
    StepAddContentDNS,
    StepAddContentCtrl,
    StepAddContentIpRange
  },
  props: {
    contains: {
      type: String,
      required: true
    }
  },
  setup() {
    const store = useStore()
    const formsValues = ref([] as any)
    const forms = ref([0])
    const requiredFields = ref([])

    const addForm = () => forms.value.push(forms.value.length)
    const setValues = (form: any) => {
      console.log(form)
      formsValues.value[form.index] = form.data
      requiredFields.value = form.requiredFields
    }

    const isValid = () => {
      const missingFields = []
      for (const form of formsValues.value) {
        for (const field of requiredFields.value) {
          if (!form[field]) missingFields.push(field) 
        }
      }

      return Boolean(missingFields.length === 0)
    }

    const test = async () => {
      // store.dispatch('spinnerModule/setSpinnerState', true)

      //const validForms = isValid()

      //if (validForms) {
        const success = await store.dispatch('inventoryModule/scanIPRanges', formsValues.value)

        if (success) {
          // display next btn if testing successful
          store.dispatch('inventoryModule/showAddStepNextButton', true)
        } else {
          store.dispatch('inventoryModule/showAddStepNextButton', false)
        }
      //}
      
      // store.dispatch('spinnerModule/setSpinnerState', false)
    }

    return {
      test,
      addForm,
      setValues,
      forms
    }
  }
})
</script>

<style scoped lang="scss"></style>
