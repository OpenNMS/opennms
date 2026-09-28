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

import PluginChecksTable from '@/components/PluginManagement/PluginChecksTable.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('PluginChecksTable.vue', () => {
  it('maps the levels to tag severities and shows the messages', () => {
    const wrapper = mount(PluginChecksTable, {
      props: { checks: [
        { id: 'structure', level: 'PASS', message: 'Well-formed archive' },
        { id: 'java', level: 'WARN', message: 'Built for Java 17' },
        { id: 'duplicate', level: 'FAIL', message: 'Already loaded' }
      ] },
      global: { plugins: [PrimeVue] }
    })
    const tags = wrapper.findAll('[data-test="check-level"]')
    expect(tags.map(t => t.attributes('data-level'))).toEqual(['PASS', 'WARN', 'FAIL'])
    expect(tags[0].classes()).toContain('p-tag-success')
    expect(tags[1].classes()).toContain('p-tag-warn')
    expect(tags[2].classes()).toContain('p-tag-danger')
    expect(wrapper.text()).toContain('Built for Java 17')
    expect(wrapper.text()).toContain('duplicate')
  })

  it('shows the empty text when no checks were reported', () => {
    const wrapper = mount(PluginChecksTable, { props: { checks: [] }, global: { plugins: [PrimeVue] }})
    expect(wrapper.find('[data-test="no-checks"]').exists()).toBe(true)
  })
})
