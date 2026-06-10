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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { defaultColumns } from '@/components/Nodes/utils'
import API from '@/services'
import { SetOperator } from '@/types'
import { categories, monitoringLocations } from '../components/Nodes/hooks/utils'

vi.mock('@/services', () => ({
  default: {
    getCategories: vi.fn(),
    getMonitoringLocations: vi.fn(),
    getServiceTypes: vi.fn()
  }
}))

describe('useNodeStructureStore', () => {
  let store: ReturnType<typeof useNodeStructureStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useNodeStructureStore()
    vi.clearAllMocks()
  })

  // ─── Initial state ──────────────────────────────────────────────────────────

  describe('initial state', () => {
    it('has empty categories and locations', () => {
      expect(store.categories).toEqual([])
      expect(store.monitoringLocations).toEqual([])
    })

    it('has categoriesLoaded and monitoringLocationsLoaded false', () => {
      expect(store.categoriesLoaded).toBe(false)
      expect(store.monitoringLocationsLoaded).toBe(false)
    })

    it('has default columns', () => {
      expect(store.columns).toEqual(defaultColumns)
    })

    it('has empty queryFilter', () => {
      expect(store.queryFilter.searchTerm).toBe('')
      expect(store.queryFilter.selectedCategories).toEqual([])
      expect(store.queryFilter.selectedFlows).toEqual([])
      expect(store.queryFilter.selectedMonitoringLocations).toEqual([])
    })

    it('has closed drawer states', () => {
      expect(store.drawerState.visible).toBe(false)
      expect(store.columnsDrawerState.visible).toBe(false)
    })
  })

  // ─── getCategories ──────────────────────────────────────────────────────────

  describe('getCategories', () => {
    it('populates categories and sets categoriesLoaded on success', async () => {
      vi.mocked(API.getCategories).mockResolvedValue({ category: categories, count: categories.length, offset: 0, totalCount: categories.length })

      await store.getCategories()

      expect(store.categories).toEqual(categories)
      expect(store.categoriesLoaded).toBe(true)
    })

    it('sets categoriesLoaded true even when list is empty', async () => {
      vi.mocked(API.getCategories).mockResolvedValue({ category: [], count: 0, offset: 0, totalCount: 0 })

      await store.getCategories()

      expect(store.categories).toEqual([])
      expect(store.categoriesLoaded).toBe(true)
    })

    it('sets categoriesLoaded true even when API call fails', async () => {
      vi.mocked(API.getCategories).mockResolvedValue(false)

      await store.getCategories()

      expect(store.categories).toEqual([])
      expect(store.categoriesLoaded).toBe(true)
    })
  })

  // ─── getMonitoringLocations ─────────────────────────────────────────────────

  describe('getMonitoringLocations', () => {
    it('populates locations and sets monitoringLocationsLoaded on success', async () => {
      vi.mocked(API.getMonitoringLocations).mockResolvedValue({ location: monitoringLocations, count: monitoringLocations.length, offset: 0, totalCount: monitoringLocations.length })

      await store.getMonitoringLocations()

      expect(store.monitoringLocations).toEqual(monitoringLocations)
      expect(store.monitoringLocationsLoaded).toBe(true)
    })

    it('sets monitoringLocationsLoaded true even when list is empty', async () => {
      vi.mocked(API.getMonitoringLocations).mockResolvedValue({ location: [], count: 0, offset: 0, totalCount: 0 })

      await store.getMonitoringLocations()

      expect(store.monitoringLocations).toEqual([])
      expect(store.monitoringLocationsLoaded).toBe(true)
    })

    it('sets monitoringLocationsLoaded true even when API call fails', async () => {
      vi.mocked(API.getMonitoringLocations).mockResolvedValue(false)

      await store.getMonitoringLocations()

      expect(store.monitoringLocations).toEqual([])
      expect(store.monitoringLocationsLoaded).toBe(true)
    })
  })

  // ─── setSearchTerm ──────────────────────────────────────────────────────────

  describe('setSearchTerm', () => {
    it('updates searchTerm', async () => {
      await store.setSearchTerm('mynode')

      expect(store.queryFilter.searchTerm).toBe('mynode')
    })

    it('preserves other filter fields', async () => {
      await store.setCategoryMode(SetOperator.Intersection)
      await store.setSearchTerm('mynode')

      expect(store.queryFilter.categoryMode).toBe(SetOperator.Intersection)
    })
  })

  // ─── setCategoryMode ────────────────────────────────────────────────────────

  describe('setCategoryMode', () => {
    it('updates categoryMode', async () => {
      await store.setCategoryMode(SetOperator.Intersection)

      expect(store.queryFilter.categoryMode).toBe(SetOperator.Intersection)
    })
  })

  // ─── isAnyFilterSelected ────────────────────────────────────────────────────

  describe('isAnyFilterSelected', () => {
    it('returns false on default state', () => {
      expect(store.isAnyFilterSelected()).toBe(false)
    })

    it('returns true when searchTerm is set', async () => {
      await store.setSearchTerm('test')

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when categories are selected', () => {
      store.queryFilter.selectedCategories = [categories[0]]

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when flows are selected', () => {
      store.queryFilter.selectedFlows = ['Ingress']

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when monitoring locations are selected', () => {
      store.queryFilter.selectedMonitoringLocations = [monitoringLocations[0]]

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when ipAddress is set', async () => {
      await store.setFilterWithIpAddress('192.168.1.1')

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when foreignSourceParams are set', async () => {
      await store.setFilterWithForeignSourceParams('foreignSource', 'MyFS')

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when snmpParams are set', async () => {
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')

      expect(store.isAnyFilterSelected()).toBe(true)
    })

    it('returns true when sysParams are set', async () => {
      await store.setFilterWithSysParams('sysName', 'router1')

      expect(store.isAnyFilterSelected()).toBe(true)
    })
  })

  // ─── clearAllFiltersAndSelections ────────────────────────────────────────────

  describe('clearAllFiltersAndSelections', () => {
    it('clears categories, flows, and locations', async () => {
      store.selectedCategories = [{ _value: 1, _text: 'Routers' }]
      store.selectedCategories2 = [{ _value: 2, _text: 'Switches' }]
      store.selectedFlows = [{ _text: 'Ingress' }]
      store.queryFilter.selectedCategories = [categories[0]]

      await store.clearAllFiltersAndSelections()

      expect(store.selectedCategories).toEqual([])
      expect(store.selectedCategories2).toEqual([])
      expect(store.selectedFlows).toEqual([])
      expect(store.queryFilter.selectedCategories).toEqual([])
    })

    it('retains searchTerm', async () => {
      await store.setSearchTerm('keep-me')
      await store.clearAllFiltersAndSelections()

      expect(store.queryFilter.searchTerm).toBe('keep-me')
    })
  })

  // ─── extended search setters ─────────────────────────────────────────────────

  describe('setFilterWithIpAddress', () => {
    it('sets ipAddress', async () => {
      await store.setFilterWithIpAddress('10.0.0.1')

      expect(store.queryFilter.ipAddress).toBe('10.0.0.1')
    })

    it('does not clear snmp params', async () => {
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')
      await store.setFilterWithIpAddress('10.0.0.1')

      expect(store.queryFilter.extendedSearch.snmpParams?.snmpIfAlias).toBe('uplink')
    })
  })

  describe('setFilterWithSnmpParams', () => {
    it('sets the given snmp field', async () => {
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')

      expect(store.queryFilter.extendedSearch.snmpParams?.snmpIfAlias).toBe('uplink')
    })

    it('does not clear ipAddress', async () => {
      await store.setFilterWithIpAddress('1.2.3.4')
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')

      expect(store.queryFilter.ipAddress).toBe('1.2.3.4')
    })

    it('does not clear other extended search groups', async () => {
      await store.setFilterWithSysParams('sysName', 'router1')
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')

      expect(store.queryFilter.extendedSearch.sysParams?.sysName).toBe('router1')
    })
  })

  describe('setFilterWithSysParams', () => {
    it('sets the given sys field', async () => {
      await store.setFilterWithSysParams('sysName', 'router1')

      expect(store.queryFilter.extendedSearch.sysParams?.sysName).toBe('router1')
    })

    it('does not clear other extended search groups', async () => {
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')
      await store.setFilterWithSysParams('sysName', 'router1')

      expect(store.queryFilter.extendedSearch.snmpParams?.snmpIfAlias).toBe('uplink')
    })
  })

  describe('setFilterWithForeignSourceParams', () => {
    it('sets the given foreign source field', async () => {
      await store.setFilterWithForeignSourceParams('foreignSource', 'MyFS')

      expect(store.queryFilter.extendedSearch.foreignSourceParams?.foreignSource).toBe('MyFS')
    })

    it('does not clear other extended search groups', async () => {
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')
      await store.setFilterWithForeignSourceParams('foreignSource', 'MyFS')

      expect(store.queryFilter.extendedSearch.snmpParams?.snmpIfAlias).toBe('uplink')
    })
  })

  describe('removeExtendedSearch', () => {
    it('clears only the specified field', async () => {
      await store.setFilterWithSnmpParams('snmpIfAlias', 'uplink')
      await store.setFilterWithSnmpParams('snmpIfName', 'eth0')
      store.removeExtendedSearch({ name: 'SNMP Alias', value: 'uplink', group: 'snmpParams', key: 'snmpIfAlias' })

      expect(store.queryFilter.extendedSearch.snmpParams?.snmpIfAlias).toBe('')
      expect(store.queryFilter.extendedSearch.snmpParams?.snmpIfName).toBe('eth0')
    })
  })

  // ─── selected item removal ───────────────────────────────────────────────────

  describe('removeCategory', () => {
    it('removes from selectedCategories and queryFilter', () => {
      store.selectedCategories = [{ _value: 1, _text: 'Routers' }, { _value: 2, _text: 'Switches' }]
      store.queryFilter.selectedCategories = [categories[0], categories[1]]

      store.removeCategory({ _value: 1, _text: 'Routers' })

      expect(store.selectedCategories).toHaveLength(1)
      expect(store.selectedCategories[0]._value).toBe(2)
      expect(store.queryFilter.selectedCategories).toHaveLength(1)
      expect(store.queryFilter.selectedCategories[0].id).toBe(2)
    })
  })

  describe('removeCategory2', () => {
    it('removes from selectedCategories2 and queryFilter', () => {
      store.selectedCategories2 = [{ _value: 1, _text: 'Routers' }, { _value: 2, _text: 'Switches' }]
      store.queryFilter.selectedCategories2 = [categories[0], categories[1]]

      store.removeCategory2({ _value: 1, _text: 'Routers' })

      expect(store.selectedCategories2).toHaveLength(1)
      expect(store.selectedCategories2[0]._value).toBe(2)
      expect(store.queryFilter.selectedCategories2).toHaveLength(1)
      expect(store.queryFilter.selectedCategories2![0].id).toBe(2)
    })
  })

  describe('removeFlow', () => {
    it('removes from selectedFlows and queryFilter', () => {
      store.selectedFlows = [{ _text: 'Ingress' }, { _text: 'Egress' }]
      store.queryFilter.selectedFlows = ['Ingress', 'Egress']

      store.removeFlow({ _text: 'Ingress' })

      expect(store.selectedFlows).toHaveLength(1)
      expect(store.selectedFlows[0]._text).toBe('Egress')
      expect(store.queryFilter.selectedFlows).toEqual(['Egress'])
    })
  })

  describe('removeMonitoringLocation', () => {
    it('removes from queryFilter by name', () => {
      store.queryFilter.selectedMonitoringLocations = [monitoringLocations[0], monitoringLocations[1]]

      store.removeMonitoringLocation({ _value: 'Default', name: monitoringLocations[0].name })

      expect(store.queryFilter.selectedMonitoringLocations).toHaveLength(1)
      expect(store.queryFilter.selectedMonitoringLocations[0].name).toBe(monitoringLocations[1].name)
    })
  })

  // ─── updateSelected* ────────────────────────────────────────────────────────

  describe('updateSelectedCategories', () => {
    it('syncs selectedCategories and queryFilter.selectedCategories', () => {
      store.updateSelectedCategories([
        { _value: 1, _text: 'Routers' },
        { _value: 2, _text: 'Switches' }
      ])

      expect(store.selectedCategories).toHaveLength(2)
      expect(store.queryFilter.selectedCategories).toHaveLength(2)
      expect(store.queryFilter.selectedCategories[0]).toMatchObject({ id: 1, name: 'Routers' })
    })
  })

  describe('updateSelectedCategories2', () => {
    it('syncs selectedCategories2 and queryFilter.selectedCategories2', () => {
      store.updateSelectedCategories2([
        { _value: 3, _text: 'Servers' },
        { _value: 4, _text: 'Production' }
      ])

      expect(store.selectedCategories2).toHaveLength(2)
      expect(store.queryFilter.selectedCategories2).toHaveLength(2)
      expect(store.queryFilter.selectedCategories2![0]).toMatchObject({ id: 3, name: 'Servers' })
    })
  })

  describe('updateSelectedFlows', () => {
    it('syncs selectedFlows and queryFilter.selectedFlows', () => {
      store.updateSelectedFlows([{ _text: 'Ingress' }, { _text: 'Egress' }])

      expect(store.selectedFlows).toHaveLength(2)
      expect(store.queryFilter.selectedFlows).toEqual(['Ingress', 'Egress'])
    })
  })

  describe('updateSelectedMonitoringLocations', () => {
    beforeEach(async () => {
      vi.mocked(API.getMonitoringLocations).mockResolvedValue({ location: monitoringLocations, count: monitoringLocations.length, offset: 0, totalCount: monitoringLocations.length })
      await store.getMonitoringLocations()
    })

    it('maps autocomplete items to full MonitoringLocation objects', async () => {
      await store.updateSelectedMonitoringLocations([
        { _value: monitoringLocations[1].name, _text: monitoringLocations[1].name }
      ])

      expect(store.queryFilter.selectedMonitoringLocations).toHaveLength(1)
      expect(store.queryFilter.selectedMonitoringLocations[0]).toEqual(monitoringLocations[1])
    })

    it('excludes items not found in the store locations list', async () => {
      await store.updateSelectedMonitoringLocations([
        { _value: 'NonExistent', _text: 'NonExistent' }
      ])

      expect(store.queryFilter.selectedMonitoringLocations).toHaveLength(0)
    })
  })

  // ─── setFromNodePreferences ──────────────────────────────────────────────────

  describe('setFromNodePreferences', () => {
    it('applies columns from preferences', async () => {
      const customColumns = [defaultColumns[0]]
      await store.setFromNodePreferences({ nodeColumns: customColumns, nodeFilter: null as any })

      expect(store.columns).toEqual(customColumns)
    })

    it('does not replace columns when preference columns are empty', async () => {
      await store.setFromNodePreferences({ nodeColumns: [], nodeFilter: null as any })

      expect(store.columns).toEqual(defaultColumns)
    })

    it('applies searchTerm from filter', async () => {
      await store.setFromNodePreferences({
        nodeColumns: [],
        nodeFilter: { searchTerm: 'hello', selectedCategories: [], selectedFlows: [], selectedMonitoringLocations: [], categoryMode: SetOperator.Union, extendedSearch: { foreignSourceParams: null as any, snmpParams: null as any, sysParams: null as any }}
      })

      expect(store.queryFilter.searchTerm).toBe('hello')
    })

    it('applies selectedCategories from filter', async () => {
      await store.setFromNodePreferences({
        nodeColumns: [],
        nodeFilter: { searchTerm: '', selectedCategories: [categories[0]], selectedFlows: [], selectedMonitoringLocations: [], categoryMode: SetOperator.Union, extendedSearch: { foreignSourceParams: null as any, snmpParams: null as any, sysParams: null as any }}
      })

      expect(store.queryFilter.selectedCategories).toEqual([categories[0]])
    })
  })

  // ─── getNodePreferences ──────────────────────────────────────────────────────

  describe('getNodePreferences', () => {
    it('returns current columns and filter', async () => {
      await store.setSearchTerm('test')
      const prefs = await store.getNodePreferences()

      expect(prefs.nodeColumns).toEqual(defaultColumns)
      expect(prefs.nodeFilter?.searchTerm).toBe('test')
    })
  })

  // ─── drawer state ────────────────────────────────────────────────────────────

  describe('drawer state', () => {
    it('openInstancesDrawerModal sets drawerState.visible true', () => {
      store.openInstancesDrawerModal()

      expect(store.drawerState.visible).toBe(true)
    })

    it('closeInstancesDrawerModal sets drawerState.visible false', () => {
      store.openInstancesDrawerModal()
      store.closeInstancesDrawerModal()

      expect(store.drawerState.visible).toBe(false)
    })

    it('openColumnsDrawerModal sets columnsDrawerState.visible true', () => {
      store.openColumnsDrawerModal()

      expect(store.columnsDrawerState.visible).toBe(true)
    })

    it('closeColumnsDrawerModal sets columnsDrawerState.visible false', () => {
      store.openColumnsDrawerModal()
      store.closeColumnsDrawerModal()

      expect(store.columnsDrawerState.visible).toBe(false)
    })
  })

  // ─── column management ───────────────────────────────────────────────────────

  describe('setNodeColumnSelection', () => {
    it('replaces columns with provided list', async () => {
      const subset = [defaultColumns[0]]
      await store.setNodeColumnSelection(subset)

      expect(store.columns).toEqual(subset)
    })
  })

  describe('updateNodeColumnSelection', () => {
    it('toggles selected state of a column by id', async () => {
      const col = { ...defaultColumns[0], selected: false }
      await store.updateNodeColumnSelection(col)

      const updated = store.columns.find(c => c.id === col.id)
      expect(updated?.selected).toBe(false)
    })
  })

  describe('resetColumnSelectionToDefault', () => {
    it('restores defaultColumns', async () => {
      await store.setNodeColumnSelection([defaultColumns[0]])
      await store.resetColumnSelectionToDefault()

      expect(store.columns).toEqual(defaultColumns)
    })
  })

  // ─── service types ───────────────────────────────────────────────────────────

  describe('service types', () => {
    it('loads service types on init', async () => {
      const store = useNodeStructureStore()
      vi.mocked(API.getServiceTypes).mockResolvedValue([{ id: 1, name: 'HTTP' }, { id: 8, name: 'HTTPS' }])
      await store.getServiceTypes()
      expect(store.allServiceTypes).toEqual([{ id: 1, name: 'HTTP' }, { id: 8, name: 'HTTPS' }])
    })

    it('updateSelectedServices updates selectedServices and queryFilter', () => {
      const store = useNodeStructureStore()
      store.updateSelectedServices([{ _value: 8, _text: 'HTTPS' }])
      expect(store.selectedServices).toEqual([{ _value: 8, _text: 'HTTPS' }])
      expect(store.queryFilter.selectedServices).toEqual(['HTTPS'])
    })

    it('removeService removes from selectedServices and queryFilter', () => {
      const store = useNodeStructureStore()
      store.updateSelectedServices([{ _value: 1, _text: 'HTTP' }, { _value: 8, _text: 'HTTPS' }])
      store.removeService({ _value: 8, _text: 'HTTPS' })
      expect(store.selectedServices).toEqual([{ _value: 1, _text: 'HTTP' }])
      expect(store.queryFilter.selectedServices).toEqual(['HTTP'])
    })

    it('clearAllFiltersAndSelections clears selectedServices', async () => {
      const store = useNodeStructureStore()
      store.updateSelectedServices([{ _value: 8, _text: 'HTTPS' }])
      await store.clearAllFiltersAndSelections()
      expect(store.selectedServices).toEqual([])
      expect(store.queryFilter.selectedServices).toEqual([])
    })

    it('setFromNodePreferences restores selectedServices after getServiceTypes', async () => {
      const store = useNodeStructureStore()
      vi.mocked(API.getServiceTypes).mockResolvedValue([
        { id: 1, name: 'HTTP' },
        { id: 8, name: 'HTTPS' }
      ])
      await store.getServiceTypes()

      await store.setFromNodePreferences({
        nodeColumns: [],
        nodeFilter: {
          searchTerm: '',
          selectedCategories: [],
          selectedFlows: [],
          selectedMonitoringLocations: [],
          categoryMode: SetOperator.Union,
          selectedServices: ['HTTPS'],
          extendedSearch: { foreignSourceParams: null as any, snmpParams: null as any, sysParams: null as any }
        }
      })

      expect(store.selectedServices).toEqual([{ _value: 8, _text: 'HTTPS' }])
      expect(store.queryFilter.selectedServices).toEqual(['HTTPS'])
    })
  })
})
