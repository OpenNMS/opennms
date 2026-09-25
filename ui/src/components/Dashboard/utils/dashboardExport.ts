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

import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

import useDownload from '@/composables/useDownload'
import { safeFileStem } from '@/components/Resources/utils/graphExport'

// A panel as captured from the screen, with its position relative to the grid
// so the PDF keeps the dashboard's arrangement.
export interface CapturedPanel {
  left: number
  top: number
  width: number
  height: number
  image: string
}

export interface PdfPage {
  panels: CapturedPanel[]
  // grid-space offset of the page's top edge; panels are drawn relative to it
  originTop: number
}

// <date>-<dashboard name>.pdf, e.g. 2026-09-07-Home.pdf
export const dashboardPdfFilename = (dashboardName: string, when: Date = new Date()): string => {
  const y = when.getFullYear()
  const m = String(when.getMonth() + 1).padStart(2, '0')
  const d = String(when.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}-${safeFileStem(dashboardName || 'Dashboard')}.pdf`
}

// Pack panels onto pages of the given height (in grid pixels), keeping each
// panel whole: a panel that would cross the page bottom starts the next page,
// and everything at or below it moves with it.
export const paginatePanels = (panels: CapturedPanel[], pageHeight: number): PdfPage[] => {
  const ordered = [...panels].sort((a, b) => a.top - b.top || a.left - b.left)
  const pages: PdfPage[] = []
  let page: PdfPage | null = null
  for (const panel of ordered) {
    const fits = page !== null && panel.top + panel.height - page.originTop <= pageHeight
    if (!page || !fits) {
      page = { panels: [], originTop: panel.top }
      pages.push(page)
    }
    page.panels.push(panel)
  }
  return pages
}

// Header text must stay readable on the captured background.
export const isDark = (color: string): boolean => {
  const m = color.match(/\d+(\.\d+)?/g)
  if (!m || m.length < 3) {
    return false
  }
  const [r, g, b] = m.map(Number)
  return (0.2126 * r + 0.7152 * g + 0.0722 * b) < 128
}

const cssColor = (el: HTMLElement): string | null => {
  const color = getComputedStyle(el).backgroundColor
  return color && color !== 'rgba(0, 0, 0, 0)' && color !== 'transparent' ? color : null
}

const captureFrame = async (frame: HTMLElement, gridRect: DOMRect, background: string): Promise<CapturedPanel | null> => {
  const rect = frame.getBoundingClientRect()
  if (!rect.width || !rect.height) {
    return null
  }
  try {
    const canvas = await html2canvas(frame, { scale: 2, useCORS: true, logging: false, backgroundColor: background })
    return {
      left: rect.left - gridRect.left,
      top: rect.top - gridRect.top,
      width: rect.width,
      height: rect.height,
      image: canvas.toDataURL('image/png')
    }
  } catch {
    // one panel that cannot be rendered (for example a tainted canvas) must not
    // take the whole export down
    return null
  }
}

const PAGE_MARGIN = 28
const HEADER_HEIGHT = 34

export interface ExportOptions {
  when?: Date
  // called after each panel, so the caller can show "n of m"
  onProgress?: (done: number, total: number) => void
}

// Let the browser paint (progress text, spinner) between CPU-bound captures.
const nextFrame = (): Promise<void> => new Promise((resolve) => {
  if (typeof requestAnimationFrame === 'function') {
    requestAnimationFrame(() => resolve())
  } else {
    setTimeout(resolve, 0)
  }
})

// Landscape A4 on the dashboard's own background, panels placed where they are
// on screen and scaled so the grid's width fills the page. Resolves to the
// number of panels exported (0 = nothing rendered, no file saved).
export const exportDashboardToPdf = async (root: HTMLElement, dashboardName: string, options: ExportOptions = {}): Promise<number> => {
  const when = options.when ?? new Date()
  const grid = (root.querySelector('.dashboard-grid') as HTMLElement | null) ?? root
  const frames = Array.from(root.querySelectorAll('.panel-frame')) as HTMLElement[]
  if (!frames.length) {
    return 0
  }
  const gridRect = grid.getBoundingClientRect()
  const background = cssColor(grid) ?? cssColor(root) ?? cssColor(document.body) ?? '#ffffff'
  options.onProgress?.(0, frames.length)
  await nextFrame()
  const captured: CapturedPanel[] = []
  for (const [index, frame] of frames.entries()) {
    const panel = await captureFrame(frame, gridRect, background)
    if (panel) {
      captured.push(panel)
    }
    options.onProgress?.(index + 1, frames.length)
    await nextFrame()
  }
  if (!captured.length) {
    return 0
  }

  const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' })
  const pageWidth = doc.internal.pageSize.getWidth()
  const pageHeight = doc.internal.pageSize.getHeight()
  const contentWidth = pageWidth - PAGE_MARGIN * 2
  const contentTop = PAGE_MARGIN + HEADER_HEIGHT
  const contentHeight = pageHeight - contentTop - PAGE_MARGIN
  const gridWidth = Math.max(gridRect.width, ...captured.map(p => p.left + p.width))
  const scale = contentWidth / gridWidth
  const pages = paginatePanels(captured, contentHeight / scale)

  const textColor = isDark(background) ? '#e6edf3' : '#1f2937'
  pages.forEach((page, index) => {
    if (index > 0) {
      doc.addPage()
    }
    doc.setFillColor(background)
    doc.rect(0, 0, pageWidth, pageHeight, 'F')
    doc.setTextColor(textColor)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(14)
    doc.text(dashboardName, PAGE_MARGIN, PAGE_MARGIN + 12)
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(9)
    doc.text(`Exported ${when.toLocaleString()}`, PAGE_MARGIN, PAGE_MARGIN + 26)
    doc.text(`Page ${index + 1} of ${pages.length}`, pageWidth - PAGE_MARGIN, PAGE_MARGIN + 12, { align: 'right' })
    for (const panel of page.panels) {
      const x = PAGE_MARGIN + panel.left * scale
      const y = contentTop + (panel.top - page.originTop) * scale
      const w = panel.width * scale
      // a single panel taller than a page is scaled down to fit rather than cut
      const h = Math.min(panel.height * scale, contentHeight)
      doc.addImage(panel.image, 'PNG', x, y, w, h)
    }
  })

  useDownload().downloadBlob(doc.output('blob'), dashboardPdfFilename(dashboardName, when))
  return captured.length
}
