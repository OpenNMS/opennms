import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useScvStore } from '@/stores/scvStore'
import API from '@/services'
import { SCV_GET_ALL_ALIAS } from '@/lib/constants'
import { SCVCredentials } from '@/types/scv'

vi.mock('@/services', () => ({
  default: {
    getAliases: vi.fn(),
    getCredentialsByAlias: vi.fn(),
    getAllCredentials: vi.fn(),
    addCredentials: vi.fn(),
    updateCredentials: vi.fn()
  }
}))

describe('useScvStore', () => {
  let store: ReturnType<typeof useScvStore>

  const mockAliases = ['alias1', 'alias2', 'alias3']

  const mockCredentials: SCVCredentials = {
    alias: 'testAlias',
    username: 'testUser',
    password: 'testPassword',
    attributes: {
      key1: 'value1',
      key2: 'value2'
    }
  }

  const mockAllCredentials: SCVCredentials[] = [
    {
      alias: 'alias1',
      username: 'user1',
      password: 'pass1',
      attributes: { attr1: 'val1', attr2: 'val2' }
    },
    {
      alias: 'alias2',
      username: 'user2',
      password: 'pass2',
      attributes: { attr3: 'val3' }
    },
    {
      alias: 'zAlias',
      username: 'user3',
      password: 'pass3',
      attributes: { searchKey: 'searchValue' }
    }
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useScvStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      expect(store.aliases).toEqual([])
      expect(store.credentials).toEqual({
        alias: '',
        username: '',
        password: '',
        attributes: {}
      })
      expect(store.dbCredentials).toEqual({})
      expect(store.isEditing).toBe(false)
    })
  })

  describe('getAliases', () => {
    it('should fetch aliases successfully', async () => {
      vi.mocked(API.getAliases).mockResolvedValue(mockAliases)

      await store.getAliases()

      expect(API.getAliases).toHaveBeenCalledTimes(1)
      expect(store.aliases).toEqual(mockAliases)
    })
  })

  describe('getCredentialsByAlias', () => {
    it('should fetch credentials by alias successfully', async () => {
      vi.mocked(API.getCredentialsByAlias).mockResolvedValue(mockCredentials)

      await store.getCredentialsByAlias('testAlias')

      expect(API.getCredentialsByAlias).toHaveBeenCalledWith('testAlias')
      expect(store.credentials).toEqual(mockCredentials)
      expect(store.dbCredentials).toEqual(mockCredentials)
      expect(store.isEditing).toBe(true)
    })

    it('should not update state when credentials are not found', async () => {
      vi.mocked(API.getCredentialsByAlias).mockResolvedValue(null)
      const initialCredentials = { ...store.credentials }

      await store.getCredentialsByAlias('nonExistentAlias')

      expect(store.credentials).toEqual(initialCredentials)
      expect(store.isEditing).toBe(false)
    })
  })

  describe('getAllCredentials', () => {
    it('should return all credentials', () => {
      store.populate()
      const result = store.getAllCredentials()

      expect(result).toBeDefined()
    })
  })

  describe('populate', () => {
    it('should populate all credentials successfully', async () => {
      vi.mocked(API.getAllCredentials).mockResolvedValue(mockAllCredentials)

      await store.populate()

      expect(API.getAllCredentials).toHaveBeenCalledTimes(1)
    })

    it('should not update state when populate fails', async () => {
      vi.mocked(API.getAllCredentials).mockResolvedValue(null)

      await store.populate()

      expect(API.getAllCredentials).toHaveBeenCalledTimes(1)
    })
  })

  describe('addCredentials', () => {
    it('should add credentials successfully', async () => {
      vi.mocked(API.addCredentials).mockResolvedValue(200)
      vi.mocked(API.getAliases).mockResolvedValue(mockAliases)

      store.credentials = { ...mockCredentials }

      await store.addCredentials()

      expect(API.addCredentials).toHaveBeenCalledWith(mockCredentials)
      expect(API.getAliases).toHaveBeenCalledTimes(1)
      expect(store.credentials.alias).toBe('')
      expect(store.isEditing).toBe(false)
    })

    it('should throw error when alias is missing', async () => {
      store.credentials = {
        alias: '',
        username: 'test',
        password: 'test',
        attributes: {}
      }

      await expect(store.addCredentials()).rejects.toThrow('Alias is required to add new credentials.')
    })

    it('should throw error when using reserved alias', async () => {
      store.credentials = {
        alias: SCV_GET_ALL_ALIAS,
        username: 'test',
        password: 'test',
        attributes: {}
      }

      await expect(store.addCredentials()).rejects.toThrow(
        `The alias "${SCV_GET_ALL_ALIAS}" is reserved and cannot be used.`
      )
    })

    it('should throw error when using reserved alias in uppercase', async () => {
      store.credentials = {
        alias: SCV_GET_ALL_ALIAS.toUpperCase(),
        username: 'test',
        password: 'test',
        attributes: {}
      }

      await expect(store.addCredentials()).rejects.toThrow(
        `The alias "${SCV_GET_ALL_ALIAS}" is reserved and cannot be used.`
      )
    })

    it('should not clear credentials when add fails', async () => {
      vi.mocked(API.addCredentials).mockResolvedValue(null)

      store.credentials = { ...mockCredentials }
      const credsBefore = { ...store.credentials }

      await store.addCredentials()

      expect(store.credentials).toEqual(credsBefore)
    })
  })

  describe('updateCredentials', () => {
    it('should update credentials successfully', async () => {
      vi.mocked(API.updateCredentials).mockResolvedValue(200)

      store.credentials = { ...mockCredentials }

      await store.updateCredentials()

      expect(API.updateCredentials).toHaveBeenCalledWith(mockCredentials)
      expect(store.credentials.alias).toBe('')
      expect(store.isEditing).toBe(false)
    })

    it('should throw error when alias is missing', async () => {
      store.credentials = {
        alias: '',
        username: 'test',
        password: 'test',
        attributes: {}
      }

      await expect(store.updateCredentials()).rejects.toThrow('Alias is required to add new credentials.')
    })

    it('should throw error when using reserved alias', async () => {
      store.credentials = {
        alias: SCV_GET_ALL_ALIAS,
        username: 'test',
        password: 'test',
        attributes: {}
      }

      await expect(store.updateCredentials()).rejects.toThrow(
        `The alias "${SCV_GET_ALL_ALIAS}" is reserved and cannot be used.`
      )
    })

    it('should not clear credentials when update fails', async () => {
      vi.mocked(API.updateCredentials).mockResolvedValue(null)

      store.credentials = { ...mockCredentials }
      const credsBefore = { ...store.credentials }

      await store.updateCredentials()

      expect(store.credentials).toEqual(credsBefore)
    })
  })

  describe('queryCredentials', () => {
    beforeEach(async () => {
      vi.mocked(API.getAllCredentials).mockResolvedValue(mockAllCredentials)
      await store.populate()
    })

    it('should return matching aliases and their keys', () => {
      const results = store.queryCredentials('alias1')

      expect(results.length).toBeGreaterThan(0)
      expect(results[0].alias).toBe('alias1')
      expect(results[0].type).toBe('alias')
    })

    it('should return items sorted by alias', () => {
      const results = store.queryCredentials('alias')

      const aliasItems = results.filter((item) => item.type === 'alias')
      expect(aliasItems[0].alias).toBe('alias1')
      expect(aliasItems[1].alias).toBe('alias2')
      expect(aliasItems[2].alias).toBe('zAlias')
    })

    it('should match by key and return parent alias', () => {
      const results = store.queryCredentials('searchKey')

      expect(results.length).toBeGreaterThan(0)
      const aliasItem = results.find((item) => item.type === 'alias' && item.alias === 'zAlias')
      const keyItem = results.find((item) => item.type === 'key' && item.key === 'searchKey')

      expect(aliasItem).toBeDefined()
      expect(keyItem).toBeDefined()
    })

    it('should search case-insensitively', () => {
      const results1 = store.queryCredentials('ALIAS1')
      const results2 = store.queryCredentials('alias1')

      expect(results1.length).toBe(results2.length)
    })

    it('should include username and password as searchable keys', () => {
      const results = store.queryCredentials('username')

      expect(results.length).toBeGreaterThan(0)
      expect(results.some((item) => item.key === 'username')).toBe(true)
    })

    it('should return empty array when no matches found', () => {
      const results = store.queryCredentials('nonExistentQuery')

      expect(results).toEqual([])
    })

    it('should return all keys when a partial query matches all aliases', () => {
      const results = store.queryCredentials('alia')
    
      expect(results.length).toBe(13) // 3 alias items + 10 keys total
      expect(results[0].alias).toBe('alias1')
      expect(results[0].type).toBe('alias')

      expect(results[1].alias).toBe('alias1')
      expect(results[1].key).toBe('username')
      expect(results[1].type).toBe('key')

      expect(results[2].alias).toBe('alias1')
      expect(results[2].key).toBe('password')
      expect(results[2].type).toBe('key')

      expect(results[3].alias).toBe('alias1')
      expect(results[3].key).toBe('attr1')
      expect(results[3].type).toBe('key')

      expect(results[4].alias).toBe('alias1')
      expect(results[4].key).toBe('attr2')
      expect(results[4].type).toBe('key')

      expect(results[5].alias).toBe('alias2')
      expect(results[5].type).toBe('alias')

      expect(results[6].alias).toBe('alias2')
      expect(results[6].key).toBe('username')
      expect(results[6].type).toBe('key')

      expect(results[7].alias).toBe('alias2')
      expect(results[7].key).toBe('password')
      expect(results[7].type).toBe('key')

      expect(results[8].alias).toBe('alias2')
      expect(results[8].key).toBe('attr3')
      expect(results[8].type).toBe('key')

      expect(results[9].alias).toBe('zAlias')
      expect(results[9].type).toBe('alias')

      expect(results[10].alias).toBe('zAlias')
      expect(results[10].key).toBe('username')
      expect(results[10].type).toBe('key')

      expect(results[11].alias).toBe('zAlias')
      expect(results[11].key).toBe('password')
      expect(results[11].type).toBe('key')

      expect(results[12].alias).toBe('zAlias')
      expect(results[12].key).toBe('searchKey')
      expect(results[12].type).toBe('key')
    })

    it('should return all aliases and keys when the query is empty', () => {
      const results = store.queryCredentials('')

      expect(results.length).toBe(13) // 3 alias items + 10 keys total
      expect(results[0].alias).toBe('alias1')
      expect(results[0].type).toBe('alias')

      expect(results[1].alias).toBe('alias1')
      expect(results[1].key).toBe('username')
      expect(results[1].type).toBe('key')

      expect(results[2].alias).toBe('alias1')
      expect(results[2].key).toBe('password')
      expect(results[2].type).toBe('key')

      expect(results[3].alias).toBe('alias1')
      expect(results[3].key).toBe('attr1')

      expect(results[3].type).toBe('key')

      expect(results[4].alias).toBe('alias1')
      expect(results[4].key).toBe('attr2')
      expect(results[4].type).toBe('key')

      expect(results[5].alias).toBe('alias2')
      expect(results[5].type).toBe('alias')

      expect(results[6].alias).toBe('alias2')
      expect(results[6].key).toBe('username')
      expect(results[6].type).toBe('key')

      expect(results[7].alias).toBe('alias2')
      expect(results[7].key).toBe('password')
      expect(results[7].type).toBe('key')

      expect(results[8].alias).toBe('alias2')
      expect(results[8].key).toBe('attr3')
      expect(results[8].type).toBe('key')

      expect(results[9].alias).toBe('zAlias')
      expect(results[9].type).toBe('alias')

      expect(results[10].alias).toBe('zAlias')
      expect(results[10].key).toBe('username')
      expect(results[10].type).toBe('key')

      expect(results[11].alias).toBe('zAlias')
      expect(results[11].key).toBe('password')
      expect(results[11].type).toBe('key')

      expect(results[12].alias).toBe('zAlias')
      expect(results[12].key).toBe('searchKey')
      expect(results[12].type).toBe('key')
    })

    it('should return correct keys for a partial query match on password', () => {
      const results = store.queryCredentials('pass')
    
      expect(results.length).toBe(6) // 3 matching password keys, plus 3 parent aliases
      expect(results[0].alias).toBe('alias1')
      expect(results[0].type).toBe('alias')
      expect(results[1].alias).toBe('alias1')
      expect(results[1].key).toBe('password')
      expect(results[1].type).toBe('key')


      expect(results[2].alias).toBe('alias2')
      expect(results[2].type).toBe('alias')
      expect(results[3].alias).toBe('alias2')
      expect(results[3].key).toBe('password')
      expect(results[3].type).toBe('key')

      expect(results[4].alias).toBe('zAlias')
      expect(results[4].type).toBe('alias')
      expect(results[5].alias).toBe('zAlias')
      expect(results[5].key).toBe('password')
      expect(results[5].type).toBe('key')
    })

    it('should return correct keys for a partial query match on an attribute', () => {
      const results = store.queryCredentials('attr')

      expect(results.length).toBe(5) // 3 matching attribute keys, plus 2 parent aliases
      expect(results[0].alias).toBe('alias1')
      expect(results[0].type).toBe('alias')
      expect(results[1].alias).toBe('alias1')
      expect(results[1].key).toBe('attr1')
      expect(results[1].type).toBe('key')

      expect(results[2].alias).toBe('alias1')
      expect(results[2].key).toBe('attr2')
      expect(results[2].type).toBe('key')

      expect(results[3].alias).toBe('alias2')
      expect(results[3].type).toBe('alias')
      expect(results[4].alias).toBe('alias2')
      expect(results[4].key).toBe('attr3')
      expect(results[4].type).toBe('key')
    })
 
    it('should return correct keys for a partial query match on a different attribute', () => {
      const results = store.queryCredentials('earch')

      expect(results.length).toBe(2)
      expect(results[0].alias).toBe('zAlias')
      expect(results[0].type).toBe('alias')

      expect(results[1].alias).toBe('zAlias')
      expect(results[1].key).toBe('searchKey')
      expect(results[1].type).toBe('key')
    })
  })

  describe('setValue', () => {
    it('should update credentials with provided key-value pairs', () => {
      store.setValue({ alias: 'newAlias' })

      expect(store.credentials.alias).toBe('newAlias')
    })

    it('should merge multiple values', () => {
      store.setValue({ alias: 'testAlias', username: 'testUser' })

      expect(store.credentials.alias).toBe('testAlias')
      expect(store.credentials.username).toBe('testUser')
    })

    it('should preserve existing values not being updated', () => {
      store.credentials = { ...mockCredentials }
      store.setValue({ username: 'newUser' })

      expect(store.credentials.username).toBe('newUser')
      expect(store.credentials.alias).toBe(mockCredentials.alias)
      expect(store.credentials.password).toBe(mockCredentials.password)
    })
  })

  describe('clearCredentials', () => {
    it('should reset credentials to initial state', async () => {
      store.credentials = { ...mockCredentials }
      store.dbCredentials = { ...mockCredentials }
      store.isEditing = true

      await store.clearCredentials()

      expect(store.credentials).toEqual({
        id: undefined,
        alias: '',
        username: '',
        password: '',
        attributes: {}
      })
      expect(store.dbCredentials).toEqual({
        id: undefined,
        alias: '',
        username: '',
        password: '',
        attributes: {}
      })
      expect(store.isEditing).toBe(false)
    })
  })

  describe('addAttribute', () => {
    it('should add empty attribute to credentials', () => {
      const initialCount = Object.keys(store.credentials.attributes).length

      store.addAttribute()

      expect(Object.keys(store.credentials.attributes).length).toBe(initialCount + 1)
      expect(store.credentials.attributes['']).toBe('')
    })

    it('should preserve existing attributes', () => {
      store.credentials.attributes = { key1: 'value1' }

      store.addAttribute()

      expect(store.credentials.attributes.key1).toBe('value1')
      expect(store.credentials.attributes['']).toBe('')
    })
  })

  describe('updateAttribute', () => {
    beforeEach(() => {
      store.credentials.attributes = {
        oldKey: 'oldValue',
        anotherKey: 'anotherValue'
      }
    })

    it('should update attribute value when key remains the same', () => {
      store.updateAttribute({
        key: 'oldKey',
        keyVal: { key: 'oldKey', value: 'newValue' }
      })

      expect(store.credentials.attributes.oldKey).toBe('newValue')
      expect(Object.keys(store.credentials.attributes).length).toBe(2)
    })

    it('should replace attribute key when key changes', () => {
      store.updateAttribute({
        key: 'oldKey',
        keyVal: { key: 'newKey', value: 'newValue' }
      })

      expect(store.credentials.attributes.oldKey).toBeUndefined()
      expect(store.credentials.attributes.newKey).toBe('newValue')
      expect(Object.keys(store.credentials.attributes).length).toBe(2)
    })

    it('should preserve other attributes when updating', () => {
      store.updateAttribute({
        key: 'oldKey',
        keyVal: { key: 'newKey', value: 'newValue' }
      })

      expect(store.credentials.attributes.anotherKey).toBe('anotherValue')
    })
  })

  describe('removeAttribute', () => {
    beforeEach(() => {
      store.credentials.attributes = {
        key1: 'value1',
        key2: 'value2',
        key3: 'value3'
      }
    })

    it('should remove specified attribute', () => {
      store.removeAttribute('key2')

      expect(store.credentials.attributes.key2).toBeUndefined()
      expect(Object.keys(store.credentials.attributes).length).toBe(2)
    })

    it('should preserve other attributes', () => {
      store.removeAttribute('key2')

      expect(store.credentials.attributes.key1).toBe('value1')
      expect(store.credentials.attributes.key3).toBe('value3')
    })

    it('should handle removing non-existent key gracefully', () => {
      const initialCount = Object.keys(store.credentials.attributes).length

      store.removeAttribute('nonExistentKey')

      expect(Object.keys(store.credentials.attributes).length).toBe(initialCount)
    })
  })
})
