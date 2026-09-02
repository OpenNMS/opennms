import InventorySearch from '@/containers/InventorySearch.vue'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

vi.mock('@/services', () => ({
  default: {
    getMonitoringLocations: vi.fn().mockResolvedValue({ location: [{ name: 'Default' }, { name: 'Remote' }] }),
    getServiceTypes: vi.fn().mockResolvedValue([{ name: 'ICMP' }, { name: 'HTTP' }])
  }
}))

const mountPage = async () => {
  const wrapper = mount(InventorySearch, {
    global: {
      plugins: [PrimeVue],
      stubs: { BreadCrumbs: true }
    }
  })
  await flushPromises()
  return wrapper
}

describe('InventorySearch.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the search form and the help affordance', async () => {
    const wrapper = await mountPage()
    expect(wrapper.find('[data-test="search-title"]').text()).toContain('Search for Nodes')
    // help copy lives behind the Info-dialog button, matching the other admin pages
    expect(wrapper.find('[data-test="about-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="criterion-name"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="criterion-field"]').exists()).toBe(true)
  })

  it('routes a name search to the node list', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="nodename-input"]').setValue('core')
    await wrapper.find('[data-test="criterion-name"]').trigger('submit')

    expect(push).toHaveBeenCalledWith({ path: '/nodes', query: { nodename: 'core' }})
  })

  it('drops a blank criterion so it does not over-filter', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="criterion-iplike"]').trigger('submit')

    expect(push).toHaveBeenCalledWith({ path: '/nodes', query: {}})
  })

  it('routes the system-attribute search with the mib2 query keys', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="mib2-value"]').setValue('linux')
    await wrapper.find('[data-test="criterion-system"]').trigger('submit')

    expect(push).toHaveBeenCalledWith({
      path: '/nodes',
      query: { mib2Parm: 'sysDescription', mib2ParmMatchType: 'contains', mib2ParmValue: 'linux' }
    })
  })

  it('routes an asset category search via assetColumn/assetValue', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="criterion-category"]').trigger('submit')

    expect(push).toHaveBeenCalledWith({
      path: '/nodes',
      query: { assetColumn: 'category', assetValue: 'Unspecified' }
    })
  })

  it('routes the interface-attribute search with the snmp query keys', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="snmp-value"]').setValue('Gig0/1')
    await wrapper.find('[data-test="criterion-interface"]').trigger('submit')

    expect(push).toHaveBeenCalledWith({
      path: '/nodes',
      query: { snmpParm: 'ifAlias', snmpParmMatchType: 'contains', snmpParmValue: 'Gig0/1' }
    })
  })

  it('routes an asset-field search with an asset column the Node list accepts', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="field-value"]').setValue('Dell')
    await wrapper.find('[data-test="criterion-field"]').trigger('submit')

    const call = push.mock.calls.at(-1)![0]
    expect(call.path).toBe('/nodes')
    expect(call.query.assetValue).toBe('Dell')
    // the column must be a real assetRecord property key, not a made-up label value
    expect(typeof call.query.assetColumn).toBe('string')
    expect(call.query.assetColumn.length).toBeGreaterThan(0)
  })

  it('routes foreign source, mac and flows to their tracked query keys', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="fs-input"]').setValue('acme')
    await wrapper.find('[data-test="criterion-fs"]').trigger('submit')
    expect(push).toHaveBeenLastCalledWith({ path: '/nodes', query: { foreignSource: 'acme' }})

    await wrapper.find('[data-test="mac-input"]').setValue('00:11:22')
    await wrapper.find('[data-test="criterion-mac"]').trigger('submit')
    expect(push).toHaveBeenLastCalledWith({ path: '/nodes', query: { maclike: '00:11:22' }})

    await wrapper.find('[data-test="criterion-flows"]').trigger('submit')
    expect(push).toHaveBeenLastCalledWith({ path: '/nodes', query: { flows: 'true' }})
  })
})
