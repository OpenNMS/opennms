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
import { mount } from '@vue/test-utils'
import GraphCollectionGrid from '@/components/GraphCollections/GraphCollectionGrid.vue'
import { formatGraphInstant, graphTimeFor, spaRouteForLegacyLink } from '@/components/GraphCollections/graphCollectionTime'
import { ResolvedGraphCollection, ResolvedGraphCollectionGraph } from '@/types/graphCollection'

const HOUR = 3600 * 1000
const DAY = 24 * HOUR
const END = Date.UTC(2026, 9, 16, 12, 0, 0)

const node = { id: 'node[1]', label: 'rtr-01', typeName: 'node', typeLabel: 'Node', link: 'element/node.jsp?node=1' }

const graph = (index: number, over: Partial<ResolvedGraphCollectionGraph> = {}): ResolvedGraphCollectionGraph => ({
  index,
  kind: 'prefab',
  title: `Graph ${index}`,
  timespan: '7_day',
  graphtype: 'mib2.bits',
  resourceId: 'node[1].interfaceSnmp[ge-0_0_0]',
  resource: {
    id: 'node[1].interfaceSnmp[ge-0_0_0]',
    label: 'ge-0/0/0',
    typeName: 'interfaceSnmp',
    typeLabel: 'SNMP Interface Data',
    link: 'element/snmpinterface.jsp?node=1&ifindex=2',
    parent: node
  },
  prefabGraphTitle: 'Bits In/Out',
  valid: true,
  error: null,
  start: END - 7 * DAY,
  end: END,
  ...over
})

const collection = (graphs: ResolvedGraphCollectionGraph[], graphsPerLine = 2): ResolvedGraphCollection => ({
  id: 3,
  type: 'custom',
  title: 'Core',
  showTimespanButton: false,
  showGraphtypeButton: false,
  graphsPerLine,
  graphTypes: ['mib2.bits'],
  graphs
})

// the real Graph draws on a canvas and fetches metrics; record what it was handed
const GraphStub = {
  props: ['canvasId', 'definition', 'resourceId', 'time', 'label', 'isSingleGraph'],
  template: '<div data-test="graph-stub" :data-canvas="canvasId" :data-start="time.startTime" :data-format="time.format" />'
}

// the ad-hoc cell owns a store and a query pipeline; record what it was handed
const AdhocStub = {
  props: ['canvasId', 'adhocState', 'start', 'end'],
  template: '<div data-test="adhoc-stub" :data-canvas="canvasId" :data-state="adhocState" :data-start="start" />'
}

const mountGrid = (value: ResolvedGraphCollection) => mount(GraphCollectionGrid, {
  props: { collection: value },
  global: {
    stubs: { Graph: GraphStub, AdhocCollectionGraph: AdhocStub, 'router-link': { props: ['to'], template: '<a :href="JSON.stringify(to)"><slot /></a>' }}
  }
})

const STATE = 'nf=rtr&df=ifHCInOctets&n=2&title=Uplinks'
const adhoc = (index: number, over: Partial<ResolvedGraphCollectionGraph> = {}): ResolvedGraphCollectionGraph => ({
  index, kind: 'adhoc', title: '', timespan: '1_day', graphtype: null, resourceId: null, resource: null,
  adhocState: STATE, valid: true, error: null, start: END - DAY, end: END, ...over
})

