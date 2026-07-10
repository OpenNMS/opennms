<template>
  <div ref="triggerEl" class="user-notification-badge-wrapper" @mouseenter="showMenu">
    <span
      :class="['notification-badge-pill', userNotificationBadgeClass]">
      {{ notificationSummary.userUnacknowledgedCount }}
    </span>
    <span
      :class="['notification-badge-pill', teamNotificationBadgeClass]">
      {{ notificationSummary.teamUnacknowledgedCount }}
    </span>

    <Button
      text
      class="menubar-dropdown-button-dark"
      aria-haspopup="true"
      aria-label="User notifications menu"
      @click="onTriggerClick"
    >
      <OnmsIcon
        :icon="noticeStatusDisplay?.iconComponent"
        :class="[noticeStatusDisplay?.colorClass, 'notice-status-display']"
      />

      <OnmsIcon class="user-notification-arrow-dropdown" :icon="IconArrowDropDown" />
    </Button>

    <Popover
      ref="pop"
      appendTo="self"
      class="onms-user-dropdown-panel user-notification-dropdown-panel"
      @hide="onPopoverHide"
    >
      <div class="menubar-dropdown-item-content" @click="onMenuItemClick(notificationConfigUrl)">
        <a :href="computeLink(notificationConfigUrl)" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper">
          <OnmsIcon
            :icon="noticeStatusDisplay?.iconComponent"
            :class="[noticeStatusDisplay?.colorClass, 'user-notifications-icon']"
          />

          <span class="left-margin-small">
            {{ noticeStatusDisplay?.title ?? '' }}
          </span>
        </a>
      </div>

      <div
        v-for="item in mainMenu.userNotificationMenu?.items?.filter(i => i.id === 'userNotificationUser')"
        :key="item.name || ''"
        class="menubar-dropdown-item-content"
        @click="onMenuItemClick(item.url || '')"
      >
        <a :href="computeLink(item.url || '')" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper">
          <OnmsIcon :icon="IconPerson" class="user-notifications-icon" />
          <span class="left-margin-small">
            {{ notificationSummary.userUnacknowledgedCount ?? 0 }} notices assigned to you
          </span>
        </a>
      </div>

      <!-- user notifications -->
      <div
        v-for="item in notificationSummary.userUnacknowledgedNotifications.notification.slice(0, maxNotifications)"
        :key="item.id || ''"
        class="menubar-dropdown-item-content notification-dropdown-item"
        @click="onNotificationItemClick(item)"
      >
        <div class="notification-dropdown-item-content dropdown-menu-wrapper">
          <div class="notification-dropdown-item-content-button">
            <i :class="`notification-badge-pill badge-severity-${item?.severity?.toLocaleLowerCase() ?? 'indeterminate'}`" />
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

      <div
        v-if="notificationSummary.userUnacknowledgedCount > maxNotifications"
        class="menubar-dropdown-item-content"
      >
        <div class="dropdown-menu-wrapper show-more-link notification-dropdown-item-content">
          <a :href="notificationsShowMoreLink" @click="onMenuItemClick(notificationsShowMoreLink)">Show more...</a>
        </div>
      </div>

      <!-- Team and On-Call links -->
      <div
        v-for="item in mainMenu.userNotificationMenu?.items?.filter(i => i.id !== 'userNotificationUser' && i.id !== 'userNotificationConfiguration')"
        :key="item.name || ''"
        class="menubar-dropdown-item-content"
        @click="onMenuItemClick(item.url || '')"
      >
        <a :href="computeLink(item.url || '')"
          class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper">
          <template v-if="item.id === 'userNotificationTeam'">
            <OnmsIcon :icon="IconGroup" class="user-notifications-icon" />
          </template>

          <template v-if="item.id === 'userNotificationOnCall'">
            <OnmsIcon :icon="IconCalendar" class="user-notifications-icon" />
          </template>

          <span class="left-margin-small">
            <template v-if="item.id === 'userNotificationTeam'">
              {{ notificationSummary.teamUnacknowledgedCount ?? 0 }} of {{ notificationSummary.totalUnacknowledgedCount ?? 0
              }} assigned to anyone but you
            </template>
            <template v-if="item.id === 'userNotificationOnCall'">
              {{ item.name }}
            </template>
          </span>
        </a>
      </div>
    </Popover>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw, ref, watch } from 'vue'

