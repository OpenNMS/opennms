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

import { describe, it, expect, vi } from 'vitest'
import { MenuListEntry } from '@featherds/menu'
import { Plugin } from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { computePluginRelLink, createTopMenuListEntry, updateWithPluginsMenuItems } from '@/components/Menu/utils'
import useMenuIcons from '@/components/Menu/useMenuIcons'
import mainMenuData from './menu-template-test.json'

const mainMenu = mainMenuData as unknown as MainMenu
const baseHref = mainMenu.baseHref  // 'http://localhost:8980/opennms/'

const { getIcon } = useMenuIcons()
const onLogout = vi.fn()

/**
 * Mirrors the `topPanels` computed property in SideMenu.vue.
 * Note that the return type will actually be MenuListEntry[], but we use any[] here since Typescript throws errors
 * as it doesn't know which discriminated union variant to expect. This isn't an issue in the actual component code.
 */
const buildTopPanels = (menu: MainMenu, plugins: Plugin[]): any[] => {
  if (!menu.username) {
    return []
  }
  const allMenus = updateWithPluginsMenuItems(menu.menus ?? [], plugins)
  return allMenus.map(i => createTopMenuListEntry(i, menu.baseHref, getIcon, onLogout) as MenuListEntry)
}

const fakePlugins: Plugin[] = [
  { extensionId: 'plugin1', menuEntry: 'Plugin One', moduleFileName: 'plugin1.es.js', resourceRootPath: 'ui-ext' },
  { extensionId: 'plugin2', menuEntry: 'Plugin Two', moduleFileName: 'plugin2.es.js', resourceRootPath: 'ui-ext' },
  { extensionId: 'plugin3', menuEntry: 'Plugin Three', moduleFileName: 'plugin3.es.js', resourceRootPath: 'ui-ext' }
]

// Convenience accessor for the componentProps.items of a top-level MenuListEntry
const childItems = (entry: any) => (entry as any).componentProps.items as any[]

