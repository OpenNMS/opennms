<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="distributed-monitoring-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Manage Minions and Locations</h1>
        <p class="page-subtitle">Monitoring locations and the Minions that poll and collect from them.</p>
      </div>
      <div class="refresh-controls">
        <span class="updated-label" data-test="updated-label">{{ updatedLabel }}</span>
        <OnmsButton
          variant="outlined"
          label="Refresh"
          :loading="refreshing"
          data-test="refresh-button"
          @click="refresh"
        />
      </div>
    </div>

    <AboutPanel />

    <OnmsTabs v-model:value="activeTab">
      <OnmsTabList>
        <OnmsTab value="minions" data-test="tab-minions">Minions ({{ minionStore.minions.length }})</OnmsTab>
        <OnmsTab value="locations" data-test="tab-locations">Monitoring locations ({{ locationStore.locations.length }})</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel value="minions">
          <MinionsTable
            :now="now"
            v-model:locationFilter="minionLocationFilter"
            @showLocation="showLocation"
            @dialogOpen="minionsDialogOpen = $event"
          />
        </OnmsTabPanel>
        <OnmsTabPanel value="locations">
          <LocationsTable
            ref="locationsTable"
            @showMinions="showMinions"
            @dialogOpen="locationsDialogOpen = $event"
          />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs, useOnmsToast } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import AboutPanel from '@/components/DistributedMonitoring/AboutPanel.vue'
import MinionsTable from '@/components/ManageMinions/MinionsTable.vue'
import LocationsTable from '@/components/ManageMonitoringLocations/LocationsTable.vue'
import { relativeTimeSince } from '@/lib/relativeTime'
import { useMenuStore } from '@/stores/menuStore'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { BreadCrumb } from '@/types'

type Tab = 'minions' | 'locations'

const AUTO_REFRESH_MS = 30_000
// a tab switch back to the page does not reload data this fresh
const MIN_VISIBILITY_REFRESH_AGE_MS = 10_000

const menuStore = useMenuStore()
const minionStore = useMinionAdminStore()
const locationStore = useMonitoringLocationAdminStore()
const route = useRoute()
const router = useRouter()
const { showToast } = useOnmsToast()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Manage Minions and Locations', to: '#', position: 'last' }
])

const tabFromQuery = (value: unknown): Tab => value === 'locations' ? 'locations' : 'minions'

const activeTab = ref<Tab>(tabFromQuery(route.query.tab))

// the active tab lives in ?tab= so links and history restore it
watch(activeTab, (tab) => {
  if (route.query.tab !== tab) {
    router.replace({ query: { ...route.query, tab }})
  }
  // node counts cost one request per location, so they are only kept fresh while shown
  locationStore.countsEnabled = tab === 'locations'
  if (tab === 'locations') {
    locationStore.getNodeCounts()
  }
})
locationStore.countsEnabled = activeTab.value === 'locations'
watch(() => route.query.tab, (tab) => {
  activeTab.value = tabFromQuery(tab)
})

const minionLocationFilter = ref<string | null>(null)
const locationsTable = ref<InstanceType<typeof LocationsTable> | null>(null)

const showMinions = (name: string) => {
  minionLocationFilter.value = name
  activeTab.value = 'minions'
}

const showLocation = (name: string) => {
  activeTab.value = 'locations'
  locationsTable.value?.searchFor(name)
}

const minionsDialogOpen = ref(false)
const locationsDialogOpen = ref(false)
const dialogOpen = computed(() => minionsDialogOpen.value || locationsDialogOpen.value)

const refreshing = ref(false)
const lastUpdated = ref<number | null>(null)
const now = ref(Date.now())

const updatedLabel = computed(() => {
  const since = relativeTimeSince(lastUpdated.value, now.value)
  return `${since ? `Updated ${since}` : 'Not updated yet'} · auto-refresh every ${AUTO_REFRESH_MS / 1000} s`
})

// both lists reload together; a failure keeps the previous rows (the tables say so)
// and is only toasted for a load the user asked for, not for the background tick
const load = async (notify: boolean) => {
  refreshing.value = true
  try {
    const [minionsOk, locationsOk] = await Promise.all([minionStore.getMinions(), locationStore.getLocations()])
    if (locationsOk && activeTab.value === 'locations') {
      await locationStore.getNodeCounts()
    }
    if (minionsOk && locationsOk) {
      lastUpdated.value = Date.now()
    } else if (notify) {
      showToast({ message: minionsOk ? 'Failed to load monitoring locations.' : 'Failed to load minions.', severity: 'error' })
    }
  } finally {
    refreshing.value = false
  }
}

const refresh = () => load(true)

let ticker: ReturnType<typeof setInterval> | undefined
let autoRefresh: ReturnType<typeof setInterval> | undefined

// a background tick is skipped while a dialog is open, a refresh is still in
// flight, or the tab is hidden; the next tick (or the page becoming visible) catches up
const tick = () => {
  if (!document.hidden && !dialogOpen.value && !refreshing.value) {
    load(false)
  }
}

const onVisibilityChange = () => {
  if (!document.hidden && (lastUpdated.value === null || Date.now() - lastUpdated.value >= MIN_VISIBILITY_REFRESH_AGE_MS)) {
    tick()
  }
}

onMounted(async () => {
  ticker = setInterval(() => {
    now.value = Date.now()
  }, 1000)
  autoRefresh = setInterval(tick, AUTO_REFRESH_MS)
  document.addEventListener('visibilitychange', onVisibilityChange)
  await Promise.all([load(true), minionStore.getCoreVersion()])
})

onBeforeUnmount(() => {
  clearInterval(ticker)
  clearInterval(autoRefresh)
  document.removeEventListener('visibilitychange', onVisibilityChange)
  locationStore.countsEnabled = false
})
</script>

<style lang="scss" scoped>
// the tabs inset their panels by 1.125rem (tabs.tabpanel.padding), so the header
// and the About panel use the same gutter to line up with the cards below
$page-gutter: 1.125rem;

.distributed-monitoring-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.about-panel {
  margin: 0 $page-gutter;
}

.page-header {
  padding: 0 $page-gutter;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.page-subtitle {
  margin: 0.25rem 0 0 0;
  color: var(--p-text-muted-color);
}

.refresh-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.updated-label {
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
  white-space: nowrap;
}
</style>
