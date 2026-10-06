import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  composeFilterRule,
  looksLikeFilterRule,
  nodeCriteriaOf,
  resourceShortId,
  textMatches,
  toFiqlSearchTerm,
  useAdhocGraphStore
} from '@/stores/adhocGraphStore'

const getNodes = vi.fn()
const getNodesByFilterRule = vi.fn()
const getResourceForNode = vi.fn()
const getGraphMetrics = vi.fn()

vi.mock('@/services', () => ({
  default: {
    getNodes: (...args: unknown[]) => getNodes(...args),
    getNodesByFilterRule: (...args: unknown[]) => getNodesByFilterRule(...args),
    getResourceForNode: (...args: unknown[]) => getResourceForNode(...args),
    getGraphMetrics: (...args: unknown[]) => getGraphMetrics(...args)
  }
}))

/** A child resource as fornode lists it, graphable when it has attributes. */
const child = (id: string, attributes: string[], extra: Record<string, unknown> = {}) => ({
  id,
  label: resourceShortId(id),
  name: id,
  typeLabel: 'SNMP Interface Data',
  rrdGraphAttributes: Object.fromEntries(attributes.map(attribute => [attribute, {}])),
  ...extra
})

/** A node resource, identified by the criterion used to ask for it. */
const nodeResource = (criterion: string, children: unknown[], label = `switch-${criterion}`) => ({
  id: `node[${criterion}]`,
  label,
  name: criterion,
  children: { resource: children }
})

/** The fornode mock's usual answer: two interfaces, two attributes each. */
const twoInterfaces = (criterion: string) => Promise.resolve(nodeResource(criterion, [
  child(`node[${criterion}].interfaceSnmp[eth0]`, ['ifHCOutOctets', 'ifHCInOctets']),
  child(`node[${criterion}].interfaceSnmp[eth1]`, ['ifHCOutOctets', 'ifHCInOctets'])
]))

const ids = (items: { id: string }[]) => items.map(item => item.id)
const keys = (items: { key: string }[]) => items.map(item => item.key)

