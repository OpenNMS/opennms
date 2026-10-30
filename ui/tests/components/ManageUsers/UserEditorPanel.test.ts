import UserEditorPanel from '@/components/ManageUsers/UserEditorPanel.vue'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { OnmsSelect } from '@opennms/onms-ui'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { adminWriteSuccess } from '@/types/adminWrite'

vi.mock('@/stores/userAdminStore')

describe('UserEditorPanel.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountPanel = async (user: any = null) => {
    wrapper = mount(UserEditorPanel, {
      props: { user },
      global: { plugins: [PrimeVue] }
    })
    await flushPromises()
  }

  const setPassword = async (value: string) => {
    await wrapper.find('[data-test="password-input"] input').setValue(value)
    await wrapper.find('[data-test="password-confirm-input"] input').setValue(value)
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      availableRoles: ['ROLE_USER', 'ROLE_ADMIN'],
      createUser: vi.fn().mockResolvedValue(adminWriteSuccess()),
      updateUser: vi.fn().mockResolvedValue(adminWriteSuccess())
    }
    vi.mocked(useUserAdminStore).mockReturnValue(store)
  })

  it('flags a user id with whitespace or reserved characters and disables saving', async () => {
    await mountPanel()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose anes')
    await setPassword('secret')

    expect(wrapper.find('#user-editor-id-error').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('flags a look-alike of a reserved user id and disables saving', async () => {
    await mountPanel()
    await wrapper.find('[data-test="user-id-input"]').setValue('ADMIN')
    await setPassword('secret')

    expect(wrapper.find('#user-editor-id-error').text()).toBe('The user-id "ADMIN" is reserved.')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('flags an email without a domain part', async () => {
    await mountPanel()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')
    await setPassword('secret')
    await wrapper.find('[data-test="email-input"]').setValue('not-an-email')

    expect(wrapper.find('#user-editor-email-error').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('requires a password before a new user can be saved', async () => {
    await mountPanel()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')

    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('creates a valid user and closes', async () => {
    await mountPanel()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')
    await setPassword('secret')
    await wrapper.find('[data-test="email-input"]').setValue('jose@example.org')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.createUser).toHaveBeenCalledWith(expect.objectContaining({ userId: 'jose', password: 'secret' }))
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('loads and saves the phone, pager, PIN, time-zone and duty-schedule fields', async () => {
    await mountPanel({
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
    // the duty schedule is parsed into the day/time widget
    expect((wrapper.find('[data-test="duty-0-begin"]').element as HTMLInputElement).value).toBe('08:00')
    expect((wrapper.find('[data-test="duty-0-end"]').element as HTMLInputElement).value).toBe('17:00')

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
      // round-trips back to the canonical string form
      dutySchedules: ['MoWeFr800-1700']
    }))
  })

  it('builds a duty schedule from day toggles and the default times', async () => {
    await mountPanel({ userId: 'jose', dutySchedules: [], roles: [] })

    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    for (const day of ['Mo', 'Tu', 'We', 'Th', 'Fr']) {
      await wrapper.find(`[data-test="duty-0-day-${day}"]`).trigger('click')
    }
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    // default 09:00-17:00 serialize to 900-1700
    expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({
      dutySchedules: ['MoTuWeThFr900-1700']
    }))
  })

  it('disables Save while a duty schedule row has no day', async () => {
    await mountPanel({ userId: 'jose', dutySchedules: [], roles: [] })
    const save = () => wrapper.find('[data-test="save-button"]').attributes('disabled')
    expect(save()).toBeUndefined()

    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    expect(save()).toBeDefined()

    await wrapper.find('[data-test="duty-0-day-Mo"]').trigger('click')
    expect(save()).toBeUndefined()
  })

  it('shows a server rejection in the panel and stays open', async () => {
    store.createUser.mockResolvedValue({ message: 'User jose already exists.', stale: false })
    await mountPanel()
    await wrapper.find('[data-test="user-id-input"]').setValue('jose')
    await setPassword('secret')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="save-error"]').text()).toContain('already exists')
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('shows the user fields first and the duty schedules on their own tab', async () => {
    // isVisible needs the panel in the document; the inactive tab is hidden with v-show
    wrapper = mount(UserEditorPanel, {
      props: { user: { userId: 'jose', dutySchedules: [], roles: [], version: 1 }},
      global: { plugins: [PrimeVue] },
      attachTo: document.body
    })
    await flushPromises()

    expect(wrapper.find('[data-test="email-input"]').isVisible()).toBe(true)
    expect(wrapper.find('[data-test="roles-select"]').isVisible()).toBe(true)
    expect(wrapper.find('[data-test="add-duty-button"]').isVisible()).toBe(false)

    await wrapper.find('[data-test="tab-duty-schedules"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="add-duty-button"]').isVisible()).toBe(true)
    expect(wrapper.find('[data-test="email-input"]').isVisible()).toBe(false)

    // another user opens on the User tab again
    await wrapper.setProps({ user: { userId: 'ana', dutySchedules: [], roles: [], version: 2 }})
    expect(wrapper.find('[data-test="email-input"]').isVisible()).toBe(true)
    wrapper.unmount()
  })

  describe('time zone', () => {
    const saveUser = async () => {
      await wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
    }

    it('sends an empty string when the time zone is cleared', async () => {
      await mountPanel({ userId: 'jose', timeZoneId: 'America/New_York', dutySchedules: [], roles: [] })
      wrapper.findComponent(OnmsSelect).vm.$emit('update:modelValue', null)
      await flushPromises()
      await saveUser()

      expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({ timeZoneId: '' }))
    })

    it('sends the stored time zone when it is untouched', async () => {
      await mountPanel({ userId: 'jose', timeZoneId: 'America/New_York', dutySchedules: [], roles: [] })
      await saveUser()

      expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({ timeZoneId: 'America/New_York' }))
    })

    it('offers a stored alias missing from the browser list, selected, and keeps it on save', async () => {
      await mountPanel({ userId: 'jose', timeZoneId: 'US/Eastern', dutySchedules: [], roles: [] })
      const select = wrapper.findComponent(OnmsSelect)

      expect(select.props('options')).toContain('US/Eastern')
      expect(select.props('modelValue')).toBe('US/Eastern')
      await saveUser()

      expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({ timeZoneId: 'US/Eastern' }))
    })
  })

  describe('confirm password on create', () => {
    const confirm = () => wrapper.find('[data-test="password-confirm-input"] input')
    const saveDisabled = () => wrapper.find('[data-test="save-button"]').attributes('disabled')

    it('keeps Save disabled and shows the mismatch message until the passwords match', async () => {
      await mountPanel()
      await wrapper.find('[data-test="user-id-input"]').setValue('jose')
      await wrapper.find('[data-test="password-input"] input').setValue('secret')
      expect(saveDisabled()).toBeDefined()

      await confirm().setValue('secrex')
      expect(wrapper.text()).toContain('Passwords do not match')
      expect(saveDisabled()).toBeDefined()

      await confirm().setValue('secret')
      expect(wrapper.text()).not.toContain('Passwords do not match')
      expect(saveDisabled()).toBeUndefined()
    })

    it('is not shown when editing a user', async () => {
      await mountPanel({ userId: 'jose', dutySchedules: [], roles: [] })

      expect(wrapper.find('[data-test="password-input"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="password-confirm-input"]').exists()).toBe(false)
    })
  })

  describe('comments', () => {
    it('is a textarea whose multi-line value round-trips unchanged', async () => {
      await mountPanel({ userId: 'jose', userComments: 'line one\nline two', dutySchedules: [], roles: [] })
      const input = wrapper.find('[data-test="comments-input"]')

      expect(input.element.tagName).toBe('TEXTAREA')
      expect((input.element as HTMLTextAreaElement).value).toBe('line one\nline two')
    })

    it('accepts a newly typed multi-line comment and sends its inner newlines', async () => {
      await mountPanel({ userId: 'jose', dutySchedules: [], roles: [] })
      await wrapper.find('[data-test="comments-input"]').setValue('  first\nsecond\n\nthird  ')

      expect(wrapper.find('#user-editor-comments-error').exists()).toBe(false)
      await wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()

      expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({ userComments: 'first\nsecond\n\nthird' }))
    })
  })

  it('closes on Back and on Cancel without saving', async () => {
    await mountPanel({ userId: 'jose', dutySchedules: [], roles: [] })
    await wrapper.find('[data-test="back-button"]').trigger('click')
    await wrapper.find('[data-test="cancel-button"]').trigger('click')

    expect(wrapper.emitted('close')).toHaveLength(2)
    expect(store.updateUser).not.toHaveBeenCalled()
  })
})
