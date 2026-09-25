import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import API from '@/services'

vi.mock('@/services', () => ({
  default: {
    listCategories: vi.fn(),
    createCategory: vi.fn(),
    updateCategoryDescription: vi.fn(),
    deleteCategory: vi.fn()
  }
}))

const cat = (name: string) => ({ id: 1, name, description: '', authorizedGroups: [] })
const ok = { success: true, message: '' }
const failed = (message: string) => ({ success: false, message })

describe('useCategoryAdminStore', () => {
  let store: ReturnType<typeof useCategoryAdminStore>
  beforeEach(() => {
    setActivePinia(createPinia())
    store = useCategoryAdminStore()
    vi.clearAllMocks()
  })

  it('starts empty', () => {
    expect(store.categories).toEqual([])
  })

  it('getCategories loads on success and preserves on failure', async () => {
    vi.mocked(API.listCategories).mockResolvedValue([cat('Routers')])
    expect(await store.getCategories()).toBe(true)
    expect(store.categories).toEqual([cat('Routers')])
    expect(store.loadError).toBe(false)
    vi.mocked(API.listCategories).mockResolvedValue(null)
    expect(await store.getCategories()).toBe(false)
    expect(store.categories).toEqual([cat('Routers')])
    expect(store.loadError).toBe(true)
  })

  it('createCategory refreshes on success, not on failure', async () => {
    vi.mocked(API.createCategory).mockResolvedValue(ok)
    vi.mocked(API.listCategories).mockResolvedValue([cat('A')])
    expect((await store.createCategory(cat('A'))).success).toBe(true)
    expect(API.listCategories).toHaveBeenCalledTimes(1)
    vi.clearAllMocks()
    vi.mocked(API.createCategory).mockResolvedValue(failed('exists'))
    expect(await store.createCategory(cat('A'))).toEqual({ success: false, message: 'exists' })
    expect(API.listCategories).not.toHaveBeenCalled()
  })

  it('updateCategoryDescription passes name+description and refreshes', async () => {
    vi.mocked(API.updateCategoryDescription).mockResolvedValue(ok)
    vi.mocked(API.listCategories).mockResolvedValue([cat('A')])
    await store.updateCategoryDescription('A', 'new desc')
    expect(API.updateCategoryDescription).toHaveBeenCalledWith('A', 'new desc')
  })

  it('deleteCategory refreshes on success', async () => {
    vi.mocked(API.deleteCategory).mockResolvedValue(ok)
    vi.mocked(API.listCategories).mockResolvedValue([])
    await store.deleteCategory('A')
    expect(store.categories).toEqual([])
  })
})
