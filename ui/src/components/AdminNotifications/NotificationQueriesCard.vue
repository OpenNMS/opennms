<template>
  <TableCard class="notification-queries-bar">
    <div class="bar-content">
      <div class="query-links">
        <a
          :class="{ active: store.preset === 'yourOutstanding' }"
          data-test="query-your-outstanding"
          @click.prevent="store.applyPreset('yourOutstanding')"
        >Your outstanding notices</a>
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
        >All outstanding notices</a>
        <span class="separator">|</span>
        <a
          :class="{ active: store.preset === 'allAcknowledged' }"
          data-test="query-all-acknowledged"
          @click.prevent="store.applyPreset('allAcknowledged')"
        >All acknowledged notices</a>
      </div>
      <div class="query-forms">
        <div class="search-row">
          <IftaLabel>
            <OnmsInputText
              id="notification-user-search"
              v-model="userSearch"
              data-test="user-search-input"
              @keyup.enter="searchByUser"
            />
            <label for="notification-user-search">Check notices for user</label>
          </IftaLabel>
          <OnmsIconButton
            variant="outlined"
            :icon="Search"
            aria-label="Show notices for user"
            data-test="user-search-button"
            @click="searchByUser"
          />
        </div>
        <div class="search-row">
          <IftaLabel>
            <OnmsInputText
              id="notification-notice-search"
              v-model="noticeSearch"
              data-test="notice-search-input"
              @keyup.enter="goToNotice"
            />
            <label for="notification-notice-search">Get details for notice</label>
          </IftaLabel>
          <OnmsIconButton
            variant="outlined"
            :icon="Search"
            aria-label="Get notice detail"
            data-test="notice-search-button"
            @click="goToNotice"
          />
        </div>
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import IftaLabel from 'primevue/iftalabel'
import { OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'

import TableCard from '@/components/Common/TableCard.vue'
import Search from '@/components/icons/action/Search.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNoticesStore } from '@/stores/noticesStore'

const menuStore = useMenuStore()
const store = useNoticesStore()

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
