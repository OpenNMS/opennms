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
    <form ref="sidemenuLogoutForm" name="vueSidemenuLogoutForm" :action="computeLogoutFormLink()" method="post"></form>
  </div>
</template>

<script setup lang="ts">
import { computed, DefineComponent, markRaw, onMounted, ref } from 'vue'
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
import { MainMenu, MenuItem, TopMenuItem } from '@/types/mainMenu'
import { computePluginRelLink, createFakePlugin, createMenuItem, createTopMenuItem } from './utils'

const menuStore = useMenuStore()
const pluginStore = usePluginStore()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)
const sidemenuLogoutForm = ref()

const getMenuLink = (url?: string | null) => {
  if (mainMenu.value?.baseHref && url) {
    return `${mainMenu.value.baseHref}${url}`
  }
  
  return '#'
}

const createTopMenuIcon = (menuItem: TopMenuItem) => {
  let icon: DefineComponent | null = null

  if (menuItem.id) {
    if (menuItem.id === 'self-service') {
      icon = IconPerson
    } else if (menuItem.id === 'logout') {
      icon = IconLogout
    }
  }

  switch (menuItem.name) {
    case 'Search':
      icon = IconSearch; break
    case 'Info':
      icon = IconInfo; break
    case 'Status':
      icon = IconFeedback; break
    case 'Reports':
      icon = IconReporting; break
    case 'Dashboards':
      icon = IconDashboard; break
    case 'Maps':
      icon = IconLocation; break
    case 'Plugins':
      icon = IconInstances; break
    case 'Help':
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

const createPanel = (topMenuItem: TopMenuItem) => {
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
const createFakeSearchMenu = (searchMenu: TopMenuItem) => {
  return {
    ...createMenuItem('search', 'Search'),
    url: searchMenu.url
  }
}

const computeLogoutFormLink = () => {
  const logoutMenu = mainMenu.value.selfServiceMenu?.items?.find(x => x.id === 'logout')
  const baseLink = logoutMenu?.url || 'j_spring_security_logout'

  return getMenuLink(baseLink)
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

const createSelfServiceMenu = () => {
  const logoutMenuItem = {
    ...createMenuItem('logout', 'Logout'),
    onClick: () => sidemenuLogoutForm.value.submit()
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

onMounted(async () => {
  console.log('SideMenu onMounted')
})
</script>

<style lang="scss">
@import "@featherds/dropdown/scss/mixins";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

#opennms-sidemenu-vue-container {
  .feather-app-rail, .feather-app-rail-expanded {
    top: 3.5rem;
  }

  .feather-panel-bar-details {
    .feather-icon {
      color: var(--feather-surface);
    }
  }

  #opennms-sidebar-control {
    // background-color: var(--feather-surface-dark);
    background-color: midnightblue;
    // keep it above legacy content
    z-index: 1;

    /*
    :deep(.feather-dock) {
      background: var(--feather-surface-dark);
    }
    */

    /*
    #opennms-sidebar-control-panel-bar {
      :deep(.feather-panel-bar .feather-panel-bar-details .feather-panel-bar-summary) {
        color: var(--feather-state-text-color-on-surface)
      }
    }
    */
  }
}
</style>

<style scoped>
.spacer {
  margin-top: 1em;
}
</style>
