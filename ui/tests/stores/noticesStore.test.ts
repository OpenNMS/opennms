import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNoticesStore } from '@/stores/noticesStore'
import { useAuthStore } from '@/stores/authStore'
import API from '@/services'
import { OnmsNotice } from '@/types/notices'

vi.mock('@/services', () => ({
  default: {
    browseNotices: vi.fn(),
    acknowledgeNotice: vi.fn()
  }
}))

const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

describe('useNoticesStore', () => {
  let store: ReturnType<typeof useNoticesStore>

  const mockNotices: OnmsNotice[] = [
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

  const mockResult = { notices: mockNotices, totalCount: 5 }

  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.whoAmI = { id: 'admin', fullName: 'Administrator', internal: true, roles: ['ROLE_ADMIN'] }
    store = useNoticesStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should default to the current user outstanding notices', () => {
      expect(store.preset).toBe('yourOutstanding')
      expect(store.notices).toEqual([])
      expect(store.totalCount).toBe(0)
      expect(store.first).toBe(0)
      expect(store.rows).toBe(10)
      expect(store.title).toBe('Your Outstanding Notices')
    })
  })

  describe('load', () => {
    it('should query outstanding notices for the current user', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)

      await store.load()

      expect(API.browseNotices).toHaveBeenCalledWith({
        acktype: 'unack',
        user: 'admin',
        limit: 10,
        offset: 0
      })
      expect(store.notices).toEqual(mockNotices)
      expect(store.totalCount).toBe(5)
    })
  })

  describe('applyPreset', () => {
    it('allOutstanding should drop the user filter and reset paging', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)
      store.first = 30

      await store.applyPreset('allOutstanding')

      expect(store.preset).toBe('allOutstanding')
      expect(store.first).toBe(0)
      expect(API.browseNotices).toHaveBeenCalledWith({
        acktype: 'unack',
        user: null,
        limit: 10,
        offset: 0
      })
      expect(store.title).toBe('All Outstanding Notices')
    })

    it('allAcknowledged should query acknowledged notices', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)

      await store.applyPreset('allAcknowledged')

      expect(API.browseNotices).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'ack', user: null })
      )
      expect(store.title).toBe('All Acknowledged Notices')
    })

    it('userSearch should filter outstanding notices by the given user', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)

      await store.applyPreset('userSearch', 'operator')

      expect(store.userFilter).toBe('operator')
      expect(API.browseNotices).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'unack', user: 'operator' })
      )
      expect(store.title).toBe("Outstanding Notices for 'operator'")
    })
  })

  describe('onPage', () => {
    it('should reload with the new offset and page size', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)

      await store.onPage(20, 20)

      expect(store.first).toBe(20)
      expect(store.rows).toBe(20)
      expect(API.browseNotices).toHaveBeenCalledWith(
        expect.objectContaining({ limit: 20, offset: 20 })
      )
    })
  })

  describe('acknowledge', () => {
    it('should acknowledge and reload on success', async () => {
      vi.mocked(API.acknowledgeNotice).mockResolvedValue(true)
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)

      const ok = await store.acknowledge(mockNotices[0])

      expect(ok).toBe(true)
      expect(API.acknowledgeNotice).toHaveBeenCalledWith(1, true)
      expect(API.browseNotices).toHaveBeenCalledTimes(1)
    })

    it('should refuse user-scoped queries without a user id', async () => {
      const authStore = useAuthStore()
      authStore.whoAmI = { id: '', fullName: '', internal: true, roles: [] }

      await store.load()

      expect(API.browseNotices).not.toHaveBeenCalled()
      expect(store.notices).toEqual([])
      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })

    it('should clamp to the last valid page when the ack empties the current page', async () => {
      // land far past the end (offset 40), then ack away the last row there;
      // 25 remaining rows at 10/page puts the last valid page at offset 20
      vi.mocked(API.browseNotices).mockResolvedValue({ notices: mockNotices, totalCount: 41 })
      await store.onPage(40, 10)
      vi.mocked(API.acknowledgeNotice).mockResolvedValue(true)
      vi.mocked(API.browseNotices)
        .mockResolvedValueOnce({ notices: [], totalCount: 25 })
        .mockResolvedValueOnce({ notices: mockNotices, totalCount: 25 })

      await store.acknowledge(mockNotices[0])

      expect(store.first).toBe(20)
      expect(store.notices).toEqual(mockNotices)
    })

    it('should rewind a page when the ack empties the current page', async () => {
      // land on page 2 (offset 10) with one row, then ack it away
      vi.mocked(API.browseNotices).mockResolvedValue({ notices: mockNotices, totalCount: 11 })
      await store.onPage(10, 10)
      vi.mocked(API.acknowledgeNotice).mockResolvedValue(true)
      vi.mocked(API.browseNotices)
        .mockResolvedValueOnce({ notices: [], totalCount: 10 })
        .mockResolvedValueOnce({ notices: mockNotices, totalCount: 10 })

      await store.acknowledge(mockNotices[0])

      expect(store.first).toBe(0)
      expect(store.notices).toEqual(mockNotices)
    })

    it('should not reload when acknowledging fails', async () => {
      vi.mocked(API.acknowledgeNotice).mockResolvedValue(false)

      const ok = await store.acknowledge(mockNotices[0])

      expect(ok).toBe(false)
      expect(API.browseNotices).not.toHaveBeenCalled()
    })
  })

  describe('fetchForExport', () => {
    it('scopes the export to the current user without widening', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)

      const result = await store.fetchForExport(1000)

      expect(API.browseNotices).toHaveBeenCalledWith({
        acktype: 'unack',
        user: 'admin',
        limit: 1000,
        offset: 0
      })
      expect(result).toEqual(mockResult)
    })

    it('refuses to export (no query) when a user-scoped preset has no user id', async () => {
      const authStore = useAuthStore()
      authStore.whoAmI = { id: '', fullName: '', internal: true, roles: [] }

      const result = await store.fetchForExport(1000)

      expect(API.browseNotices).not.toHaveBeenCalled()
      expect(result).toEqual({ notices: [], totalCount: 0 })
      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })

    it('exports all outstanding notices with no user filter for a non-scoped preset', async () => {
      vi.mocked(API.browseNotices).mockResolvedValue(mockResult)
      await store.applyPreset('allOutstanding')
      vi.clearAllMocks()

      await store.fetchForExport(500)

      expect(API.browseNotices).toHaveBeenCalledWith(
        expect.objectContaining({ acktype: 'unack', user: null, limit: 500 })
      )
    })
  })
})
