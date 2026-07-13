<template>
  <header class="onms-menubar" ref="outsideClick" @mouseleave="resetMenuItems">
    <div class="onms-menubar__left">
      <div class="center-flex">
        <a class="logo-link home" title="Home" :href="mainMenu.homeUrl || '/'">
          <IconLogo class="onms-menubar__logo" />
        </a>
      </div>
    </div>

    <div class="onms-menubar__center">
      <Search class="search-left-margin" id="onms-central-search-control" />

      <!-- Provision/Quick add node menu -->
      <div v-if="displayAddNodeButton" class="quick-add-node-wrapper">
        <Button
          v-if="mainMenu.provisionMenu"
          label="Add a Node"
          @click="onAddNode"
        />
      </div>
    </div>

    <div class="onms-menubar__right">
      <div class="date-wrapper">
        <div class="date-formatted-time">{{ formattedTime }}</div>
        <div class="date-formatted-date">{{ formattedDate }}</div>
      </div>

       <span title="Toggle Light/Dark Mode">
        <OnmsIcon
            :icon="LightDarkMode"
            title="Toggle Light/Dark Mode"
            class="light-dark"
            role="button"
            tabindex="0"
            @click="appStore.toggleTheme()"
            @keydown.enter.space.prevent="appStore.toggleTheme()"
          />
        </span>
      <template v-if="mainMenu.username">
        <UserNotificationsMenuItem
          :expanded="currentDropdownMenu === DropdownMenuType.UserNotifications"
          @menuShow="() => onMenuShow(DropdownMenuType.UserNotifications)"
          @menuHide="() => onMenuHide(DropdownMenuType.UserNotifications)"
        />

        <UserSelfServiceMenuItem
          :expanded="currentDropdownMenu === DropdownMenuType.SelfService"
          @menuShow="() => onMenuShow(DropdownMenuType.SelfService)"
          @menuHide="() => onMenuHide(DropdownMenuType.SelfService)"
        />
      </template>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'

import { useOutsideClick } from '@/composables/useOutsideClick'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import LightDarkMode from '@/components/icons/action/LightDarkMode.vue'
import Button from 'primevue/button'

// see vite.config.ts, resolve.alias for the actual logo file that is imported
import IconLogo from './src/assets/ProductLogo.vue'
import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { DropdownMenuType } from './types'
import { MainMenu } from '@/types/mainMenu'
import Search from './Search.vue'
import UserNotificationsMenuItem from './UserNotificationsMenuItem.vue'
import UserSelfServiceMenuItem from './UserSelfServiceMenuItem.vue'

const appStore = useAppStore()
const menuStore = useMenuStore()
const lastShift = reactive({ lastKey: '', timeSinceLastKey: 0 })
const outsideClick = ref()
const currentDropdownMenu = ref<DropdownMenuType>(DropdownMenuType.None)

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const displayAddNodeButton = computed(() => (mainMenu?.value.displayAddNodeButton ?? false))

const formattedDate = computed<string>(() => mainMenu.value?.formattedDate ?? '')
const formattedTime = computed<string>(() => mainMenu.value?.formattedTime ?? '')

useOutsideClick(outsideClick.value, () => {
  resetMenuItems()
})

const resetMenuItems = () => {
  currentDropdownMenu.value = DropdownMenuType.None
}

const onMenuShow = (val: DropdownMenuType) => {
  currentDropdownMenu.value = val
}

const onMenuHide = (val: DropdownMenuType) => {
  if (currentDropdownMenu.value === val) {
    currentDropdownMenu.value = DropdownMenuType.None
  }
}

const onAddNode = () => {
  const url = computeLink(mainMenu.value.provisionMenu?.url || '')
  window.location.assign(url)
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

        const elem: HTMLInputElement | null = document.querySelector('.onms-search-input-wrapper input.search-input')

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

onMounted(() => {
  window.addEventListener('keyup', shiftCheck)
})

onUnmounted(() => {
  window.removeEventListener('keyup', shiftCheck)
})
</script>

<style lang="scss" scoped>
@import '@/styles/onms-typography';
@import "@/styles/onms-tokens";

// Fixed top menu bar. Replaces FeatherAppBar, which was used only as a fixed
// 3-column flex bar (its skip-link, responsive hamburger, scroll-hide and
// full-width features were all inactive/hidden here).
.onms-menubar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--onms-zindex-fixed, 1030);
  display: flex;
  align-items: center;
  height: var(--onms-header-height, 3.75rem);
  padding: 0 1rem;
  background-color: var(--onms-surface-dark);
  color: var(--onms-state-text-color-on-surface-dark);
}

.onms-menubar__left {
  flex: 1 1 0;
  display: flex;
  align-items: center;
  min-width: 0;
}

.onms-menubar__center {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
}

.onms-menubar__right {
  flex: 1 1 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.logo-link.home {
  display: inline-flex;
  align-items: center;
  margin-right: 1rem;
}

// The logo is a wide wordmark SVG (viewBox 624x69). Size by height and let
// width follow; override the SVG's own max-width so it isn't clamped/distorted.
.onms-menubar__logo {
  height: 1.75rem;
  width: auto;
  max-width: none;
}

// Notification-status colors stay on their light-mode values so the indicator
// meaning doesn't change with the active theme. We re-declare the onms token
// variables locally with those fixed values and keep the consuming rules
// pointing at the token variable names.
.alarm-error,
.alarm-ok,
.alarm-unknown {
  --onms-primary-text-on-color: rgba(255, 255, 255, 1);
  color: var($primary-text-on-color) !important;
}

.alarm-error {
  --onms-error: #a5021f;
  background-color: var($error);
}

.alarm-ok {
  --onms-success: #0b720c;
  background-color: var($success);
}

.alarm-unknown {
  --onms-indeterminate: #0092c7;
  background-color: var($indeterminate);
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
}

.notice-status-display {
  font-size: 2em;
  border-radius: 1.5em;
  padding: 0.1em;
}

.date-wrapper {
  display: inline-flex;
  flex-direction: column;
  font-family: var(--onms-header-font-family);
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
// Shared header height, consumed by both the top menu bar and the side menu rail.
:root {
  --onms-header-height: 3.75rem;
}

.light-dark {
  font-size: 24px;
  margin-top: 2px;
  margin-right: 0.5rem;
  color: var(--onms-state-text-color-on-surface-dark);
  cursor: pointer;
  outline: none;

  &:hover {
    opacity: 0.8;
  }

  &:focus-visible {
    outline: 2px solid var(--onms-primary);
    outline-offset: 2px;
    border-radius: 4px;
  }
}

.center-flex {
  display: flex;
  align-items: center;
  padding-top: 3px;
}
</style>
