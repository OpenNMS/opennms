<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app" :ref="outsideClick" @mouseleave="resetMenuItems">
    <template v-slot:left>
      <div class="center-flex">
        <FeatherAppBarLink :icon="Logo" title="Home" class="logo-link home" type="home" :url="mainMenu.homeUrl || '/'" />
        <Search class="search-left-margin" />
      </div>
    </template>

    <template v-slot:right>
      <template v-if="mainMenu.username">
        <!-- Provision/Quick add node menu -->
        <div class="quick-add-node-wrapper">
          <FeatherButton
            primary
            v-if="mainMenu.provisionMenu"
            @click="onAddNode"
          >Add Node</FeatherButton>
        </div>

        <UserNotificationsMenuItem :ref="userNotificationsMenu" />

        <template v-if="mainMenu.username">
          <div class="notifications-icon-wrapper">
            <FeatherTooltip
              :title="noticeStatusDisplay?.title"
              :alignment="PointerAlignment.left"
              :placement="PopoverPlacement.top"
               v-slot="{ attrs, on }">
              <FeatherIcon
                v-bind="attrs"
                v-on="on"
                :icon="noticeStatusDisplay?.iconComponent"
                :class="[noticeStatusDisplay?.colorClass, 'notice-status-display']"
              />
            </FeatherTooltip>
          </div>

          <div class="date-wrapper">
            <div class="date-formatted-date">{{ formattedDate }}</div>
            <div class="date-formatted-time">{{ formattedTime }}</div>
          </div>
        </template>
      </template>

      <!-- <FeatherIcon :icon="LightDarkMode" title="Toggle Light/Dark Mode" class="pointer light-dark"
        @click="toggleDarkLightMode(null)" /> -->
    </template>
  </FeatherAppBar>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, reactive, ref } from 'vue'
import { useOutsideClick } from '@featherds/composables/events/OutsideClick'
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import IconHide from '@featherds/icon/action/Hide'
import IconNotifications from '@featherds/icon/action/Notifications'
import { FeatherTooltip, PointerAlignment, PopoverPlacement } from '@featherds/tooltip'
import Logo from '@/assets/LogoHorizon.vue'
// import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu, NoticeStatusDisplay } from '@/types/mainMenu'
import Search from './Search.vue'
import UserNotificationsMenuItem from './UserNotificationsMenuItem.vue'
import { getFormattedDateTime, FormattedDateTime } from './utils'

// const appStore = useAppStore()
const menuStore = useMenuStore()
const lastShift = reactive({ lastKey: '', timeSinceLastKey: 0 })
// const theme = ref('')
// const light = 'open-light'
// const dark = 'open-dark'
const outsideClick = ref()
const userNotificationsMenu = ref()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const formattedDateTime = computed<FormattedDateTime>(() => getFormattedDateTime(mainMenu.value?.formattedTime ?? ''))
const formattedDate = computed(() => formattedDateTime.value.date)
const formattedTime = computed(() => `${formattedDateTime.value.time} (UTC${formattedDateTime.value.utcOp}${formattedDateTime.value.utc})`)

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

const noticeStatusDisplay = computed<NoticeStatusDisplay>(() => {
  const status = mainMenu.value?.noticeStatus

  if (status === 'On') {
    return {
      icon: 'fa-solid fa-bell',
      iconComponent: markRaw(IconNotifications),
      colorClass: 'alarm-ok',
      title: 'Notices: On'
    }
  } else if (status === 'Off') {
    return {
      icon: 'fa-solid fa-bell-slash',
      iconComponent: markRaw(IconHide),
      colorClass: 'alarm-error',
      title: 'Notices: Off'
    }
  }

  // 'Unknown'
  return {
    icon: 'fa-solid fa-bell',
    iconComponent: markRaw(IconNotifications),
    colorClass: 'alarm-unknown',
    title: 'Notices: Unknown'
  }
})

// const toggleDarkLightMode = (savedTheme: string | null) => {
//   const el = document.body
//   const newTheme = theme.value === light ? dark : light

//   if (savedTheme && (savedTheme === light || savedTheme === dark)) {
//     theme.value = savedTheme
//     el.classList.add(savedTheme)
//     return
//   }

//   // set the new theme on the body
//   el.classList.add(newTheme)

//   // remove the current theme
//   if (theme.value) {
//     el.classList.remove(theme.value)
//   }

//   // save the new theme in data and localStorage
//   theme.value = newTheme
//   localStorage.setItem('theme', theme.value)
//   appStore.setTheme(theme.value)
// }

const computeLink = (url: string, isVueLink?: boolean | null) => {
  const baseLink = (isVueLink ? import.meta.env.VITE_VUE_BASE_URL : (mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL)) || ''
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
  console.log('Got theme: ', savedTheme)

  // toggleDarkLightMode(savedTheme)
  window.addEventListener('keyup', shiftCheck)
})
</script>

<style lang="scss" scoped>
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
  margin-left: 60px;
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
  margin-left: 1em;

  .date-formatted-date {
    display: flex;
    justify-content: right;
    font-weight: 800;
    font-size: 1.25em;
  }

  .date-formatted-time {
    display: flex;
    justify-content: right;
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
  // IMPORTANT ui-components specific
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
