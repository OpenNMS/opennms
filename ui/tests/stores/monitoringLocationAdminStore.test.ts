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
    deleteMonitoringLocation: vi.fn()
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

describe('useMonitoringLocationAdminStore', () => {
  let store: ReturnType<typeof useMonitoringLocationAdminStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useMonitoringLocationAdminStore()
    vi.clearAllMocks()
  })

  it('starts empty', () => {
    expect(store.locations).toEqual([])
  })

  it('getLocations loads on success and preserves the list on failure', async () => {
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('Default')], totalCount: 1 })
    await store.getLocations()
    expect(store.locations).toEqual([loc('Default')])
    expect(store.loadError).toBe(false)

    vi.mocked(API.listMonitoringLocations).mockResolvedValue(null)
    await store.getLocations()
    expect(store.locations).toEqual([loc('Default')])
    expect(store.loadError).toBe(true)
  })

  it('createLocation refreshes on success and not on failure', async () => {
    vi.mocked(API.createMonitoringLocation).mockResolvedValue(null)
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 1 })
    expect(await store.createLocation(loc('A'))).toBeNull()
    expect(API.listMonitoringLocations).toHaveBeenCalledTimes(1)

    vi.clearAllMocks()
    vi.mocked(API.createMonitoringLocation).mockResolvedValue('boom')
    expect(await store.createLocation(loc('A'))).toBe('boom')
    expect(API.listMonitoringLocations).not.toHaveBeenCalled()
  })

  it('updateLocation passes the payload through and refreshes', async () => {
    vi.mocked(API.updateMonitoringLocation).mockResolvedValue(null)
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 1 })
    const payload = loc('A')
    await store.updateLocation(payload)
    expect(API.updateMonitoringLocation).toHaveBeenCalledWith(payload)
    expect(API.listMonitoringLocations).toHaveBeenCalledTimes(1)
  })

  it('deleteLocation refreshes on success', async () => {
    vi.mocked(API.deleteMonitoringLocation).mockResolvedValue(null)
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [], totalCount: 0 })
    await store.deleteLocation('A')
    expect(store.locations).toEqual([])
  })

  it('flags truncation when the server had more rows than were fetched', async () => {
    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 5 })
    await store.getLocations()
    expect(store.truncated).toBe(true)

    vi.mocked(API.listMonitoringLocations).mockResolvedValue({ locations: [loc('A')], totalCount: 1 })
    await store.getLocations()
    expect(store.truncated).toBe(false)
  })
})
