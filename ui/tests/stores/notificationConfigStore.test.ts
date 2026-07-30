import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import API from '@/services'
import { DestinationPath } from '@/types/notificationConfig'

vi.mock('@/services', () => ({
  default: {
    getNotificationConfigStatus: vi.fn(),
    setNotificationConfigStatus: vi.fn(),
    getDestinationPaths: vi.fn()
  }
}))

describe('useNotificationConfigStore', () => {
  let store: ReturnType<typeof useNotificationConfigStore>

  const mockPath: DestinationPath = {
    name: 'Email-Admin',
    'initial-delay': '0s',
    target: [{ name: 'Admin', command: ['javaEmail'] }]
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useNotificationConfigStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should start empty with unknown notifd status', () => {
      expect(store.notifdStatus).toBeNull()
      expect(store.destinationPaths).toEqual([])
    })
  })

  describe('notifd status', () => {
    it('should load the status', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue('on')

      await store.getStatus()

      expect(store.notifdStatus).toBe('on')
    })

    it('should update the status on success', async () => {
      vi.mocked(API.setNotificationConfigStatus).mockResolvedValue(true)

      const ok = await store.setStatus('on')

      expect(ok).toBe(true)
      expect(store.notifdStatus).toBe('on')
    })

    it('should keep the old status on failure', async () => {
      store.notifdStatus = 'off'
      vi.mocked(API.setNotificationConfigStatus).mockResolvedValue(false)

      const ok = await store.setStatus('on')

      expect(ok).toBe(false)
      expect(store.notifdStatus).toBe('off')
    })
  })

  describe('populate', () => {
    it('should load everything the dialog needs', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue('off')
      vi.mocked(API.getDestinationPaths).mockResolvedValue([mockPath])

      await store.populate()

      expect(store.notifdStatus).toBe('off')
      expect(store.destinationPaths).toEqual([mockPath])
    })
  })
})
