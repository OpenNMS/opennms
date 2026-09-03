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

import WsmanDataCollectionPanel from '@/components/ManageWsman/WsmanDataCollectionPanel.vue'
import { WsmanDataCollection } from '@/types/wsmanAdmin'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const DATA = {
  rrdRepository: '/opt/opennms/share/rrd/snmp/',
  sources: ['wsman-datacollection-config.xml', 'dell-idrac.xml'],
  versions: { 'wsman-datacollection-config.xml': 'a', 'dell-idrac.xml': 'b' },
  collections: [{ name: 'default', source: 'wsman-datacollection-config.xml', rrdStep: 300, rras: ['RRA:AVERAGE:0.5:1:2016'], includeAllSystemDefinitions: true, includedSystemDefinitions: [] }],
  groups: [
    { name: 'drac-power-supply', source: 'dell-idrac.xml', resourceType: 'dracPowerSupplyIndex', resourceUri: 'http://schemas.dmtf.org/wbem/wscim/1/*', dialect: null, filter: 'select InputVoltage from DCIM_PowerSupplyView',
      attributes: [{ name: 'InputVoltage', alias: 'inputVoltage', type: 'gauge', indexOf: null, filter: null }] },
    { name: 'win-cpu', source: 'microsoft-windows.xml', resourceType: 'node', resourceUri: 'http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*', dialect: null, filter: null, attributes: [] }
  ],
  systemDefinitions: [{ name: 'Dell iDRAC 8', source: 'dell-idrac.xml', rules: ['#productVendor matches \'^Dell.*\''], includedGroups: ['drac-system-board'] }]
}

const mountPanel = (data: WsmanDataCollection = DATA) => mount(WsmanDataCollectionPanel, {
  props: { dataCollection: data },
  global: { plugins: [PrimeVue], stubs: { OnmsCard: OnmsCardStub }}
})

describe('WsmanDataCollectionPanel.vue', () => {
  it('shows the repository, the source files and every table with its source column', () => {
    const wrapper = mountPanel()
    expect(wrapper.find('[data-test="rrd-repository"]').text()).toBe('/opt/opennms/share/rrd/snmp/')
    expect(wrapper.find('[data-test="sources"]').text()).toBe('wsman-datacollection-config.xml, dell-idrac.xml')
    const collections = wrapper.find('[data-test="collections-table"]').text()
    expect(collections).toContain('default')
    expect(collections).toContain('All')
    const sysDefs = wrapper.find('[data-test="system-definitions-table"]').text()
    expect(sysDefs).toContain('Dell iDRAC 8')
    expect(sysDefs).toContain('drac-system-board')
    expect(wrapper.find('[data-test="groups-table"]').text()).toContain('drac-power-supply')
  })

  it('emits edit and delete with the kind and the object, and offers no Add buttons', async () => {
    const wrapper = mountPanel()
    await wrapper.findAll('[data-test="edit-group"]')[0].trigger('click')
    await wrapper.find('[data-test="delete-system-definition"]').trigger('click')
    expect(wrapper.emitted('edit')?.[0]).toEqual(['group', DATA.groups[0]])
    expect(wrapper.emitted('delete')?.[0]).toEqual(['systemDefinition', DATA.systemDefinitions[0]])
    // creation is held back until the files move into the database
    expect(wrapper.find('[data-test="add-group"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="add-collection"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="add-system-definition"]').exists()).toBe(false)
    await wrapper.find('[data-test="reset-data-collection"]').trigger('click')
    expect(wrapper.emitted('reset')).toHaveLength(1)
  })

  it('filters groups by name, source or resource type', async () => {
    const wrapper = mountPanel()
    await wrapper.find('[data-test="group-filter"]').setValue('microsoft')
    const table = wrapper.find('[data-test="groups-table"]').text()
    expect(table).toContain('win-cpu')
    expect(table).not.toContain('drac-power-supply')
    await wrapper.find('[data-test="group-filter"]').setValue('nothing-matches')
    expect(wrapper.find('[data-test="no-groups"]').exists()).toBe(true)
  })

  it('labels the storage after the time-series strategy and marks the RRD repository unused elsewhere', () => {
    const rrd = mountPanel()
    expect(rrd.find('[data-test="timeseries-strategy"]').text()).toBe('RRD files')
    expect(rrd.find('[data-test="rrd-unused"]').exists()).toBe(false)
    expect(rrd.find('[data-test="collections-table"]').text()).toContain('RRAs')

    const newts = mountPanel({ ...DATA, timeseriesStrategy: 'newts' })
    expect(newts.find('[data-test="timeseries-strategy"]').text()).toBe('Newts')
    expect(newts.find('[data-test="rrd-unused"]').text()).toContain('not used with Newts')
    expect(newts.find('[data-test="collections-table"]').text()).toContain('RRAs (RRD only)')

    const plugin = mountPanel({ ...DATA, timeseriesStrategy: 'integration' })
    expect(plugin.find('[data-test="timeseries-strategy"]').text()).toBe('time-series integration plugin')
  })
})
