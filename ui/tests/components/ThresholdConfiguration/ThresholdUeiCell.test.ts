import ThresholdUeiCell from '@/components/ThresholdConfiguration/Group/ThresholdUeiCell.vue'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('ThresholdUeiCell.vue', () => {
  let wrapper: VueWrapper<any>

  const mountComponent = (uei?: string) => {
    wrapper = mount(ThresholdUeiCell, { props: { uei }})
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    useMenuStore().mainMenu = { baseHref: '/opennms/' } as any
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('links a custom UEI to its notifications page', () => {
    const wrapper = mountComponent('uei.opennms.org/example/highCpuExceeded')

    const link = wrapper.find('[data-test="threshold-uei-link"]')
    expect(link.attributes('href')).toBe(
      '/opennms/admin/notification/noticeWizard/notifsForUEI.jsp?uei=uei.opennms.org%2Fexample%2FhighCpuExceeded'
    )
  })

  it('renders a placeholder rather than a broken link when no UEI is set', () => {
    // A blank UEI means the standard threshold event is used, and there is nothing useful to link to.
    const wrapper = mountComponent('')

    expect(wrapper.find('[data-test="threshold-uei-link"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="threshold-uei-empty"]').text()).toBe('--')
  })

  it('falls back to a sane base path when the menu has not loaded yet', () => {
    useMenuStore().mainMenu = undefined as any

    const wrapper = mountComponent('uei.opennms.org/example/x')

    expect(wrapper.find('[data-test="threshold-uei-link"]').attributes('href'))
      .toContain('/opennms/admin/notification/noticeWizard/notifsForUEI.jsp')
  })
})
