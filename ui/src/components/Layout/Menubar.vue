<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="logo" title="Home" type="home" url="/" />
      <template v-if="mainMenu.username">
        <span class="body-large left-margin-small">{{ mainMenu.formattedTime }}</span>
        <font-awesome-icon
          :icon="noticesDisplay.icon"
          :class="`${noticesDisplay.colorClass} left-margin-small`"
          :title="noticesDisplay.title"
        ></font-awesome-icon>
      </template>
      <Search v-if="!route.fullPath.includes('/map')" class="search-left-margin" />
     </template>

    <template v-slot:right>
      <a :href="computeSearchLink()" class="top-menu-link">Search</a>

      <template v-if="mainMenu.username">
        <!-- Normal menus -->
        <FeatherDropdown
          v-for="menuItem in menuItems"
          :key="menuItem.name || ''"
          class="menubar-dropdown"
        >
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
          <FeatherDropdownItem
            v-for="item in menuItem.items"
            :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)"
          >
            <a :href="computeLink(item.url || '', item.isVueLink)" class="dropdown-menu-link">{{ item.name }}</a>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- Help menu -->
        <FeatherDropdown
          v-if="mainMenu.helpMenu"
          class="menubar-dropdown-dark"
        >
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              {{ mainMenu.helpMenu.name }}
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>
          <FeatherDropdownItem
            v-for="item in mainMenu.helpMenu.items"
            :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)"
          >
            <a :href="computeLink(item.url || '', item.isVueLink)" class="dropdown-menu-link">
              <template v-if="item.icon">
                <font-awesome-icon :icon="`fa-solid ${item.icon}`"></font-awesome-icon>
              </template>
              <span :class="{'left-margin-small': item.icon}">
                {{ item.name }}
              </span>
            </a>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- Self-service menu -->
        <FeatherDropdown
          v-if="mainMenu.selfServiceMenu"
          class="menubar-dropdown"
        >
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
          <FeatherDropdownItem
            v-for="item in mainMenu.selfServiceMenu.items"
            :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)"
          >
            <a :href="computeLink(item.url || '', item.isVueLink)" class="dropdown-menu-link">
              <template v-if="item.icon">
                <font-awesome-icon :icon="`fa-solid ${item.icon}`"></font-awesome-icon>
              </template>
              <span :class="{'left-margin-small': item.icon}">
                {{ item.name }}
              </span>
            </a>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- User notifications menu -->
        <FeatherDropdown
          v-if="mainMenu.userNotificationMenu"
          class="menubar-dropdown">
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on" class="menubar-dropdown-button-dark">
              <span :class="{'notification-badge-pill': true, 'badge-severity-minor': notificationSummary.userUnacknowledgedCount > 0}">
                {{ notificationSummary.userUnacknowledgedCount }}
              </span>
              <span :class="{'notification-badge-pill': true, 'badge-severity-minor': notificationSummary.teamUnacknowledgedCount > 0}">
                {{ notificationSummary.teamUnacknowledgedCount }}
              </span>
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>

          <FeatherDropdownItem
            v-for="item in mainMenu.userNotificationMenu.items?.filter(i => i.id === 'user')"
            :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)"
          >
            <a :href="computeLink(item.url || '')" class="dropdown-menu-link">
              <template v-if="item.icon">
                <FeatherIcon :icon="Person" />
              </template>
              <span class="left-margin-small">
                {{ notificationSummary.userUnacknowledgedCount }} notices assigned to you
              </span>
            </a>
          </FeatherDropdownItem>

          <!-- user notifications -->
          <FeatherDropdownItem
            v-for="item in notificationSummary.userUnacknowledgedNotifications.notification"
            :key="item.id || ''"
            class="notification-dropdown-item"
            @click="onNotificationItemClick(item)"
          >
              <template #default>
                <div class="notification-dropdown-item-content">
                  <span :class="`notification-badge-pill badge-severity-${item.severity.toLocaleLowerCase()}`">
                    &nbsp;
                  </span>
                  <span class="font-weight-bold left-margin-small">
                  {{ new Date(item.pageTime).toLocaleDateString() }} {{ new Date(item.pageTime).toLocaleTimeString() }}
                  </span>
                  <a href="#" @click="onNotificationItemClick(item)">
                    <FeatherIcon :icon="View" class="left-margin-small" />
                  </a>
                  <!--
                  <br />>
                  <span class="row">{{ item.notificationName }}</span>
                  <span class="row">{{ item.nodeLabel }}</span>
                  <span class="row">{{ item.ipAddress }}</span>
                  <span class="row">{{ item.serviceType?.name }}</span>
                  -->
                </div>
              
              </template>
          </FeatherDropdownItem>

          <!-- Team and On-Call links -->
          <FeatherDropdownItem
            v-for="item in mainMenu.userNotificationMenu.items?.filter(i => i.id !== 'user')"
            :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)"
          >
            <a :href="computeLink(item.url || '')" class="dropdown-menu-link">
              <template v-if="item.icon && item.id === 'team'">
                <font-awesome-icon icon="fa-solid fa-users"></font-awesome-icon>
              </template>
              <template v-if="item.icon && item.id === 'oncall'">
                <font-awesome-icon icon="fa-solid fa-calendar"></font-awesome-icon>
              </template>
              <span class="left-margin-small">
                <template v-if="item.id === 'team'">
                    {{ notificationSummary.teamUnacknowledgedCount }} of {{ notificationSummary.totalUnacknowledgedCount }} assigned to anyone but you
                </template>
                <template v-if="item.id === 'oncall'">
                    {{ item.name }}
                </template>
              </span>
            </a>
          </FeatherDropdownItem>
        </FeatherDropdown>

        <!-- Provision/Quick add node menu -->
        <a
          v-if="mainMenu.provisionMenu"
          :href="computeLink(mainMenu.provisionMenu?.url || '')"
          class="top-menu-icon horiz-padding-small"
        >
          <FeatherIcon
            :icon="AddCircleAlt"
            class="pointer light-dark"
          />
        </a>

        <!-- Flows menu -->
        <a
          v-if="mainMenu.flowsMenu"
          :href="computeLink(mainMenu.flowsMenu?.url || '')"
          class="menu-link horiz-padding-small"
        >
          <font-awesome-icon
            :icon="`fa-solid ${mainMenu.flowsMenu.icon || 'fa-minus-circle'}`"
            class="top-menu-icon"
            :title="`${mainMenu.flowsMenu?.name || 'Flow Management'}`"
          ></font-awesome-icon>
        </a>

        <!-- Admin/Configuration menu -->
        <a
          v-if="mainMenu.configurationMenu"
          :href="computeLink(mainMenu.configurationMenu.url || '')"
          class="menu-link horiz-padding-small"
        >
          <font-awesome-icon
            :icon="`fa-solid ${mainMenu.configurationMenu.icon || 'fa-cogs'}`"
            class="top-menu-icon"
            :title="`${mainMenu.configurationMenu?.name || 'Configure OpenNMS'}`"
          ></font-awesome-icon>
        </a>
      </template>

      <!--
      <FeatherButton @click="returnHandler" class="return-btn">Back to main page</FeatherButton>
      -->

      <FeatherIcon
        :icon="LightDarkMode"
        title="Toggle Light/Dark Mode"
        class="pointer light-dark"
        @click="toggleDarkLightMode(null)"
      />
    </template>

  </FeatherAppBar>
  <FeatherDialog v-model="notificationDialogVisible" :labels="notificationDialogLabels">
    <template #default>
      <div class="dialog-content-container">
        <div class="row">
            <p>Notification {{ notificationDialogItem.id }}</p>
          <span :class="`fa fa-circle text-severity-${notificationDialogItem.severity}`"></span>
          <font-awesome-icon
            icon="fa-solid fa-circle"
            :class="`'menu-link text-severity-'${notificationDialogItem.severity ? notificationDialogItem.severity.toLowerCase() : 'indeterminate'}`"
            :title="notificationDialogItem.severity"
          ></font-awesome-icon>
        </div>
        <div class="row">
          <span class="font-weight-bold">
          {{ new Date(notificationDialogItem.pageTime).toLocaleDateString() }} {{ new Date(notificationDialogItem.pageTime).toLocaleTimeString() }}
          </span>
        </div>
        <div class="row-container">
          <div class="row">Name: {{ notificationDialogItem.notificationName }}</div>
          <div class="row">Node: {{ notificationDialogItem.nodeLabel }}</div>
          <div class="row">IP Address: {{ notificationDialogItem.ipAddress }}</div>
          <div class="row">Service: {{ notificationDialogItem.serviceType?.name }}</div>
        </div>
        <div class="row">
          <span>Details:</span>
          <a
            :href="computeLink(`notification/detail.jsp?notice=${notificationDialogItem.id}`)"
            class="dropdown-menu-link left-margin-small"
          >{{ notificationDialogItem.notificationName }}</a>
        </div>
      </div>
    </template>
    <template #footer>
      <FeatherButton primary @click="notificationDialogVisible = false"
        >Close</FeatherButton
      >
    </template>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import AddCircleAlt from '@featherds/icon/action/AddCircleAlt'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import LightDarkMode from '@featherds/icon/action/LightDarkMode'
