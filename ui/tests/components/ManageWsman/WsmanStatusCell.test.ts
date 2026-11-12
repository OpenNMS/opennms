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

import WsmanStatusCell from '@/components/ManageWsman/WsmanStatusCell.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountCell = (bucket: any) => mount(WsmanStatusCell, { props: { bucket }, global: { plugins: [PrimeVue] }})

describe('WsmanStatusCell.vue', () => {
  it('shows responding over servers and colors by outages', () => {
    expect(mountCell({ servers: 3, responding: 3, down: 0, unpolled: 0, lastResponse: null }).text()).toBe('3 / 3')
    const partly = mountCell({ servers: 3, responding: 1, down: 2, unpolled: 0, lastResponse: 1700000000000 })
    expect(partly.text()).toBe('1 / 3')
    expect(partly.find('[data-test="status-cell"]').attributes('title')).toContain('2 down')
    expect(partly.find('[data-test="status-unpolled"]').exists()).toBe(false)
  })

  it('calls out servers provisioned but never polled', () => {
    const wrapper = mountCell({ servers: 0, responding: 0, down: 0, unpolled: 4, lastResponse: null })
    expect(wrapper.find('[data-test="status-unpolled"]').text()).toBe('4 not polled')
    expect(wrapper.find('[data-test="status-cell"]').attributes('title')).toContain('poller-configuration.xml')
  })

  it('renders a dash, not zeros, when the status could not be read', () => {
    const wrapper = mountCell(null)
    expect(wrapper.find('[data-test="status-unknown"]').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('0')
  })
})
