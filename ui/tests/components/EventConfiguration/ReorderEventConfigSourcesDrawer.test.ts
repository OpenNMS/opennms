import ReorderEventConfigSourcesDrawer from '@/components/EventConfiguration/ReorderEventConfigSourcesDrawer.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
import { OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockShowSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar, hideSnackbar: vi.fn() })
}))

// Stub the pieces that teleport or drag so the suite focuses on the ordering logic.
const DraggableStub = {
  name: 'Draggable',
  props: ['modelValue', 'itemKey', 'handle'],
  emits: ['update:modelValue'],
  template: '<div class="draggable-stub"><template v-for="(element, index) in modelValue" :key="element.id"><slot name="item" :element="element" :index="index" /></template></div>'
}

const stubs = {
  Draggable: DraggableStub,
  OnmsDrawer: {
    name: 'OnmsDrawer',
    props: ['visible', 'header', 'width'],
    emits: ['update:visible'],
    template: '<div class="drawer-stub" v-if="visible"><slot /></div>'
  },
  OnmsMenu: { name: 'OnmsMenu', props: ['items'], template: '<div class="menu-stub"></div>' },
  OnmsConfirmationDialog: {
    name: 'OnmsConfirmationDialog',
    props: ['visible', 'title', 'actionButtonText', 'cancelButtonText'],
    emits: ['ok', 'cancel'],
    template: '<div class="confirm-stub" v-if="visible"><slot name="content" /></div>'
  }
}

const source = (id: number, name: string, vendor = 'test'): EventConfigSource => ({
  id,
  name,
  vendor,
  description: '',
  enabled: true,
  eventCount: id * 10,
  fileOrder: 100 - id,
  evaluationOrder: id,
  uploadedBy: 'test',
  createdTime: new Date(),
  lastModified: new Date()
})

describe('ReorderEventConfigSourcesDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>

  const workingIds = () => wrapper.vm.workingSources.map((s: EventConfigSource) => s.id)

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mount(ReorderEventConfigSourcesDrawer, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
        directives: { 'onms-tooltip': OnmsTooltip },
        stubs
      }
    })
    store = useEventConfigStore()
    store.orderedSources = [
      source(1, 'Cisco.syslog.events', 'Cisco'),
      source(2, 'Cisco.airespace', 'Cisco'),
      source(3, 'Fortinet.fortigate.events', 'Fortinet'),
      source(4, 'Standard.events', 'opennms'),
      source(5, 'opennms.pollerd.events', 'opennms')
    ]
    store.catchAllSource = source(99, 'opennms.catch-all.events', 'opennms')
    store.reorderSourcesDrawerState.visible = true
    store.saveSourcesOrder = vi.fn().mockResolvedValue({ ok: true, status: 200, message: '' })
    store.fetchOrderedSources = vi.fn().mockResolvedValue(undefined)
    store.hideReorderSourcesDrawer = vi.fn()
    await flushPromises()
    await nextTick()
  })

  it('initializes the working copy in evaluation order and shows the pinned catch-all', () => {
    expect(workingIds()).toEqual([1, 2, 3, 4, 5])
    expect(wrapper.find('[data-test="catch-all-row"]').text()).toContain('opennms.catch-all.events')
    expect(wrapper.find('[data-test="catch-all-row"]').text()).toContain('Always evaluated last')
    expect(wrapper.find('[data-test="moved-count"]').text()).toBe('No changes yet')
  })

  it('arrow moves shift a source one step and mark the drawer dirty', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingSources[2], -1)
    await nextTick()
    expect(workingIds()).toEqual([1, 3, 2, 4, 5])
    expect(wrapper.vm.isDirty).toBe(true)
    expect(wrapper.find('[data-test="moved-count"]').text()).toBe('2 sources moved')
  })

  it('a drag on the unfiltered list applies directly', async () => {
    const [a, b, c, d, e] = wrapper.vm.workingSources
    wrapper.vm.onVisibleReorder([b, c, a, d, e])
    await nextTick()
    expect(workingIds()).toEqual([2, 3, 1, 4, 5])
  })

  it('a drag on a filtered list inserts above the lower visible row, keeping hidden rows in place', async () => {
    // terms are OR'd across name and vendor, so unrelated sources appear together
    wrapper.vm.filterTerm = 'cisco standard'
    await nextTick()
    const visible = wrapper.vm.visibleSources
    expect(visible.map((s: EventConfigSource) => s.id)).toEqual([1, 2, 4])

    // drag Standard.events (4, at visible index 2) above Cisco.airespace (2): [1, 4, 2]
    wrapper.vm.onDragStart({ oldIndex: 2 })
    wrapper.vm.onVisibleReorder([visible[0], visible[2], visible[1]])
    await nextTick()
    expect(workingIds()).toEqual([1, 4, 2, 3, 5])
  })

  it('a filtered drag to the end lands right after the last visible row', async () => {
    wrapper.vm.filterTerm = 'cisco standard'
    await nextTick()
    const visible = wrapper.vm.visibleSources // [1, 2, 4]
    // drag Cisco.syslog (1, at visible index 0) below Standard.events (4): [2, 4, 1]
    wrapper.vm.onDragStart({ oldIndex: 0 })
    wrapper.vm.onVisibleReorder([visible[1], visible[2], visible[0]])
    await nextTick()
    expect(workingIds()).toEqual([2, 3, 4, 1, 5])
  })

  it('row menu offers top, bottom, position and above-source moves', async () => {
    wrapper.vm.rowMenuTarget = wrapper.vm.workingSources[3] // Standard.events
    const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
    expect(labels).toEqual(['Move to Top', 'Move to Bottom', 'Move to Position...', 'Move Above Source...'])

    wrapper.vm.rowMenuItems[0].command()
    await nextTick()
    expect(workingIds()).toEqual([4, 1, 2, 3, 5])
  })

  it('move-to-position dialog places the source at the exact rank', async () => {
    wrapper.vm.openMoveDialog('position', [5])
    wrapper.vm.positionValue = 1
    wrapper.vm.applyMoveDialog()
    await nextTick()
    expect(workingIds()).toEqual([5, 1, 2, 3, 4])
  })

  it('block move keeps the selection contiguous and in relative order', async () => {
    wrapper.vm.toggleSelected(1)
    wrapper.vm.toggleSelected(3)
    await nextTick()
    expect(wrapper.find('[data-test="selection-bar"]').text()).toContain('2 selected')

    wrapper.vm.moveBlockAbove([1, 3], 5)
    await nextTick()
    expect(workingIds()).toEqual([2, 4, 1, 3, 5])
  })

  it('select all shown only selects the filtered rows', async () => {
    wrapper.vm.filterTerm = 'cisco'
    await nextTick()
    wrapper.vm.selectAllShown()
    expect(wrapper.vm.selectedIdList).toEqual([1, 2])
  })

  it('save sends the complete order, closes and reports success', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingSources[4], -4)
    await wrapper.vm.saveOrder()
    expect(store.saveSourcesOrder).toHaveBeenCalledWith([5, 1, 2, 3, 4])
    expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Source order saved.' })
    expect(store.hideReorderSourcesDrawer).toHaveBeenCalled()
  })

  it('edits made while the save request is in flight stay staged instead of being discarded', async () => {
    let resolveSave!: (value: unknown) => void
    store.saveSourcesOrder = vi.fn().mockImplementation(() => new Promise((resolve) => {
      resolveSave = resolve
    }))

    wrapper.vm.moveStep(wrapper.vm.workingSources[0], 1) // [2, 1, 3, 4, 5]
    const pendingSave = wrapper.vm.saveOrder()

    // the user keeps editing while the request is pending
    wrapper.vm.moveStep(wrapper.vm.workingSources[2], -1) // [2, 3, 1, 4, 5]

    resolveSave({ ok: true, status: 200, message: '' })
    await pendingSave

    expect(store.saveSourcesOrder).toHaveBeenCalledWith([2, 1, 3, 4, 5])
    expect(store.hideReorderSourcesDrawer).not.toHaveBeenCalled()
    expect(wrapper.vm.isDirty).toBe(true)
    expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Source order saved. Changes made while saving are still unsaved.' })

    // the baseline is now what the server holds: reset returns to the submitted order
    wrapper.vm.resetOrder()
    expect(workingIds()).toEqual([2, 1, 3, 4, 5])
  })

  it('a rejected save surfaces the server message and re-fetches the current order', async () => {
    store.saveSourcesOrder = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      message: 'The order must list every source exactly once; missing: New.events'
    })
    wrapper.vm.moveStep(wrapper.vm.workingSources[0], 1)
    await wrapper.vm.saveOrder()
    expect(mockShowSnackBar).toHaveBeenCalledWith({
      msg: 'The order must list every source exactly once; missing: New.events',
      error: true
    })
    expect(store.fetchOrderedSources).toHaveBeenCalled()
    expect(store.hideReorderSourcesDrawer).not.toHaveBeenCalled()
  })

  it('reset restores the original order', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingSources[0], 2)
    await nextTick()
    expect(wrapper.vm.isDirty).toBe(true)
    wrapper.vm.resetOrder()
    await nextTick()
    expect(workingIds()).toEqual([1, 2, 3, 4, 5])
    expect(wrapper.vm.isDirty).toBe(false)
  })

  it('closing while dirty asks for confirmation instead of discarding silently', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingSources[0], 1)
    wrapper.vm.requestClose()
    await nextTick()
    expect(wrapper.vm.discardConfirmVisible).toBe(true)
    expect(store.hideReorderSourcesDrawer).not.toHaveBeenCalled()

    wrapper.vm.discardAndClose()
    expect(store.hideReorderSourcesDrawer).toHaveBeenCalled()
    expect(workingIds()).toEqual([1, 2, 3, 4, 5])
  })

  it('closing while clean just closes', () => {
    wrapper.vm.requestClose()
    expect(wrapper.vm.discardConfirmVisible).toBe(false)
    expect(store.hideReorderSourcesDrawer).toHaveBeenCalled()
  })
})
