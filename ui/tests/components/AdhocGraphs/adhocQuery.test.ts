import { describe, expect, it } from 'vitest'

import {
  buildMeasurementsPayload,
  configIsQueryable,
  DEFAULT_RESOLUTION,
  expressionIssues,
  expressionReferences,
  labelForDatasource,
  MAX_RESOLUTION,
  plottedSeries,
  querySignature,
  sanitizeLabel,
  seriesLabelIssues,
  stepForRange,
  toConvertedGraphData,
  uniqueLabel
} from '@/components/AdhocGraphs/utils/adhocQuery'
import { StartEndTime } from '@/types'
import { AdhocDatasourceOption, AdhocExpression, AdhocGraphConfig, AdhocSeries } from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'

const series = (overrides: Partial<AdhocSeries> = {}): AdhocSeries => ({
  key: 'node[1].interfaceSnmp[eth0]|ifHCInOctets',
  label: 'ifHCInOctets',
  resourceId: 'node[1].interfaceSnmp[eth0]',
  attribute: 'ifHCInOctets',
  aggregation: ConsolidationFunctionType.AVERAGE,
  color: '#2a78d6',
  style: 'line',
  hidden: false,
  ...overrides
})

const expression = (overrides: Partial<AdhocExpression> = {}): AdhocExpression => ({
  id: 'expr-1',
  label: 'bits',
  value: 'ifHCInOctets * 8',
  color: '#eb6834',
  style: 'line',
  ...overrides
})

const config = (overrides: Partial<AdhocGraphConfig> = {}): AdhocGraphConfig => ({
  series: [],
  expressions: [],
  title: '',
  verticalLabel: '',
  stacked: false,
  resolution: DEFAULT_RESOLUTION,
  ...overrides
})

// 2024-01-01T00:00:00Z .. +1h, in the seconds that StartEndTime carries
const time: StartEndTime = { startTime: 1704067200, endTime: 1704070800, format: 'hours' }

describe('sanitizeLabel', () => {
  it('reduces a node/resource/attribute triple to a JEXL identifier', () => {
    expect(sanitizeLabel('switch-01.example.com', 'eth0 (WAN)', 'ifHCInOctets'))
      .toBe('switch_01_example_com_eth0_WAN_ifHCInOctets')
  })

  it('collapses runs of separators and trims the ends', () => {
    expect(sanitizeLabel('  a -- b  ')).toBe('a_b')
  })

  it('prefixes a leading digit so the result is a legal identifier', () => {
    expect(sanitizeLabel('10.1.1.1', 'ifInOctets')).toBe('_10_1_1_1_ifInOctets')
  })

  it('falls back rather than returning an empty identifier', () => {
    expect(sanitizeLabel('***')).toBe('series')
    expect(sanitizeLabel(undefined)).toBe('series')
  })
})

describe('uniqueLabel', () => {
  it('returns the candidate when it is free', () => {
    expect(uniqueLabel('ifInOctets', ['other'])).toBe('ifInOctets')
  })

  it('suffixes past every taken variant', () => {
    expect(uniqueLabel('ifInOctets', ['ifInOctets', 'ifInOctets_2'])).toBe('ifInOctets_3')
  })

  it('de-duplicates two resources that sanitize identically', () => {
    const datasource = (resourceId: string): AdhocDatasourceOption => ({
      key: `${resourceId}|ifInOctets`,
      resourceId,
      resourceLabel: 'eth 0',
      nodeId: '1',
      nodeLabel: 'switch',
      attribute: 'ifInOctets'
    })

    const first = labelForDatasource(datasource('node[1].interfaceSnmp[eth-0]'), [])
    const second = labelForDatasource(datasource('node[1].interfaceSnmp[eth_0]'), [first])

    expect(first).toBe('switch_eth_0_ifInOctets')
    expect(second).toBe('switch_eth_0_ifInOctets_2')
  })
})

