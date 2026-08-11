import { describe, expect, it } from 'vitest'

import { DEFAULT_RESOLUTION } from '@/components/AdhocGraphs/utils/adhocQuery'
import {
  buildRrdGraphDefinition,
  describeIneligibility,
  isRrdGraphDefinition,
  reportNameFor,
  resourceTypeOf,
  RrdGraphDefinition
} from '@/components/AdhocGraphs/utils/rrdGraphDefinition'
import RrdGraphConverter from '@/components/Resources/utils/RrdGraphConverter.class'
import { PreFabGraph } from '@/types'
import { AdhocExpression, AdhocGraphConfig, AdhocSeries } from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'

const RESOURCE = 'node[1].interfaceSnmp[eth0]'

const series = (overrides: Partial<AdhocSeries> = {}): AdhocSeries => ({
  key: `${RESOURCE}|ifInOctets`,
  label: 'ifInOctets',
  resourceId: RESOURCE,
  attribute: 'ifInOctets',
  aggregation: ConsolidationFunctionType.AVERAGE,
  color: '#2a78d6',
  style: 'line',
  hidden: false,
  ...overrides
})

const expression = (overrides: Partial<AdhocExpression> = {}): AdhocExpression => ({
  id: 'expr-1',
  label: 'bitsIn',
  value: 'ifInOctets * 8',
  color: '#eb6834',
  style: 'line',
  ...overrides
})

const config = (overrides: Partial<AdhocGraphConfig> = {}): AdhocGraphConfig => ({
  series: [series()],
  expressions: [],
  title: 'WAN traffic',
  verticalLabel: 'bits per second',
  stacked: false,
  resolution: DEFAULT_RESOLUTION,
  ...overrides
})

const build = (input: AdhocGraphConfig): RrdGraphDefinition => {
  const result = buildRrdGraphDefinition(input)
  expect(isRrdGraphDefinition(result), JSON.stringify(result)).toBe(true)
  return result as RrdGraphDefinition
}

/** Feed a built definition back through the SPA's own prefab-graph parser. */
const parseBack = (definition: RrdGraphDefinition) => {
  const graphDef = {
    name: definition.reportName,
    title: 'WAN traffic',
    columns: definition.columns,
    command: definition.command,
    externalValues: [],
    propertiesValues: [],
    suppress: [],
    types: [definition.type],
    description: null,
    height: null,
    width: null,
    order: 0
  } as PreFabGraph

  return new RrdGraphConverter({ graphDef, resourceId: RESOURCE }).model
}

describe('resourceTypeOf', () => {
  it('reads the type out of a resource id', () => {
    expect(resourceTypeOf('node[1].interfaceSnmp[eth0]')).toBe('interfaceSnmp')
    expect(resourceTypeOf('node[1].nodeSnmp[]')).toBe('nodeSnmp')
    expect(resourceTypeOf('nodeSource[Demo:1].interfaceSnmp[eth0]')).toBe('interfaceSnmp')
  })

  // The bracketed part routinely contains dots and slashes, so splitting on '.'
  // would pick the wrong token.
  it('is not confused by punctuation inside the brackets', () => {
    expect(resourceTypeOf('node[1].hrStorageIndex[/var/log.d]')).toBe('hrStorageIndex')
  })

  it('returns null when there is no type to read', () => {
    expect(resourceTypeOf('nonsense')).toBeNull()
    expect(resourceTypeOf('[1]')).toBeNull()
  })
})

describe('reportNameFor', () => {
  it('names the report after the resource type and its datasources', () => {
    expect(reportNameFor(RESOURCE, ['ifHCInOctets', 'ifHCOutOctets']))
      .toBe('adhoc.interfaceSnmp.ifHCInOctets_ifHCOutOctets')
  })

  // A prefab graph renders for every resource of its type, so the instance must
  // not appear in the id — it would read as "this graph is only for eth0".
  it('leaves the resource instance out of the id', () => {
    const name = reportNameFor('node[7].interfaceSnmp[GigabitEthernet0-0-1]', ['ifInOctets'])

    expect(name).toBe('adhoc.interfaceSnmp.ifInOctets')
    expect(name).not.toContain('7')
    expect(name).not.toContain('Gigabit')
  })

  it('summarises rather than listing every datasource once there are many', () => {
    const name = reportNameFor(RESOURCE, ['a', 'b', 'c', 'd', 'e', 'f'])

    expect(name).toBe('adhoc.interfaceSnmp.a_b_c_d_and_2_more')
  })

  it('produces a key-safe id even from an odd resource id or datasource', () => {
    const name = reportNameFor('node[1].hrStorageIndex[/var/log.d]', ['hrStorage Used'])

    expect(name).toBe('adhoc.hrStorageIndex.hrStorage_Used')
    expect(name).toMatch(/^[A-Za-z0-9_.]+$/)
  })

  it('falls back when the resource type cannot be read', () => {
    expect(reportNameFor('nonsense', ['ifInOctets'])).toBe('adhoc.resource.ifInOctets')
  })
})

