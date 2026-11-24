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

import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import UploadMibFiles from '@/components/MibCompiler/UploadMibFiles.vue'

const { service, showSnackBarMock } = vi.hoisted(() => ({
  service: {
    listMibFiles: vi.fn(),
    uploadMibFiles: vi.fn()
  },
  showSnackBarMock: vi.fn()
}))

vi.mock('@/services/mibCompilerService', () => service)
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

const upload = async () => {
  const wrapper = mount(UploadMibFiles, {
    global: { plugins: [createTestingPinia({ stubActions: false }), PrimeVue] }
  })
  const input = wrapper.get('[data-test="mib-upload-input"]')
  const file = new File(['MIB'], 'NEW-MIB.txt', { type: 'text/plain' })
  Object.defineProperty(input.element, 'files', { value: [file], configurable: true })
  await input.trigger('change')
  await flushPromises()
  return wrapper
}

describe('UploadMibFiles.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    service.uploadMibFiles.mockResolvedValue({ success: [{ file: 'NEW-MIB.txt' }], errors: [] })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('stays quiet when the list reloads after an upload', async () => {
    service.listMibFiles.mockResolvedValue({ pending: [], compiled: [] })
    await upload()

    expect(service.uploadMibFiles).toHaveBeenCalled()
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })

  it('shows an error when the list cannot be reloaded after an upload', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    service.listMibFiles.mockRejectedValue(new Error('boom'))
    await upload()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to load the MIB files. Try again.', error: true })
    expect(consoleError).toHaveBeenCalled()
  })
})
