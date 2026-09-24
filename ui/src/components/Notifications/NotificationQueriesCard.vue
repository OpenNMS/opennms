<template>
  <TableCard class="notification-queries-bar">
    <div class="bar-content">
      <div class="query-presets">
        <OnmsSelectButton
          :modelValue="store.preset"
          :options="presetOptions"
          optionLabel="label"
          optionValue="value"
          aria-label="Notification query"
          data-test="query-presets"
          @update:modelValue="onPresetChange"
        />
        <OnmsIconButton
          variant="text"
          :icon="Refresh"
          tooltip="Refresh"
          aria-label="Refresh notifications"
          :disabled="store.loading"
          data-test="query-refresh-button"
          @click="store.load()"
        />
      </div>
      <div class="query-forms">
        <div class="search-row">
          <OnmsSearchInput
            v-model="userSearch"
            inputId="notification-user-search"
            placeholder="Check notifications for user"
            ariaLabel="Check notifications for user"
            dataTest="user-search-input"
            @keyup.enter="searchByUser"
          />
        </div>
        <div class="search-row">
          <OnmsSearchInput
            v-model="notificationIdSearch"
            inputId="notification-id-search"
            placeholder="Get details for notification"
            ariaLabel="Get details for notification"
            dataTest="notification-id-search-input"
            @keyup.enter="goToNotification"
          />
        </div>
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { OnmsIconButton, OnmsSearchInput, OnmsSelectButton } from '@opennms/onms-ui'

import TableCard from '@/components/Common/TableCard.vue'
import Refresh from '@opennms/onms-ui/icons/navigation/Refresh.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { NotificationQueryPreset } from '@/types/notifications'

const menuStore = useMenuStore()
const store = useNotificationsStore()

const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)

const presetOptions: { label: string, value: NotificationQueryPreset }[] = [
  { label: 'Your outstanding notifications', value: 'yourOutstanding' },
  { label: 'Outstanding for anyone but you', value: 'teamOutstanding' },
  { label: 'All outstanding notifications', value: 'allOutstanding' },
  { label: 'All acknowledged notifications', value: 'allAcknowledged' }
]

const onPresetChange = (value: unknown) => {
  store.applyPreset(value as NotificationQueryPreset)
}

const userSearch = ref('')
const notificationIdSearch = ref('')

const searchByUser = () => {
  const user = userSearch.value.trim()
  if (user) {
    store.applyPreset('userSearch', user)
  }
}

const goToNotification = () => {
  const notification = notificationIdSearch.value.trim()
  if (notification) {
    window.location.href = `${baseHref.value}notification/detail.jsp?notice=${encodeURIComponent(notification)}`
  }
}
</script>

<style lang="scss" scoped>
.notification-queries-bar {
  padding: 12px 25px;
}

.bar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.query-presets {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.query-forms {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;

  .search-row {
    display: flex;
    align-items: stretch;
    gap: 0.4rem;

    :deep(input) {
      min-width: 190px;
    }
  }
}
</style>
