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

// Covers the lazy server-side pagination added to DCBTable.vue (commit
// 1154e43aa8b) and the fix that keeps DCBSearch/DCBGroupFilters/
// DCBTableStatusDropdown from reverting the paginator's page size back to 20
// whenever they reset-and-refetch (NMS-20081 review finding 1).
//
// Unlike tests/deviceConfigBackup.test.ts, these tests use a real (non-testing)
// Pinia instance so deviceStore.updateDeviceConfigBackupQueryParams runs its
// actual merge logic instead of being replaced by a no-op spy -- that merge is
// exactly the behavior under test. Only the HTTP boundary (API.getDeviceConfigBackups)
// is mocked.

import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { OnmsMenu, OnmsSearchInput, OnmsTable, OnmsTooltip } from '@opennms/onms-ui'
import dateFormatDirective from '@/directives/v-date'
import DCBTable from '@/components/Device/DCBTable.vue'
import DCBSearch from '@/components/Device/DCBSearch.vue'
import DCBGroupFilters from '@/components/Device/DCBGroupFilters.vue'
import DCBTableStatusDropdown from '@/components/Device/DCBTableStatusDropdown.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import API from '@/services'
import { SORT } from '@/types'
import { beforeEach, describe, expect, test, vi } from 'vitest'

vi.mock('@/services', () => ({
  default: {
    getDeviceConfigBackups: vi.fn()
  }
}))

const mountOpts = {
  global: {
    plugins: [PrimeVue],
    directives: {
      date: dateFormatDirective,
      'onms-tooltip': OnmsTooltip
    }
  }
}

describe('DCB pagination', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(API.getDeviceConfigBackups).mockReset()
    vi.mocked(API.getDeviceConfigBackups).mockResolvedValue(false)
  })

  test('rows initializes from the persisted store limit on mount, not the default page size', async () => {
    const deviceStore = useDeviceStore()
    deviceStore.deviceConfigBackupQueryParams = { offset: 200, limit: 100 }

    const wrapper = mount(DCBTable, mountOpts)
    const table = wrapper.findComponent(OnmsTable)

    expect(table.props('rows')).toBe(100)
    expect(table.props('first')).toBe(200)
  })

  test('page event merges { limit, offset } from the event into the store and refetches', async () => {
    const wrapper = mount(DCBTable, mountOpts)
    const table = wrapper.findComponent(OnmsTable)

    await table.vm.$emit('page', { first: 200, rows: 100, page: 2, pageCount: 5 })

    const deviceStore = useDeviceStore()
    expect(deviceStore.deviceConfigBackupQueryParams.limit).toBe(100)
    expect(deviceStore.deviceConfigBackupQueryParams.offset).toBe(200)
    expect(API.getDeviceConfigBackups).toHaveBeenCalledTimes(1)
  })

  test('sort event resets offset to 0 while keeping the current page size', async () => {
    const wrapper = mount(DCBTable, mountOpts)
    const table = wrapper.findComponent(OnmsTable)

    // move off the default page size first, as a user would via the paginator
    await table.vm.$emit('page', { first: 0, rows: 100, page: 0, pageCount: 5 })
    vi.mocked(API.getDeviceConfigBackups).mockClear()

    await table.vm.$emit('sort', { sortField: 'ipAddress', sortOrder: -1 })

    const deviceStore = useDeviceStore()
    expect(deviceStore.deviceConfigBackupQueryParams.offset).toBe(0)
    expect(deviceStore.deviceConfigBackupQueryParams.limit).toBe(100)
    expect(deviceStore.deviceConfigBackupQueryParams.order).toBe(SORT.DESCENDING)
    expect(deviceStore.deviceConfigBackupQueryParams.orderBy).toBe('ipAddress')
    expect(API.getDeviceConfigBackups).toHaveBeenCalledTimes(1)
  })

  test('a search refetch does not clobber a non-default page size', async () => {
    const deviceStore = useDeviceStore()
    deviceStore.deviceConfigBackupQueryParams = { offset: 200, limit: 100 }

    const wrapper = mount(DCBSearch, mountOpts)
    const searchInput = wrapper.findComponent(OnmsSearchInput)

    await searchInput.vm.$emit('update:modelValue', 'cisco')

    expect(deviceStore.deviceConfigBackupQueryParams.limit).toBe(100)
    expect(deviceStore.deviceConfigBackupQueryParams.offset).toBe(0)
    expect(deviceStore.deviceConfigBackupQueryParams.search).toBe('cisco')
  })

  test('a group-by filter refetch does not clobber a non-default page size', async () => {
    const deviceStore = useDeviceStore()
    deviceStore.deviceConfigBackupQueryParams = { offset: 200, limit: 100 }
    // backupStatusOptions has a non-empty default, so the "Backup Status"
    // OnmsMenu's items are populated without further setup
    const wrapper = mount(DCBGroupFilters, mountOpts)
    const statusMenu = wrapper.findComponent({ ref: 'statusMenu' })
    const items = statusMenu.props('items') as { command: () => void }[]

    expect(items.length).toBeGreaterThan(0)
    items[0].command()

    expect(deviceStore.deviceConfigBackupQueryParams.limit).toBe(100)
    expect(deviceStore.deviceConfigBackupQueryParams.offset).toBe(0)
    expect(deviceStore.deviceConfigBackupQueryParams.groupBy).toBe('status')
  })

  test('a status-dropdown filter refetch does not clobber a non-default page size', async () => {
    const deviceStore = useDeviceStore()
    deviceStore.deviceConfigBackupQueryParams = { offset: 200, limit: 100 }

    const wrapper = mount(DCBTableStatusDropdown, mountOpts)
    const menu = wrapper.findComponent(OnmsMenu)
    const items = menu.props('items') as { command: () => void }[]

    expect(items.length).toBeGreaterThan(0)
    items[0].command()

    expect(deviceStore.deviceConfigBackupQueryParams.limit).toBe(100)
    expect(deviceStore.deviceConfigBackupQueryParams.offset).toBe(0)
    expect(deviceStore.deviceConfigBackupQueryParams.status).toBeDefined()
  })
})
