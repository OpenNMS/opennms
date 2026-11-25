<template>
  <OnmsTabs :value="activeTab" data-test="xml-config-tabs" @update:value="onTabChange">
    <OnmsTabList>
      <OnmsTab :value="0">Collections</OnmsTab>
      <OnmsTab :value="1">Group files</OnmsTab>
      <OnmsTab :value="2">Settings</OnmsTab>
      <OnmsTab :value="3">Upload / download</OnmsTab>
    </OnmsTabList>
    <OnmsTabPanels>
      <OnmsTabPanel :value="0">
        <XmlCollectionsTab />
      </OnmsTabPanel>
      <OnmsTabPanel :value="1">
        <XmlGroupFilesTab />
      </OnmsTabPanel>
      <OnmsTabPanel :value="2">
        <XmlSettingsTab />
      </OnmsTabPanel>
      <OnmsTabPanel :value="3">
        <XmlTransferTab />
      </OnmsTabPanel>
    </OnmsTabPanels>
  </OnmsTabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import XmlCollectionsTab from '@/components/XmlCollectionConfiguration/XmlCollectionsTab.vue'
import XmlGroupFilesTab from '@/components/XmlCollectionConfiguration/XmlGroupFilesTab.vue'
import XmlSettingsTab from '@/components/XmlCollectionConfiguration/XmlSettingsTab.vue'
import XmlTransferTab from '@/components/XmlCollectionConfiguration/XmlTransferTab.vue'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

// Tab names live in the URL so a tab is linkable, as on the threshold configuration page.
const TAB_NAMES = ['collections', 'group-files', 'settings', 'transfer']

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
