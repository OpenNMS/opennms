import MoveEventConfigEventDialog from '@/components/EventConfigurationDetail/Dialog/MoveEventConfigEventDialog.vue'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { EventConfigEvent } from '@/types/eventConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockShowSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar, hideSnackbar: vi.fn() })
}))

const stubs = {
  OnmsConfirmationDialog: {
    name: 'OnmsConfirmationDialog',
    props: ['visible', 'title', 'actionButtonText'],
    emits: ['ok', 'cancel'],
    template: '<div class="confirm-stub" v-if="visible"><slot name="content" /></div>'
  },
  OnmsInputNumber: { name: 'OnmsInputNumber', props: ['modelValue', 'min', 'max'], template: '<input class="number-stub" />' },
  FormField: { name: 'FormField', props: ['label', 'for', 'hint'], template: '<div><slot /></div>' }
}

const mockEvent = {
  id: 7,
  eventLabel: 'Test Event',
  eventOrder: 3
} as EventConfigEvent

describe('MoveEventConfigEventDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mount(MoveEventConfigEventDialog, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
        stubs
      }
    })
    store = useEventConfigDetailStore()
    store.eventsPagination = { page: 1, pageSize: 10, total: 9 }
    store.moveEventConfigEvent = vi.fn().mockResolvedValue({ ok: true, status: 200, message: '', eventOrder: 5 })
    store.hideMoveEventConfigEventDialog = vi.fn()
    store.showMoveEventConfigEventDialog(mockEvent)
    await nextTick()
  })

  it('initializes the position from the event\'s current order when opened', () => {
    expect(wrapper.vm.positionValue).toBe(3)
  })

  it('a successful move reports the new position and closes', async () => {
    wrapper.vm.positionValue = 5
    await wrapper.vm.moveEvent()
    await flushPromises()

    expect(store.moveEventConfigEvent).toHaveBeenCalledWith(7, { mode: 'position', position: 5 })
    expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Moved "Test Event" to position 5' })
    expect(store.hideMoveEventConfigEventDialog).toHaveBeenCalled()
  })

  it('a failed move still resets the dialog visibility so it can reopen', async () => {
    store.moveEventConfigEvent = vi.fn().mockResolvedValue({ ok: false, status: 400, message: 'position must be between 1 and 9' })

    await wrapper.vm.moveEvent()
    await flushPromises()

    expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'position must be between 1 and 9', error: true })
    // the confirmation dialog already closed itself on ok: a store flag left true would make
    // the dialog impossible to reopen from the row menu
    expect(store.hideMoveEventConfigEventDialog).toHaveBeenCalled()
  })
})
