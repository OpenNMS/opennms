<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="logo" title="Home" type="home" url="/" />
      <span class="body-large" v-date>{{ dateMillis }}</span>
      <font-awesome-icon
        :icon="noticesDisplay.icon"
        :class="noticesDisplay.colorClass"
        :title="noticesDisplay.title"
      ></font-awesome-icon>
      <Search v-if="!route.fullPath.includes('/map')" />
     </template>

    <template v-slot:right>
      <a :href="computeLink(mainMenu.searchLink)" class="menu-link">Search</a>

      <FeatherDropdown
        v-for="menuItem in menuItems"
        :key="menuItem.name"
        class="menubar-dropdown"
      >
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            <template v-if="menuItem.icon && menuItem.icon === 'Person'">
              <FeatherIcon :icon="Person" />
            </template>
            <template v-if="menuItem.icon && menuItem.icon.startsWith('fa-')">
              <font-awesome-icon :icon="`fa-solid ${menuItem.icon}`"></font-awesome-icon>
            </template>
            {{ menuItem.name }}
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem
          v-for="item in menuItem.items"
          :key="item.name"
          @click="onMenuItemClick(item.url, item.isVueLink)"
        >
          <a :href="computeLink(item.url, item.isVueLink)" class="dropdown-menu-link">{{ item.name }}</a>
        </FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            {{ mainMenu.countNoticesAssignedToOtherThanUser }} {{ mainMenu.countNoticesAssignedToOtherThanUser }}
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem
          @click="onMenuItemClick(mainMenu.noticesAssignedToUserLink)"
        >
          <a
            :href="computeLink(mainMenu.noticesAssignedToUserLink)"
            class="dropdown-menu-link"
          >
            <FeatherIcon :icon="Person" />
            <span class="left-margin-small">
              {{ mainMenu.countNoticesAssignedToUser }} notices assigned to you
            </span>
          </a>
        </FeatherDropdownItem>
        <FeatherDropdownItem
          @click="onMenuItemClick(mainMenu.noticesAssignedToOtherThanUserLink)"
        >
          <a
            :href="computeLink(mainMenu.noticesAssignedToOtherThanUserLink)"
            class="dropdown-menu-link"
          >
            <font-awesome-icon icon="fa-solid fa-users"></font-awesome-icon>
            <span class="left-margin-small">
              {{ mainMenu.countNoticesAssignedToOtherThanUser }} of {{ mainMenu.countNoticesAssignedToOtherThanUser }} assigned to anyone but you
            </span>
          </a>
        </FeatherDropdownItem>
        <FeatherDropdownItem
          @click="onMenuItemClick(mainMenu.rolesLink)"
        >
          <font-awesome-icon icon="fa-solid fa-calendar"></font-awesome-icon>
          <a
            :href="computeLink(mainMenu.rolesLink)"
            class="dropdown-menu-link left-margin-small">On-Call Schedule</a>
        </FeatherDropdownItem>
      </FeatherDropdown>

      <a
        v-if="mainMenu.quickAddNodeLink"
        :href="computeLink(mainMenu.quickAddNodeLink)"
        class="menu-link"
      >
        <FeatherIcon
          :icon="AddCircleAlt"
          class="pointer light-dark"
        />
      </a>

      <a
        v-if="mainMenu.displayAdminLink"
        :href="computeLink(mainMenu.adminLink)"
        class="menu-link"
      >
        <font-awesome-icon
          icon="fa-solid fa-cogs"
          class="menu-link"
          title="Configure OpenNMS"
        ></font-awesome-icon>
      </a>

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
import { MainMenuDefinition, MenuItemDefinition, NoticeStatusDisplay } from '@/types/mainMenu'

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

const dateMillis = computed<number>(() => (new Date()).getTime())
const mainMenu = computed<MainMenuDefinition>(() => store.state.menuModule.mainMenu)
const menuItems = computed<MenuItemDefinition[]>(() => store.state.menuModule.mainMenu.menuItems)

const noticesDisplay = computed<NoticeStatusDisplay>(() => {
  const status = mainMenu.value?.noticeStatus

  if (status === 'on') {
    return {
      icon: 'fa-solid fa-bell',
      colorClass: 'alarm-ok',
      title: 'Notices: On'
    }
  } else if (status === 'off') {
    return {
      icon: 'fa-solid fa-bell-slash',
      colorClass: 'alarm-error',
      title: 'Notices: Off'
    }
  }

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

const computeLink = (url: string, isVueLink?: boolean) => {
  const baseLink = (isVueLink ? import.meta.env.VITE_VUE_BASE_URL : import.meta.env.VITE_BASE_URL) || ''
  return `${baseLink}${url}`
}

const onMenuItemClick = (url: string, isVueLink?: boolean) => {
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