describe('expressionReferences', () => {
  it('matches a whole identifier', () => {
    expect(expressionReferences('ifInOctets * 8', 'ifInOctets')).toBe(true)
    expect(expressionReferences('(ifInOctets+ifOutOctets)', 'ifOutOctets')).toBe(true)
  })

  // The reason this is not a substring test: a shorter label is a prefix of its own
  // de-duplicated sibling, and a false match would wrongly suppress the raw series.
  it('does not match a label that is only a prefix of the identifier used', () => {
    expect(expressionReferences('ifInOctets_2 * 8', 'ifInOctets')).toBe(false)
  })

  it('is false for an empty label', () => {
    expect(expressionReferences('anything', '')).toBe(false)
  })
})

describe('stepForRange', () => {
  it('divides the range by the requested resolution', () => {
    expect(stepForRange(0, 3_600_000, 360)).toBe(10_000)
  })

  it('never returns a sub-second step', () => {
    expect(stepForRange(0, 100, 400)).toBe(1000)
    expect(stepForRange(0, 0, 400)).toBe(1000)
  })

  it('clamps an absurd resolution to the supported ceiling', () => {
    expect(stepForRange(0, 4_000_000_000, 10_000_000)).toBe(1_000_000)
  })
})

describe('buildMeasurementsPayload', () => {
  it('converts seconds to millis and asks for a relaxed, bounded query', () => {
    const payload = buildMeasurementsPayload(config({ series: [series()] }), time)

    expect(payload.start).toBe(1_704_067_200_000)
    expect(payload.end).toBe(1_704_070_800_000)
    expect(payload.relaxed).toBe(true)
    expect(payload.maxrows).toBe(MAX_RESOLUTION)
    expect(payload.step).toBe(9000)
  })

  it('emits one source per series with its label and aggregation', () => {
    const payload = buildMeasurementsPayload(
      config({ series: [series({ aggregation: ConsolidationFunctionType.MAX })] }),
      time
    )

    expect(payload.source).toEqual([{
      aggregation: 'MAX',
      attribute: 'ifHCInOctets',
      label: 'ifHCInOctets',
      resourceId: 'node[1].interfaceSnmp[eth0]',
      transient: false
    }])
  })

  it('omits the expression list entirely when there are none', () => {
    expect(buildMeasurementsPayload(config({ series: [series()] }), time).expression).toBeUndefined()
  })

  it('marks a hidden source transient only when an expression consumes it', () => {
    const consumed = buildMeasurementsPayload(
      config({ series: [series({ hidden: true })], expressions: [expression()] }),
      time
    )
    expect(consumed.source[0].transient).toBe(true)
    expect(consumed.expression).toEqual([{ label: 'bits', value: 'ifHCInOctets * 8', transient: false }])

    // Hiding a source nothing references would just yield an empty graph, so the
    // flag is derived rather than taken on trust.
    const unconsumed = buildMeasurementsPayload(
      config({ series: [series({ hidden: true })], expressions: [expression({ value: 'somethingElse * 8' })] }),
      time
    )
    expect(unconsumed.source[0].transient).toBe(false)
  })
})

describe('plottedSeries', () => {
  it('drops a consumed hidden source and appends the expressions', () => {
    const plotted = plottedSeries(config({
      series: [series({ hidden: true }), series({ key: 'k2', label: 'ifHCOutOctets', attribute: 'ifHCOutOctets' })],
      expressions: [expression()]
    }))

    expect(plotted.map(item => item.label)).toEqual(['ifHCOutOctets', 'bits'])
  })

  it('keeps a hidden source that no expression consumes', () => {
    const plotted = plottedSeries(config({ series: [series({ hidden: true })] }))
    expect(plotted.map(item => item.label)).toEqual(['ifHCInOctets'])
  })
})

describe('toConvertedGraphData', () => {
  it('produces the metrics/printStatements shape the prefab components consume', () => {
    const converted = toConvertedGraphData(config({
      series: [series()],
      expressions: [expression()],
      title: 'Traffic',
      verticalLabel: 'bits/sec'
    }))

    expect(converted.title).toBe('Traffic')
    expect(converted.verticalLabel).toBe('bits/sec')
    expect(converted.metrics.map(metric => metric.name)).toEqual(['ifHCInOctets', 'bits'])
    expect(converted.printStatements.map(statement => statement.header)).toEqual(['ifHCInOctets', 'bits'])
    expect(converted.printStatements.every(statement => statement.metric === statement.header)).toBe(true)
  })
})

