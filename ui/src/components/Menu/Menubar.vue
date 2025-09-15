<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app" :ref="outsideClick" @mouseleave="resetMenuItems">
    <template v-slot:left>
      <div class="center-flex">
        <FeatherAppBarLink :icon="IconLogo" title="Home" class="logo-link home" type="home" :url="mainMenu.homeUrl || '/'" />
      </div>
    </template>

    <template v-slot:center>
        <Search class="search-left-margin" id="onms-central-search-control" />

        <!-- Provision/Quick add node menu -->
        <div v-if="displayAddNodeButton" class="quick-add-node-wrapper">
          <FeatherButton
            primary
            v-if="mainMenu.provisionMenu"
            @click="onAddNode"
          >Add a Node</FeatherButton>
        </div>
    </template>

    <template v-slot:right>
      <div class="date-wrapper">
        <div class="date-formatted-time">{{ formattedTime }}</div>
        <div class="date-formatted-date">{{ formattedDate }}</div>
      </div>
      <template v-if="mainMenu.username">
        <UserNotificationsMenuItem :ref="userNotificationsMenu" />
        <UserSelfServiceMenuItem />
      </template>

      <!-- <FeatherIcon :icon="LightDarkMode" title="Toggle Light/Dark Mode" class="pointer light-dark"
        @click="toggleDarkLightMode(null)" /> -->
    </template>
  </FeatherAppBar>
</template>

<script setup lang="ts">
import { useOutsideClick } from '@featherds/composables/events/OutsideClick'
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'

import IconLogo from '@/assets/LogoHorizon.vue'
import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu } from '@/types/mainMenu'
import Search from './Search.vue'
import UserNotificationsMenuItem from './UserNotificationsMenuItem.vue'
import UserSelfServiceMenuItem from './UserSelfServiceMenuItem.vue'

const appStore = useAppStore()
const menuStore = useMenuStore()
const theme = ref('')
const lastShift = reactive({ lastKey: '', timeSinceLastKey: 0 })
const light = 'open-light'
const dark = 'open-dark'
const outsideClick = ref()
const userNotificationsMenu = ref()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const displayAddNodeButton = computed(() => (mainMenu?.value.displayAddNodeButton ?? false))

const formattedDate = computed<string>(() => mainMenu.value?.formattedDate ?? '')
const formattedTime = computed<string>(() => mainMenu.value?.formattedTime ?? '')

useOutsideClick(outsideClick.value, () => {
  resetMenuItems()
})

const resetMenuItems = () => {
  userNotificationsMenu.value?.hideMenu()
}

const onAddNode = () => {
  const url = computeLink(mainMenu.value.provisionMenu?.url || '')
  window.location.assign(url)
}

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

const computeLink = (url: string) => {
  const baseLink = mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL || ''
  return `${baseLink}${url}`
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

        const elem: HTMLInputElement | null = document.querySelector('#opennms-sidemenu-container .onms-search-input-wrapper input.search-input')

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
  console.log('Got theme: ', savedTheme)

  toggleDarkLightMode(savedTheme)
  window.addEventListener('keyup', shiftCheck)
})
</script>

<style lang="scss" scoped>
@use "@featherds/styles/mixins/typography" as typo;
@import "@featherds/dropdown/scss/mixins";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.alarm-error {
  background-color: var($error);
  color: var($primary-text-on-color) !important;
}

.alarm-ok {
  background-color: var($success);
  color: var($primary-text-on-color) !important;
}

.alarm-unknown {
  background-color: var($indeterminate);
  color: var($primary-text-on-color) !important;
}

.search-left-margin {
  margin-left: 1em;
}

.notifications-icon-wrapper {
  padding: 0.5em;
}

.quick-add-node-wrapper {
  margin-left: 1em;
  margin-right: 1em;

  .btn {
    :deep(.btn-content) {
      @include typo.button();
      color: var(--feather-primary-text-on-color);
      font-family: var(--feather-header-font-family);
      font-size: var(--feather-button-font-size);
      font-weight: var(--feather-button-font-weight);
      letter-spacing: var(--feather-button-letter-spacing);
    }
  }
}

.notice-status-display {
  font-size: 2em;
  border-radius: 1.5em;
  padding: 0.1em;
}

.date-wrapper {
  display: inline-flex;
  flex-direction: column;
  font-family: var(--feather-header-font-family);
  font-size: 0.875rem;
  margin-right: 1em;

  .date-formatted-date {
    display: flex;
    justify-content: right;
  }

  .date-formatted-time {
    display: flex;
    justify-content: right;
    font-weight: 800;
  }
}
</style>

<style lang="scss">
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/styles/themes/variables";

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
  max-width: 100%;
}

.banner .header {
  .logo-link.home {
    padding-left: 0;
    margin-right: 1rem;
    padding-top: 2px;
    padding-bottom: 0;
    padding-right: 0;
  }
}

// remove elevation from menubar
.header-wrapper.feather-app-bar-wrapper .header {
  box-shadow: none;
}

.header-wrapper.feather-app-bar-wrapper a.skip {
  display: none;
}

.center-flex {
  display: flex;
  align-items: center;
  padding-top: 3px;
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
