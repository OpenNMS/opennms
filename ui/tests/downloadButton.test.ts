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

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DownloadButton from '@/components/Common/DownloadButton.vue'

describe('DownloadButton', () => {
  it('shows the format under the icon and names the button', async () => {
    const wrapper = mount(DownloadButton, { props: { format: 'PDF', title: 'Download as PDF' }})

    const button = wrapper.get('button')
    expect(button.attributes('aria-label')).toBe('Download as PDF')
    expect(button.attributes('title')).toBe('Download as PDF')
    expect(wrapper.get('.download-button__format').text()).toBe('PDF')
    expect(wrapper.find('.download-button__icon').exists()).toBe(true)

    await button.trigger('click')
    expect(wrapper.emitted('click')).toHaveLength(1)
  })
})
