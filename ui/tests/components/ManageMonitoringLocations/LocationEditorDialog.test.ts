import LocationEditorDialog from '@/components/ManageMonitoringLocations/LocationEditorDialog.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/monitoringLocationAdminStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

describe('LocationEditorDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (location: any = null) => {
    wrapper = mount(LocationEditorDialog, {
      props: { visible: false, location },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      createLocation: vi.fn().mockResolvedValue(null),
      updateLocation: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useMonitoringLocationAdminStore).mockReturnValue(store)
  })

  it('flags a name with whitespace or path characters and disables saving', async () => {
    await mountDialog()
    await wrapper.find('[data-test="location-name-input"]').setValue('bad name/x')
    await wrapper.find('[data-test="monitoring-area-input"]').setValue('Area')
    expect(wrapper.find('[data-test="name-error"]').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('requires a monitoring area', async () => {
    await mountDialog()
    await wrapper.find('[data-test="location-name-input"]').setValue('LocA')
    // area empty by default
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  const fillValidNameArea = async () => {
    await wrapper.find('[data-test="location-name-input"]').setValue('LocA')
    await wrapper.find('[data-test="monitoring-area-input"]').setValue('Area A')
  }
  const setPriority = async (value: number) => {
    const input = wrapper.findComponent('[data-test="priority-input"]') as unknown as VueWrapper<any>
    input.vm.$emit('update:modelValue', value)
    await wrapper.vm.$nextTick()
  }

  it('rejects a priority below 1 and blocks saving', async () => {
    await mountDialog()
    await fillValidNameArea()
    await setPriority(0)
    expect(wrapper.find('[data-test="priority-error"]').text()).toContain('at least 1')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('rejects a priority beyond the 32-bit integer max and blocks saving', async () => {
    await mountDialog()
    await fillValidNameArea()
    await setPriority(3000000000)
    expect(wrapper.find('[data-test="priority-error"]').text()).toContain('or less')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('rejects a non-integer priority', async () => {
    await mountDialog()
    await fillValidNameArea()
    await setPriority(2.5)
    expect(wrapper.find('[data-test="priority-error"]').text()).toContain('whole number')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('accepts a valid priority', async () => {
    await mountDialog()
    await fillValidNameArea()
    await setPriority(5)
    expect(wrapper.find('[data-test="priority-error"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
  })

  it('creates a valid location and closes', async () => {
    await mountDialog()
    await wrapper.find('[data-test="location-name-input"]').setValue('LocA')
    await wrapper.find('[data-test="monitoring-area-input"]').setValue('Area A')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(store.createLocation).toHaveBeenCalledWith(
      expect.objectContaining({ 'location-name': 'LocA', 'monitoring-area': 'Area A' })
    )
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows a server rejection inside the dialog and stays open', async () => {
    store.createLocation.mockResolvedValue('Location LocA already exists.')
    await mountDialog()
    await wrapper.find('[data-test="location-name-input"]').setValue('LocA')
    await wrapper.find('[data-test="monitoring-area-input"]').setValue('Area A')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
    expect(wrapper.emitted('update:visible') ?? []).toEqual([])
  })

  it('edit preserves unexposed fields (tags) via spread', async () => {
    await mountDialog({ 'location-name': 'LocA', 'monitoring-area': 'Area', geolocation: null, latitude: 1, longitude: 2, priority: 5, tags: ['keepme'] })
    await wrapper.find('[data-test="monitoring-area-input"]').setValue('New Area')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(store.updateLocation).toHaveBeenCalledWith(expect.objectContaining({ tags: ['keepme'], 'monitoring-area': 'New Area' }))
  })
})
