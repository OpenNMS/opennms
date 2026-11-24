import ThresholdGroupHeader from '@/components/ThresholdConfiguration/Group/ThresholdGroupHeader.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { pushMock } = vi.hoisted(() => ({ pushMock: vi.fn() }))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

describe('ThresholdGroupHeader.vue', () => {
  const mountComponent = () =>
    mount(ThresholdGroupHeader, {
      props: { group: { name: 'mib2', rrdRepository: '/rrd', thresholds: [], expressions: [] }},
      global: { plugins: [PrimeVue], stubs: { ThresholdHelpDialog: true }}
    })

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('offers a Go Back button like the other detail pages', () => {
    expect(mountComponent().find('[data-test="threshold-group-back"]').text()).toBe('Go Back')
  })

  it('goes back to the threshold configuration', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-test="threshold-group-back"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/threshold-config')
  })
})
