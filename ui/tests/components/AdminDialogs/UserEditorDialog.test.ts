import UserEditorDialog from '@/components/ManageUsers/UserEditorDialog.vue'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/userAdminStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

describe('UserEditorDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (user: any = null) => {
    wrapper = mount(UserEditorDialog, {
      props: { visible: false, user },
      global: {
        plugins: [PrimeVue],
        stubs: { Dialog: DialogStub }
      }
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  const setPassword = async (value: string) => {
    await wrapper.find('[data-test="password-input"] input').setValue(value)
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      availableRoles: ['ROLE_USER', 'ROLE_ADMIN'],
      createUser: vi.fn().mockResolvedValue(null),
      updateUser: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useUserAdminStore).mockReturnValue(store)
  })

  it('flags a user id with whitespace or reserved characters and disables saving', async () => {
    await mountDialog()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose anes')
    await setPassword('secret')

    expect(wrapper.find('#user-editor-id-error').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('flags an email without a domain part', async () => {
    await mountDialog()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')
    await setPassword('secret')
    await wrapper.find('[data-test="email-input"]').setValue('not-an-email')

    expect(wrapper.find('#user-editor-email-error').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('requires a password before a new user can be saved', async () => {
    await mountDialog()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')

    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('creates a valid user and closes', async () => {
    await mountDialog()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')
    await setPassword('secret')
    await wrapper.find('[data-test="email-input"]').setValue('jose@example.org')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.createUser).toHaveBeenCalledWith(expect.objectContaining({ userId: 'jose', password: 'secret' }))
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('loads and saves the phone, pager, PIN, time-zone and duty-schedule fields', async () => {
    await mountDialog({
      userId: 'jose',
      workPhone: '555-1000',
      mobilePhone: '555-2000',
      homePhone: '555-3000',
      numericPagerService: 'AcmePage',
      numericPagerPin: '12345',
      textPagerService: 'AcmeText',
      textPagerPin: '67890',
      tuiPin: '9999',
      timeZoneId: 'America/New_York',
      dutySchedules: ['MoWeFr800-1700'],
      roles: []
    })

    expect((wrapper.find('[data-test="work-phone-input"]').element as HTMLInputElement).value).toBe('555-1000')
    expect((wrapper.find('[data-test="numeric-pin-input"]').element as HTMLInputElement).value).toBe('12345')
    expect((wrapper.find('[data-test="tui-pin-input"]').element as HTMLInputElement).value).toBe('9999')
    expect((wrapper.find('[data-test="duty-input-0"]').element as HTMLInputElement).value).toBe('MoWeFr800-1700')

    await wrapper.find('[data-test="mobile-phone-input"]').setValue('555-9999')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({
      workPhone: '555-1000',
      mobilePhone: '555-9999',
      homePhone: '555-3000',
      numericPagerService: 'AcmePage',
      numericPagerPin: '12345',
      textPagerService: 'AcmeText',
      textPagerPin: '67890',
      tuiPin: '9999',
      dutySchedules: ['MoWeFr800-1700']
    }))
  })

  it('adds and removes duty schedule rows', async () => {
    await mountDialog({ userId: 'jose', dutySchedules: [], roles: [] })

    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    await wrapper.find('[data-test="duty-input-0"]').setValue('MoTuWeThFr900-1700')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({
      dutySchedules: ['MoTuWeThFr900-1700']
    }))
  })

  it('shows a server rejection inside the dialog and stays open', async () => {
    store.createUser.mockResolvedValue('User jose already exists.')
    await mountDialog()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')
    await setPassword('secret')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
    expect(wrapper.emitted('update:visible') ?? []).toEqual([])
  })
})
