<template>
  <div class="snmp-data-collection-source-import">
    <div class="title-container">
      <div class="header">
        <div class="back">
          <FeatherBackButton @click="goBack"> Go Back </FeatherBackButton>
        </div>
        <div class="heading">
          <h1>Import SNMP Data Collection Sources</h1>
        </div>
      </div>
    </div>
    <div>
      <SnmpDataCollectionSourceImport />
    </div>
  </div>
</template>

<script setup lang="ts">
import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollectionSourceImport/SnmpDataCollectionSourceImport.vue'
import FeatherBackButton from '@featherds/back-button/src/components/FeatherBackButton/FeatherBackButton.vue'

const router = useRouter()

const goBack = () => {
  router.push({ name: 'SNMP Data Collection' })
}
</script>

<style scoped lang="scss">
.snmp-data-collection-source-import {
  padding: 20px;

  .title-container {
    margin-bottom: 20px;

    .header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      .back {
        margin-right: 20px;
      }
    }
  }
}
</style>

