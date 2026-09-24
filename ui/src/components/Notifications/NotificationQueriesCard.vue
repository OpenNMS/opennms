<template>
  <TableCard class="notification-queries-bar">
    <div class="bar-content">
      <div class="query-links">
        <a
          :class="{ active: store.preset === 'yourOutstanding' }"
          data-test="query-your-outstanding"
          @click.prevent="store.applyPreset('yourOutstanding')"
        >Your outstanding notifications</a>
        <span class="separator">|</span>
        <a
          :class="{ active: store.preset === 'teamOutstanding' }"
          data-test="query-team-outstanding"
          @click.prevent="store.applyPreset('teamOutstanding')"
        >Outstanding for anyone but you</a>
        <span class="separator">|</span>
        <a
          :class="{ active: store.preset === 'allOutstanding' }"
          data-test="query-all-outstanding"
          @click.prevent="store.applyPreset('allOutstanding')"
        >All outstanding notifications</a>
        <span class="separator">|</span>
        <a
          :class="{ active: store.preset === 'allAcknowledged' }"
          data-test="query-all-acknowledged"
          @click.prevent="store.applyPreset('allAcknowledged')"
        >All acknowledged notifications</a>
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
            v-model="noticeSearch"
            inputId="notification-notice-search"
            placeholder="Get details for notification"
            ariaLabel="Get details for notification"
            dataTest="notice-search-input"
            @keyup.enter="goToNotice"
          />
        </div>
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { OnmsSearchInput } from '@opennms/onms-ui'

import TableCard from '@/components/Common/TableCard.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationsStore } from '@/stores/notificationsStore'

const menuStore = useMenuStore()
const store = useNotificationsStore()

const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)

const userSearch = ref('')
const noticeSearch = ref('')

const searchByUser = () => {
  const user = userSearch.value.trim()
  if (user) {
    store.applyPreset('userSearch', user)
  }
}

const goToNotice = () => {
  const notice = noticeSearch.value.trim()
  if (notice) {
    window.location.href = `${baseHref.value}notification/detail.jsp?notice=${encodeURIComponent(notice)}`
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

.query-links {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;

  .separator {
    color: var(--p-text-muted-color);
  }

  a {
    color: var(--p-primary-color);
    cursor: pointer;
    white-space: nowrap;

    &.active {
      font-weight: 700;
      text-decoration: underline;
    }
  }
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
