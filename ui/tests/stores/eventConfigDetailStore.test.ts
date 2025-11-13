import { Severity } from '@/components/EventConfigEventCreate/constants'
import {
  changeEventConfigEventStatus,
  changeEventConfigSourceStatus,
  filterEventConfigEvents
} from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { EventConfigEvent, EventConfigSource } from '@/types/eventConfig'
import { createPinia, setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/eventConfigService', () => ({
  changeEventConfigEventStatus: vi.fn(),
  changeEventConfigSourceStatus: vi.fn(),
  filterEventConfigEvents: vi.fn()
}))

describe('useEventConfigDetailStore', () => {
  let store: ReturnType<typeof useEventConfigDetailStore>

  const mockSource: EventConfigSource = {
    id: 1,
    name: 'Test Source',
    vendor: 'Test Vendor',
    description: 'Test Description',
    enabled: true,
    eventCount: 10,
    fileOrder: 1,
    uploadedBy: 'testuser',
    createdTime: new Date('2024-01-01'),
    lastModified: new Date('2024-01-02')
  }

  const mockEvents: EventConfigEvent[] = [
    {
      id: 1,
      uei: 'uei.test.event1',
      eventLabel: 'Test Event 1',
      description: 'Description 1',
      severity: Severity.Major,
      enabled: true,
      xmlContent: '<xml>content1</xml>',
      createdTime: new Date('2024-01-01'),
      lastModified: new Date('2024-01-02'),
      modifiedBy: 'user1',
      sourceName: 'Test Source',
      vendor: 'Test Vendor',
      fileOrder: 1
    },
    {
      id: 2,
      uei: 'uei.test.event2',
      eventLabel: 'Test Event 2',
      description: 'Description 2',
      severity: Severity.Minor,
      enabled: false,
      xmlContent: '<xml>content2</xml>',
      createdTime: new Date('2024-01-03'),
      lastModified: new Date('2024-01-04'),
      modifiedBy: 'user2',
      sourceName: 'Test Source',
      vendor: 'Test Vendor',
      fileOrder: 2
    }
  ]

  const mockFilterResponse = {
    events: mockEvents,
    totalRecords: 2
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useEventConfigDetailStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should have correct initial state', () => {
    expect(store.events).toEqual([])
    expect(store.eventsPagination).toEqual({
      page: 1,
      pageSize: 10,
      total: 0
    })
    expect(store.eventsSearchTerm).toBe('')
    expect(store.eventsSorting).toEqual({
      sortOrder: 'desc',
      sortKey: 'createdTime'
    })
    expect(store.selectedSource).toBeNull()
    expect(store.isLoading).toBe(false)
    expect(store.deleteEventConfigEventDialogState).toEqual({
      visible: false,
      eventConfigEvent: null
    })
    expect(store.changeEventConfigEventStatusDialogState).toEqual({
      visible: false,
      eventConfigEvent: null
    })
    expect(store.deleteEventConfigSourceDialogState).toEqual({
      visible: false,
      eventConfigSource: null
    })
    expect(store.changeEventConfigSourceStatusDialogState).toEqual({
      visible: false,
      eventConfigSource: null
    })
  })

  it('should fetch events successfully when source is selected', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.fetchEventsBySourceId()

    expect(filterEventConfigEvents).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
    expect(store.events).toEqual(mockEvents)
    expect(store.eventsPagination.total).toBe(2)
    expect(store.isLoading).toBe(false)
  })

  it('should not fetch when no source is selected', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    store.selectedSource = null

    await store.fetchEventsBySourceId()

    expect(filterEventConfigEvents).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')
    consoleErrorSpy.mockRestore()
  })

  it('should fetch with correct pagination parameters', async () => {
    store.selectedSource = mockSource
    store.eventsPagination.page = 3
    store.eventsPagination.pageSize = 20
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.fetchEventsBySourceId()

    expect(filterEventConfigEvents).toHaveBeenCalledWith(1, 40, 20, '', 'createdTime', 'desc')
  })

  it('should fetch with search term', async () => {
    store.selectedSource = mockSource
    store.eventsSearchTerm = 'test search'
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.fetchEventsBySourceId()

    expect(filterEventConfigEvents).toHaveBeenCalledWith(1, 0, 10, 'test search', 'createdTime', 'desc')
  })

  it('should handle errors when fetching events', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const error = new Error('Failed to fetch events')
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockRejectedValue(error)

    await store.fetchEventsBySourceId()

    expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching events for source ID:', 1, error)
    expect(store.isLoading).toBe(false)
    consoleErrorSpy.mockRestore()
  })

  it('should set loading state correctly during fetch', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockImplementation(
      () =>
        new Promise((resolve) => {
          expect(store.isLoading).toBe(true)
          resolve(mockFilterResponse)
        })
    )

    await store.fetchEventsBySourceId()
    expect(store.isLoading).toBe(false)
  })

  it('should set selected source', () => {
    store.setSelectedEventConfigSource(mockSource)

    expect(store.selectedSource).toEqual(mockSource)
  })

  it('should update selected source', () => {
    store.selectedSource = mockSource
    const newSource = { ...mockSource, id: 2, name: 'New Source' }

    store.setSelectedEventConfigSource(newSource)

    expect(store.selectedSource).toEqual(newSource)
  })

  it('should handle page change', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.onEventsPageChange(3)

    expect(store.eventsPagination.page).toBe(3)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should handle page size change and reset page to 1', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    store.eventsPagination.page = 5

    await store.onEventsPageSizeChange(25)

    expect(store.eventsPagination.page).toBe(1)
    expect(store.eventsPagination.pageSize).toBe(25)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should handle search term change and reset page', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    store.eventsPagination.page = 3

    await store.onChangeEventsSearchTerm('new search')

    expect(store.eventsSearchTerm).toBe('new search')
    expect(store.eventsPagination.page).toBe(1)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should handle sort change', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.onEventsSortChange('uei', 'asc')

    expect(store.eventsSorting.sortKey).toBe('uei')
    expect(store.eventsSorting.sortOrder).toBe('asc')
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should refresh event config events with all defaults', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    store.eventsPagination = { page: 5, pageSize: 25, total: 100 }
    store.eventsSearchTerm = 'search term'
    store.eventsSorting = { sortKey: 'uei', sortOrder: 'asc' }

    await store.refreshEventConfigEvents()

    expect(store.eventsPagination.page).toBe(1)
    expect(store.eventsPagination.pageSize).toBe(10)
    expect(store.eventsPagination.total).toBe(2)
    expect(store.eventsSearchTerm).toBe('')
    expect(store.eventsSorting).toEqual({
      sortKey: 'createdTime',
      sortOrder: 'desc'
    })
    expect(filterEventConfigEvents).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
  })

  it('should show delete event dialog', () => {
    const mockEvent = mockEvents[0]

    store.showDeleteEventConfigEventDialog(mockEvent)

    expect(store.deleteEventConfigEventDialogState.visible).toBe(true)
    expect(store.deleteEventConfigEventDialogState.eventConfigEvent).toEqual(mockEvent)
  })

  it('should hide delete event dialog', () => {
    const mockEvent = mockEvents[0]
    store.deleteEventConfigEventDialogState = {
      visible: true,
      eventConfigEvent: mockEvent
    }

    store.hideDeleteEventConfigEventDialog()

    expect(store.deleteEventConfigEventDialogState.visible).toBe(false)
    expect(store.deleteEventConfigEventDialogState.eventConfigEvent).toBeNull()
  })

  it('should show change event status dialog', () => {
    const mockEvent = mockEvents[0]

    store.showChangeEventConfigEventStatusDialog(mockEvent)

    expect(store.changeEventConfigEventStatusDialogState.visible).toBe(true)
    expect(store.changeEventConfigEventStatusDialogState.eventConfigEvent).toEqual(mockEvent)
  })

  it('should hide change event status dialog and fetch events', async () => {
    const mockEvent = mockEvents[0]
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    store.changeEventConfigEventStatusDialogState = {
      visible: true,
      eventConfigEvent: mockEvent
    }

    await store.hideChangeEventConfigEventStatusDialog()

    expect(store.changeEventConfigEventStatusDialogState.visible).toBe(false)
    expect(store.changeEventConfigEventStatusDialogState.eventConfigEvent).toBeNull()
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should show delete source dialog', () => {
    store.showDeleteEventConfigSourceDialog(mockSource)

    expect(store.deleteEventConfigSourceDialogState.visible).toBe(true)
    expect(store.deleteEventConfigSourceDialogState.eventConfigSource).toEqual(mockSource)
  })

  it('should hide delete source dialog', async () => {
    store.deleteEventConfigSourceDialogState = {
      visible: true,
      eventConfigSource: mockSource
    }

    await store.hideDeleteEventConfigSourceDialog()

    expect(store.deleteEventConfigSourceDialogState.visible).toBe(false)
    expect(store.deleteEventConfigSourceDialogState.eventConfigSource).toBeNull()
  })

  it('should show change source status dialog', () => {
    store.showChangeEventConfigSourceStatusDialog(mockSource)

    expect(store.changeEventConfigSourceStatusDialogState.visible).toBe(true)
    expect(store.changeEventConfigSourceStatusDialogState.eventConfigSource).toEqual(mockSource)
  })

  it('should hide change source status dialog', () => {
    store.changeEventConfigSourceStatusDialogState = {
      visible: true,
      eventConfigSource: mockSource
    }

    store.hideChangeEventConfigSourceStatusDialog()

    expect(store.changeEventConfigSourceStatusDialogState.visible).toBe(false)
    expect(store.changeEventConfigSourceStatusDialogState.eventConfigSource).toBeNull()
  })

  it('should disable event successfully', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    vi.mocked(changeEventConfigEventStatus).mockResolvedValue(true)

    await store.disableEventConfigEvent(1)

    expect(changeEventConfigEventStatus).toHaveBeenCalledWith(1, 1, false)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should enable event successfully', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    vi.mocked(changeEventConfigEventStatus).mockResolvedValue(true)

    await store.enableEventConfigEvent(1)

    expect(changeEventConfigEventStatus).toHaveBeenCalledWith(1, 1, true)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should not fetch events if disable fails', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    vi.mocked(changeEventConfigEventStatus).mockResolvedValue(false)

    await store.disableEventConfigEvent(1)

    expect(changeEventConfigEventStatus).toHaveBeenCalledWith(1, 1, false)
    expect(filterEventConfigEvents).not.toHaveBeenCalled()
  })

  it('should not fetch events if enable fails', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    vi.mocked(changeEventConfigEventStatus).mockResolvedValue(false)

    await store.enableEventConfigEvent(1)

    expect(changeEventConfigEventStatus).toHaveBeenCalledWith(1, 1, true)
    expect(filterEventConfigEvents).not.toHaveBeenCalled()
  })

  it('should log error when disabling event with no source', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    store.selectedSource = null

    await store.disableEventConfigEvent(1)

    expect(changeEventConfigEventStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')
    consoleErrorSpy.mockRestore()
  })

  it('should disable source successfully', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(true)

    await store.disableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, false)
    expect(store.selectedSource?.enabled).toBe(false)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should enable source successfully', async () => {
    store.selectedSource = { ...mockSource, enabled: false }
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(true)

    await store.enableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, true)
    expect(store.selectedSource?.enabled).toBe(true)
    expect(filterEventConfigEvents).toHaveBeenCalled()
  })

  it('should not update source if disable fails', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    const originalEnabled = store.selectedSource!.enabled
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(false)

    await store.disableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, false)
    expect(store.selectedSource?.enabled).toBe(originalEnabled)
    expect(filterEventConfigEvents).not.toHaveBeenCalled()
  })

  it('should not update source if enable fails', async () => {
    store.selectedSource = { ...mockSource, enabled: false }
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)
    const originalEnabled = store.selectedSource.enabled
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(false)

    await store.enableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, true)
    expect(store.selectedSource?.enabled).toBe(originalEnabled)
    expect(filterEventConfigEvents).not.toHaveBeenCalled()
  })

  it('should log error when disabling source with no source id', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    await expect(store.disableEventConfigSource(0)).rejects.toThrow('No source selected')

    expect(changeEventConfigSourceStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')

    consoleErrorSpy.mockRestore()
  })

  it('should log error when disabling with no selected source', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    store.selectedSource = null

    await expect(store.disableEventConfigSource(1)).rejects.toThrow('No source selected')

    expect(changeEventConfigSourceStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')
    consoleErrorSpy.mockRestore()
  })

  it('should log error when enabling source with no source id', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    await expect(store.enableEventConfigSource(0)).rejects.toThrow('No source selected')

    expect(changeEventConfigSourceStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')
    consoleErrorSpy.mockRestore()
  })

  it('should log error when enabling with no selected source', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    store.selectedSource = null

    await expect(store.enableEventConfigSource(1)).rejects.toThrow('No source selected')

    expect(changeEventConfigSourceStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')
    consoleErrorSpy.mockRestore()
  })

  it('should handle multiple consecutive fetches correctly', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.fetchEventsBySourceId()
    await store.onEventsPageChange(2)
    await store.onChangeEventsSearchTerm('test')

    expect(filterEventConfigEvents).toHaveBeenCalledTimes(3)
  })

  it('should maintain state consistency after failed operations', async () => {
    store.selectedSource = mockSource
    const error = new Error('Network error')
    vi.mocked(filterEventConfigEvents).mockRejectedValue(error)
    vi.spyOn(console, 'error').mockImplementation(() => {})

    const initialEvents = [...store.events]
    const initialPagination = { ...store.eventsPagination }

    await store.fetchEventsBySourceId()

    expect(store.events).toEqual(initialEvents)
    expect(store.eventsPagination).toEqual(initialPagination)
    expect(store.isLoading).toBe(false)
  })

  it('should handle changing source and fetching events', async () => {
    store.setSelectedEventConfigSource(mockSource)
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    await store.fetchEventsBySourceId()

    expect(store.selectedSource).toEqual(mockSource)
    expect(store.events).toEqual(mockEvents)
  })

  it('should handle reset filter', async () => {
    store.selectedSource = mockSource
    vi.mocked(filterEventConfigEvents).mockResolvedValue(mockFilterResponse)

    store.refreshEventConfigEvents()

    expect(store.eventsSorting).toEqual({ sortKey: 'createdTime', sortOrder: 'desc' })
    expect(store.eventsSearchTerm).toBe('')
    expect(store.eventsPagination).toEqual({ page: 1, pageSize: 10, total: 0 })
  })

})
