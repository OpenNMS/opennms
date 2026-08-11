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

  it('falls back on unrecognised aggregations, styles and colours', () => {
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
