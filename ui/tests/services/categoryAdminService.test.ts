import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import {
  deleteCategory, listCategories
} from '@/services/categoryAdminService'
import { rest } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
  v2: { get: vi.fn() }
}))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar: vi.fn() }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

const http = (status: number, data: any = '') => {
  const e = new AxiosError('x')
  e.response = { status, data, statusText: '', headers: {}, config: { headers: new AxiosHeaders() } }
  return e
}

describe('categoryAdminService', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  describe('listCategories', () => {
    it('returns [] on 204 and normalizes a single object to an array', async () => {
      vi.mocked(rest.get).mockResolvedValueOnce({ status: 204, data: '' } as any)
      expect(await listCategories()).toEqual([])
      vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: { category: { name: 'Routers' } } } as any)
      expect(await listCategories()).toEqual([{ name: 'Routers' }])
    })
    it('returns null (not []) on failure so the store can flag a load error', async () => {
      vi.mocked(rest.get).mockRejectedValueOnce(http(500))
      expect(await listCategories()).toBeNull()
    })
  })

  describe('deleteCategory', () => {
    it('treats a 404 (already gone) as success', async () => {
      vi.mocked(rest.delete).mockRejectedValueOnce(http(404))
      expect(await deleteCategory('gone')).toBeNull()
    })
    it('does not surface an HTML error page verbatim', async () => {
      vi.mocked(rest.delete).mockRejectedValueOnce(http(500, '<html>Internal Server Error</html>'))
      const msg = await deleteCategory('Routers')
      expect(msg).not.toContain('<html>')
      expect(msg).toContain('Routers')
    })
  })
})
