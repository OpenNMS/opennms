import OpenAPI from '@/containers/OpenAPI.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Tab from 'primevue/tab'
import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest'

const { loadSpecMock } = vi.hoisted(() => ({
  loadSpecMock: vi.fn()
}))

// The web component is irrelevant here; a stub that records loadSpec calls per
// element id is what lets us see which doc got rendered.
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

const idsRendered = () => loadSpecMock.mock.calls.map(([id]) => id)

// happy-dom has no ResizeObserver, and what gets observed is worth asserting
let observedTargets: Element[] = []
let disconnected = false

class ResizeObserverStub {
  observe(target: Element) {
    observedTargets.push(target)
  }
  disconnect() {
    disconnected = true
  }
  unobserve() {
    // not used
  }
}

// Per-test mount: the height measurement reads document-level geometry, which a
// shared mount leaks between tests.
const mountPage = async (
  options: { viewport?: number, columnTop?: number, footerHeight?: number } = {},
  component: any = OpenAPI
) => {
  const { viewport = 900, columnTop = 140, footerHeight = 40 } = options

  observedTargets = []
  disconnected = false
  loadSpecMock.mockClear()
  vi.stubGlobal('ResizeObserver', ResizeObserverStub)

  Object.defineProperty(document.documentElement, 'clientHeight', { value: viewport, configurable: true })

  const footer = document.createElement('div')
  footer.className = 'app-footer'
  document.body.appendChild(footer)

  vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
    if (this.classList.contains('app-footer')) {
      return { top: viewport - footerHeight, height: footerHeight } as DOMRect
    }
    if (this.classList.contains('doc-tabs')) {
      return { top: columnTop, height: 0 } as DOMRect
    }
    return { top: 0, height: 0 } as DOMRect
  })

  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
  setActivePinia(pinia)

  const wrapper = mount(component, {
    attachTo: document.body,
    global: {
      plugins: [pinia, PrimeVue],
      stubs: { BreadCrumbs: true }
    }
  })

  await flushPromises()
  return wrapper
}

const teardown = (wrapper: VueWrapper<any> | null) => {
  wrapper?.unmount()
  document.querySelectorAll('.app-footer').forEach(el => el.remove())
  vi.restoreAllMocks()
  vi.clearAllMocks()
}

