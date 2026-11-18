import JdbcCollectionEdit from '@/containers/JdbcCollectionEdit.vue'
import { getDefaultQuery, useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcSource } from '@/types/jdbcDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, replaceMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  replaceMock: vi.fn(),
  route: { name: 'JDBC Collection Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock, replace: replaceMock })
}))

const source = (): JdbcSource => ({
  id: 'mysql',
  rrdRepository: '/rrd',
  version: 'v1',
  collections: [{
    name: 'MySQL',
    rrdStep: 300,
    rras: ['RRA:AVERAGE:0.5:1:2016'],
    queries: [{ ...getDefaultQuery(), name: 'q1', queryString: 'SELECT 1', columns: [{ name: 'a', type: 'gauge', alias: 'a' }] }],
    usedBy: ['example1/MySQL'],
    shadowedBy: null
  }]
})

describe('JdbcCollectionEdit.vue', () => {
  let store: ReturnType<typeof useJdbcDataCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(JdbcCollectionEdit, {
      global: { plugins: [PrimeVue], stubs: { 'router-link': true }, directives: { onmsTooltip: {}}}
    })
    await flushPromises()
    return wrapper
  }

  const type = async (dataTest: string, value: string) => {
    await wrapper.find(`[data-test="${dataTest}"]`).setValue(value)
  }

  const save = () => wrapper.find('[data-test="jdbc-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'JDBC Collection Create'
    route.params = { id: 'mysql' }
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useJdbcDataCollectionStore()
    store.fetchSource = vi.fn().mockImplementation(async () => {
      store.currentSource = source()
      return ok
    })
    store.fetchSources = vi.fn().mockImplementation(async () => {
      store.sources = [
        { id: 'default', collectionCount: 1, collectionNames: ['default'] },
        { id: 'mysql', collectionCount: 1, collectionNames: ['MySQL'] }
      ]
      return ok
    })
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
    store.createCollection = vi.fn().mockResolvedValue(ok)
    store.saveCollection = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  describe('create', () => {
    it('starts from the default RRD settings and shows no queries yet', async () => {
      await mountComponent()

      expect(wrapper.find('[data-test="jdbc-edit-title"]').text()).toBe('Create New JDBC Collection')
      expect(wrapper.findAll('[data-test^="jdbc-collection-rra-value-"]')).toHaveLength(5)
      expect(wrapper.find('[data-test="jdbc-queries-table"]').exists()).toBe(false)
    })

    it('refuses a name another source uses', async () => {
      await mountComponent()
      await type('jdbc-collection-edit-name', 'default')

      expect(wrapper.text()).toContain('unique across all sources')
      expect(save().attributes('disabled')).toBeDefined()
    })

    it('creates the collection and continues on its edit page', async () => {
      await mountComponent()
      await type('jdbc-collection-edit-name', ' New One ')
      await save().trigger('click')
      await flushPromises()

      expect(store.createCollection).toHaveBeenCalledWith(expect.objectContaining({ name: 'New One', rrdStep: 300, queries: [] }))
      expect(replaceMock).toHaveBeenCalledWith('/jdbc-data-collection/source/mysql/collection/New%20One/edit')
    })
  })

  describe('edit', () => {
    beforeEach(() => {
      route.name = 'JDBC Collection Edit'
      route.params = { id: 'mysql', name: 'MySQL' }
    })

    it('loads the collection with its queries; nothing to save yet', async () => {
      await mountComponent()

      expect((wrapper.find('[data-test="jdbc-collection-edit-name"]').element as HTMLInputElement).value).toBe('MySQL')
      expect(wrapper.find('[data-test="jdbc-queries-table"]').exists()).toBe(true)
      expect(save().attributes('disabled')).toBeDefined()
    })

    it('renames the collection, keeps its queries and warns about the services using it', async () => {
      await mountComponent()
      await type('jdbc-collection-edit-name', 'MySQL-2')

      expect(wrapper.text()).toContain('Used by example1/MySQL')
      await save().trigger('click')
      await flushPromises()

      expect(store.saveCollection).toHaveBeenCalledWith('MySQL', expect.objectContaining({ name: 'MySQL-2' }))
      expect(vi.mocked(store.saveCollection).mock.calls[0][1].queries).toHaveLength(1)
      expect(replaceMock).toHaveBeenCalledWith('/jdbc-data-collection/source/mysql/collection/MySQL-2/edit')
    })

    it('shows a not-found message for an unknown collection', async () => {
      route.params = { id: 'mysql', name: 'nope' }
      await mountComponent()

      expect(wrapper.find('[data-test="jdbc-edit-not-found"]').text()).toContain('has no collection named \'nope\'')
    })

    it('goes back to the source', async () => {
      await mountComponent()
      await wrapper.find('[data-test="jdbc-edit-cancel"]').trigger('click')

      expect(pushMock).toHaveBeenCalledWith('/jdbc-data-collection/source/mysql')
    })
  })
})
