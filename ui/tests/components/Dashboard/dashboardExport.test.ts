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

const pdf = {
  text: vi.fn(),
  addImage: vi.fn(),
  addPage: vi.fn(),
  rect: vi.fn(),
  setFillColor: vi.fn(),
  setTextColor: vi.fn(),
  setFont: vi.fn(),
  setFontSize: vi.fn(),
  output: vi.fn(() => new Blob(['%PDF'], { type: 'application/pdf' })),
  internal: { pageSize: { getWidth: () => 842, getHeight: () => 595 }}
}
vi.mock('jspdf', () => ({ jsPDF: vi.fn(function () {
  return pdf
}) }))
const html2canvas = vi.fn()
vi.mock('html2canvas', () => ({ default: (...args: unknown[]) => html2canvas(...args) }))
const downloadBlob = vi.fn()
vi.mock('@/composables/useDownload', () => ({ default: () => ({ downloadBlob }) }))

import { CapturedPanel, dashboardPdfFilename, exportDashboardToPdf, isDark, paginatePanels } from '@/components/Dashboard/utils/dashboardExport'

// jsdom has no layout: elements report the box given by data attributes
const rectOf = (el: Element): DOMRect => {
  const d = (el as HTMLElement).dataset ?? {}
  const top = Number(d.top ?? 0), left = Number(d.left ?? 0), width = Number(d.width ?? 0), height = Number(d.height ?? 0)
  return { top, left, width, height, bottom: top + height, right: left + width, x: left, y: top, toJSON: () => ({}) } as DOMRect
}

const frame = (title: string, box: { top: number; left: number; width: number; height: number }) =>
  `<div class="panel-frame" data-top="${box.top}" data-left="${box.left}" data-width="${box.width}" data-height="${box.height}"><span class="panel-frame__title">${title}</span></div>`

const rootWith = (frames: string): HTMLElement => {
  const root = document.createElement('div')
  root.id = 'dashboard-root'
  root.innerHTML = `<div class="dashboard-grid" data-width="1200" data-height="900">${frames}</div>`
  document.body.appendChild(root)
  return root
}

const panel = (top: number, left: number, height: number, width = 400): CapturedPanel => ({ top, left, width, height, image: 'data:,' })

describe('dashboardExport', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    document.body.innerHTML = ''
    Element.prototype.getBoundingClientRect = function () {
      return rectOf(this)
    }
    html2canvas.mockImplementation(async () => ({ toDataURL: () => 'data:image/png;base64,AAAA' }))
  })

  it('names the file after the export date and the dashboard', () => {
    expect(dashboardPdfFilename('Home', new Date(2026, 8, 7, 15, 4))).toBe('2026-09-07-Home.pdf')
    expect(dashboardPdfFilename('NOC / Wall 1', new Date(2026, 0, 31))).toBe('2026-01-31-NOC_Wall_1.pdf')
    expect(dashboardPdfFilename('', new Date(2026, 0, 31))).toBe('2026-01-31-Dashboard.pdf')
  })

  it('keeps panels whole across pages, moving a row that would be cut to the next page', () => {
    const pages = paginatePanels([panel(0, 0, 300), panel(0, 400, 300), panel(320, 0, 300), panel(640, 0, 200)], 650)
    expect(pages.map(p => p.panels.length)).toEqual([3, 1])
    expect(pages[1].originTop).toBe(640)
  })

  it('tells dark and light backgrounds apart for the header text', () => {
    expect(isDark('rgb(18, 24, 38)')).toBe(true)
    expect(isDark('rgb(244, 246, 248)')).toBe(false)
    expect(isDark('nonsense')).toBe(false)
  })

  it('captures every panel, lays pages out on the dashboard background and saves under the dated name', async () => {
    const root = rootWith(
      frame('Situations', { top: 0, left: 0, width: 600, height: 300 }) +
      frame('Outages', { top: 0, left: 600, width: 600, height: 300 }) +
      frame('Availability', { top: 320, left: 0, width: 1200, height: 500 })
    )
    const progress: [number, number][] = []
    const exported = await exportDashboardToPdf(root, 'Home', { when: new Date(2026, 8, 7), onProgress: (done, total) => progress.push([done, total]) })
    expect(exported).toBe(3)
    expect(progress).toEqual([[0, 3], [1, 3], [2, 3], [3, 3]])
    expect(html2canvas).toHaveBeenCalledTimes(3)
    expect(html2canvas.mock.calls[0][1]).toMatchObject({ scale: 2, useCORS: true })
    expect(pdf.rect).toHaveBeenCalledWith(0, 0, 842, 595, 'F')
    expect(pdf.addImage).toHaveBeenCalledTimes(3)
    // grid width 1200 scaled onto the 786pt content width: the right-hand panel starts at margin + 600 * 0.655
    const [, , x2] = pdf.addImage.mock.calls[1]
    expect(x2).toBeCloseTo(28 + 600 * (786 / 1200), 3)
    expect(pdf.text.mock.calls.map(c => c[0])).toContain('Home')
    expect(downloadBlob).toHaveBeenCalledWith(expect.any(Blob), '2026-09-07-Home.pdf')
  })

  it('skips a panel that cannot be rendered and saves the rest', async () => {
    html2canvas.mockImplementationOnce(async () => {
      throw new Error('tainted canvas')
    })
    const root = rootWith(
      frame('Map', { top: 0, left: 0, width: 600, height: 300 }) +
      frame('Outages', { top: 0, left: 600, width: 600, height: 300 })
    )
    expect(await exportDashboardToPdf(root, 'Home')).toBe(1)
    expect(pdf.addImage).toHaveBeenCalledTimes(1)
  })

  it('saves nothing when there are no panels', async () => {
    expect(await exportDashboardToPdf(rootWith(''), 'Home')).toBe(0)
    expect(downloadBlob).not.toHaveBeenCalled()
  })
})
