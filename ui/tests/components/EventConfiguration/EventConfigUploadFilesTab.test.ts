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
import { OnmsTooltip } from '@opennms/onms-ui'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

vi.mock('@/stores/eventConfigStore')
vi.mock('@/composables/useSnackbar')
vi.mock('@/services/eventConfigService')
vi.mock('@/components/EventConfiguration/eventConfigXmlValidator', () => ({
  isDuplicateFile: vi.fn().mockReturnValue(false),
  validateEventConfigFile: vi.fn()
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
  OnmsMenu: { name: 'OnmsMenu', props: ['items'], template: '<div class="menu-stub"></div>' },
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
      directives: { 'onms-tooltip': OnmsTooltip },
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
      expect(wrapper.findComponent({ name: 'EventConfigFilesUploadReportDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'UploadedFileRenameDialog' }).exists()).toBe(true)
      // the commit bar (with the Upload button) exists only once something is queued
      expect(wrapper.find('[data-test="commit-bar"]').exists()).toBe(false)
    })

    it('a filled queue keeps an add-more path in view', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml')]
      await wrapper.vm.$nextTick()
      expect(wrapper.find('[data-test="add-more-row"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-more-files"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-more-folder"]').exists()).toBe(true)
    })

    it('invites a drag or browse when the queue is empty', () => {
      expect(wrapper.text()).toContain('Drag event files or a folder here')
      expect(wrapper.find('[data-test="browse-files-link"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="browse-folder-link"]').exists()).toBe(true)
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

  describe('Folder upload', () => {
    it('flags files matching an existing source for rename-or-replace instead of skipping them', async () => {
      store.uploadedSources = [{ name: 'Existing.events' }]
      await wrapper.vm.$nextTick()
      const existing = new File(['<x/>'], 'Existing.events.xml', { type: 'text/xml' })
      const fresh = new File(['<x/>'], 'New.events.xml', { type: 'text/xml' })

      await wrapper.vm.handleFolderUpload({ target: { files: [existing, fresh], value: '' }})

      expect(wrapper.vm.eventFiles.map((f: any) => [f.file.name, f.isDuplicate])).toEqual([
        ['Existing.events.xml', true],
        ['New.events.xml', false]
      ])
    })

    it('replace-all confirms every flagged file at once and unblocks the upload', async () => {
      wrapper.vm.eventFiles = [
        makeFile('a.xml', { isDuplicate: true }),
        makeFile('b.xml', { isDuplicate: true }),
        makeFile('c.xml')
      ]
      await wrapper.vm.$nextTick()
      expect(wrapper.get('[data-test="replace-chip"]').text()).toContain('2 replace existing')
      expect(wrapper.get('[data-test="commit-headline"]').text()).toBe('Confirm 2 replacements to upload')
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)

      await wrapper.get('[data-test="replace-all-button"]').trigger('click')

      expect(wrapper.vm.eventFiles.every((f: any) => !f.isDuplicate)).toBe(true)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
      expect(wrapper.find('[data-test="replace-chip"]').exists()).toBe(false)
      expect(wrapper.get('[data-test="commit-headline"]').text()).toBe('Ready to upload')
      expect(wrapper.get('[data-test="commit-detail"]').text()).toBe('1 new source · 2 replaced')
    })

    it('the chips filter the queue to the rows that need attention', async () => {
      wrapper.vm.eventFiles = [
        makeFile('ok.xml'),
        makeFile('dup.xml', { isDuplicate: true }),
        makeFile('broken.xml', { isValid: false, errors: ['bad'] })
      ]
      await wrapper.vm.$nextTick()
      expect(wrapper.get('[data-test="commit-headline"]').text())
        .toBe('Fix 1 invalid file and confirm 1 replacement to upload')

      await wrapper.get('[data-test="invalid-chip"]').trigger('click')
      expect(wrapper.vm.queueView.map((f: any) => f.file.name)).toEqual(['broken.xml'])

      // the same chip toggles the filter back off
      await wrapper.get('[data-test="invalid-chip"]').trigger('click')
      expect(wrapper.vm.queueView).toHaveLength(3)
    })

    it('an eventconf.xml renders as the ordering callout, not a queue row', async () => {
      wrapper.vm.eventFiles = [
        { ...makeFile('eventconf.xml'), manifestEntries: 255 },
        makeFile('a.xml')
      ]
      await wrapper.vm.$nextTick()

      const callout = wrapper.get('[data-test="manifest-callout"]')
      expect(callout.text()).toContain('eventconf.xml')
      expect(callout.text()).toContain('sets the evaluation order (255 entries)')
      expect(wrapper.vm.queueView.map((f: any) => f.file.name)).toEqual(['a.xml'])
      expect(wrapper.get('[data-test="commit-detail"]').text()).toContain('evaluation order will be applied')
    })

    it('blocks uploads beyond the server attachment cap with the reason', async () => {
      wrapper.vm.eventFiles = Array.from({ length: 301 }, (_, i) => makeFile(`f${i}.xml`))
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
      expect(wrapper.get('[data-test="commit-headline"]').text())
        .toContain('limited to 300 files per request — remove 1')
    })

    it('clear queue empties everything and drops the commit bar', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml'), makeFile('b.xml')]
      await wrapper.vm.$nextTick()
      await wrapper.get('[data-test="clear-queue-button"]').trigger('click')
      expect(wrapper.vm.eventFiles).toHaveLength(0)
      expect(wrapper.find('[data-test="commit-bar"]').exists()).toBe(false)
    })

    it('the file picker appends to a large folder selection without a count limit', async () => {
      wrapper.vm.eventFiles = Array.from({ length: 12 }, (_, i) => makeFile(`folder-${i}.xml`))
      await wrapper.vm.$nextTick()

      const manifest = new File(['<x/>'], 'eventconf.xml', { type: 'text/xml' })
      await wrapper.vm.handleEventConfUpload({ target: { files: [manifest], value: '' }})

      expect(wrapper.vm.eventFiles).toHaveLength(13)
      expect(wrapper.vm.eventFiles[12].file.name).toBe('eventconf.xml')
      expect(snackbar.showSnackBar).not.toHaveBeenCalled()
    })

    it('a store refresh does not resurrect confirmations the user already gave', async () => {
      store.uploadedSources = [{ name: 'Existing.events' }]
      await wrapper.vm.$nextTick()
      const existing = new File(['<x/>'], 'Existing.events.xml', { type: 'text/xml' })
      await wrapper.vm.handleFolderUpload({ target: { files: [existing], value: '' }})
      expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true)

      await wrapper.get('[data-test="replace-all-button"]').trigger('click')
      expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)

      // a refresh replaces the uploadedSources array with the same names
      store.uploadedSources = [{ name: 'Existing.events' }, { name: 'Other.events' }]
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)
    })

    it('a drop feeds the same queue as the pickers, duplicate flags included', async () => {
      store.uploadedSources = [{ name: 'Existing.events' }]
      await wrapper.vm.$nextTick()
      const dropped = new File(['<x/>'], 'Existing.events.xml', { type: 'text/xml' })
      const readme = new File(['x'], 'readme.txt', { type: 'text/plain' })
      // no entry support: the traversal falls back to the plain file list
      const dataTransfer = { items: [], files: [dropped, readme] }

      await wrapper.vm.onDrop({ preventDefault: () => {}, dataTransfer })

      expect(wrapper.vm.eventFiles.map((f: any) => [f.file.name, f.isDuplicate])).toEqual([
        ['Existing.events.xml', true]
      ])
      expect(wrapper.vm.dragDepth).toBe(0)
    })

    it('upload stays disabled while a selection is still validating', async () => {
      let resolveValidation!: (value: { isValid: boolean; errors: string[] }) => void
      vi.mocked(validateEventConfigFile).mockImplementation(() => new Promise((resolve) => {
        resolveValidation = resolve
      }))
      const slow = new File(['<x/>'], 'slow.xml', { type: 'text/xml' })
      const pending = wrapper.vm.handleFolderUpload({ target: { files: [slow], value: '' }})
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)

      resolveValidation({ isValid: true, errors: [] })
      await pending
      expect(wrapper.vm.eventFiles).toHaveLength(1)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })
  })

  describe('Remove file', () => {
    it('removes the given entry from the queue', async () => {
      wrapper.vm.eventFiles = [makeFile('a.xml'), makeFile('b.xml')]
      await wrapper.vm.$nextTick()
      wrapper.vm.removeFileEntry(wrapper.vm.eventFiles[0])
      expect(wrapper.vm.eventFiles.map((f: any) => f.file.name)).toEqual(['b.xml'])
    })
  })

  describe('Per-file status controls', () => {
    it('opens the rename dialog from the duplicate-file button', async () => {
      wrapper.vm.eventFiles = [makeFile('dup.xml', { isDuplicate: true })]
      await wrapper.vm.$nextTick()

      const duplicate = wrapper.find('button.warning-icon')
      expect(duplicate.exists()).toBe(true)
      await duplicate.trigger('click')

      expect(wrapper.vm.displayRenameDialog).toBe(true)
      expect(wrapper.vm.selectedIndex).toBe(0)
    })

    it('the rename dialog targets the clicked row, not the first flagged one', async () => {
      wrapper.vm.eventFiles = [
        makeFile('first-dup.xml', { isDuplicate: true }),
        makeFile('ok.xml'),
        makeFile('clicked-dup.xml', { isDuplicate: true })
      ]
      await wrapper.vm.$nextTick()

      wrapper.vm.openFileRenameDialog(2)
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent({ name: 'UploadedFileRenameDialog' }).props('index')).toBe(2)
    })

    // Status only, so these stay plain icons rather than buttons that do nothing
    it('flags valid and invalid files with a tooltipped status icon', async () => {
      wrapper.vm.eventFiles = [makeFile('good.xml'), makeFile('bad.xml', { isValid: false, errors: ['Schema mismatch'] })]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('svg.success-icon').exists()).toBe(true)
      const invalid = wrapper.find('svg.error-icon')
      expect(invalid.exists()).toBe(true)
      expect(wrapper.find('button.success-icon').exists()).toBe(false)
      expect(wrapper.find('button.error-icon').exists()).toBe(false)

      // the tooltip carries the validation errors
      const tooltipValue = (invalid.element as never as Record<string, string>).$_ptooltipValue
      expect(tooltipValue).toContain('Schema mismatch')
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
