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

import PluginUnloadDialog from '@/components/PluginManagement/PluginUnloadDialog.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/pluginManagementStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><h2 data-test="dialog-header">{{ header }}</h2><slot /><slot name="footer" /></div>'
}

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: 'abc', size: 10, uploadedBy: 'admin', uploadedAt: 1, features: ['alec', 'alec-ui'], bootFile: 'alec.boot', autoStart: true, status: 'installed' as const, pendingRestart: false }

describe('PluginUnloadDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (plugin = PLUGIN) => {
    wrapper = mount(PluginUnloadDialog, {
      props: { visible: false, plugin },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = { unload: vi.fn().mockResolvedValue({ success: true, message: '', payload: { ...PLUGIN, status: 'unloaded', pendingRestart: true }}) }
    vi.mocked(usePluginManagementStore).mockReturnValue(store)
  })

  it('names what is removed and unloads only after the exact KAR name is typed', async () => {
    await mountDialog()
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('Unload alec?')
    expect(wrapper.find('[data-test="removal-callout"]').text()).toContain('alec.kar')
    expect(wrapper.find('[data-test="removal-callout"]').text()).toContain('alec.boot')
    expect(wrapper.find('[data-test="stop-callout"]').text()).toContain('alec, alec-ui')
    expect(wrapper.find('[data-test="unmanaged-note"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="unload-button"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="confirm-input"]').setValue('ALEC')
    expect(wrapper.find('[data-test="unload-button"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="confirm-input"]').setValue('alec')
    expect(wrapper.find('[data-test="unload-button"]').attributes('disabled')).toBeUndefined()
    await wrapper.find('[data-test="unload-button"]').trigger('click')
    await flushPromises()
    expect(store.unload).toHaveBeenCalledWith('alec')
    expect(wrapper.emitted('unloaded')?.[0][0]).toMatchObject({ karName: 'alec', status: 'unloaded' })
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('adds the note for an unmanaged plugin', async () => {
    await mountDialog({ ...PLUGIN, status: 'unmanaged', bootFile: null })
    expect(wrapper.find('[data-test="unmanaged-note"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="removal-callout"]').text()).not.toContain('Boot file')
  })

  it('shows the reason and stays open when the unload fails', async () => {
    store.unload.mockResolvedValueOnce({ success: false, message: 'The plugin container is not available.' })
    await mountDialog()
    await wrapper.find('[data-test="confirm-input"]').setValue('alec')
    await wrapper.find('[data-test="unload-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('not available')
    expect(wrapper.emitted('unloaded')).toBeUndefined()
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })

  it('cancels without unloading', async () => {
    await mountDialog()
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(store.unload).not.toHaveBeenCalled()
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })
})
