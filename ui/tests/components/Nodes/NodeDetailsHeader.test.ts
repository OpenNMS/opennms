// ui/tests/components/Nodes/NodeDetailsHeader.test.ts
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import { OnmsMenu, OnmsTag } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { OnmsTooltip } from '@opennms/onms-ui'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

// Shows which value was formatted without depending on the display zone or format.
vi.mock('@/lib/displayTimeZone', () => ({ formatInDisplayZone: (value: number) => `fmt:${value}` }))

const node = { id: 42, label: 'srv-42', location: 'Default', assetRecord: {}} as any

const mountHeader = () =>
  mount(NodeDetailsHeader, {
    props: { node },
    global: { plugins: [PrimeVue] }
  })

const category = (id: number, name: string) => ({ id, name, authorizedGroups: [] })

describe('NodeDetailsHeader.vue', () => {
  it('renders the node status, label, id and location badges', () => {
    const text = mountHeader().text()

    expect(text).toContain('Status:')
    expect(text).toContain('Label: srv-42')
    expect(text).toContain('ID: 42')
    expect(text).toContain('Monitoring Location: Default')
  })

  // getNodeStatusString derives these three from node.type; the tag colours them.
  describe('status severity', () => {
    const statusTag = (type?: string) => {
      const wrapper = mount(NodeDetailsHeader, {
        props: { node: { ...node, type }},
        global: { plugins: [PrimeVue] }
      })

      return wrapper.findAllComponents(OnmsTag)[0]
    }

    it('shows an active node in success', () => {
      const tag = statusTag('A')

      expect(tag.props('value')).toBe('Status: Active')
      expect(tag.props('severity')).toBe('success')
    })

    it('shows a deleted node in danger', () => {
      const tag = statusTag('D')

      expect(tag.props('value')).toBe('Status: Deleted')
      expect(tag.props('severity')).toBe('danger')
    })

    it('shows an unknown status in info', () => {
      const tag = statusTag('X')

      expect(tag.props('value')).toBe('Status: Unknown')
      expect(tag.props('severity')).toBe('info')
    })

    it('falls back to info for a node with no type at all', () => {
      const tag = statusTag(undefined)

      expect(tag.props('severity')).toBe('info')
    })
  })

  // The actions menu lives on the Node Details title row now; this header carried a second
  // copy of the same link list.
  it('no longer renders its own actions menu', () => {
    const wrapper = mountHeader()

    expect(wrapper.findComponent(OnmsMenu).exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Actions')
  })

  // Replaces the Surveillance Category Memberships panel: the names move onto the header, and
  // the panel's Edit button becomes the Surveillance Categories action in the node menu.
  describe('categories badge', () => {
    // Where the tooltip directive parks its resolved text; see OnmsTooltip.test.ts.
    const tooltipOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipValue

    const mountWith = (categories: unknown[]) =>
      mount(NodeDetailsHeader, {
        props: { node: { ...node, categories }},
        global: {
          plugins: [PrimeVue],
          directives: { 'onms-tooltip': OnmsTooltip }
        }
      })

    const badge = (categories: unknown[]) => mountWith(categories).find('[data-test="categories-tag"]')

    it('lists a single category', () => {
      expect(badge([category(1, 'Routers')]).text()).toBe('Categories: Routers')
    })

    it('lists two categories comma-separated', () => {
      expect(badge([category(1, 'Routers'), category(2, 'Switches')]).text())
        .toBe('Categories: Routers, Switches')
    })

    // Past two the badge would crowd the header row, so it shows the first two and defers.
    it('shows the first two and an ellipsis when there are more', () => {
      expect(badge([category(1, 'Routers'), category(2, 'Switches'), category(3, 'Production')]).text())
        .toBe('Categories: Routers, Switches ...')
    })

    it('says None when the node is in no categories', () => {
      expect(badge([]).text()).toBe('Categories: None')
    })

    // A node fetched before its categories arrive has no array at all.
    it('says None when the node has no categories field', () => {
      expect(mountWith(undefined as never).find('[data-test="categories-tag"]').text())
        .toBe('Categories: None')
    })

    it('puts every category in a tooltip, one per line, once some are hidden', () => {
      const el = badge([
        category(1, 'Routers'),
        category(2, 'Switches'),
        category(3, 'Production'),
        category(4, 'East-DC')
      ]).element

      expect(tooltipOf(el)).toBe('Routers\nSwitches\nProduction\nEast-DC')
    })

    // With two or fewer the badge already shows them all, so a tooltip would only repeat itself.
    it.each([
      [[]],
      [[category(1, 'Routers')]],
      [[category(1, 'Routers'), category(2, 'Switches')]]
    ])('has no tooltip when nothing is hidden (%#)', (categories) => {
      const el = badge(categories).element

      expect(tooltipOf(el)).toBeUndefined()
    })

    // Nothing here is clickable, so the cursor is the only cue that a tooltip exists -- and it
    // must not appear on a badge that has none.
    it('marks only a badge with a tooltip as hoverable', () => {
      expect(badge([category(1, 'Routers'), category(2, 'S'), category(3, 'P')]).classes())
        .toContain('tooltip-target')
      expect(badge([category(1, 'Routers')]).classes()).not.toContain('tooltip-target')
    })
  })

  // The legacy page shows the requisition and foreign id only for nodes that have them.
  describe('FS:FID badge', () => {
    const fsTag = (extra: object) =>
      mount(NodeDetailsHeader, { props: { node: { ...node, ...extra }}, global: { plugins: [PrimeVue] }})
        .find('[data-test="foreign-source-tag"]')

    it('shows foreign source and foreign id for a requisitioned node', () => {
      expect(fsTag({ foreignSource: 'DcbRequisition', foreignId: '1783366455117' }).text())
        .toBe('FS:FID: DcbRequisition:1783366455117')
    })

    it('is left out for a node with no foreign source', () => {
      expect(fsTag({ foreignSource: null, foreignId: null }).exists()).toBe(false)
    })

    it('comes right after the location badge', () => {
      const values = mount(NodeDetailsHeader, {
        props: { node: { ...node, foreignSource: 'fs', foreignId: 'fid' }},
        global: { plugins: [PrimeVue] }
      }).findAllComponents(OnmsTag).map(tag => tag.props('value'))

      expect(values.indexOf('FS:FID: fs:fid')).toBe(values.indexOf('Monitoring Location: Default') + 1)
    })
  })

  describe('flow data badge', () => {
    const NOW = Date.UTC(2026, 9, 5, 12, 0, 0)
    const DAY = 24 * 60 * 60 * 1000
    const tooltipOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipValue

    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(NOW)
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    const flowsTag = (extra: object) =>
      mount(NodeDetailsHeader, {
        props: { node: { ...node, ...extra }},
        global: { plugins: [PrimeVue], directives: { 'onms-tooltip': OnmsTooltip }}
      }).find('[data-test="flows-tag"]')

    it('shows a success badge when the node had a flow in the last week', () => {
      const tag = flowsTag({ lastIngressFlow: NOW - DAY })

      expect(tag.text()).toBe('Flow data available')
      expect(tag.classes().join(' ')).toContain('success')
    })

    it('counts either direction', () => {
      expect(flowsTag({ lastEgressFlow: NOW - DAY }).exists()).toBe(true)
    })

    it('is left out when the node has never had flows', () => {
      expect(flowsTag({}).exists()).toBe(false)
    })

    // OnmsNode.getHasFlows ages flows out after maxFlowAgeSeconds (7 days by default).
    it('is left out when the last flow is more than a week old', () => {
      expect(flowsTag({ lastIngressFlow: NOW - 8 * DAY, lastEgressFlow: NOW - 8 * DAY }).exists()).toBe(false)
    })

    it('gives the last ingress and egress flow times in its tooltip, each on its own line', () => {
      const el = flowsTag({ lastIngressFlow: NOW - DAY }).element

      expect(tooltipOf(el)).toBe(`Last ingress flow:\nfmt:${NOW - DAY}\nLast egress flow:\nNone`)
      // Keeps each timestamp on one line; see the component's unscoped style.
      expect((el as never as Record<string, unknown>).$_ptooltipClass).toBe('node-flows-tooltip')
    })
  })
})
