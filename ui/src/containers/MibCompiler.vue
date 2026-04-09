<template>
  <div class="mib-compiler-container">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Import Events from MIB</h1>
      </div>
    </div>
    <div class="tab-container">
      <FeatherTabContainer>
        <template v-slot:tabs>
          <FeatherTab>View</FeatherTab>
          <FeatherTab>Upload MIB Files</FeatherTab>
        </template>
        <FeatherTabPanel>
          <CompiledMibFiles />
          <PendingMibFiles />
        </FeatherTabPanel>
        <FeatherTabPanel>
          <UploadMibFiles />
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>

<script lang="ts" setup>
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CompiledMibFiles from '@/components/MibCompiler/CompiledMibFiles.vue'
import PendingMibFiles from '@/components/MibCompiler/PendingMibFiles.vue'
import UploadMibFiles from '@/components/MibCompiler/UploadMibFiles.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { BreadCrumb } from '@/types'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const menuStore = useMenuStore()
const store = useMibCompilerStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'MIB Compiler', to: '#', position: 'last' }
]))

onMounted(async () => {
  await Promise.all([
    store.fetchMibFiles()
    // store.fetchPendingMibFiles()
  ])
})
</script>

<style lang="scss" scoped>
.mib-compiler-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 60px 40px 25px 40px;
  }

  .tab-container {
    padding: 0px 40px 0px 40px;
  }
}
</style>
