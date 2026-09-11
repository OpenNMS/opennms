import CategoryEditorDialog from '@/components/ManageCategories/CategoryEditorDialog.vue'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

const mountDialog = async (category: any = null) => {
  const wrapper = mount(CategoryEditorDialog, {
    props: { visible: false, category },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { Dialog: DialogStub }
    }
  })
  const store = useCategoryAdminStore()
  vi.mocked(store.createCategory).mockResolvedValue(null)
  vi.mocked(store.updateCategoryDescription).mockResolvedValue(null)
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

describe('CategoryEditorDialog.vue', () => {
  let ctx: { wrapper: VueWrapper<any>, store: ReturnType<typeof useCategoryAdminStore> }

  describe('create mode', () => {
    beforeEach(async () => { ctx = await mountDialog(null) })

    it('shows the name field and disables Save until a name is entered', async () => {
      expect(ctx.wrapper.find('[data-test="category-name-input"]').exists()).toBe(true)
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Routers')
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
    })

    it('rejects a name with FIQL/markup characters', async () => {
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('net,core')
      expect(ctx.wrapper.find('[data-test="name-error"]').exists()).toBe(true)
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    })

    it('creates the category and closes on success', async () => {
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Routers')
      await ctx.wrapper.find('[data-test="category-description-input"]').setValue('core routers')
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.store.createCategory).toHaveBeenCalledWith({ name: 'Routers', description: 'core routers' })
      expect(ctx.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    })

    it('keeps the dialog open and shows a server error', async () => {
      vi.mocked(ctx.store.createCategory).mockResolvedValue('Category already exists.')
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Routers')
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
      expect(ctx.wrapper.emitted('update:visible')).toBeFalsy()
    })
  })

  describe('edit mode', () => {
    beforeEach(async () => { ctx = await mountDialog({ name: 'Routers', description: 'old' }) })

    it('hides the immutable name field and updates only the description', async () => {
      expect(ctx.wrapper.find('[data-test="category-name-input"]').exists()).toBe(false)
      await ctx.wrapper.find('[data-test="category-description-input"]').setValue('new desc')
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.store.updateCategoryDescription).toHaveBeenCalledWith('Routers', 'new desc')
    })
  })
})
