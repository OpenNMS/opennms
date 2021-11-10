<template>
  <p>
    <Button :label="buttonLabel" primeClass="rounded" @click="onBackClick"></Button>
  </p>
  <p>
    <ReqDefinitionForm title="Edit"></ReqDefinitionForm>
  </p>
</template>

<script setup lang="ts">

import { ref } from 'vue'
import Button from "../Button.vue"
import router from '@/router'
import ReqDefinitionForm from '../../Configuration/ReqDefinitionForm.vue'

const buttonLabel = ref('Back To Node Table')

//Routing Back to Table
const onBackClick = () => {
  router.push({ name: 'requisitionDefinitionsLayout' })
}

</script>
