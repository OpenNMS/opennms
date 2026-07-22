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
import type { MenuItem as PrimeMenuItem } from 'primevue/menuitem'
import { Plugin } from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { computePluginRelLink, createPrimeMenuModel, updateWithPluginsMenuItems } from '@/components/Menu/utils'
import useMenuIcons from '@/components/Menu/useMenuIcons'
import mainMenuData from './menu-template-test.json'

const mainMenu = mainMenuData as unknown as MainMenu
const baseHref = mainMenu.baseHref  // 'http://localhost:8980/opennms/'

const { getIcon } = useMenuIcons()
const onLogout = vi.fn()

/**
 * Mirrors how SideMenu.vue builds its TieredMenu model: apply the plugin menu
 * logic to the raw menu items, then transform into the PrimeVue `MenuItem[]`
 * model. Header entries are dropped by createPrimeMenuModel, separators are kept.
 */
const buildTopPanels = (menu: MainMenu, plugins: Plugin[]): PrimeMenuItem[] => {
  if (!menu.username) {
    return []
  }
  const allMenus = updateWithPluginsMenuItems(menu.menus ?? [], plugins)
  return createPrimeMenuModel(allMenus, menu.baseHref, getIcon, onLogout)
}

const fakePlugins: Plugin[] = [
  { extensionId: 'plugin1', menuEntry: 'Plugin One', moduleFileName: 'plugin1.es.js', resourceRootPath: 'ui-ext' },
  { extensionId: 'plugin2', menuEntry: 'Plugin Two', moduleFileName: 'plugin2.es.js', resourceRootPath: 'ui-ext' },
  { extensionId: 'plugin3', menuEntry: 'Plugin Three', moduleFileName: 'plugin3.es.js', resourceRootPath: 'ui-ext' }
]

// Convenience accessor for the child items of a top-level PrimeVue MenuItem
const childItems = (entry: PrimeMenuItem) => (entry.items ?? []) as PrimeMenuItem[]

