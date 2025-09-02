<template>
  <FeatherDropdown
    class="self-service-menubar-dropdown"
    :modelValue="displayMenu"
    @update:modelValue="(val: any) => updateDisplay(val)"
  >
    <template v-slot:trigger="{ attrs, on }">
      <div @mouseenter="showMenu" class="self-service-menubar-icon-wrapper">
        <FeatherButton link href="#" v-bind="attrs" v-on="on" class="self-service-menubar-dropdown-button-dark">
          <FeatherIcon :icon="IconAccountCircle" class="self-service-top-icon" />
          <FeatherIcon class="self-service-arrow-dropdown" :icon="ArrowDropDown" />
        </FeatherButton>
      </div>
    </template>

    <FeatherDropdownItem
      @click="onUserProfileMenuClick"
    >
      <div class="self-service-menubar-dropdown-item-content">
        <a :href="computeLink('')" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper" name="self-service-user">
          <FeatherIcon :icon="IconAccountCircle" class="self-service-icon" />
          <span class="left-margin-small">
            {{ ellipsify(mainMenu.username || '', 40) }}
          </span>
        </a>
      </div>
    </FeatherDropdownItem>

    <FeatherDropdownItem
       v-for="item in menuItems"
       :key="item?.id || ''"
       @click="onMenuItemClick(item)"
    >
      <div class="self-service-menubar-dropdown-item-content">
        <a :href="computeLink(item?.url || '')" class="dropdown-menu-link dropdown-menu-wrapper final-menu-wrapper" :name="`self-service-${item.id}`">
          <FeatherIcon :icon="createIcon(item)" class="self-service-icon" />
          <span class="left-margin-small">
            {{ item?.name || '' }}
          </span>
        </a>
      </div>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { DefineComponent } from 'vue'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import IconAccountCircle from '@featherds/icon/action/AccountCircle'
import IconHelp from '@featherds/icon/action/Help'
import IconLogout from '@featherds/icon/action/LogOut'
import IconSecurity from '@featherds/icon/network/Security'
import { ellipsify } from '@/lib/utils'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import {
  MainMenu,
  MenuItem
} from '@/types/mainMenu'

const menuStore = useMenuStore()
const displayMenu = ref(false)
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const updateDisplay = (val: any) => {
  displayMenu.value = val === true
}

const hideMenu = () => {
  displayMenu.value = false
}

const showMenu = () => {
  displayMenu.value = true
}

defineExpose({ hideMenu, showMenu })

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

.self-service-menubar-dropdown {
  margin-left: 2px;

  :deep(.feather-dropdown) {
    @include dropdown-menu-height(10);
  }
}

.self-service-menubar-dropdown-dark {
  margin-left: 2px;
  &.help-menu {
    margin-left:1px;
  }
  :deep(.feather-dropdown) {
    @include dropdown-menu-height(10);
  }
}

.self-service-menubar-dropdown-button-dark {
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

.btn.self-service-menubar-dropdown-button-dark {
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

.feather-menu {
  &.self-service-menubar-dropdown {
    margin-left:0;
  }
  .self-service-menubar-dropdown-button-dark {
    padding: 0 7px;
  }
}
</style>
