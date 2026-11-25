import OpenAPI from '@/containers/OpenAPI.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

// The other OpenAPI tests replace RapiDoc with a stub. This file loads the real
// web component, so a RapiDoc upgrade that drops an attribute or method the page
// relies on fails here instead of being found by hand. happy-dom does no layout or
// styling, so how the docs look still needs a manual check.

vi.mock('@/services', () => ({
  default: {
    getOpenApi: vi.fn(() => Promise.resolve(spec('V2'))),
    getOpenApiV1: vi.fn(() => Promise.resolve(spec('V1')))
  }
}))

const spec = (title: string) => ({
  openapi: '3.0.1',
  info: { title, version: '1' },
  tags: [{ name: 'Alarms' }],
  paths: {
    '/alarms/{id}': {
      get: {
        tags: ['Alarms'],
        summary: `Get one ${title} alarm`,
        parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' }}],
        responses: { 200: { description: 'The alarm' }}
      }
    }
  }
})

// happy-dom has no ResizeObserver
class ResizeObserverStub {
  observe() {
    // not used
  }
  disconnect() {
    // not used
  }
  unobserve() {
    // not used
  }
}

const mountPage = async (theme: 'open-light' | 'open-dark') => {
  vi.stubGlobal('ResizeObserver', ResizeObserverStub)

  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false, initialState: { appStore: { theme }}})
  setActivePinia(pinia)

  const wrapper = mount(OpenAPI, {
    attachTo: document.body,
    global: {
      plugins: [pinia, PrimeVue],
      stubs: { BreadCrumbs: true }
    }
  })

  await flushPromises()
  return wrapper
}

const docs = (wrapper: VueWrapper<any>) => wrapper.findAll('rapi-doc').map(doc => doc.element as HTMLElement)

// RapiDoc renders asynchronously after loadSpec
const waitForText = async (element: HTMLElement, text: string) => {
  await vi.waitFor(() => {
    expect(element.shadowRoot?.textContent).toContain(text)
  })
}

describe('OpenAPI.vue with the real RapiDoc', () => {
  let wrapper: VueWrapper<any> | null = null

  afterEach(() => {
    wrapper?.unmount()
    wrapper = null
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  // RapiDoc ignores an attribute it doesn't declare, so a dropped setting would
  // otherwise go unnoticed: the docs still render, just without our fonts or colours.
  it.each(['open-light', 'open-dark'] as const)('sets only attributes RapiDoc declares (%s)', async (theme) => {
    wrapper = await mountPage(theme)

    const declared = (customElements.get('rapi-doc') as unknown as { observedAttributes: string[] }).observedAttributes
    const pageAttributes = ['id', 'class']

    for (const doc of docs(wrapper)) {
      expect(doc.hasAttribute('primary-color')).toBe(true)
      const set = doc.getAttributeNames().filter(name => !pageAttributes.includes(name) && !name.startsWith('data-v-'))
      expect(set.filter(name => !declared.includes(name))).toEqual([])
    }
  })

  it('renders the V2 spec, then the V1 spec when its tab is opened', async () => {
    wrapper = await mountPage('open-light')
    const [v2, v1] = docs(wrapper)

    await waitForText(v2, 'Get one V2 alarm')

    wrapper.vm.onTabChange(1)
    await flushPromises()

    await waitForText(v1, 'Get one V1 alarm')
  })
})
