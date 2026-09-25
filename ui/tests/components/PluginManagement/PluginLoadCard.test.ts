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

import PluginLoadCard from '@/components/PluginManagement/PluginLoadCard.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

vi.mock('@/stores/pluginManagementStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2>{{ header }}</h2><slot /><slot name="footer" /></div>'
}
const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const INSPECTION = {
  karName: 'alec', size: 2048, sha256: '0123456789abcdef0123', uploadToken: 'tok',
  manifest: { 'Karaf-Feature-Start': 'true' },
  features: [{ name: 'alec', version: '3.0.0', dependencies: [] }],
  bundles: [{ symbolicName: 'org.opennms.alec', version: '3.0.0' }],
  checks: [{ id: 'structure', level: 'PASS', message: 'ok' }]
}
const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: '0123', size: 2048, uploadedBy: 'admin', uploadedAt: 1, features: ['alec'], bootFile: 'alec.boot', autoStart: true, status: 'staged', pendingRestart: false, source: 'upload' }
const INSTRUCTIONS = { packages: 'systemctl restart opennms', container: 'docker restart horizon', healthCheck: 'opennms status', note: 'Wait.' }
const CATALOG = { entries: [{ id: 'alec', name: 'ALEC', description: 'Correlation', repository: 'OpenNMS-Plugins/alec', docsUrl: null }], customAllowed: false }
const RELEASES = { repository: 'OpenNMS-Plugins/alec', releases: [{ tag: 'v3.0.4', name: 'v3.0.4', publishedAt: '2026-09-01T10:00:00Z', prerelease: false, notes: '', assets: [{ name: 'opennms-alec-plugin.kar', size: 93634560, url: 'https://github.com/x' }] }], fetchedAt: '', cached: false }

