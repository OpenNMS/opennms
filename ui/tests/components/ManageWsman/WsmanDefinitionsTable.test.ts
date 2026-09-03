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

import WsmanDefinitionsTable from '@/components/ManageWsman/WsmanDefinitionsTable.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const BASE = {
  retry: null, timeout: null, username: null, hasPassword: false, port: null, maxElements: null,
  ssl: null, strictSsl: null, path: null, productVendor: null, productVersion: null, gssAuth: null,
  ranges: [], specifics: [], ipMatches: [], requisition: null
}

const mountTable = (definitions: any[], status: any = null) => mount(WsmanDefinitionsTable, {
  props: { definitions, status },
  global: { plugins: [PrimeVue], stubs: { OnmsCard: OnmsCardStub }}
})

describe('WsmanDefinitionsTable.vue', () => {
  it('shows the empty state when there are no definitions', () => {
    const wrapper = mountTable([])
    expect(wrapper.find('[data-test="no-definitions"]').exists()).toBe(true)
  })

  it('lists ranges, specific addresses and IPLIKE matches with endpoint and credential summaries', () => {
    const wrapper = mountTable([{
      ...BASE,
      ranges: [{ begin: '10.0.0.1', end: '10.0.0.50' }],
      specifics: ['10.0.1.7'],
      ipMatches: ['192.168.*.*'],
      username: 'monitor', hasPassword: true, ssl: false, port: 5985, timeout: 5000
    }])
    const matches = wrapper.find('[data-test="definition-0-matches"]').text()
    expect(matches).toContain('10.0.0.1 – 10.0.0.50')
    expect(matches).toContain('10.0.1.7')
    expect(matches).toContain('IPLIKE 192.168.*.*')
    expect(wrapper.text()).toContain('http · port 5985')
    expect(wrapper.text()).toContain('monitor · password set')
    expect(wrapper.text()).toContain('Timeout (ms): 5000')
    expect(wrapper.find('[data-test="no-definitions"]').exists()).toBe(false)
  })

  it('shows the poller status per definition by position, and a dash without it', () => {
    const defs = [{ ...BASE, specifics: ['10.0.0.1'] }, { ...BASE, specifics: ['10.0.0.2'] }]
    const status = { serviceName: 'WS-Man', servers: 5, defaults: { servers: 0, responding: 0, down: 0, unpolled: 0, lastResponse: null },
      definitions: [{ index: 0, servers: 4, responding: 3, down: 1, unpolled: 0, lastResponse: null }, { index: 1, servers: 1, responding: 1, down: 0, unpolled: 0, lastResponse: null }] }
    const cells = mountTable(defs, status).findAll('[data-test="status-cell"]').map(c => c.text())
    expect(cells).toEqual(['3 / 4', '1 / 1'])
    expect(mountTable(defs).findAll('[data-test="status-unknown"]')).toHaveLength(2)
  })

  it('shows the linked requisition with its provisioned count and emits sync only when linked', async () => {
    const defs = [{ ...BASE, specifics: ['10.0.0.1', '10.0.0.2'], requisition: 'windows' }, { ...BASE, specifics: ['10.0.0.3'] }]
    const status = { serviceName: 'WS-Man', servers: 0, defaults: { servers: 0, responding: 0, down: 0, unpolled: 0, lastResponse: null },
      definitions: [{ index: 0, servers: 0, responding: 0, down: 0, unpolled: 0, lastResponse: null, requisition: 'windows', specificAddresses: 2, provisioned: 1 },
        { index: 1, servers: 0, responding: 0, down: 0, unpolled: 0, lastResponse: null, requisition: null, specificAddresses: 1, provisioned: 0 }] }
    const wrapper = mountTable(defs, status)
    expect(wrapper.find('[data-test="requisition-0"]').text()).toContain('windows')
    expect(wrapper.find('[data-test="requisition-0"] [data-test="provisioned"]').text()).toBe('1 of 2 addresses provisioned')
    const syncs = wrapper.findAll('[data-test="sync-definition"]')
    expect(syncs[1].attributes('disabled')).toBeDefined()
    await syncs[0].trigger('click')
    expect(wrapper.emitted('sync')?.[0]).toEqual([0])
  })

  it('emits add, edit, delete and move for the actions', async () => {
    const wrapper = mountTable([
      { ...BASE, specifics: ['10.0.0.1'] },
      { ...BASE, specifics: ['10.0.0.2'] }
    ])
    await wrapper.find('[data-test="add-definition"]').trigger('click')
    await wrapper.findAll('[data-test="edit-definition"]')[1].trigger('click')
    await wrapper.findAll('[data-test="delete-definition"]')[0].trigger('click')
    await wrapper.findAll('[data-test="move-down"]')[0].trigger('click')
    expect(wrapper.emitted('add')).toHaveLength(1)
    expect(wrapper.emitted('edit')?.[0]).toEqual([1])
    expect(wrapper.emitted('delete')?.[0]).toEqual([0])
    expect(wrapper.emitted('move')?.[0]).toEqual([0, 1])
    // the first row cannot move up, the last cannot move down
    expect(wrapper.findAll('[data-test="move-up"]')[0].attributes('disabled')).toBeDefined()
    expect(wrapper.findAll('[data-test="move-down"]')[1].attributes('disabled')).toBeDefined()
  })
})
