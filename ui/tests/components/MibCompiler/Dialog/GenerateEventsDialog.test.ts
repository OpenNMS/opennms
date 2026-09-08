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
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockGenerateEvents = vi.fn()
vi.mock('@/services/mibCompilerService', () => ({
  generateEvents: (...args: unknown[]) => mockGenerateEvents(...args)
}))

// happy-dom's DOMParser never reports parse errors, so the well-formedness
// check is mocked: XML containing 'malformed' is treated as broken.
vi.mock('@/components/MibCompiler/mibFilesValidator', async importOriginal => ({
  ...(await importOriginal<object>()),
  isWellFormedXml: (xml: string) => !xml.includes('malformed')
}))

const mockUploadEventConfigFiles = vi.fn()
vi.mock('@/services/eventConfigService', () => ({
  uploadEventConfigFiles: (...args: unknown[]) => mockUploadEventConfigFiles(...args)
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
