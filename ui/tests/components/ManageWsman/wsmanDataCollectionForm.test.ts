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

import { describe, expect, it } from 'vitest'
import { fileInput, isRra, nameTaken, remove, upsert } from '@/components/ManageWsman/wsmanDataCollectionForm'

const DC = {
  rrdRepository: '/rrd',
  sources: ['wsman-datacollection-config.xml', 'dell-idrac.xml'],
  versions: { 'wsman-datacollection-config.xml': 'root-v', 'dell-idrac.xml': 'drac-v' },
  collections: [{ name: 'default', source: 'wsman-datacollection-config.xml', rrdStep: 300, rras: ['RRA:AVERAGE:0.5:1:2016'], includeAllSystemDefinitions: true, includedSystemDefinitions: [] }],
  groups: [{ name: 'drac-system', source: 'dell-idrac.xml', resourceType: 'node', resourceUri: 'uri', dialect: null, filter: null, attributes: [{ name: 'A', alias: 'a', type: 'gauge', indexOf: null, filter: null }] }],
  systemDefinitions: [{ name: 'Dell iDRAC 8', source: 'dell-idrac.xml', rules: ['true'], includedGroups: ['drac-system'] }]
}

describe('wsmanDataCollectionForm', () => {
  it('rebuilds one file from the flattened view with its version, keeping the repository on the root only', () => {
    const root = fileInput(DC, 'wsman-datacollection-config.xml')
    expect(root.version).toBe('root-v')
    expect(root.rrdRepository).toBe('/rrd')
    expect(root.collections.map(c => c.name)).toEqual(['default'])
    expect(root.groups).toEqual([])
    const drac = fileInput(DC, 'dell-idrac.xml')
    expect(drac.rrdRepository).toBeNull()
    expect(drac.groups.map(g => g.name)).toEqual(['drac-system'])
    expect(drac.systemDefinitions[0].includedGroups).toEqual(['drac-system'])
    // a file that does not exist yet has no version, which makes the server create it
    expect(fileInput(DC, 'custom.xml').version).toBeNull()
  })

  it('upserts by original name and removes by name', () => {
    const list = [{ name: 'a' }, { name: 'b' }]
    expect(upsert(list, 'a', { name: 'renamed' }).map(x => x.name)).toEqual(['renamed', 'b'])
    expect(upsert(list, null, { name: 'c' }).map(x => x.name)).toEqual(['a', 'b', 'c'])
    expect(remove(list, 'b').map(x => x.name)).toEqual(['a'])
  })

  it('checks names across every file and validates RRAs', () => {
    expect(nameTaken(DC, 'group', 'drac-system', null)).toBe(true)
    expect(nameTaken(DC, 'group', 'drac-system', 'drac-system')).toBe(false)
    expect(nameTaken(DC, 'collection', 'new', null)).toBe(false)
    expect(isRra('RRA:AVERAGE:0.5:1:2016')).toBe(true)
    expect(isRra('RRA:MAX:0.5:288:366')).toBe(true)
    expect(isRra('bogus')).toBe(false)
  })
})
