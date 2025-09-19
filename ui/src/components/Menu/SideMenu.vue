<template>
  <div id="opennms-sidemenu-vue-container">
    <FeatherSidenav
      id="opennms-sidebar-control"
      :items="topPanels"
      v-model="isExpanded"
      @update:modelValue="(val: any) => isExpanded = !!val"
      :pushedSelector="pushedSelector"
      menuTitle="OpenNMS"
      menuHeader
      menuFooter
      @update:expanded="() => menuStore.setSideMenuExpanded(true)"
      @update:collapsed="() => menuStore.setSideMenuExpanded(false)"
    />
  </div>
</template>

<script setup lang="ts">
import { DefineComponent, markRaw } from 'vue'
import { FeatherSidenav } from '@featherds/sidebar'
import { FeatherMenuList, MenuListEntry } from '@featherds/menu'
import { FeatherIcon } from '@featherds/icon'
import IconHome from '@featherds/icon/action/Home'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import { MainMenu, MenuItem } from '@/types/mainMenu'
import { computePluginRelLink, createFakePlugin, createMenuItem, createTopMenuItem } from './utils'
import useMenuIcons from './useMenuIcons'

defineProps({
  pushedSelector: {
    type: String,
    required: true
  }
})

const TOP_MENU_ID_PREFIX = 'opennms-menu-id-'

const menuStore = useMenuStore()
const pluginStore = usePluginStore()
const { getIcon } = useMenuIcons()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)
const isExpanded = ref<boolean>(menuStore.sideMenuExpanded() ?? false)

const getMenuLink = (menuItem: MenuItem) => {
  if (mainMenu.value?.baseHref && menuItem.url) {
    if (menuItem.isExternalLink && menuItem.isExternalLink === true) {
      return menuItem.url
    }

    return `${mainMenu.value.baseHref}${menuItem.url}`
  }
  
  return '#'
}

const createTopMenuIcon = (menuItem: MenuItem) => {
  const icon: (DefineComponent | null) = getIcon(menuItem.icon)

  return (icon ?? IconHome) as typeof FeatherIcon
}

const onPerformLogout = async () => {
  await performLogout()
}

const createMenuListEntry = (menuItem: MenuItem) => {
  let onClick = menuItem.onClick

  if (menuItem.action === 'logout') {
    onClick = onPerformLogout
  }

  const target = menuItem.linkTarget === '_blank' ? '_blank' : '_self'

  return {
    id: menuItem.id ?? menuItem.name,
    type: 'item',
    title: menuItem.name,
    href: getMenuLink(menuItem),
    target,
    onClick
  } as MenuListEntry
}

const createMenuListSeparator = () => {
  return {
    id: '',
    type: 'separator'
  } as MenuListEntry
}

const createMenuListHeader = (item: MenuItem) => {
  return {
    id: '',
    type: 'header',
    title: item.name
  } as MenuListEntry
}

const createTopMenuListEntry = (topMenuItem: MenuItem) => {
  if (topMenuItem.type === 'separator') {
    return createMenuListSeparator()
  }

  if (topMenuItem.type === 'header') {
    return createMenuListHeader(topMenuItem)
  }

  // 'item'
  let entry = {
    id: `${TOP_MENU_ID_PREFIX}${topMenuItem.id ?? topMenuItem.name ?? ''}`,
    type: 'item',
    title: topMenuItem.name,
    content: '',
    icon: createTopMenuIcon(topMenuItem),
    component: markRaw(FeatherMenuList),
    componentProps: {
      items: topMenuItem.items?.map(createMenuListEntry) ?? []
    }
  } as MenuListEntry

  if (topMenuItem.action && topMenuItem.action === 'link' && topMenuItem.url && topMenuItem.url.length > 0) {
    const url = getMenuLink(topMenuItem)

    entry = {
      ...entry,
      href: url,
      onClick: () => window.location.assign(url)
    } as any as MenuListEntry
  }

  return entry
}

const createPluginsMenu = (useFake: boolean) => {
  const pluginsToUse = useFake ? [createFakePlugin()] : plugins.value ?? []  

  const pluginsMenuItems = pluginsToUse.map(plugin => {
    return {
      ...createMenuItem(`plugins_${plugin.extensionId}`, plugin.menuEntry),
      url: computePluginRelLink(plugin)
    }
  })

  const topMenuItem = {
    ...createTopMenuItem('pluginsMenu', 'Plugins', pluginsMenuItems),
    icon: 'network/Connection'
  } as MenuItem

  return topMenuItem
}

const createFlowsMenu = () => {
  const flowsMenuLink = mainMenu.value?.flowsMenu?.url ?? 'admin/classification/index.jsp'
  const flowsMenuName = mainMenu.value?.flowsMenu?.name ?? 'Flows Management'

  const flowsMenuItem = {
    ...createMenuItem('flows-management', flowsMenuName),
    url: flowsMenuLink
  }

  return createTopMenuItem('flowsMenu', 'Flows', [flowsMenuItem])
}

const topPanels = computed<MenuListEntry[]>(() => {
  // If user not logged in, don't display any menus
  if (!mainMenu.value.username) {
    return []
  }

  // Normal menus
  const allMenus = [
    ...mainMenu.value.menus ?? []
  ]

  // Flows menu
  if (mainMenu.value.flowsMenu?.url?.length) {
    allMenus.push(createFlowsMenu())
  }

  // Plugins menu
  if (plugins.value && plugins.value.length > 0) {
    allMenus.push(createPluginsMenu(false))
  }

  return allMenus.map(i => createTopMenuListEntry(i) as MenuListEntry)
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
