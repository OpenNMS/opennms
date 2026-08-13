import { format as formatDate } from 'date-fns'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

import GraphDataTable from '@/components/Resources/GraphDataTable.vue'
import { ConvertedGraphData, GraphMetricsResponse } from '@/types'

// Three samples, ascending, which is the order the measurements API returns.
// The expected strings below are derived from these with date-fns rather than
// hard-coded, so the suite does not depend on the machine's time zone.
const graphData = {
  timestamps: [1704067200000, 1704067500000, 1704067800000],
  // Axis labels — deliberately unlike what the table should render.
  formattedTimestamps: ['00:00', '00:05', '00:10'],
  labels: ['ifInOctets'],
  columns: [{ values: [10, 20, 30] }],
  formattedLabels: []
} as unknown as GraphMetricsResponse

const convertedGraphData = {
  title: '',
  verticalLabel: '',
  series: [],
  values: [],
  properties: {},
  metrics: [{ name: 'ifInOctets', label: 'ifInOctets', aggregation: 'AVERAGE', attribute: 'ifInOctets', resourceId: 'r' }],
  printStatements: [{ format: '', header: 'ifInOctets', metric: 'ifInOctets', value: NaN }]
} as unknown as ConvertedGraphData

const mountTable = () => mount(GraphDataTable, {
  props: { id: 'test', graphData, convertedGraphData },
  global: { plugins: [PrimeVue] }
})

const rows = (wrapper: ReturnType<typeof mountTable>) =>
  wrapper.findAll('tbody tr').map(row => row.findAll('td').map(cell => cell.text()))

const at = (index: number) => formatDate(new Date(graphData.timestamps[index]), 'EEE MMM d HH:mm:ss yyyy')

describe('GraphDataTable Date/Time column', () => {
  // Matches the legacy graph view, which renders this column with d3's `%c`.
  it('shows a full local date and time, not the axis label', () => {
    const first = rows(mountTable())[0][0]

    // Local time, so assert the shape rather than a literal that only holds in UTC.
    expect(first).toMatch(/^[A-Z][a-z]{2} [A-Z][a-z]{2} \d{1,2} \d{2}:\d{2}:\d{2} \d{4}$/)
    expect(first).toBe(at(2))
    expect(first).not.toBe('00:10')
  })

  it('still shows epoch milliseconds when Raw values is ticked', async () => {
    const wrapper = mountTable()
    await wrapper.find('input[type="checkbox"]').setValue(true)

    expect(rows(wrapper)[0][0]).toBe('1704067800000')
  })

  it('renders an empty cell rather than "Invalid Date" for a missing timestamp', () => {
    const wrapper = mount(GraphDataTable, {
      props: {
        id: 'test',
        graphData: { ...graphData, timestamps: [Number.NaN] } as unknown as GraphMetricsResponse,
        convertedGraphData
      },
      global: { plugins: [PrimeVue] }
    })

    expect(rows(wrapper as never)[0][0]).toBe('')
  })
})

describe('GraphDataTable row order', () => {
  // Matches the legacy view, which walks its rows backwards.
  it('puts the most recent sample first, everywhere', () => {
    expect(rows(mountTable())).toEqual([
      [at(2), '30.0'],
      [at(1), '20.0'],
      [at(0), '10.0']
    ])
  })

  // The timestamp and its values are separate arrays indexed in step, so the
  // reversal has to act on the indices. Reversing the data instead would pair
  // each timestamp with the wrong reading — silently.
  it('keeps every reading with its own timestamp', () => {
    const ascending = graphData.timestamps.map((timestamp, index) =>
      [formatDate(new Date(timestamp), 'EEE MMM d HH:mm:ss yyyy'), `${graphData.columns[0].values[index]}.0`])

    expect(rows(mountTable())).toEqual([...ascending].reverse())
  })

  it('reverses the raw values view too', async () => {
    const wrapper = mountTable()
    await wrapper.find('input[type="checkbox"]').setValue(true)

    expect(rows(wrapper)[0][0]).toBe('1704067800000')
  })
})
