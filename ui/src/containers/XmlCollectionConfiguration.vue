<template>
  <div class="xml-collection-config-container">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <h1>XML Data Collection Configuration</h1>
    </div>
    <XmlConfigTabContainer />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import XmlConfigTabContainer from '@/components/XmlCollectionConfiguration/XmlConfigTabContainer.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { useMenuStore } from '@/stores/menuStore'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { BreadCrumb } from '@/types'
import { xmlBreadcrumbs } from '@/lib/xmlCollectionRoutes'

const menuStore = useMenuStore()
const store = useXmlCollectionConfigStore()
const { report } = useActionFeedback()

const breadcrumbs = computed<BreadCrumb[]>(() => xmlBreadcrumbs(menuStore.mainMenu, []))

onMounted(async () => {
  report(await store.fetchAll())
})
</script>

<style lang="scss" scoped>
.xml-collection-config-container {
  padding: 1.5em;

  .header {
    margin-bottom: 1.25em;

    h1 {
      margin: 0;
    }
  }
}
</style>
