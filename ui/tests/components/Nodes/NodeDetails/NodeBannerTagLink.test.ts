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

import NodeBannerTagLink from '@/components/Nodes/NodeDetails/NodeBannerTagLink.vue'
import { OnmsTag, OnmsTooltip } from '@opennms/onms-ui'
import { mount, RouterLinkStub } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountLink = (props: Record<string, unknown>, slot = '<span class="lead">3</span>') =>
  mount(NodeBannerTagLink, {
    props: { label: '3 unacknowledged alarms', ...props },
    slots: { default: slot },
    global: {
      plugins: [PrimeVue],
      directives: { 'onms-tooltip': OnmsTooltip },
      stubs: { RouterLink: RouterLinkStub }
    }
  })

// Where the tooltip directive parks its resolved text; see OnmsTooltip.test.ts.
const tooltipOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipValue

describe('NodeBannerTagLink.vue', () => {
  it('is a plain link for an href outside the app', () => {
    const wrapper = mountLink({ href: '/opennms/alarm/list.htm' })

    expect(wrapper.element.tagName).toBe('A')
    expect(wrapper.attributes('href')).toBe('/opennms/alarm/list.htm')
    expect(wrapper.findComponent(RouterLinkStub).exists()).toBe(false)
  })

  it('is a router link for a route of the app', () => {
    const to = { name: 'Notifications', query: { preset: 'all' }}
    const link = mountLink({ to }).findComponent(RouterLinkStub)

    expect(link.exists()).toBe(true)
    expect(link.props('to')).toEqual(to)
  })

  it('says what it is in its name and tooltip, since the tag has no words', () => {
    const wrapper = mountLink({ href: '#' })

    expect(wrapper.attributes('aria-label')).toBe('3 unacknowledged alarms')
    expect(tooltipOf(wrapper.element)).toBe('3 unacknowledged alarms')
  })

  it('puts the slot in the tag', () => {
    const tag = mountLink({ href: '#' }).findComponent(OnmsTag)

    expect(tag.find('.lead').text()).toBe('3')
  })

  it('uses the secondary tag unless given a severity', () => {
    expect(mountLink({ href: '#' }).findComponent(OnmsTag).props('severity')).toBe('secondary')
    expect(mountLink({ href: '#', severity: 'warn' }).findComponent(OnmsTag).props('severity')).toBe('warn')
  })
})
