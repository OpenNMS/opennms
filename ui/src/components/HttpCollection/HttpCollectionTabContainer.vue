<template>
  <OnmsTabs :value="activeTab" data-test="http-collection-tabs" @update:value="onTabChange">
    <OnmsTabList>
      <OnmsTab :value="0">Collections</OnmsTab>
      <OnmsTab :value="1">Settings</OnmsTab>
      <OnmsTab :value="2">Upload / download</OnmsTab>
    </OnmsTabList>
    <OnmsTabPanels>
      <OnmsTabPanel :value="0">
        <div class="intro">
          <p>
            An HTTP collection lists the URLs the HTTP collector requests and the values it extracts from the
            responses with a regular expression. A collectd package service selects it through its
            <code>collection</code> parameter. Changes apply from the next collection on.
          </p>
        </div>
        <HttpCollectionsTable />
      </OnmsTabPanel>
      <OnmsTabPanel :value="1">
        <HttpCollectionSettingsTab />
      </OnmsTabPanel>
      <OnmsTabPanel :value="2">
        <HttpCollectionTransferTab />
      </OnmsTabPanel>
    </OnmsTabPanels>
  </OnmsTabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HttpCollectionSettingsTab from '@/components/HttpCollection/HttpCollectionSettingsTab.vue'
import HttpCollectionsTable from '@/components/HttpCollection/HttpCollectionsTable.vue'
import HttpCollectionTransferTab from '@/components/HttpCollection/HttpCollectionTransferTab.vue'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

// Tab names live in the URL so a tab is linkable.
const TAB_NAMES = ['collections', 'settings', 'transfer']

const route = useRoute()
const router = useRouter()

const activeTab = computed(() => {
  const index = TAB_NAMES.indexOf(String(route.query.tab ?? ''))
  return index >= 0 ? index : 0
})

const onTabChange = (value: unknown) => {
  const index = Number(value)

  if (Number.isNaN(index) || index < 0 || index >= TAB_NAMES.length) {
    return
  }

  router.replace({ query: { ...route.query, tab: TAB_NAMES[index] }})
}
</script>

<style lang="scss" scoped>
.intro {
  margin-bottom: 1em;

  p {
    margin: 0;
  }
}
</style>
