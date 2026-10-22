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

import AssetEditConfirmDialog from '@/components/Nodes/AssetEditConfirmDialog.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// Render the dialog's content inline rather than teleported, as other dialog tests do.
const ConfirmStub = {
  name: 'OnmsConfirmationDialog',
  props: ['visible', 'title', 'actionButtonText'],
  emits: ['ok', 'cancel'],
  template: '<div v-if="visible"><h3>{{ title }}</h3><slot name="content" /><button class="ok" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel" @click="$emit(\'cancel\')" /></div>'
}

const mountDialog = (visible = true) =>
  mount(AssetEditConfirmDialog, {
    props: { visible, foreignSource: 'Demo Stores' },
    global: { plugins: [PrimeVue], stubs: { OnmsConfirmationDialog: ConfirmStub }}
  })

describe('AssetEditConfirmDialog.vue', () => {
  it('warns that edits are rolled back by the named requisition', () => {
    const text = mountDialog().text()

    expect(text).toContain('Edit Asset Fields')
    expect(text).toContain('rolled back the next time the requisition "Demo Stores" is synchronized')
  })

  it('emits ok and cancel', async () => {
    const wrapper = mountDialog()

    await wrapper.find('.ok').trigger('click')
    await wrapper.find('.cancel').trigger('click')

    expect(wrapper.emitted('ok')).toHaveLength(1)
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('shows nothing when not visible', () => {
    expect(mountDialog(false).text()).toBe('')
  })
})
