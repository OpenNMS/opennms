<template>
  <p>
    <FeatherBackButton @click="onBackClick">Back To Node Table</FeatherBackButton>
  </p>
  <p>
    <ReqDefinitionForm title="Edit"></ReqDefinitionForm>
  </p>
</template>

<script setup lang="ts">

import { FeatherBackButton } from "@featherds/back-button"
import router from '@/router'
import ReqDefinitionForm from '../../Configuration/ReqDefinitionForm.vue'

//Routing Back to Table
const onBackClick = () => {
  router.push({ name: 'requisitionDefinitionsLayout' })
}

</script>
