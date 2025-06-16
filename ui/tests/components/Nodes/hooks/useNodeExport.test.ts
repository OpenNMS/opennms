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

import { describe, expect, test, vi } from 'vitest'
import { mock } from 'vitest-mock-extended'
import getExportDataResponseJson from './data/getExportDataResponse.json'
import getExportDataResponseCsv from './data/getExportDataResponseCSV.json'
import { useNodeExport } from '@/components/Nodes/hooks/useNodeExport'
import { Node, NodeApiResponse, NodeColumnSelectionItem, QueryParameters } from '@/types'

const { generateBlob, getExportData } = useNodeExport()

const defaultColumns: NodeColumnSelectionItem[] = [
  { id: 'id', label: 'ID', selected: true, order: 0 },
  { id: 'label', label: 'Node Label', selected: true, order: 1 },
  { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
  { id: 'location', label: 'Location', selected: true, order: 3 },
  { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
  { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
  { id: 'sysContact', label: 'Sys Contact', selected: true, order: 6 },
  { id: 'sysLocation', label: 'Sys Location', selected: true, order: 7 },
  { id: 'sysDescription', label: 'Sys Description', selected: true, order: 8 },
  { id: 'flows', label: 'Flows', selected: true, order: 9 }
]

const createNodeResponse = () => {
  const node1 = mock<Node>()
  node1.id = '1'
  node1.label = 'Node1';
  (node1 as any)['ipaddress'] = '192.168.0.1'
  node1.location = 'Location 0'
  node1.foreignSource = 'selfmonitor'
  node1.foreignId = '1'
  node1.sysContact = 'Administrator <postmaster@example.com>'
  node1.sysLocation = 'Right here, right now'
  node1.sysDescription = 'Darwin REMMUSERNAME 22.6.0 Darwin Kernel Version 22.6.0:'
  node1.lastIngressFlow = 1690316403504
  node1.lastEgressFlow = 1690316403504

  const node2 = mock<Node>()
  node2.id = '106'
  node2.label = 'WI_racine-store-pos';
  (node2 as any)['ipaddress'] = '192.168.99.106'
  node2.location = 'Default'
  node2.foreignSource = 'Demo_Stores'
  node2.foreignId = 'WI_racine-store-pos'
  node2.sysContact = 'Administrator <racineadmin@example.com>'
  node2.sysLocation = 'Racine, WI'
  node2.sysDescription = 'Alpine Linux'
  node2.lastIngressFlow = 0
  node2.lastEgressFlow = 0

  const response = {
    count: 2,
    offset: 0,
    totalCount: 2,
    node: [node1, node2]
  } as NodeApiResponse

  return response
}

vi.mock('@/services/nodeService', () => ({
  getNodes: vi.fn(() => createNodeResponse())
}))

describe('Nodes useNodeExport test', () => {
  test('test generateBlob', () => {
    const result = generateBlob('some content', 'text/html')

    expect(result).toBeInstanceOf(Blob)
    expect(result.size).toBe(12)
    expect(result.type).toBe('text/html')
  })

  test('test getExportData JSON', async () => {
    const queryParams = mock<QueryParameters>()
    const exportedData = await getExportData('json', queryParams, defaultColumns)

    expect(exportedData).not.toBeNull()

    const expected = JSON.stringify(getExportDataResponseJson, undefined, 2)

    expect(exportedData).toEqual(expected)
  })

  test('test getExportData CSV', async () => {
    const queryParams = mock<QueryParameters>()
    const exportedData = await getExportData('csv', queryParams, defaultColumns)

    expect(exportedData).not.toBeNull()

    const expected = getExportDataResponseCsv.csv.join('\n')

    expect(exportedData).toEqual(expected)
  })
})
