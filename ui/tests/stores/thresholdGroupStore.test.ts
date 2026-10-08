import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import {
  getDefaultExpression,
  getDefaultResourceFilter,
  getDefaultThreshold,
  getDefaultThresholdGroup,
  useThresholdGroupStore
} from '@/stores/thresholdGroupStore'
import { FilterOperator, ThresholdDefinitionKind, ThresholdType } from '@/lib/thresholdValidator'
import { CreateEditMode } from '@/types'
import API from '@/services'
import type { ResourceFilter, Threshold, ThresholdGroup } from '@/types/thresholdConfig'

vi.mock('@/services', () => ({
  default: {
    getThresholdGroups: vi.fn(),
    getThresholdGroup: vi.fn(),
    getThresholdingMetadata: vi.fn(),
    createThresholdGroup: vi.fn(),
    updateThresholdGroup: vi.fn(),
    deleteThresholdGroup: vi.fn()
  }
}))

const ok = (payload?: unknown) => ({ success: true, message: '', payload })
const fail = (message: string, status?: number) => ({ success: false, message, status })

const group = (overrides: Partial<ThresholdGroup> = {}): ThresholdGroup => ({
  ...getDefaultThresholdGroup(),
  name: 'mib2',
  rrdRepository: '/rrd',
  ...overrides
})

