import GroupEditorPanel from '@/components/ManageGroups/GroupEditorPanel.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/groupAdminStore')

describe('GroupEditorPanel.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountPanel = async (group: any = null) => {
    wrapper = mount(GroupEditorPanel, {
      props: { group },
      global: { plugins: [PrimeVue] }
    })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      memberCandidates: ['admin', 'jose'],
      createGroup: vi.fn().mockResolvedValue(null),
      updateGroup: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useGroupAdminStore).mockReturnValue(store)
  })

  it('flags a group name with whitespace and disables saving', async () => {
    await mountPanel()
    await wrapper.find('[data-test="group-name-input"]').setValue('Test Group')

    expect(wrapper.find('#group-editor-name-error').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    expect(store.createGroup).not.toHaveBeenCalled()
  })

  it('flags markup in the comments and disables saving', async () => {
    await mountPanel()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="group-comments-input"]').setValue('<b>styled</b>')

    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('keeps a group with a pre-existing markup comment editable', async () => {
    await mountPanel({ name: 'Legacy', comments: 'Bob\'s R&D team', users: [] })

    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(false)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="group-comments-input"]').setValue('<b>changed</b>')
    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(true)
  })

  it('submits a valid group and closes', async () => {
    await mountPanel()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="group-comments-input"]').setValue('Test Group')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.createGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'TestGroup', comments: 'Test Group' }))
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows a server rejection in the panel and stays open', async () => {
    store.createGroup.mockResolvedValue({ message: 'Group TestGroup already exists.', stale: false })
    await mountPanel()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="save-error"]').text()).toContain('already exists')
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('reloads the form and clears a previous error when the group changes', async () => {
    store.updateGroup.mockResolvedValue({ message: 'rejected', stale: false })
    await mountPanel({ name: 'Ops', comments: 'old', users: [], version: 1 })
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="save-error"]').exists()).toBe(true)

    await wrapper.setProps({ group: { name: 'Dev', comments: 'new', users: [], version: 2 }})
    expect(wrapper.find('[data-test="save-error"]').exists()).toBe(false)
    expect((wrapper.find('[data-test="group-comments-input"]').element as HTMLInputElement).value).toBe('new')
  })

  it('closes on Back and on Cancel without saving', async () => {
    await mountPanel({ name: 'Ops', users: [], version: 1 })
    await wrapper.find('[data-test="back-button"]').trigger('click')
    await wrapper.find('[data-test="cancel-button"]').trigger('click')

    expect(wrapper.emitted('close')).toHaveLength(2)
    expect(store.updateGroup).not.toHaveBeenCalled()
  })
})
