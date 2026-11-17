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

import CompileErrorsDialog from '@/components/MibCompiler/Dialog/CompileErrorsDialog.vue'
import { MibParseResult } from '@/types/mibCompiler'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

// OnmsDialog teleports to body; a passthrough stub keeps the content queryable
const OnmsDialogStub = {
  name: 'OnmsDialog',
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>',
  props: ['visible', 'header', 'width']
}

describe('CompileErrorsDialog', () => {
  let wrapper: VueWrapper

  const createWrapper = (result: MibParseResult) => {
    return mount(CompileErrorsDialog, {
      props: {
        visible: true,
        fileName: 'BCN-DNS-MIB.mib',
        result
      },
      global: {
        plugins: [PrimeVue],
        stubs: { OnmsDialog: OnmsDialogStub }
      }
    })
  }

  afterEach(() => {
    wrapper?.unmount()
  })

  it('marks missing dependencies that are already in the pending directory', () => {
    wrapper = createWrapper({
      success: false,
      errors: 'ERROR: Cannot find module BCN-SMI-MIB',
      missingDependencies: ['BCN-TC-MIB', 'BCN-SMI-MIB'],
      pendingDependencies: ['BCN-TC-MIB']
    })
    const text = wrapper.find('[data-test="missing-dependencies"]').text()
    expect(text).toContain('BCN-TC-MIB (in Pending)')
    expect(text).toContain('BCN-SMI-MIB')
    expect(text).not.toContain('BCN-SMI-MIB (in Pending)')
    expect(wrapper.find('[data-test="pending-dependencies-hint"]').exists()).toBe(true)
  })

  it('omits the pending hint when no missing dependency is in pending', () => {
    wrapper = createWrapper({
      success: false,
      errors: 'ERROR: Cannot find module BCN-SMI-MIB',
      missingDependencies: ['BCN-SMI-MIB'],
      pendingDependencies: []
    })
    expect(wrapper.find('[data-test="pending-dependencies-hint"]').exists()).toBe(false)
  })

  it('collapses the parser output behind a toggle when dependencies are missing', async () => {
    wrapper = createWrapper({
      success: false,
      errors: 'ERROR: Cannot find module SNMPv2-SMI',
      missingDependencies: ['BCN-SMI-MIB'],
      pendingDependencies: []
    })
    expect(wrapper.find('[data-test="compile-errors"]').exists()).toBe(false)
    await wrapper.find('[data-test="toggle-parser-output"]').trigger('click')
    expect(wrapper.find('[data-test="compile-errors"]').text()).toContain('Cannot find module SNMPv2-SMI')
    await wrapper.find('[data-test="toggle-parser-output"]').trigger('click')
    expect(wrapper.find('[data-test="compile-errors"]').exists()).toBe(false)
  })

  it('shows the parser output directly when there are no missing dependencies', () => {
    wrapper = createWrapper({
      success: false,
      errors: 'ERROR: unexpected token',
      missingDependencies: [],
      pendingDependencies: []
    })
    expect(wrapper.find('[data-test="toggle-parser-output"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="compile-errors"]').text()).toContain('unexpected token')
  })
})
