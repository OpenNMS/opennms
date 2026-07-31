import EventConfigUploadFilesTab from '@/components/EventConfiguration/EventConfigUploadFilesTab.vue'
import {
  isDuplicateFile,
  validateEventConfigFile
} from '@/components/EventConfiguration/eventConfigXmlValidator'
import useSnackbar from '@/composables/useSnackbar'
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

vi.mock('@/stores/eventConfigStore')
vi.mock('@/composables/useSnackbar')
vi.mock('@/services/eventConfigService')
vi.mock('@/components/EventConfiguration/eventConfigXmlValidator', () => ({
  isDuplicateFile: vi.fn().mockReturnValue(false),
  validateEventConfigFile: vi.fn(),
  MAX_FILES_UPLOAD: 10
}))

// Stub Draggable (renders its #item slot per element) and the teleporting
// child dialogs so the suite focuses on the tab's own upload logic.
const DraggableStub = {
  name: 'Draggable',
  props: ['modelValue', 'itemKey', 'handle'],
  template: '<div class="draggable-stub"><template v-for="(element, index) in modelValue" :key="index"><slot name="item" :element="element" :index="index" /></template></div>'
}

const stubs = {
  Draggable: DraggableStub,
  EventConfigFilesUploadReportDialog: { name: 'EventConfigFilesUploadReportDialog', template: '<div class="report-dialog-stub"></div>', props: ['report'] },
  UploadedFileRenameDialog: { name: 'UploadedFileRenameDialog', template: '<div class="rename-dialog-stub"></div>', props: ['visible', 'fileBucket', 'index', 'alreadyExistsNames'], emits: ['close', 'rename', 'overwrite'] }
}

const makeFile = (name: string, opts: Partial<{ isValid: boolean; isDuplicate: boolean; errors: string[] }> = {}) => ({
  file: new File(['<x/>'], name, { type: 'text/xml' }),
  isValid: opts.isValid ?? true,
  errors: opts.errors ?? [],
  isDuplicate: opts.isDuplicate ?? false
})

describe('EventConfigUploadFilesTab.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any
  let snackbar: any

  const mountTab = () => mount(EventConfigUploadFilesTab, {
    global: {
      plugins: [PrimeVue],
      directives: { tooltip: Tooltip },
      stubs
    }
  })

  beforeEach(() => {
    vi.clearAllMocks()

    store = reactive({
      uploadedSources: [],
      uploadedEventConfigFilesReportDialogState: { visible: false }
    })
    vi.mocked(useEventConfigStore).mockReturnValue(store)

    snackbar = { showSnackBar: vi.fn() }
    vi.mocked(useSnackbar).mockReturnValue(snackbar)

    vi.mocked(isDuplicateFile).mockReturnValue(false)
    vi.mocked(validateEventConfigFile).mockResolvedValue({ isValid: true, errors: [] })
    vi.mocked(uploadEventConfigFiles).mockResolvedValue({ errors: [], success: [] } as any)

    wrapper = mountTab()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Initial Rendering', () => {
    it('renders the upload controls and child dialogs', () => {
      expect(wrapper.find('.upload-files-tab').exists()).toBe(true)
      expect(wrapper.find('[data-test="upload-button"]').exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'EventConfigFilesUploadReportDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'UploadedFileRenameDialog' }).exists()).toBe(true)
    })

    it('shows "No files selected" when the queue is empty', () => {
      expect(wrapper.text()).toContain('No files selected')
    })
  })

  describe('shouldUploadDisabled', () => {
    it('is disabled when there are no files', () => {
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('is enabled for a single valid, non-duplicate file', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml')]
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('is disabled when any file is invalid', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml', { isValid: false })]
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('is disabled when any file is a duplicate', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml', { isDuplicate: true })]
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })
  })

  describe('Remove file', () => {
    it('splices the file at the given index', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml'), makeFile('b.xml')]
      await wrapper.vm.$nextTick()
      wrapper.vm.removeFile(0)
      expect(wrapper.vm.eventFiles.map((f: any) => f.file.name)).toEqual(['b.xml'])
    })
  })

  describe('Rename / overwrite', () => {
    it('clears duplicate state on overwrite', async () => {
      wrapper.vm.eventFiles = [makeFile('dup.xml', { isDuplicate: true })]
      await wrapper.vm.$nextTick()
      wrapper.vm.openFileRenameDialog(0)
      wrapper.vm.overwriteFile()
      expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)
      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })

    it('replaces the file with a validated renamed copy', async () => {
      vi.mocked(validateEventConfigFile).mockResolvedValue({ isValid: true, errors: [] })
      wrapper.vm.eventFiles = [makeFile('dup.xml', { isDuplicate: true })]
      await wrapper.vm.$nextTick()
      wrapper.vm.openFileRenameDialog(0)
      await wrapper.vm.renameFile('renamed.xml')
      await flushPromises()
      expect(wrapper.vm.eventFiles[0].file.name).toBe('renamed.xml')
      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })
  })

  describe('Upload', () => {
    it('uploads valid files, opens the report dialog and clears the queue', async () => {
      vi.mocked(uploadEventConfigFiles).mockResolvedValue({ errors: [], success: ['a'] } as any)
      wrapper.vm.eventFiles = [makeFile('a.xml')]
      await wrapper.vm.$nextTick()

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadEventConfigFiles).toHaveBeenCalledWith(expect.arrayContaining([expect.any(File)]))
      expect(wrapper.vm.eventFiles).toEqual([])
      expect(store.uploadedEventConfigFilesReportDialogState.visible).toBe(true)
    })

    it('shows an error snackbar when the upload throws', async () => {
      // The component logs the caught error; suppress the expected noise.
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(uploadEventConfigFiles).mockRejectedValue(new Error('boom'))
      wrapper.vm.eventFiles = [makeFile('a.xml')]
      await wrapper.vm.$nextTick()

      await wrapper.vm.uploadFiles()
      await flushPromises()
      expect(snackbar.showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('does nothing when there are no files', async () => {
      // The component warns about the empty queue; suppress the expected noise.
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      await wrapper.vm.uploadFiles()
      expect(uploadEventConfigFiles).not.toHaveBeenCalled()
      expect(consoleWarnSpy).toHaveBeenCalled()
      consoleWarnSpy.mockRestore()
    })
  })

  describe('Duplicate re-evaluation on uploadedSources change', () => {
    it('marks files whose name now exists as duplicates', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml', { isDuplicate: false })]
      await wrapper.vm.$nextTick()
      store.uploadedSources = [{ id: 1, name: 'a.xml' }]
      await flushPromises()
      expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true)
    })
  })
})
