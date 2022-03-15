<script setup lang="ts">
import ConfigurationToast from '@/components/Configuration/ConfigurationToast.vue'
import ConfigurationHeader from '@/components/Configuration/ConfigurationHeader.vue'
import ConfigurationTableWrapper from '@/components/Configuration/ConfigurationTableWrapper.vue'
import ThreadPools from '@/components/Configuration/ConfigurationThreadPools.vue'
import { populateProvisionD } from '@/services/configurationService'
import { useStore } from 'vuex'

const store = useStore()
populateProvisionD(store)
</script>
<template>
  <div class="feather-row">
    <div class="feather-col-12">
  <div class="wrapper feather-container center">
    <ConfigurationToast />
    <ConfigurationHeader 
      title="Configuration" 
      headline="Provisiond" 
    />
    <ConfigurationTableWrapper />
    <div class="spacer"></div>
    <ThreadPools />
  </div>
  </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@featherds/styles/mixins/typography';
@import '@featherds/styles/mixins/elevation';
.wrapper {
  margin-top: 20px;
  margin-left: 20px;
}
</style>
