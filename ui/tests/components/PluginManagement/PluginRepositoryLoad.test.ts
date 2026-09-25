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

import PluginRepositoryLoad from '@/components/PluginManagement/PluginRepositoryLoad.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

vi.mock('@/stores/pluginManagementStore')

const ALEC = { id: 'alec', name: 'ALEC', description: 'Alarm correlation', repository: 'OpenNMS-Plugins/alec', docsUrl: 'https://docs.opennms.com/alec' }
const VELOCLOUD = { id: 'velocloud', name: 'VeloCloud', description: '', repository: 'OpenNMS-Plugins/velocloud', docsUrl: null }
const CATALOG = { entries: [ALEC, VELOCLOUD], customAllowed: true }

const KAR = { name: 'opennms-alec-plugin.kar', size: 93634560, url: 'https://github.com/OpenNMS-Plugins/alec/releases/download/v3.0.4/opennms-alec-plugin.kar' }
const RELEASES = {
  repository: 'OpenNMS-Plugins/alec',
  releases: [
    { tag: 'v3.1.0-rc1', name: 'RC 1', publishedAt: '2026-09-20T10:00:00Z', prerelease: true, notes: '# RC\n<script>alert(1)</script>', assets: [KAR] },
    { tag: 'v3.0.3', name: 'v3.0.3', publishedAt: '2026-08-01T10:00:00Z', prerelease: false, notes: 'Older', assets: [KAR, { name: 'checksums.txt', size: 10, url: 'https://x' }] },
    { tag: 'v3.0.4', name: 'v3.0.4', publishedAt: '2026-09-01T10:00:00Z', prerelease: false, notes: 'Fixes **things**', assets: [KAR, { name: 'opennms-alec-plugin-legacy.kar', size: 1024, url: 'https://y' }] },
    { tag: 'v2.0.0', name: 'v2.0.0', publishedAt: '2025-01-01T10:00:00Z', prerelease: false, notes: '', assets: [] }
  ],
  fetchedAt: '2026-09-25T00:00:00Z',
  cached: true
}
const INSPECTION = {
  karName: 'alec', size: 93634560, sha256: '0123', uploadToken: 'tok', manifest: {}, features: [], bundles: [],
  checks: [{ id: 'structure', level: 'PASS', message: 'ok' }],
  source: { repository: 'OpenNMS-Plugins/alec', tag: 'v3.0.4', assetName: 'opennms-alec-plugin.kar', url: KAR.url }
}

