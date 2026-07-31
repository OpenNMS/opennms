import CategoryNodesDialog from '@/components/ManageCategories/CategoryNodesDialog.vue'
import API from '@/services'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services', () => ({
  default: {
    listAllNodes: vi.fn(),
    listCategoryNodes: vi.fn(),
    addNodeToCategory: vi.fn(),
    removeNodeFromCategory: vi.fn()
  }
}))

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}
// stub Listbox so we can read its options and drive its selection directly
const ListboxStub = {
  name: 'Listbox',
  props: ['modelValue', 'options'],
  emits: ['update:modelValue'],
  template: '<div class="listbox-stub" />'
}

describe('CategoryNodesDialog.vue', () => {
  let wrapper: VueWrapper<any>

  const mountDialog = async () => {
    wrapper = mount(CategoryNodesDialog, {
      props: { visible: false, categoryName: 'Routers' },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub, Listbox: ListboxStub } }
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  // [0] = available listbox, [1] = member listbox
  const listboxes = () => wrapper.findAllComponents(ListboxStub)

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(API.listAllNodes).mockResolvedValue([
      { id: 1, label: 'node-a' }, { id: 2, label: 'node-b' }, { id: 3, label: 'node-c' }
    ])
    vi.mocked(API.listCategoryNodes).mockResolvedValue([{ id: 1, label: 'node-a' }])
    vi.mocked(API.addNodeToCategory).mockResolvedValue(true)
    vi.mocked(API.removeNodeFromCategory).mockResolvedValue(true)
  })

  it('splits nodes into available and member lists', async () => {
    await mountDialog()
    const [available, member] = listboxes()
    expect((member.props('options') as any[]).map((n) => n.id)).toEqual([1])
    expect((available.props('options') as any[]).map((n) => n.id).sort()).toEqual([2, 3])
  })

  it('applies only the diff (adds newly moved members, removes dropped ones)', async () => {
    await mountDialog()
    let [available, member] = listboxes()
    // select node 2 in available and add it
    available.vm.$emit('update:modelValue', [{ id: 2, label: 'node-b' }])
    await flushPromises()
    await wrapper.find('[data-test="add-selected"]').trigger('click')
    await flushPromises()
    // select node 1 in members and remove it
    ;[available, member] = listboxes()
    member.vm.$emit('update:modelValue', [{ id: 1, label: 'node-a' }])
    await flushPromises()
    await wrapper.find('[data-test="remove-selected"]').trigger('click')
    await flushPromises()

    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(API.addNodeToCategory).toHaveBeenCalledWith('Routers', 2)
    expect(API.removeNodeFromCategory).toHaveBeenCalledWith('Routers', 1)
    expect(API.addNodeToCategory).toHaveBeenCalledTimes(1)
    expect(API.removeNodeFromCategory).toHaveBeenCalledTimes(1)
  })

  it('save is disabled when nothing changed', async () => {
    await mountDialog()
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('add-all moves every available node into the category', async () => {
    await mountDialog()
    await wrapper.find('[data-test="add-all"]').trigger('click')
    await flushPromises()
    const [available, member] = listboxes()
    expect((available.props('options') as any[]).length).toBe(0)
    expect((member.props('options') as any[]).map((n) => n.id).sort()).toEqual([1, 2, 3])
  })

  it('shows an error and disables Save when the member fetch fails (never arms a destructive diff)', async () => {
    vi.mocked(API.listCategoryNodes).mockResolvedValue(null as any)
    await mountDialog()
    expect(wrapper.find('[data-test="nodes-load-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('shows an error and disables Save when the all-nodes fetch fails', async () => {
    vi.mocked(API.listAllNodes).mockResolvedValue(null as any)
    await mountDialog()
    expect(wrapper.find('[data-test="nodes-load-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('does not arm a removal when a member id is missing from the all-nodes list (TOCTOU)', async () => {
    // node 9 is a member but was NOT returned by listAllNodes (added/skewed between the two fetches)
    vi.mocked(API.listAllNodes).mockResolvedValue([{ id: 1, label: 'node-a' }, { id: 2, label: 'node-b' }])
    vi.mocked(API.listCategoryNodes).mockResolvedValue([{ id: 9, label: 'node-i' }])
    await mountDialog()
    // clean on open: Save disabled, and the member appears in the members list
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    const [, member] = listboxes()
    expect((member.props('options') as any[]).map((n) => n.id)).toContain(9)
  })

  it('warns when requisitioned nodes are present', async () => {
    vi.mocked(API.listAllNodes).mockResolvedValue([{ id: 1, label: 'node-a', requisitioned: true }])
    vi.mocked(API.listCategoryNodes).mockResolvedValue([])
    await mountDialog()
    expect(wrapper.find('[data-test="requisitioned-warning"]').exists()).toBe(true)
  })
})

describe('CategoryNodesDialog.vue — real Listbox (filter must actually render)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(API.listAllNodes).mockResolvedValue([{ id: 1, label: 'node-a' }, { id: 2, label: 'node-b' }])
    vi.mocked(API.listCategoryNodes).mockResolvedValue([])
  })

  it('renders a real filter/search input on each list (regression: PickList filterBy did nothing in v4)', async () => {
    const wrapper = mount(CategoryNodesDialog, {
      props: { visible: false, categoryName: 'Routers' },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub } }
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    // PrimeVue Listbox renders its filter as an input inside .p-listbox-filter-container
    const filters = wrapper.findAll('.p-listbox-filter, input[data-pc-name="pcfilter"], .p-listbox-filter-container input')
    expect(filters.length).toBeGreaterThanOrEqual(2)
  })
})
