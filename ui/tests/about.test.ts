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
import About from '@/containers/About.vue'

const about = {
  displayVersion: '37.0.0-SNAPSHOT',
  product: 'horizon',
  docsVersion: 'latest',
  serverTime: 'Fri Oct 30 12:00:00 EDT 2026',
  javaVersion: '21.0.12 (Ubuntu)',
  javaRuntime: 'OpenJDK Runtime Environment (21.0.12)',
  javaSpecification: 'Java Platform API Specification (Oracle Corporation, 21)',
  javaVirtualMachine: 'OpenJDK 64-Bit Server VM (Ubuntu, 21.0.12)',
  javaVirtualMachineSpecification: 'Java Virtual Machine Specification (Oracle Corporation, 21)',
  operatingSystem: 'Linux 7.0.0 (amd64)',
  osgiContainer: 'Apache Karaf 4.4.8',
  servletContainer: 'jetty/9.4.58 (Servlet Spec 3.1)',
  databaseType: 'PostgreSQL',
  databaseVersion: '16.4',
  timeSeriesStrategy: 'RRD',
  ipcStrategy: 'Sink: OSGI; RPC: OSGI'
}

const getAbout = vi.fn()
vi.mock('@/services', () => ({
  default: {
    getAbout: (...a: unknown[]) => getAbout(...a)
  }
}))

const mountPage = () =>
  mount(About, {
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      stubs: ['router-link', 'BreadCrumbs']
    }
  })

const detail = (wrapper: ReturnType<typeof mountPage>, label: string) => {
  const dts = wrapper.findAll('[data-test="version-details"] dt')
  const index = dts.findIndex(dt => dt.text() === label)
  return index < 0 ? undefined : wrapper.findAll('[data-test="version-details"] dd')[index]
}

describe('About', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getAbout.mockResolvedValue({ success: true, message: '', payload: { ...about }})
  })

  it('shows the server details and links the version to its release notes', async () => {
    const wrapper = mountPage()
    await flushPromises()

    expect(detail(wrapper, 'Database Type')?.text()).toBe('PostgreSQL')
    expect(detail(wrapper, 'IPC Strategy')?.text()).toBe('Sink: OSGI; RPC: OSGI')
    expect(wrapper.find('.product').text()).toBe('OpenNMS Horizon')
    expect(wrapper.find('[data-test="version"]').text()).toBe('37.0.0-SNAPSHOT')
    expect(wrapper.find('[data-test="release-notes"]').attributes('href'))
      .toBe('https://docs.opennms.com/horizon/latest/releasenotes/whatsnew.html')
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
  })

  it('still shows the client-side details and an error when the server details fail to load', async () => {
    getAbout.mockResolvedValue({ success: false, message: 'Unable to load the system details.' })
    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Unable to load the system details.')
    expect(detail(wrapper, 'Client Time')).toBeDefined()
    expect(detail(wrapper, 'User Agent')).toBeDefined()
    expect(detail(wrapper, 'Database Type')).toBeUndefined()
    expect(wrapper.find('[data-test="release-notes"]').exists()).toBe(false)
  })
})
