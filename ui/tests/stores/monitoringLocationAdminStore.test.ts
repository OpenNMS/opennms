import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import API from '@/services'
import { MonitoringLocation } from '@/types'

vi.mock('@/services', () => ({
  default: {
    listMonitoringLocations: vi.fn(),
    createMonitoringLocation: vi.fn(),
    updateMonitoringLocation: vi.fn(),
    deleteMonitoringLocation: vi.fn(),
    getNodeCountByLocation: vi.fn()
  }
}))

const loc = (name: string): MonitoringLocation => ({
  'location-name': name,
  'monitoring-area': name,
  name,
  area: name,
  geolocation: null,
  latitude: 0,
  longitude: 0,
  priority: 100,
  tags: []
})
const ok = { success: true, message: '' }
const failed = (message: string) => ({ success: false, message })

describe('useMonitoringLocationAdminStore', () => {
  let store: ReturnType<typeof useMonitoringLocationAdminStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useMonitoringLocationAdminStore()
    vi.clearAllMocks()
  })

  it('starts empty and idle', () => {
    expect(store.locations).toEqual([])
    expect(store.loading).toBe(false)
  })

  it('getLocations loads on success and preserves the list on failure', async () => {
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('Default')], totalCount: 1 })
    expect(await store.getLocations()).toBe(true)
    expect(store.locations).toEqual([loc('Default')])
    expect(store.loadError).toBe(false)

    vi.mocked(API.listMonitoringLocations).mockResolvedValue(null)
    expect(await store.getLocations()).toBe(false)
    expect(store.locations).toEqual([loc('Default')])
    expect(store.loadError).toBe(true)
  })

  it('getLocations sets loading around the fetch, also when it fails', async () => {
    let release!: (value: null) => void
    vi.mocked(API.listMonitoringLocations).mockReturnValue(new Promise((resolve) => {
      release = resolve
    }))
    const pending = store.getLocations()
    expect(store.loading).toBe(true)
    release(null)
    await pending
    expect(store.loading).toBe(false)
  })

  it('createLocation bubbles the result and refreshes only on success', async () => {
    vi.mocked(API.createMonitoringLocation).mockResolvedValue(ok)
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 1 })
    expect(await store.createLocation(loc('A'))).toEqual(ok)
    expect(API.listMonitoringLocations).toHaveBeenCalledTimes(1)

    vi.clearAllMocks()
    vi.mocked(API.createMonitoringLocation).mockResolvedValue(failed('boom'))
    expect(await store.createLocation(loc('A'))).toEqual({ success: false, message: 'boom' })
    expect(API.listMonitoringLocations).not.toHaveBeenCalled()
  })

  it('updateLocation passes the payload through and refreshes', async () => {
    vi.mocked(API.updateMonitoringLocation).mockResolvedValue(ok)
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 1 })
    const payload = loc('A')
    expect(await store.updateLocation(payload)).toEqual(ok)
    expect(API.updateMonitoringLocation).toHaveBeenCalledWith(payload)
    expect(API.listMonitoringLocations).toHaveBeenCalledTimes(1)
  })

  it('deleteLocation refreshes on success and bubbles a failure untouched', async () => {
    vi.mocked(API.deleteMonitoringLocation).mockResolvedValue(ok)
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [], totalCount: 0 })
    expect(await store.deleteLocation('A')).toEqual(ok)
    expect(store.locations).toEqual([])

    vi.clearAllMocks()
    vi.mocked(API.deleteMonitoringLocation).mockResolvedValue(failed('nodes still assigned'))
    expect(await store.deleteLocation('A')).toEqual({ success: false, message: 'nodes still assigned' })
    expect(API.listMonitoringLocations).not.toHaveBeenCalled()
  })

  it('flags truncation when the server had more rows than were fetched', async () => {
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 5 })
    await store.getLocations()
    expect(store.truncated).toBe(true)

    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 1 })
    await store.getLocations()
    expect(store.truncated).toBe(false)
  })

  it('getLocations does not count nodes; getNodeCounts does, keeping null for a failed count', async () => {
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('Default'), loc('Raleigh')], totalCount: 2 })
    vi.mocked(API.getNodeCountByLocation).mockImplementation(async (name: string) => name === 'Default' ? 12 : null)
    await store.getLocations()
    expect(API.getNodeCountByLocation).not.toHaveBeenCalled()
    expect(store.nodeCounts).toEqual({})

    await store.getNodeCounts()
    expect(API.getNodeCountByLocation).toHaveBeenCalledTimes(2)
    expect(store.nodeCounts).toEqual({ Default: 12, Raleigh: null })
  })

  it('getNodeCounts replaces the previous counts', async () => {
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('Default')], totalCount: 1 })
    vi.mocked(API.getNodeCountByLocation).mockResolvedValue(1)
    await store.getLocations()
    await store.getNodeCounts()
    expect(store.nodeCounts).toEqual({ Default: 1 })
    vi.mocked(API.getNodeCountByLocation).mockResolvedValue(3)
    await store.getNodeCounts()
    expect(store.nodeCounts).toEqual({ Default: 3 })
  })
})
