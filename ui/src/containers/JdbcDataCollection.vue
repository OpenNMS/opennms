<template>
  <div class="jdbc-data-collection-container">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <h1>JDBC Data Collection</h1>
    </div>
    <div class="intro">
      <p>
        Sources hold the SQL queries the JDBC collector runs and how their results are stored.
        <OnmsIconButton
          :icon="InfoIcon"
          iconSize="1.2em"
          class="info-icon-button"
          title="About JDBC data collection sources"
          aria-label="About JDBC data collection sources"
          data-test="jdbc-sources-info-icon"
          @click="isHelpVisible = true"
        />
      </p>
    </div>
    <OnmsTabs :value="activeTab" data-test="jdbc-tabs" @update:value="onTabChange">
      <OnmsTabList>
        <OnmsTab :value="0">Sources</OnmsTab>
        <OnmsTab :value="1">Import</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <JdbcSourcesTable />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <JdbcSourceImport />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="JDBC data collection sources"
      :text="SOURCES_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import JdbcSourceImport from '@/components/JdbcDataCollection/JdbcSourceImport.vue'
import JdbcSourcesTable from '@/components/JdbcDataCollection/JdbcSourcesTable.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { SOURCES_HELP } from '@/lib/jdbcDataCollectionHelpText'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { OnmsIconButton, OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

// Tab names live in the URL so a tab is linkable, as on the threshold configuration page.
const TAB_NAMES = ['sources', 'import']

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useJdbcDataCollectionStore()
const { report } = useActionFeedback()

const isHelpVisible = ref(false)

const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'JDBC Data Collection', to: '#', position: 'last' }
]))

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

onMounted(async () => {
  const result = await store.fetchSources()

  if (!result.success) {
    report(result)
  }
})
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;

.info-icon-button {
  @include info-icon-button;
}

.jdbc-data-collection-container {
  padding: 1.5em;

  .header {
    margin-bottom: 0.5em;

    h1 {
      margin: 0;
    }
  }

  .intro p {
    display: flex;
    align-items: center;
    gap: 0.25em;
    margin: 0 0 1em 0;
  }
}
</style>
