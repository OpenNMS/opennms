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

import { describe, it, expect, vi, beforeEach } from 'vitest'

const pdf = {
  setFontSize: vi.fn(),
  text: vi.fn(),
  addImage: vi.fn(),
  addPage: vi.fn(),
  output: vi.fn(() => new Blob(['%PDF'], { type: 'application/pdf' })),
  splitTextToSize: vi.fn((t: string) => String(t).split('\n')),
  internal: { pageSize: { getWidth: () => 800, getHeight: () => 600 }}
}
vi.mock('jspdf', () => ({ jsPDF: vi.fn(function () {
  return pdf
}) }))
vi.mock('chart.js', () => ({ Chart: { getChart: vi.fn() }}))

const downloadBlob = vi.fn()
vi.mock('@/composables/useDownload', () => ({ default: () => ({ downloadBlob }) }))

import { exportGraphsToPdf } from '@/components/Resources/utils/graphExport'
import { Chart } from 'chart.js'

const containerWith = (...ids: string[]): HTMLElement => {
  const el = document.createElement('div')
  el.innerHTML = ids.map(id => `<canvas id="${id}"></canvas>`).join('')
  return el
}

describe('exportGraphsToPdf', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // jsdom has no real canvas backend; stub the bits the exporter touches
    HTMLCanvasElement.prototype.getContext = vi.fn(() => ({ fillStyle: '', fillRect: vi.fn(), drawImage: vi.fn() })) as any
    HTMLCanvasElement.prototype.toDataURL = vi.fn(() => 'data:image/png;base64,AAAA')
  })

  it('exports nothing and returns 0 when no canvas has a live chart', () => {
    vi.mocked(Chart.getChart).mockReturnValue(undefined as any)

    const count = exportGraphsToPdf(containerWith('a', 'b'), 'Resource Graphs')

    expect(count).toBe(0)
    expect(pdf.addImage).not.toHaveBeenCalled()
    expect(downloadBlob).not.toHaveBeenCalled()
  })

  it('adds one image per live chart, saves a sanitized filename, and returns the count', () => {
    vi.mocked(Chart.getChart).mockImplementation(
      (c: any) => ({ options: { plugins: { title: { text: `Title ${c.id}` }}}}) as any
    )

    const count = exportGraphsToPdf(containerWith('g1', 'g2'), 'Resource Graphs')

    expect(count).toBe(2)
    expect(pdf.addImage).toHaveBeenCalledTimes(2)
    expect(downloadBlob).toHaveBeenCalledWith(expect.any(Blob), 'Resource_Graphs.pdf')
  })

  it('skips canvases with no chart but still exports the ones that have one', () => {
    vi.mocked(Chart.getChart).mockImplementation(
      (c: any) => (c.id === 'live' ? ({ options: { plugins: { title: { text: 'Live' }}}}) : undefined) as any
    )

    const count = exportGraphsToPdf(containerWith('dead', 'live'), 'Resource Graphs')

    expect(count).toBe(1)
    expect(pdf.addImage).toHaveBeenCalledTimes(1)
  })

  it('skips a canvas whose image capture throws instead of aborting the whole export', () => {
    vi.mocked(Chart.getChart).mockImplementation(
      (c: any) => ({ options: { plugins: { title: { text: c.id }}}}) as any
    )
    // make the SECOND canvas's toDataURL throw (e.g. tainted canvas)
    let calls = 0
    HTMLCanvasElement.prototype.toDataURL = vi.fn(() => {
      calls += 1
      if (calls === 2) {
        throw new Error('tainted canvas')
      }
      return 'data:image/png;base64,AAAA'
    })

    const count = exportGraphsToPdf(containerWith('ok', 'bad'), 'Resource Graphs')

    expect(count).toBe(1)
    expect(downloadBlob).toHaveBeenCalled()
  })
})
