import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import RrdDefinitionDialog from '@/components/AdhocGraphs/RrdDefinitionDialog.vue'
import { DEFAULT_RESOLUTION } from '@/components/AdhocGraphs/utils/adhocQuery'
import { AdhocGraphConfig, AdhocSeries } from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'

const copyToClipboard = vi.fn()

vi.mock('@/composables/useClipboard', () => ({
  copyToClipboard: (text: string) => copyToClipboard(text),
  default: () => ({ copyToClipboard })
}))

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

const config = (overrides: Partial<AdhocGraphConfig> = {}): AdhocGraphConfig => ({
  series: [series()],
  expressions: [],
  title: 'WAN traffic',
  verticalLabel: 'bits per second',
  stacked: false,
  resolution: DEFAULT_RESOLUTION,
  ...overrides
})

// OnmsDialog teleports its content to document.body (appendTo defaults to 'body'),
// so the dialog's DOM is outside the wrapper and has to be queried from the
// document rather than through wrapper.find().
const q = (dataTest: string) => document.querySelector(`[data-test="${dataTest}"]`)

const textOf = (dataTest: string) => (q(dataTest)?.textContent ?? '').replace(/\s+/g, ' ')

const click = async (dataTest: string) => {
  const element = q(dataTest)
  expect(element, `no element for ${dataTest}`).not.toBeNull();
  (element as HTMLElement).click()
  await flushPromises()
}

const mounted: ReturnType<typeof mount>[] = []

// The dialog's content lands in the DOM a tick after mount, so every case awaits
// it rather than querying synchronously.
const mountDialog = async (graphConfig: AdhocGraphConfig) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
  setActivePinia(pinia)

  const wrapper = mount(RrdDefinitionDialog, {
    props: { visible: true, config: graphConfig },
    global: { plugins: [PrimeVue, pinia] },
    attachTo: document.body
  })

  mounted.push(wrapper)
  await flushPromises()
  return wrapper
}

describe('RrdDefinitionDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    copyToClipboard.mockResolvedValue(undefined)
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop()?.unmount()
    }
    document.body.innerHTML = ''
  })

  it('renders the definition for a single-resource graph', async () => {
    await mountDialog(config())
    const text = textOf('rrd-definition-text')

    expect(text).toContain('report.adhoc.interfaceSnmp.ifInOctets.columns=ifInOctets')
    expect(text).toContain('report.adhoc.interfaceSnmp.ifInOctets.type=interfaceSnmp')
    expect(text).toContain('DEF:ifInOctets={rrd1}:ifInOctets:AVERAGE')
    expect(q('rrd-definition-blocked')).toBeNull()
  })

  it('copies the definition, not just the command', async () => {
    await mountDialog(config())

    await click('rrd-definition-copy')

    expect(copyToClipboard).toHaveBeenCalledTimes(1)
    const copied = copyToClipboard.mock.calls[0][0] as string
    expect(copied).toContain('report.adhoc.interfaceSnmp.ifInOctets.name=WAN traffic')
    expect(copied).toContain('report.adhoc.interfaceSnmp.ifInOctets.command=')
  })

  // The gate is the point of the feature: explain rather than emit a definition
  // that would throw when the server tries to resolve {rrd2}.
  it('explains why a multi-resource graph cannot be exported, and offers no copy', async () => {
    await mountDialog(config({
      series: [
        series(),
        series({ key: 'b', resourceId: 'node[2].interfaceSnmp[eth1]', label: 'other', attribute: 'ifOutOctets' })
      ]
    }))

    expect(q('rrd-definition-blocked')).not.toBeNull()
    expect(textOf('rrd-definition-blocked')).toContain('bound to a single resource')
    expect(q('rrd-definition-text')).toBeNull()
    expect(q('rrd-definition-copy')).toBeNull()
  })

  it('explains an expression it cannot convert', async () => {
    await mountDialog(config({
      expressions: [{ id: 'e1', label: 'x', value: 'math:abs(ifInOctets)', color: '#eb6834', style: 'line' }]
    }))
    expect(textOf('rrd-definition-blocked')).toContain('Function calls are not supported')
  })

  it('emits close from the close button', async () => {
    const wrapper = await mountDialog(config())
    await click('rrd-definition-close')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })
})