describe('OpenAPI.vue', () => {
  let wrapper: VueWrapper<any> | null = null

  beforeAll(() => {
    if (!customElements.get('rapi-doc')) {
      customElements.define('rapi-doc', RapiDocStub)
    }
  })

  afterEach(() => {
    teardown(wrapper)
    wrapper = null
  })

  it('renders a tab per API version', async () => {
    wrapper = await mountPage()

    const tabs = wrapper.findAllComponents(Tab)
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('V2 API')
    expect(tabs[1].text()).toBe('V1 API')
  })

  it('renders only the V2 doc up front', async () => {
    wrapper = await mountPage()

    expect(idsRendered()).toEqual(['thedoc'])
  })

  it('renders the V1 doc when its tab is first opened', async () => {
    wrapper = await mountPage()

    wrapper.vm.onTabChange(1)
    await flushPromises()

    expect(idsRendered()).toEqual(['thedoc', 'thedocV1'])
  })

  it('does not re-render the V1 doc when returning to its tab', async () => {
    wrapper = await mountPage()

    wrapper.vm.onTabChange(1)
    await flushPromises()
    wrapper.vm.onTabChange(0)
    await flushPromises()
    wrapper.vm.onTabChange(1)
    await flushPromises()

    expect(idsRendered().filter(id => id === 'thedocV1')).toHaveLength(1)
  })

  // The specs are cached at module scope, so observing a cold start takes a fresh
  // module instance. Both fetches are left pending until after the tab is clicked.
  it('shares the in-flight fetch when the V1 tab is opened before it resolves', async () => {
    vi.resetModules()

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
    wrapper = await mountPage({}, freshPage)

    wrapper.vm.onTabChange(1)
    await flushPromises()

    resolveV2({ openapi: '3.0.1', info: { title: 'V2' }, paths: {}})
    resolveV1({ openapi: '3.0.1', info: { title: 'V1' }, paths: {}})
    await flushPromises()

    expect(API.getOpenApi).toHaveBeenCalledTimes(1)
    expect(API.getOpenApiV1).toHaveBeenCalledTimes(1)
    expect(idsRendered()).toEqual(['thedoc', 'thedocV1'])
  })

  it('coerces the selected tab to a number', async () => {
    wrapper = await mountPage()

    wrapper.vm.onTabChange('1')
    await flushPromises()

    expect(wrapper.vm.activeTab).toBe(1)
    expect(typeof wrapper.vm.activeTab).toBe('number')
  })

  it('keeps both docs inside tab panels so only one is displayed', async () => {
    wrapper = await mountPage()

    const column = wrapper.find('.doc-tabs')
    expect(column.exists()).toBe(true)

    const docs = column.findAll('rapi-doc')
    expect(docs).toHaveLength(2)

    for (const doc of docs) {
      expect(doc.element.closest('.p-tabpanel')).not.toBeNull()
    }
  })

  // Any page overflow reintroduces the jump, so the column takes exactly what is left.
  it('sizes the doc column to the space left in the viewport', async () => {
    wrapper = await mountPage({ viewport: 900, columnTop: 140, footerHeight: 40 })

    expect((wrapper.find('.doc-tabs').element as HTMLElement).style.height).toBe('720px')
  })

  // The header grows after the first measurement, so one measurement is not enough.
  it('re-measures when the page chrome reflows', async () => {
    wrapper = await mountPage()

    expect(observedTargets).toContain(document.body)
  })

  it('clamps the column height when the viewport cannot hold it', async () => {
    // 200 viewport less a 40 footer leaves 20, well under the minimum
    wrapper = await mountPage({ viewport: 200, columnTop: 140, footerHeight: 40 })

    expect((wrapper.find('.doc-tabs').element as HTMLElement).style.height).toBe('320px')
  })

  // Watched on the column, so the tab strip is covered as well as the docs.
  it('schedules a scroll restore after a click anywhere in the column', async () => {
    wrapper = await mountPage()
    const frames = vi.fn()
    vi.stubGlobal('requestAnimationFrame', frames)

    await wrapper.find('.doc-tabs').trigger('click')
    expect(frames).toHaveBeenCalled()

    frames.mockClear()
    await wrapper.findAllComponents(Tab)[1].trigger('click')
    expect(frames).toHaveBeenCalled()
  })

  // Focus-driven scrolls land outside the frames the click handler watches.
  it('holds the document at the top while the column fits', async () => {
    wrapper = await mountPage()
    document.documentElement.scrollTop = 120

    window.dispatchEvent(new Event('scroll'))

    expect(document.documentElement.scrollTop).toBe(0)
  })

  it('leaves the page scroll alone when the column cannot fit', async () => {
    wrapper = await mountPage({ viewport: 200, columnTop: 140, footerHeight: 40 })
    document.documentElement.scrollTop = 120

    window.dispatchEvent(new Event('scroll'))

    expect(document.documentElement.scrollTop).toBe(120)
  })

  it('stops measuring and listening once unmounted', async () => {
    wrapper = await mountPage()
    const column = wrapper.find('.doc-tabs').element
    const removeListener = vi.spyOn(window, 'removeEventListener')
    const columnRemove = vi.spyOn(column, 'removeEventListener')

    wrapper.unmount()
    wrapper = null

    expect(removeListener).toHaveBeenCalledWith('resize', expect.any(Function))
    expect(disconnected).toBe(true)
    expect(columnRemove).toHaveBeenCalledWith('click', expect.any(Function), true)
    expect(columnRemove).toHaveBeenCalledWith('keyup', expect.any(Function), true)
  })
})
