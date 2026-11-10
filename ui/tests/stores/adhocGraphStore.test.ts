import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { nodeCriteriaOf, toFiqlSearchTerm, useAdhocGraphStore } from '@/stores/adhocGraphStore'
import { AdhocDatasourceOption, AdhocResourceOption } from '@/types/adhocGraph'

const getNodes = vi.fn()
const getResourceForNode = vi.fn()
const getResourceById = vi.fn()
const getGraphMetrics = vi.fn()

vi.mock('@/services', () => ({
  default: {
    getNodes: (...args: unknown[]) => getNodes(...args),
    getResourceForNode: (...args: unknown[]) => getResourceForNode(...args),
    getResourceById: (...args: unknown[]) => getResourceById(...args),
    getGraphMetrics: (...args: unknown[]) => getGraphMetrics(...args)
  }
}))

/** A node resource whose children are the graphable sub-resources. */
const nodeResource = (nodeLabel: string, childIds: string[]) => ({
  id: `node[${nodeLabel}]`,
  label: nodeLabel,
  children: {
    resource: childIds.map(id => ({ id, label: id, typeLabel: 'SNMP Interface Data' }))
  }
})

const resourceOption = (id: string): AdhocResourceOption => ({
  id,
  label: id,
  typeLabel: 'SNMP Interface Data',
  nodeId: '1',
  nodeLabel: 'switch-01'
})

