import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { NotificationConfigEditMode, useNotificationConfigStore } from '@/stores/notificationConfigStore'
import API from '@/services'
import { DestinationPath, EventNotification, PathOutage } from '@/types/notificationConfig'
import { createFailureResult, createResultWithPayload, createSuccessResponse } from '@/types/validation'

const loaded = <T>(payload: T) => createResultWithPayload(true, '', payload)
const loadFailed = (message = 'Failed to load.') => createResultWithPayload<never>(false, message)

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
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue(loaded('on'))

      const ok = await store.getStatus()

      expect(ok.success).toBe(true)
      expect(store.notifdStatus).toBe('on')
    })

    it('reports failure so the tab loader does not latch', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue(loadFailed())

      expect((await store.getStatus()).success).toBe(false)
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
      vi.mocked(API.getEventNotifications).mockResolvedValue(loaded(mockNotifications))

      const ok = await store.getEventNotifications()

      expect(ok.success).toBe(true)
      expect(store.eventNotifications).toEqual(mockNotifications)
    })

    it('reports failure so the tab loader does not latch', async () => {
      vi.mocked(API.getEventNotifications).mockResolvedValueOnce(loaded(mockNotifications))
      await store.getEventNotifications()
      // a failed reload returns false and keeps the prior data
      vi.mocked(API.getEventNotifications).mockResolvedValueOnce(loadFailed())

      expect((await store.getEventNotifications()).success).toBe(false)
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
      vi.mocked(API.addEventNotification).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getEventNotifications).mockResolvedValue(loaded(mockNotifications))

      const ok = await store.addEventNotification(mockNotifications[0])

      expect(ok.success).toBe(true)
      expect(API.getEventNotifications).toHaveBeenCalledTimes(1)
    })

    it('should refresh the list after updating and pass the original name', async () => {
      vi.mocked(API.updateEventNotification).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getEventNotifications).mockResolvedValue(loaded(mockNotifications))

      const renamed = { ...mockNotifications[0], name: 'nodeDown-renamed' }
      const ok = await store.updateEventNotification('nodeDown', renamed)

      expect(ok.success).toBe(true)
      expect(API.updateEventNotification).toHaveBeenCalledWith('nodeDown', renamed)
      expect(API.getEventNotifications).toHaveBeenCalledTimes(1)
    })

    it('should refresh the list after deleting', async () => {
      vi.mocked(API.deleteEventNotification).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getEventNotifications).mockResolvedValue(loaded([mockNotifications[1]]))

      const ok = await store.deleteEventNotification('nodeDown')

      expect(ok.success).toBe(true)
      expect(store.eventNotifications).toEqual([mockNotifications[1]])
    })

    it('should not refresh after a failed delete', async () => {
      vi.mocked(API.deleteEventNotification).mockResolvedValue(createFailureResult('rejected'))

      const ok = await store.deleteEventNotification('nodeDown')

      expect(ok.success).toBe(false)
      expect(API.getEventNotifications).not.toHaveBeenCalled()
    })
  })

  describe('destination paths', () => {
    it('should load the destination paths for the editor picker', async () => {
      vi.mocked(API.getDestinationPaths).mockResolvedValue(loaded([mockPath]))

      await store.getDestinationPaths()

      expect(store.destinationPaths).toEqual([mockPath])
    })

    it('should refresh after add, update and delete', async () => {
      vi.mocked(API.addDestinationPath).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.updateDestinationPath).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.deleteDestinationPath).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getDestinationPaths).mockResolvedValue(loaded([mockPath]))

      await store.addDestinationPath(mockPath)
      await store.updateDestinationPath('Email-Admin', mockPath)
      await store.deleteDestinationPath('Email-Admin')

      expect(API.getDestinationPaths).toHaveBeenCalledTimes(3)
      expect(store.destinationPaths).toEqual([mockPath])
    })

    it('should pass the original name when renaming', async () => {
      vi.mocked(API.updateDestinationPath).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getDestinationPaths).mockResolvedValue(loaded([]))

      const renamed = { ...mockPath, name: 'Email-Ops' }
      await store.updateDestinationPath('Email-Admin', renamed)

      expect(API.updateDestinationPath).toHaveBeenCalledWith('Email-Admin', renamed)
    })
  })

  describe('results for the UI to report', () => {
    it('passes the server reason through on a failed mutation', async () => {
      vi.mocked(API.deleteEventNotification).mockResolvedValue(createFailureResult('The last notification cannot be deleted.'))

      const result = await store.deleteEventNotification('nodeDown')

      expect(result).toEqual(expect.objectContaining({ success: false, message: 'The last notification cannot be deleted.' }))
    })

    it('reports a failed refresh after a successful save in errors', async () => {
      vi.mocked(API.addDestinationPath).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getDestinationPaths).mockResolvedValue(loadFailed('Failed to load destination paths.'))

      const result = await store.addDestinationPath(mockPath)

      expect(result.success).toBe(true)
      expect(result.errors).toEqual(['Failed to load destination paths.'])
    })

    it('reports no errors when the refresh succeeds', async () => {
      vi.mocked(API.applyPathOutage).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getPathOutages).mockResolvedValue(loaded(mockPathOutages))

      const result = await store.applyPathOutage({ rule: 'IPADDR IPLIKE *.*.*.*', criticalIp: '192.168.1.1' })

      expect(result.success).toBe(true)
      expect(result.errors).toBeUndefined()
    })

    it('getUsersAndGroups returns the first failed lookup and keeps the lists that loaded', async () => {
      vi.mocked(API.getNotificationUsers).mockResolvedValue(loaded(['admin']))
      vi.mocked(API.getNotificationGroups).mockResolvedValue(loadFailed('Failed to load groups.'))
      vi.mocked(API.getOnCallRoles).mockResolvedValue(loadFailed('Failed to load on-call roles.'))

      const result = await store.getUsersAndGroups()

      expect(result).toEqual(expect.objectContaining({ success: false, message: 'Failed to load groups.' }))
      expect(store.users).toEqual(['admin'])
    })
  })

  describe('event notification editor mode', () => {
    it('starts on the table', () => {
      expect(store.eventNotificationEditMode).toBe(NotificationConfigEditMode.Table)
      expect(store.currentEventNotification).toBeNull()
    })

    it('opens in edit mode for an existing notification and create mode for null', () => {
      store.openEventNotificationEditor(mockNotifications[0])
      expect(store.eventNotificationEditMode).toBe(NotificationConfigEditMode.Edit)
      expect(store.currentEventNotification).toEqual(mockNotifications[0])

      store.openEventNotificationEditor(null)
      expect(store.eventNotificationEditMode).toBe(NotificationConfigEditMode.Create)
      expect(store.currentEventNotification).toBeNull()
    })

    it('closes back to the table and clears the current notification', () => {
      store.openEventNotificationEditor(mockNotifications[0])
      store.closeEventNotificationEditor()

      expect(store.eventNotificationEditMode).toBe(NotificationConfigEditMode.Table)
      expect(store.currentEventNotification).toBeNull()
    })
  })

  describe('destination path editor mode', () => {
    it('starts on the table', () => {
      expect(store.destinationPathEditMode).toBe(NotificationConfigEditMode.Table)
      expect(store.currentDestinationPath).toBeNull()
    })

    it('opens in edit mode for an existing path and create mode for null', () => {
      store.openDestinationPathEditor(mockPath)
      expect(store.destinationPathEditMode).toBe(NotificationConfigEditMode.Edit)
      expect(store.currentDestinationPath).toEqual(mockPath)

      store.openDestinationPathEditor(null)
      expect(store.destinationPathEditMode).toBe(NotificationConfigEditMode.Create)
      expect(store.currentDestinationPath).toBeNull()
    })

    it('closes back to the table and clears the current path', () => {
      store.openDestinationPathEditor(mockPath)
      store.closeDestinationPathEditor()

      expect(store.destinationPathEditMode).toBe(NotificationConfigEditMode.Table)
      expect(store.currentDestinationPath).toBeNull()
    })
  })

  describe('editor lookups', () => {
    it('should load the notification commands', async () => {
      vi.mocked(API.getNotificationCommands).mockResolvedValue(loaded([{ name: 'javaEmail' }]))

      await store.getCommands()

      expect(store.commands).toEqual([{ name: 'javaEmail' }])
    })

    it('should load users, groups and roles for the target picker', async () => {
      vi.mocked(API.getNotificationUsers).mockResolvedValue(loaded(['admin']))
      vi.mocked(API.getNotificationGroups).mockResolvedValue(loaded(['Admin']))
      vi.mocked(API.getOnCallRoles).mockResolvedValue(loaded(['oncall']))

      const ok = await store.getUsersAndGroups()

      expect(ok.success).toBe(true)
      expect(store.users).toEqual(['admin'])
      expect(store.groups).toEqual(['Admin'])
      expect(store.roles).toEqual(['oncall'])
    })

    it('reports failure when a lookup errors so the tab loader can retry', async () => {
      vi.mocked(API.getDestinationPaths).mockResolvedValueOnce(loaded([mockPath]))
      await store.getDestinationPaths()
      // a failed reload returns false and keeps the prior data
      vi.mocked(API.getDestinationPaths).mockResolvedValueOnce(loadFailed())
      expect((await store.getDestinationPaths()).success).toBe(false)
      expect(store.destinationPaths).toEqual([mockPath])

      // getUsersAndGroups is false if ANY of the three lookups fails
      vi.mocked(API.getNotificationUsers).mockResolvedValue(loaded(['admin']))
      vi.mocked(API.getNotificationGroups).mockResolvedValue(loadFailed())
      vi.mocked(API.getOnCallRoles).mockResolvedValue(loaded(['oncall']))
      expect((await store.getUsersAndGroups()).success).toBe(false)

      vi.mocked(API.getNotificationCommands).mockResolvedValueOnce(loadFailed())
      expect((await store.getCommands()).success).toBe(false)
    })
  })

  describe('path outages', () => {
    it('should load path outages', async () => {
      vi.mocked(API.getPathOutages).mockResolvedValue(loaded(mockPathOutages))

      const ok = await store.getPathOutages()

      expect(ok.success).toBe(true)
      expect(store.pathOutages).toEqual(mockPathOutages)
    })

    it('should report failure and not clobber existing outages when the load errors', async () => {
      vi.mocked(API.getPathOutages).mockResolvedValueOnce(loaded(mockPathOutages))
      await store.getPathOutages()
      vi.mocked(API.getPathOutages).mockResolvedValueOnce(loadFailed())

      const ok = await store.getPathOutages()

      expect(ok.success).toBe(false)
      // prior data is preserved so the tab can retry instead of latching empty
      expect(store.pathOutages).toEqual(mockPathOutages)
    })

    it('should refresh after apply and delete', async () => {
      vi.mocked(API.applyPathOutage).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.deletePathOutage).mockResolvedValue(createSuccessResponse())
      vi.mocked(API.getPathOutages).mockResolvedValue(loaded(mockPathOutages))

      await store.applyPathOutage({ rule: 'IPADDR IPLIKE *.*.*.*', criticalIp: '192.168.1.1' })
      await store.deletePathOutage(1)

      expect(API.getPathOutages).toHaveBeenCalledTimes(2)
    })

    it('should not refresh after a failed apply', async () => {
      vi.mocked(API.applyPathOutage).mockResolvedValue(createFailureResult('rejected'))

      await store.applyPathOutage({ rule: 'bogus rule' })

      expect(API.getPathOutages).not.toHaveBeenCalled()
    })

    it('should pass the preview through', async () => {
      const preview = { totalCount: 3, nodes: mockPathOutages }
      vi.mocked(API.previewPathOutageRule).mockResolvedValue(loaded(preview))

      const result = await store.previewPathOutageRule('IPADDR IPLIKE *.*.*.*')

      expect(result.payload).toEqual(preview)
    })
  })
})
