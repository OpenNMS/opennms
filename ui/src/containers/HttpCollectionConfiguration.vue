<template>
  <div class="http-collection-config-container">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>HTTP Data Collection Configuration</h1>
      </div>
    </div>
    <div class="tab-container">
      <HttpCollectionTabContainer />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import HttpCollectionTabContainer from '@/components/HttpCollection/HttpCollectionTabContainer.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useHttpCollectionStore()
const { report } = useActionFeedback()

const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'HTTP Data Collection Configuration', to: '#', position: 'last' }
]))

onMounted(async () => {
  const [collections, settings] = await Promise.all([
    store.fetchCollections(),
    store.fetchSettings()
  ])

  store.fetchMetadata()

  const failure = [collections, settings].find(result => !result.success)

  if (failure) {
    report(failure)
  }
})
</script>

<style lang="scss" scoped>
.http-collection-config-container {
  padding: 1.5em;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.25em;
    padding: 0;

    h1 {
      margin: 0;
    }
  }

  .tab-container {
    padding: 0;
  }
}
</style>
