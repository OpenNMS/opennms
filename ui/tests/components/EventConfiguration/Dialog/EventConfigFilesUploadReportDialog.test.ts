import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import EventConfigFilesUploadReportDialog from '@/components/EventConfiguration/Dialog/EventConfigFilesUploadReportDialog.vue'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'

// Stub the Common OnmsConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events),
// independent of the underlying dialog library.
const ConfirmationDialogStub = {
  name: 'OnmsConfirmationDialog',
  template: '<div class="confirmation-dialog"><div class="modal-body"><slot name="content"></slot></div><button class="action-btn" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit(\'cancel\')">{{ cancelButtonText || \'Cancel\' }}</button></div>',
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('EventConfigFilesUploadReportDialog', () => {
  let wrapper: any
  let store: ReturnType<typeof useEventConfigStore>

  const mockReport = {
    success: [{ file: 'file1.json' }],
    errors: [{ file: 'file3.json', error: 'Invalid format' }]
  } as unknown as EventConfigFilesUploadResponse

  beforeEach(async () => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: true
    })
    store = useEventConfigStore(pinia)
    store.$state = {
      uploadedEventConfigFilesReportDialogState: {
        visible: true
      },
      deleteEventConfigSourceDialogState: {
        visible: false,
        eventConfigSource: null
      },
      changeEventConfigSourceStatusDialogState: {
        visible: false,
        eventConfigSource: null
      },
      sources: [],
      sourcesPagination: { page: 1, pageSize: 10, total: 0 },
      sourcesSearchTerm: '',
      sourcesSorting: { sortOrder: 'desc', sortKey: 'createdTime' },
      isLoading: false,
      activeTab: 0,
      uploadedSources: [],
      orderedSources: [],
      catchAllSource: null,
      isSavingSourceOrder: false,
      sourcesReorderMode: false,
      createEventConfigSourceDialogState: { visible: false }
    }

    wrapper = mount(EventConfigFilesUploadReportDialog, {
      props: {
        report: mockReport
      },
      global: {
        plugins: [pinia],
        stubs: {
          OnmsConfirmationDialog: ConfirmationDialogStub
        }
      }
    })

    await flushPromises()
  })

  it('renders the dialog when visible is true', () => {
    expect(wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).props('title')).toBe('Upload Report')
  })

  it('displays the correct status message for mixed success and errors', () => {
    const statusMessage = wrapper.find('p').text()
    expect(statusMessage).toBe('Some files uploaded successfully, while others failed.')
  })

  it('displays the correct status message for all successes', async () => {
    await wrapper.setProps({
      report: {
        success: [{ file: 'file1.json' }, { file: 'file2.json' }],
        errors: []
      }
    })
    await flushPromises()
    const statusMessage = wrapper.find('p').text()
    expect(statusMessage).toBe('All files uploaded successfully.')
  })

  it('displays the correct status message for all errors', async () => {
    await wrapper.setProps({
      report: {
        success: [],
        errors: [
          { file: 'file3.json', error: 'Invalid format' },
          { file: 'file4.json', error: 'Duplicate file' }
        ]
      }
    })
    await flushPromises()
    const statusMessage = wrapper.find('p').text()
    expect(statusMessage).toBe('All files failed to upload.')
  })

  it('displays the correct status message for no files', async () => {
    await wrapper.setProps({
      report: {
        success: [],
        errors: []
      }
    })
    await flushPromises()
    const statusMessage = wrapper.find('p').text()
    expect(statusMessage).toBe('No files were uploaded.')
  })

  it('summarizes successes and keeps errors prominent instead of listing every file', async () => {
    // errors are always visible with their reason; successes roll up into a count
    expect(wrapper.get('[data-test="report-summary"]').text()).toContain('1 source uploaded')
    expect(wrapper.get('[data-test="report-summary"]').text()).toContain('1 file was not uploaded')
    const errors = wrapper.get('[data-test="report-errors"]')
    expect(errors.text()).toContain('file3.json')
    expect(errors.text()).toContain('Invalid format')
    // the file list stays collapsed until asked for
    expect(wrapper.find('[data-test="report-file-list"]').exists()).toBe(false)
    await wrapper.get('[data-test="toggle-file-list"]').trigger('click')
    expect(wrapper.get('[data-test="report-file-list"]').text()).toContain('file1.json')
  })

  it('rolls event counts up into the summary line', async () => {
    await wrapper.setProps({
      report: {
        success: [
          { file: 'a.events', eventCount: 40 },
          { file: 'b.events', eventCount: 2 }
        ],
        errors: []
      } as unknown as EventConfigFilesUploadResponse
    })
    expect(wrapper.get('[data-test="report-summary"]').text()).toContain('2 sources uploaded · 42 event definitions')
  })

  it('promotes the applied source order into the summary', async () => {
    await wrapper.setProps({
      report: {
        success: [
          { file: 'file1.json' },
          { file: 'eventconf.xml (source order)', message: 'Source order applied (3 entries)' }
        ],
        errors: []
      } as unknown as EventConfigFilesUploadResponse
    })
    expect(wrapper.get('[data-test="report-summary"]').text()).toContain('Source order applied (3 entries)')
    // the order step is a summary line, not an uploaded source
    expect(wrapper.get('[data-test="toggle-file-list"]').text()).toContain('1 uploaded source')
  })

  it('calls fetchEventConfigs and closes dialog when Close button is clicked', async () => {
    const closeButton = wrapper.find('.cancel-btn')
    expect(closeButton.exists()).toBe(true)
    await closeButton.trigger('click')
    await flushPromises()
    expect(store.fetchEventConfigs).toHaveBeenCalled()
    expect(store.$state.uploadedEventConfigFilesReportDialogState.visible).toBe(false)
  })

  it('calls fetchEventConfigs, resets active tab, and closes dialog when View Uploaded Files button is clicked', async () => {
    const viewButton = wrapper.find('.action-btn')
    expect(viewButton.exists()).toBe(true)
    await viewButton.trigger('click')
    await flushPromises()
    expect(store.fetchEventConfigs).toHaveBeenCalled()
    expect(store.resetActiveTab).toHaveBeenCalled()
    expect(store.$state.uploadedEventConfigFilesReportDialogState.visible).toBe(false)
  })

  it('hides the dialog when visible is false', async () => {
    store.$state.uploadedEventConfigFilesReportDialogState.visible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).props('visible')).toBe(false)
  })
})
