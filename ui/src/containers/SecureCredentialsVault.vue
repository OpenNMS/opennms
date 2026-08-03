<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
   <div class="scv-container">
    <div class="list"><SCVListVue /></div>
    <div class="form"><SCVFormVue /></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import SCVListVue from '@/components/SCV/SCVList.vue'
import SCVFormVue from '@/components/SCV/SCVForm.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useScvStore } from '@/stores/scvStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const scvStore = useScvStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Secure Credentials Vault', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  await scvStore.getAliases()
  await scvStore.populate()
})
</script>

<style lang="scss" scoped>
.scv-container {
  padding: 2px;
  margin-left: 2px;
  display: flex;
  flex-grow: 1;
  gap: 2px;

  .list {
    min-width: 200px;
    max-width: 350px;
  }
  .form {
    width: 600px;
  }
}
</style>
