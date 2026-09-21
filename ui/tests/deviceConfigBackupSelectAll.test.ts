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

// Covers NMS-20081: DCB "select all" previously snapshotted only the loaded
// page's ids into selectedIds and never re-synced on paging/sort/filter, so
// Download acted on stale ids after paging and Backup filtered the new page
// by old ids (empty list -> error). These tests pin the fix: Download/Backup
// now resolve "all matching current filters" at action time via a dedicated
// fetch-all, chunking downloads at 500 ids per GET.
//
// Store-level only (no component mount), following the real-Pinia /
// API-boundary-only-mock convention from tests/deviceConfigBackupPagination.test.ts.

import { createPinia, setActivePinia } from 'pinia'
import { useDeviceStore } from '@/stores/deviceStore'
import API from '@/services'
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { SORT } from '@/types'
import { beforeEach, describe, expect, test, vi } from 'vitest'

const { showSnackBarMock, downloadFileMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  downloadFileMock: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('@/composables/useDownload', () => ({
  default: () => ({ downloadFile: downloadFileMock })
}))

vi.mock('@/services', () => ({
  default: {
    getDeviceConfigBackups: vi.fn(),
    downloadDeviceConfigs: vi.fn(),
    backupDeviceConfig: vi.fn()
  }
}))

const baseConfig: DeviceConfigBackup = {
  id: 0,
  ipInterfaceId: 1,
  ipAddress: '10.0.0.1',
  deviceName: 'device',
  location: 'location',
  lastBackupDate: 1,
  lastUpdatedDate: 1,
  lastSucceededDate: 1,
  lastFailedDate: 1,
  backupStatus: 'success',
  scheduledInterval: {},
  fileName: 'file',
  failureReason: '',
  encoding: '',
  configType: 'running',
  configName: 'Running Configuration',
  nodeId: 1,
  nodeLabel: 'node',
  operatingSystem: '',
  isSuccessfulBackup: true,
  nextScheduledBackupDate: 1,
  config: 'config',
  monitoredServiceId: 1,
  serviceName: 'svc'
}

const makeConfigs = (ids: number[]): DeviceConfigBackup[] => ids.map(id => ({ ...baseConfig, id }))

const idRange = (start: number, count: number): number[] => Array.from({ length: count }, (_, i) => start + i)

const listResponse = (ids: number[], total: number) => ({
  data: makeConfigs(ids),
  headers: { 'content-range': String(total) }
})

describe('DCB select-all resolves all matching configs at action time', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(API.getDeviceConfigBackups).mockReset()
    vi.mocked(API.downloadDeviceConfigs).mockReset()
    vi.mocked(API.backupDeviceConfig).mockReset()
    showSnackBarMock.mockReset()
    downloadFileMock.mockReset()

    vi.mocked(API.downloadDeviceConfigs).mockResolvedValue({
      headers: {},
      data: new Blob()
    } as any)
    vi.mocked(API.backupDeviceConfig).mockResolvedValue({ status: 202 } as any)
  })

  test('stale-snapshot regression pin: download after paging fetches all 1200 matching ids, not the stale loaded page', async () => {
    const deviceStore = useDeviceStore()

    // sort applied before select-all, so the fetch-all call must carry it
    await deviceStore.updateDeviceConfigBackupQueryParams({ order: SORT.DESCENDING, orderBy: 'ipAddress' })

    // page 1: 20 configs loaded, server reports 1200 total matching current filters
    const page1Ids = idRange(1, 20)
    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(page1Ids, 1200) as any)
    await deviceStore.getDeviceConfigBackups()

    deviceStore.setSelectedIds('all')

    // simulate paging to a different page: the loaded rows change entirely
    const page2Ids = idRange(1000, 20)
    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(page2Ids, 1200) as any)
    await deviceStore.getDeviceConfigBackups()

    // the fetch-all call download triggers
    const allIds = idRange(1, 1200)
    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(allIds, 1200) as any)

    await deviceStore.downloadSelectedDevices()

    const calls = vi.mocked(API.getDeviceConfigBackups).mock.calls
    const fetchAllCall = calls[calls.length - 1][0]
    expect(fetchAllCall).toMatchObject({
      offset: 0,
      limit: 1200,
      order: SORT.DESCENDING,
      orderBy: 'ipAddress'
    })

    const downloadedIds = vi.mocked(API.downloadDeviceConfigs).mock.calls.flatMap(call => call[0] as number[])
    expect(downloadedIds).toEqual(allIds)
    expect(downloadedIds).not.toEqual(page2Ids)
    expect(downloadedIds).not.toEqual(page1Ids)
  })

  test('chunking: 1200 matching ids download in exactly 3 calls of 500/500/200, in order', async () => {
    const deviceStore = useDeviceStore()

    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(idRange(1, 20), 1200) as any)
    await deviceStore.getDeviceConfigBackups()
    deviceStore.setSelectedIds('all')

    const allIds = idRange(1, 1200)
    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(allIds, 1200) as any)

    await deviceStore.downloadSelectedDevices()

    expect(API.downloadDeviceConfigs).toHaveBeenCalledTimes(3)
    const calls = vi.mocked(API.downloadDeviceConfigs).mock.calls
    expect(calls[0][0]).toEqual(allIds.slice(0, 500))
    expect(calls[1][0]).toEqual(allIds.slice(500, 1000))
    expect(calls[2][0]).toEqual(allIds.slice(1000, 1200))
    expect((calls[0][0] as number[]).length).toBe(500)
    expect((calls[1][0] as number[]).length).toBe(500)
    expect((calls[2][0] as number[]).length).toBe(200)
    expect(downloadFileMock).toHaveBeenCalledTimes(3)
  })

  test('backup-all: one API.backupDeviceConfig call with the full 1200-config list', async () => {
    const deviceStore = useDeviceStore()

    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(idRange(1, 20), 1200) as any)
    await deviceStore.getDeviceConfigBackups()
    deviceStore.setSelectedIds('all')

    const allIds = idRange(1, 1200)
    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(allIds, 1200) as any)

    await deviceStore.backupSelectedDevices()

    expect(API.backupDeviceConfig).toHaveBeenCalledTimes(1)
    const payload = vi.mocked(API.backupDeviceConfig).mock.calls[0][0] as DeviceConfigBackup[]
    expect(payload).toHaveLength(1200)
    expect(payload.map(c => c.id)).toEqual(allIds)
  })

  test('explicit-selection path is unchanged: no fetch-all call, download and backup act only on the chosen ids', async () => {
    const deviceStore = useDeviceStore()

    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(idRange(1, 20), 1200) as any)
    await deviceStore.getDeviceConfigBackups()

    deviceStore.setSelectedIds([3, 7])
    vi.mocked(API.getDeviceConfigBackups).mockClear()

    await deviceStore.downloadSelectedDevices()

    expect(API.getDeviceConfigBackups).not.toHaveBeenCalled()
    expect(API.downloadDeviceConfigs).toHaveBeenCalledTimes(1)
    expect(API.downloadDeviceConfigs).toHaveBeenCalledWith([3, 7])

    await deviceStore.backupSelectedDevices()

    expect(API.getDeviceConfigBackups).not.toHaveBeenCalled()
    expect(API.backupDeviceConfig).toHaveBeenCalledTimes(1)
    const payload = vi.mocked(API.backupDeviceConfig).mock.calls[0][0] as DeviceConfigBackup[]
    expect(payload.map(c => c.id).sort()).toEqual([3, 7])
  })

  test('setSelectedIds with an explicit array after \'all\' resets allSelected to false', async () => {
    const deviceStore = useDeviceStore()

    vi.mocked(API.getDeviceConfigBackups).mockResolvedValueOnce(listResponse(idRange(1, 20), 1200) as any)
    await deviceStore.getDeviceConfigBackups()

    deviceStore.setSelectedIds('all')
    expect(deviceStore.allSelected).toBe(true)

    deviceStore.setSelectedIds([3, 7])
    expect(deviceStore.allSelected).toBe(false)
  })
})
