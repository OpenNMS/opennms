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

import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import CommercialSupport from '@/containers/CommercialSupport.vue'
import CommunityResources from '@/containers/CommunityResources.vue'
import Documentation from '@/containers/Documentation.vue'

const getAbout = vi.fn()
vi.mock('@/services', () => ({
  default: {
    getAbout: (...a: unknown[]) => getAbout(...a)
  }
}))

const adminRole = { value: true }
vi.mock('@/composables/useRole', () => ({ default: () => ({ adminRole }) }))

const mountPage = (component: object) =>
  mount(component, {
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: ['router-link', 'BreadCrumbs']
    }
  })

const links = (wrapper: ReturnType<typeof mountPage>) =>
  wrapper.findAll('[data-test="help-link"]').map(a => ({ text: a.find('.tile-label').text(), href: a.attributes('href'), target: a.attributes('target') }))

describe('Documentation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('links the guides for this product and docs version', async () => {
    getAbout.mockResolvedValue({ success: true, message: '', payload: { product: 'meridian', docsVersion: '2026' }})
    const wrapper = mountPage(Documentation)
    await flushPromises()

    const deployment = links(wrapper).find(l => l.text === 'Deployment Guide')
    expect(deployment?.href).toBe('https://docs.opennms.com/meridian/2026/deployment/core/introduction.html')
    expect(deployment?.target).toBe('_blank')
    // in-app links stay in the same tab
    expect(links(wrapper).find(l => l.text === 'OpenAPI Documentation')).toEqual({ text: 'OpenAPI Documentation', href: '#/open-api', target: undefined })
  })

  it('offers only version-neutral links when the product and version are unknown', async () => {
    getAbout.mockResolvedValue({ success: false, message: 'Unable to load the system details.' })
    const wrapper = mountPage(Documentation)
    await flushPromises()

    // never guess a product: a Meridian install must not be sent to the Horizon guides
    expect(links(wrapper).map(l => l.text)).toEqual(['OpenNMS Documentation', 'OpenAPI Documentation'])
    expect(wrapper.find('[data-test="docs-unavailable"]').exists()).toBe(true)
  })

  it('shows no release guides while the product and version are still loading', () => {
    getAbout.mockReturnValue(new Promise(() => {}))
    const wrapper = mountPage(Documentation)

    expect(links(wrapper).map(l => l.text)).toEqual(['OpenNMS Documentation', 'OpenAPI Documentation'])
    expect(wrapper.find('[data-test="docs-unavailable"]').exists()).toBe(false)
  })
})

describe('CommercialSupport', () => {
  it('offers the portal, the system report to admins, and the ways to get support', () => {
    adminRole.value = true
    const texts = links(mountPage(CommercialSupport)).map(l => l.text)
    expect(texts).toEqual(['Why OpenNMS', 'Contact Us', 'Support Portal', 'Generate a System Report'])
  })

  it('hides the system report link from non-admins', () => {
    adminRole.value = false
    const texts = links(mountPage(CommercialSupport)).map(l => l.text)
    expect(texts).not.toContain('Generate a System Report')
    adminRole.value = true
  })
})

describe('CommunityResources', () => {
  it('lists the community and project links, all opening in a new tab', () => {
    const all = links(mountPage(CommunityResources))
    expect(all.map(l => l.text)).toEqual(['Discourse', 'Web Chat', 'OpenNMS on GitHub', 'Issue Tracker', 'Continuous Integration'])
    expect(all.every(l => l.target === '_blank')).toBe(true)
  })
})