import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import IconArrowDropDown from '@/components/icons/navigation/ArrowDropDown.vue'
import IconCalendar from '@/components/icons/action/Calendar.vue'
import IconGroup from '@/components/icons/action/Group.vue'
import IconNotificationsOff from '@/components/icons/notification/NotificationsOff.vue'
import IconNotificationSelected from '@/components/icons/notification/NotificationSelected.vue'
import IconPerson from '@/components/icons/action/Person.vue'
import Button from 'primevue/button'
import Popover from 'primevue/popover'
import { useMenuStore } from '@/stores/menuStore'
import {
  MainMenu,
  NoticeStatusDisplay,
  NotificationSummary,
  OnmsNotification
} from '@/types/mainMenu'

const menuStore = useMenuStore()
const maxNotifications = 2
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const props = defineProps({
  expanded: {
    required: true,
    type: Boolean
  }
})

const emit = defineEmits(['menu-show', 'menu-hide'])

const pop = ref()
const triggerEl = ref<HTMLElement>()

const showMenu = () => {
  emit('menu-show')
}

const onTriggerClick = () => {
  if (props.expanded) {
    emit('menu-hide')
  } else {
    emit('menu-show')
  }
}

const onPopoverHide = () => {
  emit('menu-hide')
}

// The parent (Menubar) owns the open/close state via the `expanded` prop
// (hover to open, close on header mouseleave / other dropdown / outside click).
// Drive the Popover imperatively from that single source of truth. A synthetic
// event carrying `currentTarget` is required because Popover.show reads it.
watch(() => props.expanded, (val) => {
  if (val) {
    if (triggerEl.value) {
      pop.value?.show({ currentTarget: triggerEl.value }, triggerEl.value)
    }
  } else {
    pop.value?.hide()
  }
})

const notificationSummary = computed<NotificationSummary>(() => menuStore.notificationSummary)

const notificationConfigUrl = computed(() => {
  const item = mainMenu.value?.userNotificationMenu?.items?.find(i => i.id === 'userNotificationConfiguration')

  return item?.url ?? ''
})

const noticeStatusDisplay = computed<NoticeStatusDisplay>(() => {
  const status = mainMenu.value?.noticeStatus

  if (status === 'On') {
    return {
      icon: 'fa-solid fa-bell',
      iconComponent: markRaw(IconNotificationSelected),
      colorClass: 'alarm-ok',
      title: 'Notices: On'
    }
  } else if (status === 'Off') {
    return {
      icon: 'fa-solid fa-bell-slash',
      iconComponent: markRaw(IconNotificationsOff),
      colorClass: 'alarm-error',
      title: 'Notices: Off'
    }
  }

  // 'Unknown'
  return {
    icon: 'fa-solid fa-bell',
    iconComponent: markRaw(IconNotificationSelected),
    colorClass: 'alarm-unknown',
    title: 'Notices: Unknown'
  }
})

const notificationsShowMoreLink = computed<string>(() =>
  mainMenu.value.userNotificationMenu?.items?.filter(item => item.id === 'userNotificationUser')[0].url || ''
)

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
    .notification.map(n => severities.indexOf(n.severity?.toLowerCase() ?? 'indeterminate')) || []

  const maxSeverityIndex = Math.max.apply(Math, severityIndexList)
  const maxSeverity = severities[maxSeverityIndex]

  return `badge-severity-${maxSeverity}`
})

const teamNotificationBadgeClass = computed<string>(() =>
  notificationSummary.value.teamUnacknowledgedCount === 0 ? 'badge-severity-cleared' : 'badge-info'
)

const computeLink = (url: string) => {
  const baseLink = mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL || ''
  return `${baseLink}${url}`
}

const onMenuItemClick = (url: string) => {
  const link = computeLink(url)
  window.location.assign(link)
}

const onNotificationItemClick = (item: OnmsNotification) => {
  const url = `notification/detail.jsp?notice=${item.id}`
  onMenuItemClick(url)
}
</script>

<style lang="scss" scoped>
@import '@/styles/onms-typography';
@import "@featherds/styles/themes/variables";

