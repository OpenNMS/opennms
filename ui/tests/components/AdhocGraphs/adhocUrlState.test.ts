import { describe, expect, it } from 'vitest'

import { DEFAULT_RESOLUTION } from '@/components/AdhocGraphs/utils/adhocQuery'
import {
  AdhocLinkState,
  decodeAdhocState,
  encodeAdhocState,
  encodedQueryLength,
  MAX_QUERY_LENGTH,
  RouteQuery
} from '@/components/AdhocGraphs/utils/adhocUrlState'
import { StartEndTime } from '@/types'
import { AdhocGraphConfig } from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'

const time: StartEndTime = { startTime: 1704067200, endTime: 1704070800, format: 'hours' }

const KEY_IN = 'node[1].interfaceSnmp[eth0]|ifHCInOctets'
const KEY_OUT = 'node[FS:A].interfaceSnmp[eth1]|ifHCOutOctets'

/** Series are generated, so the config carries none; the link carries the picker and the edits. */
const fullConfig: AdhocGraphConfig = {
  series: [],
  expressions: [
    { id: 'expr-1', label: 'total_bits', value: '(in_octets + out_octets) * 8', color: '#1baf7a', style: 'stack' }
  ],
  title: 'WAN traffic',
  verticalLabel: 'bits/sec',
  stacked: true,
  resolution: 800
}

const fullLink: AdhocLinkState = {
  selection: {
    nodeFilter: 'catincRouters & location=\'Default\'',
    resourceFilter: 'interfaceSnmp[eth*]',
    datasourceFilter: 'ifHC*Octets',
    pickedNodeIds: ['1', 'FS:A'],
    pickedResourceIds: ['node[1].interfaceSnmp[eth0]', 'node[FS:A].interfaceSnmp[eth1]'],
    pickedDatasourceKeys: [KEY_IN, KEY_OUT]
  },
  overrides: {
    [KEY_IN]: { label: 'in_octets', aggregation: ConsolidationFunctionType.MAX, style: 'area', color: '#2a78d6', hidden: true },
    [KEY_OUT]: { color: '#eb6834' }
  },
  seriesCount: 2
}

const emptyLink: AdhocLinkState = {
  selection: {
    nodeFilter: '',
    resourceFilter: '',
    datasourceFilter: '',
    pickedNodeIds: [],
    pickedResourceIds: [],
    pickedDatasourceKeys: []
  },
  overrides: {},
  seriesCount: 0
}

describe('encodeAdhocState / decodeAdhocState', () => {
  it('round-trips a complete graph', () => {
    const restored = decodeAdhocState(encodeAdhocState(fullConfig, time, fullLink))

    expect(restored).not.toBeNull()
    expect(restored?.time).toEqual(time)
    expect(restored?.config.title).toBe('WAN traffic')
    expect(restored?.config.verticalLabel).toBe('bits/sec')
    expect(restored?.config.stacked).toBe(true)
    expect(restored?.config.resolution).toBe(800)
    expect(restored?.config.series).toEqual([])
    // The id is regenerated on decode; everything else survives.
    expect(restored?.config.expressions).toEqual([{ ...fullConfig.expressions[0], id: 'expr-0' }])
    expect(restored?.link).toEqual(fullLink)
  })

  it('writes the picker under short keys so a link stays readable', () => {
    const query = encodeAdhocState(fullConfig, time, fullLink)

    expect(query.nf).toBe('catincRouters & location=\'Default\'')
    expect(query.rf).toBe('interfaceSnmp[eth*]')
    expect(query.df).toBe('ifHC*Octets')
    expect(query.pn).toEqual(['1', 'FS:A'])
    expect(query.pr).toEqual(fullLink.selection.pickedResourceIds)
    expect(query.pd).toEqual([KEY_IN, KEY_OUT])
    expect(query.n).toBe('2')
    expect(query.s).toBeUndefined()
  })

  it('writes only the fields of an override that were set', () => {
    const query = encodeAdhocState(fullConfig, time, fullLink)

    expect(query.o).toEqual([
      'node[1].interfaceSnmp[eth0]~ifHCInOctets~MAX~in_octets~area~#2a78d6~1',
      'node[FS:A].interfaceSnmp[eth1]~ifHCOutOctets~~~~#eb6834~'
    ])
  })

  it('omits defaulted and empty fields so a simple graph gets a short link', () => {
    const query = encodeAdhocState(
      { ...fullConfig, expressions: [], title: '', verticalLabel: '', stacked: false, resolution: DEFAULT_RESOLUTION },
      time,
      { ...emptyLink, overrides: { [KEY_IN]: {}}}
    )

    expect(Object.keys(query).sort()).toEqual(['end', 'fmt', 'start'])
  })

  it('returns null when the query carries no ad-hoc state', () => {
    expect(decodeAdhocState({})).toBeNull()
    expect(decodeAdhocState({ unrelated: 'value' })).toBeNull()
  })

  it('counts a filter alone as state worth restoring', () => {
    const restored = decodeAdhocState({ nf: 'catincRouters' })

    expect(restored).not.toBeNull()
    expect(restored?.link.selection.nodeFilter).toBe('catincRouters')
    expect(restored?.link.seriesCount).toBe(0)
  })

  it('accepts a single pick that vue-router hands over as a bare string', () => {
    const restored = decodeAdhocState({ pd: KEY_IN, start: '1704067200' })

    expect(restored?.link.selection.pickedDatasourceKeys).toEqual([KEY_IN])
  })
})

