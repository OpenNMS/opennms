import StagedReorderList from '@/components/Common/StagedReorderList.vue'
import { EventConfigSource } from '@/types/eventConfig'
import { OnmsTooltip } from '@opennms/onms-ui'
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

describe('StagedReorderList.vue', () => {
  let wrapper: VueWrapper<any>
  let saveMock: ReturnType<typeof vi.fn>
  let refetchMock: ReturnType<typeof vi.fn>

  const items = () => [
    source(1, 'Cisco.syslog.events', 'Cisco'),
    source(2, 'Cisco.airespace', 'Cisco'),
    source(3, 'Fortinet.fortigate.events', 'Fortinet'),
    source(4, 'Standard.events', 'opennms'),
    source(5, 'opennms.pollerd.events', 'opennms')
  ]

  const workingIds = () => wrapper.vm.workingItems.map((s: EventConfigSource) => s.id)

  beforeEach(async () => {
    vi.clearAllMocks()
    saveMock = vi.fn().mockResolvedValue({ ok: true, status: 200, message: '' })
    refetchMock = vi.fn().mockResolvedValue(undefined)
    // `as any`: mount() cannot infer the component's generic parameter, so the props
    // are checked against the T = { id: number } default otherwise
    wrapper = mount(StagedReorderList as any, {
      props: {
        items: items(),
        itemLabel: (item: EventConfigSource) => item.name,
        itemSearchText: (item: EventConfigSource) => `${item.name} ${item.vendor}`,
        itemNoun: 'source',
        intro: 'Sources at the top are evaluated first.',
        filterPlaceholder: 'Filter by name or vendor',
        saving: false,
        save: saveMock,
        refetch: refetchMock
      },
      slots: {
        item: '<template #item="{ item }"><span class="item-name">{{ item.name }}</span></template>',
        pinned: '<div data-test="pinned-slot">pinned content</div>'
      },
      global: {
        plugins: [PrimeVue],
        directives: { 'onms-tooltip': OnmsTooltip },
        stubs
      }
    })
    await flushPromises()
    await nextTick()
  })

  it('initializes the working copy in evaluation order and renders the pinned slot', () => {
    expect(workingIds()).toEqual([1, 2, 3, 4, 5])
    expect(wrapper.find('[data-test="pinned-slot"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="moved-count"]').text()).toBe('No changes yet')
  })

  it('arrow moves shift an item one step and mark the list dirty', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingItems[2], -1)
    await nextTick()
    expect(workingIds()).toEqual([1, 3, 2, 4, 5])
    expect(wrapper.vm.isDirty).toBe(true)
    expect(wrapper.find('[data-test="moved-count"]').text()).toBe('2 sources moved')
  })

  it('a drag on the unfiltered list applies directly', async () => {
    const [a, b, c, d, e] = wrapper.vm.workingItems
    wrapper.vm.onVisibleReorder([b, c, a, d, e])
    await nextTick()
    expect(workingIds()).toEqual([2, 3, 1, 4, 5])
  })

  it('a drag on a filtered list inserts above the lower visible row, keeping hidden rows in place', async () => {
    // terms are OR'd across the search text, so unrelated items appear together
    wrapper.vm.filterTerm = 'cisco standard'
    await nextTick()
    const visible = wrapper.vm.visibleItems
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
    const visible = wrapper.vm.visibleItems // [1, 2, 4]
    // drag Cisco.syslog (1, at visible index 0) below Standard.events (4): [2, 4, 1]
    wrapper.vm.onDragStart({ oldIndex: 0 })
    wrapper.vm.onVisibleReorder([visible[1], visible[2], visible[0]])
    await nextTick()
    expect(workingIds()).toEqual([2, 3, 4, 1, 5])
  })

  it('row menu offers top, bottom, position and above moves named after the noun', async () => {
    wrapper.vm.rowMenuTarget = wrapper.vm.workingItems[3] // Standard.events
    const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
    expect(labels).toEqual(['Move to Top', 'Move to Bottom', 'Move to Position...', 'Move Above Source...'])

    wrapper.vm.rowMenuItems[0].command()
    await nextTick()
    expect(workingIds()).toEqual([4, 1, 2, 3, 5])
  })

  it('move-to-position dialog places a single item at the exact rank', async () => {
    wrapper.vm.openMoveDialog('position', [5])
    wrapper.vm.positionValue = 1
    wrapper.vm.applyMoveDialog()
    await nextTick()
    expect(workingIds()).toEqual([5, 1, 2, 3, 4])
  })

  it('insert-at-position moves a selected block contiguously', async () => {
    wrapper.vm.toggleSelected(1)
    wrapper.vm.toggleSelected(4)
    wrapper.vm.openMoveDialog('position', wrapper.vm.selectedIdList)
    wrapper.vm.positionValue = 2
    wrapper.vm.applyMoveDialog()
    await nextTick()
    expect(workingIds()).toEqual([2, 1, 4, 3, 5])
    expect(wrapper.vm.selectedIdList).toEqual([])
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
    wrapper.vm.moveStep(wrapper.vm.workingItems[4], -4)
    await wrapper.vm.saveOrder()
    expect(saveMock).toHaveBeenCalledWith([5, 1, 2, 3, 4])
    expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Source order saved.' })
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('edits made while the save request is in flight stay staged instead of being discarded', async () => {
    let resolveSave!: (value: unknown) => void
    saveMock.mockImplementation(() => new Promise((resolve) => {
      resolveSave = resolve
    }))

    wrapper.vm.moveStep(wrapper.vm.workingItems[0], 1) // [2, 1, 3, 4, 5]
    const pendingSave = wrapper.vm.saveOrder()

    // the user keeps editing while the request is pending
    wrapper.vm.moveStep(wrapper.vm.workingItems[2], -1) // [2, 3, 1, 4, 5]

    resolveSave({ ok: true, status: 200, message: '' })
    await pendingSave

    expect(saveMock).toHaveBeenCalledWith([2, 1, 3, 4, 5])
    expect(wrapper.emitted('close')).toBeFalsy()
    expect(wrapper.vm.isDirty).toBe(true)
    expect(mockShowSnackBar).toHaveBeenCalledWith({ msg: 'Source order saved. Changes made while saving are still unsaved.' })

    // the baseline is now what the server holds: reset returns to the submitted order
    wrapper.vm.resetOrder()
    expect(workingIds()).toEqual([2, 1, 3, 4, 5])
  })

  it('a rejected save surfaces the server message and re-fetches the current order', async () => {
    saveMock.mockResolvedValue({
      ok: false,
      status: 400,
      message: 'The order must list every source exactly once; missing: New.events'
    })
    wrapper.vm.moveStep(wrapper.vm.workingItems[0], 1)
    await wrapper.vm.saveOrder()
    expect(mockShowSnackBar).toHaveBeenCalledWith({
      msg: 'The order must list every source exactly once; missing: New.events',
      error: true
    })
    expect(refetchMock).toHaveBeenCalled()
    expect(wrapper.emitted('close')).toBeFalsy()
  })

  it('reset restores the original order', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingItems[0], 2)
    await nextTick()
    expect(wrapper.vm.isDirty).toBe(true)
    wrapper.vm.resetOrder()
    await nextTick()
    expect(workingIds()).toEqual([1, 2, 3, 4, 5])
    expect(wrapper.vm.isDirty).toBe(false)
  })

  it('cancelling while dirty asks for confirmation instead of discarding silently', async () => {
    wrapper.vm.moveStep(wrapper.vm.workingItems[0], 1)
    wrapper.vm.requestClose()
    await nextTick()
    expect(wrapper.vm.discardConfirmVisible).toBe(true)
    expect(wrapper.emitted('close')).toBeFalsy()

    wrapper.vm.discardAndClose()
    expect(wrapper.emitted('close')).toBeTruthy()
    expect(workingIds()).toEqual([1, 2, 3, 4, 5])
  })

  it('cancelling while clean just closes', () => {
    wrapper.vm.requestClose()
    expect(wrapper.vm.discardConfirmVisible).toBe(false)
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  describe('windowed rendering', () => {
    beforeEach(async () => {
      await wrapper.setProps({ renderChunk: 3 })
      // props changes do not retrigger the items watcher; re-init picks up the new chunk
      await wrapper.setProps({ items: items() })
      await nextTick()
    })

    it('renders only the first chunk and says how many rows are below', () => {
      expect(wrapper.findAll('.reorder-row').length).toBe(3)
      expect(wrapper.find('[data-test="filter-count"]').text()).toBe('Showing 3 of 5 sources')
      expect(wrapper.find('[data-test="show-more-row"]').text()).toContain('2 more sources below')
    })

    it('show more extends the window until everything is rendered', async () => {
      await wrapper.get('[data-test="show-more-button"]').trigger('click')
      expect(wrapper.findAll('.reorder-row').length).toBe(5)
      expect(wrapper.find('[data-test="show-more-row"]').exists()).toBe(false)
    })

    it('filtering resets the window and select-all only covers rendered rows', async () => {
      await wrapper.get('[data-test="show-more-button"]').trigger('click')
      wrapper.vm.onFilterChange('events')
      await nextTick()
      // matches: Cisco.syslog.events, Fortinet.fortigate.events, Standard.events, opennms.pollerd.events
      expect(wrapper.findAll('.reorder-row').length).toBe(3)
      wrapper.vm.selectAllShown()
      expect(wrapper.vm.selectedIdList).toEqual([1, 3, 4])
    })

    it('moves still act on the full working list, not the window', async () => {
      wrapper.vm.rowMenuTarget = wrapper.vm.workingItems[0]
      wrapper.vm.rowMenuItems[1].command() // Move to Bottom
      await nextTick()
      expect(workingIds()).toEqual([2, 3, 4, 5, 1])
    })
  })
})
