import CategoriesAbout from '@/components/ManageCategories/CategoriesAbout.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('CategoriesAbout.vue', () => {
  it('renders the help content', () => {
    const wrapper = mount(CategoriesAbout)
    expect(wrapper.text()).toContain('What surveillance categories are for')
    expect(wrapper.text()).toContain('How to use this page')
  })
})
