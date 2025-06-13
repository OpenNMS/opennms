<template>
  <div id="opennms-sidemenu-vue-container">
    <FeatherSidebar
      id="opennms-sidebar-control"
      :items="topPanels"
      pushedSelector=".app-layout"
      menuTitle="OpenNMS"
      menuHeader
      menuFooter
    />
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { DefineComponent, markRaw } from 'vue'
import { FeatherSidebar } from '@featherds/sidebar'
import { FeatherMenuList, MenuListEntry } from '@featherds/menu'
import { FeatherIcon } from '@featherds/icon'
import type { Panel } from '@featherds/panel-bar'
import IconDashboard from '@featherds/icon/action/Dashboard'
import IconFeedback from '@featherds/icon/action/Feedback'
import IconHelp from '@featherds/icon/action/Help'
import IconHome from '@featherds/icon/action/Home'
import IconInfo from '@featherds/icon/action/Info'
import IconInstances from '@featherds/icon/network/Instances'
import IconLocation from '@featherds/icon/action/Location'
import IconLogout from '@featherds/icon/action/LogOut'
import IconPerson from '@featherds/icon/action/Person'
import IconReporting from '@featherds/icon/action/Reporting'
import IconSearch from '@featherds/icon/action/Search'

import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import { MainMenu, MenuItem } from '@/types/mainMenu'
import { computePluginRelLink, createFakePlugin, createMenuItem, createTopMenuItem } from './utils'

const menuStore = useMenuStore()
const pluginStore = usePluginStore()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)

const getMenuLink = (url?: string | null) => {
  if (mainMenu.value?.baseHref && url) {
    return `${mainMenu.value.baseHref}${url}`
  }
  
  return '#'
}

const createTopMenuIcon = (menuItem: MenuItem) => {
  let icon: DefineComponent | null = null

  switch (menuItem.id) {
    case 'selfServiceMenu':
      icon = IconPerson; break
    case 'logout':
      icon = IconLogout; break
    case 'searchMenu':
      icon = IconSearch; break
    case 'info':
      icon = IconInfo; break
    case 'statusMenu':
      icon = IconFeedback; break
    case 'reportsMenu':
      icon = IconReporting; break
    case 'dashboardsMenu':
      icon = IconDashboard; break
    case 'mapsMenu':
      icon = IconLocation; break
    case 'plugins':
      icon = IconInstances; break
    case 'helpMenu':
      icon = IconHelp; break
  }

  return (icon ?? IconHome) as typeof FeatherIcon
}

const createMenuListEntry = (menuItem: MenuItem) => {
  return {
    id: menuItem.id ?? menuItem.name,
    type: 'item',
    title: menuItem.name,
    href: getMenuLink(menuItem?.url),
    onClick: menuItem.onClick
  } as MenuListEntry
}

const createPanel = (topMenuItem: MenuItem) => {
  return {
    id: topMenuItem.id ?? topMenuItem.name,
    title: topMenuItem.name,
    content: '',
    icon: createTopMenuIcon(topMenuItem),
    component: markRaw(FeatherMenuList),
    componentProps: {
      items: topMenuItem.items?.map(createMenuListEntry) ?? []
    }
  } as Panel
}

// TODO: Add this to the Menu Rest service
const createFakeSearchMenu = (searchMenu: MenuItem) => {
  return {
    ...createMenuItem('search', 'Search'),
    url: searchMenu.url
  }
}

const createPluginsMenu = (useFake: boolean) => {
  const pluginsToUse = useFake ? [createFakePlugin()] : plugins.value ?? []  

  const pluginsMenuItems = pluginsToUse.map(plugin => {
    return {
      ...createMenuItem(`plugins_${plugin.extensionId}`, plugin.menuEntry),
      url: computePluginRelLink(plugin)
    }
  })

  return createTopMenuItem('plugins', 'Plugins', pluginsMenuItems)
}

const createFlowsMenu = () => {
  const flowsMenuLink = mainMenu.value?.flowsMenu?.url ?? 'admin/classification/index.jsp'
  const flowsMenuName = mainMenu.value?.flowsMenu?.name ?? 'Flows Management'

  const flowsMenuItem = {
    ...createMenuItem('flows-management', flowsMenuName),
    url: flowsMenuLink
  }

  return createTopMenuItem('flows', 'Flows', [flowsMenuItem])
}

const createAdministrationMenu = () => {
  const configMenuLink = mainMenu.value?.configurationMenu?.url ?? 'admin/index.jsp'
  const configMenuName = mainMenu.value?.configurationMenu?.name ?? 'Configure'

  const adminMenuItem = {
    ...createMenuItem('configuration', configMenuName),
    url: configMenuLink
  }

  return createTopMenuItem('administration', 'Administration', [adminMenuItem])
}

const onLogout = async () => {
  const submitter = axios.create({
    baseURL: mainMenu.value.baseHref,
    withCredentials: true
  })

  try {
    await submitter.post('j_spring_security_logout')
  } catch (e) {
    console.error('Error attempting logout: ', e)
    return
  }

  // For the Vue SPA app, this is needed to replace the full page
  window.location.assign(mainMenu.value.baseHref)
}

const createSelfServiceMenu = () => {
  const logoutMenuItem = {
    ...createMenuItem('logout', 'Logout'),
    onClick: onLogout
  }

  const otherItems = mainMenu.value?.selfServiceMenu?.items?.filter(i => i.id !== 'logout') || []

  const items = [
    ...otherItems,
    logoutMenuItem
  ]

  const name = mainMenu.value?.selfServiceMenu?.name || 'You'
  return createTopMenuItem('self-service', name, items)
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

  allMenus.push(createAdministrationMenu())
  allMenus.push(createSelfServiceMenu())

  if (mainMenu.value.helpMenu) {
    allMenus.push(mainMenu.value.helpMenu)
  }

  // HACK for now for Search menu
  const searchMenu = allMenus.find(m => m.name === 'Search')

  if (searchMenu) {
    searchMenu.items = [createFakeSearchMenu(searchMenu)]
  }

  return allMenus.map(i => createPanel(i) as Panel)
})
</script>

<style lang="scss" scoped>
@import "@featherds/dropdown/scss/mixins";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

#opennms-sidemenu-vue-container {
  :deep(.feather-dock) {
    background-color: midnightblue;
    z-index: 1;
  }

  :deep(.feather-panel-bar-header) {
    background-color: midnightblue;
    color: white;
  }

  :deep(.feather-panel-bar-content) {
    background-color: midnightblue;
    color: white;

    a.feather-list-item {
      color: white;

      &:visited {
        color: white;
      }
    }
  }

  :deep(.feather-panel-bar-details) {
    background-color: midnightblue;
    color: white;
  }

  :deep(.feather-panel-bar-summary) {
    background-color: midnightblue;
    color: white;
  }
}
</style>
