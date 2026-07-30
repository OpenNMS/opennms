import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import API from '@/services'
import { DestinationPath, PathOutage } from '@/types/notificationConfig'

vi.mock('@/services', () => ({
  default: {
    getNotificationConfigStatus: vi.fn(),
    setNotificationConfigStatus: vi.fn(),
    getDestinationPaths: vi.fn(),
    getPathOutages: vi.fn(),
    previewPathOutageRule: vi.fn(),
    applyPathOutage: vi.fn(),
    deletePathOutage: vi.fn()
  }
}))

describe('useNotificationConfigStore', () => {
  let store: ReturnType<typeof useNotificationConfigStore>

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
      expect(store.destinationPaths).toEqual([])
      expect(store.pathOutages).toEqual([])
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

  describe('path outages', () => {
    it('should load path outages', async () => {
      vi.mocked(API.getPathOutages).mockResolvedValue(mockPathOutages)

      await store.getPathOutages()

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

  describe('populate', () => {
    it('should load everything the dialog needs', async () => {
      vi.mocked(API.getNotificationConfigStatus).mockResolvedValue('off')
      vi.mocked(API.getDestinationPaths).mockResolvedValue([mockPath])
      vi.mocked(API.getPathOutages).mockResolvedValue(mockPathOutages)

      await store.populate()

      expect(store.notifdStatus).toBe('off')
      expect(store.destinationPaths).toEqual([mockPath])
      expect(store.pathOutages).toEqual(mockPathOutages)
    })
  })
})
