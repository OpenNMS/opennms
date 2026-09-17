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
const fail = (message: string) => ({ success: false, message })

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

  describe('service CRUD', () => {
    beforeEach(() => {
      const store = useThreshdConfigurationStore()
      store.currentPackage = packageWithGroup('one', 'mib2')
    })

    test('appends when the index is null and replaces when it is not', () => {
      const store = useThreshdConfigurationStore()

      store.upsertService(null, { ...getDefaultThreshdService(), name: 'ICMP' })
      expect(store.currentPackage?.services).toHaveLength(2)

      store.upsertService(0, { ...getDefaultThreshdService(), name: 'replaced' })
      expect(store.currentPackage?.services).toHaveLength(2)
      expect(store.currentPackage?.services[0].name).toBe('replaced')
    })

    test('removes by index and ignores an index that is not there', () => {
      const store = useThreshdConfigurationStore()

      store.removeService(9)
      expect(store.currentPackage?.services).toHaveLength(1)

      store.removeService(0)
      expect(store.currentPackage?.services).toHaveLength(0)
    })

    test('does nothing when no package is loaded', () => {
      const store = useThreshdConfigurationStore()
      store.currentPackage = null

      expect(() => store.upsertService(null, getDefaultThreshdService())).not.toThrow()
      expect(() => store.removeService(0)).not.toThrow()
    })
  })

  describe('packages', () => {
    test('clears the loaded package when it is the one deleted', async () => {
      const store = useThreshdConfigurationStore()
      store.currentPackage = packageWithGroup('one', 'mib2')

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

    test('saves the whole package and refetches it for the new entity tag', async () => {
      const store = useThreshdConfigurationStore()
      const sent = packageWithGroup('one', 'mib2')
      store.currentPackage = sent

      vi.mocked(API.updateThreshdPackage).mockResolvedValue(ok() as never)
      vi.mocked(API.getThreshdPackage).mockResolvedValue(ok({ ...sent, version: 'new' }) as never)
      vi.mocked(API.getThreshdConfiguration).mockResolvedValue(ok(getDefaultThreshdConfiguration()) as never)

      await store.saveCurrentPackage()

      expect(API.updateThreshdPackage).toHaveBeenCalledWith('one', sent)
      expect(store.currentPackage?.version).toBe('new')
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
