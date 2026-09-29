<template>
  <OnmsTabs
    v-model:value="activeTab"
    class="notifications-config-tabs"
    data-test="notifications-config-tabs"
  >
    <OnmsTabList>
      <OnmsTab value="event-notifications" data-test="tab-event-notifications">Event Notifications</OnmsTab>
      <OnmsTab value="destination-paths" data-test="tab-destination-paths">Destination Paths</OnmsTab>
      <OnmsTab value="path-outages" data-test="tab-path-outages">Path Outages</OnmsTab>
      <OnmsTab value="general" data-test="tab-general">General</OnmsTab>
    </OnmsTabList>
    <OnmsTabPanels>
      <OnmsTabPanel value="event-notifications">
        <EventNotificationsTab />
      </OnmsTabPanel>
      <OnmsTabPanel value="destination-paths">
        <DestinationPathsTab />
      </OnmsTabPanel>
      <OnmsTabPanel value="path-outages">
        <PathOutagesTab />
      </OnmsTabPanel>
      <OnmsTabPanel value="general">
        <div class="general-tab">
          <div class="status-toggle">
            <OnmsToggleSwitch
              :modelValue="store.notifdStatus === 'on'"
              :disabled="store.notifdStatus === null || statusPending"
              aria-label="Turn notifications on or off"
              data-test="notifd-status-toggle"
              @update:modelValue="onStatusToggle"
            />
            <span class="status-label">Notifications are <strong>{{ store.notifdStatus ?? 'unknown' }}</strong></span>
          </div>
          <p class="status-hint">
            System-wide switch. While off, OpenNMS will not create outgoing notifications for any
            event. The current status is also reflected by the bell icon in the top bar.
          </p>
        </div>
      </OnmsTabPanel>
    </OnmsTabPanels>
  </OnmsTabs>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { OnmsTabs, OnmsTabList, OnmsTab, OnmsTabPanels, OnmsTabPanel, OnmsToggleSwitch } from '@opennms/onms-ui'

import DestinationPathsTab from '@/components/Notifications/DestinationPathsTab.vue'
import EventNotificationsTab from '@/components/Notifications/EventNotificationsTab.vue'
import PathOutagesTab from '@/components/Notifications/PathOutagesTab.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { NotifdStatus } from '@/types/notificationConfig'
import { createSuccessResponse, ValidationResult } from '@/types/validation'

const store = useNotificationConfigStore()
const { withSpinner, report, showError, showSuccess } = useActionFeedback()

// Open on Event Notifications, the first tab in the final order (now live).
const activeTab = ref('event-notifications')

const statusPending = ref(false)

// Every tab loads its own data on first activation and latches only on
// success, so a transient failure (restart, 500) retries on the next visit
// instead of leaving the tab dead for the lifetime of the page.
const loadedTabs = ref(new Set<string>())

// A tab's loads succeed together or the tab retries; one failed action shows
// one message (the first failure's).
const allOf = (results: ValidationResult[]): ValidationResult =>
  results.find(result => !result.success) ?? createSuccessResponse()

const TAB_LOADERS: Record<string, () => Promise<ValidationResult>> = {
  // destination paths feed the event notification editor's path picker
  'event-notifications': async () =>
    allOf(await Promise.all([store.getEventNotifications(), store.getDestinationPaths()])),
  'destination-paths': async () =>
    allOf(await Promise.all([store.getDestinationPaths(), store.getCommands(), store.getUsersAndGroups()])),
  'path-outages': () => store.getPathOutages(),
  general: () => store.getStatus()
}

const ensureTabLoaded = async (tab: string) => {
  const loader = TAB_LOADERS[tab]
  if (!loader || loadedTabs.value.has(tab)) {
    return
  }
  if (report(await withSpinner(loader))) {
    loadedTabs.value = new Set([...loadedTabs.value, tab])
  }
}

watch(activeTab, tab => ensureTabLoaded(tab), { immediate: true })

const onStatusToggle = async (value: boolean) => {
  const status = (value ? 'on' : 'off') as NotifdStatus
  statusPending.value = true
  try {
    if (await withSpinner(() => store.setStatus(status))) {
      showSuccess(`Notifications turned ${status}.`)
    } else {
      showError('Failed to update notification status.')
    }
  } finally {
    statusPending.value = false
  }
}
</script>

<style lang="scss" scoped>
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
