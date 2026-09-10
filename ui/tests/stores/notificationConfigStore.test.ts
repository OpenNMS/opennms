import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import API from '@/services'
import { DestinationPath, EventNotification, PathOutage } from '@/types/notificationConfig'

vi.mock('@/services', () => ({
  default: {
    getNotificationConfigStatus: vi.fn(),
    setNotificationConfigStatus: vi.fn(),
    getEventNotifications: vi.fn(),
    setEventNotificationStatus: vi.fn(),
    addEventNotification: vi.fn(),
    updateEventNotification: vi.fn(),
    deleteEventNotification: vi.fn(),
    getDestinationPaths: vi.fn(),
    addDestinationPath: vi.fn(),
    updateDestinationPath: vi.fn(),
    deleteDestinationPath: vi.fn(),
    testDestinationPath: vi.fn(),
    getNotificationCommands: vi.fn(),
    getNotificationUsers: vi.fn(),
    getNotificationGroups: vi.fn(),
    getOnCallRoles: vi.fn(),
    getPathOutages: vi.fn(),
    previewPathOutageRule: vi.fn(),
    applyPathOutage: vi.fn(),
    deletePathOutage: vi.fn()
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

  const mockPathOutages: PathOutage[] = [
    { nodeId: 1, nodeLabel: 'localhost', criticalPathIp: '192.168.1.1', criticalPathServiceName: 'ICMP' }
  ]

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
      expect(store.commands).toEqual([])
      expect(store.pathOutages).toEqual([])
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

  describe('event notifications', () => {
    it('should load event notifications', async () => {
      vi.mocked(API.getEventNotifications).mockResolvedValue(mockNotifications)

      const ok = await store.getEventNotifications()

      expect(ok).toBe(true)
      expect(store.eventNotifications).toEqual(mockNotifications)
    })

    it('reports failure so the tab loader does not latch', async () => {
      vi.mocked(API.getEventNotifications).mockResolvedValueOnce(mockNotifications)
      await store.getEventNotifications()
      // a failed reload returns false and keeps the prior data
      vi.mocked(API.getEventNotifications).mockResolvedValueOnce(null)

      expect(await store.getEventNotifications()).toBe(false)
      expect(store.eventNotifications).toEqual(mockNotifications)
    })

    it('should update the local status on a successful toggle', async () => {
      store.eventNotifications = mockNotifications.map(n => ({ ...n }))
      vi.mocked(API.setEventNotificationStatus).mockResolvedValue(true)

      const ok = await store.setEventNotificationStatus('nodeDown', 'off')

      expect(ok).toBe(true)
      expect(store.eventNotifications.find(n => n.name === 'nodeDown')?.status).toBe('off')
    })

    it('should leave the local status alone on a failed toggle', async () => {
      store.eventNotifications = mockNotifications.map(n => ({ ...n }))
      vi.mocked(API.setEventNotificationStatus).mockResolvedValue(false)

      await store.setEventNotificationStatus('nodeDown', 'off')

      expect(store.eventNotifications.find(n => n.name === 'nodeDown')?.status).toBe('on')
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

  describe('destination paths', () => {
    it('should load the destination paths for the editor picker', async () => {
      vi.mocked(API.getDestinationPaths).mockResolvedValue([mockPath])

      await store.getDestinationPaths()

      expect(store.destinationPaths).toEqual([mockPath])
    })

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

  describe('path outages', () => {
    it('should load path outages', async () => {
      vi.mocked(API.getPathOutages).mockResolvedValue(mockPathOutages)

      const ok = await store.getPathOutages()

      expect(ok).toBe(true)
      expect(store.pathOutages).toEqual(mockPathOutages)
    })

    it('should report failure and not clobber existing outages when the load errors', async () => {
      vi.mocked(API.getPathOutages).mockResolvedValueOnce(mockPathOutages)
      await store.getPathOutages()
      vi.mocked(API.getPathOutages).mockResolvedValueOnce(null)

      const ok = await store.getPathOutages()

      expect(ok).toBe(false)
      // prior data is preserved so the tab can retry instead of latching empty
      expect(store.pathOutages).toEqual(mockPathOutages)
    })

    it('should refresh after apply and delete', async () => {
      vi.mocked(API.applyPathOutage).mockResolvedValue(true)
      vi.mocked(API.deletePathOutage).mockResolvedValue(true)
      vi.mocked(API.getPathOutages).mockResolvedValue(mockPathOutages)

      await store.applyPathOutage({ rule: 'IPADDR IPLIKE *.*.*.*', criticalIp: '192.168.1.1' })
      await store.deletePathOutage(1)

      expect(API.getPathOutages).toHaveBeenCalledTimes(2)
    })

    it('should not refresh after a failed apply', async () => {
      vi.mocked(API.applyPathOutage).mockResolvedValue(false)

      await store.applyPathOutage({ rule: 'bogus rule' })

      expect(API.getPathOutages).not.toHaveBeenCalled()
    })

    it('should pass the preview through', async () => {
      const preview = { totalCount: 3, nodes: mockPathOutages }
      vi.mocked(API.previewPathOutageRule).mockResolvedValue(preview)

      const result = await store.previewPathOutageRule('IPADDR IPLIKE *.*.*.*')

      expect(result).toEqual(preview)
    })
  })
})
