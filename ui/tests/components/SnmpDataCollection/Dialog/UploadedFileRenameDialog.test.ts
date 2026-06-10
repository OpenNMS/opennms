import UploadedFileRenameDialog from '@/components/SnmpDataCollection/Dialog/UploadedFileRenameDialog.vue'
import { SnmpDataCollectionSourceNamesAndIds, UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox, FeatherCheckboxGroup } from '@featherds/checkbox'
import { FeatherInput } from '@featherds/input'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue', 'hideClose']
  }
}))

describe('UploadedFileRenameDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let mockFileBucket: UploadSnmpDataCollectionFileType[]
  let mockAlreadyExistsNames: SnmpDataCollectionSourceNamesAndIds[]

  beforeEach(async () => {
    vi.clearAllMocks()

    mockFileBucket = [
      {
        file: new File(['content'], 'test-file.xml', { type: 'text/xml' }),
        isValid: true,
        errors: [],
        isDuplicate: false
      },
      {
        file: new File(['content'], 'another-file.xml', { type: 'text/xml' }),
        isValid: true,
        errors: [],
        isDuplicate: false
      }
    ]

    mockAlreadyExistsNames = [
      { id: 1, name: 'existing-file.xml' },
      { id: 2, name: 'system-file.xml' }
    ]

    wrapper = mount(UploadedFileRenameDialog, {
      props: {
        visible: false,
        fileBucket: mockFileBucket,
        alreadyExistsNames: mockAlreadyExistsNames,
        index: 0
      },
      global: {
        components: {
          FeatherButton,
          FeatherCheckbox,
          FeatherCheckboxGroup,
          FeatherInput
        }
      }
    })

    await flushPromises()
    await nextTick()

    // Open the dialog to trigger the watcher
    await wrapper.setProps({ visible: true })
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial Rendering', () => {
    it('should render the component', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('should render FeatherDialog component', () => {
      expect(wrapper.findComponent({ name: 'FeatherDialog' }).exists()).toBe(true)
    })

    it('should have dialog title "Rename Uploaded File"', () => {
      expect(wrapper.vm.labels.title).toBe('Rename Uploaded File')
    })

    it('should display original file name in the message', () => {
      // originalFileName is set by the watcher when visible becomes true
      expect(wrapper.vm.originalFileName).toBe('test-file.xml')
    })

    it('should display instruction text', () => {
      expect(wrapper.html()).toContain('Choose one of the following options:')
    })

    it('should render FeatherCheckboxGroup component', () => {
      expect(wrapper.findComponent(FeatherCheckboxGroup).exists()).toBe(true)
    })

    it('should render two checkbox options', () => {
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
      expect(checkboxes.length).toBe(2)
    })

    it('should render overwrite checkbox with correct label', () => {
      expect(wrapper.html()).toContain('Keep Original File Name')
      expect(wrapper.html()).toContain('Overwrite Existing File')
    })

    it('should render rename checkbox with correct label', () => {
      expect(wrapper.html()).toContain('Rename Uploaded File to:')
    })

    it('should not show input field initially when rename is not selected', () => {
      expect(wrapper.findComponent(FeatherInput).exists()).toBe(false)
    })

    it('should render footer buttons', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
    })

    it('should render Cancel button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Cancel')
    })

    it('should render Save Changes button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].text()).toBe('Save Changes')
    })

    it('should have primary prop on Save Changes button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].props('primary')).toBe(true)
    })

    it('should have Save Changes button disabled initially', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].props('disabled')).toBe(true)
    })
  })

  describe('Dialog Visibility', () => {
    it('should show dialog when visible prop is true', () => {
      expect(wrapper.props('visible')).toBe(true)
    })

    it('should update dialogVisible when visible prop changes', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()
      expect(wrapper.vm.dialogVisible).toBe(false)
    })

    it('should set original file name when dialog becomes visible', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()

      await wrapper.setProps({ visible: true })
      await nextTick()

      expect(wrapper.vm.originalFileName).toBe('test-file.xml')
    })

    it('should reset state when dialog becomes hidden', async () => {
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
    describe('Overwrite Checkbox', () => {
      it('should enable overwrite option when checked', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[0].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.vm.overwriteFile).toBe(true)
      })

      it('should disable rename option when overwrite is checked', async () => {
        wrapper.vm.renameFile = true
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[0].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.vm.renameFile).toBe(false)
      })

      it('should clear error when overwrite is selected', async () => {
        wrapper.vm.error = 'some error'
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[0].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.vm.error).toBeUndefined()
      })

      it('should enable Save Changes button when overwrite is checked', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[0].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.vm.shouldRemainDisabled).toBe(false)
      })
    })

    describe('Rename Checkbox', () => {
      it('should enable rename option when checked', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[1].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.vm.renameFile).toBe(true)
      })

      it('should disable overwrite option when rename is checked', async () => {
        wrapper.vm.overwriteFile = true
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[1].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.vm.overwriteFile).toBe(false)
      })

      it('should show input field when rename is selected', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[1].vm.$emit('update:modelValue', true)
        await nextTick()

        expect(wrapper.findComponent(FeatherInput).exists()).toBe(true)
      })

      it('should keep Save Changes button disabled when rename is checked without valid name', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[1].vm.$emit('update:modelValue', true)
        await nextTick()

        wrapper.vm.newFileName = ''
        wrapper.vm.validateName()
        expect(wrapper.vm.shouldRemainDisabled).toBe(true)
      })
    })

    describe('Checkbox Toggle Behavior', () => {
      it('should handle unchecking overwrite checkbox', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[0].vm.$emit('update:modelValue', true)
        await nextTick()
        await checkboxes[0].vm.$emit('update:modelValue', false)
        await nextTick()

        expect(wrapper.vm.overwriteFile).toBe(false)
      })

      it('should handle unchecking rename checkbox', async () => {
        const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
        await checkboxes[1].vm.$emit('update:modelValue', true)
        await nextTick()
        await checkboxes[1].vm.$emit('update:modelValue', false)
        await nextTick()

        expect(wrapper.vm.renameFile).toBe(false)
      })
    })
  })

  describe('File Name Validation', () => {
    describe('Empty File Name', () => {
      it('should show error when file name is empty', () => {
        wrapper.vm.newFileName = ''
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('File name cannot be empty.')
      })
    })

    describe('File Extension Validation', () => {
      it('should show error when file name does not end with .xml', () => {
        wrapper.vm.newFileName = 'test-file.txt'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('File name must end with .xml')
      })

      it('should accept file name ending with .xml', () => {
        wrapper.vm.newFileName = 'new-file.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(true)
        expect(wrapper.vm.error).toBeUndefined()
      })
    })

    describe('Same Name Validation', () => {
      it('should show error when new name is same as original', () => {
        wrapper.vm.originalFileName = 'test-file.xml'
        wrapper.vm.newFileName = 'test-file.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('New file name must be different from the original name.')
      })
    })

    describe('Duplicate in Upload List', () => {
      it('should show error when file name exists in upload list', () => {
        wrapper.vm.newFileName = 'another-file.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
      })

      it('should be case-insensitive when checking upload list', () => {
        wrapper.vm.newFileName = 'ANOTHER-FILE.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
      })

      it('should handle trimmed names when checking upload list', () => {
        // onChangeFileName already trims the value before validation
        wrapper.vm.onChangeFileName('  another-file.xml  ')

        expect(wrapper.vm.newFileName).toBe('another-file.xml')
        expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
      })
    })

    describe('Duplicate in System', () => {
      it('should show error when file name exists in system', () => {
        wrapper.vm.newFileName = 'existing-file.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('A file with this name already exists in the system.')
      })

      it('should be case-insensitive when checking system files', () => {
        wrapper.vm.newFileName = 'EXISTING-FILE.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('A file with this name already exists in the system.')
      })

      it('should handle names without .xml extension in system check', () => {
        wrapper.vm.newFileName = 'system-file.xml'
        const isValid = wrapper.vm.validateName()

        expect(isValid).toBe(false)
        expect(wrapper.vm.error).toBe('A file with this name already exists in the system.')
      })
    })

    describe('Parametrized Validation Tests', () => {
      const validationScenarios = [
        {
          description: 'empty string',
          fileName: '',
          expectedValid: false,
          expectedError: 'File name cannot be empty.'
        },
        {
          description: 'file without extension',
          fileName: 'test-file',
          expectedValid: false,
          expectedError: 'File name must end with .xml'
        },
        {
          description: 'file with wrong extension',
          fileName: 'test-file.json',
          expectedValid: false,
          expectedError: 'File name must end with .xml'
        },
        {
          description: 'valid unique file name',
          fileName: 'unique-file.xml',
          expectedValid: true,
          expectedError: undefined
        },
        {
          description: 'file name with special characters',
          fileName: 'test-file_123.xml',
          expectedValid: true,
          expectedError: undefined
        },
        {
          description: 'file name with hyphens',
          fileName: 'test-file-name.xml',
          expectedValid: true,
          expectedError: undefined
        }
      ]

      it.each(validationScenarios)(
        'should validate $description correctly',
        ({ fileName, expectedValid, expectedError }) => {
          wrapper.vm.newFileName = fileName
          const isValid = wrapper.vm.validateName()

          expect(isValid).toBe(expectedValid)
          expect(wrapper.vm.error).toBe(expectedError)
        }
      )
    })
  })

  describe('Input Field Interaction', () => {
    beforeEach(async () => {
      wrapper.vm.renameFile = true
      await nextTick()
    })

    it('should show input field when rename is selected', () => {
      expect(wrapper.findComponent(FeatherInput).exists()).toBe(true)
    })

    it('should have correct label for input field', () => {
      const input = wrapper.findComponent(FeatherInput)
      expect(input.props('label')).toBe('New File Name')
    })

    it('should have correct placeholder text', () => {
      const input = wrapper.findComponent(FeatherInput)
      expect(input.attributes('placeholder')).toBe('Enter new file name (must end with .events.xml)')
    })

    it('should update newFileName when input changes', async () => {
      wrapper.vm.onChangeFileName('new-name.xml')
      await nextTick()

      expect(wrapper.vm.newFileName).toBe('new-name.xml')
    })

    it('should trim whitespace from input', async () => {
      wrapper.vm.onChangeFileName('  new-name.xml  ')
      await nextTick()

      expect(wrapper.vm.newFileName).toBe('new-name.xml')
    })

    it('should validate name when input changes', async () => {
      wrapper.vm.onChangeFileName('new-valid-name.xml')
      await nextTick()

      expect(wrapper.vm.error).toBeUndefined()
    })

    it('should display error message in input field', async () => {
      wrapper.vm.newFileName = ''
      wrapper.vm.validateName()
      await nextTick()

      const input = wrapper.findComponent(FeatherInput)
      expect(input.props('error')).toBe('File name cannot be empty.')
    })

    it('should handle undefined value in onChangeFileName', () => {
      const originalName = wrapper.vm.newFileName
      wrapper.vm.onChangeFileName(undefined)
      // Should not throw error and should not update newFileName
      expect(wrapper.vm.newFileName).toBe(originalName)
    })

    it('should handle null value in onChangeFileName', () => {
      const originalName = wrapper.vm.newFileName
      wrapper.vm.onChangeFileName(null)
      // Should not throw error
      expect(wrapper.vm.newFileName).toBe(originalName)
    })
  })

  describe('Button Click Handlers', () => {
    describe('Cancel Button', () => {
      it('should emit close event when Cancel button is clicked', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')

        expect(wrapper.emitted('close')).toBeTruthy()
      })

      it('should reset state when Cancel button is clicked', async () => {
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'test.xml'
        wrapper.vm.error = 'error'

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')

        expect(wrapper.vm.renameFile).toBe(false)
        expect(wrapper.vm.newFileName).toBe('')
        expect(wrapper.vm.error).toBeUndefined()
      })
    })

    describe('Save Changes Button - Overwrite', () => {
      it('should emit overwrite event when overwrite is selected and Save is clicked', async () => {
        wrapper.vm.overwriteFile = true
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('overwrite')).toBeTruthy()
      })

      it('should not emit rename event when overwrite is selected', async () => {
        wrapper.vm.overwriteFile = true
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('rename')).toBeFalsy()
      })
    })

    describe('Save Changes Button - Rename', () => {
      it('should emit rename event with new file name when valid', async () => {
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'new-unique-name.xml'
        wrapper.vm.validateName()
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('rename')).toBeTruthy()
        expect(wrapper.emitted('rename')?.[0]).toEqual(['new-unique-name.xml'])
      })

      it('should not emit rename event when file name is invalid', async () => {
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = ''
        wrapper.vm.validateName()
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('rename')).toBeFalsy()
      })

      it('should not emit rename event when index is out of bounds', async () => {
        await wrapper.setProps({ index: -1 })
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'valid-name.xml'
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('rename')).toBeFalsy()
      })

      it('should not emit rename event when index exceeds fileBucket length', async () => {
        await wrapper.setProps({ index: 99 })
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'valid-name.xml'
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('rename')).toBeFalsy()
      })
    })

    describe('Save Button Disabled State', () => {
      it('should keep button disabled when no option is selected', () => {
        expect(wrapper.vm.shouldRemainDisabled).toBe(true)
      })

      it('should enable button when overwrite is selected', async () => {
        wrapper.vm.overwriteFile = true
        await nextTick()

        expect(wrapper.vm.shouldRemainDisabled).toBe(false)
      })

      it('should keep button disabled when rename is selected with error', async () => {
        wrapper.vm.renameFile = true
        wrapper.vm.error = 'some error'
        await nextTick()

        expect(wrapper.vm.shouldRemainDisabled).toBe(true)
      })

      it('should enable button when rename is selected with valid name', async () => {
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'valid-new-name.xml'
        wrapper.vm.validateName()
        await nextTick()

        expect(wrapper.vm.shouldRemainDisabled).toBe(false)
      })
    })
  })

  describe('Watcher Behavior', () => {
    it('should update dialogVisible when visible prop changes', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()

      expect(wrapper.vm.dialogVisible).toBe(false)

      await wrapper.setProps({ visible: true })
      await nextTick()

      expect(wrapper.vm.dialogVisible).toBe(true)
    })

    it('should set original file name from file bucket when dialog opens', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()

      await wrapper.setProps({
        visible: true,
        index: 1,
        fileBucket: mockFileBucket
      })
      await nextTick()

      expect(wrapper.vm.originalFileName).toBe('another-file.xml')
    })

    it('should set newFileName to originalFileName when dialog opens', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()

      await wrapper.setProps({ visible: true, index: 0 })
      await nextTick()

      expect(wrapper.vm.newFileName).toBe(wrapper.vm.originalFileName)
    })

    it('should clear error when dialog opens', async () => {
      wrapper.vm.error = 'some error'
      await wrapper.setProps({ visible: false })
      await nextTick()

      await wrapper.setProps({ visible: true })
      await nextTick()

      expect(wrapper.vm.error).toBeUndefined()
    })

    it('should reset all state when index is invalid on dialog open', async () => {
      await wrapper.setProps({ visible: false })
      await nextTick()

      await wrapper.setProps({ visible: true, index: -1 })
      await nextTick()

      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.overwriteFile).toBe(false)
      expect(wrapper.vm.newFileName).toBe('')
      expect(wrapper.vm.originalFileName).toBe('')
    })
  })

  describe('Edge Cases', () => {
    it('should handle empty file bucket', async () => {
      await wrapper.setProps({
        visible: false
      })
      await nextTick()

      await wrapper.setProps({
        fileBucket: [],
        index: 0,
        visible: true
      })
      await nextTick()

      expect(wrapper.vm.originalFileName).toBe('')
    })

    it('should handle file name with multiple dots', () => {
      wrapper.vm.newFileName = 'test.file.name.xml'
      const isValid = wrapper.vm.validateName()

      expect(isValid).toBe(true)
    })

    it('should handle very long file names', () => {
      const longName = 'a'.repeat(200) + '.xml'
      wrapper.vm.newFileName = longName
      const isValid = wrapper.vm.validateName()

      expect(isValid).toBe(true)
    })

    it('should handle file names with spaces', () => {
      wrapper.vm.newFileName = 'file with spaces.xml'
      const isValid = wrapper.vm.validateName()

      expect(isValid).toBe(true)
    })

    it('should handle file names with special characters', () => {
      wrapper.vm.newFileName = 'file!@#$%^&*().xml'
      const isValid = wrapper.vm.validateName()

      expect(isValid).toBe(true)
    })

    it('should handle file names with unicode characters', () => {
      wrapper.vm.newFileName = 'файл-тест-中文.xml'
      const isValid = wrapper.vm.validateName()

      expect(isValid).toBe(true)
    })

    it('should handle case where fileBucket has files with varying case', async () => {
      const mixedCaseFiles: UploadSnmpDataCollectionFileType[] = [
        {
          file: new File(['content'], 'Test-File.xml', { type: 'text/xml' }),
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: new File(['content'], 'another-file.xml', { type: 'text/xml' }),
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]

      await wrapper.setProps({ visible: false })
      await nextTick()

      await wrapper.setProps({ fileBucket: mixedCaseFiles, index: 0, visible: true })
      await nextTick()

      // Now originalFileName is 'Test-File.xml'
      // Try to use another file from the list with different case
      wrapper.vm.newFileName = 'ANOTHER-FILE.xml'
      const isValid = wrapper.vm.validateName()

      expect(isValid).toBe(false)
      expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
    })

    it('should handle undefined in checkbox model value', async () => {
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
      await checkboxes[0].vm.$emit('update:modelValue', undefined)
      await nextTick()

      expect(wrapper.vm.overwriteFile).toBe(false)
    })

    it('should handle rapid checkbox toggling', async () => {
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)

      await checkboxes[0].vm.$emit('update:modelValue', true)
      await checkboxes[1].vm.$emit('update:modelValue', true)
      await checkboxes[0].vm.$emit('update:modelValue', true)
      await nextTick()

      expect(wrapper.vm.overwriteFile).toBe(true)
      expect(wrapper.vm.renameFile).toBe(false)
    })
  })

  describe('Computed Properties', () => {
    describe('shouldRemainDisabled', () => {
      it('should return true when no checkbox is selected', () => {
        wrapper.vm.renameFile = false
        wrapper.vm.overwriteFile = false

        expect(wrapper.vm.shouldRemainDisabled).toBe(true)
      })

      it('should return false when overwrite is selected', () => {
        wrapper.vm.overwriteFile = true
        wrapper.vm.renameFile = false

        expect(wrapper.vm.shouldRemainDisabled).toBe(false)
      })

      it('should return false when rename is selected with no error', () => {
        wrapper.vm.renameFile = true
        wrapper.vm.overwriteFile = false
        wrapper.vm.error = undefined

        expect(wrapper.vm.shouldRemainDisabled).toBe(false)
      })

      it('should return true when rename is selected with error', () => {
        wrapper.vm.renameFile = true
        wrapper.vm.overwriteFile = false
        wrapper.vm.error = 'some error'

        expect(wrapper.vm.shouldRemainDisabled).toBe(true)
      })

      const disabledScenarios = [
        {
          description: 'both unchecked, no error',
          renameFile: false,
          overwriteFile: false,
          error: undefined,
          expected: true
        },
        {
          description: 'rename checked with error',
          renameFile: true,
          overwriteFile: false,
          error: 'error message',
          expected: true
        },
        {
          description: 'rename checked without error',
          renameFile: true,
          overwriteFile: false,
          error: undefined,
          expected: false
        },
        {
          description: 'overwrite checked',
          renameFile: false,
          overwriteFile: true,
          error: undefined,
          expected: false
        }
      ]

      it.each(disabledScenarios)(
        'should return $expected when $description',
        ({ renameFile, overwriteFile, error, expected }) => {
          wrapper.vm.renameFile = renameFile
          wrapper.vm.overwriteFile = overwriteFile
          wrapper.vm.error = error

          expect(wrapper.vm.shouldRemainDisabled).toBe(expected)
        }
      )
    })
  })

  describe('Methods', () => {
    describe('validateName', () => {
      it('should return false for empty name', () => {
        wrapper.vm.newFileName = ''
        expect(wrapper.vm.validateName()).toBe(false)
      })

      it('should return false for name without .xml extension', () => {
        wrapper.vm.newFileName = 'test.txt'
        expect(wrapper.vm.validateName()).toBe(false)
      })

      it('should return false for name matching original', () => {
        wrapper.vm.originalFileName = 'test.xml'
        wrapper.vm.newFileName = 'test.xml'
        expect(wrapper.vm.validateName()).toBe(false)
      })

      it('should return false for duplicate in upload list', () => {
        wrapper.vm.newFileName = 'another-file.xml'
        expect(wrapper.vm.validateName()).toBe(false)
      })

      it('should return false for duplicate in system', () => {
        wrapper.vm.newFileName = 'existing-file.xml'
        expect(wrapper.vm.validateName()).toBe(false)
      })

      it('should return true for valid unique name', () => {
        wrapper.vm.newFileName = 'totally-unique-name.xml'
        expect(wrapper.vm.validateName()).toBe(true)
      })
    })

    describe('onChangeFileName', () => {
      it('should update newFileName and validate', () => {
        wrapper.vm.onChangeFileName('new-name.xml')

        expect(wrapper.vm.newFileName).toBe('new-name.xml')
      })

      it('should trim the value', () => {
        wrapper.vm.onChangeFileName('  spaced-name.xml  ')

        expect(wrapper.vm.newFileName).toBe('spaced-name.xml')
      })

      it('should not update for falsy values', () => {
        const originalName = wrapper.vm.newFileName
        wrapper.vm.onChangeFileName(null)

        expect(wrapper.vm.newFileName).toBe(originalName)
      })
    })

    describe('saveChanges', () => {
      it('should emit overwrite when overwriteFile is true', () => {
        wrapper.vm.overwriteFile = true
        wrapper.vm.saveChanges()

        expect(wrapper.emitted('overwrite')).toBeTruthy()
      })

      it('should emit rename with new name when renameFile is true and name is valid', () => {
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'unique-name.xml'
        wrapper.vm.saveChanges()

        expect(wrapper.emitted('rename')).toBeTruthy()
        expect(wrapper.emitted('rename')?.[0]).toEqual(['unique-name.xml'])
      })

      it('should not emit rename when name is invalid', () => {
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = ''
        wrapper.vm.saveChanges()

        expect(wrapper.emitted('rename')).toBeFalsy()
      })

      it('should not emit rename when index is negative', async () => {
        await wrapper.setProps({ index: -1 })
        wrapper.vm.renameFile = true
        wrapper.vm.newFileName = 'valid.xml'
        wrapper.vm.saveChanges()

        expect(wrapper.emitted('rename')).toBeFalsy()
      })
    })

    describe('handleDialogHidden', () => {
      it('should reset all state', () => {
        wrapper.vm.renameFile = true
        wrapper.vm.overwriteFile = true
        wrapper.vm.newFileName = 'test.xml'
        wrapper.vm.originalFileName = 'original.xml'
        wrapper.vm.error = 'error'

        wrapper.vm.handleDialogHidden()

        expect(wrapper.vm.renameFile).toBe(false)
        expect(wrapper.vm.overwriteFile).toBe(false)
        expect(wrapper.vm.newFileName).toBe('')
        expect(wrapper.vm.originalFileName).toBe('')
        expect(wrapper.vm.error).toBeUndefined()
      })

      it('should emit close event', () => {
        wrapper.vm.handleDialogHidden()

        expect(wrapper.emitted('close')).toBeTruthy()
      })
    })

    describe('onChangeRenameFile', () => {
      it('should set renameFile to true when value is true', () => {
        wrapper.vm.onChangeRenameFile(true)
        expect(wrapper.vm.renameFile).toBe(true)
      })

      it('should set renameFile to false when value is false', () => {
        wrapper.vm.onChangeRenameFile(false)
        expect(wrapper.vm.renameFile).toBe(false)
      })

      it('should set renameFile to false when value is undefined', () => {
        wrapper.vm.onChangeRenameFile(undefined)
        expect(wrapper.vm.renameFile).toBe(false)
      })

      it('should uncheck overwriteFile when renameFile is checked', () => {
        wrapper.vm.overwriteFile = true
        wrapper.vm.onChangeRenameFile(true)

        expect(wrapper.vm.overwriteFile).toBe(false)
      })
    })

    describe('onChangeOverwriteFile', () => {
      it('should set overwriteFile to true when value is true', () => {
        wrapper.vm.onChangeOverwriteFile(true)
        expect(wrapper.vm.overwriteFile).toBe(true)
      })

      it('should set overwriteFile to false when value is false', () => {
        wrapper.vm.onChangeOverwriteFile(false)
        expect(wrapper.vm.overwriteFile).toBe(false)
      })

      it('should set overwriteFile to false when value is undefined', () => {
        wrapper.vm.onChangeOverwriteFile(undefined)
        expect(wrapper.vm.overwriteFile).toBe(false)
      })

      it('should uncheck renameFile when overwriteFile is checked', () => {
        wrapper.vm.renameFile = true
        wrapper.vm.onChangeOverwriteFile(true)

        expect(wrapper.vm.renameFile).toBe(false)
      })

      it('should set newFileName to originalFileName when checked', () => {
        wrapper.vm.originalFileName = 'original.xml'
        wrapper.vm.newFileName = 'different.xml'
        wrapper.vm.onChangeOverwriteFile(true)

        expect(wrapper.vm.newFileName).toBe('original.xml')
      })

      it('should clear error when checked', () => {
        wrapper.vm.error = 'some error'
        wrapper.vm.onChangeOverwriteFile(true)

        expect(wrapper.vm.error).toBeUndefined()
      })
    })
  })

  describe('Accessibility', () => {
    it('should have data-test attribute on dialog', () => {
      expect(wrapper.html()).toContain('data-test="dialog-title"')
    })

    it('should have data-test attribute on save button', () => {
      expect(wrapper.html()).toContain('data-test="save-button"')
    })

    it('should have data-test attribute on file name input when visible', async () => {
      wrapper.vm.renameFile = true
      await nextTick()

      expect(wrapper.html()).toContain('data-test="file-name"')
    })

    it('should have meaningful button labels', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Cancel')
      expect(buttons[1].text()).toBe('Save Changes')
    })

    it('should have checkbox group with vertical layout', () => {
      const checkboxGroup = wrapper.findComponent(FeatherCheckboxGroup)
      expect(checkboxGroup.props('vertical')).toBe(true)
    })
  })

  describe('Integration Tests', () => {
    it('should handle complete overwrite flow', async () => {
      // Select overwrite option
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
      await checkboxes[0].vm.$emit('update:modelValue', true)
      await nextTick()

      // Click save button
      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      // Verify overwrite event emitted
      expect(wrapper.emitted('overwrite')).toBeTruthy()
      expect(wrapper.emitted('rename')).toBeFalsy()
    })

    it('should handle complete rename flow with valid name', async () => {
      // Select rename option
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
      await checkboxes[1].vm.$emit('update:modelValue', true)
      await nextTick()

      // Enter valid file name
      wrapper.vm.onChangeFileName('brand-new-file.xml')
      await nextTick()

      // Click save button
      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      // Verify rename event emitted with correct name
      expect(wrapper.emitted('rename')).toBeTruthy()
      expect(wrapper.emitted('rename')?.[0]).toEqual(['brand-new-file.xml'])
    })

    it('should handle complete rename flow with invalid name', async () => {
      // Select rename option
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
      await checkboxes[1].vm.$emit('update:modelValue', true)
      await nextTick()

      // Enter invalid file name
      wrapper.vm.onChangeFileName('existing-file.xml')
      await nextTick()

      // Save button should be disabled
      expect(wrapper.vm.shouldRemainDisabled).toBe(true)

      // Click save button
      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      // Verify no event emitted
      expect(wrapper.emitted('rename')).toBeFalsy()
    })

    it('should handle cancel flow and reset state', async () => {
      // Make some changes
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
      await checkboxes[1].vm.$emit('update:modelValue', true)
      wrapper.vm.onChangeFileName('test-name.xml')
      await nextTick()

      // Click cancel
      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')

      // Verify state reset
      expect(wrapper.vm.renameFile).toBe(false)
      expect(wrapper.vm.newFileName).toBe('')
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('should handle switching between options', async () => {
      const checkboxes = wrapper.findAllComponents(FeatherCheckbox)

      // Select overwrite
      await checkboxes[0].vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.overwriteFile).toBe(true)
      expect(wrapper.vm.shouldRemainDisabled).toBe(false)

      // Switch to rename
      await checkboxes[1].vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.renameFile).toBe(true)
      expect(wrapper.vm.overwriteFile).toBe(false)

      // Switch back to overwrite
      await checkboxes[0].vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.overwriteFile).toBe(true)
      expect(wrapper.vm.renameFile).toBe(false)
    })

    it('should handle dialog reopen with different file', async () => {
      // Close dialog
      await wrapper.setProps({ visible: false })
      await nextTick()

      // Reopen with different file
      await wrapper.setProps({
        visible: true,
        index: 1
      })
      await nextTick()

      // Verify new file name is loaded
      expect(wrapper.vm.originalFileName).toBe('another-file.xml')
      expect(wrapper.vm.newFileName).toBe('another-file.xml')
    })
  })
})
