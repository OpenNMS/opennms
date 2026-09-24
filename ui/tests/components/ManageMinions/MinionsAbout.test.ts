import MinionsAbout from '@/components/ManageMinions/MinionsAbout.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('MinionsAbout.vue', () => {
  it('renders the help content', () => {
    const wrapper = mount(MinionsAbout)
    expect(wrapper.text()).toContain('What Minions are')
    expect(wrapper.text()).toContain('How to use this page')
  })
})