// Foreground uses PrimeVue tokens (not FeatherDS vars) so text tracks the same
// dark-mode selector that drives the Popover background. On embedded JSP pages
// the FeatherDS theme vars may not be toggled, which would leave dark-on-dark.
.dropdown-menu-link {
  color: var(--p-text-color) !important;

  &:hover {
    text-decoration: none;
  }
}

.user-notification-badge-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  margin-left: 2px;
}

// Dark trigger button (replaces FeatherButton link). Matches the OG menu look.
.menubar-dropdown-button-dark {
  color: rgba(255, 255, 255, 0.78); // --feather-surface-light or --feather-state-text-color-on-surface-dark
  background-color: transparent;
  border: none;
  text-transform: none;
  letter-spacing: normal;
  font-weight: 600;
  font-size: 0.875rem;
  padding: 0 7px;

  &:hover,
  &:focus,
  &:focus-visible {
    background-color: rgba(255, 255, 255, 0.1);
    color: #ffffff;
    box-shadow: none;
    outline: none;
  }
}

div.user-notification-badge-wrapper {
  .menubar-dropdown-button-dark {
    svg.notice-status-display.onms-icon {
      vertical-align: -0.5rem;
    }
    svg.user-notification-arrow-dropdown.onms-icon {
      vertical-align: 0;
    }
  }
}

// should match menubar-dropdown-item-content in UserSelfServiceMenu
.menubar-dropdown-item-content {
  padding-top: 0.25rem;
  padding-right: 0.5rem;
  padding-bottom: 0.25rem;
  padding-left: 0.5rem;
  font-size: 0.875rem;
  font-weight: 400;
  cursor: pointer;

  // Shaded background on hover / keyboard focus of the item row.
  &:hover,
  &:focus-within {
    background-color: var(--p-highlight-background);
  }

  .user-notifications-icon {
    font-size: 1.25rem;
  }
}

.menubar-dropdown-item-content.menubar-padding {
  padding: 10px;
}

// Notification-status colors stay on their light-theme Feather values so
// the indicator meaning doesn't change with the active theme. We re-declare
// the Feather CSS variables locally with their light-mode values and keep
// the consuming rules pointing at the Feather variable names.
.alarm-error,
.alarm-ok,
.alarm-unknown {
  --feather-primary-text-on-color: rgba(255, 255, 255, 1);
  color: var($primary-text-on-color) !important;
}

.alarm-error {
  --feather-error: #a5021f;
  background-color: var($error);
}

.alarm-ok {
  --feather-success: #0b720c;
  background-color: var($success);
}

.alarm-unknown {
  --feather-indeterminate: #0092c7;
  background-color: var($indeterminate);
}

.notice-status-display {
  font-size: 2em;
  border-radius: 1.5em;
  padding: 0.1em;
}

.notification-badge-pill {
  padding-left: 6px;
  padding-right: 6px;
  margin-left: 4px;
  margin-right: 2px;
  line-height: 1.5rem;
  font-weight: 800;
  background-color: #ffffff;
  color: #131736; // --feather-surface-dark
  border-radius: .8rem;
}

.notification-dropdown-item-content {
  border-bottom: 1px solid #ececec;

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

.dropdown-menu-wrapper {
  padding: 0 1em;
  min-width: 400px;
  padding-top: 10px;

  &.show-more-link {
    padding-bottom: 10px;

    a {
      color: var(--p-text-color)
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
// The Popover is rendered inline (appendTo="self") so it stays inside the fixed
// header's DOM subtree, preserving the header's close-on-mouseleave behavior
// (moving the pointer into the panel does not "leave" the header). PrimeVue
// positions the panel with inline absolute coords computed from viewport +
// scroll offset, which is wrong inside a fixed header — pin it under the
// trigger instead and drop the arrow (FeatherDropdown had none).
.user-notification-dropdown-panel.p-popover {
  top: 100% !important;
  inset-inline-start: auto !important;
  right: 0 !important;
  margin-top: 0 !important;

  &::before,
  &::after {
    display: none !important;
  }

  .p-popover-content {
    padding: 0.25rem 0;
    // Baseline PrimeVue-aware text color so all panel content stays legible
    // in dark mode even where FeatherDS theme vars aren't active (JSP pages).
    color: var(--p-text-color);
  }
}
</style>
