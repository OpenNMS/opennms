import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import UsersAbout from '@/components/ManageUsers/UsersAbout.vue'
import UsersTable from '@/components/ManageUsers/UsersTable.vue'

const store = vi.hoisted(() => ({
  openUserEditor: vi.fn(),
  deleteUser: vi.fn(),
  users: [] as unknown[]
}))

vi.mock('@/stores/userAdminStore', async () => {
  const { reactive } = await import('vue')
  const live = reactive(store)
  return { useUserAdminStore: () => live }
})

vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar: vi.fn() }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

enableAutoUnmount(afterEach)

// records the tooltip text on the element, so that a test can read it
const tooltipStub = {
  mounted: (el: HTMLElement, binding: { value: string }) => el.setAttribute('data-tooltip', String(binding.value)),
  updated: (el: HTMLElement, binding: { value: string }) => el.setAttribute('data-tooltip', String(binding.value))
}

const users = [
  { userId: 'DOMAIN\\jdoe', fullName: 'Jane Doe', email: 'jane@example.org', userComments: 'Migrated from the old domain', roles: ['ROLE_USER'], version: 1 },
  { userId: 'admin', fullName: 'Administrator', email: 'root@example.org', userComments: 'The built-in admin', roles: ['ROLE_ADMIN'], version: 2 },
  { userId: 'ro', fullName: 'Read Only', email: 'ro@example.org', userComments: 'Looks, never touches', roles: [], readOnly: true, version: 3 }
]

const mountTable = () => mount(UsersTable, {
  global: { plugins: [PrimeVue], directives: { 'onms-tooltip': tooltipStub, tooltip: tooltipStub }}
})

const ids = (wrapper: ReturnType<typeof mountTable>) => wrapper.findAll('.user-id').map(e => e.text())

describe('UsersTable', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.users = users
  })

  it('offers Edit, Delete and the menu on a user-id with a backslash, without a file-managed note', () => {
    const wrapper = mountTable()
    const row = wrapper.findAll('tbody tr').find(r => r.text().includes('DOMAIN\\jdoe'))!

    expect(row.find('[data-test="edit-user-button"]').exists()).toBe(true)
    expect(row.find('[data-test="delete-user-button"]').exists()).toBe(true)
    expect(row.find('[data-test="user-actions-menu-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="unaddressable-note"]').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('file-managed')
  })

  describe('filter', () => {
    const type = async (wrapper: ReturnType<typeof mountTable>, value: string) => {
      await wrapper.find('[data-test="users-filter"]').setValue(value)
      await nextTick()
    }

    it.each([
      ['id, ignoring case', 'JD', ['DOMAIN\\jdoe']],
      ['full name', 'administrator', ['admin']],
      ['email', 'RO@EXAMPLE', ['ro']],
      ['comments', 'never touches', ['ro']]
    ])('matches the %s', async (_what, term, expected) => {
      const wrapper = mountTable()
      await type(wrapper, term)
      expect(ids(wrapper)).toEqual(expected)
    })

    it('treats markup as text', async () => {
      const wrapper = mountTable()
      await type(wrapper, '<b>')
      expect(ids(wrapper)).toEqual([])
      expect(wrapper.find('b').exists()).toBe(false)
    })

    it('renders markup in a stored name or comment as text, and filters on it', async () => {
      store.users = [
        ...users,
        { userId: 'marked', fullName: '<b>Bold</b> Name', email: 'm@example.org', userComments: '<img src=x onerror="alert(1)">', roles: [], version: 4 }
      ]
      const wrapper = mountTable()
      const row = wrapper.findAll('tbody tr').find(r => r.text().includes('marked'))!

      expect(row.text()).toContain('<b>Bold</b> Name')
      expect(row.text()).toContain('<img src=x onerror="alert(1)">')
      expect(wrapper.find('tbody b').exists()).toBe(false)
      expect(wrapper.find('tbody img').exists()).toBe(false)

      await type(wrapper, '<b>')
      expect(ids(wrapper)).toEqual(['marked'])
      expect(wrapper.find('tbody b').exists()).toBe(false)
    })

    it('says that nothing matches, and not that there are no users', async () => {
      const wrapper = mountTable()
      await type(wrapper, 'nobody-has-this')

      expect(ids(wrapper)).toEqual([])
      expect(wrapper.find('[data-test="no-match"]').text()).toBe('No users match the filter.')
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)

      await type(wrapper, '')
      expect(wrapper.find('[data-test="no-match"]').exists()).toBe(false)
    })

    it('shows every user again when the filter is cleared', async () => {
      const wrapper = mountTable()
      await type(wrapper, 'JD')
      await type(wrapper, '')
      expect(ids(wrapper)).toHaveLength(3)
    })
  })

  it('says in the delete confirmation when the server refuses a delete', async () => {
    const wrapper = mountTable()
    ;(wrapper.vm as unknown as { askDelete: (u: unknown) => void }).askDelete(users[0])
    await nextTick()

    // the dialog is teleported to the body
    expect(document.body.textContent).toContain('A user cannot be deleted while they supervise an on-call role or a destination path sends to them, or if they are the last administrator.')
  })

  it('shows the comments in their own column, with the full text as the tooltip', () => {
    const long = 'x'.repeat(300)
    store.users = [{ userId: 'long', userComments: long, version: 1 }]
    const wrapper = mountTable()

    const cell = wrapper.find('[data-test="user-comments"]')
    expect(cell.text()).toBe(long)
    expect(cell.attributes('data-tooltip')).toBe(long)
    expect(wrapper.text()).toContain('Comments')
  })

  it('tags a read-only user next to the id, and no other', () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr')
    const tagged = rows.filter(r => r.find('[data-test="read-only-tag"]').exists())

    expect(tagged).toHaveLength(1)
    expect(tagged[0].text()).toContain('ro')
    expect(tagged[0].find('[data-test="read-only-tag"]').text()).toBe('read-only')
  })
})

describe('UsersAbout', () => {
  const text = () => mount(UsersAbout).text()

  it('describes the database store, not users.xml', () => {
    expect(text()).not.toContain('users.xml')
  })

  it('names the Change Password menu entry', () => {
    expect(text()).toContain('Change Password')
  })

  it('explains the read-only tag as the ROLE_READONLY role, not as a page restriction', () => {
    const t = text().replace(/\s+/g, ' ')
    expect(t).toContain('A user with the ROLE_READONLY role carries a read-only tag next to its id: that account can view but not change anything in the web console.')
    expect(t).not.toContain('cannot be changed through this page')
  })

  it('names what the editor shows', () => {
    for (const word of ['Duty Schedules', 'time zone', 'Telephone PIN', 'Pager Email', 'Comments']) {
      expect(text().toLowerCase()).toContain(word.toLowerCase())
    }
    // the editor labels the field Telephone PIN
    expect(text()).not.toContain('TUI PIN')
  })

  it('warns that destination paths do not follow a rename', () => {
    expect(text().replace(/\s+/g, ' ')).toContain('Destination paths that target the user by name do not follow a rename; update them afterwards.')
  })
})