describe('seriesLabelIssues', () => {
  it('is empty for a well-formed config', () => {
    expect(seriesLabelIssues(config({ series: [series()] }))).toEqual({})
  })

  it('flags blank, duplicate and non-identifier labels', () => {
    const blank = series({ key: 'a', label: '  ' })
    const dupeOne = series({ key: 'b', label: 'same' })
    const dupeTwo = series({ key: 'c', label: 'same' })
    const bad = series({ key: 'd', label: 'has space' })

    const issues = seriesLabelIssues(config({ series: [blank, dupeOne, dupeTwo, bad] }))

    expect(issues.a).toBe('A label is required.')
    expect(issues.b).toBe('Labels must be unique.')
    expect(issues.c).toBe('Labels must be unique.')
    expect(issues.d).toBe('Use letters, digits and underscores only.')
  })

  it('treats an expression name as occupying the same namespace', () => {
    const issues = seriesLabelIssues(config({
      series: [series({ key: 'a', label: 'shared' })],
      expressions: [expression({ label: 'shared' })]
    }))

    expect(issues.a).toBe('Labels must be unique.')
  })
})

describe('expressionIssues', () => {
  it('is empty when the expression names a known label', () => {
    expect(expressionIssues(config({ series: [series()], expressions: [expression()] }))).toEqual({})
  })

  it('reports a blank expression against the value field', () => {
    const issues = expressionIssues(config({ series: [series()], expressions: [expression({ value: '  ' })] }))
    expect(issues['expr-1']).toEqual({ field: 'value', message: 'An expression is required.' })
  })

  it('reports an expression whose every identifier is unknown', () => {
    const issues = expressionIssues(config({ series: [series()], expressions: [expression({ value: 'nope * 8' })] }))
    expect(issues['expr-1']).toEqual({ field: 'value', message: 'Unknown label: nope' })
  })

  // JEXL functions and constants would be flagged by a stricter rule; only a
  // reference to nothing at all is unambiguously wrong.
  it('accepts an expression that mixes a known label with unknown identifiers', () => {
    const issues = expressionIssues(config({
      series: [series()],
      expressions: [expression({ value: 'math:abs(ifHCInOctets)' })]
    }))
    expect(issues['expr-1']).toBeUndefined()
  })

  it('reports a blank name against the label field', () => {
    const issues = expressionIssues(config({ series: [series()], expressions: [expression({ label: '' })] }))
    expect(issues['expr-1']).toEqual({ field: 'label', message: 'A name is required.' })
  })
})

describe('querySignature', () => {
  const relative = { startTime: 1704067200, endTime: 1704153600, format: 'hours', range: { unit: 'hours' as const, amount: 24 }}

  // Re-resolving a relative window is not a change of query. If it were, every
  // Refresh would fire twice — once explicitly, once from the watcher.
  it('is unchanged when a relative window slides forward', () => {
    const later = { ...relative, startTime: 1704070800, endTime: 1704157200 }

    expect(querySignature(config({ series: [series()] }), later))
      .toBe(querySignature(config({ series: [series()] }), relative))
  })

  it('changes when the range itself changes', () => {
    const other = { ...relative, range: { unit: 'hours' as const, amount: 2 }}

    expect(querySignature(config({ series: [series()] }), other))
      .not.toBe(querySignature(config({ series: [series()] }), relative))
  })

  it('still tracks the instants of an absolute window', () => {
    const later = { startTime: 1704070800, endTime: 1704157200, format: 'hours' }

    expect(querySignature(config({ series: [series()] }), later))
      .not.toBe(querySignature(config({ series: [series()] }), time))
  })
})

describe('configIsQueryable', () => {
  it('is false with no series', () => {
    expect(configIsQueryable(config())).toBe(false)
  })

  it('is true for a valid series-only config', () => {
    expect(configIsQueryable(config({ series: [series()] }))).toBe(true)
  })

  it('is false while any series or expression is invalid', () => {
    expect(configIsQueryable(config({ series: [series({ label: '' })] }))).toBe(false)
    expect(configIsQueryable(config({
      series: [series()],
      expressions: [expression({ value: '' })]
    }))).toBe(false)
  })
})
