import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import API from '@/services'
import { DestinationPath, EventNotification } from '@/types/notificationConfig'

vi.mock('@/services', () => ({
  default: {
    getNotificationConfigStatus: vi.fn(),
    setNotificationConfigStatus: vi.fn(),
    getEventNotifications: vi.fn(),
    setEventNotificationStatus: vi.fn(),
    addEventNotification: vi.fn(),
    updateEventNotification: vi.fn(),
    deleteEventNotification: vi.fn(),
    getDestinationPaths: vi.fn()
  }
}))

describe('useNotificationConfigStore', () => {
  let store: ReturnType<typeof useNotificationConfigStore>

  const mockNotifications: EventNotification[] = [
    {
      name: 'nodeDown',
      status: 'on',
      uei: 'uei.opennms.org/nodes/nodeDown',
      destinationPath: 'Email-Admin'
    },
    {
      name: 'High Threshold',
      status: 'off',
      uei: 'uei.opennms.org/threshold/highThresholdExceeded',
      destinationPath: 'Email-Admin'
    }
  ]

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
      expect(store.eventNotifications).toEqual([])
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

  describe('event notifications', () => {
    it('should load event notifications', async () => {
      vi.mocked(API.getEventNotifications).mockResolvedValue(mockNotifications)

      await store.getEventNotifications()

      expect(store.eventNotifications).toEqual(mockNotifications)
    })

    it('should update the local status on a successful toggle', async () => {
      store.eventNotifications = mockNotifications.map((n) => ({ ...n }))
      vi.mocked(API.setEventNotificationStatus).mockResolvedValue(true)

      const ok = await store.setEventNotificationStatus('nodeDown', 'off')

      expect(ok).toBe(true)
      expect(store.eventNotifications.find((n) => n.name === 'nodeDown')?.status).toBe('off')
    })

    it('should leave the local status alone on a failed toggle', async () => {
      store.eventNotifications = mockNotifications.map((n) => ({ ...n }))
      vi.mocked(API.setEventNotificationStatus).mockResolvedValue(false)

      await store.setEventNotificationStatus('nodeDown', 'off')

      expect(store.eventNotifications.find((n) => n.name === 'nodeDown')?.status).toBe('on')
    })

    it('should refresh the list after adding', async () => {
      vi.mocked(API.addEventNotification).mockResolvedValue(true)
      vi.mocked(API.getEventNotifications).mockResolvedValue(mockNotifications)

      const ok = await store.addEventNotification(mockNotifications[0])

      expect(ok).toBe(true)
      expect(API.getEventNotifications).toHaveBeenCalledTimes(1)
    })

    it('should refresh the list after updating and pass the original name', async () => {
      vi.mocked(API.updateEventNotification).mockResolvedValue(true)
      vi.mocked(API.getEventNotifications).mockResolvedValue(mockNotifications)

      const renamed = { ...mockNotifications[0], name: 'nodeDown-renamed' }
      const ok = await store.updateEventNotification('nodeDown', renamed)

      expect(ok).toBe(true)
      expect(API.updateEventNotification).toHaveBeenCalledWith('nodeDown', renamed)
      expect(API.getEventNotifications).toHaveBeenCalledTimes(1)
    })

    it('should refresh the list after deleting', async () => {
      vi.mocked(API.deleteEventNotification).mockResolvedValue(true)
      vi.mocked(API.getEventNotifications).mockResolvedValue([mockNotifications[1]])

      const ok = await store.deleteEventNotification('nodeDown')

      expect(ok).toBe(true)
      expect(store.eventNotifications).toEqual([mockNotifications[1]])
    })

    it('should not refresh after a failed delete', async () => {
      vi.mocked(API.deleteEventNotification).mockResolvedValue(false)

      const ok = await store.deleteEventNotification('nodeDown')

      expect(ok).toBe(false)
      expect(API.getEventNotifications).not.toHaveBeenCalled()
    })
  })

  describe('populate', () => {
    it('should load everything the dialog needs', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue('off')
      vi.mocked(API.getEventNotifications).mockResolvedValue(mockNotifications)
      vi.mocked(API.getDestinationPaths).mockResolvedValue([mockPath])

      await store.populate()

      expect(store.notifdStatus).toBe('off')
      expect(store.eventNotifications).toEqual(mockNotifications)
      expect(store.destinationPaths).toEqual([mockPath])
    })
  })
})
