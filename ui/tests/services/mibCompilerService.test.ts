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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  compileMibFile,
  deleteMibFile,
  generateDataCollection,
  generateEvents,
  generateGraphTemplates,
  getMibFileContent,
  listMibFiles,
  updatePendingMibFile,
  uploadMibFiles
} from '@/services/mibCompilerService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('mibCompilerService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listMibFiles GETs /mibs', async () => {
    const data = { pending: [], compiled: [] }
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data })
    expect(await listMibFiles()).toEqual(data)
    expect(v2.get).toHaveBeenCalledWith('/mibs')
  })

  it('uploadMibFiles POSTs multipart form data with an upload part per file', async () => {
    vi.mocked(v2.post).mockResolvedValue({ status: 200, data: { success: [], errors: [] }})
    const fileA = new File(['a'], 'A-MIB.txt')
    const fileB = new File(['b'], 'B-MIB.txt')
    await uploadMibFiles([fileA, fileB])
    expect(v2.post).toHaveBeenCalledTimes(1)
    const [endpoint, formData] = vi.mocked(v2.post).mock.calls[0]
    expect(endpoint).toBe('/mibs/upload')
    expect((formData as FormData).getAll('upload')).toEqual([fileA, fileB])
  })

  it('getMibFileContent GETs the raw file content as text', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: 'IF-MIB DEFINITIONS' })
    expect(await getMibFileContent('pending', 'IF-MIB.txt')).toBe('IF-MIB DEFINITIONS')
    expect(v2.get).toHaveBeenCalledWith('/mibs/pending/IF-MIB.txt', expect.objectContaining({ responseType: 'text' }))
  })

  it('updatePendingMibFile PUTs text/plain content', async () => {
    vi.mocked(v2.put).mockResolvedValue({ status: 200, data: {}})
    await updatePendingMibFile('IF-MIB.txt', 'content')
    expect(v2.put).toHaveBeenCalledWith(
      '/mibs/pending/IF-MIB.txt',
      'content',
      { headers: { 'Content-Type': 'text/plain' }}
    )
  })

  it('deleteMibFile DELETEs the file in the given directory', async () => {
    vi.mocked(v2.delete).mockResolvedValue({ status: 204, data: undefined })
    await deleteMibFile('compiled', 'IF-MIB.mib')
    expect(v2.delete).toHaveBeenCalledWith('/mibs/compiled/IF-MIB.mib')
  })

  it('compileMibFile POSTs with the overwrite parameter', async () => {
    vi.mocked(v2.post).mockResolvedValue({ status: 200, data: { success: true }})
    await compileMibFile('IF-MIB.txt', true)
    expect(v2.post).toHaveBeenCalledWith('/mibs/pending/IF-MIB.txt/compile', null, { params: { overwrite: true }})
  })

  it('generateEvents POSTs with the ueiBase parameter when given', async () => {
    vi.mocked(v2.post).mockResolvedValue({ status: 200, data: { success: true }})
    await generateEvents('IF-MIB.mib', 'uei.opennms.org/traps/IF-MIB')
    expect(v2.post).toHaveBeenCalledWith(
      '/mibs/compiled/IF-MIB.mib/events',
      null,
      { params: { ueiBase: 'uei.opennms.org/traps/IF-MIB' }}
    )
  })

  it('generateEvents omits the ueiBase parameter when not given', async () => {
    vi.mocked(v2.post).mockResolvedValue({ status: 200, data: { success: true }})
    await generateEvents('IF-MIB.mib')
    expect(v2.post).toHaveBeenCalledWith('/mibs/compiled/IF-MIB.mib/events', null, { params: {}})
  })

  it('generateDataCollection POSTs to the datacollection endpoint', async () => {
    vi.mocked(v2.post).mockResolvedValue({ status: 200, data: { success: true }})
    await generateDataCollection('IF-MIB.mib')
    expect(v2.post).toHaveBeenCalledWith('/mibs/compiled/IF-MIB.mib/datacollection')
  })

  it('generateGraphTemplates POSTs with the dryRun parameter', async () => {
    vi.mocked(v2.post).mockResolvedValue({ status: 200, data: { success: true }})
    await generateGraphTemplates('IF-MIB.mib', false)
    expect(v2.post).toHaveBeenCalledWith('/mibs/compiled/IF-MIB.mib/graph-templates', null, { params: { dryRun: false }})
  })
})
