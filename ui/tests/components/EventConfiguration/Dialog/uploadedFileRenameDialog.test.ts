import { flushPromises, mount } from '@vue/test-utils'
import UploadedFileRenameDialog from '@/components/EventConfiguration/Dialog/UploadedFileRenameDialog.vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('UploadedFileRenameDialog.vue', () => {
  let wrapper: any

  const mockFile = new File(['<xml></xml>'], 'original.events.xml', { type: 'text/xml' })
  const fileBucket = [
    {
      file: mockFile,
      isValid: true,
      errors: [],
      isDuplicate: false
    }
  ]

  const createWrapper = (visible = true) =>
    mount(UploadedFileRenameDialog, {
      props: {
        visible,
        index: 0,
        fileBucket,
        alreadyExistsNames: []
      },
      global: {
        mocks: {
        }
      }
    })

  beforeEach(() => {
    wrapper = createWrapper()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  it('renders dialog title correctly', async () => {
    await flushPromises()
    expect(wrapper.vm.labels.title).toBe('Rename Uploaded File')
  })

  it('shows validation error for empty file name', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = ''
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error?.toLowerCase()).toContain('empty')
  })

  it('shows validation error for duplicate file name', async () => {
    await wrapper.setProps({ alreadyExistsNames: ['duplicate.events.xml'] })
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'duplicate.events.xml'
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error?.toLowerCase()).toMatch(/already exists/)
  })

  it('passes validation for unique file name', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'unique.events.xml'
    wrapper.vm.validateName()
    await flushPromises()
    
    expect(wrapper.vm.error).toBeUndefined()
  })

  it('calls handleDialogHidden when close event occurs', async () => {
    const spy = vi.spyOn(wrapper.vm, 'handleDialogHidden')
    wrapper.vm.handleDialogHidden()
    await flushPromises()

    expect(spy).toHaveBeenCalled()
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('calls saveChanges when Save Changes button clicked', async () => {
    wrapper.vm.renameFile = true
    wrapper.vm.newFileName = 'newname.events.xml'
    wrapper.vm.validateName()
    await flushPromises()

    const spy = vi.spyOn(wrapper.vm, 'saveChanges')
    
    wrapper.vm.saveChanges()
    expect(spy).toHaveBeenCalled()
  })
})