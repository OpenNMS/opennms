<template>
  <div id="opennms-sidemenu-vue-container">
    <FeatherSidenav
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
import { DefineComponent, markRaw } from 'vue'
import { FeatherSidenav } from '@featherds/sidebar'
import { FeatherMenuList, MenuListEntry } from '@featherds/menu'
import { FeatherIcon } from '@featherds/icon'
import type { Panel } from '@featherds/panel-bar'
import IconApiConfig from '@featherds/icon/network/ApiConfig'
import IconApiEndpoints from '@featherds/icon/network/ApiEndpoints'
import IconBuild from '@featherds/icon/network/Build'
import IconColumnChart from '@featherds/icon/datavis/ColumnChart'
import IconDashboard from '@featherds/icon/action/Dashboard'
import IconHelp from '@featherds/icon/action/Help'
import IconHome from '@featherds/icon/action/Home'
import IconInstances from '@featherds/icon/network/Instances'
import IconLogout from '@featherds/icon/action/LogOut'
import IconLogsAlt from '@featherds/icon/network/LogsAlt'
import IconManageProfile from '@featherds/icon/action/ManageProfile'
import IconNetworkServer from '@featherds/icon/network/Server'
import IconNodes from '@featherds/icon/network/Nodes'
import IconPerson from '@featherds/icon/action/Person'
import IconSearch from '@featherds/icon/action/Search'
import IconContactSupport from '@featherds/icon/action/ContactSupport'
import IconView from '@featherds/icon/action/View'
import IconViewDetails from '@featherds/icon/action/ViewDetails'

import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import { MainMenu, MenuItem } from '@/types/mainMenu'
import { computePluginRelLink, createFakePlugin, createMenuItem, createTopMenuItem } from './utils'

const TOP_MENU_ID_PREFIX = 'opennms-menu-id-'

const menuStore = useMenuStore()
const pluginStore = usePluginStore()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)

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

  switch (menuItem.id) {
    case 'operationsMenu':
      icon = IconDashboard; break
    case 'inventoryMenu':
      icon = IconNetworkServer; break
    case 'remoteMonitoringMenu':
      icon = IconView; break
    case 'dashboardsMenu':
      icon = IconDashboard; break
    case 'metricsMenu':
      icon = IconColumnChart; break
    case 'discoveryMenu':
      icon = IconNodes; break
    case 'distributedMonitoringMenu':
      icon = IconViewDetails; break
    case 'toolsMenu':
      icon = IconBuild; break
    case 'integrationsMenu':
      icon = IconApiEndpoints; break
    case 'administrationMenu':
      icon = IconManageProfile; break
    case 'internalLogsMenu':
      icon = IconLogsAlt; break
    case 'apiDocumentationMenu':
      icon = IconApiConfig; break
    case 'supportMenu':
      icon = IconContactSupport; break
    case 'helpMenu':
      icon = IconHelp; break
    case 'selfServiceMenu':
      icon = IconPerson; break
    case 'logout':
      icon = IconLogout; break
    case 'searchMenu':
      icon = IconSearch; break
    case 'pluginsMenu':
      icon = IconInstances; break
  }

  return (icon ?? IconHome) as typeof FeatherIcon
}

const createMenuListEntry = (menuItem: MenuItem) => {
  return {
    id: menuItem.id ?? menuItem.name,
    type: 'item',
    title: menuItem.name,
    href: getMenuLink(menuItem),
    onClick: menuItem.onClick
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

  return createTopMenuItem('pluginsMenu', 'Plugins', pluginsMenuItems)
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

// const createAdministrationMenu = () => {
//   const configMenuLink = mainMenu.value?.configurationMenu?.url ?? 'admin/index.jsp'
//   const configMenuName = mainMenu.value?.configurationMenu?.name ?? 'Configure'

//   const adminMenuItem = {
//     ...createMenuItem('configuration', configMenuName),
//     url: configMenuLink
//   }

//   return createTopMenuItem('administration', 'Administration', [adminMenuItem])
// }

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

  // allMenus.push(createAdministrationMenu())
  // allMenus.push(createSelfServiceMenu())

  // if (mainMenu.value.helpMenu) {
  //   allMenus.push(mainMenu.value.helpMenu)
  // }

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
  // put Sidenav below the top menu and make sure popover menus are over geomap
  :deep(.feather-dock) {
    top: 3.75rem;     // --feather-header-height
    z-index: 2000;    // over the geomap
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
