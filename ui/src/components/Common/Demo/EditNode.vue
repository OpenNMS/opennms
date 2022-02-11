<template>
  <p class="btn-back-to-requisition">
    <FeatherBackButton @click="onBackClick">Back To Node Table</FeatherBackButton>
  </p>
  <p>
    <ReqDefinitionForm title="Edit"></ReqDefinitionForm>
  </p>
</template>

<script setup lang="ts">

import router from '@/router'
import ReqDefinitionForm from '../../Configuration/ReqDefinitionForm.vue'

//Routing Back to Table
const onBackClick = () => {
  router.push({ name: 'requisitionDefinitionsLayout' })
}

</script>
<style scoped>
.btn-back-to-requisition {
  margin: 10px 0 10px 0;
}
</style>
