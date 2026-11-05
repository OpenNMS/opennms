import ManageApplications from '@/containers/ManageApplications.vue'
import API from '@/services'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services', () => ({
  default: {
    listApplicationSummaries: vi.fn()
  }
}))

const BreadCrumbsStub = { name: 'BreadCrumbs', props: ['items'], template: '<nav data-test="breadcrumbs" />' }

const mountPage = async () => {
  const pinia = createPinia()
  setActivePinia(pinia)
  const wrapper = mount(ManageApplications, {
    global: {
      plugins: [PrimeVue, pinia],
      stubs: { BreadCrumbs: BreadCrumbsStub, AboutDialog: true, ApplicationCreateDialog: true, ApplicationEditorDialog: true, ApplicationDeleteDialog: true }
    }
  })
  await flushPromises()
  return wrapper
}

describe('ManageApplications.vue (container)', () => {
  beforeEach(() => vi.clearAllMocks())

  it('loads the applications once and lists them under the page title', async () => {
    vi.mocked(API.listApplicationSummaries).mockResolvedValue({
      success: true,
      message: '',
      payload: [
        { id: 1, name: 'Billing', serviceCount: 0, perspectiveLocations: [] },
        { id: 2, name: 'Web Store', serviceCount: 3, perspectiveLocations: ['RDU'] }
      ]
    })
    const wrapper = await mountPage()
    expect(API.listApplicationSummaries).toHaveBeenCalledTimes(1)
    expect(wrapper.find('h1.page-title').text()).toBe('Manage Applications')
    expect(wrapper.findComponent(BreadCrumbsStub).props('items').at(-1)).toMatchObject({ label: 'Manage Applications', position: 'last' })
    expect(wrapper.findAll('[data-test="application-name"]').map(name => name.text())).toEqual(['Billing', 'Web Store'])
    expect(wrapper.findAll('[data-test="service-count"]').map(count => count.text())).toEqual(['0', '3'])
  })

  it('says the list could not be loaded instead of showing an empty one', async () => {
    vi.mocked(API.listApplicationSummaries).mockResolvedValue({ success: false, message: 'Failed to load applications.' })
    const wrapper = await mountPage()
    expect(wrapper.find('[data-test="empty-list"]').text()).toContain('Could not load applications.')
    expect(wrapper.find('[data-test="application-name"]').exists()).toBe(false)
  })
})
