import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import {
  getDefaultAddressRange,
  getDefaultThreshdConfiguration,
  getDefaultThreshdPackage,
  getDefaultThreshdService,
  useThreshdConfigurationStore
} from '@/stores/threshdConfigurationStore'
import { THRESHOLDING_GROUP_PARAMETER, ThreshdServiceStatus } from '@/lib/thresholdValidator'
import API from '@/services'
import type { ThreshdConfiguration, ThreshdPackage } from '@/types/thresholdConfig'

vi.mock('@/services', () => ({
  default: {
    getThreshdConfiguration: vi.fn(),
    getThreshdPackages: vi.fn(),
    getThreshdPackage: vi.fn(),
    createThreshdPackage: vi.fn(),
    updateThreshdPackage: vi.fn(),
    updateThreshdConfiguration: vi.fn(),
    deleteThreshdPackage: vi.fn(),
    reloadThreshdConfiguration: vi.fn()
  }
}))

const ok = (payload?: unknown) => ({ success: true, message: '', payload })
const fail = (message: string, status?: number) => ({ success: false, message, status })

const packageWithGroup = (name: string, groupName: string): ThreshdPackage => ({
  ...getDefaultThreshdPackage(),
  name,
  filter: 'IPADDR != \'0.0.0.0\'',
  services: [
    {
      ...getDefaultThreshdService(),
      name: 'SNMP',
      parameters: [{ key: THRESHOLDING_GROUP_PARAMETER, value: groupName }]
    }
  ]
})

const config = (packages: ThreshdPackage[]): ThreshdConfiguration => ({
  ...getDefaultThreshdConfiguration(),
  packages
})

