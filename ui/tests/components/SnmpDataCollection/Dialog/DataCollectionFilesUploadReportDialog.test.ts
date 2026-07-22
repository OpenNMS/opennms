import DataCollectionFilesUploadReportDialog from '@/components/SnmpDataCollection/Dialog/DataCollectionFilesUploadReportDialog.vue'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'

// Stub the Common ConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events).
// The action button maps to "View Uploaded Files", cancel maps to "Close".
const ConfirmationDialogStub = {
  name: 'ConfirmationDialog',
  template: '<div class="confirmation-dialog"><div class="dialog-content"><slot name="content"></slot></div><button class="action-btn" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit(\'cancel\')">{{ cancelButtonText || \'Cancel\' }}</button></div>',
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('DataCollectionFilesUploadReportDialog.vue', () => {
  let wrapper: VueWrapper<any>

  const mountComponent = (report: any = { success: [], errors: [] }, dialogVisible = true) => {
    return mount(DataCollectionFilesUploadReportDialog, {
      props: { report: report as EventConfigFilesUploadResponse, dialogVisible },
      global: {
        stubs: { ConfirmationDialog: ConfirmationDialogStub }
      }
    })
  }

  const confirmationDialog = () => wrapper.findComponent({ name: 'ConfirmationDialog' })

  beforeEach(async () => {
    wrapper = mountComponent()
    await flushPromises()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Initial Rendering', () => {
    it('renders the ConfirmationDialog', () => {
      expect(confirmationDialog().exists()).toBe(true)
    })

    it('uses "Upload Report" as the title', () => {
      expect(confirmationDialog().props('title')).toBe('Upload Report')
    })

    it('uses "View Uploaded Files" as the action button and "Close" as cancel', () => {
      expect(confirmationDialog().props('actionButtonText')).toBe('View Uploaded Files')
      expect(confirmationDialog().props('cancelButtonText')).toBe('Close')
    })

    it('passes dialogVisible through to the visible prop', async () => {
      await wrapper.setProps({ dialogVisible: false })
      expect(confirmationDialog().props('visible')).toBe(false)
    })

    it('renders Message and Details headings', () => {
      expect(wrapper.html()).toContain('Message:')
      expect(wrapper.html()).toContain('Details:')
    })

    it('renders the upload report scroll container', () => {
      expect(wrapper.html()).toContain('upload-report-scroll')
    })
  })

  describe('Upload Report Status', () => {
    it.each([
      [{ success: [{ file: 'a.xml' }], errors: [] }, 'All files uploaded successfully.'],
      [{ success: [], errors: [{ file: 'a.xml' }] }, 'All files failed to upload.'],
      [{ success: [{ file: 'a.xml' }], errors: [{ file: 'b.xml' }] }, 'Some files uploaded successfully, while others failed.'],
      [{ success: [], errors: [] }, 'No files were uploaded.']
    ])('shows correct status message', async (report, expected) => {
      await wrapper.setProps({ report: report as any })
      await flushPromises()
      expect(wrapper.text()).toContain(expected)
    })

    it('treats undefined success/errors arrays as no files', () => {
      expect(wrapper.vm.getUploadReportStatus()).toBe('No files were uploaded.')
    })
  })

  describe('File List Display', () => {
    it('lists successful files with the success class', async () => {
      await wrapper.setProps({ report: { success: [{ file: 'success.xml' }], errors: [] } as any })
      await flushPromises()
      expect(wrapper.html()).toContain('success.xml')
      expect(wrapper.html()).toContain('Successfully uploaded')
      expect(wrapper.findAll('.text-success')).toHaveLength(1)
    })

    it('lists failed files with the danger class', async () => {
      await wrapper.setProps({ report: { success: [], errors: [{ file: 'failed.xml' }] } as any })
      await flushPromises()
      expect(wrapper.html()).toContain('failed.xml')
      expect(wrapper.html()).toContain('Failed to upload')
      expect(wrapper.findAll('.text-danger')).toHaveLength(1)
    })

    it('lists both success and error files', async () => {
      await wrapper.setProps({
        report: { success: [{ file: 's1.xml' }, { file: 's2.xml' }], errors: [{ file: 'e1.xml' }] } as any
      })
      await flushPromises()
      expect(wrapper.findAll('li')).toHaveLength(3)
      expect(wrapper.findAll('.text-success')).toHaveLength(2)
      expect(wrapper.findAll('.text-danger')).toHaveLength(1)
    })
  })

  describe('Event Emissions', () => {
    it('emits close when Close (cancel) is triggered', async () => {
      await wrapper.find('.cancel-btn').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
      expect(wrapper.emitted('close')).toHaveLength(1)
    })

    it('emits view when View Uploaded Files (ok) is triggered', async () => {
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('view')).toBeTruthy()
      expect(wrapper.emitted('view')).toHaveLength(1)
    })
  })
})