describe('GraphCollectionGrid.vue', () => {
  it('lays the graphs out graphsPerLine to a row', () => {
    const wrapper = mountGrid(collection([graph(0), graph(1), graph(2)], 3))
    expect(wrapper.find('[data-test="gc-grid"]').attributes('style')).toContain('repeat(3, minmax(0, 1fr))')
    expect(wrapper.findAll('[data-test="gc-cell"]')).toHaveLength(3)
  })

  it('gives two drawings of the same graph for the same resource distinct canvas ids', () => {
    const wrapper = mountGrid(collection([graph(0), graph(1, { start: END - DAY, timespan: '1_day' })]))
    const canvases = wrapper.findAll('[data-test="graph-stub"]').map(stub => stub.attributes('data-canvas'))
    expect(new Set(canvases).size).toBe(2)
    expect(canvases.every(id => id?.startsWith('gc-3-'))).toBe(true)
  })

  it('hands the graph its window in seconds with a granularity fitting the span', () => {
    const wrapper = mountGrid(collection([graph(0)]))
    const stub = wrapper.find('[data-test="graph-stub"]')
    expect(stub.attributes('data-start')).toBe(String((END - 7 * DAY) / 1000))
    expect(stub.attributes('data-format')).toBe('days')
    expect(wrapper.find('[data-test="gc-window"]').text()).toBe(
      `From ${formatGraphInstant(END - 7 * DAY)} to ${formatGraphInstant(END)}`
    )
  })

  it('shows the server\'s reason instead of a chart for a graph that cannot be drawn', () => {
    const broken = graph(1, { valid: false, error: 'Resource node[9].interfaceSnmp[x] no longer exists.', resource: null })
    const wrapper = mountGrid(collection([graph(0), broken]))
    expect(wrapper.findAll('[data-test="graph-stub"]')).toHaveLength(1)
    const placeholder = wrapper.find('[data-test="gc-placeholder"]')
    expect(placeholder.text()).toContain('This graph cannot be drawn.')
    expect(placeholder.text()).toContain('no longer exists')
    // the broken cell keeps its own heading so the user can tell which entry it is
    expect(wrapper.findAll('[data-test="gc-title"]')[1].text()).toBe('Graph 1')
    expect(wrapper.findAll('[data-test="gc-detail"]')).toHaveLength(1)
  })

  it('links the parent node to the node page and the detail link to the single graph, with encoded segments', () => {
    const wrapper = mountGrid(collection([graph(0)]))
    expect(wrapper.find('[data-test="gc-parent"]').attributes('href')).toBe('"/node/1"')
    expect(wrapper.find('[data-test="gc-resource"]').attributes('href')).toBe('/opennms/element/snmpinterface.jsp?node=1&ifindex=2')
    const detail = JSON.parse(wrapper.find('[data-test="gc-detail"]').attributes('href') as string)
    expect(detail.path).toBe('/resource-graphs/graphs/ge-0%2F0%2F0/mib2.bits/node%5B1%5D.interfaceSnmp%5Bge-0_0_0%5D')
  })

  it('falls back to the prefabricated title when the entry has none', () => {
    const wrapper = mountGrid(collection([graph(0, { title: '' })]))
    expect(wrapper.find('[data-test="gc-title"]').text()).toBe('Bits In/Out')
  })

  it('draws an ad-hoc entry from its stored state over the window, with a link into the builder on that window', () => {
    const wrapper = mountGrid(collection([graph(0), adhoc(1)]))
    const stub = wrapper.find('[data-test="adhoc-stub"]')
    expect(stub.attributes('data-state')).toBe(STATE)
    expect(stub.attributes('data-start')).toBe(String(END - DAY))
    expect(stub.attributes('data-canvas')).toBe(wrapper.findAll('[data-test="gc-cell"]')[1].find('[data-test="adhoc-stub"]').attributes('data-canvas'))
    // one prefab graph, one ad-hoc graph, and neither cell draws the other kind
    expect(wrapper.findAll('[data-test="graph-stub"]')).toHaveLength(1)
    // the entry has no title of its own, so the one the builder stored is used
    expect(wrapper.findAll('[data-test="gc-title"]')[1].text()).toBe('Uplinks')
    expect(wrapper.findAll('[data-test="gc-detail"]')).toHaveLength(1)
    const builder = JSON.parse(wrapper.find('[data-test="gc-builder"]').attributes('href') as string)
    expect(builder.path).toBe('/adhoc-graphs')
    expect(builder.query).toMatchObject({ nf: 'rtr', df: 'ifHCInOctets', n: '2', title: 'Uplinks', start: String((END - DAY) / 1000), end: String(END / 1000), fmt: 'hours' })
  })

  it('keeps an ad-hoc cell, and its store, when the window moves, and only hands it the new window', async () => {
    const wrapper = mountGrid(collection([graph(0), adhoc(1)]))
    const before = wrapper.findComponent(AdhocStub)
    const canvas = before.attributes('data-canvas')
    await wrapper.setProps({ collection: collection([graph(0, { start: END - 2 * DAY }), adhoc(1, { start: END - 2 * DAY })]) })
    const after = wrapper.findComponent(AdhocStub)
    expect(after.vm).toBe(before.vm)
    expect(after.attributes('data-canvas')).toBe(canvas)
    expect(after.attributes('data-start')).toBe(String(END - 2 * DAY))
  })

  it('remakes an ad-hoc cell when a refresh brings a different definition to its index', async () => {
    const wrapper = mountGrid(collection([adhoc(0)]))
    const before = wrapper.findComponent(AdhocStub)
    // the entry was edited, or another one now sits at this index: the old sources must not be drawn under the new title
    await wrapper.setProps({ collection: collection([adhoc(0, { adhocState: 'nf=core&df=ifHCOutOctets&n=1&title=Downlinks' })]) })
    const after = wrapper.findComponent(AdhocStub)
    expect(after.vm).not.toBe(before.vm)
    expect(after.attributes('data-state')).toBe('nf=core&df=ifHCOutOctets&n=1&title=Downlinks')
    expect(wrapper.find('[data-test="gc-title"]').text()).toBe('Downlinks')
  })

  it('shows the placeholder for an ad-hoc entry the server flags, without a chart', () => {
    const wrapper = mountGrid(collection([adhoc(0, { valid: false, error: 'Unknown timespan \'fortnight\'.' })]))
    expect(wrapper.find('[data-test="adhoc-stub"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="gc-placeholder"]').text()).toContain('fortnight')
    expect(wrapper.find('[data-test="gc-title"]').text()).toBe('Uplinks')
  })
})

describe('graphCollectionTime', () => {
  it('picks the axis granularity from the span', () => {
    expect(graphTimeFor(END - HOUR, END).format).toBe('minutes')
    expect(graphTimeFor(END - 8 * HOUR, END).format).toBe('hours')
    expect(graphTimeFor(END - 7 * DAY, END).format).toBe('days')
    expect(graphTimeFor(END - 365 * DAY, END).format).toBe('months')
    expect(graphTimeFor(END - HOUR, END)).toMatchObject({ startTime: (END - HOUR) / 1000, endTime: END / 1000 })
  })

  it('maps only the node page onto an SPA route', () => {
    expect(spaRouteForLegacyLink('element/node.jsp?node=42')).toBe('/node/42')
    expect(spaRouteForLegacyLink('element/snmpinterface.jsp?node=1&ifindex=2')).toBeNull()
  })
})
