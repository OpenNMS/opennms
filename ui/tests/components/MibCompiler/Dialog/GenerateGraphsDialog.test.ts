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

import GenerateGraphsDialog from '@/components/MibCompiler/Dialog/GenerateGraphsDialog.vue'
import { MibGraphTemplatesResult } from '@/types/mibCompiler'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

type Deferred = {
  resolve: (value: MibGraphTemplatesResult) => void
  reject: (reason: unknown) => void
}
let deferreds: Deferred[] = []

const mockGenerateGraphTemplates = vi.fn()
vi.mock('@/services/mibCompilerService', () => ({
  generateGraphTemplates: (...args: unknown[]) => mockGenerateGraphTemplates(...args)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar })
}))

const OnmsDialogStub = {
  name: 'OnmsDialog',
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>',
  props: ['visible', 'header', 'width']
}

const result = (mibName: string): MibGraphTemplatesResult => ({
  success: true,
  mibName,
  graphCount: 3,
  fileName: `${mibName}-graph.properties`,
  content: `reports for ${mibName}`,
  written: false
})

describe('GenerateGraphsDialog', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    vi.clearAllMocks()
    deferreds = []
    mockGenerateGraphTemplates.mockImplementation(() =>
      new Promise<MibGraphTemplatesResult>((resolve, reject) => {
        deferreds.push({ resolve, reject })
      }))
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  const createWrapper = () => {
    return mount(GenerateGraphsDialog, {
      props: { visible: false, fileName: 'A-MIB.mib' },
      global: {
        plugins: [PrimeVue],
        stubs: { OnmsDialog: OnmsDialogStub }
      }
    })
  }

  it('ignores a preview response that arrives after the dialog reopened for another file', async () => {
    wrapper = createWrapper()
    await wrapper.setProps({ visible: true })
    await flushPromises()

    // close while A-MIB is still loading, then reopen for B-MIB
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ fileName: 'B-MIB.mib' })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(deferreds.length).toBe(2)

    // A's late response must not fill B's dialog or clear its loading state
    deferreds[0].resolve(result('A-MIB'))
    await flushPromises()
    expect(wrapper.find('[data-test="graph-count"]').text()).not.toContain('A-MIB')

    deferreds[1].resolve(result('B-MIB'))
    await flushPromises()
    expect(wrapper.find('[data-test="graph-count"]').text()).toContain('B-MIB')
  })

  it('ignores a write response that arrives after the dialog reopened for another file', async () => {
    wrapper = createWrapper()
    await wrapper.setProps({ visible: true })
    await flushPromises()
    deferreds[0].resolve(result('A-MIB'))
    await flushPromises()

    // start the write for A-MIB, then close and reopen for B-MIB while it runs
    await wrapper.find('[data-test="write-button"]').trigger('click')
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ fileName: 'B-MIB.mib' })
    await wrapper.setProps({ visible: true })
    await flushPromises()

    // A's late write completion must not switch B's dialog to the saved state
    deferreds[1].resolve({ ...result('A-MIB'), written: true })
    await flushPromises()
    expect(wrapper.find('[data-test="graphs-saved"]').exists()).toBe(false)

    deferreds[2].resolve(result('B-MIB'))
    await flushPromises()
    expect(wrapper.find('[data-test="graph-count"]').text()).toContain('B-MIB')
  })

  it('asks for confirmation before overwriting an existing graph template file', async () => {
    wrapper = createWrapper()
    await wrapper.setProps({ visible: true })
    await flushPromises()
    deferreds[0].resolve(result('A-MIB'))
    await flushPromises()

    // the server answers the write with a 409: the target file exists
    await wrapper.find('[data-test="write-button"]').trigger('click')
    deferreds[1].reject(Object.assign(new Error('Request failed with status code 409'), {
      isAxiosError: true,
      response: { status: 409, data: { targetFile: 'A-MIB-graph.properties' }}
    }))
    await flushPromises()

    expect(wrapper.find('[data-test="graphs-saved"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="confirm-overwrite"]').text()).toContain('A-MIB-graph.properties')
    expect(mockGenerateGraphTemplates).toHaveBeenLastCalledWith('A-MIB.mib', false, false)

    // confirming retries the write with overwrite=true
    await wrapper.find('[data-test="overwrite-button"]').trigger('click')
    expect(mockGenerateGraphTemplates).toHaveBeenLastCalledWith('A-MIB.mib', false, true)
    deferreds[2].resolve({ ...result('A-MIB'), written: true })
    await flushPromises()
    expect(wrapper.find('[data-test="graphs-saved"]').exists()).toBe(true)
  })

  it('returns to the preview from the overwrite confirmation', async () => {
    wrapper = createWrapper()
    await wrapper.setProps({ visible: true })
    await flushPromises()
    deferreds[0].resolve(result('A-MIB'))
    await flushPromises()

    await wrapper.find('[data-test="write-button"]').trigger('click')
    deferreds[1].reject(Object.assign(new Error('Request failed with status code 409'), {
      isAxiosError: true,
      response: { status: 409, data: { targetFile: 'A-MIB-graph.properties' }}
    }))
    await flushPromises()
    expect(wrapper.find('[data-test="confirm-overwrite"]').exists()).toBe(true)

    await wrapper.find('[data-test="back-button"]').trigger('click')
    expect(wrapper.find('[data-test="confirm-overwrite"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="graph-count"]').text()).toContain('A-MIB')
  })

  it('ignores a preview response that arrives after the dialog closed', async () => {
    wrapper = createWrapper()
    await wrapper.setProps({ visible: true })
    await flushPromises()
    await wrapper.setProps({ visible: false })

    deferreds[0].resolve(result('A-MIB'))
    await flushPromises()
    expect(mockShowSnackBar).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="graph-count"]').exists()).toBe(false)
  })
})
