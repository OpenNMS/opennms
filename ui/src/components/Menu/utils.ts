///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { DefineComponent, markRaw } from 'vue'
import { FeatherMenuList, MenuListEntry } from '@featherds/menu'
import { FeatherIcon } from '@featherds/icon'
import IconHome from '@featherds/icon/action/Home'
import { Plugin } from '@/types'
import { MenuItem } from '@/types/mainMenu'

const TOP_MENU_ID_PREFIX = 'opennms-menu-id-'

const createMenuItem = (id: string, name: string) => {
  return {
    id,
    name,
    url: null,
    locationMatch: null,
    roles: null
  } as MenuItem
}

const createTopMenuItem = (id: string, name: string, items: MenuItem[], url?: string) => {
  return {
    id,
    name,
    items,
    url: (url && url.length > 0) ? url : null,
    locationMatch: null,
    roles: null
  } as MenuItem
}

const computePluginRelLink = (plugin: Plugin) => {
  return `ui/#/plugins/${plugin.extensionId}/${plugin.resourceRootPath}/${plugin.moduleFileName}`
}

// Create a fake ALEC plugin for demo purposes
const createFakePlugin = () => {
  // Example: https://github.com/OpenNMS/alec/blob/develop/features/ui/src/main/resources/OSGI-INF/blueprint/blueprint.xml
  // <property name="id" value="alecUiExtension"/>
  // <property name="menuEntry" value="ALEC"/>
  // <property name="resourceRoot" value="ui-ext"/>
  // <property name="moduleFileName" value="alecUiExtension.es.js"/>

  const fakePlugin = {
    extensionId: 'alecUiExtension',
    menuEntry: 'ALEC',
    moduleFileName: 'alecUiExtension.es.js',
    resourceRootPath: 'ui-ext'
  } as Plugin

  return fakePlugin
}

const getMenuLink = (menuItem: MenuItem, baseHref?: string | null) => {
  if (baseHref && menuItem.url) {
    if (menuItem.isExternalLink === true) {
      return menuItem.url
    }

    return `${baseHref}${menuItem.url}`
  }

  return '#'
}

const createMenuIcon = (menuItem: MenuItem, getIcon: (iconId?: string | null) => DefineComponent | null) => {
  const icon: (DefineComponent | null) = getIcon(menuItem.icon)

  return (icon ?? IconHome) as typeof FeatherIcon
}

const createMenuListEntry = (
  menuItem: MenuItem,
  baseHref: string | null | undefined,
  getIcon: (iconId?: string | null) => DefineComponent | null,
  onLogout: () => void
) => {
  let onClick = menuItem.onClick

  if (menuItem.action === 'logout') {
    onClick = onLogout
  }

  const target = menuItem.linkTarget === '_blank' ? '_blank' : '_self'

  let icon: typeof FeatherIcon | undefined = undefined

  if (menuItem.icon) {
    icon = createMenuIcon(menuItem, getIcon)
  }

  return {
    id: menuItem.id ?? menuItem.name,
    type: 'item',
    title: menuItem.name,
    href: getMenuLink(menuItem, baseHref),
    icon: icon,
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

const createPluginsMenu = (plugins: Plugin[], menuItem?: MenuItem) => {
  // you can test by using const pluginsToUse = [createFakePlugin()] to see how the menu looks with plugins,
  // even if you don't have any real plugins installed
  const pluginsMenuItems = plugins.map(plugin => {
    return {
      ...createMenuItem(`plugins_${plugin.extensionId}`, plugin.menuEntry),
      url: computePluginRelLink(plugin)
    }
  })

  // name and icon can be customized using the menu template
  const name = menuItem?.name ?? 'Plugins'
  const icon = menuItem?.icon ?? 'network/Connection'

  const topMenuItem = {
    ...createTopMenuItem('pluginsMenu', name, pluginsMenuItems),
    icon
  } as MenuItem

  return topMenuItem
}

const createTopMenuListEntry = (
  topMenuItem: MenuItem,
  baseHref: string | null | undefined,
  getIcon: (iconId?: string | null) => DefineComponent | null,
  onLogout: () => void
) => {
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
    icon: createMenuIcon(topMenuItem, getIcon),
    component: markRaw(FeatherMenuList),
    componentProps: {
      items: topMenuItem.items?.map(item => createMenuListEntry(item, baseHref, getIcon, onLogout)) ?? []
    }
  } as MenuListEntry

  if (topMenuItem.action && topMenuItem.action === 'link' && topMenuItem.url && topMenuItem.url.length > 0) {
    const url = getMenuLink(topMenuItem, baseHref)

    entry = {
      ...entry,
      href: url,
      onClick: () => window.location.assign(url)
    } as any as MenuListEntry
  }

  return entry
}

/**
 * Applies plugin menu logic to the given menu items:
 * - If plugins are installed and a 'plugins' menu entry exists, each such entry is replaced with a
 *   fully populated Plugins menu (preserving its position and any custom name/icon from the template).
 * - If plugins are installed but no 'plugins' menu entry exists, a "Plugins" menu is appended.
 * - If no plugins are installed, any 'plugins' menu entries are removed.
 *
 * Note, we do not guard against multiple 'plugins' entries in the menu template, but we also don't expect that to be a common scenario.
 * If it does happen, all 'plugins' entries will be treated the same way (either all removed, or all populated with plugins).
 *
 * @param menuItems The menu items to update with plugin menu items as needed
 * @param plugins The list of currently installed plugins, used to determine whether to add/remove/populate the "Plugins" menu
 * @returns The updated menu items with plugin menu items added/removed as needed
 */
const updateWithPluginsMenuItems = (menuItems: MenuItem[], plugins: Plugin[]): MenuItem[] => {
  const hasPluginsInstalled = plugins && plugins.length > 0
  const hasPluginMenuEntries = menuItems.some(menu => menu.type === 'plugins')

  // plugins installed and template has a 'plugins' placeholder: replace it with the populated menu in-place
  if (hasPluginsInstalled && hasPluginMenuEntries) {
    return menuItems.map(menu => menu.type === 'plugins' ? createPluginsMenu(plugins, menu) : menu)
  }

  // no plugins installed: remove any 'plugins' placeholder entries
  if (!hasPluginsInstalled && hasPluginMenuEntries) {
    return menuItems.filter(menu => menu.type !== 'plugins')
  }

  // plugins installed but no placeholder: auto-append a default Plugins menu at the end
  if (hasPluginsInstalled && !hasPluginMenuEntries) {
    return [...menuItems, createPluginsMenu(plugins)]
  }

  return [...menuItems]
}

export {
  computePluginRelLink,
  createFakePlugin,
  createMenuItem,
  createPluginsMenu,
  createTopMenuItem,
  createTopMenuListEntry,
  getMenuLink,
  updateWithPluginsMenuItems
}