import View from '@featherds/icon/action/View'
import Person from '@featherds/icon/action/Person'
import Logo from '@/assets/Logo.vue'
import Search from './Search.vue'
import { useStore } from 'vuex'
import {
  MainMenu,
  TopMenuItem,
  NoticeStatusDisplay,
  NotificationSummary,
  OnmsNotification
} from '@/types/mainMenu'

const store = useStore()
const route = useRoute()
const returnHandler = () => window.location.href = '/opennms/'
const logo = Logo
const theme = ref('')
const light = 'open-light'
const dark = 'open-dark'
const notificationDialogVisible = ref(false)
const notificationDialogLabels = ref({
  title: 'Notification Dialog',
  close: 'Close'
})
const notificationDialogItem = ref({} as OnmsNotification)

const mainMenu = computed<MainMenu>(() => store.state.menuModule.mainMenu)
const menuItems = computed<TopMenuItem[]>(() => {
  if (store.state.menuModule.mainMenu && store.state.menuModule.mainMenu.menus) {
    return store.state.menuModule.mainMenu.menus?.filter((m : TopMenuItem) => m.name !== 'Search')
  } else {
    return []
  }
})

const notificationSummary = computed<NotificationSummary>(() => store.state.menuModule.notificationSummary)

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
  store.dispatch('appModule/setTheme', theme.value)
}