describe('threshdConfigurationStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('defaults', () => {
    test('seeds a new service with the parameter that binds it to a threshold group', () => {
      // A service without this parameter thresholds nothing, which is a confusing thing to create by default.
      const service = getDefaultThreshdService()

      expect(service.parameters).toEqual([{ key: THRESHOLDING_GROUP_PARAMETER, value: '' }])
      expect(service.status).toBe(ThreshdServiceStatus.On)
      expect(service.interval).toBeGreaterThan(0)
    })

    test('gives every default its own arrays', () => {
      getDefaultThreshdPackage().services.push(getDefaultThreshdService())

      expect(getDefaultThreshdPackage().services).toEqual([])
      expect(getDefaultAddressRange()).toEqual({ begin: '', end: '' })
    })
  })

  describe('packagesUsingGroup', () => {
    test('finds every package whose services apply the group', () => {
      const store = useThreshdConfigurationStore()
      store.config = config([
        packageWithGroup('one', 'mib2'),
        packageWithGroup('two', 'cisco'),
        packageWithGroup('three', 'mib2')
      ])

      expect(store.packagesUsingGroup('mib2')).toEqual(['one', 'three'])
      expect(store.packagesUsingGroup('cisco')).toEqual(['two'])
      expect(store.packagesUsingGroup('unused')).toEqual([])
    })

    test('copes with packages that have no services or no parameters', () => {
      const store = useThreshdConfigurationStore()
      store.config = config([
        { ...getDefaultThreshdPackage(), name: 'bare' },
        packageWithGroup('one', 'mib2')
      ])

      expect(store.packagesUsingGroup('mib2')).toEqual(['one'])
    })
  })

  describe('saving', () => {
    // As the detail page has it: fetched under 'one', so writes address 'one' whatever the form says.
    const load = async (pkg: ThreshdPackage) => {
      const store = useThreshdConfigurationStore()
      vi.mocked(API.getThreshdPackage).mockResolvedValueOnce(ok(pkg) as never)
      await store.fetchPackage(pkg.name)
      return store
    }

    beforeEach(() => {
      vi.mocked(API.getThreshdConfiguration).mockResolvedValue(ok(getDefaultThreshdConfiguration()) as never)
      vi.mocked(API.getThreshdPackages).mockResolvedValue(ok([]) as never)
    })

    test('saves the whole package and refetches it for the new entity tag', async () => {
      const sent = packageWithGroup('one', 'mib2')
      const store = await load(sent)

      vi.mocked(API.updateThreshdPackage).mockResolvedValue(ok() as never)
      vi.mocked(API.getThreshdPackage).mockResolvedValue(ok({ ...sent, version: 'new' }) as never)

      await store.saveCurrentPackage()

      expect(API.updateThreshdPackage).toHaveBeenCalledWith('one', sent)
      expect(store.currentPackage?.version).toBe('new')
    })

    test('writes a pending rename to the stored name and then follows it', async () => {
      const store = await load(packageWithGroup('one', 'mib2'))
      store.currentPackage!.name = ' renamed '

      vi.mocked(API.updateThreshdPackage).mockResolvedValue(ok() as never)
      vi.mocked(API.getThreshdPackage).mockResolvedValue(ok(packageWithGroup('renamed', 'mib2')) as never)

      await store.saveService(null, { ...getDefaultThreshdService(), name: 'ICMP' })

      expect(vi.mocked(API.updateThreshdPackage).mock.calls[0][0]).toBe('one')
      expect(API.getThreshdPackage).toHaveBeenLastCalledWith('renamed')
      expect(store.loadedPackageName).toBe('renamed')
    })

    test('appends when the index is null and replaces when it is not', async () => {
      const store = await load(packageWithGroup('one', 'mib2'))
      vi.mocked(API.updateThreshdPackage).mockResolvedValue(fail('stop here') as never)

      await store.saveService(null, { ...getDefaultThreshdService(), name: 'ICMP' })
      expect(vi.mocked(API.updateThreshdPackage).mock.calls[0][1].services.map(s => s.name)).toEqual(['SNMP', 'ICMP'])

      await store.saveService(0, { ...getDefaultThreshdService(), name: 'replaced' })
      expect(vi.mocked(API.updateThreshdPackage).mock.calls[1][1].services.map(s => s.name)).toEqual(['replaced'])
    })

    test('leaves the loaded package untouched when a service save fails', async () => {
      const store = await load(packageWithGroup('one', 'mib2'))
      vi.mocked(API.updateThreshdPackage).mockResolvedValue(fail('Threshd package \'one\' has changed since it was read.') as never)

      const added = await store.saveService(null, { ...getDefaultThreshdService(), name: 'ICMP' })
      const edited = await store.saveService(0, { ...getDefaultThreshdService(), name: 'replaced' })
      const deleted = await store.deleteService(0)

      expect([added.success, edited.success, deleted.success]).toEqual([false, false, false])
      expect(store.currentPackage?.services.map(s => s.name)).toEqual(['SNMP'])
    })

    test('loads the stored package after a 412, so the next save is not rejected too', async () => {
      const store = await load({ ...packageWithGroup('one', 'mib2'), version: 'stale' })
      vi.mocked(API.updateThreshdPackage).mockResolvedValue(fail('Threshd package \'one\' has changed since it was read.', 412) as never)
      vi.mocked(API.getThreshdPackage).mockResolvedValue(
        ok({ ...packageWithGroup('one', 'mib2'), outageCalendars: ['maintenance'], version: 'current' }) as never)

      const result = await store.saveService(null, { ...getDefaultThreshdService(), name: 'ICMP' })

      expect(result.success).toBe(false)
      expect(result.message).toContain('The current version has been loaded')
      expect(API.getThreshdPackage).toHaveBeenLastCalledWith('one')
      expect(store.currentPackage?.version).toBe('current')
      expect(store.currentPackage?.outageCalendars).toEqual(['maintenance'])
    })

    test('removes by index and refuses an index that is not there', async () => {
      const store = await load(packageWithGroup('one', 'mib2'))
      vi.mocked(API.updateThreshdPackage).mockResolvedValue(fail('stop here') as never)

      expect((await store.deleteService(9)).success).toBe(false)
      expect(API.updateThreshdPackage).not.toHaveBeenCalled()

      await store.deleteService(0)
      expect(vi.mocked(API.updateThreshdPackage).mock.calls[0][1].services).toEqual([])
    })

    test('refuses to save when no package is loaded', async () => {
      const store = useThreshdConfigurationStore()

      expect((await store.saveCurrentPackage()).success).toBe(false)
      expect((await store.saveService(null, getDefaultThreshdService())).success).toBe(false)
      expect((await store.deleteService(0)).success).toBe(false)
      expect(API.updateThreshdPackage).not.toHaveBeenCalled()
    })
  })

  describe('packages', () => {
    test('clears the loaded package when it is the one deleted', async () => {
      const store = useThreshdConfigurationStore()
      vi.mocked(API.getThreshdPackage).mockResolvedValueOnce(ok(packageWithGroup('one', 'mib2')) as never)
      await store.fetchPackage('one')

      vi.mocked(API.deleteThreshdPackage).mockResolvedValue(ok() as never)
      vi.mocked(API.getThreshdConfiguration).mockResolvedValue(ok(getDefaultThreshdConfiguration()) as never)
      vi.mocked(API.getThreshdPackages).mockResolvedValue(ok([]) as never)

      await store.deletePackage('one')

      expect(store.currentPackage).toBeNull()
    })

    test('drops the loaded package when a fetch fails', async () => {
      const store = useThreshdConfigurationStore()
      store.currentPackage = packageWithGroup('one', 'mib2')

      vi.mocked(API.getThreshdPackage).mockResolvedValue(fail('gone') as never)

      await store.fetchPackage('one')

      expect(store.currentPackage).toBeNull()
    })
  })

  test('resetState clears everything', () => {
    const store = useThreshdConfigurationStore()
    store.config = config([packageWithGroup('one', 'mib2')])
    store.currentPackage = packageWithGroup('one', 'mib2')

    store.resetState()

    expect(store.config.packages).toEqual([])
    expect(store.currentPackage).toBeNull()
  })
})