describe('useAdhocGraphStore', () => {
  let store: ReturnType<typeof useAdhocGraphStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useAdhocGraphStore()
    vi.clearAllMocks()
    getNodes.mockResolvedValue({ node: [], totalCount: 0, count: 0, offset: 0 })
    getResourceForNode.mockResolvedValue(null)
    getResourceById.mockResolvedValue(null)
    getGraphMetrics.mockResolvedValue(null)
  })

  describe('searchNodes', () => {
    it('asks for a bounded, label-ordered page and maps the response', async () => {
      getNodes.mockResolvedValue({ node: [{ id: '7', label: 'switch-01' }] })

      await store.searchNodes('')

      expect(getNodes).toHaveBeenCalledWith(expect.objectContaining({ limit: 100, orderBy: 'label' }))
      expect(getNodes.mock.calls[0][0]._s).toBeUndefined()
      expect(store.nodeOptions).toEqual([{ id: '7', label: 'switch-01' }])
    })

    it('sends a FIQL contains search when a term is given', async () => {
      await store.searchNodes('  core  ')
      expect(getNodes.mock.calls[0][0]._s).toBe('label==*core*')
    })

    it('empties the list when the request fails', async () => {
      getNodes.mockResolvedValue({ node: [{ id: '7', label: 'switch-01' }] })
      await store.searchNodes('')

      getNodes.mockResolvedValue(false)
      await store.searchNodes('nope')

      expect(store.nodeOptions).toEqual([])
    })
  })

  // `label==*<term>*` is string concatenation: a comma or semicolon typed in the
  // search box used to terminate the comparison, producing a malformed filter, a
  // failed request and an empty picker with nothing to explain it.
  describe('toFiqlSearchTerm', () => {
    it('leaves an ordinary host name alone', () => {
      expect(toFiqlSearchTerm('core-switch-01.example.com')).toBe('core-switch-01.example.com')
    })

    it('drops every character that is FIQL grammar', () => {
      for (const char of [',', ';', '(', ')', '=', '!', '<', '>', '~', '*']) {
        expect(toFiqlSearchTerm(`a${char}b`), char).toBe('a b')
      }
    })

    it('collapses the whitespace it leaves behind and trims', () => {
      expect(toFiqlSearchTerm('  core,,;;switch  ')).toBe('core switch')
    })

    it('reduces a term of pure syntax to nothing, so no filter is sent', () => {
      expect(toFiqlSearchTerm(',;()')).toBe('')
    })
  })

  describe('searchNodes escapes the filter', () => {
    it('does not let a comma break out of the comparison', async () => {
      await store.searchNodes('core,switch')

      expect(getNodes.mock.calls[0][0]._s).toBe('label==*core switch*')
    })

    it('sends no filter at all when nothing searchable remains', async () => {
      await store.searchNodes(';;;')

      expect(getNodes.mock.calls[0][0]._s).toBeUndefined()
    })
  })

  describe('a failing lookup is isolated', () => {
    // Without per-item isolation one rejection escapes Promise.all, propagates out
    // of loadResources and strands resourcesLoading at true.
    it('keeps the other nodes when one rejects, and stops the spinner', async () => {
      getResourceForNode.mockImplementation((id: string) => (id === '1' ?
        Promise.reject(new Error('boom')) :
        Promise.resolve(nodeResource(`switch-${id}`, [`node[${id}].interfaceSnmp[eth0]`]))))

      await store.setSelectedNodes([{ id: '1', label: 'a' }, { id: '2', label: 'b' }])

      expect(store.resourceOptions.map(option => option.id)).toEqual(['node[2].interfaceSnmp[eth0]'])
      expect(store.resourcesLoading).toBe(false)
    })

    it('keeps the other resources when a datasource lookup rejects', async () => {
      getResourceById.mockImplementation((id: string) => (id.includes('eth0') ?
        Promise.reject(new Error('boom')) :
        Promise.resolve({ rrdGraphAttributes: { ifHCInOctets: {}}})))

      await store.setSelectedResources([
        resourceOption('node[1].interfaceSnmp[eth0]'),
        resourceOption('node[1].interfaceSnmp[eth1]')
      ])

      expect(store.datasourceOptions.map(option => option.resourceId)).toEqual(['node[1].interfaceSnmp[eth1]'])
      expect(store.datasourcesLoading).toBe(false)
    })

    it('does not strand the spinner when every lookup rejects', async () => {
      getResourceForNode.mockRejectedValue(new Error('boom'))

      await store.setSelectedNodes([{ id: '1', label: 'a' }])

      expect(store.resourceOptions).toEqual([])
      expect(store.resourcesLoading).toBe(false)
    })
  })

  describe('the selection cascade', () => {
    it('loads the children of every selected node', async () => {
      getResourceForNode.mockImplementation((id: string) =>
        Promise.resolve(nodeResource(`switch-${id}`, [`node[${id}].interfaceSnmp[eth0]`])))

      await store.setSelectedNodes([{ id: '1', label: 'a' }, { id: '2', label: 'b' }])

      expect(store.resourceOptions.map(option => option.id)).toEqual([
        'node[1].interfaceSnmp[eth0]',
        'node[2].interfaceSnmp[eth0]'
      ])
      expect(store.resourceOptions[0].nodeLabel).toBe('switch-1')
    })

    it('loads the sorted graphable attributes of every selected resource', async () => {
      getResourceById.mockResolvedValue({ rrdGraphAttributes: { ifHCOutOctets: {}, ifHCInOctets: {}}})

      await store.setSelectedResources([resourceOption('node[1].interfaceSnmp[eth0]')])

      expect(store.datasourceOptions.map(option => option.attribute)).toEqual(['ifHCInOctets', 'ifHCOutOctets'])
      expect(store.datasourceOptions[0].key).toBe('node[1].interfaceSnmp[eth0]|ifHCInOctets')
    })

    it('prunes only the selections that disappeared when a node is deselected', async () => {
      getResourceForNode.mockImplementation((id: string) =>
        Promise.resolve(nodeResource(`switch-${id}`, [`node[${id}].interfaceSnmp[eth0]`])))
      getResourceById.mockImplementation((id: string) =>
        Promise.resolve({ rrdGraphAttributes: { ifHCInOctets: {}}, id }))

      await store.setSelectedNodes([{ id: '1', label: 'a' }, { id: '2', label: 'b' }])
      await store.setSelectedResources([
        resourceOption('node[1].interfaceSnmp[eth0]'),
        resourceOption('node[2].interfaceSnmp[eth0]')
      ])
      store.setSelectedDatasources([...store.datasourceOptions])
      expect(store.selectedDatasources).toHaveLength(2)

      // Dropping node 2 must not disturb what was chosen under node 1.
      await store.setSelectedNodes([{ id: '1', label: 'a' }])

      expect(store.selectedResources.map(resource => resource.id)).toEqual(['node[1].interfaceSnmp[eth0]'])
      expect(store.selectedDatasources.map(datasource => datasource.key))
        .toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])
    })

    it('clears the whole cascade when the last node is deselected', async () => {
      getResourceForNode.mockResolvedValue(nodeResource('switch-1', ['node[1].interfaceSnmp[eth0]']))
      getResourceById.mockResolvedValue({ rrdGraphAttributes: { ifHCInOctets: {}}})

      await store.setSelectedNodes([{ id: '1', label: 'a' }])
      await store.setSelectedResources([resourceOption('node[1].interfaceSnmp[eth0]')])
      store.setSelectedDatasources([...store.datasourceOptions])

      await store.setSelectedNodes([])

      expect(store.resourceOptions).toEqual([])
      expect(store.selectedResources).toEqual([])
      expect(store.datasourceOptions).toEqual([])
      expect(store.selectedDatasources).toEqual([])
    })
  })

  describe('out-of-order responses', () => {
    it('ignores a superseded node search', async () => {
      let releaseSlow: (value: unknown) => void = () => undefined
      getNodes
        .mockReturnValueOnce(new Promise((resolve) => {
          releaseSlow = resolve
        }))
        .mockResolvedValueOnce({ node: [{ id: '2', label: 'fast' }] })

      const slow = store.searchNodes('slow')
      await store.searchNodes('fast')

      releaseSlow({ node: [{ id: '1', label: 'slow' }] })
      await slow

      expect(store.nodeOptions).toEqual([{ id: '2', label: 'fast' }])
    })

    it('ignores a superseded resource load', async () => {
      let releaseSlow: (value: unknown) => void = () => undefined
      getResourceForNode
        .mockReturnValueOnce(new Promise((resolve) => {
          releaseSlow = resolve
        }))
        .mockResolvedValueOnce(nodeResource('fast', ['node[2].interfaceSnmp[eth0]']))

      const slow = store.setSelectedNodes([{ id: '1', label: 'slow' }])
      await store.setSelectedNodes([{ id: '2', label: 'fast' }])

      releaseSlow(nodeResource('slow', ['node[1].interfaceSnmp[eth0]']))
      await slow

      expect(store.resourceOptions.map(option => option.id)).toEqual(['node[2].interfaceSnmp[eth0]'])
    })
  })

  describe('adoptDatasources', () => {
    it('injects link-restored sources as both the options and the selection', () => {
      const restored: AdhocDatasourceOption[] = [{
        key: 'node[1].interfaceSnmp[eth0]|ifHCInOctets',
        resourceId: 'node[1].interfaceSnmp[eth0]',
        resourceLabel: 'node[1].interfaceSnmp[eth0]',
        nodeId: '',
        nodeLabel: '',
        attribute: 'ifHCInOctets'
      }]

      store.adoptDatasources(restored)

      expect(store.datasourceOptions).toHaveLength(1)
      expect(store.selectedDatasources.map(datasource => datasource.key)).toEqual([restored[0].key])
    })

    it('prefers an already-loaded option over the sparse stand-in from a link', async () => {
      getResourceById.mockResolvedValue({ rrdGraphAttributes: { ifHCInOctets: {}}})
      await store.setSelectedResources([resourceOption('node[1].interfaceSnmp[eth0]')])

      store.adoptDatasources([{
        key: 'node[1].interfaceSnmp[eth0]|ifHCInOctets',
        resourceId: 'node[1].interfaceSnmp[eth0]',
        resourceLabel: 'node[1].interfaceSnmp[eth0]',
        nodeId: '',
        nodeLabel: '',
        attribute: 'ifHCInOctets'
      }])

      expect(store.datasourceOptions).toHaveLength(1)
      expect(store.selectedDatasources[0].nodeLabel).toBe('switch-01')
    })
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

  describe('restoreSelection', () => {
    beforeEach(() => {
      getResourceForNode.mockImplementation((criterion: string) =>
        Promise.resolve(nodeResource(`switch-${criterion}`, [
          `node[${criterion}].interfaceSnmp[eth0]`,
          `node[${criterion}].interfaceSnmp[eth1]`
        ])))
      getResourceById.mockResolvedValue({ rrdGraphAttributes: { ifHCInOctets: {}, ifHCOutOctets: {}}})
    })

    // The bug this covers: a shared link populated only the datasource pane, so
    // the node and resource panes showed nothing selected.
    it('populates all three panes from the link sources', async () => {
      await store.restoreSelection([
        { resourceId: 'node[1].interfaceSnmp[eth0]', attribute: 'ifHCInOctets' }
      ])

      expect(store.selectedNodes).toEqual([{ id: '1', label: 'switch-1' }])
      expect(store.nodeOptions).toContainEqual({ id: '1', label: 'switch-1' })
      expect(store.selectedResources.map(resource => resource.id)).toEqual(['node[1].interfaceSnmp[eth0]'])
      expect(store.selectedDatasources.map(datasource => datasource.key))
        .toEqual(['node[1].interfaceSnmp[eth0]|ifHCInOctets'])
    })

    it('offers the siblings of a restored resource, so the picker is usable', async () => {
      await store.restoreSelection([
        { resourceId: 'node[1].interfaceSnmp[eth0]', attribute: 'ifHCInOctets' }
      ])

      expect(store.resourceOptions.map(resource => resource.id)).toEqual([
        'node[1].interfaceSnmp[eth0]',
        'node[1].interfaceSnmp[eth1]'
      ])
      expect(store.datasourceOptions.map(datasource => datasource.attribute))
        .toEqual(['ifHCInOctets', 'ifHCOutOctets'])
    })

    it('restores real labels rather than the raw resource id', async () => {
      await store.restoreSelection([
        { resourceId: 'node[1].interfaceSnmp[eth0]', attribute: 'ifHCInOctets' }
      ])

      expect(store.selectedDatasources[0].nodeLabel).toBe('switch-1')
      expect(store.selectedDatasources[0].resourceLabel).toBe('node[1].interfaceSnmp[eth0]')
      expect(store.selectedResources[0].nodeLabel).toBe('switch-1')
    })

    it('restores a selection spanning two nodes', async () => {
      await store.restoreSelection([
        { resourceId: 'node[1].interfaceSnmp[eth0]', attribute: 'ifHCInOctets' },
        { resourceId: 'node[2].interfaceSnmp[eth1]', attribute: 'ifHCOutOctets' }
      ])

      expect(store.selectedNodes.map(node => node.id)).toEqual(['1', '2'])
      expect(store.selectedResources.map(resource => resource.id)).toEqual([
        'node[1].interfaceSnmp[eth0]',
        'node[2].interfaceSnmp[eth1]'
      ])
      expect(store.selectedDatasources).toHaveLength(2)
    })

    it('keeps a stand-in for a resource the server no longer knows about', async () => {
      getResourceForNode.mockResolvedValue(null)

      await store.restoreSelection([
        { resourceId: 'node[9].interfaceSnmp[gone]', attribute: 'ifHCInOctets' }
      ])

      // Series must survive: the query is relaxed, so it comes back as NaN rather
      // than silently disappearing from the graph.
      expect(store.selectedDatasources.map(datasource => datasource.key))
        .toEqual(['node[9].interfaceSnmp[gone]|ifHCInOctets'])
    })

    it('falls back to stand-ins when no node can be parsed at all', async () => {
      await store.restoreSelection([{ resourceId: 'nonsense', attribute: 'ifHCInOctets' }])

      expect(getResourceForNode).not.toHaveBeenCalled()
      expect(store.selectedDatasources.map(datasource => datasource.key)).toEqual(['nonsense|ifHCInOctets'])
    })
  })

  describe('searchNodes keeps the selection visible', () => {
    it('pins selected nodes to the top of a later, unrelated search', async () => {
      getResourceForNode.mockResolvedValue(nodeResource('switch-1', ['node[1].interfaceSnmp[eth0]']))
      await store.setSelectedNodes([{ id: '1', label: 'switch-1' }])

      getNodes.mockResolvedValue({ node: [{ id: '2', label: 'router-2' }] })
      await store.searchNodes('router')

      expect(store.nodeOptions.map(node => node.id)).toEqual(['1', '2'])
    })

    it('does not list a selected node twice when the search also returns it', async () => {
      getResourceForNode.mockResolvedValue(nodeResource('switch-1', ['node[1].interfaceSnmp[eth0]']))
      await store.setSelectedNodes([{ id: '1', label: 'switch-1' }])

      getNodes.mockResolvedValue({ node: [{ id: '1', label: 'switch-1' }, { id: '2', label: 'router-2' }] })
      await store.searchNodes('')

      expect(store.nodeOptions.map(node => node.id)).toEqual(['1', '2'])
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
    it('resets every list and discards responses still in flight', async () => {
      let releaseSlow: (value: unknown) => void = () => undefined
      getResourceForNode.mockReturnValueOnce(new Promise((resolve) => {
        releaseSlow = resolve
      }))

      const slow = store.setSelectedNodes([{ id: '1', label: 'a' }])
      store.clearAll()

      releaseSlow(nodeResource('switch-1', ['node[1].interfaceSnmp[eth0]']))
      await slow

      expect(store.selectedNodes).toEqual([])
      expect(store.resourceOptions).toEqual([])
      expect(store.measurements).toBeNull()
    })
  })
})
