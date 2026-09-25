import UserEditor from '@/containers/UserEditor.vue'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/userAdminStore')
vi.mock('@/stores/menuStore', () => ({
  useMenuStore: () => ({ mainMenu: { homeUrl: '/opennms/index.jsp' }})
}))

const push = vi.fn()
let routeUserId = 'create'
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { userId: routeUserId }}),
  useRouter: () => ({ push })
}))

describe('UserEditor.vue (page)', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountPage = async (userId = 'create') => {
    routeUserId = userId
    wrapper = mount(UserEditor, {
      global: {
        plugins: [PrimeVue],
        stubs: {
          BreadCrumbs: true,
          DutySchedulesTab: true,
          TimeZonePicker: true,
          TogglePanel: {
            props: ['header', 'collapsed'],
            template: '<div :data-collapsed="String(collapsed)" data-test="more-contacts-panel"><slot v-if="!collapsed" /></div>'
          }
        }
      }
    })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      users: [{
        userId: 'jose', fullName: 'Jose', userComments: '', email: 'j@x.org',
        dutySchedules: ['Mo900-1700'], roles: ['ROLE_USER'], timeZoneId: null
      }],
      availableRoles: ['ROLE_USER', 'ROLE_ADMIN'],
      populate: vi.fn().mockResolvedValue(undefined),
      createUser: vi.fn().mockResolvedValue(null),
      updateUser: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useUserAdminStore).mockReturnValue(store)
  })

  it('renders the three tabs of NMS-20281', async () => {
    await mountPage()
    expect(wrapper.find('[data-test="tab-general"]').text()).toBe('General Information')
    expect(wrapper.find('[data-test="tab-duty"]').text()).toBe('Duty Schedule')
    expect(wrapper.find('[data-test="tab-roles"]').text()).toBe('Roles')
  })

  it('creates through the page and returns to the list', async () => {
    await mountPage()
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('Create New User')

    await wrapper.find('[data-test="user-id-input"]').setValue('newuser')
    await wrapper.find('[data-test="password-input"] input').setValue('secret')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.createUser).toHaveBeenCalledWith(expect.objectContaining({ userId: 'newuser', password: 'secret' }))
    expect(push).toHaveBeenCalledWith({ path: '/admin/users' })
  })

  it('loads an existing user, hides id/password, and updates in place', async () => {
    await mountPage('jose')
    expect(wrapper.find('[data-test="editor-title"]').text()).toBe('Edit User: jose')
    expect(wrapper.find('[data-test="user-id-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="password-input"]').exists()).toBe(false)
    expect((wrapper.find('[data-test="email-input"]').element as HTMLInputElement).value).toBe('j@x.org')

    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({
      userId: 'jose',
      dutySchedules: ['Mo900-1700']
    }))
    expect(push).toHaveBeenCalledWith({ path: '/admin/users' })
  })

  it('shows a server rejection on the page and stays there', async () => {
    store.createUser.mockResolvedValue('User newuser already exists.')
    await mountPage()
    await wrapper.find('[data-test="user-id-input"]').setValue('newuser')
    await wrapper.find('[data-test="password-input"] input').setValue('secret')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="editor-error"]').text()).toContain('already exists')
    expect(push).not.toHaveBeenCalled()
  })

  it('folds rare contact methods away on create, but shows them for a user who has one', async () => {
    await mountPage()
    // TogglePanel stub below renders collapsed state as an attribute
    expect(wrapper.find('[data-test="more-contacts-panel"]').attributes('data-collapsed')).toBe('true')

    store.users = [{ userId: 'pager-user', workPhone: '555-1234', dutySchedules: [], roles: [] }]
    await mountPage('pager-user')
    expect(wrapper.find('[data-test="more-contacts-panel"]').attributes('data-collapsed')).toBe('false')
  })

  it('reports a missing user instead of offering a blank form', async () => {
    await mountPage('gone')

    expect(wrapper.find('[data-test="editor-not-found"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').exists()).toBe(false)
  })
})