describe('PluginRepositoryLoad.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  beforeEach(() => {
    vi.clearAllMocks()
    // the release dates go through the display time zone store
    setActivePinia(createPinia())
    store = reactive({ loadCatalog: vi.fn(), loadReleases: vi.fn(), fetchFromRepository: vi.fn(), catalog: CATALOG, releases: null })
    // the mock store cannot set itself, so the result is mirrored by hand
    store.loadReleases.mockImplementation(async () => {
      store.releases = RELEASES
      return { success: true, message: '', payload: RELEASES }
    })
    vi.mocked(usePluginManagementStore).mockReturnValue(store)
  })

  afterEach(() => wrapper?.unmount())

  const mountTab = async (disabled = false) => {
    wrapper = mount(PluginRepositoryLoad, { props: { disabled }, global: { plugins: [PrimeVue] }})
    await flushPromises()
  }

  const select = (inputId: string) => wrapper.findAllComponents({ name: 'OnmsSelect' }).find(s => s.props('inputId') === inputId)

  const pick = async (inputId: string, value: string) => {
    await select(inputId)?.vm.$emit('update:modelValue', value)
    await flushPromises()
  }

  it('reads the catalog on mount when it is not there yet, and reports a failed read', async () => {
    store.catalog = undefined
    store.loadCatalog.mockImplementation(async () => {
      store.catalog = null
    })
    await mountTab()
    expect(store.loadCatalog).toHaveBeenCalledTimes(1)
    expect(wrapper.find('[data-test="catalog-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="plugin-select"]').exists()).toBe(false)
    wrapper.unmount()
    await mountTab()
    expect(store.loadCatalog).toHaveBeenCalledTimes(1)
  })

  it('lists the catalog with the custom option, shows the description and the documentation link', async () => {
    await mountTab()
    expect(wrapper.find('[data-test="catalog-loading"]').exists()).toBe(false)
    expect(select('plugin-catalog-entry')?.props('options')).toEqual([
      { label: 'ALEC', value: 'alec' }, { label: 'VeloCloud', value: 'velocloud' }, { label: 'Other GitHub repository', value: '__custom__' }
    ])
    expect(wrapper.find('[data-test="plugin-docs-link"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="fetch-plugin"]').attributes('disabled')).toBeDefined()
    await pick('plugin-catalog-entry', 'alec')
    expect(wrapper.text()).toContain('Alarm correlation')
    const link = wrapper.find('[data-test="plugin-docs-link"]')
    expect(link.attributes('href')).toBe(ALEC.docsUrl)
    expect(link.attributes('target')).toBe('_blank')
    expect(link.attributes('rel')).toContain('noopener')
    await pick('plugin-catalog-entry', 'velocloud')
    expect(wrapper.find('[data-test="plugin-docs-link"]').exists()).toBe(false)
  })

  it('hides the custom option when the server does not allow it', async () => {
    store.catalog = { entries: [ALEC], customAllowed: false }
    await mountTab()
    expect(select('plugin-catalog-entry')?.props('options')).toEqual([{ label: 'ALEC', value: 'alec' }])
  })

  it('reads the releases for a catalog entry and preselects the newest release, pre-releases last', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    expect(store.loadReleases).toHaveBeenCalledWith({ catalogId: 'alec' })
    expect(select('plugin-release-tag')?.props('options')).toEqual([
      { label: 'v3.0.4 · 2026-09-01', value: 'v3.0.4' },
      { label: 'v3.0.3 · 2026-08-01', value: 'v3.0.3' },
      { label: 'v2.0.0 · 2025-01-01', value: 'v2.0.0' },
      { label: 'v3.1.0-rc1 · 2026-09-20 · pre-release', value: 'v3.1.0-rc1' }
    ])
    expect(select('plugin-release-tag')?.props('modelValue')).toBe('v3.0.4')
    expect(wrapper.text()).toContain('4 releases on OpenNMS-Plugins/alec (cached)')
    expect(wrapper.find('[data-test="fetch-plugin"]').attributes('disabled')).toBeUndefined()
  })

  it('offers the file select only when a release has more than one .kar asset, and refuses one without any', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    expect(select('plugin-release-asset')?.props('options')).toEqual([
      { label: 'opennms-alec-plugin.kar (89.3 MB)', value: 'opennms-alec-plugin.kar' },
      { label: 'opennms-alec-plugin-legacy.kar (1.0 KB)', value: 'opennms-alec-plugin-legacy.kar' }
    ])
    expect(select('plugin-release-asset')?.props('modelValue')).toBe('opennms-alec-plugin.kar')
    await pick('plugin-release-tag', 'v3.0.3')
    expect(select('plugin-release-asset')).toBeUndefined()
    expect(wrapper.find('[data-test="no-kar-asset"]').exists()).toBe(false)
    await pick('plugin-release-tag', 'v2.0.0')
    expect(wrapper.find('[data-test="no-kar-asset"]').text()).toContain('v2.0.0 has no .kar file')
    expect(wrapper.find('[data-test="fetch-plugin"]').attributes('disabled')).toBeDefined()
  })

  it('shows the release notes as text, never as HTML', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    expect(wrapper.find('[data-test="release-notes"]').text()).toBe('Fixes **things**')
    await pick('plugin-release-tag', 'v3.1.0-rc1')
    const notes = wrapper.find('[data-test="release-notes"]')
    expect(notes.element.tagName).toBe('PRE')
    expect(notes.text()).toContain('<script>alert(1)</script>')
    expect(notes.find('script').exists()).toBe(false)
    await pick('plugin-release-tag', 'v2.0.0')
    expect(wrapper.find('[data-test="release-notes"]').exists()).toBe(false)
  })

  it('validates a custom repository and looks its releases up on request', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', '__custom__')
    expect(store.loadReleases).not.toHaveBeenCalled()
    const input = wrapper.find('[data-test="custom-repository"]')
    expect(wrapper.find('[data-test="lookup-releases"]').attributes('disabled')).toBeDefined()
    await input.setValue('not a repo')
    expect(wrapper.find('.field-error').text()).toBe('Enter the repository as owner/repository.')
    expect(wrapper.find('[data-test="lookup-releases"]').attributes('disabled')).toBeDefined()
    await input.setValue('https://github.com/acme/plugin')
    expect(wrapper.find('.field-error').exists()).toBe(true)
    await input.setValue('acme/my.plugin-1_0')
    expect(wrapper.find('.field-error').exists()).toBe(false)
    expect(wrapper.find('[data-test="lookup-releases"]').attributes('disabled')).toBeUndefined()
    await wrapper.find('[data-test="lookup-releases"]').trigger('click')
    await flushPromises()
    expect(store.loadReleases).toHaveBeenCalledWith({ repository: 'acme/my.plugin-1_0' })
    expect(select('plugin-release-tag')?.props('modelValue')).toBe('v3.0.4')
    store.fetchFromRepository.mockResolvedValueOnce({ success: true, message: '', payload: INSPECTION })
    await wrapper.find('[data-test="fetch-plugin"]').trigger('click')
    await flushPromises()
    expect(store.fetchFromRepository).toHaveBeenCalledWith({ repository: 'acme/my.plugin-1_0', tag: 'v3.0.4', assetName: 'opennms-alec-plugin.kar' })
  })

  it('shows the server reason when the releases cannot be read, and refreshes on request', async () => {
    store.loadReleases.mockImplementationOnce(async () => {
      store.releases = null
      return { success: false, message: 'GitHub rate limit reached; try again after 14:30 or configure a token.' }
    })
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    expect(wrapper.find('[data-test="releases-error"]').text()).toBe('GitHub rate limit reached; try again after 14:30 or configure a token.')
    expect(select('plugin-release-tag')).toBeUndefined()
    await pick('plugin-catalog-entry', 'velocloud')
    expect(wrapper.find('[data-test="releases-error"]').exists()).toBe(false)
    expect(store.loadReleases).toHaveBeenLastCalledWith({ catalogId: 'velocloud' })
    await wrapper.find('[data-test="refresh-releases"]').trigger('click')
    await flushPromises()
    expect(store.loadReleases).toHaveBeenCalledTimes(3)
    expect(store.loadReleases).toHaveBeenLastCalledWith({ catalogId: 'velocloud' })
  })

  it('fetches the chosen asset, showing the download in progress, and emits the inspection with its source line', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    let resolveFetch: (value: unknown) => void = () => {}
    store.fetchFromRepository.mockReturnValueOnce(new Promise((resolve) => {
      resolveFetch = resolve
    }))
    await wrapper.find('[data-test="fetch-plugin"]').trigger('click')
    await flushPromises()
    expect(wrapper.emitted('reset')).toHaveLength(2)
    expect(wrapper.find('[data-test="fetch-progress"]').text()).toBe('Downloading opennms-alec-plugin.kar (89.3 MB)…')
    expect(wrapper.find('[data-test="fetch-plugin"]').attributes('disabled')).toBeDefined()
    resolveFetch({ success: true, message: '', payload: INSPECTION })
    await flushPromises()
    expect(wrapper.find('[data-test="fetch-progress"]').exists()).toBe(false)
    expect(store.fetchFromRepository).toHaveBeenCalledWith({ catalogId: 'alec', tag: 'v3.0.4', assetName: 'opennms-alec-plugin.kar' })
    expect(wrapper.emitted('checked')?.[0]).toEqual([INSPECTION, 'Fetched from OpenNMS-Plugins/alec v3.0.4 (opennms-alec-plugin.kar, 89.3 MB)'])
    expect(wrapper.find('[data-test="fetch-error"]').exists()).toBe(false)
  })

  it('builds the source line from the selection when the server sends none', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    store.fetchFromRepository.mockResolvedValueOnce({ success: true, message: '', payload: { ...INSPECTION, size: 1024, source: undefined }})
    await wrapper.find('[data-test="fetch-plugin"]').trigger('click')
    await flushPromises()
    expect(wrapper.emitted('checked')?.[0][1]).toBe('Fetched from OpenNMS-Plugins/alec v3.0.4 (opennms-alec-plugin.kar, 1.0 KB)')
  })

  it('shows a failed fetch inline and keeps the selection', async () => {
    await mountTab()
    await pick('plugin-catalog-entry', 'alec')
    store.fetchFromRepository.mockResolvedValueOnce({ success: false, message: 'Download refused: host example.com is not allowed.' })
    await wrapper.find('[data-test="fetch-plugin"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="fetch-error"]').text()).toBe('Download refused: host example.com is not allowed.')
    expect(wrapper.emitted('checked')).toBeUndefined()
    expect(select('plugin-release-tag')?.props('modelValue')).toBe('v3.0.4')
    await pick('plugin-release-tag', 'v3.0.3')
    expect(wrapper.find('[data-test="fetch-error"]').text()).toContain('not allowed')
    await pick('plugin-catalog-entry', 'velocloud')
    expect(wrapper.find('[data-test="fetch-error"]').exists()).toBe(false)
  })

  it('disables the controls while the container is unavailable', async () => {
    await mountTab(true)
    expect(select('plugin-catalog-entry')?.props('disabled')).toBe(true)
    const fetch = wrapper.find('[data-test="fetch-plugin"]')
    expect(fetch.attributes('disabled')).toBeDefined()
    expect(fetch.attributes('title')).toContain('not available')
  })
})
