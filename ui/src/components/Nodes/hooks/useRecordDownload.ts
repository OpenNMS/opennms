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

import { ServiceType } from '@/types'
import { useNodeExport } from './useNodeExport'

export type RecordDownloadFormat = 'csv' | 'json'

/**
 * The CSV columns are whatever fields the records actually carry, in the order the API returns
 * them. Taken from the data rather than from a type, since the API sends fields the types do not
 * all declare (Event.serviceType among them).
 */
const getCsvColumns = (records: Record<string, unknown>[]) => {
  const columns: string[] = []

  for (const record of records) {
    for (const field of Object.keys(record)) {
      if (!columns.includes(field)) {
        columns.push(field)
      }
    }
  }

  return columns
}

/**
 * serviceType arrives as an object; its name is the part worth a CSV cell. Anything else
 * non-primitive keeps its JSON form rather than stringifying to '[object Object]'.
 */
const flattenCsvValue = (field: string, value: unknown) => {
  if (value === null || value === undefined) {
    return ''
  }

  if (field === 'serviceType') {
    return (value as ServiceType).name ?? ''
  }

  return typeof value === 'object' ? JSON.stringify(value) : String(value)
}

/**
 * Quote a field only when it needs it, doubling any embedded quote, per RFC 4180. Event log
 * messages carry markup and commas, so unquoted values would break the row apart.
 */
const toCsvValue = (value: string) =>
  /[",\r\n]/.test(value) ? `"${value.replace(/"/g, '""')}"` : value

const buildCsv = (records: Record<string, unknown>[]) => {
  const columns = getCsvColumns(records)

  return [
    columns.join(','),
    ...records.map(record => columns.map(field => toCsvValue(flattenCsvValue(field, record[field]))).join(','))
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
    // Read the records as plain field bags: the export covers whatever the API sent, including
    // the fields the record types do not declare.
    const rows = records as Record<string, unknown>[]

    const contentType = format === 'json' ? 'application/json' : 'text/csv'
    const data = format === 'json' ? buildJson(records) : buildCsv(rows)

    generateDownload(generateBlob(data, contentType), `${baseName}.${format}`)
  }

  return {
    downloadRecords
  }
}