describe('Menu utils', () => {
  describe('Scenario 1: no plugins installed, no top level Plugins menu should be displayed', () => {
    it('omits the pluginsMenu entry and all other entries are fully correct', () => {
      const panels = buildTopPanels(mainMenu, [])

      // Template has 10 entries; pluginsMenu is removed when no plugins are installed
      expect(panels).toHaveLength(9)

      // [0] testHeader
      const header = panels[0]
      expect(header.type).toBe('header')
      expect(header.title).toBe('Test Header')

      // [1] inventoryMenu
      const inventory = panels[1]
      expect(inventory.id).toBe('opennms-menu-id-inventoryMenu')
      expect(inventory.type).toBe('item')
      expect(inventory.title).toBe('Inventory')
      expect(inventory.icon).toBeDefined()

      const inventoryItems = childItems(inventory)
      expect(inventoryItems).toHaveLength(3)
      expect(inventoryItems[0]).toMatchObject({
        id: 'nodes',
        type: 'item',
        title: 'Nodes',
        href: `${baseHref}element/nodeList.htm`,
        target: '_self'
      })
      expect(inventoryItems[0].icon).toBeUndefined()
      expect(inventoryItems[1]).toMatchObject({
        id: 'structuredNodeList',
        type: 'item',
        title: 'Structured Node List',
        href: `${baseHref}ui/index.html#/nodes`,
        target: '_self'
      })
      expect(inventoryItems[2]).toMatchObject({
        id: 'deviceConfigs',
        type: 'item',
        title: 'Device Configs',
        href: `${baseHref}ui/index.html#/device-config-backup`,
        target: '_self'
      })

      // [2] mapsMenu — action: "link", so href and onClick are set directly on the entry
      const maps = panels[2]
      expect(maps.id).toBe('opennms-menu-id-mapsMenu')
      expect(maps.type).toBe('item')
      expect(maps.title).toBe('Geographical Map')
      expect(maps.icon).toBeDefined()
      expect(maps.href).toBe(`${baseHref}ui/index.html#/map`)
      expect(typeof maps.onClick).toBe('function')

      // [3] separator
      const separator = panels[3]
      expect(separator.type).toBe('separator')
      expect(separator.id).toBe('')

      // [4] administrationMenu
      const admin = panels[4]
      expect(admin.id).toBe('opennms-menu-id-administrationMenu')
      expect(admin.type).toBe('item')
      expect(admin.title).toBe('Administration')
      expect(admin.icon).toBeDefined()

      const adminItems = childItems(admin)
      expect(adminItems).toHaveLength(2)
      expect(adminItems[0]).toMatchObject({
        id: 'configureOpenNms',
        type: 'item',
        title: 'Configure OpenNMS',
        href: `${baseHref}admin/index.jsp`,
        target: '_self'
      })
      expect(adminItems[0].icon).toBeDefined()  // has icon: network/Configuration
      expect(adminItems[1]).toMatchObject({
        id: 'flowClassification',
        type: 'item',
        title: 'Flow Classification',
        href: `${baseHref}admin/classification/index.jsp`,
        target: '_self'
      })
      expect(adminItems[1].icon).toBeUndefined()

      // [5] integrationsMenu
      const integrations = panels[5]
      expect(integrations.id).toBe('opennms-menu-id-integrationsMenu')
      expect(integrations.type).toBe('item')
      expect(integrations.title).toBe('Integrations')
      expect(integrations.icon).toBeDefined()

      const integrationItems = childItems(integrations)
      expect(integrationItems).toHaveLength(4)
      expect(integrationItems[0]).toMatchObject({
        id: 'snmpAgentConfiguration',
        type: 'item',
        title: 'SNMP Agent Configuration',
        href: `${baseHref}ui/index.html#/snmp-config`,
        target: '_self'
      })
      expect(integrationItems[1]).toMatchObject({
        id: 'externalRequisitions',
        type: 'item',
        title: 'External Requisitions',
        href: `${baseHref}ui/index.html#/configuration`,
        target: '_self'
      })
      expect(integrationItems[2]).toMatchObject({
        id: 'geocodingServices',
        type: 'item',
        title: 'Geocoding Services',
        href: `${baseHref}admin/geoservice/index.jsp`,
        target: '_self'
      })
      expect(integrationItems[3]).toMatchObject({
        id: 'zenithConnect',
        type: 'item',
        title: 'Connect to Zenith',
        href: `${baseHref}ui/index.html#/zenith-connect`,
        target: '_self'
      })

      // [6] toolsMenu (pluginsMenu was removed, so toolsMenu moves up)
      const tools = panels[6]
      expect(tools.id).toBe('opennms-menu-id-toolsMenu')
      expect(tools.type).toBe('item')
      expect(tools.title).toBe('Tools')
      expect(tools.icon).toBeDefined()

      const toolsItems = childItems(tools)
      expect(toolsItems).toHaveLength(3)
      expect(toolsItems[0]).toMatchObject({
        id: 'snmpMibCompiler',
        type: 'item',
        title: 'SNMP MIB Compiler',
        href: `${baseHref}admin/mibCompiler.jsp`,
        target: '_self'
      })
      expect(toolsItems[1]).toMatchObject({
        id: 'fileEditor',
        type: 'item',
        title: 'File Editor',
        href: `${baseHref}ui/index.html#/file-editor`,
        target: '_self'
      })
      expect(toolsItems[2]).toMatchObject({
        id: 'scv',
        type: 'item',
        title: 'Secure Credentials Vault',
        href: `${baseHref}ui/index.html#/scv`,
        target: '_self'
      })

      // [7] internalLogsMenu
      const logs = panels[7]
      expect(logs.id).toBe('opennms-menu-id-internalLogsMenu')
      expect(logs.type).toBe('item')
      expect(logs.title).toBe('Internal Logs')
      expect(logs.icon).toBeDefined()

      const logsItems = childItems(logs)
      expect(logsItems).toHaveLength(2)
      expect(logsItems[0]).toMatchObject({
        id: 'logViewer',
        type: 'item',
        title: 'Log Viewer',
        href: `${baseHref}ui/index.html#/logs`,
        target: '_self'
      })
      expect(logsItems[1]).toMatchObject({
        id: 'instrumentationLogReader',
        type: 'item',
        title: 'Instrumentation Log Reader',
        href: `${baseHref}admin/nodemanagement/instrumentationLogReader.jsp`,
        target: '_self'
      })

      // [8] supportMenu — children are external links
      const support = panels[8]
      expect(support.id).toBe('opennms-menu-id-supportMenu')
      expect(support.type).toBe('item')
      expect(support.title).toBe('Support')
      expect(support.icon).toBeDefined()

      const supportItems = childItems(support)
      expect(supportItems).toHaveLength(2)
      // External links: href is the URL as-is (no baseHref prefix), target is '_blank'
      expect(supportItems[0]).toMatchObject({
        id: 'professionalSupport',
        type: 'item',
        title: 'Professional Support',
        href: 'https://www.opennms.com/support',
        target: '_blank'
      })
      expect(supportItems[1]).toMatchObject({
        id: 'chat',
        type: 'item',
        title: 'Chat',
        href: 'https://chat.opennms.org',
        target: '_blank'
      })
    })
  })

  describe('Scenario 2: plugins are installed, template has pluginsMenu entry', () => {
    it('places the Plugins menu between integrationsMenu and toolsMenu', () => {
      const panels = buildTopPanels(mainMenu, fakePlugins)

      // Template has 10 entries; pluginsMenu entry is kept and populated with plugin items
      expect(panels).toHaveLength(10)

      // Verify the neighbours of the Plugins entry are correct
      expect(panels[5].id).toBe('opennms-menu-id-integrationsMenu')
      expect(panels[7].id).toBe('opennms-menu-id-toolsMenu')

      // [6] Plugins menu — populated from the template's pluginsMenu entry
      const pluginsEntry = panels[6]
      expect(pluginsEntry.id).toBe('opennms-menu-id-pluginsMenu')
      expect(pluginsEntry.type).toBe('item')
      expect(pluginsEntry.title).toBe('Plugins')
      expect(pluginsEntry.icon).toBeDefined()

      const pluginItems = childItems(pluginsEntry)
      expect(pluginItems).toHaveLength(3)
      expect(pluginItems[0]).toMatchObject({
        id: 'plugins_plugin1',
        type: 'item',
        title: 'Plugin One',
        href: `${baseHref}${computePluginRelLink(fakePlugins[0])}`,
        target: '_self'
      })
      expect(pluginItems[1]).toMatchObject({
        id: 'plugins_plugin2',
        type: 'item',
        title: 'Plugin Two',
        href: `${baseHref}${computePluginRelLink(fakePlugins[1])}`,
        target: '_self'
      })
      expect(pluginItems[2]).toMatchObject({
        id: 'plugins_plugin3',
        type: 'item',
        title: 'Plugin Three',
        href: `${baseHref}${computePluginRelLink(fakePlugins[2])}`,
        target: '_self'
      })
    })
  })

  describe('Scenario 3: plugins are installed, no pluginsMenu entry in template. Top level Plugins menu should be auto-appended', () => {
    const menuWithoutPluginsEntry: MainMenu = {
      ...mainMenu,
      menus: mainMenu.menus.filter(m => m.type !== 'plugins')
    }

    it('appends the Plugins menu as the last top-level entry', () => {
      const panels = buildTopPanels(menuWithoutPluginsEntry, fakePlugins)

      // 9 template entries (pluginsMenu removed from template) + 1 auto-appended = 10
      expect(panels).toHaveLength(10)

      // The entry before plugins should be supportMenu
      expect(panels[8].id).toBe('opennms-menu-id-supportMenu')

      // [9] Plugins menu — auto-appended at the end
      const pluginsEntry = panels[9]
      expect(pluginsEntry.id).toBe('opennms-menu-id-pluginsMenu')
      expect(pluginsEntry.type).toBe('item')
      expect(pluginsEntry.title).toBe('Plugins')
      expect(pluginsEntry.icon).toBeDefined()

      const pluginItems = childItems(pluginsEntry)
      expect(pluginItems).toHaveLength(3)
      expect(pluginItems[0]).toMatchObject({
        id: 'plugins_plugin1',
        type: 'item',
        title: 'Plugin One',
        href: `${baseHref}${computePluginRelLink(fakePlugins[0])}`,
        target: '_self'
      })
      expect(pluginItems[1]).toMatchObject({
        id: 'plugins_plugin2',
        type: 'item',
        title: 'Plugin Two',
        href: `${baseHref}${computePluginRelLink(fakePlugins[1])}`,
        target: '_self'
      })
      expect(pluginItems[2]).toMatchObject({
        id: 'plugins_plugin3',
        type: 'item',
        title: 'Plugin Three',
        href: `${baseHref}${computePluginRelLink(fakePlugins[2])}`,
        target: '_self'
      })
    })
  })
})
