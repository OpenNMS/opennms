import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest'

// Kept out of OpenAPI.test.ts: the cold-start check needs vi.resetModules(), and
// after that happy-dom silently drops writes to documentElement.style, which the
// layout tests there assert on. A separate file gets its own environment.

const { loadSpecMock } = vi.hoisted(() => ({
  loadSpecMock: vi.fn()
}))

vi.mock('rapidoc', () => ({}))

vi.mock('@/services', () => ({
  default: {
    getOpenApi: vi.fn(() => Promise.resolve({ openapi: '3.0.1', info: { title: 'V2' }, paths: {}})),
    getOpenApiV1: vi.fn(() => Promise.resolve({ openapi: '3.0.1', info: { title: 'V1' }, paths: {}}))
  }
}))

class RapiDocStub extends HTMLElement {
  loadSpec(spec: unknown) {
    loadSpecMock(this.id, spec)
  }
}

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

const idsRendered = () => loadSpecMock.mock.calls.map(([id]) => id)

describe('OpenAPI.vue spec fetching', () => {
  let wrapper: VueWrapper<any> | null = null

  beforeAll(() => {
    if (!customElements.get('rapi-doc')) {
      customElements.define('rapi-doc', RapiDocStub)
    }
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = null
    vi.restoreAllMocks()
    vi.clearAllMocks()
  })

  // Both fetches are left pending until after the tab is clicked, so a second
  // call would be visible as a second pair of requests.
  it('shares the in-flight fetch when the V1 tab is opened before it resolves', async () => {
    vi.resetModules()
    loadSpecMock.mockClear()
    vi.stubGlobal('ResizeObserver', ResizeObserverStub)

    const API = (await import('@/services')).default as any
    let resolveV2 = (_spec: Record<string, unknown>) => {}
    let resolveV1 = (_spec: Record<string, unknown>) => {}
    API.getOpenApi.mockImplementation(() => new Promise((resolve) => {
      resolveV2 = resolve
    }))
    API.getOpenApiV1.mockImplementation(() => new Promise((resolve) => {
      resolveV1 = resolve
    }))

    const freshPage = (await import('@/containers/OpenAPI.vue')).default
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    setActivePinia(pinia)

    wrapper = mount(freshPage, {
      attachTo: document.body,
      global: {
        plugins: [pinia, PrimeVue],
        stubs: { BreadCrumbs: true }
      }
    })
    await flushPromises()

    wrapper.vm.onTabChange(1)
    await flushPromises()

    resolveV2({ openapi: '3.0.1', info: { title: 'V2' }, paths: {}})
    resolveV1({ openapi: '3.0.1', info: { title: 'V1' }, paths: {}})
    await flushPromises()

    expect(API.getOpenApi).toHaveBeenCalledTimes(1)
    expect(API.getOpenApiV1).toHaveBeenCalledTimes(1)
    expect(idsRendered()).toEqual(['thedoc', 'thedocV1'])
  })
})
