<template>
  <div ref="triggerEl" class="self-service-menubar-icon-wrapper" @mouseenter="showMenu">
    <Button
      text
      class="self-service-menubar-dropdown-button-dark"
      aria-haspopup="true"
      aria-label="User self-service menu"
      @click="onTriggerClick"
    >
      <FeatherIcon :icon="IconAccountCircle" class="self-service-top-icon" />
      <FeatherIcon class="self-service-arrow-dropdown" :icon="ArrowDropDown" />
    </Button>

    <Popover
      ref="pop"
      appendTo="self"
      class="onms-user-dropdown-panel self-service-dropdown-panel"
      @hide="onPopoverHide"
    >
      <div class="self-service-menubar-dropdown-item-content" @click="onUserProfileMenuClick">
        <a :href="computeLink('')" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper" name="self-service-user">
          <FeatherIcon :icon="IconAccountCircle" class="self-service-icon" />
          <span class="left-margin-small">
            {{ ellipsify(mainMenu.username || '', 40) }}
          </span>
        </a>
      </div>

      <div
        v-for="item in menuItems"
        :key="item?.id || ''"
        class="self-service-menubar-dropdown-item-content"
        @click="onMenuItemClick(item)"
      >
        <a :href="item.action === 'logout' ? '#' : computeLink(item?.url || '')" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper" :name="`self-service-${item.id}`">
          <FeatherIcon :icon="createIcon(item)" class="self-service-icon" />
          <span class="left-margin-small">
            {{ item?.name || '' }}
          </span>
        </a>
      </div>
    </Popover>
  </div>
</template>

<script setup lang="ts">
import { DefineComponent, computed, ref, watch } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import IconAccountCircle from '@featherds/icon/action/AccountCircle'
import IconHelp from '@featherds/icon/action/Help'
import IconLogout from '@featherds/icon/action/LogOut'
import IconSecurity from '@featherds/icon/network/Security'
import Button from 'primevue/button'
import Popover from 'primevue/popover'
import { ellipsify } from '@/lib/utils'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import {
  MainMenu,
  MenuItem
} from '@/types/mainMenu'

const menuStore = useMenuStore()
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

const menuItems = computed<MenuItem[]>(() => {
  const helpMenu = mainMenu.value.helpMenu?.items?.find(m => m.id === 'helpMain')
  const changePasswordMenu = mainMenu.value.selfServiceMenu?.items?.find(m => m.id === 'changePassword')
  const logoutMenu = mainMenu.value.selfServiceMenu?.items?.find(m => m.id === 'logout')

  return [helpMenu, changePasswordMenu, logoutMenu].map(m => m as MenuItem).filter(m => m !== undefined) || []
})

const createIcon = (menuItem: MenuItem) => {
  let icon: DefineComponent | null = null

  switch (menuItem.id) {
    case 'helpMenu':
      icon = IconHelp; break
    case 'logout':
      icon = IconLogout; break
    case 'changePassword':
      icon = IconSecurity; break
  }

  return (icon ?? IconHelp) as typeof FeatherIcon
}

const computeLink = (url: string) => {
  const baseLink = mainMenu.value?.baseHref || import.meta.env.VITE_BASE_URL || ''
  return `${baseLink}${url}`
}

const onUserProfileMenuClick = () => {
  const link = computeLink('')
  window.location.assign(link)
}

const onMenuItemClick = async (item: MenuItem) => {
  if (item.action === 'logout') {
    await performLogout()
    return
  }

  const link = computeLink(item.url || '')
  window.location.assign(link)
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.self-service-menubar-icon-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  margin-left: 2px;
}

// Foreground uses PrimeVue tokens (not FeatherDS vars) so text tracks the same
// dark-mode selector that drives the Popover background. On embedded JSP pages
// the FeatherDS theme vars may not be toggled, which would leave dark-on-dark.
.dropdown-menu-link {
  color: var(--p-text-color) !important;

  &:hover {
    text-decoration: none;
  }
}

// Dark trigger button (replaces FeatherButton link). Matches the OG menu look.
.self-service-menubar-dropdown-button-dark {
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

div.self-service-menubar-icon-wrapper {
  .self-service-menubar-dropdown-button-dark {
    svg.self-service-top-icon.feather-icon {
      vertical-align: -0.5rem;
    }
    svg.self-service-arrow-dropdown.feather-icon {
      vertical-align: 0;
    }
  }
}

// should match menubar-dropdown-item-content in UserNotificationsMenu
.self-service-menubar-dropdown-item-content {
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
}

.feather-icon.self-service-top-icon {
  font-size: 2em;
  margin-right: 0.25rem;
}

.feather-icon.self-service-icon {
  font-size: 1.25em;
}

.self-service-menubar-dropdown-item-content.menubar-padding {
  padding: 10px;
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
</style>

<style lang="scss">
// The Popover is rendered inline (appendTo="self") so it stays inside the fixed
// header's DOM subtree, preserving the header's close-on-mouseleave behavior
// (moving the pointer into the panel does not "leave" the header). PrimeVue
// positions the panel with inline absolute coords computed from viewport +
// scroll offset, which is wrong inside a fixed header — pin it under the
// trigger instead and drop the arrow (FeatherDropdown had none).
.self-service-dropdown-panel.p-popover {
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
