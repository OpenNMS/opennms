import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useKscStore } from '@/stores/kscStore'
import API from '@/services'
import { KscReport } from '@/types/ksc'

vi.mock('@/services', () => ({
  default: {
    getKscReports: vi.fn(),
    getKscReport: vi.fn(),
    createKscReport: vi.fn(),
    updateKscReport: vi.fn(),
    deleteKscReport: vi.fn(),
    reloadKscConfig: vi.fn()
  }
}))

const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

const report = (over: Partial<KscReport> = {}): KscReport => ({
  id: 0,
  label: 'Test',
  show_timespan_button: null,
  show_graphtype_button: null,
  graphs_per_line: 1,
  kscGraph: [],
  ...over
})

describe('useKscStore', () => {
  let store: ReturnType<typeof useKscStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useKscStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('load', () => {
    it('populates reports on success', async () => {
      const reports = [report(), report({ id: 1, label: 'Second' })]
      vi.mocked(API.getKscReports).mockResolvedValue(reports)

      await store.load()

      expect(store.reports).toEqual(reports)
      expect(showSnackBar).not.toHaveBeenCalled()
      expect(store.loading).toBe(false)
    })

    it('surfaces an error instead of leaving a silent empty list', async () => {
      vi.mocked(API.getKscReports).mockRejectedValue(new Error('boom'))

      await store.load()

      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(store.reports).toEqual([])
      expect(store.loading).toBe(false)
    })
  })

  describe('saveReport', () => {
    it('creates when the id is null and reloads', async () => {
      vi.mocked(API.createKscReport).mockResolvedValue(7)
      vi.mocked(API.getKscReports).mockResolvedValue([])

      const ok = await store.saveReport(report({ id: null, label: 'New' }))

      expect(ok).toBe(true)
      expect(API.createKscReport).toHaveBeenCalledTimes(1)
      expect(API.updateKscReport).not.toHaveBeenCalled()
      expect(API.getKscReports).toHaveBeenCalledTimes(1)
      expect(showSnackBar).toHaveBeenCalledWith(expect.not.objectContaining({ error: true }))
    })

    it('updates when the id is present, passing id and body', async () => {
      vi.mocked(API.updateKscReport).mockResolvedValue()
      vi.mocked(API.getKscReports).mockResolvedValue([])
      const r = report({ id: 3, label: 'Edited' })

      const ok = await store.saveReport(r)

      expect(ok).toBe(true)
      expect(API.updateKscReport).toHaveBeenCalledWith(3, r)
      expect(API.createKscReport).not.toHaveBeenCalled()
    })

    it('returns false and surfaces the error on failure without reloading', async () => {
      vi.mocked(API.createKscReport).mockRejectedValue({ response: { data: 'Bad rule' }})

      const ok = await store.saveReport(report({ id: null }))

      expect(ok).toBe(false)
      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true, msg: 'Bad rule' }))
      expect(API.getKscReports).not.toHaveBeenCalled()
    })
  })

  describe('removeReport', () => {
    it('refuses to delete a report with no id', async () => {
      const ok = await store.removeReport(report({ id: null }))

      expect(ok).toBe(false)
      expect(API.deleteKscReport).not.toHaveBeenCalled()
    })

    it('deletes and reloads on success', async () => {
      vi.mocked(API.deleteKscReport).mockResolvedValue()
      vi.mocked(API.getKscReports).mockResolvedValue([])

      const ok = await store.removeReport(report({ id: 4 }))

      expect(ok).toBe(true)
      expect(API.deleteKscReport).toHaveBeenCalledWith(4)
      expect(API.getKscReports).toHaveBeenCalledTimes(1)
    })

    it('returns false and surfaces the error on failure', async () => {
      vi.mocked(API.deleteKscReport).mockRejectedValue(new Error('nope'))

      const ok = await store.removeReport(report({ id: 4 }))

      expect(ok).toBe(false)
      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })
  })

  describe('reloadConfig', () => {
    it('reloads config then refreshes the list', async () => {
      vi.mocked(API.reloadKscConfig).mockResolvedValue()
      vi.mocked(API.getKscReports).mockResolvedValue([])

      await store.reloadConfig()

      expect(API.reloadKscConfig).toHaveBeenCalledTimes(1)
      expect(API.getKscReports).toHaveBeenCalledTimes(1)
    })

    it('surfaces an error and does not refresh when reload fails', async () => {
      vi.mocked(API.reloadKscConfig).mockRejectedValue(new Error('io'))

      await store.reloadConfig()

      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(API.getKscReports).not.toHaveBeenCalled()
    })
  })

  describe('getReport', () => {
    it('delegates to the service', async () => {
      const r = report({ id: 9 })
      vi.mocked(API.getKscReport).mockResolvedValue(r)

      const result = await store.getReport(9)

      expect(result).toEqual(r)
      expect(API.getKscReport).toHaveBeenCalledWith(9)
    })
  })
})
