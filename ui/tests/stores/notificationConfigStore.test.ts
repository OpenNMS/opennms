import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import API from '@/services'
import { DestinationPath } from '@/types/notificationConfig'

vi.mock('@/services', () => ({
  default: {
    getNotificationConfigStatus: vi.fn(),
    setNotificationConfigStatus: vi.fn(),
    getDestinationPaths: vi.fn(),
    addDestinationPath: vi.fn(),
    updateDestinationPath: vi.fn(),
    deleteDestinationPath: vi.fn(),
    testDestinationPath: vi.fn(),
    getNotificationCommands: vi.fn(),
    getNotificationUsers: vi.fn(),
    getNotificationGroups: vi.fn(),
    getOnCallRoles: vi.fn()
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
      expect(store.commands).toEqual([])
    })
  })

  describe('notifd status', () => {
    it('should load the status', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue('on')

      const ok = await store.getStatus()

      expect(ok).toBe(true)
      expect(store.notifdStatus).toBe('on')
    })

    it('reports failure so the tab loader does not latch', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue(null)

      expect(await store.getStatus()).toBe(false)
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

  describe('destination paths', () => {
    it('should refresh after add, update and delete', async () => {
      vi.mocked(API.addDestinationPath).mockResolvedValue(true)
      vi.mocked(API.updateDestinationPath).mockResolvedValue(true)
      vi.mocked(API.deleteDestinationPath).mockResolvedValue(true)
      vi.mocked(API.getDestinationPaths).mockResolvedValue([mockPath])

      await store.addDestinationPath(mockPath)
      await store.updateDestinationPath('Email-Admin', mockPath)
      await store.deleteDestinationPath('Email-Admin')

      expect(API.getDestinationPaths).toHaveBeenCalledTimes(3)
      expect(store.destinationPaths).toEqual([mockPath])
    })

    it('should pass the original name when renaming', async () => {
      vi.mocked(API.updateDestinationPath).mockResolvedValue(true)
      vi.mocked(API.getDestinationPaths).mockResolvedValue([])

      const renamed = { ...mockPath, name: 'Email-Ops' }
      await store.updateDestinationPath('Email-Admin', renamed)

      expect(API.updateDestinationPath).toHaveBeenCalledWith('Email-Admin', renamed)
    })
  })

  describe('editor lookups', () => {
    it('should load the notification commands', async () => {
      vi.mocked(API.getNotificationCommands).mockResolvedValue([{ name: 'javaEmail' }])

      await store.getCommands()

      expect(store.commands).toEqual([{ name: 'javaEmail' }])
    })

    it('should load users, groups and roles for the target picker', async () => {
      vi.mocked(API.getNotificationUsers).mockResolvedValue(['admin'])
      vi.mocked(API.getNotificationGroups).mockResolvedValue(['Admin'])
      vi.mocked(API.getOnCallRoles).mockResolvedValue(['oncall'])

      const ok = await store.getUsersAndGroups()

      expect(ok).toBe(true)
      expect(store.users).toEqual(['admin'])
      expect(store.groups).toEqual(['Admin'])
      expect(store.roles).toEqual(['oncall'])
    })

    it('reports failure when a lookup errors so the tab loader can retry', async () => {
      vi.mocked(API.getDestinationPaths).mockResolvedValueOnce([mockPath])
      await store.getDestinationPaths()
      // a failed reload returns false and keeps the prior data
      vi.mocked(API.getDestinationPaths).mockResolvedValueOnce(null)
      expect(await store.getDestinationPaths()).toBe(false)
      expect(store.destinationPaths).toEqual([mockPath])

      // getUsersAndGroups is false if ANY of the three lookups fails
      vi.mocked(API.getNotificationUsers).mockResolvedValue(['admin'])
      vi.mocked(API.getNotificationGroups).mockResolvedValue(null)
      vi.mocked(API.getOnCallRoles).mockResolvedValue(['oncall'])
      expect(await store.getUsersAndGroups()).toBe(false)

      vi.mocked(API.getNotificationCommands).mockResolvedValueOnce(null)
      expect(await store.getCommands()).toBe(false)
    })
  })
})
