<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app" :ref="outsideClick" @mouseleave="resetMenuItems">
    <template v-slot:left>
      <div class="center-flex">
        <FeatherAppBarLink :icon="Logo" title="Home" class="logo-link home" type="home" :url="mainMenu.homeUrl || '/'" />
        <template v-if="mainMenu.username">
          <span class="body-large left-margin-small formatted-time">{{ mainMenu.formattedTime }}</span>
          <font-awesome-icon :icon="noticesDisplay.icon"
            :class="`${noticesDisplay.colorClass} left-margin-small bell-icon`" :title="noticesDisplay.title">
          </font-awesome-icon>
        </template>
        <Search class="search-left-margin" />
      </div>
    </template>

    <template v-slot:right>
      <a :href="computeSearchLink()" class="top-menu-link top-menu-search">Search</a>

      <template v-if="mainMenu.username">
        <!-- Normal menus -->
        <FeatherDropdown :tabIndex="0" @keyup.enter="() => onHoverMenuItem(index)" @mouseenter="() => onHoverMenuItem(index)"
          :modelValue="menuItemsHovered[index]" right v-for="menuItem,index in menuItems" :key="menuItem.name || ''"
          class="menubar-dropdown">
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              <template v-if="menuItem.icon && menuItem.iconType === 'feather' && menuItem.icon === 'Person'">
                <FeatherIcon :icon="Person" />
              </template>
              <template v-if="menuItem.icon && menuItem.iconType === 'fa'">
                <font-awesome-icon :icon="`fa-solid ${menuItem.icon}`"></font-awesome-icon>
              </template>
              {{ menuItem.name }}
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>
          <FeatherDropdownItem v-for="item in menuItem.items" :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)">
            <div class="menubar-dropdown-item-content menubar-padding">
              <a :href="computeLink(item.url || '', item.isVueLink)" class="dropdown-menu-link">
                <template v-if="item.icon && item.iconType === 'fa'">
                  <font-awesome-icon :icon="`fa-solid ${item.icon}`"></font-awesome-icon>
                </template>
                {{ item.name }}
                </a>
            </div>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- Plugins menu -->
        <FeatherDropdown v-if="plugins && plugins.length" class="menubar-dropdown-dark"
          @mouseenter="hoverItem(PluginIndex)" :modelValue="hoveredItems[PluginIndex]">
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              Plugins
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>
          <FeatherDropdownItem v-for="plugin of plugins" :key="plugin.extensionId"
            @click="onMenuItemClick(computePluginRelLink(plugin))">

            <div class="menubar-dropdown-item-content menubar-padding">
              <a :href="computeLink(computePluginRelLink(plugin))" class="dropdown-menu-link">
                <FeatherIcon :icon="UpdateUtilities" />
                <span class="left-margin-small">
                  {{ plugin.menuEntry }}
                </span>
              </a>
            </div>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- Help menu -->
        <FeatherDropdown v-if="mainMenu.helpMenu" class="menubar-dropdown-dark help-menu" @mouseenter="hoverItem(HelpIndex)"
          :modelValue="hoveredItems[HelpIndex]">
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              {{ mainMenu.helpMenu.name }}
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>
          <FeatherDropdownItem v-for="item in mainMenu.helpMenu.items" :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)">
            <div class="menubar-dropdown-item-content menubar-padding">
              <a :href="computeLink(item.url || '', item.isVueLink)" class="dropdown-menu-link">
                <template v-if="item.icon">
                  <font-awesome-icon :icon="`fa-solid ${item.icon}`"></font-awesome-icon>
                </template>
                <span :class="{'left-margin-small': item.icon}">
                  {{ item.name }}
                </span>
              </a>
            </div>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <form ref="logoutForm" name="vueLogoutForm" :action="computeLogoutFormLink()" method="post"></form>

        <!-- Self-service menu -->
        <FeatherDropdown v-if="mainMenu.selfServiceMenu" class="menubar-dropdown"
          @mouseenter="hoverItem(SelfServiceIndex)" :modelValue="hoveredItems[SelfServiceIndex]">
          <template v-slot:trigger="{ attrs, on }">
            <!-- TODO: clickable link -->
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              <template v-if="mainMenu.selfServiceMenu.icon &&
              (mainMenu.selfServiceMenu.iconType === 'feather' && mainMenu.selfServiceMenu.icon === 'Person') ||
              (mainMenu.selfServiceMenu.iconType === 'fa' && mainMenu.selfServiceMenu.icon === 'fa-user')">
                <FeatherIcon :icon="Person" />
              </template>
              {{ mainMenu.selfServiceMenu.name }}
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>
          <FeatherDropdownItem v-for="item in mainMenu.selfServiceMenu.items" :key="item.name || ''"
            @click="onSelfServiceMenuItemClick(item)">
            <div class="menubar-dropdown-item-content menubar-padding">
              <a :href="computeSelfServiceMenuLink(item)" class="dropdown-menu-link">
                <template v-if="item.icon">
                  <font-awesome-icon :icon="`fa-solid ${item.icon}`"></font-awesome-icon>
                </template>
                <span :class="{'left-margin-small': item.icon}">
                  {{ item.name }}
                </span>
              </a>
            </div>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- User notifications menu -->
        <FeatherDropdown @mouseenter="hoverItem(UserIndex)" v-if="mainMenu.userNotificationMenu"
          class="menubar-dropdown" :modelValue="hoveredItems[UserIndex]">
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              <span
                :class="['notification-badge-pill', userNotificationBadgeClass]">
                {{ notificationSummary.userUnacknowledgedCount }}
              </span>
              <span
                :class="['notification-badge-pill', teamNotificationBadgeClass]">
                {{ notificationSummary.teamUnacknowledgedCount }}
              </span>
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>

          <FeatherDropdownItem v-for="item in mainMenu.userNotificationMenu.items?.filter(i => i.id === 'user')"
            :key="item.name || ''" @click="onMenuItemClick(item.url || '', item.isVueLink)">
            <div class="menubar-dropdown-item-content">
              <a :href="computeLink(item.url || '')" class="dropdown-menu-link dropdown-menu-wrapper">
                <template v-if="item.icon">
                  <FeatherIcon :icon="Person" />
                </template>
                <span class="left-margin-small">
                  {{ notificationSummary.userUnacknowledgedCount }} notices assigned to you
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
          <FeatherDropdownItem v-for="item in mainMenu.userNotificationMenu.items?.filter(i => i.id !== 'user')"
            :key="item.name || ''" @click="onMenuItemClick(item.url || '', item.isVueLink)">
            <div class="menubar-dropdown-item-content">
              <a :href="computeLink(item.url || '')"
                class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper">
                <template v-if="item.icon && item.id === 'team'">
                  <font-awesome-icon icon="fa-solid fa-users"></font-awesome-icon>
                </template>
                <template v-if="item.icon && item.id === 'oncall'">
                  <font-awesome-icon icon="fa-solid fa-calendar"></font-awesome-icon>
                </template>
                <span class="left-margin-small">
                  <template v-if="item.id === 'team'">
                    {{ notificationSummary.teamUnacknowledgedCount }} of {{ notificationSummary.totalUnacknowledgedCount
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

        <!-- Provision/Quick add node menu -->
        <a v-if="mainMenu.provisionMenu" :href="computeLink(mainMenu.provisionMenu?.url || '')"
          class="top-menu-icon horiz-padding-small">
          <FeatherIcon :icon="AddCircleAlt" class="pointer light-dark"
            :title="`${mainMenu.provisionMenu?.name || 'Quick-Add Node'}`" />
        </a>

        <!-- Flows menu -->
        <a v-if="mainMenu.flowsMenu" :href="computeLink(mainMenu.flowsMenu?.url || '')"
          class="menu-link horiz-padding-small">
          <font-awesome-icon :icon="`fa-solid ${mainMenu.flowsMenu.icon || 'fa-minus-circle'}`" class="top-menu-icon"
            :title="`${mainMenu.flowsMenu?.name || 'Flow Management'}`"></font-awesome-icon>
        </a>

        <!-- Admin/Configuration menu -->
        <a v-if="mainMenu.configurationMenu" :href="computeLink(mainMenu.configurationMenu.url || '')"
          class="menu-link horiz-padding-small menubar-cogs">
          <font-awesome-icon :icon="`fa-solid ${mainMenu.configurationMenu.icon || 'fa-cogs'}`" class="top-menu-icon"
            :title="`${mainMenu.configurationMenu?.name || 'Configure OpenNMS'}`"></font-awesome-icon>
        </a>
      </template>

      <!--<FeatherIcon :icon="LightDarkMode" title="Toggle Light/Dark Mode" class="pointer light-dark"
        @click="toggleDarkLightMode(null)" /> -->
    </template>
  </FeatherAppBar>
</template>

<script setup lang="ts">
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import AddCircleAlt from '@featherds/icon/action/AddCircleAlt'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import UpdateUtilities from '@featherds/icon/action/UpdateUtilities'
import Person from '@featherds/icon/action/Person'
import Logo from '@/assets/LogoBluebird.vue'
import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import Search from './Search.vue'

import {
  MainMenu,
  MenuItem,
  TopMenuItem,
  NoticeStatusDisplay,
  NotificationSummary,
  OnmsNotification
} from '@/types/mainMenu'
import { useOutsideClick } from '@featherds/composables/events/OutsideClick'

const appStore = useAppStore()
const menuStore = useMenuStore()
const pluginStore = usePluginStore()
const theme = ref('')
const lastShift = reactive({ lastKey: '', timeSinceLastKey: 0 })
const light = 'open-light'
const dark = 'open-dark'
const maxNotifications = 2
const logoutForm = ref()
const outsideClick = ref()
const HelpIndex = 0
const SelfServiceIndex = 1
const UserIndex = 2
const PluginIndex = 3

useOutsideClick(outsideClick.value, () => {
  resetMenuItems()
})
const plugins = computed<Plugin[]>(() => pluginStore.plugins)

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const menuItems = computed<TopMenuItem[]>(() => {
  if (mainMenu.value && mainMenu.value.menus) {
    return mainMenu.value.menus?.filter((m: TopMenuItem) => m.name !== 'Search')
  } else {
    return []
  }
})

const menuItemsHovered = ref<Array<boolean>>([])
const hoveredItems = ref<Array<boolean>>([])
const resetMenuItems = () => {
  for (let i = 0; i < menuItemsHovered.value.length; i++) {
    menuItemsHovered.value[i] = false
  }
  for (let i = 0; i < hoveredItems.value.length; i++) {
    hoveredItems.value[i] = false
  }
}
const onHoverMenuItem = (key: number) => {
  resetMenuItems()
  menuItemsHovered.value[key] = true
}
const hoverItem = (key: number) => {
  resetMenuItems()
  hoveredItems.value[key] = true
}

const notificationSummary = computed<NotificationSummary>(() => menuStore.notificationSummary)

const noticesDisplay = computed<NoticeStatusDisplay>(() => {
  const status = mainMenu.value?.noticeStatus

  if (status === 'On') {
    return {
      icon: 'fa-solid fa-bell',
      colorClass: 'alarm-ok',
      title: 'Notices: On'
    }
  } else if (status === 'Off') {
    return {
      icon: 'fa-solid fa-bell-slash',
      colorClass: 'alarm-error',
      title: 'Notices: Off'
    }
  }

  // 'Unknown'
  return {
    icon: 'fa-solid fa-bell',
    colorClass: '',
    title: ''
  }
})

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

const toggleDarkLightMode = (savedTheme: string | null) => {
  const el = document.body
  const newTheme = theme.value === light ? dark : light

  if (savedTheme && (savedTheme === light || savedTheme === dark)) {
    theme.value = savedTheme
    el.classList.add(savedTheme)
    return
  }

  // set the new theme on the body
  el.classList.add(newTheme)

  // remove the current theme
  if (theme.value) {
    el.classList.remove(theme.value)
  }

  // save the new theme in data and localStorage
  theme.value = newTheme
  localStorage.setItem('theme', theme.value)
  appStore.setTheme(theme.value)
}

const computeLink = (url: string, isVueLink?: boolean | null) => {
  const baseLink = (isVueLink ? import.meta.env.VITE_VUE_BASE_URL : (mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL)) || ''
  return `${baseLink}${url}`
}

const computePluginRelLink = (plugin: Plugin) => {
  return `ui/#/plugins/${plugin.extensionId}/${plugin.resourceRootPath}/${plugin.moduleFileName}`
}

const computeLogoutFormLink = () => {
  const logoutMenu = mainMenu.value.selfServiceMenu?.items?.find(x => x.id === 'logout')
  const baseLink = logoutMenu?.url || 'j_spring_security_logout'

  return computeLink(baseLink, false)
}

const computeSelfServiceMenuLink = (item: MenuItem) => {
  if (item.id === 'logout') {
    return 'javascript:document.vueLogoutForm.submit()'
  }

  return computeLink(item.url || '', item.isVueLink)
}

const computeSearchLink = () => {
  if (mainMenu.value.menus) {
    const searchMenus = mainMenu.value?.menus.filter(m => m.name === 'Search')
    if (searchMenus && searchMenus.length > 0) {
      return computeLink(searchMenus[0].url || '')
    }
  }

  return ''
}

const onMenuItemClick = (url: string, isVueLink?: boolean | null) => {
  const link = computeLink(url, isVueLink)
  window.location.assign(link)
}

const onSelfServiceMenuItemClick = (item: MenuItem) => {
  if (item.id === 'logout') {
    logoutForm.value.submit()
    return
  }

  onMenuItemClick(item.url || '', item.isVueLink)
}

const onNotificationItemClick = (item: OnmsNotification) => {
  const url = `notification/detail.jsp?notice=${item.id}`
  onMenuItemClick(url)
}

const clearShiftCheck = () => {
  lastShift.lastKey = ''
  lastShift.timeSinceLastKey = 0
}

/**
 * Used to focus the search bar at the top of the page when the user hits either shift
 * key in quick succession (less than 2000 ms). Only stores a single shift keypress, ignores
 * all other input. Upon detection of the second shift keypress, it clears all stored values,
 * focuses the search box in the MenuBar and returns to its default state.
 * 
 * Logic:
 * If user presses either left or right shift key and we're in a default state, store it in temporary memory, 
 * along with the time it was pressed.
 * 
 * If user presses any other key, clear stored values and stored timestamp.
 * 
 * If user's last keypress was a shift key, 
 * but they take longer than the variable shiftDelay (currently 2000ms), 
 * clear state and return to default.
 * 
 * If the user presses a shift key, directly after pressing a shift key, 
 * focus the search box, clear the values and return to a default state.
 * 
 */
const shiftCheck = (e: KeyboardEvent) => {
  const shiftCodes = ['ShiftLeft', 'ShiftRight']
  const shiftDelay = 2000
  if (shiftCodes.includes(e.code)) {
    if (shiftCodes.includes(lastShift.lastKey)) {
      if (Date.now() - lastShift.timeSinceLastKey < shiftDelay) {
        clearShiftCheck()
        const elem: HTMLInputElement | null = document.querySelector('.menubar-search textarea')
        if (elem) {
          elem.focus()
        }
      } else {
        clearShiftCheck()
      }
    } else {
      lastShift.lastKey = e.code
      lastShift.timeSinceLastKey = Date.now()
    }
  } else {
    clearShiftCheck()
  }
}

onMounted(async () => {
  const savedTheme = localStorage.getItem('theme')
  toggleDarkLightMode(savedTheme)
  window.addEventListener('keyup', shiftCheck)
})



</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
@import "@featherds/dropdown/scss/mixins";

.return-btn {
  background: var($secondary-variant);
  color: var($primary-text-on-color) !important;
  margin-right: 20px;
}

.alarm-error {
  color: var($error);
}

.alarm-ok {
  color: var($success);
}

.dropdown-menu-link {
  color: var($primary-text-on-surface) !important;

  &:hover {
    text-decoration: none;
  }
}

.left-margin-small {
  margin-left: 4px;
}

.horiz-padding-small {
  padding-left: 4px;
  padding-right: 4px;
}

.search-left-margin {
  margin-left: 60px;
}

.menu-link {
  color: var($primary-text-on-color) !important;
  background-color: var(--feather-surface-dark);
  margin-left: 2px;
  // make it look more like OG menu
  text-transform: none;
  letter-spacing: normal;
  font-weight: 400;
  font-size: .875rem;
}

.top-menu-link,
a.top-menu-link:visited {
  color: rgba(255, 255, 255, 0.78);
  font-weight: 400;
  font-size: .875rem;
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
  font-weight: 400;
  font-size: 0.875rem;
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}


.menubar-dropdown-item-content {
  padding-top: 0.33rem;
  padding-right: 1.25rem;
  padding-bottom: 0.33rem;
  padding-left: 1.25rem;
  font-size: 0.875rem;
  font-weight: 400;
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

.font-weight-bold,
.font-weight-bold span {
  font-weight: 800;
}

.row {
  display: flex;
}

.column {
  display: flex;
}

.column-label {
  display: flex;
  min-width: 100px;
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

body {
  background: var($background);
}

.open-light {
  @include open-light;
}

.open-dark {
  @include open-dark;
}

.light-dark {
  font-size: 24px;
  margin-top: 2px;
}

.header .header-content {
  padding-left: 1rem;
  padding-right: 1rem;
}

.banner .header {
  .logo-link.home {
    padding-left: 0;
    margin-right: 1rem;
    padding-top: 2px;
    padding-bottom: 0;
    padding-right: 0;
  }

  .body-large.formatted-time {
    margin-right: 1rem;
  }
}

body .feather-menu .feather-menu-dropdown {
  border-radius: 4px;

  .feather-dropdown {
    border: 1px solid rgba(0, 0, 0, .35);
  }
}

.center-horiz {
  a.top-menu-icon.add-circle {
    color: hsla(0, 0%, 100%, .5);

    svg {
      background-color: hsla(0, 0%, 100%, 0.5);
      border-radius: 50%;
      height: 20px;
      width: 20px;

      path:first-child {
        fill: rgb(19, 23, 54);
      }

      path:last-child {
        fill: transparent;
      }
    }
  }
}

// remove elevation from menubar
.header-wrapper.feather-app-bar-wrapper .header {
  box-shadow: none;
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

.center-flex {
  display: flex;
  align-items: center;
  padding-top: 3px;
}

.full-width-left {
  width: 100%;
  text-align: left;
}

.menubar-cogs {
  display: flex;
  align-items: center;

  svg {
    width: 23px;
    height: 23px;
  }
}

a.top-menu-icon svg.feather-icon {
  color: #FFF;
}

.feather-menu {
  &.menubar-dropdown {
    margin-left:0;
  }
  .menubar-dropdown-button-dark {
    padding: 0 7px;
  }
}

.header-content {
  .right.center-horiz {
    margin-right:2px;
  }
    .top-menu-search {
      margin-right:5px;
    }
}
</style>
