import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import {
  addNodeToCategory,
  createCategory,
  deleteCategory,
  listCategories
} from '@/services/categoryAdminService'
import { rest } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
  v2: { get: vi.fn() }
}))

const http = (status: number, data: any = '') => {
  const e = new AxiosError('x')
  e.response = { status, data, statusText: '', headers: {}, config: { headers: new AxiosHeaders() }}
  return e
}

describe('categoryAdminService', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  describe('listCategories', () => {
    it('returns [] on 204 and normalizes a single object to an array', async () => {
      vi.mocked(rest.get).mockResolvedValueOnce({ status: 204, data: '' } as any)
      expect(await listCategories()).toEqual([])
      vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: { category: { name: 'Routers' }}} as any)
      expect(await listCategories()).toEqual([{ name: 'Routers' }])
    })
    it('returns null (not []) on failure so the store can flag a load error', async () => {
      vi.mocked(rest.get).mockRejectedValueOnce(http(500))
      expect(await listCategories()).toBeNull()
    })
  })

  describe('deleteCategory', () => {
    it('surfaces the server reason when the category is unknown', async () => {
      vi.mocked(rest.delete).mockRejectedValueOnce(http(400, 'A category with name \'gone\' does not exist.'))
      const result = await deleteCategory('gone')
      expect(result.success).toBe(false)
      expect(result.message).toContain('does not exist')
    })
    it('does not surface an HTML error page verbatim', async () => {
      vi.mocked(rest.delete).mockRejectedValueOnce(http(500, '<html>Internal Server Error</html>'))
      const result = await deleteCategory('Routers')
      expect(result.success).toBe(false)
      expect(result.message).not.toContain('<html>')
      expect(result.message).toContain('Routers')
    })
  })

  describe('createCategory', () => {
    it('returns a success result and keeps a short server message on failure', async () => {
      vi.mocked(rest.post).mockResolvedValueOnce({} as any)
      expect((await createCategory({ name: 'Routers' })).success).toBe(true)
      vi.mocked(rest.post).mockRejectedValueOnce(http(400, 'Category already exists'))
      expect(await createCategory({ name: 'Routers' })).toEqual({ success: false, message: 'Category already exists' })
    })
  })

  describe('addNodeToCategory', () => {
    it('declares the XML content type the endpoint consumes', async () => {
      vi.mocked(rest.put).mockResolvedValueOnce({} as any)
      expect(await addNodeToCategory('Routers', 7)).toBe(true)
      expect(vi.mocked(rest.put).mock.calls.at(-1)?.[2]).toMatchObject({ headers: { 'Content-Type': 'application/xml' }})
    })
  })
})
