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

import MibFilesTable from '@/components/MibCompiler/MibFilesTable.vue'
import { MibFileInfo } from '@/types/mibCompiler'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

const files: MibFileInfo[] = [
  { name: 'IF-MIB.txt', size: 2048, lastModified: 1700000000000 },
  { name: 'SNMPv2-SMI.txt', size: 512, lastModified: 1700000100000 }
]

describe('MibFilesTable', () => {
  let wrapper: VueWrapper

  const createWrapper = (props: Record<string, unknown> = {}) => {
    return mount(MibFilesTable, {
      props: {
        location: 'pending',
        title: 'Pending MIB Files',
        files,
        ...props
      },
      global: {
        plugins: [PrimeVue]
      }
    })
  }

  afterEach(() => {
    wrapper?.unmount()
  })

  it('renders a row per file', async () => {
    wrapper = createWrapper()
    await flushPromises()
    const names = wrapper.findAll('[data-test="file-name"]').map(node => node.text())
    expect(names).toEqual(['IF-MIB.txt', 'SNMPv2-SMI.txt'])
  })

  it('filters rows by the search term', async () => {
    wrapper = createWrapper()
    await flushPromises()
    await wrapper.find('input[data-test="search-input"]').setValue('if-mib')
    await flushPromises()
    const names = wrapper.findAll('[data-test="file-name"]').map(node => node.text())
    expect(names).toEqual(['IF-MIB.txt'])
  })

  it('shows the empty list message when there are no files', async () => {
    wrapper = createWrapper({ files: [] })
    await flushPromises()
    expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('emits edit, compile and delete for pending files', async () => {
    wrapper = createWrapper()
    await flushPromises()
    await wrapper.findAll('[data-test="edit-button"]')[0].trigger('click')
    await wrapper.findAll('[data-test="compile-button"]')[0].trigger('click')
    await wrapper.findAll('[data-test="delete-button"]')[0].trigger('click')
    expect(wrapper.emitted('edit')?.[0]).toEqual([files[0]])
    expect(wrapper.emitted('compile')?.[0]).toEqual([files[0]])
    expect(wrapper.emitted('delete')?.[0]).toEqual([files[0]])
  })

  it('emits view when the file name is clicked', async () => {
    wrapper = createWrapper()
    await flushPromises()
    await wrapper.findAll('[data-test="file-name"]')[1].trigger('click')
    expect(wrapper.emitted('view')?.[0]).toEqual([files[1]])
  })

  it('hides edit/compile and shows the generate menu for compiled files', async () => {
    wrapper = createWrapper({ location: 'compiled', title: 'Compiled MIB Files' })
    await flushPromises()
    expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="compile-button"]').exists()).toBe(false)
    expect(wrapper.findAll('[data-test="row-menu-button"]').length).toBe(files.length)
  })
})
