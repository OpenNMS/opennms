<template>
  <div id="opennms-sidemenu-vue-container">
    <FeatherSidenav
      id="opennms-sidebar-control"
      :hoverMode="true"
      :items="topPanels"
      v-model="isExpanded"
      @update:modelValue="(val: any) => isExpanded = !!val"
      :pushedSelector="pushedSelector"
      menuTitle="OpenNMS"
      menuHeader
      menuFooter
      @update:expanded="() => menuStore.setSideMenuExpanded(true)"
      @update:collapsed="() => menuStore.setSideMenuExpanded(false)"
      @update:="() => menuStore.setSideMenuExpanded(false)"
    />
  </div>
</template>

<script setup lang="ts">
import { FeatherSidenav } from '@featherds/sidebar'
import { MenuListEntry } from '@featherds/menu'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { createTopMenuListEntry, updateWithPluginsMenuItems } from './utils'
import useMenuIcons from './useMenuIcons'

defineProps({
  pushedSelector: {
    type: String,
    required: true
  }
})

const menuStore = useMenuStore()
const pluginStore = usePluginStore()
const { getIcon } = useMenuIcons()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)
const isExpanded = ref<boolean>(menuStore.sideMenuExpanded() ?? false)

const onPerformLogout = async () => {
  await performLogout()
}

const topPanels = computed<MenuListEntry[]>(() => {
  // If user not logged in, don't display any menus
  if (!mainMenu.value.username) {
    return []
  }

  const allMenus = updateWithPluginsMenuItems(mainMenu.value.menus ?? [], plugins.value)

  return allMenus.map(i => createTopMenuListEntry(i, mainMenu.value.baseHref, getIcon, onPerformLogout) as MenuListEntry)
})
</script>

<style lang="scss" scoped>
@import "@featherds/dropdown/scss/mixins";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

#opennms-sidemenu-vue-container {
  // put Sidenav below the top menu and make sure popover menus are over geomap
  :deep(.feather-dock) {
    z-index: 2000;    // over the geomap
  }

  #opennms-sidebar-control {
    --feather-dock-header-offset: 3.75rem;

    // tighten spacing between toggle button and top of menu items
    --feather-dock-content-padding-top: 3em;
    --feather-dock-toggle-top: 2em;

    // Pin the side menu to dark regardless of active theme so that the
    // side menu and top menubar stay visually consistent with one another.
    --feather-dock-background-color: var(--feather-surface-dark);
    --feather-dock-color: var(--feather-state-text-color-on-surface-dark);
  }

  // fix Sidenav toggle button placement
  :deep(.feather-dock.dock-open > button.feather-dock-toggle) {
    top: var(--feather-header-height);
    left: calc(var(--feather-dock-width) - 3.75rem);
  }

  :deep(.feather-dock.dock-closed > button.feather-dock-toggle) {
    top: 0;
    left: 0;
  }

  :deep(li.feather-list-item.li-separator.disabled span.feather-list-item-text > hr) {
      border: 1px;
      border: 1px solid var(--feather-dock-color);
  }

  :deep(.feather-popover-container > .popover) {
    max-width: 24rem;
  }
}
</style>

<style lang="scss">
#opennms-sidebar-control {
  #opennms-sidebar-control-content {
    // tighten spacing around menu separator
    // less padding around menu separator so it doesn't just go fully across the menu
    #opennms-sidebar-control-menu {
      .feather-list-item.hover.focus.disabled.li-separator {
        height: 1.25em;
        padding: 0 0.5rem;
      }

      // tighten vertical space between expand/collapse button and first actual menu item
      // note, menu-template.json has a "dummy" first menu item of type="header" so the the
      // hard-coded "Menu" header is not emitted. This overrides the min-height of the empty li
      li:first-child.feather-list-header {
        min-height: 0.1rem;
      }
    }

    // when dock is closed, want the separator not quite fully across horizontally, but closer to fully across than when it's open
    #opennms-sidebar-control-menu.dock-closed {
      .feather-list-item.hover.focus.disabled.li-separator {
        height: 1.25em;
        padding: 0 0.1rem;
      }
    }
  }
}
</style>
