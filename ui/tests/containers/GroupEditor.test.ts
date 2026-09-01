import GroupEditor from '@/containers/GroupEditor.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/groupAdminStore')
vi.mock('@/stores/menuStore', () => ({
  useMenuStore: () => ({ mainMenu: { homeUrl: '/opennms/index.jsp' }})
}))

const push = vi.fn()
let routeGroupName = 'create'
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { groupName: routeGroupName }}),
  useRouter: () => ({ push })
}))

describe('GroupEditor.vue (page)', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountPage = async (groupName = 'create') => {
    routeGroupName = groupName
    wrapper = mount(GroupEditor, {
      global: {
        plugins: [PrimeVue],
        stubs: { BreadCrumbs: true }
      }
    })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      groups: [{ name: 'Ops', comments: 'ops team', users: ['admin'] }],
      memberCandidates: ['admin', 'jose'],
      populate: vi.fn().mockResolvedValue(undefined),
      createGroup: vi.fn().mockResolvedValue(null),
      updateGroup: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useGroupAdminStore).mockReturnValue(store)
  })

  it('creates through the page and returns to the list', async () => {
    await mountPage()
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('Create New Group')

    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="group-comments-input"]').setValue('Test Group')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.createGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'TestGroup', comments: 'Test Group' }))
    expect(push).toHaveBeenCalledWith({ path: '/admin/groups' })
  })

  it('loads an existing group, hides the name field, and updates in place', async () => {
    await mountPage('Ops')
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('Edit Group: Ops')
    expect(wrapper.find('[data-test="group-name-input"]').exists()).toBe(false)
    expect((wrapper.find('[data-test="group-comments-input"]').element as HTMLInputElement).value).toBe('ops team')
    expect(wrapper.find('[data-test="member-list"]').text()).toContain('admin')

    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.updateGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'Ops', users: ['admin'] }))
    expect(push).toHaveBeenCalledWith({ path: '/admin/groups' })
  })

  it('flags a group name with whitespace and disables saving', async () => {
    await mountPage()
    await wrapper.find('[data-test="group-name-input"]').setValue('Test Group')

    expect(wrapper.find('#group-editor-name-error').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    expect(store.createGroup).not.toHaveBeenCalled()
  })

  it('keeps a group with a pre-existing markup comment editable', async () => {
    store.groups = [{ name: 'Legacy', comments: 'Bob\'s R&D team', users: [] }]
    await mountPage('Legacy')

    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(false)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="group-comments-input"]').setValue('<b>changed</b>')
    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(true)
  })

  it('shows a server rejection on the page and stays there', async () => {
    store.createGroup.mockResolvedValue('Group TestGroup already exists.')
    await mountPage()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="editor-error"]').text()).toContain('already exists')
    expect(push).not.toHaveBeenCalled()
  })

  it('reports a missing group instead of offering a blank form that would overwrite it', async () => {
    await mountPage('gone')

    expect(wrapper.find('[data-test="editor-not-found"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="editor-error"]').text()).toContain('was not found')
    expect(wrapper.find('[data-test="save-button"]').exists()).toBe(false)
  })
})
