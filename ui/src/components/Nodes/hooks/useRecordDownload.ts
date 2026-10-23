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

import { useNodeExport } from './useNodeExport'

export type RecordDownloadFormat = 'csv' | 'json'

/**
 * Flatten a record into dotted leaf paths: `monitoredService.serviceType.name` rather than a
 * JSON blob in one cell. Arrays stay whole -- their length varies per row, so they cannot be
 * columns -- and are emitted as JSON.
 */
const toCsvLeaves = (value: unknown, prefix = ''): Array<[string, unknown]> => {
  if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
    return Object.entries(value as Record<string, unknown>)
      .flatMap(([key, child]) => toCsvLeaves(child, prefix ? `${prefix}.${key}` : key))
  }

  return [[prefix, value]]
}

/**
 * The CSV columns are whatever leaves the records actually carry, in the order the API returns
 * them. Taken from the data rather than from a type, since the API sends fields the types do
 * not all declare.
 */
const getCsvColumns = (rows: Array<Map<string, unknown>>) => {
  const columns: string[] = []

  for (const row of rows) {
    for (const field of row.keys()) {
      if (!columns.includes(field)) {
        columns.push(field)
      }
    }
  }

  return columns
}

/**
 * Quote a field only when it needs it, doubling any embedded quote, per RFC 4180. Event log
 * messages carry markup and commas, so unquoted values would break the row apart.
 *
 * A leading = + - @ tab or CR is also neutralised with a single quote: this data comes from
 * traps and syslog, and a spreadsheet would otherwise run it as a formula. Numbers are left
 * alone -- they cannot be formulas, and guarding them would corrupt a legitimate negative.
 * Same treatment the notices table and the resource-graph export already apply.
 */
const toCsvValue = (value: unknown) => {
  if (value === null || value === undefined) {
    return ''
  }

  const raw = typeof value === 'object' ? JSON.stringify(value) : String(value)
  const guarded = typeof value !== 'number' && /^[=+\-@\t\r]/.test(raw) ? `'${raw}` : raw

  return /[",\r\n]/.test(guarded) ? `"${guarded.replace(/"/g, '""')}"` : guarded
}

const buildCsv = (records: unknown[]) => {
  const rows = records.map(record => new Map(toCsvLeaves(record)))
  const columns = getCsvColumns(rows)

  return [
    columns.join(','),
    ...rows.map(row => columns.map(field => toCsvValue(row.get(field))).join(','))
  ].join('\n')
}

const buildJson = (records: unknown[]) => JSON.stringify(records, null, 2)

/**
 * Download a set of API records as a CSV or JSON file, covering every field they carry rather
 * than only the columns a table displays. Shared by the Node Details panels.
 */
export const useRecordDownload = () => {
  const { generateBlob, generateDownload } = useNodeExport()

  const downloadRecords = (records: unknown[], baseName: string, format: RecordDownloadFormat) => {
    const contentType = format === 'json' ? 'application/json' : 'text/csv'
    const data = format === 'json' ? buildJson(records) : buildCsv(records)

    generateDownload(generateBlob(data, contentType), `${baseName}.${format}`)
  }

  return {
    downloadRecords
  }
}
