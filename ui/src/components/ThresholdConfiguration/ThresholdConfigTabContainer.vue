<template>
  <OnmsTabs :value="activeTab" data-test="threshold-config-tabs" @update:value="onTabChange">
    <OnmsTabList>
      <OnmsTab :value="0">Groups</OnmsTab>
      <OnmsTab :value="1">Packages</OnmsTab>
      <OnmsTab :value="2">Upload / download</OnmsTab>
    </OnmsTabList>
    <OnmsTabPanels>
      <OnmsTabPanel :value="0">
        <ThresholdGroupsTab />
      </OnmsTabPanel>
      <OnmsTabPanel :value="1">
        <ThresholdPackagesTab />
      </OnmsTabPanel>
      <OnmsTabPanel :value="2">
        <ThresholdConfigTransferTab />
      </OnmsTabPanel>
    </OnmsTabPanels>
  </OnmsTabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ThresholdConfigTransferTab from '@/components/ThresholdConfiguration/ThresholdConfigTransferTab.vue'
import ThresholdGroupsTab from '@/components/ThresholdConfiguration/ThresholdGroupsTab.vue'
import ThresholdPackagesTab from '@/components/ThresholdConfiguration/ThresholdPackagesTab.vue'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

// Tab names live in the URL so a tab is linkable, matching how the topology view keeps ?view=.
const TAB_NAMES = ['groups', 'packages', 'transfer']

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
