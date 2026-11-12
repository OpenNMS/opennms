import JdbcQueryEdit from '@/containers/JdbcQueryEdit.vue'
import { getDefaultQuery, useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcSource } from '@/types/jdbcDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'JDBC Query Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

const source = (): JdbcSource => ({
  id: 'mysql',
  version: 'v1',
  collections: [{
    name: 'MySQL',
    rrdStep: 300,
    rras: ['RRA:AVERAGE:0.5:1:2016'],
    queries: [
      { ...getDefaultQuery(), name: 'q1', queryString: 'SELECT 1 AS a', columns: [{ name: 'a', type: 'gauge', alias: 'a' }] },
      { ...getDefaultQuery(), name: 'q2', queryString: 'SELECT 2 AS b', columns: [{ name: 'b', type: 'counter', alias: 'b' }] }
    ]
  }]
})

describe('JdbcQueryEdit.vue', () => {
  let store: ReturnType<typeof useJdbcDataCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(JdbcQueryEdit, { global: { plugins: [PrimeVue], directives: { onmsTooltip: {}}}})
    await flushPromises()
    return wrapper
  }

  const type = async (dataTest: string, value: string) => {
    await wrapper.find(`[data-test="${dataTest}"]`).setValue(value)
  }

  const save = () => wrapper.find('[data-test="jdbc-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'JDBC Query Create'
    route.params = { id: 'mysql', name: 'MySQL' }
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useJdbcDataCollectionStore()
    store.fetchSource = vi.fn().mockImplementation(async () => {
      store.currentSource = source()
      return ok
    })
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
    store.saveQuery = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('starts a new query with one empty column', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="jdbc-edit-title"]').text()).toBe('Create New JDBC Query')
    expect(wrapper.findAll('[data-test^="jdbc-column-name-"]')).toHaveLength(1)
    expect(save().attributes('disabled')).toBeDefined()
  })

  it('refuses the name of another query of the collection', async () => {
    await mountComponent()
    await type('jdbc-query-edit-name', 'q2')

    expect(wrapper.text()).toContain('already has a query named \'q2\'')
  })

  it('saves a new query at the end and goes back to the collection', async () => {
    await mountComponent()
    await type('jdbc-query-edit-name', 'q3')
    await type('jdbc-query-edit-sql', 'SELECT COUNT(*) AS c FROM t')
    await type('jdbc-column-name-0', 'c')
    await type('jdbc-column-alias-0', ' cAlias ')

    expect(save().attributes('disabled')).toBeUndefined()
    await save().trigger('click')
    await flushPromises()

    expect(store.saveQuery).toHaveBeenCalledWith('MySQL', -1, expect.objectContaining({
      name: 'q3',
      queryString: 'SELECT COUNT(*) AS c FROM t',
      columns: [expect.objectContaining({ name: 'c', alias: 'cAlias', type: 'gauge' })]
    }))
    expect(pushMock).toHaveBeenCalledWith('/jdbc-data-collection/source/mysql/collection/MySQL/edit')
  })

  it('edits a query in place, keeping its own name allowed', async () => {
    route.name = 'JDBC Query Edit'
    route.params = { id: 'mysql', name: 'MySQL', index: '1' }
    await mountComponent()

    expect((wrapper.find('[data-test="jdbc-query-edit-name"]').element as HTMLInputElement).value).toBe('q2')
    await type('jdbc-query-edit-sql', 'SELECT 3 AS b')
    await save().trigger('click')
    await flushPromises()

    expect(store.saveQuery).toHaveBeenCalledWith('MySQL', 1, expect.objectContaining({ name: 'q2', queryString: 'SELECT 3 AS b' }))
  })

  it('stays on the page when the save fails', async () => {
    route.name = 'JDBC Query Edit'
    route.params = { id: 'mysql', name: 'MySQL', index: '0' }
    store.saveQuery = vi.fn().mockResolvedValue({ success: false, message: 'boom' })
    await mountComponent()
    await type('jdbc-query-edit-sql', 'SELECT 9 AS a')
    await save().trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'boom', error: true })
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('shows a not-found message for an unknown query', async () => {
    route.name = 'JDBC Query Edit'
    route.params = { id: 'mysql', name: 'MySQL', index: '7' }
    await mountComponent()

    expect(wrapper.find('[data-test="jdbc-edit-not-found"]').exists()).toBe(true)
  })
})
