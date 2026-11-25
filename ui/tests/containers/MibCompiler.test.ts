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
import MibCompiler from '@/containers/MibCompiler.vue'
import { useMenuStore } from '@/stores/menuStore'

const { service, showSnackBarMock } = vi.hoisted(() => ({
  service: {
    listMibFiles: vi.fn(),
    compileMibFile: vi.fn(),
    deleteMibFile: vi.fn(),
    getMibFileContent: vi.fn()
  },
  showSnackBarMock: vi.fn()
}))

vi.mock('@/services/mibCompilerService', () => service)
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

const mountPage = () => {
  const pinia = createTestingPinia({ stubActions: false })
  useMenuStore(pinia).mainMenu = { homeUrl: '/opennms' } as never
  return mount(MibCompiler, {
    global: {
      plugins: [pinia, PrimeVue],
      stubs: {
        BreadCrumbs: true,
        UploadMibFiles: true,
        MibFilesTable: true,
        MibFileViewerDrawer: true,
        CompileErrorsDialog: true,
        GenerateDataCollectionDialog: true,
        GenerateEventsDialog: true,
        GenerateGraphsDialog: true
      }
    }
  })
}

describe('MibCompiler.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('stays quiet when the MIB files load', async () => {
    service.listMibFiles.mockResolvedValue({ pending: [], compiled: [] })
    mountPage()
    await flushPromises()

    expect(service.listMibFiles).toHaveBeenCalled()
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })

  it('shows an error when the MIB files fail to load', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    service.listMibFiles.mockRejectedValue(new Error('boom'))
    mountPage()
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to load the MIB files. Try again.', error: true })
    expect(consoleError).toHaveBeenCalled()
  })
})