const computeLink = (url: string, isVueLink?: boolean | null) => {
  const baseLink = (isVueLink ? import.meta.env.VITE_VUE_BASE_URL : (mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL)) || ''
  return `${baseLink}${url}`
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

const onNotificationItemClick = (item: OnmsNotification) => {
  notificationDialogLabels.value.title = `Notification ${item.id}`
  notificationDialogItem.value = item
  notificationDialogVisible.value = true
}

onMounted(async () => {
  const savedTheme = localStorage.getItem('theme')
  toggleDarkLightMode(savedTheme)
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
.top-menu-link, a.top-menu-link:visited {
  color: #ffffff;
  margin-left: 2px;
  font-weight: 400;
  font-size: .875rem;
}

.top-menu-icon {
  color: #ffffff;
  margin-left: 2px;
}
.menubar-dropdown {
  margin-left: 2px;

  :deep(.feather-dropdown)  {
    @include dropdown-menu-height(10);
  }
}

.menubar-dropdown-dark {
  margin-left: 2px;

  :deep(.feather-dropdown)  {
    @include dropdown-menu-height(10);
  }
}
.menubar-dropdown-button-dark {
  // make it look more like OG menu
  color: rgba(255, 255, 255, 0.78); // --feather-surface-light or --feather-state-text-color-on-surface-dark
  background-color: #131736;   // --feather-surface-dark
  text-transform: none;
  letter-spacing: normal;
  font-weight: 400;
  font-size: 0.875rem;
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}
.notification-badge-pill {
  padding-left: 6px;
  padding-right: 6px;
  margin-left: 4px;
  margin-right: 2px;
  background-color: #ffffff;
  color: #131736;   // --feather-surface-dark
  border-radius: .8rem;
}
.notification-dropdown-item {
//  min-height: 200px;
}
.notification-dropdown-item-content {
//  min-height: 200px;
//  overflow-y: none;
}

.dialog-content-container {
  display: flex;
  flex-direction: column;
  min-width: 400px;
}
.row {
  display: flex;
}
.row-container {
  display: flex;
  flex-direction: column;
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
</style>
