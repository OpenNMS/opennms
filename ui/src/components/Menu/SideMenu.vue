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
    />
  </div>
</template>

<script setup lang="ts">
import { DefineComponent, markRaw } from 'vue'
import { FeatherSidenav } from '@featherds/sidebar'
import { FeatherMenuList, MenuListEntry } from '@featherds/menu'
import { FeatherIcon } from '@featherds/icon'
import type { Panel } from '@featherds/panel-bar'
import IconHome from '@featherds/icon/action/Home'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import { MainMenu, MenuItem } from '@/types/mainMenu'
import { computePluginRelLink, createFakePlugin, createMenuItem, createTopMenuItem } from './utils'
import useMenuIcons from './useMenuIcons'

const props = defineProps({
  pushedSelector: {
    type: String,
    required: true
  }
})

const TOP_MENU_ID_PREFIX = 'opennms-menu-id-'

const menuStore = useMenuStore()
const pluginStore = usePluginStore()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)
const isExpanded = ref<boolean>(menuStore.sideMenuExpanded() ?? false)

const { getIcon } = useMenuIcons()

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
  let icon: DefineComponent | null = null

  icon = getIcon(menuItem.icon)

  return (icon ?? IconHome) as typeof FeatherIcon
}

const onPerformLogout = async () => {
  console.log('In onPerformLogout...')

  await performLogout()

  console.log('| exiting onPerformLogout...')
}

const onLockMenu = () => {
  menuStore.setSideMenuExpanded(true)
}

const onUnlockMenu = () => {
  menuStore.setSideMenuExpanded(false)
}

const createMenuListEntry = (menuItem: MenuItem) => {
  let onClick = menuItem.onClick

  if (menuItem.action === 'logout') {
    onClick = onPerformLogout
  } else if (menuItem.action === 'lockMenu') {
    onClick = onLockMenu
  } else if (menuItem.action === 'unlockMenu') {
    onClick = onUnlockMenu
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

const createPanel = (topMenuItem: MenuItem) => {
  if (topMenuItem.type === 'separator') {
    return {
      id: '',
      type: 'separator'
    } as Panel
  }

  if (topMenuItem.type === 'header') {
    return {
      id: '',
      type: 'header',
      title: topMenuItem.name
    } as Panel
  }

  // 'item'
  return {
    id: `${TOP_MENU_ID_PREFIX}${topMenuItem.id ?? topMenuItem.name ?? ''}`,
    type: 'item',
    title: topMenuItem.name,
    content: '',
    icon: createTopMenuIcon(topMenuItem),
    component: markRaw(FeatherMenuList),
    componentProps: {
      items: topMenuItem.items?.map(createMenuListEntry) ?? []
    }
  } as Panel
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

const createSeparatorMenu = () => {
  return {
    type: 'separator'
  } as MenuItem
}

const createLockMenu = (isLocked: boolean) => {
  return {
    id: 'lockMenu',
    name: isLocked ? 'Unlock Menu' : 'Lock Menu',
    action: isLocked ? 'unlockMenu' : 'lockMenu',
    url: '#',
    locationMatch: null,
    icon: isLocked ? 'action/Unlock' : 'action/Lock',
    items: [
      {
        id: 'lockItem',
        name: isLocked ? 'Unlock Menu' : 'Lock Menu',
        url: '#',
        locationMatch: null,
        action: isLocked ? 'unlockMenu' : 'lockMenu'
      }
    ]
  } as MenuItem
}

const topPanels = computed<Panel[]>(() => {
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
  } else {
    allMenus.push(createPluginsMenu(true))
  }

  // Lock/unlock menu
  allMenus.push(createSeparatorMenu(), createLockMenu(menuStore.sideMenuExpanded() ?? false))

  return allMenus.map(i => createPanel(i) as Panel)
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
