<template>
  <FeatherDropdown
    class="menubar-dropdown"
    :modelValue="displayMenu"
    @update:modelValue="(val: any) => updateDisplay(val)"
  >
    <template v-slot:trigger="{ attrs, on }">
      <div @mouseenter="showMenu">
        <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
          <span
            :class="['notification-badge-pill', userNotificationBadgeClass]">
            {{ notificationSummary.userUnacknowledgedCount ?? 0 }}
          </span>
          <span
            :class="['notification-badge-pill', teamNotificationBadgeClass]">
            {{ notificationSummary.teamUnacknowledgedCount ?? 0 }}
          </span>
          <FeatherIcon :icon="ArrowDropDown" />
        </FeatherButton>
      </div>
    </template>

    <FeatherDropdownItem v-for="item in mainMenu.userNotificationMenu?.items?.filter(i => i.id === 'user')"
      :key="item.name || ''" @click="onMenuItemClick(item.url || '', item.isVueLink)">
      <div class="menubar-dropdown-item-content">
        <a :href="computeLink(item.url || '')" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper">
          <template v-if="item.icon">
            <FeatherIcon :icon="Person" class="user-notifications-icon" />
          </template>
          <span class="left-margin-small">
            {{ notificationSummary.userUnacknowledgedCount ?? 0 }} notices assigned to you
          </span>
        </a>
      </div>
    </FeatherDropdownItem>

    <!-- user notifications -->
    <FeatherDropdownItem
      v-for="item in notificationSummary.userUnacknowledgedNotifications.notification.slice(0, maxNotifications)"
      :key="item.id || ''" class="notification-dropdown-item" @click="onNotificationItemClick(item)">
      <template #default>
        <div class="menubar-dropdown-item-content">
          <div class="notification-dropdown-item-content dropdown-menu-wrapper">
            <div @click="onNotificationItemClick(item)" class="notification-dropdown-item-content-button">
              <i :class="`notification-badge-pill badge-severity-${item?.severity?.toLocaleLowerCase()}`" />
              <div class="full-width-left">
                <div>
                  <span class="font-weight-bold">
                    {{ new Date(item.pageTime).toLocaleDateString() }} {{ new
                    Date(item.pageTime).toLocaleTimeString()
                    }}
                  </span>
                </div>
                <div class="dropdown-info-bar">
                  <span>{{ item.notificationName }}</span>
                  <span>{{ item.nodeLabel }}</span>
                  <span>{{ item.ipAddress }}</span>
                  <span>{{ item.serviceType?.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </FeatherDropdownItem>
    <FeatherDropdownItem v-if="notificationSummary.userUnacknowledgedCount > maxNotifications">
      <div class="menubar-dropdown-item-content">
        <div class="dropdown-menu-wrapper show-more-link notification-dropdown-item-content">
          <a :href="notificationsShowMoreLink" @click="onMenuItemClick(notificationsShowMoreLink)">Show more...</a>
        </div>
      </div>
    </FeatherDropdownItem>
    <!-- Team and On-Call links -->
    <FeatherDropdownItem v-for="item in mainMenu.userNotificationMenu?.items?.filter(i => i.id !== 'user')"
      :key="item.name || ''" @click="onMenuItemClick(item.url || '', item.isVueLink)">
      <div class="menubar-dropdown-item-content">
        <a :href="computeLink(item.url || '')"
          class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper">
          <template v-if="item.icon && item.id === 'team'">
            <FeatherIcon :icon="Person" class="user-notifications-icon" />
          </template>
          <template v-if="item.icon && item.id === 'oncall'">
            <FeatherIcon :icon="Calendar" class="user-notifications-icon" />
          </template>
          <span class="left-margin-small">
            <template v-if="item.id === 'team'">
              {{ notificationSummary.teamUnacknowledgedCount ?? 0 }} of {{ notificationSummary.totalUnacknowledgedCount ?? 0
              }} assigned to anyone but you
            </template>
            <template v-if="item.id === 'oncall'">
              {{ item.name }}
            </template>
          </span>
        </a>
      </div>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import Calendar from '@featherds/icon/action/Calendar'
import Person from '@featherds/icon/action/Person'
import { useMenuStore } from '@/stores/menuStore'
import {
  MainMenu,
  NotificationSummary,
  OnmsNotification
} from '@/types/mainMenu'

const menuStore = useMenuStore()
const displayMenu = ref(false)
const maxNotifications = 2
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const updateDisplay = (val: any) => {
  if (val === true) {
    displayMenu.value = true
  } else {
    displayMenu.value = false
  }
}

const hideMenu = () => {
  displayMenu.value = false
}

const showMenu = () => {
  displayMenu.value = true
}

defineExpose({ hideMenu, showMenu })

const notificationSummary = computed<NotificationSummary>(() => menuStore.notificationSummary)

const userNotificationBadgeClass = computed<string>(() => {
  if (notificationSummary.value.userUnacknowledgedCount === 0) {
    return 'badge-severity-cleared'
  }

  if (!notificationSummary.value.userUnacknowledgedNotifications ||
      !notificationSummary.value.userUnacknowledgedNotifications.notification) {
    return 'badge-severity-indeterminate'
  }

  const severities = ['cleared', 'indeterminate', 'warning', 'minor', 'major', 'critical']

  const severityIndexList = notificationSummary.value.userUnacknowledgedNotifications
    .notification.map(n => severities.indexOf(n.severity.toLowerCase())) || []

  const maxSeverityIndex = Math.max.apply(Math, severityIndexList)
  const maxSeverity = severities[maxSeverityIndex]

  return `badge-severity-${maxSeverity}`
})

const teamNotificationBadgeClass = computed<string>(() =>
  notificationSummary.value.teamUnacknowledgedCount === 0 ?  'badge-severity-cleared' : 'badge-info'
)

const notificationsShowMoreLink = computed<string>(() =>
  mainMenu.value.userNotificationMenu?.items?.filter(item => item.id === 'user')[0].url || ''
)

const computeLink = (url: string, isVueLink?: boolean | null) => {
  const baseLink = (isVueLink ? import.meta.env.VITE_VUE_BASE_URL : (mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL)) || ''
  return `${baseLink}${url}`
}

const onMenuItemClick = (url: string, isVueLink?: boolean | null) => {
  const link = computeLink(url, isVueLink)
  window.location.assign(link)
}

const onNotificationItemClick = (item: OnmsNotification) => {
  const url = `notification/detail.jsp?notice=${item.id}`
  onMenuItemClick(url)
}
</script>

<style lang="scss" scoped>
@import "@featherds/dropdown/scss/mixins";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.dropdown-menu-link {
  color: var($primary-text-on-surface) !important;

  &:hover {
    text-decoration: none;
  }
}

.menubar-dropdown {
  margin-left: 2px;

  :deep(.feather-dropdown) {
    @include dropdown-menu-height(10);
  }
}

.menubar-dropdown-dark {
  margin-left: 2px;
  &.help-menu {
    margin-left:1px;
  }
  :deep(.feather-dropdown) {
    @include dropdown-menu-height(10);
  }
}

.menubar-dropdown-button-dark {
  // make it look more like OG menu
  color: rgba(255, 255, 255, 0.78); // --feather-surface-light or --feather-state-text-color-on-surface-dark
  background-color: #131736; // --feather-surface-dark
  text-transform: none;
  letter-spacing: normal;
  font-weight: 600; // 400
  font-size: 0.875rem;
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}

.btn.menubar-dropdown-button-dark {
  :deep(.btn-content) {
    // make it look more like OG menu
    color: rgba(255, 255, 255, 0.78); // --feather-surface-light or --feather-state-text-color-on-surface-dark
    text-transform: none;
    letter-spacing: normal;
    font-weight: 600;
    font-size: 0.875rem;
    padding-left: 0.1rem;
    padding-right: 0.1rem;
  }
}

.menubar-dropdown-item-content {
  padding-top: 0.33rem;
  padding-right: 1.25rem;
  padding-bottom: 0.33rem;
  padding-left: 1.25rem;
  font-size: 0.875rem;
  font-weight: 400;

  .user-notifications-icon {
    font-size: 1.25rem;
  }
}

.menubar-dropdown-item-content.menubar-padding {
  padding: 10px;
}

.notification-badge-pill {
  padding-left: 6px;
  padding-right: 6px;
  margin-left: 4px;
  margin-right: 2px;
  background-color: #ffffff;
  color: #131736; // --feather-surface-dark
  border-radius: .8rem;
}

.notification-dropdown-item {
  //  min-height: 200px;
}

.notification-dropdown-item-content {
  border-bottom: 1px solid #ececec;

  //  min-height: 200px;
  //  overflow-y: none;
  .notification-dropdown-item-content-button {
    display: flex;
    align-items: center;
    background-color: transparent;
    border: none;
    width: 100%;
  }

  i {
    width: 15px;
    height: 15px;
    margin-right: 15px;
  }
}

.badge-info {
  background-color: #17a2b8;
}

.badge-severity-indeterminate {
  background-color: #5dafdd;
}

.badge-severity-cleared {
  background-color: #cdcdd0;
}

.badge-severity-normal {
  background-color: #438953;
}

.badge-severity-warning {
  background-color: #fff000;
}

.badge-severity-minor {
  background-color: #ffd60a;
}

.badge-severity-major {
  background-color: #ff9f0a;
}

.badge-severity-critical {
  background-color: #df5251;
}
</style>

<style lang="scss">
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/styles/themes/variables";

body .feather-menu .feather-menu-dropdown {
  border-radius: 4px;

  .feather-dropdown {
    border: 1px solid rgba(0, 0, 0, .35);
  }
}

.feather-dropdown {
  .feather-list-item {
    height: auto;
    padding: 0;
  }

  .dropdown-menu-wrapper {
    padding: 0 1em;
    min-width: 400px;
    padding-top: 10px;

    &.show-more-link {
      padding-bottom: 10px;

      a {
        color: var(--feather-primary-text-on-surface)
      }
    }
  }

  .final-menu-wrapper {
    display: block;
    padding-left: 20px;
    padding-bottom: 10px;

    svg {
      margin-right: 10px;
    }
  }
}

.dropdown-info-bar {
  display: flex;
  align-items: center;
  text-align: left;
  margin-bottom: 10px;

  span {
    margin-right: 10px;
  }
}

.full-width-left {
  width: 100%;
  text-align: left;
}

.feather-menu {
  &.menubar-dropdown {
    margin-left:0;
  }
  .menubar-dropdown-button-dark {
    padding: 0 7px;
  }
}
</style>
