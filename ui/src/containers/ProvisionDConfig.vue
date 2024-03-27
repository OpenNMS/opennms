<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="wrapper feather-container center">
        <Snackbar />
        <ConfigurationHeader
          title="Configuration"
          headline="External Requisitions and Thread Pools"
        />
        <ConfigurationTableWrapper />
        <div class="spacer"></div>
        <ThreadPools />
      </div>
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
>
import Snackbar from '@/components/Common/Snackbar.vue'
import ConfigurationHeader from '@/components/Configuration/ConfigurationHeader.vue'
import ConfigurationTableWrapper from '@/components/Configuration/ConfigurationTableWrapper.vue'
import ThreadPools from '@/components/Configuration/ConfigurationThreadPools.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useConfigurationStore } from '@/stores/configurationStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const configurationStore = useConfigurationStore()
const menuStore = useMenuStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'External Requisitions and Thread Pools', to: '#', position: 'last' }
  ]
})

configurationStore.getProvisionDService()
</script>
<style
  lang="scss"
  scoped
>
@import '@featherds/styles/mixins/typography';
@import '@featherds/styles/mixins/elevation';

.wrapper {
  margin-top: 20px;
  margin-left: 20px;
}
</style>

