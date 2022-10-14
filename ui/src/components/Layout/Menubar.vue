<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="logo" title="Home" type="home" url="/" />
      <template v-if="mainMenu.username">
        <span class="body-large">{{ mainMenu.formattedTime }}</span>
        <font-awesome-icon
          :icon="noticesDisplay.icon"
          :class="noticesDisplay.colorClass"
          :title="noticesDisplay.title"
        ></font-awesome-icon>
      </template>
      <Search v-if="!route.fullPath.includes('/map')" />
     </template>

    <template v-slot:right>
      <a :href="computeSearchLink()" class="menu-link">Search</a>

      <template v-if="mainMenu.username">
        <!-- Normal menus -->
        <FeatherDropdown
          v-for="menuItem in menuItems"
          :key="menuItem.name || ''"
          class="menubar-dropdown"
        >
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on">
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
          class="menubar-dropdown"
        >
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton link href="#" v-bind="attrs" v-on="on">
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
            <FeatherButton link href="#" v-bind="attrs" v-on="on">
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
            <FeatherButton link href="#" v-bind="attrs" v-on="on">
              {{ notificationSummary.userUnacknowledgedCount }} {{ notificationSummary.teamUnacknowledgedCount }}
              <FeatherIcon :icon="ArrowDropDown" />
            </FeatherButton>
          </template>

          <FeatherDropdownItem
            v-for="item in mainMenu.userNotificationMenu.items"
            :key="item.name || ''"
            @click="onMenuItemClick(item.url || '', item.isVueLink)"
          >
            <a :href="computeLink(item.url || '')" class="dropdown-menu-link">
              <template v-if="item.icon && item.id === 'user'">
                <FeatherIcon :icon="Person" />
              </template>
              <template v-if="item.icon && item.id === 'team'">
                <font-awesome-icon icon="fa-solid fa-users"></font-awesome-icon>
              </template>
              <template v-if="item.icon && item.id === 'oncall'">
                <font-awesome-icon icon="fa-solid fa-calendar"></font-awesome-icon>
              </template>
              <span class="left-margin-small">
                <template v-if="item.id === 'user'">
                  {{ notificationSummary.userUnacknowledgedCount }} notices assigned to you
                </template>
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
          class="menu-link"
        >
          <FeatherIcon
            :icon="AddCircleAlt"
            class="pointer light-dark"
          />
        </a>

        <!-- Admin/Configuration menu -->
        <a
          v-if="mainMenu.configurationMenu"
          :href="computeLink(mainMenu.configurationMenu.url || '')"
          class="menu-link"
        >
          <font-awesome-icon
            :icon="`fa-solid ${mainMenu.configurationMenu.icon || 'fa-cogs'}`"
            class="menu-link"
            :title="`${mainMenu.configurationMenu?.name} || 'Configure OpenNMS'`"
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
</template>

<script setup lang="ts">
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import AddCircleAlt from '@featherds/icon/action/AddCircleAlt'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import LightDarkMode from '@featherds/icon/action/LightDarkMode'
import Person from '@featherds/icon/action/Person'
import Logo from '@/assets/Logo.vue'
import Search from './Search.vue'
import { useStore } from 'vuex'
import { MainMenu, TopMenuItem, NoticeStatusDisplay, NotificationSummary } from '@/types/mainMenu'

const store = useStore()
const route = useRoute()
const returnHandler = () => window.location.href = '/opennms/'
const logo = Logo
const theme = ref('')
const light = 'open-light'
const dark = 'open-dark'
// TODO: use username from store
const username = ref('admin1')
const noticesCountUser = ref(0)
const noticesCountOther = ref(1)

//const dateMillis = computed<number>(() => (new Date()).getTime())
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
.menu-link {
  color: var($primary-text-on-color) !important;
  margin-left: 2px;
}
.menubar-dropdown {
  margin-left: 2px;

  :deep(.feather-dropdown)  {
    @include dropdown-menu-height(10);
  }
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
