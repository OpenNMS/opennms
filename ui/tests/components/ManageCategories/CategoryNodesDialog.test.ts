import CategoryNodesDialog from '@/components/ManageCategories/CategoryNodesDialog.vue'
import API from '@/services'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services', () => ({
  default: {
    getNodes: vi.fn(),
    addNodeToCategory: vi.fn(),
    removeNodeFromCategory: vi.fn()
  }
}))

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}
const ToggleStub = {
  name: 'ToggleSwitch',
  props: ['modelValue', 'disabled'],
  emits: ['update:modelValue'],
  template: '<button class="toggle-stub" :disabled="disabled" @click="$emit(\'update:modelValue\', !modelValue)" />'
}
const CheckboxStub = {
  name: 'Checkbox',
  props: ['modelValue'],
  emits: ['update:modelValue'],
  template: '<input type="checkbox" class="cb-stub" @change="$emit(\'update:modelValue\', !modelValue)" />'
}

const CAT_ID = 5

// node whose categories include CAT_ID is a member; foreignSource => requisitioned
const node = (id: number, over: Record<string, any> = {}) => ({
  id: String(id), label: `node-${id}`, location: 'Default', foreignSource: null, categories: [], ...over
})

const nodesResponse = (nodes: any[], total = nodes.length) => ({ node: nodes, totalCount: total, count: nodes.length, offset: 0 })

describe('CategoryNodesDialog.vue (server-paged node picker)', () => {
  let wrapper: VueWrapper<any>

  const mountDialog = async () => {
    wrapper = mount(CategoryNodesDialog, {
      props: { visible: false, categoryId: CAT_ID, categoryName: 'Routers' },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub, ToggleSwitch: ToggleStub, Checkbox: CheckboxStub } }
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  const toggles = () => wrapper.findAllComponents(ToggleStub)

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(API.getNodes).mockResolvedValue(nodesResponse([
      node(1, { categories: [{ id: CAT_ID, name: 'Routers' }] }), // member
      node(2), // not a member
      node(3, { foreignSource: 'Provisiond' }) // requisitioned, not a member
    ]) as any)
    vi.mocked(API.addNodeToCategory).mockResolvedValue(true)
    vi.mocked(API.removeNodeFromCategory).mockResolvedValue(true)
  })

  afterEach(() => vi.useRealTimers())

  it('loads nodes and reflects membership from each node\'s categories', async () => {
    await mountDialog()
    const t = toggles()
    expect(t.length).toBe(3)
    expect(t[0].props('modelValue')).toBe(true)   // node 1 is a member
    expect(t[1].props('modelValue')).toBe(false)  // node 2 is not
  })

  it('adds a node when toggled on, removes when toggled off', async () => {
    await mountDialog()
    await toggles()[1].trigger('click') // node 2 off -> on
    await flushPromises()
    expect(API.addNodeToCategory).toHaveBeenCalledWith('Routers', 2)

    await toggles()[0].trigger('click') // node 1 on -> off
    await flushPromises()
    expect(API.removeNodeFromCategory).toHaveBeenCalledWith('Routers', 1)
  })

  it('locks requisitioned nodes by default and unlocks them via the override', async () => {
    await mountDialog()
    expect(wrapper.find('[data-test="requisitioned-warning"]').exists()).toBe(true)
    expect(toggles()[2].props('disabled')).toBe(true) // node 3 requisitioned, locked

    await wrapper.find('.cb-stub').trigger('change') // allow requisitioned
    await flushPromises()
    expect(toggles()[2].props('disabled')).toBe(false)
  })

  it('reverts the optimistic toggle when the add call fails', async () => {
    vi.mocked(API.addNodeToCategory).mockResolvedValue(false)
    await mountDialog()
    await toggles()[1].trigger('click')
    await flushPromises()
    expect(toggles()[1].props('modelValue')).toBe(false) // reverted
  })

  it('searches server-side (sends a label== FIQL filter) after typing', async () => {
    vi.useFakeTimers()
    await wrapper?.unmount?.()
    wrapper = mount(CategoryNodesDialog, {
      props: { visible: true, categoryId: CAT_ID, categoryName: 'Routers' },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub, ToggleSwitch: ToggleStub, Checkbox: CheckboxStub } }
    })
    await Promise.resolve()
    vi.mocked(API.getNodes).mockClear()
    await wrapper.find('[data-test="node-search"]').setValue('rout')
    vi.advanceTimersByTime(350)
    await Promise.resolve()
    const params = vi.mocked(API.getNodes).mock.calls.at(-1)?.[0] as any
    expect(params._s).toBe('label==*rout*')
  })

  it('shows an error state when the node fetch fails', async () => {
    vi.mocked(API.getNodes).mockResolvedValue(false as any)
    await mountDialog()
    expect(wrapper.find('[data-test="nodes-empty"]').text()).toContain('Could not load')
  })
})
