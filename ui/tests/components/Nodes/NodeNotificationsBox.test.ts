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

import NodeNotificationsBox from '@/components/Nodes/NodeNotificationsBox.vue'
import { OnmsIcon, OnmsTooltip } from '@opennms/onms-ui'
import Cancel from '@opennms/onms-ui/icons/action/Cancel.vue'
import CheckCircle from '@opennms/onms-ui/icons/action/CheckCircle.vue'
import Group from '@opennms/onms-ui/icons/action/Group.vue'
import Person from '@opennms/onms-ui/icons/action/Person.vue'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import { mount, RouterLinkStub } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'
import { markRaw } from 'vue'

vi.mock('vue-router', async importOriginal => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRoute: () => ({ params: { id: '42' }})
}))

const mountBox = () =>
  mount(NodeNotificationsBox, {
    global: {
      plugins: [PrimeVue],
      directives: { 'onms-tooltip': OnmsTooltip },
      stubs: { RouterLink: RouterLinkStub }
    }
  })

// Where the tooltip directive parks its resolved text; see OnmsTooltip.test.ts.
const tooltipOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipValue

const presets = [
  ['your-outstanding-notifications-link', 'yourOutstanding', 'Your outstanding notifications for this node', Person, Cancel],
  ['your-acknowledged-notifications-link', 'yourAcknowledged', 'Your acknowledged notifications for this node', Person, CheckCircle],
  ['all-outstanding-notifications-link', 'allOutstanding', 'All outstanding notifications for this node', Group, Cancel],
  ['all-acknowledged-notifications-link', 'allAcknowledged', 'All acknowledged notifications for this node', Group, CheckCircle]
] as const

describe('NodeNotificationsBox.vue', () => {
  it('has the panels\' untinted background, without the severity strip', () => {
    const box = mountBox().find('[data-test="node-notifications-box"]')

    expect(box.classes()).toContain('node-details-banner--none')
    expect(box.classes()).toContain('node-details-banner--no-strip')
  })

  it('has a tag for each preset, in the panel\'s order, then View All Notifications', () => {
    const links = mountBox().findAllComponents(RouterLinkStub)

    expect(links.map(link => link.attributes('data-test')))
      .toEqual([...presets.map(([dataTest]) => dataTest), 'view-all-notifications-link'])
  })

  // The panel's View Notifications button, as a tag: the Notifications page, not for this node.
  describe('view-all-notifications-link', () => {
    const link = () => mountBox().findComponent<typeof RouterLinkStub>('[data-test="view-all-notifications-link"]')

    it('links to the Notifications page, as the panel\'s button does', () => {
      expect(link().props('to')).toEqual({ name: 'Notifications', query: { preset: 'yourOutstanding' }})
    })

    it('says what it is in its name and tooltip', () => {
      expect(link().attributes('aria-label')).toBe('View All Notifications')
      expect(tooltipOf(link().element)).toBe('View All Notifications')
    })

    it('shows the one View Details icon', () => {
      expect(link().findAllComponents(OnmsIcon).map(icon => icon.props('icon'))).toEqual([markRaw(ViewDetails)])
    })
  })

  describe.each(presets)('%s', (dataTest, preset, label, who, state) => {
    const link = () => mountBox().findAllComponents(RouterLinkStub).find(l => l.attributes('data-test') === dataTest)!

    it('links to the Notifications page for this node', () => {
      expect(link().props('to')).toEqual({ name: 'Notifications', query: { preset, node: '42' }})
    })

    it('says what it is in its name and tooltip', () => {
      expect(link().attributes('aria-label')).toBe(label)
      expect(tooltipOf(link().element)).toBe(label)
    })

    it('shows whose notifications and which', () => {
      expect(link().findAllComponents(OnmsIcon).map(icon => icon.props('icon'))).toEqual([markRaw(who), markRaw(state)])
    })
  })
})
