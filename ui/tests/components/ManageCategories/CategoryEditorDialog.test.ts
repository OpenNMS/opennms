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
  vi.mocked(store.createCategory).mockResolvedValue({ success: true, message: '' })
  vi.mocked(store.updateCategoryDescription).mockResolvedValue({ success: true, message: '' })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

describe('CategoryEditorDialog.vue', () => {
  let ctx: { wrapper: VueWrapper<any>, store: ReturnType<typeof useCategoryAdminStore> }

  describe('create mode', () => {
    beforeEach(async () => {
      ctx = await mountDialog(null)
    })

    it('shows the name field and disables Save until a name is entered', async () => {
      expect(ctx.wrapper.find('[data-test="category-name-input"]').exists()).toBe(true)
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Routers')
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
    })

    it('rejects a name longer than the 64-character column', async () => {
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('x'.repeat(65))
      expect(ctx.wrapper.find('.field-error').text()).toContain('64')
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('x'.repeat(64))
      expect(ctx.wrapper.find('.field-error').exists()).toBe(false)
    })

    it('rejects a description longer than the 256-character column', async () => {
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Routers')
      await ctx.wrapper.find('[data-test="category-description-input"]').setValue('d'.repeat(257))
      expect(ctx.wrapper.find('.field-error').text()).toContain('256')
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    })

    it('rejects a name of . since its item URL would be the collection', async () => {
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('.')
      expect(ctx.wrapper.find('.field-error').text()).toContain('cannot be . or ..')
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    })

    it('rejects path and markup characters but allows spaces and punctuation the legacy page accepted', async () => {
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('net/core')
      expect(ctx.wrapper.find('.field-error').exists()).toBe(true)
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Core Routers (east)')
      expect(ctx.wrapper.find('.field-error').exists()).toBe(false)
      expect(ctx.wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
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
      vi.mocked(ctx.store.createCategory).mockResolvedValue({ success: false, message: 'Category already exists.' })
      await ctx.wrapper.find('[data-test="category-name-input"]').setValue('Routers')
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
      expect(ctx.wrapper.emitted('update:visible')).toBeFalsy()
    })
  })

  it('has a ghost Cancel button that closes without saving', async () => {
    ctx = await mountDialog(null)
    await ctx.wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(ctx.store.createCategory).not.toHaveBeenCalled()
    expect(ctx.wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  describe('edit mode', () => {
    beforeEach(async () => {
      ctx = await mountDialog({ name: 'Routers', description: 'old' })
    })

    it('hides the immutable name field and updates only the description', async () => {
      expect(ctx.wrapper.find('[data-test="category-name-input"]').exists()).toBe(false)
      await ctx.wrapper.find('[data-test="category-description-input"]').setValue('new desc')
      await ctx.wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(ctx.store.updateCategoryDescription).toHaveBeenCalledWith('Routers', 'new desc')
    })
  })
})
