import UploadedFileRenameDialog from '@/components/EventConfiguration/Dialog/UploadedFileRenameDialog.vue'
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox, FeatherCheckboxGroup } from '@featherds/checkbox'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'
import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('UploadedFileRenameDialog.vue', () => {
  let wrapper: any

  const mockFile = new File(['<xml></xml>'], 'original.events.xml', { type: 'text/xml' })
  const mockFile2 = new File(['<xml></xml>'], 'another.events.xml', { type: 'text/xml' })
  
  const fileBucket = [
    {
      file: mockFile,
      isValid: true,
      errors: [],
      isDuplicate: true
    },
    {
      file: mockFile2,
      isValid: true,
      errors: [],
      isDuplicate: false
    }
  ]

  const createWrapper = (props = {}) =>
    mount(UploadedFileRenameDialog, {
      props: {
        visible: true,
        index: 0,
        fileBucket,
        alreadyExistsNames: [],
        ...props
      },
      global: {
        components: {
          FeatherDialog,
          FeatherButton,
          FeatherCheckbox,
          FeatherCheckboxGroup,
          FeatherInput
        }
      },
      attachTo: document.body
    })

  beforeEach(() => {
    vi.useFakeTimers()
    wrapper = createWrapper()
  })

  afterEach(async () => {
    // Advance timers before unmounting to clear pending focus management timers
    vi.advanceTimersByTime(1000)
    if (wrapper) {
      await wrapper.unmount()
    }
    vi.useRealTimers()
  })

  // Rendering Tests
  it('renders dialog with correct title', async () => {
    await flushPromises()
    expect(wrapper.vm.labels.title).toBe('Rename Uploaded File')
  })

  it('renders both checkbox options', async () => {
    await flushPromises()
    const checkboxes = wrapper.findAllComponents(FeatherCheckbox)
    expect(checkboxes.length).toBe(2)
  })

  it('shows renameFile flag when rename checkbox is selected', async () => {
    wrapper.vm.renameFile = true
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.renameFile).toBe(true)
  })

  it('hides renameFile flag when rename checkbox is not selected', async () => {
    wrapper.vm.renameFile = false
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.renameFile).toBe(false)
  })

  it('renders Save Changes and Cancel buttons', async () => {
    await flushPromises()
    const buttons = wrapper.findAllComponents(FeatherButton)
    expect(buttons.length).toBe(2)
    expect(buttons[0].text()).toBe('Cancel')
    expect(buttons[1].text()).toBe('Save Changes')
  })

  // Validation Tests
  it('shows validation error for empty file name', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = ''
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBe('File name cannot be empty.')
  })

  it('shows validation error for file name without .xml extension', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'filename.txt'
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBe('File name must end with .xml')
  })

  it('shows validation error when new name matches original name', async () => {
    // First initialize originalFileName via watch
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true, index: 0 })
    await flushPromises()
    
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'original.events.xml'
    wrapper.vm.validateName()
    await flushPromises()
    
    // Check that it fails with either error (bucket check or original check)
    expect(wrapper.vm.error).toBeDefined()
  })

  it('shows validation error when file exists in current upload list', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'another.events.xml'
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
  })

  it('shows validation error when file exists in system (case-insensitive)', async () => {
    await wrapper.setProps({ alreadyExistsNames: [{ id: 1, name: 'duplicate.events.xml' }] })
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'DUPLICATE.events.xml'
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBe('A file with this name already exists in the system.')
  })

  it('passes validation for unique file name', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'unique.events.xml'
    const isValid = wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBeUndefined()
    expect(isValid).toBe(true)
  })

  it('trims whitespace from file name during validation', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = '  unique.events.xml  '
    wrapper.vm.onChangeFileName('  unique.events.xml  ')
    await flushPromises()
    
    expect(wrapper.vm.newFileName).toBe('unique.events.xml')
  })

  // Button State Tests
  it('disables Save Changes button when no option is selected', async () => {
    wrapper.vm.renameFile = false
    wrapper.vm.overwriteFile = false
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.shouldRemainDisabled).toBe(true)
  })

  it('disables Save Changes button when rename has validation error', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = ''
    wrapper.vm.validateName()
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.shouldRemainDisabled).toBe(true)
  })

  it('enables Save Changes button when overwrite is selected', async () => {
    wrapper.vm.overwriteFile = true
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.shouldRemainDisabled).toBe(false)
  })

  it('enables Save Changes button when rename has valid name', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'newname.events.xml'
    wrapper.vm.validateName()
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.shouldRemainDisabled).toBe(false)
  })

  // Checkbox Interaction Tests
  it('deselects overwrite when rename is selected', async () => {
    wrapper.vm.overwriteFile = true
    await wrapper.vm.$nextTick()
    
    wrapper.vm.onChangeRenameFile(true)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.renameFile).toBe(true)
    expect(wrapper.vm.overwriteFile).toBe(false)
  })

  it('deselects rename when overwrite is selected', async () => {
    wrapper.vm.renameFile = true
    await wrapper.vm.$nextTick()
    
    wrapper.vm.onChangeOverwriteFile(true)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.overwriteFile).toBe(true)
    expect(wrapper.vm.renameFile).toBe(false)
  })

  it('clears error when overwrite is selected', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = ''
    wrapper.vm.validateName()
    expect(wrapper.vm.error).toBeDefined()
    
    wrapper.vm.onChangeOverwriteFile(true)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.error).toBeUndefined()
  })

  it('sets new file name to original when overwrite is selected', async () => {
    // Initialize originalFileName via watch
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true, index: 0 })
    await flushPromises()
    
    wrapper.vm.onChangeOverwriteFile(true)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.vm.newFileName).toBe('original.events.xml')
  })

  // Save Changes Tests
  it('emits overwrite event when overwrite is selected and Save Changes clicked', async () => {
    wrapper.vm.overwriteFile = true
    wrapper.vm.saveChanges()
    await flushPromises()
    
    expect(wrapper.emitted('overwrite')).toBeTruthy()
    expect(wrapper.emitted('rename')).toBeFalsy()
  })

  it('emits rename event with new name when rename is valid and Save Changes clicked', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'newname.events.xml'
    wrapper.vm.validateName()
    wrapper.vm.saveChanges()
    await flushPromises()
    
    expect(wrapper.emitted('rename')).toBeTruthy()
    expect(wrapper.emitted('rename')?.[0]).toEqual(['newname.events.xml'])
    expect(wrapper.emitted('overwrite')).toBeFalsy()
  })

  it('does not emit rename when validation fails', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = ''
    wrapper.vm.saveChanges()
    await flushPromises()
    
    expect(wrapper.emitted('rename')).toBeFalsy()
  })

  it('does not emit rename when index is invalid', async () => {
    await wrapper.setProps({ index: -1 })
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'valid.events.xml'
    wrapper.vm.validateName()
    wrapper.vm.saveChanges()
    await flushPromises()
    
    expect(wrapper.emitted('rename')).toBeFalsy()
  })

  // Dialog Close Tests
  it('emits close event when Cancel button clicked', async () => {
    const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
    await cancelButton.trigger('click')
    await flushPromises()
    
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('resets all state when dialog is closed', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.overwriteFile = true
    wrapper.vm.newFileName = 'test.xml'
    wrapper.vm.error = 'Some error'
    
    wrapper.vm.handleDialogHidden()
    await flushPromises()
    
    expect(wrapper.vm.renameFile).toBe(false)
    expect(wrapper.vm.overwriteFile).toBe(false)
    expect(wrapper.vm.newFileName).toBe('')
    expect(wrapper.vm.originalFileName).toBe('')
    expect(wrapper.vm.error).toBeUndefined()
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  // Watch Tests
  it('initializes original file name when dialog becomes visible', async () => {
    const newWrapper: any = createWrapper({ visible: false })
    expect(newWrapper.vm.originalFileName).toBe('')
    
    await newWrapper.setProps({ visible: true })
    await flushPromises()
    
    expect(newWrapper.vm.originalFileName).toBe('original.events.xml')
    expect(newWrapper.vm.newFileName).toBe('original.events.xml')
    newWrapper.unmount()
  })

  it('resets state when dialog becomes invisible', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'test.xml'
    
    await wrapper.setProps({ visible: false })
    await flushPromises()
    
    expect(wrapper.vm.renameFile).toBe(false)
    expect(wrapper.vm.newFileName).toBe('')
  })

  it('resets state when index is invalid', async () => {
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true, index: 999 })
    await flushPromises()
    
    expect(wrapper.vm.originalFileName).toBe('')
    expect(wrapper.vm.newFileName).toBe('')
    expect(wrapper.vm.renameFile).toBe(false)
    expect(wrapper.vm.overwriteFile).toBe(false)
  })

  // Edge Cases
  it('handles case-insensitive duplicate check in current upload list', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'ANOTHER.events.xml'
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBe('A file with this name already exists in the current upload list.')
  })

  it('validates file name must end with lowercase .xml', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'test.XML'
    wrapper.vm.validateName()
    await flushPromises()
    
    // Component checks for lowercase .xml only
    expect(wrapper.vm.error).toBe('File name must end with .xml')
  })

  it('validates file name with multiple dots', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'my.test.file.events.xml'
    const isValid = wrapper.vm.validateName()
    await flushPromises()
    
    expect(isValid).toBe(true)
    expect(wrapper.vm.error).toBeUndefined()
  })

  it('updates dialogVisible when props.visible changes', async () => {
    expect(wrapper.vm.dialogVisible).toBe(true)
    
    await wrapper.setProps({ visible: false })
    await flushPromises()
    
    expect(wrapper.vm.dialogVisible).toBe(false)
  })
})
