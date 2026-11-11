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

import GenerateEventsDialog from '@/components/MibCompiler/Dialog/GenerateEventsDialog.vue'
import { DOMWrapper, flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockGenerateEvents = vi.fn()
const mockFindExistingUeis = vi.fn()
vi.mock('@/services/mibCompilerService', () => ({
  generateEvents: (...args: unknown[]) => mockGenerateEvents(...args),
  findExistingUeis: (...args: unknown[]) => mockFindExistingUeis(...args)
}))

// happy-dom's DOMParser never reports parse errors, so the well-formedness
// check is mocked: XML containing 'malformed' is treated as broken.
vi.mock('@/components/MibCompiler/mibFilesValidator', async importOriginal => ({
  ...(await importOriginal<object>()),
  isWellFormedXml: (xml: string) => !xml.includes('malformed')
}))

const mockUploadEventConfigFiles = vi.fn()
const mockGetOrderedEventConfigSources = vi.fn()
vi.mock('@/services/eventConfigService', () => ({
  uploadEventConfigFiles: (...args: unknown[]) => mockUploadEventConfigFiles(...args),
  getOrderedEventConfigSources: (...args: unknown[]) => mockGetOrderedEventConfigSources(...args)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar })
}))

const mockRouterPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush })
}))

// OnmsDialog teleports to body; a passthrough stub keeps the content queryable.
const OnmsDialogStub = {
  name: 'OnmsDialog',
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>',
  props: ['visible', 'header', 'width']
}

const enabled = (...names: string[]) => names.map(name => ({ name, enabled: true }))

// the [UEI, sources] cell texts of the duplicate-UEI table rows in a container
const rowsOf = (container: DOMWrapper<Element>) =>
  container.findAll('[data-test="duplicate-uei-row"]')
    .map(row => row.findAll('td').map(cell => cell.text().replace(/\s+/g, ' ').trim()))

const eventsXml = '<events><event><uei>uei.opennms.org/traps/IF-MIB/linkDown</uei></event></events>'
const preview = {
  success: true,
  mibName: 'IF-MIB',
  ueiBase: 'uei.opennms.org/traps/IF-MIB',
  eventCount: 2,
  suggestedFileName: 'IF-MIB.events.xml',
  eventsXml
}