describe('Menu utils', () => {
  describe('Scenario 1: no plugins installed, no top level Plugins menu should be displayed', () => {
    it('omits the pluginsMenu entry and all other entries are fully correct', () => {
      const panels = buildTopPanels(mainMenu, [])

      // Template has 10 entries; pluginsMenu is removed when no plugins are
      // installed, and the leading header entry is dropped by the PrimeVue model.
      expect(panels).toHaveLength(8)

      // [0] inventoryMenu (the header is dropped, so inventory is first)
      const inventory = panels[0]
      expect(inventory.key).toBe('opennms-menu-id-inventoryMenu')
      expect(inventory.label).toBe('Inventory')
      expect(inventory.iconComponent).toBeDefined()

      const inventoryItems = childItems(inventory)
      expect(inventoryItems).toHaveLength(3)
      expect(inventoryItems[0]).toMatchObject({
        key: 'nodes',
        label: 'Nodes',
        url: `${baseHref}ui/index.html#/nodes`,
        target: '_self'
      })
      expect(inventoryItems[0].iconComponent).toBeUndefined()
      expect(inventoryItems[1]).toMatchObject({
        key: 'legacyNodes',
        label: 'Nodes (Legacy)',
        url: `${baseHref}element/nodeList.htm`,
        target: '_self'
      })
      expect(inventoryItems[2]).toMatchObject({
        key: 'deviceConfigs',
        label: 'Device Configs',
        url: `${baseHref}ui/index.html#/device-config-backup`,
        target: '_self'
      })

      // [1] mapsMenu — action: "link", so url is set directly on the top-level entry
      const maps = panels[1]
      expect(maps.key).toBe('opennms-menu-id-mapsMenu')
      expect(maps.label).toBe('Geographical Map')
      expect(maps.iconComponent).toBeDefined()
      expect(maps.url).toBe(`${baseHref}ui/index.html#/map`)

      // [2] separator
      const separator = panels[2]
      expect(separator.separator).toBe(true)

      // [3] administrationMenu
      const admin = panels[3]
      expect(admin.key).toBe('opennms-menu-id-administrationMenu')
      expect(admin.label).toBe('Administration')
      expect(admin.iconComponent).toBeDefined()

      const adminItems = childItems(admin)
      expect(adminItems).toHaveLength(2)
      expect(adminItems[0]).toMatchObject({
        key: 'configureOpenNms',
        label: 'Configure OpenNMS',
        url: `${baseHref}admin/index.jsp`,
        target: '_self'
      })
      expect(adminItems[0].iconComponent).toBeDefined()  // has icon: network/Configuration
      expect(adminItems[1]).toMatchObject({
        key: 'flowClassification',
        label: 'Flow Classification',
        url: `${baseHref}admin/classification/index.jsp`,
        target: '_self'
      })
      expect(adminItems[1].iconComponent).toBeUndefined()

      // [4] integrationsMenu
      const integrations = panels[4]
      expect(integrations.key).toBe('opennms-menu-id-integrationsMenu')
      expect(integrations.label).toBe('Integrations')
      expect(integrations.iconComponent).toBeDefined()

      const integrationItems = childItems(integrations)
      expect(integrationItems).toHaveLength(4)
      expect(integrationItems[0]).toMatchObject({
        key: 'snmpAgentConfiguration',
        label: 'SNMP Agent Configuration',
        url: `${baseHref}ui/index.html#/snmp-config`,
        target: '_self'
      })
      expect(integrationItems[1]).toMatchObject({
        key: 'externalRequisitions',
        label: 'External Requisitions',
        url: `${baseHref}ui/index.html#/configuration`,
        target: '_self'
      })
      expect(integrationItems[2]).toMatchObject({
        key: 'geocodingServices',
        label: 'Geocoding Services',
        url: `${baseHref}admin/geoservice/index.jsp`,
        target: '_self'
      })
      expect(integrationItems[3]).toMatchObject({
        key: 'zenithConnect',
        label: 'Connect to Zenith',
        url: `${baseHref}ui/index.html#/zenith-connect`,
        target: '_self'
      })

      // [5] toolsMenu (pluginsMenu was removed, so toolsMenu moves up)
      const tools = panels[5]
      expect(tools.key).toBe('opennms-menu-id-toolsMenu')
      expect(tools.label).toBe('Tools')
      expect(tools.iconComponent).toBeDefined()

      const toolsItems = childItems(tools)
      expect(toolsItems).toHaveLength(3)
      expect(toolsItems[0]).toMatchObject({
        key: 'snmpMibCompiler',
        label: 'SNMP MIB Compiler',
        url: `${baseHref}admin/mibCompiler.jsp`,
        target: '_self'
      })
      expect(toolsItems[1]).toMatchObject({
        key: 'fileEditor',
        label: 'File Editor',
        url: `${baseHref}ui/index.html#/file-editor`,
        target: '_self'
      })
      expect(toolsItems[2]).toMatchObject({
        key: 'scv',
        label: 'Secure Credentials Vault',
        url: `${baseHref}ui/index.html#/scv`,
        target: '_self'
      })

      // [6] internalLogsMenu
      const logs = panels[6]
      expect(logs.key).toBe('opennms-menu-id-internalLogsMenu')
      expect(logs.label).toBe('Internal Logs')
      expect(logs.iconComponent).toBeDefined()

      const logsItems = childItems(logs)
      expect(logsItems).toHaveLength(2)
      expect(logsItems[0]).toMatchObject({
        key: 'logViewer',
        label: 'Log Viewer',
        url: `${baseHref}ui/index.html#/logs`,
        target: '_self'
      })
      expect(logsItems[1]).toMatchObject({
        key: 'instrumentationLogReader',
        label: 'Instrumentation Log Reader',
        url: `${baseHref}admin/nodemanagement/instrumentationLogReader.jsp`,
        target: '_self'
      })

      // [7] supportMenu — children are external links
      const support = panels[7]
      expect(support.key).toBe('opennms-menu-id-supportMenu')
      expect(support.label).toBe('Support')
      expect(support.iconComponent).toBeDefined()

      const supportItems = childItems(support)
      expect(supportItems).toHaveLength(2)
      // External links: url is the URL as-is (no baseHref prefix), target is '_blank'
      expect(supportItems[0]).toMatchObject({
        key: 'professionalSupport',
        label: 'Professional Support',
        url: 'https://www.opennms.com/support',
        target: '_blank'
      })
      expect(supportItems[1]).toMatchObject({
        key: 'chat',
        label: 'Chat',
        url: 'https://chat.opennms.org',
        target: '_blank'
      })
    })
  })

  describe('Scenario 2: plugins are installed, template has pluginsMenu entry', () => {
    it('places the Plugins menu between integrationsMenu and toolsMenu', () => {
      const panels = buildTopPanels(mainMenu, fakePlugins)

      // Template has 10 entries; pluginsMenu is kept and populated, the header is
      // dropped by the PrimeVue model.
      expect(panels).toHaveLength(9)

      // Verify the neighbours of the Plugins entry are correct
      expect(panels[4].key).toBe('opennms-menu-id-integrationsMenu')
      expect(panels[6].key).toBe('opennms-menu-id-toolsMenu')

      // [5] Plugins menu — populated from the template's pluginsMenu entry
      const pluginsEntry = panels[5]
      expect(pluginsEntry.key).toBe('opennms-menu-id-pluginsMenu')
      expect(pluginsEntry.label).toBe('Plugins')
      expect(pluginsEntry.iconComponent).toBeDefined()

      const pluginItems = childItems(pluginsEntry)
      expect(pluginItems).toHaveLength(3)
      expect(pluginItems[0]).toMatchObject({
        key: 'plugins_plugin1',
        label: 'Plugin One',
        url: `${baseHref}${computePluginRelLink(fakePlugins[0])}`,
        target: '_self'
      })
      expect(pluginItems[1]).toMatchObject({
        key: 'plugins_plugin2',
        label: 'Plugin Two',
        url: `${baseHref}${computePluginRelLink(fakePlugins[1])}`,
        target: '_self'
      })
      expect(pluginItems[2]).toMatchObject({
        key: 'plugins_plugin3',
        label: 'Plugin Three',
        url: `${baseHref}${computePluginRelLink(fakePlugins[2])}`,
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

      // 9 template entries (pluginsMenu removed from template) + 1 auto-appended,
      // minus the dropped header = 9.
      expect(panels).toHaveLength(9)

      // The entry before plugins should be supportMenu
      expect(panels[7].key).toBe('opennms-menu-id-supportMenu')

      // [8] Plugins menu — auto-appended at the end
      const pluginsEntry = panels[8]
      expect(pluginsEntry.key).toBe('opennms-menu-id-pluginsMenu')
      expect(pluginsEntry.label).toBe('Plugins')
      expect(pluginsEntry.iconComponent).toBeDefined()

      const pluginItems = childItems(pluginsEntry)
      expect(pluginItems).toHaveLength(3)
      expect(pluginItems[0]).toMatchObject({
        key: 'plugins_plugin1',
        label: 'Plugin One',
        url: `${baseHref}${computePluginRelLink(fakePlugins[0])}`,
        target: '_self'
      })
      expect(pluginItems[1]).toMatchObject({
        key: 'plugins_plugin2',
        label: 'Plugin Two',
        url: `${baseHref}${computePluginRelLink(fakePlugins[1])}`,
        target: '_self'
      })
      expect(pluginItems[2]).toMatchObject({
        key: 'plugins_plugin3',
        label: 'Plugin Three',
        url: `${baseHref}${computePluginRelLink(fakePlugins[2])}`,
        target: '_self'
      })
    })
  })
})
