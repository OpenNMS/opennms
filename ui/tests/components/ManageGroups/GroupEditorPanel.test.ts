import GroupEditorPanel from '@/components/ManageGroups/GroupEditorPanel.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { adminWriteSuccess } from '@/types/adminWrite'

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
      memberCandidates: ['admin', 'jose', 'ana', 'zed'],
      availableCategories: ['Routers', 'Servers', 'Switches'],
      createGroup: vi.fn().mockResolvedValue(adminWriteSuccess()),
      updateGroup: vi.fn().mockResolvedValue(adminWriteSuccess())
    }
    vi.mocked(useGroupAdminStore).mockReturnValue(store)
  })

  it('flags a look-alike of the reserved group name and disables saving', async () => {
    await mountPanel()
    await wrapper.find('[data-test="group-name-input"]').setValue('ADMIN')

    expect(wrapper.text()).toContain('The group name "ADMIN" is reserved.')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
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

  describe('tabs', () => {
    const CATEGORY_HINT = 'Used when ACLs are enabled (org.opennms.web.aclsEnabled): members of this group see only nodes in these categories.'

    const mountAttached = async (group: any = null) => {
      wrapper = mount(GroupEditorPanel, {
        props: { group },
        global: { plugins: [PrimeVue] },
        attachTo: document.body
      })
      await flushPromises()
    }

    const save = () => wrapper.find('[data-test="save-button"]')
    const categorySelect = (): VueWrapper<any> => wrapper.findComponent('[data-test="categories-select"]') as VueWrapper<any>
    const memberSelect = (): VueWrapper<any> => wrapper.findComponent('[data-test="add-member-select"]') as VueWrapper<any>

    it('holds name, comments and members on the Group tab, the editor on Duty Schedules and the multi-select on Categories', async () => {
      await mountAttached()

      expect(wrapper.find('[data-test="tab-group"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="tab-duty-schedules"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="tab-categories"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="group-name-input"]').isVisible()).toBe(true)
      expect(wrapper.find('[data-test="group-comments-input"]').isVisible()).toBe(true)
      expect(wrapper.find('[data-test="add-member-select"]').isVisible()).toBe(true)
      expect(wrapper.find('[data-test="add-duty-button"]').isVisible()).toBe(false)
      expect(categorySelect().isVisible()).toBe(false)

      await wrapper.find('[data-test="tab-duty-schedules"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="add-duty-button"]').isVisible()).toBe(true)
      expect(wrapper.find('[data-test="group-name-input"]').isVisible()).toBe(false)

      await wrapper.find('[data-test="tab-categories"]').trigger('click')
      await flushPromises()
      expect(categorySelect().isVisible()).toBe(true)
      expect(categorySelect().props('options')).toEqual(['Routers', 'Servers', 'Switches'])
      expect(wrapper.text()).toContain(CATEGORY_HINT)
      wrapper.unmount()
    })

    it('opens another group on the Group tab', async () => {
      await mountAttached({ name: 'Ops', users: [], version: 1 })
      await wrapper.find('[data-test="tab-categories"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="group-comments-input"]').isVisible()).toBe(false)

      await wrapper.setProps({ group: { name: 'Dev', users: [], version: 2 }})
      expect(wrapper.find('[data-test="group-comments-input"]').isVisible()).toBe(true)
      wrapper.unmount()
    })

    it('loads the duty schedules and categories of the group and saves them back', async () => {
      await mountPanel({ name: 'Ops', users: [], dutySchedules: ['MoWeFr800-1700'], categories: ['Servers', 'Routers'], version: 3 })

      expect((wrapper.find('[data-test="duty-0-begin"]').element as HTMLInputElement).value).toBe('08:00')
      expect(categorySelect().props('modelValue')).toEqual(['Servers', 'Routers'])

      await wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()

      expect(store.updateGroup).toHaveBeenCalledWith(expect.objectContaining({
        name: 'Ops',
        dutySchedules: ['MoWeFr800-1700'],
        categories: ['Routers', 'Servers'],
        version: 3
      }))
    })

    it('sends the chosen categories sorted on update and on create', async () => {
      await mountPanel({ name: 'Ops', users: [], categories: [], version: 3 })
      await categorySelect().vm.$emit('update:modelValue', ['Switches', 'Routers'])
      await save().trigger('click')
      await flushPromises()
      expect(store.updateGroup).toHaveBeenCalledWith(expect.objectContaining({ categories: ['Routers', 'Switches'] }))

      wrapper.unmount()
      await mountPanel()
      await wrapper.find('[data-test="group-name-input"]').setValue('NewGroup')
      await categorySelect().vm.$emit('update:modelValue', ['Switches', 'Servers'])
      await save().trigger('click')
      await flushPromises()
      expect(store.createGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'NewGroup', categories: ['Servers', 'Switches'] }))
    })

    it('does not invent an empty category list for a group that came without one', async () => {
      await mountPanel({ name: 'Ops', users: [], version: 3 })
      await save().trigger('click')
      await flushPromises()

      expect(store.updateGroup.mock.calls[0][0].categories).toBeUndefined()
    })

    it('sends an empty list once the categories of a group have been cleared', async () => {
      await mountPanel({ name: 'Ops', users: [], categories: ['Routers'], version: 3 })
      await categorySelect().vm.$emit('update:modelValue', [])
      await save().trigger('click')
      await flushPromises()

      expect(store.updateGroup.mock.calls[0][0].categories).toEqual([])
    })

    it('adds the chosen users in candidate order and does not offer members again', async () => {
      await mountPanel({ name: 'Ops', users: ['jose'], version: 1 })
      expect(memberSelect().props('options')).toEqual(['admin', 'ana', 'zed'])

      await memberSelect().vm.$emit('update:modelValue', ['zed', 'admin'])
      await wrapper.find('[data-test="add-member-button"]').trigger('click')

      expect(wrapper.findAll('[data-test="member-list"] .member-name').map(n => n.text())).toEqual(['jose', 'admin', 'zed'])
      expect(memberSelect().props('options')).toEqual(['ana'])
      expect(wrapper.find('[data-test="add-member-button"]').attributes('disabled')).toBeDefined()
    })

    it('still reorders and removes members', async () => {
      await mountPanel({ name: 'Ops', users: ['admin', 'jose', 'ana'], version: 1 })
      await wrapper.findAll('[data-test="move-up-button"]')[2].trigger('click')
      await wrapper.findAll('[data-test="remove-member-button"]')[0].trigger('click')
      await save().trigger('click')
      await flushPromises()

      expect(store.updateGroup.mock.calls[0][0].users).toEqual(['ana', 'jose'])
    })

    it('round-trips a multi-line comment unchanged', async () => {
      const comment = 'line one\nline two\n\nline four'
      await mountPanel({ name: 'Ops', comments: comment, users: [], version: 1 })
      const input = wrapper.find('[data-test="group-comments-input"]')
      expect(input.element.tagName).toBe('TEXTAREA')
      expect((input.element as HTMLTextAreaElement).value).toBe(comment)

      await save().trigger('click')
      await flushPromises()
      expect(store.updateGroup.mock.calls[0][0].comments).toBe(comment)
    })

    it('keeps Save disabled for a dayless duty row while another tab is active', async () => {
      await mountAttached({ name: 'Ops', users: [], version: 1 })
      await wrapper.find('[data-test="tab-duty-schedules"]').trigger('click')
      await flushPromises()
      await wrapper.find('[data-test="add-duty-button"]').trigger('click')
      expect(save().attributes('disabled')).toBeDefined()

      await wrapper.find('[data-test="tab-group"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="group-comments-input"]').isVisible()).toBe(true)
      expect(save().attributes('disabled')).toBeDefined()

      await wrapper.find('[data-test="tab-duty-schedules"]').trigger('click')
      await flushPromises()
      await wrapper.find('[data-test="duty-0-day-Mo"]').trigger('click')
      expect(save().attributes('disabled')).toBeUndefined()
      wrapper.unmount()
    })
  })
})