describe('GenerateEventsDialog', () => {
  let wrapper: VueWrapper

  const createWrapper = async () => {
    const mounted = mount(GenerateEventsDialog, {
      props: {
        visible: false,
        fileName: 'IF-MIB.mib'
      },
      global: {
        plugins: [PrimeVue],
        stubs: { OnmsDialog: OnmsDialogStub }
      }
    })
    // the dialog resets its state on the false -> true transition
    await mounted.setProps({ visible: true })
    await flushPromises()
    return mounted
  }

  beforeEach(() => {
    vi.clearAllMocks()
    // no existing source by default: saving needs no confirmation
    mockGetOrderedEventConfigSources.mockResolvedValue([])
    // by default the save-time check agrees with the preview
    mockFindExistingUeis.mockImplementation(async () => (await mockGenerateEvents.mock.results.at(-1)?.value)?.existingUeis ?? {})
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('prefills the UEI base from the file name', async () => {
    wrapper = await createWrapper()
    const input = wrapper.find('[data-test="uei-base-input"]').element as HTMLInputElement
    expect(input.value).toBe('uei.opennms.org/traps/IF-MIB')
  })

  it('generates a preview and shows the events XML', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()

    expect(mockGenerateEvents).toHaveBeenCalledWith('IF-MIB.mib', 'uei.opennms.org/traps/IF-MIB')
    expect(wrapper.find('[data-test="event-count"]').text()).toContain('2')
    const textarea = wrapper.find('[data-test="events-xml"]').element as HTMLTextAreaElement
    expect(textarea.value).toBe(eventsXml)
  })

  it('shows no duplicate warning when no other source defines the UEIs', async () => {
    mockGenerateEvents.mockResolvedValue({ ...preview, existingUeis: {}})
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="duplicate-ueis"]').exists()).toBe(false)
  })

  it('warns about UEIs that other event sources already define', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('acme.events', 'vendor.events') }
    })
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()

    const warning = wrapper.find('[data-test="duplicate-ueis"]')
    expect(warning.exists()).toBe(true)
    expect(warning.text()).toContain('1 UEI is already defined')
    expect(rowsOf(warning)).toEqual([['uei.opennms.org/traps/IF-MIB/linkDown', 'acme.events vendor.events']])
    expect(warning.find('[data-test="disabled-tag"]').exists()).toBe(false)
    expect(warning.find('[data-test="disabled-duplicates-hint"]').exists()).toBe(false)
  })

  it('asks for confirmation before a new source overrides the definitions of other sources', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('acme.events', 'vendor.events') }
    })
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'acme.events' }, { name: 'vendor.events' }])
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    // nothing is uploaded until the user confirms
    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="confirm-replace"]').exists()).toBe(false)
    const overridden = wrapper.find('[data-test="confirm-overridden-ueis"]')
    expect(overridden.text()).toContain('evaluated before all existing sources')
    expect(rowsOf(overridden)).toEqual([['uei.opennms.org/traps/IF-MIB/linkDown', 'acme.events vendor.events']])
    expect(overridden.text()).toContain('do not contain alarm data')
    const confirmButton = wrapper.find('[data-test="confirm-save-button"]')
    expect(confirmButton.text()).toBe('Save Anyway')

    await confirmButton.trigger('click')
    await flushPromises()
    expect(mockUploadEventConfigFiles).toHaveBeenCalledTimes(1)
    expect(wrapper.find('[data-test="events-saved"]').exists()).toBe(true)
  })

  it('returns to the preview from the duplicate confirmation', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('acme.events') }
    })
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'acme.events' }])
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="confirm-overridden-ueis"]').exists()).toBe(true)

    await wrapper.find('[data-test="back-button"]').trigger('click')
    expect(wrapper.find('[data-test="confirm-save"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="events-xml"]').exists()).toBe(true)
    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
  })

  it('splits duplicates by the position of an existing source', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      eventsXml: '<events><event><uei>uei.opennms.org/traps/IF-MIB/linkDown</uei></event>'
        + '<event><uei>uei.opennms.org/traps/IF-MIB/linkUp</uei></event></events>',
      existingUeis: {
        'uei.opennms.org/traps/IF-MIB/linkDown': enabled('early.events'),
        'uei.opennms.org/traps/IF-MIB/linkUp': enabled('late.events')
      }
    })
    // evaluation order: early.events, then the existing IF-MIB.events, then late.events
    mockGetOrderedEventConfigSources.mockResolvedValue([
      { name: 'early.events' }, { name: 'IF-MIB.events' }, { name: 'late.events' }
    ])
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="duplicate-ueis"]').text()).toContain('2 UEIs are already defined')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="confirm-replace"]').exists()).toBe(true)
    const overridden = wrapper.find('[data-test="confirm-overridden-ueis"]')
    expect(overridden.text()).toContain('IF-MIB.events is evaluated before the listed sources')
    expect(overridden.text()).toContain('their definitions of the 1 UEI below')
    expect(rowsOf(overridden)).toEqual([['uei.opennms.org/traps/IF-MIB/linkUp', 'late.events']])
    const shadowed = wrapper.find('[data-test="confirm-shadowed-ueis"]')
    expect(rowsOf(shadowed)).toEqual([['uei.opennms.org/traps/IF-MIB/linkDown', 'early.events']])
  })

  it('only asks to replace when the duplicates are all evaluated first', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('early.events') }
    })
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'early.events' }, { name: 'IF-MIB.events' }])
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="confirm-overridden-ueis"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="confirm-shadowed-ueis"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="confirm-save-button"]').text()).toBe('Replace Events')
  })

  it('saves without confirmation when a duplicate source was deleted after the preview', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('deleted.events') }
    })
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(mockUploadEventConfigFiles).toHaveBeenCalledTimes(1)
  })

  it('tags disabled definitions in the warning', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: {
        'uei.opennms.org/traps/IF-MIB/linkDown': [{ name: 'acme.events', enabled: true }, { name: 'old.events', enabled: false }]
      }
    })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()

    const warning = wrapper.find('[data-test="duplicate-ueis"]')
    const sources = warning.findAll('[data-test="duplicate-uei-source"]')
    expect(sources.map(source => source.text().replace(/\s+/g, ' ').trim())).toEqual(['acme.events', 'old.events disabled'])
    expect(sources[0].find('[data-test="disabled-tag"]').exists()).toBe(false)
    expect(sources[1].find('[data-test="disabled-tag"]').exists()).toBe(true)
    expect(warning.find('[data-test="disabled-duplicates-hint"]').exists()).toBe(true)
  })

  it('leaves disabled definitions out of the confirmation', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: {
        'uei.opennms.org/traps/IF-MIB/linkDown': [{ name: 'acme.events', enabled: true }, { name: 'old.events', enabled: false }]
      }
    })
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'acme.events' }, { name: 'old.events' }])
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(rowsOf(wrapper.find('[data-test="confirm-overridden-ueis"]')))
      .toEqual([['uei.opennms.org/traps/IF-MIB/linkDown', 'acme.events']])
  })

  it('saves without confirmation when only disabled definitions share the UEIs', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': [{ name: 'old.events', enabled: false }] }
    })
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'old.events' }])
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="duplicate-ueis"]').exists()).toBe(true)

    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="confirm-save"]').exists()).toBe(false)
    expect(mockUploadEventConfigFiles).toHaveBeenCalledTimes(1)
  })

  it('checks the UEIs of the edited XML again when saving', async () => {
    mockGenerateEvents.mockResolvedValue({ ...preview, existingUeis: {}})
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'acme.events' }])
    // the edit renames the UEI to one that acme.events already defines
    mockFindExistingUeis.mockResolvedValue({ 'uei.opennms.org/vendor/acme/linkDown': enabled('acme.events') })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="duplicate-ueis"]').exists()).toBe(false)

    await wrapper.find('[data-test="events-xml"]').setValue(
      '<events><event><uei>uei.opennms.org/vendor/acme/linkDown</uei></event></events>')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(mockFindExistingUeis).toHaveBeenCalledWith('IF-MIB.mib',
      '<events><event><uei>uei.opennms.org/vendor/acme/linkDown</uei></event></events>')
    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
    expect(rowsOf(wrapper.find('[data-test="confirm-overridden-ueis"]')))
      .toEqual([['uei.opennms.org/vendor/acme/linkDown', 'acme.events']])

    // back in the preview, the warning shows the duplicate found on save
    await wrapper.find('[data-test="back-button"]').trigger('click')
    expect(rowsOf(wrapper.find('[data-test="duplicate-ueis"]')))
      .toEqual([['uei.opennms.org/vendor/acme/linkDown', 'acme.events']])
  })

  it('saves without confirmation when a duplicate is gone by the time of the save', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('acme.events') }
    })
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'acme.events' }])
    mockFindExistingUeis.mockResolvedValue({})
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="confirm-save"]').exists()).toBe(false)
    expect(mockUploadEventConfigFiles).toHaveBeenCalledTimes(1)
  })

  it('blocks saving when the duplicate check fails', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    mockFindExistingUeis.mockRejectedValue(new Error('boom'))
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="validation-error"]').text()).toContain('duplicate UEIs')
  })

  it('drops a duplicate from the warning once its UEI is edited away', async () => {
    mockGenerateEvents.mockResolvedValue({
      ...preview,
      existingUeis: { 'uei.opennms.org/traps/IF-MIB/linkDown': enabled('acme.events') }
    })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="duplicate-ueis"]').exists()).toBe(true)

    await wrapper.find('[data-test="events-xml"]').setValue(
      '<events><event><uei>uei.opennms.org/traps/IF-MIB/linkDownRenamed</uei></event></events>')
    // the XML is parsed only once the typing pauses
    expect(wrapper.find('[data-test="duplicate-ueis"]').exists()).toBe(true)
    await new Promise(resolve => setTimeout(resolve, 350))
    expect(wrapper.find('[data-test="duplicate-ueis"]').exists()).toBe(false)
  })

  it('emits failed when the parse is unsuccessful', async () => {
    const failure = { success: false, errors: 'parse error', missingDependencies: ['FOO-MIB'] }
    mockGenerateEvents.mockResolvedValue(failure)
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('failed')?.[0]).toEqual([failure])
  })

  it('saves the edited XML through the eventconf upload endpoint', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(mockUploadEventConfigFiles).toHaveBeenCalledTimes(1)
    const uploaded = mockUploadEventConfigFiles.mock.calls[0][0] as File[]
    expect(uploaded[0].name).toBe('IF-MIB.events.xml')
    expect(wrapper.find('[data-test="events-saved"]').exists()).toBe(true)
    expect(mockShowSnackBar).toHaveBeenCalled()
  })

  it('asks for confirmation when the event source already exists', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'IF-MIB.events' }])
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    // nothing is uploaded until the user confirms the replacement
    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="confirm-replace"]').text()).toContain('IF-MIB.events')

    await wrapper.find('[data-test="confirm-save-button"]').trigger('click')
    await flushPromises()
    expect(mockUploadEventConfigFiles).toHaveBeenCalledTimes(1)
    expect(wrapper.find('[data-test="events-saved"]').exists()).toBe(true)
  })

  it('returns to the preview from the replace confirmation', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    mockGetOrderedEventConfigSources.mockResolvedValue([{ name: 'IF-MIB.events' }])
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="confirm-replace"]').exists()).toBe(true)

    await wrapper.find('[data-test="back-button"]').trigger('click')
    expect(wrapper.find('[data-test="confirm-replace"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="events-xml"]').exists()).toBe(true)
    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
  })

  it('blocks saving when the source check fails', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    mockGetOrderedEventConfigSources.mockRejectedValue(new Error('boom'))
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="validation-error"]').text()).toContain('existing event source')
  })

  it('rejects malformed XML before uploading', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="events-xml"]').setValue('<events>malformed</events>')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(mockUploadEventConfigFiles).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="validation-error"]').exists()).toBe(true)
  })

  it('navigates to the event configuration page after saving', async () => {
    mockGenerateEvents.mockResolvedValue(preview)
    mockUploadEventConfigFiles.mockResolvedValue({ success: [{ file: 'IF-MIB.events' }], errors: [] })
    wrapper = await createWrapper()
    await wrapper.find('[data-test="generate-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    await wrapper.find('[data-test="go-to-event-config-button"]').trigger('click')

    expect(mockRouterPush).toHaveBeenCalledWith('/event-config')
  })
})