// Links made before filters existed carry each series as an `s` entry. They have
// to keep drawing exactly the graph they were made for.
describe('legacy static links', () => {
  const entry = 'node[1].interfaceSnmp[eth0]~ifHCInOctets~MAX~in_octets~area~#2a78d6~1'

  it('become picks at every level plus a full override', () => {
    const restored = decodeAdhocState({ s: [entry, 'nodeSource[FS:A].interfaceSnmp[eth1]~ifHCOutOctets~AVERAGE~out~line~#eb6834~0'] })

    expect(restored?.link.selection).toEqual({
      nodeFilter: '',
      resourceFilter: '',
      datasourceFilter: '',
      pickedNodeIds: ['1', 'FS:A'],
      pickedResourceIds: ['node[1].interfaceSnmp[eth0]', 'nodeSource[FS:A].interfaceSnmp[eth1]'],
      pickedDatasourceKeys: [KEY_IN, 'nodeSource[FS:A].interfaceSnmp[eth1]|ifHCOutOctets']
    })
    expect(restored?.link.overrides[KEY_IN]).toEqual({
      label: 'in_octets',
      aggregation: ConsolidationFunctionType.MAX,
      style: 'area',
      color: '#2a78d6',
      hidden: true
    })
    expect(restored?.link.overrides['nodeSource[FS:A].interfaceSnmp[eth1]|ifHCOutOctets']?.hidden).toBe(false)
  })

  it('does not turn series into picks when the link already has a picker', () => {
    const restored = decodeAdhocState({ s: entry, nf: 'catincRouters' })

    expect(restored?.link.selection.pickedDatasourceKeys).toEqual([])
    // The edit still applies if the filter happens to produce that series.
    expect(restored?.link.overrides[KEY_IN]?.label).toBe('in_octets')
  })

  it('drops a duplicate so one series cannot be picked twice', () => {
    expect(decodeAdhocState({ s: [entry, entry] })?.link.selection.pickedDatasourceKeys).toHaveLength(1)
  })

  it('keeps a weighted line style', () => {
    const restored = decodeAdhocState({ s: 'node[1]~ifInOctets~AVERAGE~in~line2~#2a78d6~0' })
    expect(restored?.link.overrides['node[1]|ifInOctets']?.style).toBe('line2')
  })
})

// `~` separates the fields inside an entry, and users type it: `=~` is JEXL's
// match operator. Before these fields were escaped, `a =~ [1,2] ? 1 : 0` decoded
// back as `a =` — silently, taking the style and color with it.
describe('the field separator survives values that contain it', () => {
  const withExpression = (value: string, label = 'bits'): AdhocGraphConfig => ({
    ...fullConfig,
    expressions: [{ id: 'expr-1', label, value, color: '#1baf7a', style: 'line' }]
  })

  const roundTrip = (config: AdhocGraphConfig, link = emptyLink) =>
    decodeAdhocState(encodeAdhocState(config, time, link))

  it('round-trips a JEXL match operator without truncating it', () => {
    const restored = roundTrip(withExpression('in_octets =~ [1,2] ? 1 : 0'))

    expect(restored?.config.expressions[0].value).toBe('in_octets =~ [1,2] ? 1 : 0')
    expect(restored?.config.expressions[0].style).toBe('line')
    expect(restored?.config.expressions[0].color).toBe('#1baf7a')
  })

  it('round-trips a bare tilde and several of them', () => {
    expect(roundTrip(withExpression('~a'))?.config.expressions[0].value).toBe('~a')
    expect(roundTrip(withExpression('a~~b~'))?.config.expressions[0].value).toBe('a~~b~')
  })

  it('round-trips a literal percent, and a literal %7E', () => {
    expect(roundTrip(withExpression('a % 2'))?.config.expressions[0].value).toBe('a % 2')
    expect(roundTrip(withExpression('x%7Ey'))?.config.expressions[0].value).toBe('x%7Ey')
  })

  it('round-trips a tilde in an overridden series', () => {
    const key = 'node[1].hrStorageIndex[PROGRA~1]|used'
    const restored = roundTrip(fullConfig, { ...emptyLink, overrides: { [key]: { label: 'progra' }}})

    expect(restored?.link.overrides).toEqual({ [key]: { label: 'progra' }})
  })

  it('leaves an ordinary link unchanged, so URLs do not grow', () => {
    const query = encodeAdhocState(fullConfig, time, fullLink)

    expect(query.o?.[0]).toBe('node[1].interfaceSnmp[eth0]~ifHCInOctets~MAX~in_octets~area~#2a78d6~1')
  })
})

