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

import StatusListPanel from '@/components/Dashboard/panels/StatusListPanel.vue'
import SeverityIcon from '@/components/Common/SeverityIcon.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const mountPanel = async () => {
  const wrapper = mount(StatusListPanel, {
    props: {
      loader: async () => [
        { id: 1, name: 'Alpha', severity: 'CRITICAL' },
        { id: 2, name: 'Beta', severity: 'normal' }
      ],
      emptyText: 'none',
      refreshTick: 0
    } as never,
    global: { plugins: [createTestingPinia({ stubActions: false })] }
  })
  await flushPromises()
  return wrapper
}

describe('Dashboard StatusListPanel', () => {
  it('draws each row\'s severity with a SeverityIcon', async () => {
    const wrapper = await mountPanel()

    expect(wrapper.findAllComponents(SeverityIcon).map(i => i.props('severity'))).toEqual(['CRITICAL', 'normal'])
  })
})
