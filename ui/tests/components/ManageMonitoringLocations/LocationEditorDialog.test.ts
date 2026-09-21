import LocationEditorDialog from '@/components/ManageMonitoringLocations/LocationEditorDialog.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

const loc = (name: string) => ({
  'location-name': name, 'monitoring-area': 'Area', name, area: 'Area',
  geolocation: null, latitude: 1, longitude: 2, priority: 5, tags: []
})

const mountDialog = async (location: any = null, existing: any[] = []) => {
  const wrapper = mount(LocationEditorDialog, {
    props: { visible: false, location },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { Dialog: DialogStub }
    }
  })
  const store = useMonitoringLocationAdminStore()
  store.locations = existing
  vi.mocked(store.createLocation).mockResolvedValue({ success: true, message: '' })
  vi.mocked(store.updateLocation).mockResolvedValue({ success: true, message: '' })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

const saveDisabled = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="save-button"]').attributes('disabled') !== undefined
const fieldErrors = (wrapper: VueWrapper<any>) => wrapper.findAll('.field-error').map(e => e.text())

describe('LocationEditorDialog.vue', () => {
  let ctx: { wrapper: VueWrapper<any>, store: ReturnType<typeof useMonitoringLocationAdminStore> }

  beforeEach(() => {
    showToast.mockClear()
  })

  describe('create mode', () => {
    beforeEach(async () => {
      ctx = await mountDialog(null, [loc('Default'), loc('Raleigh')])
    })

    const fillValidNameArea = async () => {
      await ctx.wrapper.find('[data-test="location-name-input"]').setValue('LocA')
      await ctx.wrapper.find('[data-test="monitoring-area-input"]').setValue('Area A')
    }
    const setPriority = async (value: number) => {
      const input = ctx.wrapper.findComponent('[data-test="priority-input"]') as unknown as VueWrapper<any>
      input.vm.$emit('update:modelValue', value)
      await ctx.wrapper.vm.$nextTick()
    }

    it('disables Save until both a name and a monitoring area are entered', async () => {
      expect(saveDisabled(ctx.wrapper)).toBe(true)
      await ctx.wrapper.find('[data-test="location-name-input"]').setValue('LocA')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
      await ctx.wrapper.find('[data-test="monitoring-area-input"]').setValue('Area A')
      expect(saveDisabled(ctx.wrapper)).toBe(false)
    })

    it('rejects path and markup characters but allows spaces', async () => {
      await ctx.wrapper.find('[data-test="monitoring-area-input"]').setValue('Area')
      for (const bad of ['net/core', 'a\\b', 'a%b', 'a?b', 'a#b', 'a<b', 'a>b', 'a"b', 'a\'b', 'a`b']) {
        await ctx.wrapper.find('[data-test="location-name-input"]').setValue(bad)
        expect(fieldErrors(ctx.wrapper).join(' ')).toContain('must not contain the characters')
        expect(saveDisabled(ctx.wrapper)).toBe(true)
      }
      await ctx.wrapper.find('[data-test="location-name-input"]').setValue('Data Center (east)')
      expect(fieldErrors(ctx.wrapper)).toEqual([])
      expect(saveDisabled(ctx.wrapper)).toBe(false)
    })

    it('flags a name that already exists (case-sensitive) and disables Save', async () => {
      await ctx.wrapper.find('[data-test="monitoring-area-input"]').setValue('Area')
      await ctx.wrapper.find('[data-test="location-name-input"]').setValue('Raleigh')
      expect(fieldErrors(ctx.wrapper).join(' ')).toContain('A location named \'Raleigh\' already exists.')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
      await ctx.wrapper.find('[data-test="location-name-input"]').setValue('raleigh')
      expect(fieldErrors(ctx.wrapper)).toEqual([])
      expect(saveDisabled(ctx.wrapper)).toBe(false)
    })

    it('rejects a name longer than the 256-character column', async () => {
      await ctx.wrapper.find('[data-test="monitoring-area-input"]').setValue('Area')
      const input = ctx.wrapper.find('[data-test="location-name-input"]')
      expect(input.attributes('maxlength')).toBe('256')
      await input.setValue('x'.repeat(257))
      expect(fieldErrors(ctx.wrapper).join(' ')).toContain('256')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
      await input.setValue('x'.repeat(256))
      expect(fieldErrors(ctx.wrapper)).toEqual([])
    })

    it('rejects a monitoring area longer than the 256-character column', async () => {
      await ctx.wrapper.find('[data-test="location-name-input"]').setValue('LocA')
      const input = ctx.wrapper.find('[data-test="monitoring-area-input"]')
      expect(input.attributes('maxlength')).toBe('256')
      await input.setValue('a'.repeat(257))
      expect(fieldErrors(ctx.wrapper).join(' ')).toContain('256')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
    })

    it('rejects a priority below 1 and blocks saving', async () => {
      await fillValidNameArea()
      await setPriority(0)
      expect(fieldErrors(ctx.wrapper).join(' ')).toContain('at least 1')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
    })

    it('rejects a priority beyond the 32-bit integer max and blocks saving', async () => {
      await fillValidNameArea()
      await setPriority(3000000000)
      expect(fieldErrors(ctx.wrapper).join(' ')).toContain('or less')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
    })

    it('rejects a non-integer priority', async () => {
      await fillValidNameArea()
      await setPriority(2.5)
      expect(fieldErrors(ctx.wrapper).join(' ')).toContain('whole number')
      expect(saveDisabled(ctx.wrapper)).toBe(true)
    })

    it('accepts a valid priority and shows the hint', async () => {
      await fillValidNameArea()
      await setPriority(5)
      expect(fieldErrors(ctx.wrapper)).toEqual([])
      expect(ctx.wrapper.text()).toContain('Lower numbers sort first; leave empty for the server default.')
      expect(ctx.wrapper.text()).not.toContain('100')
      expect(saveDisabled(ctx.wrapper)).toBe(false)
    })

    it('creates the location, toasts and closes on success', async () => {
      await fillValidNameArea()
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.store.createLocation).toHaveBeenCalledWith(
        expect.objectContaining({ 'location-name': 'LocA', 'monitoring-area': 'Area A' })
      )
      expect(showToast).toHaveBeenCalledWith({ message: 'Monitoring location \'LocA\' created.', severity: 'success' })
      expect(ctx.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    })

    it('shows a server rejection inside the dialog, does not toast and stays open', async () => {
      vi.mocked(ctx.store.createLocation).mockResolvedValue({ success: false, message: 'Location LocA already exists.' })
      await fillValidNameArea()
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
      expect(showToast).not.toHaveBeenCalled()
      expect(ctx.wrapper.emitted('update:visible')).toBeFalsy()
    })
  })

  it('has a ghost Cancel button that closes without saving', async () => {
    ctx = await mountDialog(null)
    const cancel = ctx.wrapper.findComponent('[data-test="cancel-button"]') as unknown as VueWrapper<any>
    expect(cancel.props('variant')).toBe('ghost')
    await cancel.trigger('click')
    expect(ctx.store.createLocation).not.toHaveBeenCalled()
    expect(ctx.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  describe('edit mode', () => {
    beforeEach(async () => {
      ctx = await mountDialog({ ...loc('LocA'), tags: ['keepme'] }, [loc('LocA')])
    })

    it('hides the immutable name field and does not flag its own name as a duplicate', () => {
      expect(ctx.wrapper.find('[data-test="location-name-input"]').exists()).toBe(false)
      expect(fieldErrors(ctx.wrapper)).toEqual([])
      expect(saveDisabled(ctx.wrapper)).toBe(false)
    })

    it('preserves unexposed fields (tags) via spread and toasts on update', async () => {
      await ctx.wrapper.find('[data-test="monitoring-area-input"]').setValue('New Area')
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.store.updateLocation).toHaveBeenCalledWith(expect.objectContaining({ tags: ['keepme'], 'monitoring-area': 'New Area' }))
      expect(showToast).toHaveBeenCalledWith({ message: 'Monitoring location \'LocA\' updated.', severity: 'success' })
      expect(ctx.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    })
  })
})
