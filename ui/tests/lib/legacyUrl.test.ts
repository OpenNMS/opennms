import { legacyUrl } from '@/lib/legacyUrl'
import { useMenuStore } from '@/stores/menuStore'
import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it } from 'vitest'

const BUILD_BASE = import.meta.env.BASE_URL.replace(/ui\/?$/, '')

describe('legacyUrl', () => {
  afterEach(() => setActivePinia(undefined))

  it('falls back to the build-time base with no store at all', () => {
    setActivePinia(undefined)
    expect(legacyUrl('admin/applications.htm')).toBe(`${BUILD_BASE}admin/applications.htm`)
  })

  it('falls back to the build-time base until the menu has loaded', () => {
    setActivePinia(createPinia())
    expect(legacyUrl('admin/applications.htm')).toBe(`${BUILD_BASE}admin/applications.htm`)
  })

  it('prefers the baseHref the server put in the menu', () => {
    setActivePinia(createPinia())
    useMenuStore().mainMenu = { baseHref: '/nms/' } as any
    expect(legacyUrl('element/node.jsp?node=1')).toBe('/nms/element/node.jsp?node=1')
  })
})
