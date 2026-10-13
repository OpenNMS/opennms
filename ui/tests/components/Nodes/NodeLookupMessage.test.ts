import NodeLookupMessage from '@/components/Nodes/NodeLookupMessage.vue'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

const mountMessage = (state: 'invalid' | 'notFound' | 'error', value = 'abc') =>
  mount(NodeLookupMessage, {
    props: { state, value },
    global: { plugins: [PrimeVue], stubs: { RouterLink: RouterLinkStub }}
  })

describe('NodeLookupMessage', () => {
  beforeEach(() => {
    push.mockClear()
  })

  it('says the id is not valid, naming the value', () => {
    const text = mountMessage('invalid', 'abc').find('[data-test="node-lookup-message"]').text()
    expect(text).toContain('Invalid Node ID')
    expect(text).toContain('\'abc\'')
    expect(text).toContain('foreignSource:foreignId')
  })

  it('says the node was not found', () => {
    expect(mountMessage('notFound', '12345').text()).toContain('Node Not Found')
  })

  it('says the lookup failed', () => {
    expect(mountMessage('error', 'req3:1').text()).toContain('could not be loaded')
  })

  it('goes to the node typed in, trimmed', async () => {
    const wrapper = mountMessage('notFound', '12345')
    await wrapper.find('[data-test="node-lookup-input"]').setValue('  SmokeTests:TestMachine1 ')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(push).toHaveBeenCalledTimes(1)
    expect(push).toHaveBeenCalledWith({ name: 'Node Details', params: { id: 'SmokeTests:TestMachine1' }})
  })

  it('does nothing for an empty entry', async () => {
    const wrapper = mountMessage('notFound', '12345')
    await wrapper.find('[data-test="node-lookup-input"]').setValue('   ')
    await wrapper.find('form').trigger('submit')

    expect(push).not.toHaveBeenCalled()
  })

  it('submits the form from the Go button', () => {
    const wrapper = mountMessage('notFound', '12345')
    expect(wrapper.find('[data-test="node-lookup-button"]').attributes('type')).toBe('submit')
  })

  it('links to the node list', () => {
    expect(mountMessage('invalid').findComponent(RouterLinkStub).props('to')).toEqual({ name: 'Nodes' })
  })
})