describe('describeIneligibility', () => {
  it('accepts a single-resource graph', () => {
    expect(describeIneligibility(config())).toBeNull()
  })

  it('refuses an empty graph', () => {
    expect(describeIneligibility(config({ series: [] }))).toContain('no series')
  })

  // The reason this whole feature is gated: {rrdN} resolves against one resource.
  it('refuses a graph spanning two resources, and says how many', () => {
    const reason = describeIneligibility(config({
      series: [series(), series({ key: 'b', resourceId: 'node[2].interfaceSnmp[eth1]', label: 'other' })]
    }))

    expect(reason).toContain('bound to a single resource')
    expect(reason).toContain('2 of them')
  })

  it('refuses an expression it cannot convert, with the converter reason', () => {
    const reason = describeIneligibility(config({
      expressions: [expression({ value: 'math:abs(ifInOctets)' })]
    }))

    expect(reason).toContain('Expression \'bitsIn\'')
    expect(reason).toContain('Function calls are not supported')
  })

  it('refuses an expression built on another expression', () => {
    const reason = describeIneligibility(config({
      expressions: [expression(), expression({ id: 'expr-2', label: 'doubled', value: 'bitsIn * 2' })]
    }))

    expect(reason).toContain('which is not a source series')
  })
})

describe('buildRrdGraphDefinition', () => {
  it('emits the four report properties', () => {
    const definition = build(config())

    expect(definition.reportName).toBe('adhoc.interfaceSnmp.ifInOctets')
    expect(definition.type).toBe('interfaceSnmp')
    expect(definition.columns).toEqual(['ifInOctets'])
    // The title stays the human-readable name; only the id is derived.
    expect(definition.properties).toContain('report.adhoc.interfaceSnmp.ifInOctets.name=WAN traffic')
    expect(definition.properties).toContain('report.adhoc.interfaceSnmp.ifInOctets.columns=ifInOctets')
    expect(definition.properties).toContain('report.adhoc.interfaceSnmp.ifInOctets.type=interfaceSnmp')
  })

  it('emits a DEF per series with its consolidation function', () => {
    const definition = build(config({
      series: [series(), series({ key: 'b', label: 'ifOutOctets', attribute: 'ifOutOctets', aggregation: ConsolidationFunctionType.MAX })]
    }))

    expect(definition.command).toContain('DEF:ifInOctets={rrd1}:ifInOctets:AVERAGE')
    expect(definition.command).toContain('DEF:ifOutOctets={rrd2}:ifOutOctets:MAX')
    expect(definition.columns).toEqual(['ifInOctets', 'ifOutOctets'])
  })

  // {rrdN} is positional over `columns`, so one attribute read at two consolidation
  // functions is a single column referenced twice.
  it('shares one column between two series on the same attribute', () => {
    const definition = build(config({
      series: [
        series(),
        series({ key: 'b', label: 'ifInOctetsMax', aggregation: ConsolidationFunctionType.MAX })
      ]
    }))

    expect(definition.columns).toEqual(['ifInOctets'])
    expect(definition.command).toContain('DEF:ifInOctets={rrd1}:ifInOctets:AVERAGE')
    expect(definition.command).toContain('DEF:ifInOctetsMax={rrd1}:ifInOctets:MAX')
  })

  it('emits a CDEF in RPN for each expression', () => {
    const definition = build(config({ expressions: [expression()] }))
    expect(definition.command).toContain('CDEF:bitsIn=ifInOctets,8,*')
  })

  // The style names exist to line up with these commands one-for-one.
  it('maps every style onto its RRD draw command', () => {
    expect(build(config()).command).toContain('LINE1:ifInOctets#2a78d6:')
    expect(build(config({ series: [series({ style: 'line2' })] })).command).toContain('LINE2:ifInOctets#2a78d6:')
    expect(build(config({ series: [series({ style: 'line3' })] })).command).toContain('LINE3:ifInOctets#2a78d6:')
    expect(build(config({ series: [series({ style: 'area' })] })).command).toContain('AREA:ifInOctets#2a78d6:')

    const stacked = build(config({ series: [series({ style: 'stack' })] })).command
    expect(stacked).toContain('AREA:ifInOctets#2a78d6:')
    expect(stacked).toContain(':STACK')
  })

  it('emits Avg/Min/Max/Last GPRINTs for each drawn series', () => {
    const command = build(config()).command
    for (const cf of ['AVERAGE', 'MIN', 'MAX', 'LAST']) {
      expect(command).toContain(`GPRINT:ifInOctets:${cf}:`)
    }
  })

  // Same rule the measurements query applies via `transient`: keep the data, drop
  // the drawing.
  it('keeps the DEF but draws nothing for a hidden, consumed series', () => {
    const definition = build(config({
      series: [series({ hidden: true })],
      expressions: [expression()]
    }))

    expect(definition.command).toContain('DEF:ifInOctets=')
    expect(definition.command).not.toContain('LINE1:ifInOctets#')
    expect(definition.command).toContain('LINE1:bitsIn#')
  })

  it('draws a hidden series that no expression consumes', () => {
    const definition = build(config({ series: [series({ hidden: true })] }))
    expect(definition.command).toContain('LINE1:ifInOctets#')
  })

  it('omits the vertical label when there is none', () => {
    expect(build(config({ verticalLabel: '  ' })).command).not.toContain('--vertical-label')
  })

  // The id no longer depends on the title, so an untitled graph still gets a
  // meaningful one — which is the point of deriving it from the data.
  it('still names an untitled graph after its datasources', () => {
    const definition = build(config({ title: '' }))
    expect(definition.reportName).toBe('adhoc.interfaceSnmp.ifInOctets')
    expect(definition.command).toContain('--title="Ad-hoc graph"')
  })

  it('names a multi-datasource report after all of them', () => {
    const definition = build(config({
      series: [series(), series({ key: 'b', label: 'ifOutOctets', attribute: 'ifOutOctets' })]
    }))
    expect(definition.reportName).toBe('adhoc.interfaceSnmp.ifInOctets_ifOutOctets')
  })
})

