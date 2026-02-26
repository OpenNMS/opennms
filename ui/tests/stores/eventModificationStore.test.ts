import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { getEventConfSourceById } from '@/services/eventConfigService'
import { CreateEditMode } from '@/types'
import type { EventConfigSource, EventConfigEvent } from '@/types/eventConfig'

vi.mock('@/services/eventConfigService', () => ({
  getEventConfSourceById: vi.fn()
}))

describe('eventModificationStore', () => {
  let store: ReturnType<typeof useEventModificationStore>

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

  const mockEvent: EventConfigEvent = {
    id: 1,
    uei: 'uei.test.event',
    eventLabel: 'Test Event',
    description: 'Test event description',
    severity: 'Major',
    enabled: true,
    xmlContent: '<event>test</event>',
    createdTime: new Date('2024-01-01'),
    lastModified: new Date('2024-01-02'),
    modifiedBy: 'user1',
    sourceName: 'Test Source',
    vendor: 'Test Vendor',
    fileOrder: 1
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useEventModificationStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('Initial State', () => {
    it('should have null selectedSource initially', () => {
      expect(store.selectedSource).toBeNull()
    })

    it('should have isEditMode as None initially', () => {
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
    })

    it('should have null eventConfigEvent initially', () => {
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should have correct initial state structure', () => {
      expect(store.$state).toEqual({
        selectedSource: null,
        eventModificationState: {
          isEditMode: CreateEditMode.None,
          eventConfigEvent: null
        }
      })
    })
  })

  describe('fetchSourceById', () => {
    it('should fetch source and set selectedSource on success', async () => {
      vi.mocked(getEventConfSourceById).mockResolvedValue(mockSource)

      await store.fetchSourceById('1')

      expect(getEventConfSourceById).toHaveBeenCalledWith('1')
      expect(store.selectedSource).toEqual(mockSource)
    })

    it('should handle fetch error gracefully', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Fetch failed')
      vi.mocked(getEventConfSourceById).mockRejectedValue(error)

      await store.fetchSourceById('999')

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching source by ID:', '999', error)
      expect(store.selectedSource).toBeNull()
      
      consoleErrorSpy.mockRestore()
    })

    it('should call service with correct ID parameter', async () => {
      vi.mocked(getEventConfSourceById).mockResolvedValue(mockSource)

      await store.fetchSourceById('42')

      expect(getEventConfSourceById).toHaveBeenCalledWith('42')
      expect(getEventConfSourceById).toHaveBeenCalledTimes(1)
    })

    it('should update selectedSource when fetching different sources', async () => {
      const firstSource = { ...mockSource, id: 1, name: 'First Source' }
      const secondSource = { ...mockSource, id: 2, name: 'Second Source' }

      vi.mocked(getEventConfSourceById).mockResolvedValueOnce(firstSource)
      await store.fetchSourceById('1')
      expect(store.selectedSource).toEqual(firstSource)

      vi.mocked(getEventConfSourceById).mockResolvedValueOnce(secondSource)
      await store.fetchSourceById('2')
      expect(store.selectedSource).toEqual(secondSource)
    })
  })

  describe('setSelectedEventConfigSource', () => {
    it('should set selectedSource, isEditMode, and eventConfigEvent', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Edit, mockEvent)

      expect(store.selectedSource).toEqual(mockSource)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Edit)
      expect(store.eventModificationState.eventConfigEvent).toEqual(mockEvent)
    })

    it('should set Create mode correctly', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Create, null)

      expect(store.selectedSource).toEqual(mockSource)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should allow null eventConfigEvent', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Create, null)

      expect(store.selectedSource).toEqual(mockSource)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should overwrite previous state', () => {
      const firstSource = { ...mockSource, id: 1, name: 'First' }
      const secondSource = { ...mockSource, id: 2, name: 'Second' }

      store.setSelectedEventConfigSource(firstSource, CreateEditMode.Edit, mockEvent)
      expect(store.selectedSource?.name).toBe('First')

      store.setSelectedEventConfigSource(secondSource, CreateEditMode.Create, null)
      expect(store.selectedSource?.name).toBe('Second')
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should handle all CreateEditMode values', () => {
      // Test with Edit mode
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Edit, mockEvent)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Edit)

      // Test with Create mode
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Create, null)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)

      // Test with None mode
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.None, null)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
    })
  })

  describe('openCreateWithoutSource', () => {
    it('should set isEditMode and eventConfigEvent without setting selectedSource', () => {
      store.openCreateWithoutSource(CreateEditMode.Create, mockEvent)

      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
      expect(store.eventModificationState.eventConfigEvent).toEqual(mockEvent)
    })

    it('should allow null eventConfigEvent', () => {
      store.openCreateWithoutSource(CreateEditMode.Create, null)

      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should not affect existing selectedSource', () => {
      store.selectedSource = mockSource

      store.openCreateWithoutSource(CreateEditMode.Create, mockEvent)

      expect(store.selectedSource).toEqual(mockSource)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
    })

    it('should overwrite previous eventModificationState', () => {
      const firstEvent = { ...mockEvent, id: 1, uei: 'uei.first' }
      const secondEvent = { ...mockEvent, id: 2, uei: 'uei.second' }

      store.openCreateWithoutSource(CreateEditMode.Edit, firstEvent)
      expect(store.eventModificationState.eventConfigEvent?.uei).toBe('uei.first')

      store.openCreateWithoutSource(CreateEditMode.Create, secondEvent)
      expect(store.eventModificationState.eventConfigEvent?.uei).toBe('uei.second')
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
    })

    it('should handle Edit mode', () => {
      store.openCreateWithoutSource(CreateEditMode.Edit, mockEvent)

      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Edit)
      expect(store.eventModificationState.eventConfigEvent).toEqual(mockEvent)
    })
  })

  describe('resetEventModificationState', () => {
    it('should reset all state to initial values', () => {
      store.selectedSource = mockSource
      store.eventModificationState.isEditMode = CreateEditMode.Edit
      store.eventModificationState.eventConfigEvent = mockEvent

      store.resetEventModificationState()

      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should reset from Create mode', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Create, mockEvent)

      store.resetEventModificationState()

      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should reset from Edit mode', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Edit, mockEvent)

      store.resetEventModificationState()

      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should be idempotent - multiple calls have same result', () => {
      store.selectedSource = mockSource
      store.eventModificationState.isEditMode = CreateEditMode.Edit

      store.resetEventModificationState()
      const firstResetState = { ...store.$state }

      store.resetEventModificationState()
      const secondResetState = { ...store.$state }

      expect(firstResetState).toEqual(secondResetState)
    })

    it('should clear complex eventConfigEvent objects', () => {
      const complexEvent: EventConfigEvent = {
        ...mockEvent,
        xmlContent: '<event><complex>content</complex></event>',
        description: 'Very long description with special characters!@#$%'
      }

      store.eventModificationState.eventConfigEvent = complexEvent
      store.resetEventModificationState()

      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })
  })

  describe('Integration Scenarios', () => {
    it('should handle complete create event flow', async () => {
      // Fetch source
      vi.mocked(getEventConfSourceById).mockResolvedValue(mockSource)
      await store.fetchSourceById('1')

      // Set up for create mode
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Create, null)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)

      // Reset after completion
      store.resetEventModificationState()
      expect(store.selectedSource).toBeNull()
    })

    it('should handle complete edit event flow', async () => {
      // Fetch source
      vi.mocked(getEventConfSourceById).mockResolvedValue(mockSource)
      await store.fetchSourceById('1')

      // Set up for edit mode
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Edit, mockEvent)
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Edit)
      expect(store.eventModificationState.eventConfigEvent).toEqual(mockEvent)

      // Reset after completion
      store.resetEventModificationState()
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should handle create without source flow', () => {
      store.openCreateWithoutSource(CreateEditMode.Create, mockEvent)
      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)

      store.resetEventModificationState()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
    })

    it('should handle switching between sources', async () => {
      const source1 = { ...mockSource, id: 1, name: 'Source 1' }
      const source2 = { ...mockSource, id: 2, name: 'Source 2' }

      vi.mocked(getEventConfSourceById).mockResolvedValueOnce(source1)
      await store.fetchSourceById('1')
      store.setSelectedEventConfigSource(source1, CreateEditMode.Edit, mockEvent)

      vi.mocked(getEventConfSourceById).mockResolvedValueOnce(source2)
      await store.fetchSourceById('2')
      store.setSelectedEventConfigSource(source2, CreateEditMode.Create, null)

      expect(store.selectedSource?.name).toBe('Source 2')
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.Create)
    })

    it('should handle cancellation flow', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Edit, mockEvent)
      expect(store.selectedSource).not.toBeNull()

      // User cancels
      store.resetEventModificationState()
      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
    })
  })

  describe('Edge Cases', () => {
    it('should handle empty string ID in fetchSourceById', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getEventConfSourceById).mockRejectedValue(new Error('Invalid ID'))

      await store.fetchSourceById('')

      expect(getEventConfSourceById).toHaveBeenCalledWith('')
      expect(consoleErrorSpy).toHaveBeenCalled()
      
      consoleErrorSpy.mockRestore()
    })

    it('should handle rapid successive state changes', () => {
      store.setSelectedEventConfigSource(mockSource, CreateEditMode.Create, null)
      store.openCreateWithoutSource(CreateEditMode.Edit, mockEvent)
      store.resetEventModificationState()

      expect(store.selectedSource).toBeNull()
      expect(store.eventModificationState.isEditMode).toBe(CreateEditMode.None)
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })

    it('should maintain state independence between properties', () => {
      store.selectedSource = mockSource
      store.eventModificationState.isEditMode = CreateEditMode.Edit
      store.eventModificationState.eventConfigEvent = null

      expect(store.selectedSource).not.toBeNull()
      expect(store.eventModificationState.eventConfigEvent).toBeNull()
    })
  })
})
