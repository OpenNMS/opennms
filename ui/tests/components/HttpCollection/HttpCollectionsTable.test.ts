import HttpCollectionsTable from '@/components/HttpCollection/HttpCollectionsTable.vue'
import HttpCollectionSettingsTab from '@/components/HttpCollection/HttpCollectionSettingsTab.vue'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

describe('HTTP collection overview', () => {
  let store: ReturnType<typeof useHttpCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useHttpCollectionStore()
    store.collections = [
      { name: 'doc-count', rrdStep: 300, uriCount: 1, attributeCount: 2 },
      { name: 'apache', rrdStep: 60, uriCount: 2, attributeCount: 9 }
    ]
    store.settings = { rrdRepository: '/rrd/', version: 'v1' }
    store.deleteCollection = vi.fn().mockResolvedValue(ok)
    store.saveSettings = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  describe('HttpCollectionsTable', () => {
    const mountTable = async () => {
      wrapper = mount(HttpCollectionsTable, { global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>' }}}})
      await flushPromises()
    }

    it('filters the collections by name', async () => {
      await mountTable()
      expect(wrapper.findAll('[data-test="http-collection-link"]')).toHaveLength(2)

      await wrapper.find('[data-test="http-collection-search"]').setValue('apa')

      expect(wrapper.findAll('[data-test="http-collection-link"]').map(link => link.text())).toEqual(['apache'])
    })

    it('deletes a collection after confirmation', async () => {
      await mountTable()
      await wrapper.findAll('[data-test="http-collection-delete"]')[1].trigger('click')
      wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
      await flushPromises()

      expect(store.deleteCollection).toHaveBeenCalledWith('apache')
      expect(showSnackBarMock).toHaveBeenCalledWith(expect.objectContaining({ msg: 'HTTP collection deleted.' }))
    })

    it('opens the create page', async () => {
      await mountTable()
      await wrapper.find('[data-test="http-collection-create"]').trigger('click')

      expect(pushMock).toHaveBeenCalledWith('/http-collection-config/create-collection')
    })
  })

  describe('HttpCollectionSettingsTab', () => {
    it('saves a changed rrd repository with the loaded version', async () => {
      wrapper = mount(HttpCollectionSettingsTab, { global: { plugins: [PrimeVue] }})
      await flushPromises()

      const save = wrapper.find('[data-test="http-settings-save"]')
      expect(save.attributes('disabled')).toBeDefined()

      await wrapper.find('[data-test="http-settings-rrd-repository"]').setValue(' /var/rrd/ ')
      await save.trigger('click')
      await flushPromises()

      expect(store.saveSettings).toHaveBeenCalledWith({ rrdRepository: '/var/rrd/', version: 'v1' })
    })
  })
})
