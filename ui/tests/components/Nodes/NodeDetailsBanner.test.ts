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

import NodeDetailsBanner from '@/components/Nodes/NodeDetailsBanner.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('NodeDetailsBanner.vue', () => {
  it('renders its slot', () => {
    const wrapper = mount(NodeDetailsBanner, { slots: { default: '<strong>Hello</strong>' }})

    expect(wrapper.html()).toContain('<strong>Hello</strong>')
  })

  it('is neutral by default', () => {
    expect(mount(NodeDetailsBanner).classes()).toContain('node-details-banner--none')
  })

  it.each(['normal', 'warning', 'minor', 'major', 'critical'] as const)('tints for %s', (severity) => {
    expect(mount(NodeDetailsBanner, { props: { severity }}).classes()).toContain(`node-details-banner--${severity}`)
  })

  it('passes attributes through to its root', () => {
    const wrapper = mount(NodeDetailsBanner, { attrs: { role: 'status', 'data-test': 'x' }})

    expect(wrapper.attributes('role')).toBe('status')
    expect(wrapper.attributes('data-test')).toBe('x')
  })
})
