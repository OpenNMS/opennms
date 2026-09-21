import { describe, expect, it } from 'vitest'

import { DEFAULT_RESOLUTION } from '@/components/AdhocGraphs/utils/adhocQuery'
import {
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

const fullConfig: AdhocGraphConfig = {
  series: [
    {
      key: 'node[1].interfaceSnmp[eth0]|ifHCInOctets',
      label: 'in_octets',
      resourceId: 'node[1].interfaceSnmp[eth0]',
      attribute: 'ifHCInOctets',
      aggregation: ConsolidationFunctionType.MAX,
      color: '#2a78d6',
      style: 'area',
      hidden: true
    },
    {
      key: 'node[2].interfaceSnmp[eth1]|ifHCOutOctets',
      label: 'out_octets',
      resourceId: 'node[2].interfaceSnmp[eth1]',
      attribute: 'ifHCOutOctets',
      aggregation: ConsolidationFunctionType.AVERAGE,
      color: '#eb6834',
      style: 'line3',
      hidden: false
    }
  ],
  expressions: [
    { id: 'expr-1', label: 'total_bits', value: '(in_octets + out_octets) * 8', color: '#1baf7a', style: 'stack' }
  ],
  title: 'WAN traffic',
  verticalLabel: 'bits/sec',
  stacked: true,
  resolution: 800
}

describe('encodeAdhocState / decodeAdhocState', () => {
  it('round-trips a complete graph', () => {
    const restored = decodeAdhocState(encodeAdhocState(fullConfig, time))

    expect(restored).not.toBeNull()
    expect(restored?.time).toEqual(time)
    expect(restored?.config.title).toBe('WAN traffic')
    expect(restored?.config.verticalLabel).toBe('bits/sec')
    expect(restored?.config.stacked).toBe(true)
    expect(restored?.config.resolution).toBe(800)
    expect(restored?.config.series).toEqual(fullConfig.series)
    // The id is regenerated on decode; everything else survives.
    expect(restored?.config.expressions).toEqual([{ ...fullConfig.expressions[0], id: 'expr-0' }])
  })

  it('omits defaulted fields so a simple graph gets a short link', () => {
    const query = encodeAdhocState({ ...fullConfig, title: '', verticalLabel: '', stacked: false, resolution: DEFAULT_RESOLUTION }, time)

    expect(query.title).toBeUndefined()
    expect(query.vlabel).toBeUndefined()
    expect(query.stacked).toBeUndefined()
    expect(query.res).toBeUndefined()
  })

  it('returns null when the query carries no ad-hoc state', () => {
    expect(decodeAdhocState({})).toBeNull()
    expect(decodeAdhocState({ unrelated: 'value' })).toBeNull()
  })

  it('accepts a single-series query that vue-router hands over as a bare string', () => {
    const restored = decodeAdhocState({
      s: 'node[1].x|ifInOctets~ifInOctets~AVERAGE~in~line~#2a78d6~0',
      start: '1704067200',
      end: '1704070800',
      fmt: 'hours'
    })

    expect(restored?.config.series).toHaveLength(1)
    expect(restored?.config.series[0].label).toBe('in')
  })
})

// `~` separates the fields inside an entry, and users type it: `=~` is JEXL's
// match operator. Before these fields were escaped, `a =~ [1,2] ? 1 : 0` decoded
// back as `a =` — silently, taking the style and color with it.
describe('the field separator survives values that contain it', () => {
  const withExpression = (value: string, label = 'bits'): AdhocGraphConfig => ({
    ...fullConfig,
    expressions: [{ id: 'e1', label, value, color: '#eb6834', style: 'line' }]
  })

  const roundTrip = (input: AdhocGraphConfig) => decodeAdhocState(encodeAdhocState(input, time))

  it('round-trips a JEXL match operator without truncating it', () => {
    const value = 'in_octets =~ [1,2] ? 1 : 0'
    const restored = roundTrip(withExpression(value))

    expect(restored?.config.expressions[0].value).toBe(value)
    // The fields after the tilde used to be shifted out of place.
    expect(restored?.config.expressions[0].style).toBe('line')
    expect(restored?.config.expressions[0].color).toBe('#eb6834')
  })

  it('round-trips a bare tilde and several of them', () => {
    expect(roundTrip(withExpression('~in_octets'))?.config.expressions[0].value).toBe('~in_octets')
    expect(roundTrip(withExpression('a ~ b ~ c'))?.config.expressions[0].value).toBe('a ~ b ~ c')
  })

  it('round-trips a tilde in an expression name', () => {
    expect(roundTrip(withExpression('in_octets * 8', 'od~d'))?.config.expressions[0].label).toBe('od~d')
  })

  // A literal percent must not be mistaken for the escape it produces.
  it('round-trips a literal percent, and a literal %7E', () => {
    expect(roundTrip(withExpression('in_octets % 100'))?.config.expressions[0].value).toBe('in_octets % 100')
    expect(roundTrip(withExpression('a %7E b'))?.config.expressions[0].value).toBe('a %7E b')
    expect(roundTrip(withExpression('a %25 b'))?.config.expressions[0].value).toBe('a %25 b')
  })

  // Storage resources can carry a Windows short name.
  it('round-trips a tilde in a resource id', () => {
    const resourceId = 'node[1].hrStorageIndex[C:\\PROGRA~1]'
    const restored = roundTrip({
      ...fullConfig,
      series: [{ ...fullConfig.series[0], resourceId, key: `${resourceId}|ifInOctets` }],
      expressions: []
    })

    expect(restored?.config.series[0].resourceId).toBe(resourceId)
    expect(restored?.config.series[0].attribute).toBe('ifHCInOctets')
    expect(restored?.config.series[0].color).toBe('#2a78d6')
  })

  it('leaves an ordinary link unchanged, so URLs do not grow', () => {
    const query = encodeAdhocState(fullConfig, time)
    const entries = Array.isArray(query.s) ? query.s : [query.s]

    for (const entry of entries) {
      expect(entry).not.toContain('%')
    }
  })
})

describe('relative time ranges in the URL', () => {
  const relative: StartEndTime = { startTime: 1704067200, endTime: 1704153600, format: 'hours', range: { unit: 'hours', amount: 24 }}

  // The bug this fixes: absolute instants freeze a bookmark to whenever it was made.
  it('writes the range instead of the instants it resolved to', () => {
    const query = encodeAdhocState(fullConfig, relative)

    expect(query.range).toBe('hours:24')
    expect(query.start).toBeUndefined()
    expect(query.end).toBeUndefined()
  })

  it('round-trips the range unresolved, for the caller to anchor to now', () => {
    const restored = decodeAdhocState(encodeAdhocState(fullConfig, relative))

    expect(restored?.time.range).toEqual({ unit: 'hours', amount: 24 })
  })

  it('still writes absolute instants for a custom range', () => {
    const query = encodeAdhocState(fullConfig, time)

    expect(query.range).toBeUndefined()
    expect(query.start).toBe('1704067200')
    expect(query.end).toBe('1704070800')
  })

  it('decodes a range-only link, with no start or end present', () => {
    const restored = decodeAdhocState({ range: 'days:7' })

    expect(restored).not.toBeNull()
    expect(restored?.time.range).toEqual({ unit: 'days', amount: 7 })
  })

  it('ignores a nonsense range rather than sliding by something arbitrary', () => {
    for (const value of ['fortnights:2', 'hours:0', 'hours:-3', 'hours', 'hours:abc', '']) {
      expect(decodeAdhocState({ range: value, start: '1704067200' })?.time.range, value).toBeUndefined()
    }
  })

  it('prefers the range when a link somehow carries both', () => {
    const restored = decodeAdhocState({ range: 'hours:2', start: '1704067200', end: '1704070800' })

    expect(restored?.time.range).toEqual({ unit: 'hours', amount: 2 })
  })
})

describe('decodeAdhocState is defensive', () => {
  it('drops entries with no resource id or attribute rather than throwing', () => {
    const restored = decodeAdhocState({
      s: ['', '~~~~~~', 'only-a-resource-id', 'node[1]~ifInOctets~AVERAGE~in~line~#2a78d6~0'],
      start: '1704067200'
    })

    expect(restored?.config.series.map(entry => entry.attribute)).toEqual(['ifInOctets'])
  })

  it('drops a duplicate series so one selection cannot be plotted twice', () => {
    const entry = 'node[1]~ifInOctets~AVERAGE~in~line~#2a78d6~0'
    expect(decodeAdhocState({ s: [entry, entry] })?.config.series).toHaveLength(1)
  })

  it('keeps a weighted line style through a round trip', () => {
    const restored = decodeAdhocState({ s: 'node[1]~ifInOctets~AVERAGE~in~line2~#2a78d6~0' })
    expect(restored?.config.series[0].style).toBe('line2')
  })

  it('falls back on unrecognized aggregations, styles and colors', () => {
    const restored = decodeAdhocState({ s: 'node[1]~ifInOctets~BOGUS~in~spiral~red~0' })
    const entry = restored?.config.series[0]

    expect(entry?.aggregation).toBe(ConsolidationFunctionType.AVERAGE)
    expect(entry?.style).toBe('line')
    expect(entry?.color).toBe('')
  })

  it('drops an expression missing its name or value', () => {
    const restored = decodeAdhocState({
      s: 'node[1]~ifInOctets~AVERAGE~in~line~#2a78d6~0',
      e: ['~in * 8', 'nameless~', 'bits~in * 8~line~#eb6834']
    })

    expect(restored?.config.expressions.map(expression => expression.label)).toEqual(['bits'])
  })

  it('falls back to a sane time range and resolution on garbage input', () => {
    const restored = decodeAdhocState({ s: 'node[1]~ifInOctets', start: 'yesterday', end: '-5', res: '0' })

    expect(restored?.time.startTime).toBe(0)
    expect(restored?.time.endTime).toBe(0)
    expect(restored?.time.format).toBe('hours')
    expect(restored?.config.resolution).toBe(DEFAULT_RESOLUTION)
  })

  it('tolerates a null-valued key from a query like ?stacked', () => {
    expect(() => decodeAdhocState({ s: null, e: [null], stacked: null, start: null })).not.toThrow()
  })
})

describe('encodedQueryLength', () => {
  it('grows with the number of series and flags an unshareable selection', () => {
    const short = encodeAdhocState(fullConfig, time)
    expect(encodedQueryLength(short)).toBeLessThan(MAX_QUERY_LENGTH)

    const many: AdhocGraphConfig = {
      ...fullConfig,
      series: Array.from({ length: 60 }, (_unused, index) => ({
        ...fullConfig.series[0],
        key: `node[${index}].interfaceSnmp[GigabitEthernet0-0-${index}]|ifHCInOctets`,
        label: `in_octets_${index}`,
        resourceId: `node[${index}].interfaceSnmp[GigabitEthernet0-0-${index}]`
      }))
    }

    expect(encodedQueryLength(encodeAdhocState(many, time))).toBeGreaterThan(MAX_QUERY_LENGTH)
  })

  it('counts a bare-string value as well as an array one', () => {
    const query: RouteQuery = { s: 'abc' }
    expect(encodedQueryLength(query)).toBeGreaterThan(0)
  })
})
