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
import PluginManagementAbout from '@/components/PluginManagement/PluginManagementAbout.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const INSTRUCTIONS = { packages: 'sudo systemctl restart opennms', container: 'docker compose restart horizon', healthCheck: 'sudo /opt/opennms/bin/opennms status', note: 'Wait for the web interface to answer.' }

describe('PluginManagementAbout.vue', () => {
  it('renders the help sections with the restart commands and the status wording', () => {
    const wrapper = mount(PluginManagementAbout, {
      props: { restartInstructions: INSTRUCTIONS, deployDir: '/opt/opennms/deploy' },
      global: { plugins: [PrimeVue] }
    })
    expect(wrapper.findAll('.help-section')).toHaveLength(2)
    expect(wrapper.findAll('.section-title').map(t => t.text())).toEqual([
      'What a plugin is', 'Two ways to load', 'What Load does', 'When a restart is needed', 'What Unload does', 'Restarting OpenNMS', 'Audit log and access'
    ])
    const text = wrapper.text()
    expect(text.indexOf('From a repository')).toBeLessThan(text.indexOf('From a file'))
    expect(text).toContain('catalog of official plugins')
    expect(text).toContain('GitHub releases')
    expect(text).toContain('same checks')
    expect(text).toContain('only accepted from GitHub')
    expect(text).toContain('GitHub token')
    expect(text).toContain('cleaned up automatically')
    expect(text).toContain('OpenNMS Integration API')
    expect(text).toContain('featuresBoot.d')
    expect(text).toContain('Karaf-Feature-Start: false')
    expect(text).toContain('within seconds')
    expect(text).toContain('every later boot')
    expect(text).toContain('Load pending restart')
    expect(text).toContain('Unload pending restart')
    expect(text).toContain('Failed to start')
    expect(text).toContain('Loaded by hand')
    expect(text).toContain('Features to start')
    expect(text).toContain('karaf.log')
    expect(text).toContain('/opt/opennms/deploy')
    expect(text).toContain('plugin-management.log')
    expect(text).toContain('Only administrators')
    expect(wrapper.find('[data-test="restart-packages"]').text()).toBe(INSTRUCTIONS.packages)
    expect(wrapper.find('[data-test="restart-container"]').text()).toBe(INSTRUCTIONS.container)
    expect(wrapper.find('[data-test="restart-health-check"]').text()).toBe(INSTRUCTIONS.healthCheck)
    expect(wrapper.find('[data-test="restart-note"]').text()).toBe(INSTRUCTIONS.note)
  })

  it('falls back when the restart instructions could not be read', () => {
    const wrapper = mount(PluginManagementAbout, { props: { restartInstructions: null }, global: { plugins: [PrimeVue] }})
    expect(wrapper.find('[data-test="about-no-instructions"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('deploy/')
  })
})
