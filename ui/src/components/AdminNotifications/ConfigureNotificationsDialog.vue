<template>
  <Dialog
    :visible="visible"
    modal
    maximizable
    header="Configure Notifications"
    class="configure-notifications-dialog"
    :style="{ width: '1100px', maxWidth: '95vw' }"
    data-test="configure-notifications-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <!-- Tab order and the default tab are declared here once; the sibling
         PRs each replace one placeholder panel and register one loader. -->
    <Tabs v-model:value="activeTab">
      <TabList>
        <Tab value="event-notifications" data-test="tab-event-notifications">Event Notifications</Tab>
        <Tab value="destination-paths" data-test="tab-destination-paths">Destination Paths</Tab>
        <Tab value="path-outages" data-test="tab-path-outages">Path Outages</Tab>
        <Tab value="general" data-test="tab-general">General</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="event-notifications">
          <p class="tab-placeholder" data-test="placeholder-event-notifications">
            Event notification management arrives with NMS-20118.
          </p>
        </TabPanel>
        <TabPanel value="destination-paths">
          <p class="tab-placeholder" data-test="placeholder-destination-paths">
            Destination path management arrives with NMS-20119.
          </p>
        </TabPanel>
        <TabPanel value="path-outages">
          <p class="tab-placeholder" data-test="placeholder-path-outages">
            Path outage management arrives with NMS-20120.
          </p>
        </TabPanel>
        <TabPanel value="general">
          <div class="general-tab">
            <div class="status-toggle">
              <PToggleSwitch
                :modelValue="store.notifdStatus === 'on'"
                :disabled="store.notifdStatus === null || statusPending"
                aria-label="Turn notifications on or off"
                data-test="notifd-status-toggle"
                @update:modelValue="onStatusToggle"
              />
              <span class="status-label">Notifications are <strong>{{ store.notifdStatus ?? 'unknown' }}</strong></span>
            </div>
            <p class="status-hint">
              System-wide switch. While off, OpenNMS will not create outgoing notices for any
              event. The current status is also reflected by the bell icon in the top bar.
            </p>
          </div>
        </TabPanel>
      </TabPanels>
    </Tabs>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import Dialog from 'primevue/dialog'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'
import ToggleSwitch from 'primevue/toggleswitch'

import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { NotifdStatus } from '@/types/notificationConfig'

const PToggleSwitch = ToggleSwitch

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible'])

const store = useNotificationConfigStore()

// Event Notifications is first in the final tab order but is only a placeholder
// until NMS-20118 lands, so open on General (the one live tab) for now.
const activeTab = ref('general')

const statusPending = ref(false)

// Every tab loads its own data on first activation and latches only on
// success, so a transient failure (restart, 500) retries on the next visit
// instead of leaving the tab dead for the lifetime of the page.
const loadedTabs = ref(new Set<string>())

const TAB_LOADERS: Record<string, () => Promise<boolean>> = {
  general: async () => {
    await store.getStatus()
    return store.notifdStatus !== null
  }
}

const ensureTabLoaded = async (tab: string) => {
  const loader = TAB_LOADERS[tab]
  if (!loader || loadedTabs.value.has(tab)) {
    return
  }
  if (await loader()) {
    loadedTabs.value = new Set([...loadedTabs.value, tab])
  }
}

watch(
  [() => props.visible, activeTab],
  ([isVisible, tab]) => {
    if (isVisible) {
      ensureTabLoaded(tab)
    }
  }
)

const onStatusToggle = async (value: boolean) => {
  statusPending.value = true
  try {
    await store.setStatus((value ? 'on' : 'off') as NotifdStatus)
  } finally {
    statusPending.value = false
  }
}
</script>

<style lang="scss" scoped>
.tab-placeholder {
  padding: 1rem 0;
  color: var(--p-text-muted-color);
}

.general-tab {
  padding: 1rem 0;

  .status-toggle {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .status-hint {
    margin-top: 1rem;
    color: var(--p-text-muted-color);
    font-size: 0.9rem;
    max-width: 60ch;
  }
}
</style>