describe('escaping', () => {
  it('single-escapes the effective command and doubles it for the properties file', () => {
    const definition = build(config())

    // What RRDtool receives.
    expect(definition.command).toContain('GPRINT:ifInOctets:AVERAGE:"Avg \\: %8.2lf %s"')
    // What goes on disk, matching the shipped snmp-graph.properties.d files.
    expect(definition.properties).toContain('GPRINT:ifInOctets:AVERAGE:"Avg \\\\: %8.2lf %s"')
  })

  it('escapes a quote in the title rather than breaking the argument', () => {
    const definition = build(config({ title: 'The "big" one' }))
    expect(definition.command).toContain('--title="The \\"big\\" one"')
  })

  it('breaks the properties command across continuation lines', () => {
    const lines = build(config()).properties.split('\n')
    const continuations = lines.filter(line => line.endsWith(' \\'))

    expect(continuations.length).toBeGreaterThan(0)
    // Every continuation line but the last of the block ends in a backslash.
    expect(lines[lines.length - 1].endsWith('\\')).toBe(false)
  })
})

// The SPA already contains the parser for this format (a TypeScript port of
// Backshift), so a generated definition can be checked by reading it back.
describe('round-trips through RrdGraphConverter', () => {
  it('recovers the title, vertical label and metrics of a simple graph', () => {
    const model = parseBack(build(config()))

    expect(model.title).toBe('WAN traffic')
    expect(model.verticalLabel).toBe('bits per second')
    expect(model.metrics.map((metric: { name?: string }) => metric.name)).toContain('ifInOctets')
    expect(model.metrics[0].attribute).toBe('ifInOctets')
    expect(model.metrics[0].aggregation).toBe('AVERAGE')
    expect(model.metrics[0].resourceId).toBe(RESOURCE)
  })

  it('recovers the drawn series, their colours and their order', () => {
    const model = parseBack(build(config({
      series: [
        series({ style: 'area', color: '#1baf7a' }),
        series({ key: 'b', label: 'ifOutOctets', attribute: 'ifOutOctets', color: '#eb6834' })
      ]
    })))

    const drawn = model.series.filter((entry: { type: string }) => entry.type !== 'hidden')
    expect(drawn.map((entry: { metric: string }) => entry.metric)).toEqual(['ifInOctets', 'ifOutOctets'])
    expect(drawn[0].type).toBe('area')
    expect(drawn[0].color).toBe('#1baf7a')
    expect(drawn[1].type).toBe('line')
    expect(drawn[1].color).toBe('#eb6834')
  })

  it('recovers an expression as a JEXL metric equivalent to the original', () => {
    const model = parseBack(build(config({ expressions: [expression()] })))
    const derived = model.metrics.find((metric: { name?: string }) => metric.name === 'bitsIn') as { expression: string }

    expect(derived).toBeDefined()
    // The CDEF went out as RPN and came back as JEXL — the same arithmetic the
    // user typed into the expression editor.
    expect(derived.expression.replace(/\s+/g, '')).toBe('(ifInOctets*8)')
  })

  // A GPRINT with a consolidation function makes the converter synthesise its own
  // consolidated metric (ifInOctets_AVERAGE_<id>), so the statements are matched
  // by their format rather than by metric name.
  it('recovers an Avg/Min/Max/Last legend for the drawn series', () => {
    const model = parseBack(build(config()))
    const formats = model.printStatements
      .map((statement: { format: string }) => statement.format)
      .filter((format: string) => format.includes('%8.2lf'))

    expect(formats).toHaveLength(4)
    expect(formats[0]).toContain('Avg')
    expect(formats[1]).toContain('Min')
    expect(formats[2]).toContain('Max')
    expect(formats[3]).toContain('Last')

    // The escaped colon survived both escaping layers and came back as a plain one.
    expect(formats[0]).toContain('Avg : ')

    const consolidated = model.printStatements
      .filter((statement: { format: string }) => statement.format.includes('%8.2lf'))
      .map((statement: { metric: string }) => statement.metric)
    expect(consolidated.every((metric: string) => metric.startsWith('ifInOctets_'))).toBe(true)
  })
})