describe('useAdhocGraphStore', () => {
  let store: ReturnType<typeof useAdhocGraphStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useAdhocGraphStore()
    vi.clearAllMocks()
    getNodes.mockResolvedValue({ node: [], totalCount: 0, count: 0, offset: 0 })
    getNodesByFilterRule.mockResolvedValue({ nodes: [] })
    getResourceForNode.mockImplementation(twoInterfaces)
    getGraphMetrics.mockResolvedValue(null)
  })

  describe('nodeCriteriaOf', () => {
    it('reads a bare node id', () => {
      expect(nodeCriteriaOf('node[42].interfaceSnmp[eth0]')).toBe('42')
    })

    it('reads a foreign-source pair', () => {
      expect(nodeCriteriaOf('nodeSource[Demo:1].interfaceSnmp[eth0]')).toBe('Demo:1')
    })

    it('is null when there is no node part', () => {
      expect(nodeCriteriaOf('nonsense')).toBeNull()
    })
  })

  describe('resourceShortId', () => {
    it('strips the node segment', () => {
      expect(resourceShortId('node[1].interfaceSnmp[eth0]')).toBe('interfaceSnmp[eth0]')
      expect(resourceShortId('node[FS:A].hrStorageIndex[C: Label]')).toBe('hrStorageIndex[C: Label]')
      expect(resourceShortId('nodeSource[FS:A].responseTime[127.0.0.1]')).toBe('responseTime[127.0.0.1]')
    })
  })

  // `label==*<term>*` is string concatenation: a comma or semicolon typed in the
  // search box used to terminate the comparison, producing a malformed filter.
  describe('toFiqlSearchTerm', () => {
    it('leaves an ordinary host name alone', () => {
      expect(toFiqlSearchTerm('core-switch-01.example.com')).toBe('core-switch-01.example.com')
    })

    it('drops every character that is FIQL grammar', () => {
      for (const char of [',', ';', '(', ')', '=', '!', '<', '>', '~', '*']) {
        expect(toFiqlSearchTerm(`a${char}b`), char).toBe('a b')
      }
    })

    it('reduces a term of pure syntax to nothing, so no filter is sent', () => {
      expect(toFiqlSearchTerm(',;()')).toBe('')
    })
  })

  // One box, no toggle: this is what decides whether the text is a rule.
  describe('looksLikeFilterRule', () => {
    it('is false for a label fragment or nothing', () => {
      for (const text of ['', '   ', 'core-sw', 'island', 'Isis-router', 'switch 01']) {
        expect(looksLikeFilterRule(text), text).toBe(false)
      }
    })

    it('is true for anything using the rule grammar', () => {
      for (const text of [
        'catincRouters',
        'catincRouters & catincSNMP',
        'catincProduction | catincStaging',
        '!catincRetired',
        'location=\'Default\'',
        'IPADDR IPLIKE 10.*.*.*',
        'isSNMP',
        'notisICMP',
        'nodeLabel LIKE \'%core%\'',
        '(catincA)'
      ]) {
        expect(looksLikeFilterRule(text), text).toBe(true)
      }
    })
  })

  describe('composeFilterRule', () => {
    it('yields nothing for a label fragment, which goes to the label search instead', () => {
      expect(composeFilterRule('core-sw')).toBe('')
    })

    it('passes a rule through, trimmed', () => {
      expect(composeFilterRule('  catincRouters & catincSNMP ')).toBe('catincRouters & catincSNMP')
    })

    it('unwraps a pasted Grafana nodeFilter() call and drops its display arguments', () => {
      expect(composeFilterRule('nodeFilter(catincRouters, labelFormat=id:label, valueFormat=id)')).toBe('catincRouters')
    })

    it('turns an empty nodeFilter() into the every-node rule', () => {
      expect(composeFilterRule('nodeFilter()')).toBe('IPADDR != \'0.0.0.0\'')
    })
  })

  describe('textMatches', () => {
    it('matches everything when the pattern is blank', () => {
      expect(textMatches('  ', ['anything'])).toBe(true)
    })

    it('is a case-insensitive substring match without wildcards, over any field', () => {
      expect(textMatches('ETH0', ['interfaceSnmp[eth0]', 'eth0'])).toBe(true)
      expect(textMatches('eth0', ['interfaceSnmp[lo]', 'lo', 'eth0-005056b6b6b6'])).toBe(true)
      expect(textMatches('eth9', ['interfaceSnmp[eth0]', 'eth0'])).toBe(false)
    })

    it('is an anchored wildcard match with * and ?, brackets literal', () => {
      expect(textMatches('interfaceSnmp[eth*]', ['interfaceSnmp[eth0]'])).toBe(true)
      expect(textMatches('interfaceSnmp[eth*]', ['interfaceSnmp[lo]'])).toBe(false)
      expect(textMatches('eth*', ['interfaceSnmp[eth0]'])).toBe(false)
      expect(textMatches('ifHC*Octets', ['ifHCInOctets'])).toBe(true)
      expect(textMatches('ifHC*Octets', ['ifHCOutOctets'])).toBe(true)
      expect(textMatches('ifHC*Octets', ['ifInOctets'])).toBe(false)
      expect(textMatches('eth?', ['eth0'])).toBe(true)
      expect(textMatches('eth?', ['eth10'])).toBe(false)
      expect(textMatches('*', ['anything at all'])).toBe(true)
    })

    it('does not treat regex characters in the pattern as regex', () => {
      expect(textMatches('responseTime[127.0.0.1]', ['responseTime[127.0.0.1]'])).toBe(true)
      expect(textMatches('responseTime[127.0.0.1]', ['responseTime[127x0x0x1]'])).toBe(false)
      expect(textMatches('a+b*', ['a+bc'])).toBe(true)
    })
  })

  describe('evaluateNodes', () => {
    it('shows the first page by label when the box is empty, and graphs none of it', async () => {
      getNodes.mockResolvedValue({ node: [{ id: '7', label: 'switch-01' }], totalCount: 1, count: 1 })

      await store.evaluateNodes()

      expect(getNodes).toHaveBeenCalledWith(expect.objectContaining({ limit: 100, orderBy: 'label' }))
      expect(getNodes.mock.calls[0][0]._s).toBeUndefined()
      expect(getNodesByFilterRule).not.toHaveBeenCalled()
      expect(store.nodeOptions).toEqual([{ id: '7', label: 'switch-01' }])
      expect(store.effectiveNodes).toEqual([])
      expect(getResourceForNode).not.toHaveBeenCalled()
    })

    it('sends a label fragment as a FIQL contains search, escaped', async () => {
      await store.setNodeFilter('  core,switch  ')

      expect(getNodes.mock.calls[0][0]._s).toBe('label==*core switch*')
    })

    it('says how many more a label search matched than it listed', async () => {
      getNodes.mockResolvedValue({ node: [{ id: '1', label: 'a' }], totalCount: 2310, count: 1 })

      await store.setNodeFilter('a')

      expect(store.nodeMatchOverflow).toBe(2309)
    })

    it('hands a rule to the filter engine, and everything it matches is effective', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [
        { id: '3', label: 'rtr-03', foreignSource: 'FS', foreignId: 'R3', location: 'Default' },
        { id: '4', label: 'rtr-04' }
      ] })

      await store.setNodeFilter('catincRouters')

      expect(getNodesByFilterRule).toHaveBeenCalledWith('catincRouters')
      expect(getNodes).not.toHaveBeenCalled()
      expect(store.nodeMatchOverflow).toBe(0)
      expect(store.effectiveNodes).toEqual([
        { id: '3', label: 'rtr-03', foreignSource: 'FS', foreignId: 'R3', location: 'Default' },
        { id: '4', label: 'rtr-04' }
      ])
      expect(getResourceForNode).toHaveBeenCalledTimes(2)
    })

    it('reports a rule the engine rejects, then clears the report once one works', async () => {
      getNodesByFilterRule.mockResolvedValueOnce({ error: 'invalid' })
      await store.setNodeFilter('catinc(')

      expect(store.nodeFilterError).toMatch(/could not be parsed/)
      expect(store.nodeOptions).toEqual([])
      expect(store.nodesLoading).toBe(false)

      getNodesByFilterRule.mockResolvedValueOnce({ error: 'failed' })
      await store.setNodeFilter('catincRouters')
      expect(store.nodeFilterError).toMatch(/could not be evaluated/)

      getNodesByFilterRule.mockResolvedValueOnce({ nodes: [{ id: '1', label: 'a' }] })
      await store.setNodeFilter('catincRouters')
      expect(store.nodeFilterError).toBe('')
    })

    it('ignores a superseded search', async () => {
      let releaseSlow: (value: unknown) => void = () => undefined
      getNodes
        .mockReturnValueOnce(new Promise((resolve) => {
          releaseSlow = resolve
        }))
        .mockResolvedValueOnce({ node: [{ id: '2', label: 'fast' }] })

      const slow = store.setNodeFilter('slow')
      await store.setNodeFilter('fast')

      releaseSlow({ node: [{ id: '1', label: 'slow' }] })
      await slow

      expect(store.nodeOptions).toEqual([{ id: '2', label: 'fast' }])
    })
  })

  describe('node picks', () => {
    beforeEach(() => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'a' }, { id: '2', label: 'b' }] })
    })

    it('narrow a filter match to the picks, and clearing them goes back to all', async () => {
      await store.setNodeFilter('catincRouters')
      expect(ids(store.effectiveNodes)).toEqual(['1', '2'])

      await store.setPickedNodes([{ id: '2', label: 'b' }])
      expect(ids(store.effectiveNodes)).toEqual(['2'])

      await store.setPickedNodes([])
      expect(ids(store.effectiveNodes)).toEqual(['1', '2'])
    })

    it('are listed first, once, ahead of the other matches', async () => {
      await store.setNodeFilter('catincRouters')
      await store.setPickedNodes([{ id: '2', label: 'b' }, { id: '9', label: 'elsewhere' }])

      expect(ids(store.nodeOptions)).toEqual(['2', '9', '1'])
    })

    it('are effective even when the box is empty', async () => {
      await store.setPickedNodes([{ id: '5', label: 'picked' }])

      expect(ids(store.effectiveNodes)).toEqual(['5'])
      expect(getResourceForNode).toHaveBeenCalledWith('5')
    })
  })

  describe('resources', () => {
    beforeEach(() => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'a' }, { id: '2', label: 'b' }] })
    })

    it('are loaded once per effective node, in node order, keeping only graphable descendants', async () => {
      getResourceForNode.mockImplementation((criterion: string) => Promise.resolve(nodeResource(criterion, [
        child(`node[${criterion}].interfaceSnmp[eth0]`, ['ifHCInOctets']),
        // A grouping resource with no attributes of its own, but a graphable child.
        {
          id: `node[${criterion}].group[x]`,
          label: 'group',
          children: { resource: [child(`node[${criterion}].group[x].leaf[y]`, ['value'], { typeLabel: 'Leaf' })] }
        }
      ])))

      await store.setNodeFilter('catincRouters')

      expect(getResourceForNode).toHaveBeenCalledTimes(2)
      expect(ids(store.resourceOptions)).toEqual([
        'node[1].interfaceSnmp[eth0]',
        'node[1].group[x].leaf[y]',
        'node[2].interfaceSnmp[eth0]',
        'node[2].group[x].leaf[y]'
      ])
      expect(store.resourceOptions[0].nodeLabel).toBe('switch-1')
      expect(store.resourceOptions[0].attributes).toEqual(['ifHCInOctets'])
      expect(store.resourceOptions[1].typeLabel).toBe('Leaf')
    })

    it('are not refetched for a node already loaded', async () => {
      await store.setNodeFilter('catincRouters')
      expect(getResourceForNode).toHaveBeenCalledTimes(2)

      await store.setPickedNodes([{ id: '1', label: 'a' }])
      await store.setPickedNodes([])

      expect(getResourceForNode).toHaveBeenCalledTimes(2)
    })

    it('survive one node failing, and the spinner stops', async () => {
      getResourceForNode.mockImplementation((criterion: string) => (criterion === '1' ?
        Promise.reject(new Error('boom')) :
        twoInterfaces(criterion)))

      await store.setNodeFilter('catincRouters')

      expect(ids(store.resourceOptions)).toEqual(['node[2].interfaceSnmp[eth0]', 'node[2].interfaceSnmp[eth1]'])
      expect(store.resourcesLoading).toBe(false)
    })

    it('are none of them effective until a filter or a pick says so', async () => {
      await store.setNodeFilter('catincRouters')

      expect(store.resourceOptions).toHaveLength(4)
      expect(store.effectiveResources).toEqual([])
      expect(store.datasourceOptions).toEqual([])
    })

    it('are all effective when a filter matches them', async () => {
      await store.setNodeFilter('catincRouters')
      store.setResourceFilter('interfaceSnmp[eth1]')

      expect(ids(store.effectiveResources)).toEqual(['node[1].interfaceSnmp[eth1]', 'node[2].interfaceSnmp[eth1]'])
      expect(ids(store.resourceOptions)).toEqual(['node[1].interfaceSnmp[eth1]', 'node[2].interfaceSnmp[eth1]'])
    })

    it('match on the id, label, name or type', async () => {
      getResourceForNode.mockImplementation((criterion: string) => Promise.resolve(nodeResource(criterion, [
        child(`node[${criterion}].interfaceSnmp[eth0-0050]`, ['x'], { label: 'Uplink to core', name: 'eth0-0050' }),
        child(`node[${criterion}].responseTime[127.0.0.1]`, ['x'], { typeLabel: 'Response Time', name: '127.0.0.1' })
      ])))
      await store.setNodeFilter('catincRouters')

      store.setResourceFilter('uplink')
      expect(ids(store.effectiveResources)).toEqual(['node[1].interfaceSnmp[eth0-0050]', 'node[2].interfaceSnmp[eth0-0050]'])

      store.setResourceFilter('response time')
      expect(ids(store.effectiveResources)).toEqual(['node[1].responseTime[127.0.0.1]', 'node[2].responseTime[127.0.0.1]'])

      store.setResourceFilter('eth0-0050')
      expect(store.effectiveResources).toHaveLength(2)
    })

    it('stay picked when the filter stops matching them, pinned on top', async () => {
      await store.setNodeFilter('catincRouters')
      store.setPickedResources([store.resourceOptions[0]])
      store.setResourceFilter('interfaceSnmp[eth1]')

      expect(ids(store.effectiveResources)).toEqual(['node[1].interfaceSnmp[eth0]'])
      expect(ids(store.resourceOptions)).toEqual([
        'node[1].interfaceSnmp[eth0]',
        'node[1].interfaceSnmp[eth1]',
        'node[2].interfaceSnmp[eth1]'
      ])
    })

    it('drop their picks when their node is deselected by hand', async () => {
      await store.setNodeFilter('catincRouters')
      store.setPickedResources([store.resourceOptions[0], store.resourceOptions[2]])
      expect(ids(store.pickedResources)).toEqual(['node[1].interfaceSnmp[eth0]', 'node[2].interfaceSnmp[eth0]'])

      await store.setPickedNodes([{ id: '1', label: 'a' }])

      expect(ids(store.pickedResources)).toEqual(['node[1].interfaceSnmp[eth0]'])
    })

    // A pause mid-rule gets a 400 from the engine and, for a moment, no effective
    // nodes. That used to prune every resource and datasource pick for good.
    it('keep their picks through a rule the engine rejects, and through an empty match', async () => {
      await store.setNodeFilter('catincRouters')
      store.setPickedResources([store.resourceOptions[0]])
      store.setResourceFilter('*')
      store.setPickedDatasources([store.datasourceOptions[0]])

      getNodesByFilterRule.mockResolvedValueOnce({ error: 'invalid' })
      await store.setNodeFilter('catincRouters &')
      expect(store.effectiveDatasources).toEqual([])
      expect(ids(store.pickedResources)).toEqual(['node[1].interfaceSnmp[eth0]'])
      expect(keys(store.pickedDatasources)).toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])

      getNodesByFilterRule.mockResolvedValueOnce({ nodes: [] })
      await store.setNodeFilter('catincNothing')
      expect(ids(store.pickedResources)).toEqual(['node[1].interfaceSnmp[eth0]'])

      await store.setNodeFilter('catincRouters & catincSNMP')
      expect(keys(store.effectiveDatasources)).toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])
    })

    it('keep their picks when every node pick is cleared and the filter still matches the node', async () => {
      await store.setNodeFilter('catincRouters')
      await store.setPickedNodes([{ id: '1', label: 'a' }])
      store.setPickedResources([store.resourceOptions[0]])

      await store.setPickedNodes([])

      expect(ids(store.effectiveResources)).toEqual(['node[1].interfaceSnmp[eth0]'])
    })

    // storeByForeignSource: resource ids say `nodeSource[Demo:r1]`, search says `7`.
    it('reach a pick whose id form differs from the node that search listed', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '7', label: 'rtr', foreignSource: 'Demo', foreignId: 'r1' }] })
      getResourceForNode.mockResolvedValue(nodeResource('7', [
        child('nodeSource[Demo:r1].interfaceSnmp[eth0]', ['ifHCInOctets']),
        child('nodeSource[Demo:r1].interfaceSnmp[eth1]', ['ifHCInOctets'])
      ], 'rtr'))

      await store.restore({
        nodeFilter: 'catincRouters',
        resourceFilter: '',
        datasourceFilter: '',
        pickedNodeIds: [],
        pickedResourceIds: ['nodeSource[Demo:r1].interfaceSnmp[eth0]', 'nodeSource[Demo:r1].interfaceSnmp[gone]'],
        pickedDatasourceKeys: ['nodeSource[Demo:r1].interfaceSnmp[eth0]|ifHCInOctets']
      })

      // The live one by its candidate, the vanished one by its node's other name.
      expect(ids(store.effectiveResources)).toEqual([
        'nodeSource[Demo:r1].interfaceSnmp[eth0]',
        'nodeSource[Demo:r1].interfaceSnmp[gone]'
      ])
      expect(store.effectiveResources[0].nodeLabel).toBe('rtr')
      expect(keys(store.effectiveDatasources)).toEqual(['nodeSource[Demo:r1].interfaceSnmp[eth0]|ifHCInOctets'])
    })

    it('are listed once when the same node is known under two ids', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '7', label: 'rtr', foreignSource: 'Demo', foreignId: 'r1' }] })
      getResourceForNode.mockImplementation(() => Promise.resolve(nodeResource('7', [
        child('nodeSource[Demo:r1].interfaceSnmp[eth0]', ['ifHCInOctets'])
      ], 'rtr')))

      // A legacy link named the node `Demo:r1`; the rule then finds it as `7`.
      await store.setPickedNodes([{ id: 'Demo:r1', label: 'Demo:r1' }])
      await store.setNodeFilter('catincRouters')

      expect(ids(store.nodeOptions)).toEqual(['Demo:r1'])

      await store.setPickedNodes([{ id: 'Demo:r1', label: 'rtr' }, { id: '7', label: 'rtr', foreignSource: 'Demo', foreignId: 'r1' }])
      expect(ids(store.resourceOptions)).toEqual(['nodeSource[Demo:r1].interfaceSnmp[eth0]'])
    })

    it('are not fetched at all past the node limit, which is reported instead', async () => {
      getNodesByFilterRule.mockResolvedValue({
        nodes: Array.from({ length: 201 }, (_unused, index) => ({ id: String(index), label: `n${index}` }))
      })

      await store.setNodeFilter('catincEverything')

      expect(getResourceForNode).not.toHaveBeenCalled()
      expect(store.nodeLimitExceeded).toBe(201)
      expect(store.resourceOptions).toEqual([])
      expect(store.resourcesLoading).toBe(false)

      await store.setPickedNodes([{ id: '1', label: 'n1' }])
      expect(store.nodeLimitExceeded).toBe(0)
      expect(getResourceForNode).toHaveBeenCalledTimes(1)
    })

    it('ignore a superseded load', async () => {
      let releaseSlow: (value: unknown) => void = () => undefined
      getResourceForNode
        .mockReturnValueOnce(new Promise((resolve) => {
          releaseSlow = resolve
        }))
        .mockImplementationOnce(twoInterfaces)

      const slow = store.setPickedNodes([{ id: '1', label: 'slow' }])
      await store.setPickedNodes([{ id: '2', label: 'fast' }])

      releaseSlow(nodeResource('1', [child('node[1].interfaceSnmp[eth0]', ['x'])]))
      await slow

      expect(ids(store.resourceOptions)).toEqual(['node[2].interfaceSnmp[eth0]', 'node[2].interfaceSnmp[eth1]'])
    })
  })

  describe('datasources', () => {
    beforeEach(async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'a' }] })
      await store.setNodeFilter('catincRouters')
      store.setResourceFilter('interfaceSnmp[*]')
    })

    it('are every attribute of every effective resource, sorted, without another request', () => {
      expect(keys(store.datasourceOptions)).toEqual([
        'node[1].interfaceSnmp[eth0]|ifHCInOctets',
        'node[1].interfaceSnmp[eth0]|ifHCOutOctets',
        'node[1].interfaceSnmp[eth1]|ifHCInOctets',
        'node[1].interfaceSnmp[eth1]|ifHCOutOctets'
      ])
      expect(store.datasourceOptions[0]).toEqual(expect.objectContaining({
        resourceLabel: 'interfaceSnmp[eth0]',
        nodeLabel: 'switch-1',
        attribute: 'ifHCInOctets'
      }))
      expect(getResourceForNode).toHaveBeenCalledTimes(1)
    })

    it('are not effective until filtered or picked', () => {
      expect(store.effectiveDatasources).toEqual([])
    })

    it('follow a wildcard filter', () => {
      store.setDatasourceFilter('ifHC*Octets')
      expect(store.effectiveDatasources).toHaveLength(4)

      store.setDatasourceFilter('*In*')
      expect(keys(store.effectiveDatasources)).toEqual([
        'node[1].interfaceSnmp[eth0]|ifHCInOctets',
        'node[1].interfaceSnmp[eth1]|ifHCInOctets'
      ])
    })

    it('narrow to picks, and set aside (not drop) a pick whose resource is filtered out', () => {
      store.setDatasourceFilter('ifHC*Octets')
      store.setPickedDatasources([store.datasourceOptions[0], store.datasourceOptions[2]])
      expect(store.effectiveDatasources).toHaveLength(2)

      store.setResourceFilter('interfaceSnmp[eth0]')
      expect(keys(store.effectiveDatasources)).toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])
      expect(keys(store.reachablePickedDatasources)).toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])
      expect(store.pickedDatasources).toHaveLength(2)

      store.setResourceFilter('interfaceSnmp[*]')
      expect(store.effectiveDatasources).toHaveLength(2)
    })

    it('drop a pick whose resource is deselected by hand', () => {
      store.setDatasourceFilter('ifHC*Octets')
      store.setPickedDatasources([store.datasourceOptions[0], store.datasourceOptions[2]])

      store.setPickedResources([store.resourceOptions[0]])

      expect(keys(store.pickedDatasources)).toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])
    })
  })

  describe('restore', () => {
    it('re-evaluates filters against the server as it stands now', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'a' }, { id: '2', label: 'b' }] })

      await store.restore({
        nodeFilter: 'catincRouters',
        resourceFilter: 'interfaceSnmp[eth0]',
        datasourceFilter: 'ifHCInOctets',
        pickedNodeIds: [],
        pickedResourceIds: [],
        pickedDatasourceKeys: []
      })

      expect(getNodesByFilterRule).toHaveBeenCalledWith('catincRouters')
      expect(keys(store.effectiveDatasources)).toEqual([
        'node[1].interfaceSnmp[eth0]|ifHCInOctets',
        'node[2].interfaceSnmp[eth0]|ifHCInOctets'
      ])
    })

    // A link from before filters existed carries only picks; all three panes must
    // still show them, with real labels, and the graph must be exactly the link's.
    it('fills in picks known only by id once their node loads', async () => {
      await store.restore({
        nodeFilter: '',
        resourceFilter: '',
        datasourceFilter: '',
        pickedNodeIds: ['1'],
        pickedResourceIds: ['node[1].interfaceSnmp[eth0]'],
        pickedDatasourceKeys: ['node[1].interfaceSnmp[eth0]|ifHCInOctets']
      })

      expect(store.pickedNodes).toEqual([{ id: '1', label: 'switch-1' }])
      expect(ids(store.nodeOptions)).toContain('1')
      expect(ids(store.pickedResources)).toEqual(['node[1].interfaceSnmp[eth0]'])
      expect(store.effectiveResources[0].attributes).toEqual(['ifHCInOctets', 'ifHCOutOctets'])
      expect(store.effectiveDatasources).toEqual([expect.objectContaining({
        key: 'node[1].interfaceSnmp[eth0]|ifHCInOctets',
        resourceLabel: 'interfaceSnmp[eth0]',
        nodeLabel: 'switch-1'
      })])
      // The picker is usable: the sibling is offered too.
      expect(ids(store.resourceOptions)).toEqual(['node[1].interfaceSnmp[eth0]', 'node[1].interfaceSnmp[eth1]'])
    })

    it('keeps a stand-in for a resource the server no longer knows about', async () => {
      getResourceForNode.mockResolvedValue(nodeResource('1', [child('node[1].interfaceSnmp[eth1]', ['ifHCInOctets'])]))

      await store.restore({
        nodeFilter: '',
        resourceFilter: '',
        datasourceFilter: '',
        pickedNodeIds: ['1'],
        pickedResourceIds: ['node[1].interfaceSnmp[gone]'],
        pickedDatasourceKeys: ['node[1].interfaceSnmp[gone]|ifHCInOctets']
      })

      // Series must survive: the query is relaxed, so it comes back as NaN rather
      // than silently disappearing from the graph.
      expect(keys(store.effectiveDatasources)).toEqual(['node[1].interfaceSnmp[gone]|ifHCInOctets'])
    })

    it('keeps a node the server does not answer for, by id', async () => {
      getResourceForNode.mockResolvedValue(null)

      await store.restore({
        nodeFilter: '',
        resourceFilter: '',
        datasourceFilter: '',
        pickedNodeIds: ['9'],
        pickedResourceIds: ['node[9].interfaceSnmp[x]'],
        pickedDatasourceKeys: ['node[9].interfaceSnmp[x]|ifHCInOctets']
      })

      expect(store.pickedNodes).toEqual([{ id: '9', label: '9' }])
      expect(keys(store.effectiveDatasources)).toEqual(['node[9].interfaceSnmp[x]|ifHCInOctets'])
    })
  })

  describe('selectionState', () => {
    it('is the picker as a link carries it', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'a' }] })
      await store.setNodeFilter('catincRouters')
      store.setResourceFilter('interfaceSnmp[*]')
      store.setPickedResources([store.resourceOptions[1]])
      store.setDatasourceFilter('ifHC*')
      store.setPickedDatasources([store.datasourceOptions[0]])

      expect(store.selectionState).toEqual({
        nodeFilter: 'catincRouters',
        resourceFilter: 'interfaceSnmp[*]',
        datasourceFilter: 'ifHC*',
        pickedNodeIds: [],
        pickedResourceIds: ['node[1].interfaceSnmp[eth1]'],
        pickedDatasourceKeys: ['node[1].interfaceSnmp[eth1]|ifHCInOctets']
      })
    })
  })

  describe('runQuery', () => {
    it('stores the measurements on success', async () => {
      getGraphMetrics.mockResolvedValue({ labels: ['in'], columns: [{ values: [1] }] })

      await store.runQuery({ start: 0, end: 1, step: 1, source: [] })

      expect(store.measurements).toEqual({ labels: ['in'], columns: [{ values: [1] }] })
      expect(store.queryError).toBe('')
      expect(store.queryLoading).toBe(false)
    })

    it('reports an error and drops stale data on failure', async () => {
      getGraphMetrics.mockResolvedValue({ labels: ['in'], columns: [{ values: [1] }] })
      await store.runQuery({ start: 0, end: 1, step: 1, source: [] })

      getGraphMetrics.mockResolvedValue(null)
      await store.runQuery({ start: 0, end: 1, step: 1, source: [] })

      expect(store.measurements).toBeNull()
      expect(store.queryError).not.toBe('')
    })
  })

  describe('clearAll', () => {
    it('resets every filter, pick and list, and discards responses still in flight', async () => {
      getNodesByFilterRule.mockResolvedValue({ nodes: [{ id: '1', label: 'a' }] })
      await store.setNodeFilter('catincRouters')
      store.setResourceFilter('*')
      store.setDatasourceFilter('*')
      store.setPickedDatasources([store.datasourceOptions[0]])

      let releaseSlow: (value: unknown) => void = () => undefined
      getResourceForNode.mockReturnValueOnce(new Promise((resolve) => {
        releaseSlow = resolve
      }))
      const slow = store.setPickedNodes([{ id: '2', label: 'b' }])

      store.clearAll()

      releaseSlow(nodeResource('2', [child('node[2].interfaceSnmp[eth0]', ['x'])]))
      await slow

      expect(store.nodeFilter).toBe('')
      expect(store.resourceFilter).toBe('')
      expect(store.datasourceFilter).toBe('')
      expect(store.nodeOptions).toEqual([])
      expect(store.pickedNodes).toEqual([])
      expect(store.pickedDatasources).toEqual([])
      expect(store.resourceOptions).toEqual([])
      expect(store.effectiveDatasources).toEqual([])
      expect(store.measurements).toBeNull()
    })
  })
})
