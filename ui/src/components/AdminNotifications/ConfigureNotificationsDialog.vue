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
    <Tabs v-model:value="activeTab">
      <TabList>
        <Tab value="event-notifications" data-test="tab-event-notifications">Event Notifications</Tab>
        <Tab value="general" data-test="tab-general">General</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="event-notifications">
          <EventNotificationsTable />
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

import EventNotificationsTable from '@/components/AdminNotifications/EventNotificationsTable.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { NotifdStatus } from '@/types/notificationConfig'

const PToggleSwitch = ToggleSwitch

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible'])

const store = useNotificationConfigStore()

const activeTab = ref('event-notifications')
const statusPending = ref(false)
const populated = ref(false)

watch(
  () => props.visible,
  async (isVisible) => {
    if (isVisible && !populated.value) {
      await store.populate()
      populated.value = true
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
