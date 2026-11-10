import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { useAuthStore } from '@/stores/authStore'
import API from '@/services'
import { OnmsNotification } from '@/types/notifications'
import { createResultWithPayload } from '@/types/validation'

vi.mock('@/services', () => ({
  default: {
    browseNotifications: vi.fn(),
    acknowledgeNotification: vi.fn(),
    getNodeById: vi.fn()
  }
}))

const loaded = <T>(payload: T) => createResultWithPayload(true, '', payload)

describe('useNotificationsStore', () => {
  let store: ReturnType<typeof useNotificationsStore>

  const mockNotifications: OnmsNotification[] = [
    {
      id: 1,
      subject: 'Notice #1: node down',
      severity: 'MAJOR',
      pageTime: 1785327487410,
      nodeId: 16,
      nodeLabel: 'Core-Router-01'
    },
    {
      id: 2,
      subject: 'Notice #2: interface down',
      severity: 'MAJOR',
      pageTime: 1785327487000,
      ipAddress: '10.0.0.5'
    }
  ]

  const mockResult = { notifications: mockNotifications, totalCount: 5 }

  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.whoAmI = { id: 'admin', fullName: 'Administrator', internal: true, roles: ['ROLE_ADMIN'] }
    store = useNotificationsStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should default to the current user outstanding notifications', () => {
      expect(store.preset).toBe('yourOutstanding')
      expect(store.notifications).toEqual([])
      expect(store.totalCount).toBe(0)
      expect(store.first).toBe(0)
      expect(store.rows).toBe(10)
      expect(store.title).toBe('Your Outstanding Notifications')
    })
  })

  describe('load', () => {
    // resolve browseNotifications calls by hand, in any order
    const deferredBrowse = () => {
      const pending: Array<(value: unknown) => void> = []
      vi.mocked(API.browseNotifications).mockImplementation(() => new Promise(resolve => pending.push(resolve as (value: unknown) => void)) as never)
      return pending
    }
    const acked = [{ ...mockNotifications[0], id: 99, ackUser: 'admin' }]

    it('drops a slower earlier response that lands after a newer load', async () => {
      const pending = deferredBrowse()

      const first = store.applyPreset('allAcknowledged')
      const second = store.applyPreset('yourOutstanding')
      // the newer request answers first, then the older one arrives late
      pending[1](loaded(mockResult))
      await second
      pending[0](loaded({ notifications: acked, totalCount: 1 }))
      await first

      expect(store.title).toBe('Your Outstanding Notifications')
      expect(store.notifications).toEqual(mockNotifications)
      expect(store.totalCount).toBe(5)
      expect(store.loading).toBe(false)
    })

    it('a stale response cannot overwrite a load that returned without querying', async () => {
      const pending = deferredBrowse()

      const first = store.applyPreset('allAcknowledged')
      await store.applyPreset('userSearch') // awaiting a user: clears the table, no request
      pending[0](loaded({ notifications: acked, totalCount: 1 }))
      await first

      expect(store.notifications).toEqual([])
      expect(store.totalCount).toBe(0)
      expect(store.loading).toBe(false)
    })

    it('keeps loading true until the latest request finishes', async () => {
      const pending = deferredBrowse()

      const first = store.load()
      const second = store.load()
      pending[0](loaded(mockResult))
      await first
      expect(store.loading).toBe(true)

      pending[1](loaded(mockResult))
      await second
      expect(store.loading).toBe(false)
    })

    it('should query outstanding notifications for the current user', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      await store.load()

      expect(API.browseNotifications).toHaveBeenCalledWith({
        acktype: 'unack',
        user: 'admin',
        excludeUser: null,
        nodeId: null,
        limit: 10,
        offset: 0
      })
      expect(store.notifications).toEqual(mockNotifications)
      expect(store.totalCount).toBe(5)
    })
  })

  describe('applyPreset', () => {
    it('teamOutstanding should exclude the current user', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      await store.applyPreset('teamOutstanding')

      expect(API.browseNotifications).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'unack', user: null, excludeUser: 'admin' })
      )
    })

    it('allOutstanding should drop the user filter and reset paging', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))
      store.first = 30

      await store.applyPreset('allOutstanding')

      expect(store.preset).toBe('allOutstanding')
      expect(store.first).toBe(0)
      expect(API.browseNotifications).toHaveBeenCalledWith({
        acktype: 'unack',
        user: null,
        excludeUser: null,
        nodeId: null,
        limit: 10,
        offset: 0
      })
      expect(store.title).toBe('All Outstanding Notifications')
    })

    it('allAcknowledged should query acknowledged notifications', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      await store.applyPreset('allAcknowledged')

      expect(API.browseNotifications).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'ack', user: null })
      )
      expect(store.title).toBe('All Acknowledged Notifications')
    })

    it('userSearch should filter outstanding notifications by the given user', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      await store.applyPreset('userSearch', 'operator')

      expect(store.userFilter).toBe('operator')
      expect(API.browseNotifications).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'unack', user: 'operator' })
      )
      expect(store.title).toBe('Outstanding Notifications for \'operator\'')
    })

    it('userSearch with no user waits for one instead of querying', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))
      await store.applyPreset('userSearch', 'operator')
      vi.mocked(API.browseNotifications).mockClear()

      const result = await store.applyPreset('userSearch')

      expect(store.awaitingUser).toBe(true)
      expect(API.browseNotifications).not.toHaveBeenCalled()
      // nothing to query is not a failure
      expect(result.success).toBe(true)
      expect(store.notifications).toEqual([])
      expect(store.totalCount).toBe(0)
      expect(store.title).toBe('Outstanding Notifications for User')
    })
  })

  describe('onPage', () => {
    it('should reload with the new offset and page size', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      await store.onPage(20, 20)

      expect(store.first).toBe(20)
      expect(store.rows).toBe(20)
      expect(API.browseNotifications).toHaveBeenCalledWith(
        expect.objectContaining({ limit: 20, offset: 20 })
      )
    })
  })

  describe('acknowledge', () => {
    it('should acknowledge and reload on success', async () => {
      vi.mocked(API.acknowledgeNotification).mockResolvedValue(true)
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      const result = await store.acknowledge(mockNotifications[0])

      expect(result.success).toBe(true)
      expect(result.errors).toBeUndefined()
      expect(API.acknowledgeNotification).toHaveBeenCalledWith(1, true)
      expect(API.browseNotifications).toHaveBeenCalledTimes(1)
    })

    it('should refuse user-scoped queries without a user id', async () => {
      const authStore = useAuthStore()
      authStore.whoAmI = { id: '', fullName: '', internal: true, roles: [] }

      const result = await store.load()

      expect(API.browseNotifications).not.toHaveBeenCalled()
      expect(store.notifications).toEqual([])
      expect(result.success).toBe(false)
      expect(result.message).toMatch(/Cannot determine the current user/)

      await store.applyPreset('teamOutstanding')
      expect(API.browseNotifications).not.toHaveBeenCalled()
    })

    it('should clamp to the last valid page when the ack empties the current page', async () => {
      // land far past the end (offset 40), then ack away the last row there;
      // 25 remaining rows at 10/page puts the last valid page at offset 20
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded({ notifications: mockNotifications, totalCount: 41 }))
      await store.onPage(40, 10)
      vi.mocked(API.acknowledgeNotification).mockResolvedValue(true)
      vi.mocked(API.browseNotifications)
        .mockResolvedValueOnce(loaded({ notifications: [], totalCount: 25 }))
        .mockResolvedValueOnce(loaded({ notifications: mockNotifications, totalCount: 25 }))

      await store.acknowledge(mockNotifications[0])

      expect(store.first).toBe(20)
      expect(store.notifications).toEqual(mockNotifications)
    })

    it('should rewind a page when the ack empties the current page', async () => {
      // land on page 2 (offset 10) with one row, then ack it away
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded({ notifications: mockNotifications, totalCount: 11 }))
      await store.onPage(10, 10)
      vi.mocked(API.acknowledgeNotification).mockResolvedValue(true)
      vi.mocked(API.browseNotifications)
        .mockResolvedValueOnce(loaded({ notifications: [], totalCount: 10 }))
        .mockResolvedValueOnce(loaded({ notifications: mockNotifications, totalCount: 10 }))

      await store.acknowledge(mockNotifications[0])

      expect(store.first).toBe(0)
      expect(store.notifications).toEqual(mockNotifications)
    })

    it('should not reload when acknowledging fails', async () => {
      vi.mocked(API.acknowledgeNotification).mockResolvedValue(false)

      const result = await store.acknowledge(mockNotifications[0])

      expect(result).toEqual(expect.objectContaining({ success: false, message: 'Failed to acknowledge notification 1.' }))
      expect(API.browseNotifications).not.toHaveBeenCalled()
    })

    it('reports a failed reload after a successful acknowledge in errors', async () => {
      vi.mocked(API.acknowledgeNotification).mockResolvedValue(true)
      vi.mocked(API.browseNotifications).mockResolvedValue(createResultWithPayload(false, 'Failed to load notifications.'))

      const result = await store.acknowledge(mockNotifications[0])

      expect(result.success).toBe(true)
      expect(result.errors).toEqual(['Failed to load notifications.'])
    })
  })

  describe('fetchForExport', () => {
    it('returns nothing without querying while awaiting a user', async () => {
      await store.applyPreset('userSearch')

      const result = await store.fetchForExport(100)

      expect(API.browseNotifications).not.toHaveBeenCalled()
      expect(result.success).toBe(true)
      expect(result.payload).toEqual({ notifications: [], totalCount: 0 })
    })

    it('scopes the export to the current user without widening', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      const result = await store.fetchForExport(1000)

      expect(API.browseNotifications).toHaveBeenCalledWith({
        acktype: 'unack',
        user: 'admin',
        excludeUser: null,
        nodeId: null,
        limit: 1000,
        offset: 0
      })
      expect(result.payload).toEqual(mockResult)
    })

    it('carries excludeUser for a teamOutstanding export so it stays scoped', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))
      await store.applyPreset('teamOutstanding')
      vi.mocked(API.browseNotifications).mockClear()

      await store.fetchForExport(1000)

      expect(API.browseNotifications).toHaveBeenCalledWith(
        expect.objectContaining({ user: null, excludeUser: 'admin', limit: 1000 })
      )
    })

    it('refuses to export (no query) when a user-scoped preset has no user id', async () => {
      const authStore = useAuthStore()
      authStore.whoAmI = { id: '', fullName: '', internal: true, roles: [] }

      const result = await store.fetchForExport(1000)

      expect(API.browseNotifications).not.toHaveBeenCalled()
      expect(result.success).toBe(false)
      expect(result.message).toMatch(/nothing to export/)
    })

    it('exports all outstanding notifications with no user filter for a non-scoped preset', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))
      await store.applyPreset('allOutstanding')
      vi.clearAllMocks()

      await store.fetchForExport(500)

      expect(API.browseNotifications).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'unack', user: null, limit: 500 })
      )
    })
  })

  describe('node filter', () => {
    beforeEach(() => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))
      vi.mocked(API.getNodeById).mockResolvedValue({ id: '16', label: 'Core-Router-01' })
    })

    it('narrows a preset to one node and names it in the title', async () => {
      await store.applyQuery('allOutstanding', '16')

      expect(API.browseNotifications).toHaveBeenCalledWith(expect.objectContaining({ acktype: 'unack', user: null, nodeId: '16' }))
      expect(store.nodeFilter).toEqual({ id: '16', label: 'Core-Router-01' })
      expect(store.title).toBe('All Outstanding Notifications for Node Core-Router-01')
    })

    it('falls back to the node id when the label lookup fails, and still filters', async () => {
      vi.mocked(API.getNodeById).mockResolvedValue(false)

      await store.applyQuery('allOutstanding', '99')

      expect(API.browseNotifications).toHaveBeenCalledWith(expect.objectContaining({ nodeId: '99' }))
      expect(store.title).toBe('All Outstanding Notifications for Node 99')
    })

    it('keeps the node filter when the preset changes', async () => {
      await store.applyQuery('allOutstanding', '16')
      await store.applyPreset('allAcknowledged')

      expect(API.browseNotifications).toHaveBeenLastCalledWith(expect.objectContaining({ acktype: 'ack', nodeId: '16' }))
    })

    it('clears the node filter and reloads', async () => {
      await store.applyQuery('allOutstanding', '16')
      await store.clearNodeFilter()

      expect(store.nodeFilter).toBeNull()
      expect(API.browseNotifications).toHaveBeenLastCalledWith(expect.objectContaining({ nodeId: null }))
    })

    it('sets a node filter on the current preset and reloads from the first page', async () => {
      await store.applyPreset('allAcknowledged')
      await store.onPage(20, 10)

      await store.setNodeFilter('16')

      expect(store.preset).toBe('allAcknowledged')
      expect(store.first).toBe(0)
      expect(API.browseNotifications).toHaveBeenLastCalledWith(expect.objectContaining({ acktype: 'ack', nodeId: '16', offset: 0 }))
      expect(store.nodeFilter).toEqual({ id: '16', label: 'Core-Router-01' })
    })

    it('keeps the user search when a node filter is set', async () => {
      await store.applyPreset('userSearch', 'operator')

      await store.setNodeFilter('16')

      expect(store.preset).toBe('userSearch')
      expect(store.userFilter).toBe('operator')
      expect(API.browseNotifications).toHaveBeenLastCalledWith(expect.objectContaining({ user: 'operator', nodeId: '16' }))
    })

    it('replaces the node filter, dropping the old label, and clears it with null', async () => {
      vi.mocked(API.getNodeById).mockResolvedValue({ id: '17', label: 'Other' })
      await store.setNodeFilter('16')

      const pending = store.setNodeFilter('17')
      expect(store.nodeFilter).toEqual({ id: '17', label: null })
      await pending
      expect(store.nodeFilter).toEqual({ id: '17', label: 'Other' })

      await store.setNodeFilter(null)
      expect(store.nodeFilter).toBeNull()
      expect(API.browseNotifications).toHaveBeenLastCalledWith(expect.objectContaining({ nodeId: null }))
    })

    it('looks the node label up once when the same node is applied twice', async () => {
      await store.setNodeFilter('16')
      await store.setNodeFilter('16')

      expect(API.getNodeById).toHaveBeenCalledTimes(1)
      expect(store.nodeFilter).toEqual({ id: '16', label: 'Core-Router-01' })
    })

    it('looks the node label up again when a different node is applied', async () => {
      await store.setNodeFilter('16')
      vi.mocked(API.getNodeById).mockResolvedValue({ id: '17', label: 'Other' })
      await store.setNodeFilter('17')

      expect(API.getNodeById).toHaveBeenCalledTimes(2)
      expect(API.getNodeById).toHaveBeenLastCalledWith('17')
    })

    it('looks the node label up again when the first lookup failed', async () => {
      vi.mocked(API.getNodeById).mockResolvedValueOnce(false)
      await store.setNodeFilter('16')
      expect(store.nodeFilter).toEqual({ id: '16', label: null })

      await store.setNodeFilter('16')

      expect(API.getNodeById).toHaveBeenCalledTimes(2)
      expect(store.nodeFilter).toEqual({ id: '16', label: 'Core-Router-01' })
    })

    it('passes the node filter to the export as well', async () => {
      await store.applyQuery('allOutstanding', '16')
      await store.fetchForExport(500)

      expect(API.browseNotifications).toHaveBeenLastCalledWith(expect.objectContaining({ nodeId: '16', limit: 500 }))
    })
  })

  describe('yourAcknowledged', () => {
    it('asks for the current user acknowledged notifications', async () => {
      vi.mocked(API.browseNotifications).mockResolvedValue(loaded(mockResult))

      await store.applyPreset('yourAcknowledged')

      expect(API.browseNotifications).toHaveBeenCalledWith(expect.objectContaining({ acktype: 'ack', user: 'admin', excludeUser: null }))
      expect(store.title).toBe('Your Acknowledged Notifications')
    })

    it('refuses rather than widening when the current user is unknown', async () => {
      useAuthStore().whoAmI = { id: '', fullName: '', internal: true, roles: [] } as never

      const result = await store.applyPreset('yourAcknowledged')

      expect(result.success).toBe(false)
      expect(API.browseNotifications).not.toHaveBeenCalled()
    })
  })
})
