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
    deleteThresholdGroup: vi.fn(),
    reloadThresholdingConfiguration: vi.fn()
  }
}))

const ok = (payload?: unknown) => ({ success: true, message: '', payload })
const fail = (message: string) => ({ success: false, message })

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
      store.currentGroup = group({ thresholds: [getDefaultThreshold()], expressions: [] })
    })

    test('appends when the index is null and replaces when it is not', () => {
      const store = useThresholdGroupStore()
      const added = { ...getDefaultThreshold(), dsName: 'added' }

      store.upsertDefinition(ThresholdDefinitionKind.Threshold, null, added)
      expect(store.currentGroup?.thresholds).toHaveLength(2)
      expect(store.currentGroup?.thresholds[1].dsName).toBe('added')

      store.upsertDefinition(ThresholdDefinitionKind.Threshold, 0, { ...getDefaultThreshold(), dsName: 'replaced' })
      expect(store.currentGroup?.thresholds).toHaveLength(2)
      expect(store.currentGroup?.thresholds[0].dsName).toBe('replaced')
    })

    test('appends rather than throwing when the index is out of range', () => {
      const store = useThresholdGroupStore()

      store.upsertDefinition(ThresholdDefinitionKind.Threshold, 99, { ...getDefaultThreshold(), dsName: 'x' })

      expect(store.currentGroup?.thresholds).toHaveLength(2)
    })

    test('routes expressions to the expression list', () => {
      const store = useThresholdGroupStore()

      store.upsertDefinition(ThresholdDefinitionKind.Expression, null, getDefaultExpression())

      expect(store.currentGroup?.expressions).toHaveLength(1)
      expect(store.currentGroup?.thresholds).toHaveLength(1)
    })

    test('removes by index and ignores an index that is not there', () => {
      const store = useThresholdGroupStore()

      store.removeDefinition(ThresholdDefinitionKind.Threshold, 5)
      expect(store.currentGroup?.thresholds).toHaveLength(1)

      store.removeDefinition(ThresholdDefinitionKind.Threshold, 0)
      expect(store.currentGroup?.thresholds).toHaveLength(0)
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
      const sent = group()
      store.currentGroup = sent

      vi.mocked(API.updateThresholdGroup).mockResolvedValue(ok() as never)
      vi.mocked(API.getThresholdGroup).mockResolvedValue(ok(group({ version: 'new' })) as never)
      vi.mocked(API.getThresholdGroups).mockResolvedValue(ok([]) as never)

      const result = await store.saveCurrentGroup()

      expect(result.success).toBe(true)
      expect(API.updateThresholdGroup).toHaveBeenCalledWith('mib2', sent)
      expect(API.getThresholdGroup).toHaveBeenCalledWith('mib2')
      // The refetched group carries the new entity tag, so a second save is not rejected with a 412.
      expect(store.currentGroup?.version).toBe('new')
    })

    test('refuses to save when no group is loaded', async () => {
      const store = useThresholdGroupStore()

      const result = await store.saveCurrentGroup()

      expect(result.success).toBe(false)
      expect(API.updateThresholdGroup).not.toHaveBeenCalled()
    })

    test('does not refetch after a failed save', async () => {
      const store = useThresholdGroupStore()
      store.currentGroup = group()

      vi.mocked(API.updateThresholdGroup).mockResolvedValue(fail('nope') as never)

      const result = await store.saveCurrentGroup()

      expect(result.success).toBe(false)
      expect(API.getThresholdGroup).not.toHaveBeenCalled()
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
