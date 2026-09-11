import MinionEditorDialog from '@/components/ManageMinions/MinionEditorDialog.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/minionAdminStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

describe('MinionEditorDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const minion = { id: 'm1', label: 'Minion One', location: 'Default', type: 'Minion', status: 'UP', version: '1.0', properties: { region: 'us' }}

  const mountDialog = async (m: any = minion) => {
    wrapper = mount(MinionEditorDialog, {
      props: { visible: false, minion: m },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = { updateMinion: vi.fn().mockResolvedValue(null) }
    vi.mocked(useMinionAdminStore).mockReturnValue(store)
  })

  it('prefills label, location and properties from the minion', async () => {
    await mountDialog()
    expect((wrapper.find('[data-test="label-input"]').element as HTMLInputElement).value).toBe('Minion One')
    expect((wrapper.find('[data-test="prop-key-0"]').element as HTMLInputElement).value).toBe('region')
  })

  it('saves an edit payload of id/label/location/properties only (server fields preserved by the service)', async () => {
    await mountDialog()
    await wrapper.find('[data-test="label-input"]').setValue('Renamed')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const arg = store.updateMinion.mock.calls[0][0]
    expect(arg).toEqual({ id: 'm1', label: 'Renamed', location: 'Default', properties: { region: 'us' }})
    // the editor deliberately does NOT send server-maintained fields
    expect(arg.status).toBeUndefined()
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('preserves a property key verbatim (does not trim keys)', async () => {
    await mountDialog({ ...minion, properties: { ' region': 'us' }})
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(store.updateMinion.mock.calls[0][0].properties).toEqual({ ' region': 'us' })
  })

  it('requires a location', async () => {
    await mountDialog()
    await wrapper.find('[data-test="location-input"]').setValue('')
    expect(wrapper.find('[data-test="location-error"]').text()).toContain('required')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('blocks saving on duplicate property keys', async () => {
    await mountDialog()
    await wrapper.find('[data-test="add-property-button"]').trigger('click')
    await wrapper.find('[data-test="prop-key-1"]').setValue('region')
    expect(wrapper.find('[data-test="prop-error"]').text()).toContain('unique')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('shows a server rejection inside the dialog and stays open', async () => {
    store.updateMinion.mockResolvedValue('Update failed on the server.')
    await mountDialog()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('Update failed')
    expect(wrapper.emitted('update:visible') ?? []).toEqual([])
  })
})