describe('thresholdGroupStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('defaults', () => {
    test('produces a schema-valid threshold', () => {
      const threshold = getDefaultThreshold()

      // rearm and trigger are populated rather than blank: the schema demands them even for the change
      // thresholds that ignore them at runtime.
      expect(threshold.type).toBe(ThresholdType.High)
      expect(threshold.filterOperator).toBe(FilterOperator.Or)
      expect(threshold.relaxed).toBe(false)
      expect(threshold.rearm).toBe('0')
      expect(threshold.trigger).toBe('1')
      expect(threshold.dsName).toBe('')
      expect(threshold.resourceFilters).toEqual([])
    })

    test('produces an expression with the same base defaults', () => {
      const expression = getDefaultExpression()

      expect(expression.expression).toBe('')
      expect(expression.trigger).toBe('1')
      expect(expression).not.toHaveProperty('dsName')
    })

    test('gives every default its own arrays', () => {
      const first = getDefaultThreshold()
      first.resourceFilters.push(getDefaultResourceFilter())

      expect(getDefaultThreshold().resourceFilters).toEqual([])
    })
  })

  describe('definition CRUD', () => {
    beforeEach(() => {
      const store = useThresholdGroupStore()
      store.currentGroup = group({ thresholds: [{ ...getDefaultThreshold(), dsName: 'original' }], expressions: [] })
      // Stop at the write: these tests look at what would be sent.
      vi.mocked(API.updateThresholdGroup).mockResolvedValue(fail('stop here') as never)
    })

    const sent = (call = 0) => vi.mocked(API.updateThresholdGroup).mock.calls[call][1]

    test('appends when the index is null and replaces when it is not', async () => {
      const store = useThresholdGroupStore()

      await store.saveDefinition(ThresholdDefinitionKind.Threshold, null, { ...getDefaultThreshold(), dsName: 'added' })
      expect(sent(0).thresholds.map(t => t.dsName)).toEqual(['original', 'added'])

      await store.saveDefinition(ThresholdDefinitionKind.Threshold, 0, { ...getDefaultThreshold(), dsName: 'replaced' })
      expect(sent(1).thresholds.map(t => t.dsName)).toEqual(['replaced'])
    })

    test('appends rather than throwing when the index is out of range', async () => {
      const store = useThresholdGroupStore()

      await store.saveDefinition(ThresholdDefinitionKind.Threshold, 99, { ...getDefaultThreshold(), dsName: 'x' })

      expect(sent().thresholds).toHaveLength(2)
    })

    test('routes expressions to the expression list', async () => {
      const store = useThresholdGroupStore()

      await store.saveDefinition(ThresholdDefinitionKind.Expression, null, getDefaultExpression())

      expect(sent().expressions).toHaveLength(1)
      expect(sent().thresholds).toHaveLength(1)
    })

    test('removes by index and refuses an index that is not there', async () => {
      const store = useThresholdGroupStore()

      expect((await store.deleteDefinition(ThresholdDefinitionKind.Threshold, 5)).success).toBe(false)
      expect(API.updateThresholdGroup).not.toHaveBeenCalled()

      await store.deleteDefinition(ThresholdDefinitionKind.Threshold, 0)
      expect(sent().thresholds).toEqual([])
    })

    test('leaves the loaded group untouched when a save fails', async () => {
      const store = useThresholdGroupStore()

      const added = await store.saveDefinition(ThresholdDefinitionKind.Threshold, null, getDefaultThreshold())
      const edited = await store.saveDefinition(ThresholdDefinitionKind.Threshold, 0, { ...getDefaultThreshold(), dsName: 'x' })
      const deleted = await store.deleteDefinition(ThresholdDefinitionKind.Threshold, 0)

      expect([added.success, edited.success, deleted.success]).toEqual([false, false, false])
      expect(store.currentGroup?.thresholds.map(t => t.dsName)).toEqual(['original'])
      // Not a conflict, so there is nothing newer to load.
      expect(API.getThresholdGroup).not.toHaveBeenCalled()
    })
  })

  describe('resource filter ordering', () => {
    const filters = (): ResourceFilter[] => [
      { field: 'first', content: '.*' },
      { field: 'second', content: '.*' },
      { field: 'third', content: '.*' }
    ]

    test('swaps a filter with its neighbour', () => {
      const store = useThresholdGroupStore()

      expect(store.moveResourceFilter(filters(), 1, -1).map(f => f.field)).toEqual(['second', 'first', 'third'])
      expect(store.moveResourceFilter(filters(), 1, 1).map(f => f.field)).toEqual(['first', 'third', 'second'])
    })

    test('is a no-op at both bounds', () => {
      const store = useThresholdGroupStore()

      expect(store.moveResourceFilter(filters(), 0, -1).map(f => f.field)).toEqual(['first', 'second', 'third'])
      expect(store.moveResourceFilter(filters(), 2, 1).map(f => f.field)).toEqual(['first', 'second', 'third'])
    })

    test('does not mutate the array it was given', () => {
      const store = useThresholdGroupStore()
      const original = filters()

      store.moveResourceFilter(original, 0, 1)

      expect(original.map(f => f.field)).toEqual(['first', 'second', 'third'])
    })
  })

  describe('persistence', () => {
    test('writes the whole group and refetches, so the next save has a current version', async () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group({ version: 'old' })

      vi.mocked(API.updateThresholdGroup).mockResolvedValue(ok() as never)
      vi.mocked(API.getThresholdGroup).mockResolvedValue(ok(group({ version: 'new' })) as never)
      vi.mocked(API.getThresholdGroups).mockResolvedValue(ok([]) as never)

      const result = await store.saveDefinition(ThresholdDefinitionKind.Threshold, null, getDefaultThreshold())

      expect(result.success).toBe(true)
      expect(API.updateThresholdGroup).toHaveBeenCalledWith('mib2', expect.objectContaining({ version: 'old' }))
      expect(API.getThresholdGroup).toHaveBeenCalledWith('mib2')
      // The refetched group carries the new entity tag, so a second save is not rejected with a 412.
      expect(store.currentGroup?.version).toBe('new')
    })

    test('refuses to save when no group is loaded', async () => {
      const store = useThresholdGroupStore()

      const result = await store.saveDefinition(ThresholdDefinitionKind.Threshold, null, getDefaultThreshold())

      expect(result.success).toBe(false)
      expect(API.updateThresholdGroup).not.toHaveBeenCalled()
    })

    test('loads the stored group after a 412, so the next save is not rejected too', async () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group({ version: 'stale' })

      vi.mocked(API.updateThresholdGroup).mockResolvedValue(fail('Threshold group \'mib2\' has changed since it was read.', 412) as never)
      vi.mocked(API.getThresholdGroup).mockResolvedValue(
        ok(group({ version: 'current', thresholds: [{ ...getDefaultThreshold(), dsName: 'theirs' }] })) as never)

      const result = await store.saveDefinition(ThresholdDefinitionKind.Threshold, null, getDefaultThreshold())

      expect(result.success).toBe(false)
      expect(result.message).toContain('has changed since it was read.')
      expect(result.message).toContain('The current version has been loaded')
      expect(store.currentGroup?.version).toBe('current')
      expect(store.currentGroup?.thresholds.map(t => t.dsName)).toEqual(['theirs'])
    })

    test('loads the stored group after a rename is rejected with a 412', async () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group({ version: 'stale' })

      vi.mocked(API.updateThresholdGroup).mockResolvedValue(fail('changed', 412) as never)
      vi.mocked(API.getThresholdGroup).mockResolvedValue(ok(group({ version: 'current' })) as never)

      await store.renameGroup('mib2', group({ name: 'renamed', version: 'stale' }))

      expect(API.getThresholdGroup).toHaveBeenCalledWith('mib2')
      expect(store.currentGroup?.version).toBe('current')
    })

    test('clears the loaded group when it is the one deleted', async () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group()

      vi.mocked(API.deleteThresholdGroup).mockResolvedValue(ok() as never)
      vi.mocked(API.getThresholdGroups).mockResolvedValue(ok([]) as never)

      await store.deleteGroup('mib2')

      expect(store.currentGroup).toBeNull()
    })

    test('drops the loaded group when a fetch fails, instead of showing stale data', async () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group()

      vi.mocked(API.getThresholdGroup).mockResolvedValue(fail('gone') as never)

      await store.fetchGroup('mib2')

      expect(store.currentGroup).toBeNull()
    })
  })

  describe('drawer', () => {
    test('clones the definition being edited so cancel really discards', () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group({ thresholds: [{ ...getDefaultThreshold(), dsName: 'original' }] })

      store.openDefinitionDrawer(ThresholdDefinitionKind.Threshold, CreateEditMode.Edit, 0)
      const draft = store.drawerDefinition() as Threshold
      draft.dsName = 'edited'

      expect(store.currentGroup?.thresholds[0].dsName).toBe('original')
    })

    test('hands back a fresh default in create mode', () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group()

      store.openDefinitionDrawer(ThresholdDefinitionKind.Expression, CreateEditMode.Create)

      expect(store.drawerDefinition()).toHaveProperty('expression', '')
    })

    test('resets on close', () => {
      const store = useThresholdGroupStore()

      store.openDefinitionDrawer(ThresholdDefinitionKind.Expression, CreateEditMode.Edit, 3)
      store.closeDefinitionDrawer()

      expect(store.definitionDrawer.visible).toBe(false)
      expect(store.definitionDrawer.index).toBe(-1)
    })
  })

  test('resetState clears everything', () => {
    const store = useThresholdGroupStore()
    store.currentGroup = group()
    store.groups = [{ name: 'mib2', rrdRepository: '/rrd', thresholdCount: 0, expressionCount: 0 }]
    store.dsTypes = [{ name: 'node', label: 'Node' }]

    store.resetState()

    expect(store.currentGroup).toBeNull()
    expect(store.groups).toEqual([])
    expect(store.dsTypes).toEqual([])
  })
})
