import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import EventConfigFilesUploadReportDialog from '@/components/EventConfiguration/Dialog/EventConfigFilesUploadReportDialog.vue'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue']
  }
}))

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
      uploadedSourceNames: []
    }

    wrapper = mount(EventConfigFilesUploadReportDialog, {
      props: {
        report: mockReport
      },
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherDialog
        }
      }
    })

    await flushPromises()
  })

  it('renders the dialog when visible is true', () => {
    expect(wrapper.findComponent(FeatherDialog).exists()).toBe(true)
    expect(wrapper.findComponent(FeatherDialog).props('labels')).toEqual({
      title: 'Upload Report',
      close: 'Close'
    })
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

  it('renders success and error file lists correctly', () => {
    const successItems = wrapper.findAll('li span.text-success')
    const errorItems = wrapper.findAll('li span.text-danger')
    expect(successItems).toHaveLength(1)
    expect(successItems.at(0)?.text()).toBe('file1.json')
    expect(errorItems).toHaveLength(1)
    expect(errorItems.at(0)?.text()).toBe('file3.json')
    expect(wrapper.text()).toContain('Successfully uploaded')
    expect(wrapper.text()).toContain('Failed to upload')
  })

  it('calls fetchEventConfigs and closes dialog when Close button is clicked', async () => {
    const closeButton = wrapper.findAllComponents(FeatherButton).at(0)
    expect(closeButton.exists()).toBe(true)
    await closeButton.trigger('click')
    await flushPromises()
    expect(store.fetchEventConfigs).toHaveBeenCalled()
    expect(store.$state.uploadedEventConfigFilesReportDialogState.visible).toBe(false)
  })

  it('calls fetchEventConfigs, resets active tab, and closes dialog when View Uploaded Files button is clicked', async () => {
    const viewButton = wrapper.findAllComponents(FeatherButton).at(1)
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
    expect(wrapper.findComponent(FeatherDialog).props('modelValue')).toBe(false)
  })
})