describe('PluginLoadCard.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  // PrimeVue's TabList positions its ink bar from a timer it never clears
  beforeEach(() => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] })
    vi.clearAllMocks()
    // the release dates go through the display time zone store
    setActivePinia(createPinia())
    store = reactive({
      check: vi.fn(), install: vi.fn(), loadCatalog: vi.fn(), loadReleases: vi.fn(), fetchFromRepository: vi.fn(),
      catalog: CATALOG, releases: null, restartInstructions: INSTRUCTIONS, state: { deployDir: '/opt/opennms/deploy' }
    })
    vi.mocked(usePluginManagementStore).mockReturnValue(store)
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    wrapper?.unmount()
    vi.useRealTimers()
  })

  const mountCard = (disabled = false) => {
    wrapper = mount(PluginLoadCard, {
      props: { disabled },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub, OnmsCard: OnmsCardStub }}
    })
  }

  const openFileTab = async () => {
    await wrapper.find('[data-test="load-tab-file"]').trigger('click')
    await flushPromises()
  }

  const chooseFile = async (name = 'alec.kar') => {
    const file = new File(['kar'], name)
    const input = wrapper.find('[data-test="plugin-file-input"]')
    Object.defineProperty(input.element, 'files', { value: [file], configurable: true })
    await input.trigger('change')
    return file
  }

  const runChecks = async (checks: any[]) => {
    store.check.mockResolvedValueOnce({ success: true, message: '', payload: { ...INSPECTION, checks }})
    await wrapper.find('[data-test="run-checks"]').trigger('click')
    await flushPromises()
  }

  it('opens the help in a modal from the Info button in the header', async () => {
    mountCard()
    expect(wrapper.find('[data-test="about-dialog"]').exists()).toBe(false)
    const button = wrapper.find('[data-test="about-button"]')
    expect(button.attributes('aria-label')).toBe('About Plugin management')
    await button.trigger('click')
    const dialog = wrapper.find('[data-test="about-dialog"]')
    expect(dialog.exists()).toBe(true)
    expect(dialog.find('h2').text()).toBe('About Plugin management')
    expect(dialog.find('[data-test="plugin-about"]').exists()).toBe(true)
    expect(dialog.text()).toContain('/opt/opennms/deploy')
    expect(dialog.find('[data-test="restart-packages"]').text()).toBe(INSTRUCTIONS.packages)
  })

  it('opens on the repository tab, with the file tab second', async () => {
    mountCard()
    await flushPromises()
    const tabs = wrapper.findAll('.p-tab')
    expect(tabs.map(t => t.text())).toEqual(['From a repository', 'From a file'])
    expect(wrapper.find('[data-test="load-tab-repository"]').attributes('aria-selected')).toBe('true')
    expect(wrapper.find('[data-test="load-tab-file"]').attributes('aria-selected')).toBe('false')
    expect(wrapper.find('[data-test="plugin-repository-load"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(false)
    await openFileTab()
    expect(wrapper.find('[data-test="load-tab-file"]').attributes('aria-selected')).toBe('true')
  })

  it('feeds a repository fetch into the shared result with its source line', async () => {
    mountCard()
    await flushPromises()
    // the mock store cannot set itself, so the result is mirrored by hand
    store.loadReleases.mockImplementationOnce(async () => {
      store.releases = RELEASES
      return { success: true, message: '', payload: RELEASES }
    })
    const select = wrapper.findAllComponents({ name: 'OnmsSelect' }).find(s => s.props('inputId') === 'plugin-catalog-entry')
    await select?.vm.$emit('update:modelValue', 'alec')
    await flushPromises()
    store.fetchFromRepository.mockResolvedValueOnce({ success: true, message: '', payload: { ...INSPECTION, size: 93634560, source: { repository: 'OpenNMS-Plugins/alec', tag: 'v3.0.4', assetName: 'opennms-alec-plugin.kar', url: 'https://github.com/x' }}})
    await wrapper.find('[data-test="fetch-plugin"]').trigger('click')
    await flushPromises()
    expect(store.fetchFromRepository).toHaveBeenCalledWith({ catalogId: 'alec', tag: 'v3.0.4', assetName: 'opennms-alec-plugin.kar' })
    expect(wrapper.find('[data-test="summary-source"]').text()).toBe('Fetched from OpenNMS-Plugins/alec v3.0.4 (opennms-alec-plugin.kar, 89.3 MB)')
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeUndefined()
    store.install.mockResolvedValueOnce({ success: true, message: '', payload: { plugin: PLUGIN, restartRequired: false, restartInstructions: INSTRUCTIONS }})
    await wrapper.find('[data-test="load-plugin"]').trigger('click')
    await flushPromises()
    expect(store.install).toHaveBeenCalledWith({ uploadToken: 'tok', karName: 'alec', acknowledgeWarnings: false })
    expect(wrapper.find('[data-test="plugin-loaded-dialog"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(false)
  })

  it('needs a .kar file before the checks can run', async () => {
    mountCard()
    await openFileTab()
    expect(wrapper.find('[data-test="run-checks"]').attributes('disabled')).toBeDefined()
    await chooseFile('notes.txt')
    expect(wrapper.find('[data-test="load-error"]').text()).toContain('.kar extension')
    expect(wrapper.find('[data-test="no-file"]').exists()).toBe(true)
    await chooseFile()
    expect(wrapper.find('[data-test="selected-file"]').text()).toContain('alec.kar')
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="run-checks"]').attributes('disabled')).toBeUndefined()
  })

  it('runs the checks, shows the summary with the upload as the source, and enables Load when every check passes', async () => {
    mountCard()
    await openFileTab()
    const file = await chooseFile()
    await runChecks(INSPECTION.checks)
    expect(store.check).toHaveBeenCalledWith(file)
    expect(wrapper.find('[data-test="summary-source"]').text()).toBe('Uploaded alec.kar (2.0 KB)')
    expect(wrapper.find('[data-test="summary-kar-name"]').text()).toBe('alec')
    expect(wrapper.find('[data-test="summary-size"]').text()).toBe('2.0 KB')
    expect(wrapper.find('[data-test="summary-sha256"]').text()).toBe('0123456789ab')
    expect(wrapper.find('[data-test="summary-features"]').text()).toBe('alec 3.0.0')
    expect(wrapper.find('[data-test="summary-bundles"]').text()).toBe('1')
    expect(wrapper.find('[data-test="summary-feature-start"]').text()).toBe('true')
    expect(wrapper.findAll('[data-test="check-level"]')).toHaveLength(1)
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeUndefined()
    expect(wrapper.find('[data-test="ack-warnings"]').exists()).toBe(false)
  })

  it('keeps Load disabled when a check fails', async () => {
    mountCard()
    await openFileTab()
    await chooseFile()
    await runChecks([{ id: 'duplicate', level: 'FAIL', message: 'already loaded' }, { id: 'java', level: 'WARN', message: 'old' }])
    expect(wrapper.find('[data-test="fail-note"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="ack-warnings"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeDefined()
  })

  it('requires the warnings to be acknowledged and sends the acknowledgement with the install', async () => {
    mountCard()
    await openFileTab()
    await chooseFile()
    await runChecks([{ id: 'java', level: 'WARN', message: 'Built for Java 17' }])
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="ack-warnings"] input[type="checkbox"]').setValue(true)
    expect(wrapper.find('[data-test="load-plugin"]').attributes('disabled')).toBeUndefined()

    store.install.mockResolvedValueOnce({ success: true, message: '', payload: { plugin: PLUGIN, restartRequired: false, restartInstructions: INSTRUCTIONS }})
    await wrapper.find('[data-test="load-plugin"]').trigger('click')
    await flushPromises()
    expect(store.install).toHaveBeenCalledWith({ uploadToken: 'tok', karName: 'alec', acknowledgeWarnings: true })
  })

  it('opens the result dialog on success and clears the selection', async () => {
    mountCard()
    await openFileTab()
    await chooseFile()
    await runChecks(INSPECTION.checks)
    store.install.mockResolvedValueOnce({ success: true, message: '', payload: { plugin: PLUGIN, restartRequired: false, restartInstructions: INSTRUCTIONS }})
    await wrapper.find('[data-test="load-plugin"]').trigger('click')
    await flushPromises()
    expect(store.install).toHaveBeenCalledWith({ uploadToken: 'tok', karName: 'alec', acknowledgeWarnings: false })
    const dialog = wrapper.find('[data-test="plugin-loaded-dialog"]')
    expect(dialog.exists()).toBe(true)
    expect(dialog.text()).toContain('Plugin alec loaded')
    expect(dialog.find('[data-test="written-list"]').text()).toContain('alec.boot')
    expect(dialog.find('[data-test="loaded-note-auto"]').exists()).toBe(true)
    expect(dialog.find('[data-test="restart-packages"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="no-file"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(false)
  })

  it('shows the server reason inline when the check or the install fails', async () => {
    mountCard()
    await openFileTab()
    await chooseFile()
    store.check.mockResolvedValueOnce({ success: false, message: 'Not a KAR file.' })
    await wrapper.find('[data-test="run-checks"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Not a KAR file.')
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(false)

    await runChecks(INSPECTION.checks)
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
    store.install.mockResolvedValueOnce({ success: false, message: 'Check compatibility failed.' })
    await wrapper.find('[data-test="load-plugin"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Check compatibility failed.')
    expect(wrapper.find('[data-test="plugin-loaded-dialog"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(true)
  })

  it('drops the result when a new file is chosen', async () => {
    mountCard()
    await openFileTab()
    await chooseFile()
    await runChecks(INSPECTION.checks)
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(true)
    await chooseFile('other.kar')
    expect(wrapper.find('[data-test="inspection-summary"]').exists()).toBe(false)
  })

  it('disables every action while the container is unavailable', async () => {
    mountCard(true)
    await flushPromises()
    expect(wrapper.find('[data-test="fetch-plugin"]').attributes('disabled')).toBeDefined()
    await openFileTab()
    expect(wrapper.find('[data-test="choose-file"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('[data-test="choose-file"]').attributes('title')).toContain('not available')
    await chooseFile()
    expect(wrapper.find('[data-test="run-checks"]').attributes('disabled')).toBeDefined()
  })
})
