import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { groupPath, nameFromParam, packagePath } from '@/lib/thresholdRoutes'

const NAMES = ['CPU Load', 'CPU 90%', 'CPU%20load', 'a/b']

const createTestRouter = () => {
  const component = { template: '<div />' }

  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component },
      { path: '/threshold-config/group/:name', component },
      { path: '/threshold-config/package/:name', component }
    ]
  })
}

describe('nameFromParam', () => {
  it('returns the param as it is, because the router has already decoded it', () => {
    expect(nameFromParam('CPU 90%')).toBe('CPU 90%')
    expect(nameFromParam('CPU%20load')).toBe('CPU%20load')
    expect(nameFromParam(undefined)).toBe('')
  })

  it.each(NAMES)('reads back the group name %s from the path groupPath builds', async (name) => {
    const router = createTestRouter()

    await router.push(groupPath(name))

    expect(nameFromParam(router.currentRoute.value.params.name)).toBe(name)
  })

  it.each(NAMES)('reads back the package name %s from the path packagePath builds', async (name) => {
    const router = createTestRouter()

    await router.push(packagePath(name))

    expect(nameFromParam(router.currentRoute.value.params.name)).toBe(name)
  })
})
