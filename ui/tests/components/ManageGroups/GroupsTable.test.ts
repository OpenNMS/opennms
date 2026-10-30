import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import GroupsAbout from '@/components/ManageGroups/GroupsAbout.vue'
import GroupsTable from '@/components/ManageGroups/GroupsTable.vue'

const store = vi.hoisted(() => ({
  openGroupEditor: vi.fn(),
  deleteGroup: vi.fn(),
  groups: [] as unknown[]
}))

vi.mock('@/stores/groupAdminStore', async () => {
  const { reactive } = await import('vue')
  const live = reactive(store)
  return { useGroupAdminStore: () => live }
})

vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar: vi.fn() }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

enableAutoUnmount(afterEach)

const noop = { mounted: () => undefined }

const groups = [
  { name: 'ops/east', comments: 'East coast operators', users: ['alice'], version: 1 },
  { name: 'Admin', comments: 'Administrators', users: ['admin'], version: 2 },
  { name: 'Night', comments: 'Overnight cover', users: ['bob', 'Carol'], version: 3 }
]

const mountTable = () => mount(GroupsTable, {
  global: { plugins: [PrimeVue], directives: { 'onms-tooltip': noop, tooltip: noop }}
})

const names = (wrapper: ReturnType<typeof mountTable>) => wrapper.findAll('.group-name').map(e => e.text())

describe('GroupsTable', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.groups = groups
  })

  it('offers Edit, Delete and the menu on a group name with a slash, without a file-managed note', () => {
    const wrapper = mountTable()
    const row = wrapper.findAll('tbody tr').find(r => r.text().includes('ops/east'))!

    expect(row.find('[data-test="edit-group-button"]').exists()).toBe(true)
    expect(row.find('[data-test="delete-group-button"]').exists()).toBe(true)
    expect(row.find('[data-test="group-actions-menu-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="unaddressable-note"]').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('file-managed')
  })

  describe('filter', () => {
    const type = async (wrapper: ReturnType<typeof mountTable>, value: string) => {
      await wrapper.find('[data-test="groups-filter"]').setValue(value)
      await nextTick()
    }

    it.each([
      ['name, ignoring case', 'OPS', ['ops/east']],
      ['comments', 'overnight', ['Night']],
      ['member ids', 'CAROL', ['Night']]
    ])('matches the %s', async (_what, term, expected) => {
      const wrapper = mountTable()
      await type(wrapper, term)
      expect(names(wrapper)).toEqual(expected)
    })

    it('treats markup as text', async () => {
      const wrapper = mountTable()
      await type(wrapper, '<b>')
      expect(names(wrapper)).toEqual([])
      expect(wrapper.find('b').exists()).toBe(false)
    })
  })
})

describe('GroupsAbout', () => {
  const text = () => mount(GroupsAbout).text()

  it('describes the database store, not groups.xml', () => {
    expect(text()).not.toContain('groups.xml')
  })

  it('says a delete is refused while an on-call role or a destination path uses the group', () => {
    expect(text()).toMatch(/refused while an on-call role or a destination path uses/)
  })

  it('warns that destination paths do not follow a rename', () => {
    expect(text().replace(/\s+/g, ' ')).toContain('Destination paths that target the group by name do not follow a rename; update them afterwards.')
  })

  it('names the tabs of the editor and what a rename carries', () => {
    const t = text().toLowerCase()
    for (const word of ['duty schedules', 'categories', 'members', 'comments', 'on-call roles']) {
      expect(t).toContain(word)
    }
  })
})
