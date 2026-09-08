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

import { Chart } from 'chart.js'
import { jsPDF } from 'jspdf'

import useDownload from '@/composables/useDownload'
import { ConvertedGraphData, GraphMetricsResponse } from '@/types'

// A safe filename stem from a graph title/definition.
export const safeFileStem = (name: string): string =>
  (name || 'graph').replace(/[^\w.-]+/g, '_').replace(/^_+|_+$/g, '') || 'graph'

// Header cells come from graph config text, so neutralize spreadsheet
// formula-injection prefixes (=,+,-,@,tab,CR) before quoting. Value cells are
// numbers or ISO timestamps and are emitted raw (guarding them would corrupt
// legitimate negatives).
const escapeCsvHeader = (value: string): string => {
  const guarded = /^[=+\-@\t\r]/.test(value) ? `'${value}` : value
  return /[",\r\n]/.test(guarded) ? `"${guarded.replaceAll('"', '""')}"` : guarded
}

// One column per plotted metric, one row per timestamp. Times are emitted as ISO
// strings and values as raw numbers (gaps left blank) so the CSV is analysis-ready.
export const buildGraphCsv = (graphData: GraphMetricsResponse, converted: ConvertedGraphData): string => {
  const headerFor = (metricName: string): string => {
    const statement = converted.printStatements.find(s => s.metric === metricName)
    return statement?.header || metricName
  }
  const columnFor = (metricName: string): number[] => {
    const index = graphData.labels.indexOf(metricName)
    return index >= 0 ? (graphData.columns[index]?.values ?? []) : []
  }

  // Only export metrics that actually have a column in the response; transient or
  // CDEF metrics absent from labels would otherwise emit a header over blank cells.
  const columns = converted.metrics
    .filter(m => m.name && graphData.labels.includes(m.name as string))
    .map(m => ({ header: headerFor(m.name as string), values: columnFor(m.name as string) }))

  const lines: string[] = []
  lines.push(['Date/Time', ...columns.map(c => escapeCsvHeader(c.header))].join(','))

  graphData.timestamps.forEach((ts, i) => {
    const row = [Number.isFinite(ts) ? new Date(ts).toISOString() : '']
    for (const column of columns) {
      const value = column.values[i]
      row.push(value === null || value === undefined || Number.isNaN(value) ? '' : String(value))
    }
    lines.push(row.join(','))
  })

  return lines.join('\r\n')
}

export const downloadTextFile = (filename: string, text: string, mimeType: string): void => {
  // Leading BOM so Excel opens the UTF-8 CSV correctly; hand the blob to the
  // shared useDownload helper rather than rolling a separate anchor here.
  const blob = new Blob([`\uFEFF${text}`], { type: mimeType })
  useDownload().downloadBlob(blob, filename)
}

export const downloadGraphCsv = (
  graphData: GraphMetricsResponse,
  converted: ConvertedGraphData,
  fileStem: string
): void => {
  downloadTextFile(`${safeFileStem(fileStem)}.csv`, buildGraphCsv(graphData, converted), 'text/csv;charset=utf-8')
}

// Chart.js canvases are transparent; flatten onto white so the PDF image is not
// rendered over a black background.
const canvasToPngOnWhite = (canvas: HTMLCanvasElement): string => {
  const flattened = document.createElement('canvas')
  flattened.width = canvas.width
  flattened.height = canvas.height
  const ctx = flattened.getContext('2d')
  if (!ctx) {
    return canvas.toDataURL('image/png')
  }
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, flattened.width, flattened.height)
  ctx.drawImage(canvas, 0, 0)
  return flattened.toDataURL('image/png')
}

// Render each live chart inside `container` into a single landscape PDF. The chart
// image already carries the graph title (Chart.js bakes it in), so only the legend
// stats are added as text. Returns the number of graphs exported (0 = nothing
// saved). Note: only DOM-mounted graphs are captured — with infinite scroll the
// caller may not have loaded them all.
export const exportGraphsToPdf = (container: HTMLElement, docTitle: string): number => {
  const canvases = (Array.from(container.querySelectorAll('canvas')) as HTMLCanvasElement[])
    .filter(c => Chart.getChart(c))
  if (!canvases.length) {
    return 0
  }

  const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' })
  const pageWidth = doc.internal.pageSize.getWidth()
  const pageHeight = doc.internal.pageSize.getHeight()
  const margin = 40
  const contentWidth = pageWidth - margin * 2
  let y = margin
  let exported = 0

  doc.setFontSize(15)
  doc.text(docTitle, margin, y)
  y += 24

  for (const canvas of canvases) {
    try {
      const legend = (document.getElementById(`${canvas.id}-lc`)?.innerText ?? '').trim()
      const legendLines = legend ? (doc.splitTextToSize(legend, contentWidth) as string[]) : []
      const legendHeight = legendLines.length * 12

      // preserve aspect ratio, but never let a single graph exceed one page
      const ratio = canvas.height / canvas.width || 0.4
      let imgWidth = contentWidth
      let imgHeight = imgWidth * ratio
      const maxImgHeight = pageHeight - margin * 2 - legendHeight - 12
      if (imgHeight > maxImgHeight) {
        imgHeight = Math.max(60, maxImgHeight)
        imgWidth = Math.min(contentWidth, imgHeight / ratio)
      }

      // new page if the block doesn't fit in what's left (unless already at the top)
      if (y + imgHeight + legendHeight + 12 > pageHeight - margin && y > margin) {
        doc.addPage()
        y = margin
      }

      doc.addImage(canvasToPngOnWhite(canvas), 'PNG', margin, y, imgWidth, imgHeight)
      y += imgHeight + 6
      if (legendLines.length) {
        doc.setFontSize(9)
        doc.text(legendLines, margin, y)
        y += legendHeight
      }
      y += 12
      exported++
    } catch {
      // a tainted or over-large canvas shouldn't abort the whole export
      continue
    }
  }

  if (!exported) {
    return 0
  }
  useDownload().downloadBlob(doc.output('blob'), `${safeFileStem(docTitle)}.pdf`)
  return exported
}
