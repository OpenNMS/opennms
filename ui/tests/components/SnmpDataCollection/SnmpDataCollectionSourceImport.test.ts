import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollection/SnmpDataCollectionSourceImport.vue'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockUploadDataCollectionFiles = vi.fn()
const mockGetAllSnmpCollectionProfiles = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  uploadDataCollectionFiles: (...args: any[]) => mockUploadDataCollectionFiles(...args),
  getAllSnmpCollectionProfiles: (...args: any[]) => mockGetAllSnmpCollectionProfiles(...args),
  // The component's onMounted calls store.fetchAllSourcesNames (before the test
  // overrides it), so this service function must resolve cleanly.
  getAllSnmpCollectionSourcesNamesAndIds: vi.fn().mockResolvedValue([])
}))

vi.mock('@/components/SnmpDataCollection/snmpDataCollectionSourceXmlValidator', () => ({
  validateSnmpDataCollectionSourceFile: vi.fn(),
  isDuplicateFile: vi.fn().mockReturnValue(false)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

const stubs = {
  DataCollectionFilesUploadReportDialog: {
    name: 'DataCollectionFilesUploadReportDialog',
    template: '<div class="upload-report-dialog-stub"></div>',
    props: ['report', 'dialogVisible'],
    emits: ['close', 'view']
  },
  UploadedFileRenameDialog: {
    name: 'UploadedFileRenameDialog',
    template: '<div class="rename-dialog-stub"></div>',
    props: ['visible', 'fileBucket', 'index', 'alreadyExistsNames'],
    emits: ['close', 'rename', 'overwrite']
  }
}

// Builds a source-file record with sensible defaults so individual tests only
// declare the fields they care about.
const makeFile = (
  name: string,
  overrides: Partial<UploadSnmpDataCollectionFileType> = {}
): UploadSnmpDataCollectionFileType => ({
  file: new File(['<x/>'], name, { type: 'text/xml' }),
  isValid: true,
  errors: [],
  isDuplicate: false,
  kind: 'group',
  groupName: name.replace(/\.xml$/, ''),
  ...overrides
})

describe('SnmpDataCollectionSourceImport.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionStore>

  const mountImport = () => mount(SnmpDataCollectionSourceImport, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    mockGetAllSnmpCollectionProfiles.mockResolvedValue([
      { id: 1, name: 'profileA', enabled: true, sourceNames: [] },
      { id: 2, name: 'profileB', enabled: true, sourceNames: [] }
    ])

    wrapper = mountImport()
    store = useSnmpDataCollectionStore()
    store.uploadedSourceNames = []
    store.fetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)
    store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial Rendering', () => {
    it('renders the header controls and child dialogs', () => {
      expect(wrapper.find('.data-collection-source-import-container').exists()).toBe(true)
      expect(wrapper.find('[data-test="choose-file-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="choose-folder-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="upload-button"]').exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'UploadedFileRenameDialog' }).exists()).toBe(true)
    })

    it('shows the empty list when no files are queued', () => {
      expect(wrapper.find('[data-test="import-files-table"]').exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
    })

    it('renders the available profile checkboxes', () => {
      expect(wrapper.find('[data-test="profile-checkbox-profileA"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="profile-checkbox-profileB"]').exists()).toBe(true)
    })
  })

  describe('Table rendering', () => {
    it('renders a row per queued file', async () => {
      wrapper.vm.sourceFiles = [makeFile('a.xml'), makeFile('b.xml')]
      await nextTick()
      expect(wrapper.find('[data-test="import-files-table"]').exists()).toBe(true)
      expect(wrapper.findAll('tbody tr').length).toBe(2)
    })

    it('orders new sources before update (duplicate) rows', async () => {
      wrapper.vm.sourceFiles = [
        makeFile('update.xml', { isDuplicate: true }),
        makeFile('new.xml', { isDuplicate: false })
      ]
      await nextTick()
      const ordered = wrapper.vm.orderedSourceFiles.map((f: UploadSnmpDataCollectionFileType) => f.file.name)
      expect(ordered).toEqual(['new.xml', 'update.xml'])
    })

    it('renders a Source kind tag for group files', async () => {
      wrapper.vm.sourceFiles = [makeFile('a.xml', { kind: 'group' })]
      await nextTick()
      expect(wrapper.find('[data-test="kind-chip-a.xml"]').text()).toBe('Source')
    })

    it('renders a config kind tag with count for config files', async () => {
      wrapper.vm.sourceFiles = [makeFile('cfg.xml', { kind: 'config', groupName: undefined, profileNames: ['p1', 'p2'] } as any)]
      await nextTick()
      expect(wrapper.find('[data-test="kind-chip-cfg.xml"]').text()).toContain('Profiles config (2)')
    })

    it('renders an update tag for duplicate files', async () => {
      wrapper.vm.sourceFiles = [makeFile('dup.xml', { isDuplicate: true, groupName: 'dup' })]
      await nextTick()
      expect(wrapper.find('[data-test="update-chip-dup.xml"]').text()).toContain('Will update existing source \'dup\'')
    })
  })

  describe('Profile selection', () => {
    it('adds and removes profile names via toggleProfile', () => {
      wrapper.vm.toggleProfile('profileA', true)
      expect(wrapper.vm.selectedProfileNames).toContain('profileA')
      wrapper.vm.toggleProfile('profileA', true)
      expect(wrapper.vm.selectedProfileNames.filter((n: string) => n === 'profileA').length).toBe(1)
      wrapper.vm.toggleProfile('profileA', false)
      expect(wrapper.vm.selectedProfileNames).not.toContain('profileA')
    })

    it('flags profile selection as required for a new source without a config file', async () => {
      wrapper.vm.sourceFiles = [makeFile('new.xml', { kind: 'group', isDuplicate: false })]
      await nextTick()
      expect(wrapper.vm.profileSelectionIsRequired).toBe(true)
    })

    it('does not require profile selection when a config file is present', async () => {
      wrapper.vm.sourceFiles = [
        makeFile('new.xml', { kind: 'group', isDuplicate: false }),
        makeFile('cfg.xml', { kind: 'config', groupName: undefined } as any)
      ]
      await nextTick()
      expect(wrapper.vm.hasConfigFile).toBe(true)
      expect(wrapper.vm.profileSelectionIsRequired).toBe(false)
    })
  })

  describe('Upload button enablement', () => {
    it('is disabled when no files are queued', () => {
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('is disabled when a new source has no profile selected', async () => {
      wrapper.vm.sourceFiles = [makeFile('new.xml', { kind: 'group', isDuplicate: false })]
      wrapper.vm.selectedProfileNames = []
      await nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('is enabled for a valid new source once a profile is selected', async () => {
      wrapper.vm.sourceFiles = [makeFile('new.xml', { kind: 'group', isDuplicate: false })]
      wrapper.vm.selectedProfileNames = ['profileA']
      await nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('is enabled for an update-only batch without a profile', async () => {
      wrapper.vm.sourceFiles = [makeFile('dup.xml', { kind: 'group', isDuplicate: true })]
      wrapper.vm.selectedProfileNames = []
      await nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('is disabled when any queued file is invalid', async () => {
      wrapper.vm.sourceFiles = [makeFile('dup.xml', { kind: 'group', isDuplicate: true, isValid: false })]
      await nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })
  })

  describe('Remove file', () => {
    it('removes a file by reference regardless of display order', async () => {
      const updateFile = makeFile('update.xml', { isDuplicate: true })
      const newFile = makeFile('new.xml', { isDuplicate: false })
      wrapper.vm.sourceFiles = [updateFile, newFile]
      await nextTick()

      // ordered list shows new.xml first, but removing it should drop the
      // matching source-of-truth entry, not the first display row. Pass the
      // reactive array entry (the template hands removeFile the reactive row).
      const newRow = wrapper.vm.sourceFiles.find((f: UploadSnmpDataCollectionFileType) => f.file.name === 'new.xml')
      wrapper.vm.removeFile(newRow)
      await nextTick()
      const remaining = wrapper.vm.sourceFiles.map((f: UploadSnmpDataCollectionFileType) => f.file.name)
      expect(remaining).toEqual(['update.xml'])
    })
  })

  describe('Upload', () => {
    it('uploads valid files with the selected profiles and clears the queue', async () => {
      mockUploadDataCollectionFiles.mockResolvedValue({ errors: [], success: ['new'] })
      wrapper.vm.sourceFiles = [makeFile('new.xml', { kind: 'group', isDuplicate: false })]
      wrapper.vm.selectedProfileNames = ['profileA']
      await nextTick()

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(mockUploadDataCollectionFiles).toHaveBeenCalledWith(
        expect.arrayContaining([expect.any(File)]),
        ['profileA']
      )
      expect(wrapper.vm.sourceFiles).toEqual([])
      expect(store.fetchSnmpCollectionSources).toHaveBeenCalled()
      expect(store.fetchAllSourcesNames).toHaveBeenCalled()
    })

    it('shows an error snackbar when the upload throws', async () => {
      // The component logs the caught error; suppress the expected noise.
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      mockUploadDataCollectionFiles.mockRejectedValue(new Error('boom'))
      wrapper.vm.sourceFiles = [makeFile('new.xml', { kind: 'group', isDuplicate: false })]
      await nextTick()

      await wrapper.vm.uploadFiles()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Rename / overwrite', () => {
    it('clears duplicate state on overwrite', async () => {
      wrapper.vm.sourceFiles = [makeFile('dup.xml', { isDuplicate: true })]
      await nextTick()
      wrapper.vm.openFileRenameDialog(0)
      wrapper.vm.overwriteFile()
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })
  })

  describe('Duplicate re-evaluation on uploadedSourceNames change', () => {
    it('marks files whose group name now exists as duplicates', async () => {
      wrapper.vm.sourceFiles = [makeFile('a.xml', { groupName: 'a', isDuplicate: false })]
      await nextTick()
      store.uploadedSourceNames = [{ id: 9, name: 'A' }] as any
      await flushPromises()
      await nextTick()
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })
  })
})