describe('relative time ranges in the URL', () => {
  const relative: StartEndTime = { startTime: 1, endTime: 2, format: 'hours', range: { unit: 'hours', amount: 24 }}

  it('writes the range instead of the instants it resolved to', () => {
    const query = encodeAdhocState(fullConfig, relative, emptyLink)

    expect(query.range).toBe('hours:24')
    expect(query.start).toBeUndefined()
    expect(query.end).toBeUndefined()
  })

  it('round-trips the range unresolved, for the caller to anchor to now', () => {
    const restored = decodeAdhocState(encodeAdhocState(fullConfig, relative, emptyLink))

    expect(restored?.time.range).toEqual({ unit: 'hours', amount: 24 })
  })

  it('still writes absolute instants for a custom range', () => {
    const query = encodeAdhocState(fullConfig, time, emptyLink)

    expect(query.start).toBe('1704067200')
    expect(query.end).toBe('1704070800')
    expect(query.range).toBeUndefined()
  })

  it('decodes a range-only link, with no start or end present', () => {
    const restored = decodeAdhocState({ range: 'days:7' })

    expect(restored?.time.range).toEqual({ unit: 'days', amount: 7 })
    expect(restored?.time.startTime).toBe(0)
  })

  it('ignores a nonsense range rather than sliding by something arbitrary', () => {
    expect(decodeAdhocState({ range: 'fortnights:2', nf: 'x' })?.time.range).toBeUndefined()
    expect(decodeAdhocState({ range: 'hours:-3', nf: 'x' })?.time.range).toBeUndefined()
    expect(decodeAdhocState({ range: 'hours', nf: 'x' })?.time.range).toBeUndefined()
  })
})

describe('decodeAdhocState is defensive', () => {
  it('drops override entries with no resource id or attribute rather than throwing', () => {
    const restored = decodeAdhocState({
      o: ['', '~~~~~~', 'only-a-resource-id', 'node[1]~ifInOctets~~renamed~~~'],
      start: '1704067200'
    })

    expect(restored?.link.overrides).toEqual({ 'node[1]|ifInOctets': { label: 'renamed' }})
  })

  it('falls back on unrecognized aggregations, styles and colors', () => {
    const restored = decodeAdhocState({ o: 'node[1]~ifInOctets~BOGUS~in~spiral~red~2' })

    expect(restored?.link.overrides['node[1]|ifInOctets']).toEqual({ label: 'in' })
  })

  it('drops an expression missing its name or value', () => {
    const restored = decodeAdhocState({
      nf: 'catincRouters',
      e: ['~in * 8', 'nameless~', 'bits~in * 8~line~#eb6834']
    })

    expect(restored?.config.expressions.map(expression => expression.label)).toEqual(['bits'])
  })

  it('falls back to a sane time range, resolution and count on garbage input', () => {
    const restored = decodeAdhocState({ nf: 'x', start: 'yesterday', end: '-5', res: '0', n: 'many' })

    expect(restored?.time.startTime).toBe(0)
    expect(restored?.time.endTime).toBe(0)
    expect(restored?.time.format).toBe('hours')
    expect(restored?.config.resolution).toBe(DEFAULT_RESOLUTION)
    expect(restored?.link.seriesCount).toBe(0)
  })

  it('tolerates a null-valued key from a query like ?stacked', () => {
    expect(() => decodeAdhocState({ s: null, o: [null], pd: null, stacked: null, start: null })).not.toThrow()
  })
})

describe('encodedQueryLength', () => {
  it('grows with the number of picks and flags an unshareable selection', () => {
    const small = encodeAdhocState(fullConfig, time, fullLink)
    expect(encodedQueryLength(small)).toBeLessThan(MAX_QUERY_LENGTH)

    const many: AdhocLinkState = {
      ...emptyLink,
      selection: {
        ...emptyLink.selection,
        pickedDatasourceKeys: Array.from({ length: 200 }, (_unused, index) =>
          `node[${index}].interfaceSnmp[GigabitEthernet0-0-${index}]|ifHCInOctets`)
      }
    }

    expect(encodedQueryLength(encodeAdhocState(fullConfig, time, many))).toBeGreaterThan(MAX_QUERY_LENGTH)
  })

  it('counts a bare-string value as well as an array one', () => {
    const asString: RouteQuery = { nf: 'abc' }
    const asArray: RouteQuery = { pd: ['abc'] }

    expect(encodedQueryLength(asString)).toBe(encodedQueryLength(asArray))
  })
})
