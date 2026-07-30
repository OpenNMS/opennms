import UploadedFileRenameDialog from '@/components/EventConfiguration/Dialog/UploadedFileRenameDialog.vue'
import { UploadedSourceNamesResponse, UploadEventFileType } from '@/types/eventConfig'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import InputText from 'primevue/inputtext'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// Stub the teleporting PrimeVue Dialog so its slots render inline and can be
// queried directly; the real Checkbox/InputText/Button still render so wiring
// (disabled state, change handlers) is exercised.
const DialogStub = {
  name: 'Dialog',
  template: '<div class="dialog-stub" v-if="visible"><slot></slot><div class="dialog-footer"><slot name="footer"></slot></div></div>',
  props: ['visible', 'header', 'modal', 'draggable', 'closable'],
  emits: ['update:visible', 'hide']
}

describe('UploadedFileRenameDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let mockFileBucket: UploadEventFileType[]
  let mockAlreadyExistsNames: UploadedSourceNamesResponse[]

  const mountComponent = () => mount(UploadedFileRenameDialog, {
    props: {
      visible: false,
      fileBucket: mockFileBucket,
      alreadyExistsNames: mockAlreadyExistsNames,
      index: 0
    },
    global: {
      plugins: [PrimeVue],
      stubs: { Dialog: DialogStub }
    }
  })

  const saveButton = () => wrapper.findAllComponents(Button).find(b => b.props('label') === 'Save Changes')
  const cancelButton = () => wrapper.findAllComponents(Button).find(b => b.props('label') === 'Cancel')

  beforeEach(async () => {
    vi.clearAllMocks()

    mockFileBucket = [
      { file: new File(['content'], 'test-file.xml', { type: 'text/xml' }), isValid: true, errors: [], isDuplicate: false },
      { file: new File(['content'], 'another-file.xml', { type: 'text/xml' }), isValid: true, errors: [], isDuplicate: false }
    ] as any

    mockAlreadyExistsNames = [
      { id: 1, name: 'existing-file.xml' },
      { id: 2, name: 'system-file.xml' }
    ] as any

    wrapper = mountComponent()
    await flushPromises()
    await nextTick()

    // Open the dialog to trigger the watcher
    await wrapper.setProps({ visible: true })
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Initial Rendering', () => {
    it('renders the component', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('uses the dialog title "Rename Uploaded File"', () => {
      expect(wrapper.vm.labels.title).toBe('Rename Uploaded File')
    })

    it('sets the original file name from the bucket when opened', () => {
      expect(wrapper.vm.originalFileName).toBe('test-file.xml')
    })

    it('displays the instruction text', () => {
      expect(wrapper.html()).toContain('Choose one of the following options:')
    })

    it('renders two checkbox options', () => {
      expect(wrapper.findAllComponents(Checkbox)).toHaveLength(2)
    })

    it('renders the option labels', () => {
      expect(wrapper.html()).toContain('Keep Original File Name')
      expect(wrapper.html()).toContain('Overwrite Existing File')
      expect(wrapper.html()).toContain('Rename Uploaded File to:')
    })

    it('does not show the input field until rename is selected', () => {
      expect(wrapper.findComponent(InputText).exists()).toBe(false)
    })

    it('renders Cancel and Save Changes buttons', () => {
      expect(saveButton()).toBeDefined()
      expect(cancelButton()).toBeDefined()
    })

    it('disables Save Changes initially', () => {
      expect((saveButton()?.element as HTMLButtonElement).disabled).toBe(true)
    })
  })

  describe('Dialog Visibility', () => {
    it('updates dialogVisible when the visible prop changes', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()
      expect(wrapper.vm.dialogVisible).toBe(false)
    })

    it('resets state when the dialog becomes hidden', async () => {
      wrapper.vm.renameFile = true
      wrapper.vm.newFileName = 'test.xml'
      wrapper.vm.error = 'some error'

      await wrapper.setProps({ visible: false })
      await nextTick()

      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.newFileName).toBe('')
      expect(wrapper.vm.error).toBeUndefined()
    })
  })

  describe('Checkbox Interactions', () => {
    it('checks overwrite and disables rename', async () => {
      const checkboxes = wrapper.findAllComponents(Checkbox)
      await checkboxes[0].vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.overwriteFile).toBe(true)
      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.shouldRemainDisabled).toBe(false)
    })

    it('checks rename and shows the input field', async () => {
      const checkboxes = wrapper.findAllComponents(Checkbox)
      await checkboxes[1].vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.renameFile).toBe(true)
      expect(wrapper.vm.overwriteFile).toBe(false)
      expect(wrapper.findComponent(InputText).exists()).toBe(true)
    })

    it('treats undefined checkbox value as false', async () => {
      const checkboxes = wrapper.findAllComponents(Checkbox)
      await checkboxes[0].vm.$emit('update:modelValue', undefined)
      await nextTick()
      expect(wrapper.vm.overwriteFile).toBe(false)
    })
  })

  describe('Input Field', () => {
    beforeEach(async () => {
      wrapper.vm.renameFile = true
      await nextTick()
    })

    it('shows the input field with the New File Name label', () => {
      expect(wrapper.findComponent(InputText).exists()).toBe(true)
      expect(wrapper.html()).toContain('New File Name')
    })

    it('has the placeholder text', () => {
      expect(wrapper.findComponent(InputText).attributes('placeholder'))
        .toBe('Enter new file name (must end with .xml)')
    })

    it('updates and trims newFileName via onChangeFileName', async () => {
      wrapper.vm.onChangeFileName('  new-name.xml  ')
      await nextTick()
      expect(wrapper.vm.newFileName).toBe('new-name.xml')
    })

    it('marks the input invalid when there is an error', async () => {
      wrapper.vm.newFileName = ''
      wrapper.vm.validateName()
      await nextTick()
      expect(wrapper.findComponent(InputText).props('invalid')).toBe(true)
      expect(wrapper.text()).toContain('File name cannot be empty.')
    })
  })

  describe('File Name Validation', () => {
    it.each([
      ['', false, 'File name cannot be empty.'],
      ['test-file.txt', false, 'File name must end with .xml'],
      ['another-file.xml', false, 'A file with this name already exists in the current upload list.'],
      ['existing-file.xml', false, 'A file with this name already exists in the system.'],
      ['unique-file.xml', true, undefined]
    ])('validates "%s"', (fileName, expectedValid, expectedError) => {
      wrapper.vm.newFileName = fileName
      expect(wrapper.vm.validateName()).toBe(expectedValid)
      expect(wrapper.vm.error).toBe(expectedError)
    })

    it('rejects a name equal to the original', () => {
      wrapper.vm.originalFileName = 'test-file.xml'
      wrapper.vm.newFileName = 'test-file.xml'
      expect(wrapper.vm.validateName()).toBe(false)
      expect(wrapper.vm.error).toBe('New file name must be different from the original name.')
    })

    it('is case-insensitive against the upload list', () => {
      wrapper.vm.newFileName = 'ANOTHER-FILE.xml'
      expect(wrapper.vm.validateName()).toBe(false)
      expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
    })
  })

  describe('shouldRemainDisabled', () => {
    it.each([
      ['both unchecked', false, false, undefined, true],
      ['rename with error', true, false, 'err', true],
      ['rename without error', true, false, undefined, false],
      ['overwrite checked', false, true, undefined, false]
    ])('%s', (_desc, renameFile, overwriteFile, error, expected) => {
      wrapper.vm.renameFile = renameFile
      wrapper.vm.overwriteFile = overwriteFile
      wrapper.vm.error = error
      expect(wrapper.vm.shouldRemainDisabled).toBe(expected)
    })
  })

  describe('Save / Cancel', () => {
    it('emits overwrite when overwrite is selected and Save is clicked', async () => {
      wrapper.vm.overwriteFile = true
      await nextTick()
      await saveButton()?.trigger('click')
      expect(wrapper.emitted('overwrite')).toBeTruthy()
      expect(wrapper.emitted('rename')).toBeFalsy()
    })

    it('emits rename with the new name when valid', async () => {
      wrapper.vm.renameFile = true
      wrapper.vm.newFileName = 'brand-new-file.xml'
      wrapper.vm.validateName()
      await nextTick()
      await saveButton()?.trigger('click')
      expect(wrapper.emitted('rename')?.[0]).toEqual(['brand-new-file.xml'])
    })

    it('does not emit rename when the name is invalid', () => {
      wrapper.vm.renameFile = true
      wrapper.vm.newFileName = ''
      wrapper.vm.saveChanges()
      expect(wrapper.emitted('rename')).toBeFalsy()
    })

    it('does not emit rename when index is out of bounds', async () => {
      await wrapper.setProps({ index: -1 })
      wrapper.vm.renameFile = true
      wrapper.vm.newFileName = 'valid-name.xml'
      wrapper.vm.saveChanges()
      expect(wrapper.emitted('rename')).toBeFalsy()
    })

    it('emits close and resets state when Cancel is clicked', async () => {
      wrapper.vm.renameFile = true
      wrapper.vm.newFileName = 'test.xml'
      await nextTick()
      await cancelButton()?.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.newFileName).toBe('')
    })

    it('emits close only once on cancel (hide guard)', async () => {
      await cancelButton()?.trigger('click')
      expect(wrapper.emitted('close')).toHaveLength(1)
    })
  })

  describe('Change Handlers', () => {
    it('onChangeRenameFile unchecks overwrite', () => {
      wrapper.vm.overwriteFile = true
      wrapper.vm.onChangeRenameFile(true)
      expect(wrapper.vm.renameFile).toBe(true)
      expect(wrapper.vm.overwriteFile).toBe(false)
    })

    it('onChangeOverwriteFile unchecks rename, copies original name, clears error', () => {
      wrapper.vm.originalFileName = 'original.xml'
      wrapper.vm.newFileName = 'different.xml'
      wrapper.vm.error = 'some error'
      wrapper.vm.renameFile = true
      wrapper.vm.onChangeOverwriteFile(true)
      expect(wrapper.vm.overwriteFile).toBe(true)
      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.newFileName).toBe('original.xml')
      expect(wrapper.vm.error).toBeUndefined()
    })
  })

  describe('Watcher', () => {
    it('loads a different file on reopen', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()
      await wrapper.setProps({ visible: true, index: 1 })
      await nextTick()
      expect(wrapper.vm.originalFileName).toBe('another-file.xml')
      expect(wrapper.vm.newFileName).toBe('another-file.xml')
    })

    it('resets state when index is invalid on open', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()
      await wrapper.setProps({ visible: true, index: -1 })
      await nextTick()
      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.overwriteFile).toBe(false)
      expect(wrapper.vm.newFileName).toBe('')
      expect(wrapper.vm.originalFileName).toBe('')
    })

    it('handles an empty file bucket', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()
      await wrapper.setProps({ fileBucket: [], index: 0, visible: true })
      await nextTick()
      expect(wrapper.vm.originalFileName).toBe('')
    })
  })

  describe('Accessibility / data-test hooks', () => {
    it('exposes the save-button data-test hook', () => {
      expect(wrapper.html()).toContain('data-test="save-button"')
    })

    it('exposes the file-name data-test hook when renaming', async () => {
      wrapper.vm.renameFile = true
      await nextTick()
      expect(wrapper.html()).toContain('data-test="file-name"')
    })
  })
})
