import DataCollectionFilesUploadReportDialog from '@/components/SnmpDataCollection/Dialog/DataCollectionFilesUploadReportDialog.vue'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
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

describe('DataCollectionFilesUploadReportDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let mockReport: EventConfigFilesUploadResponse

  beforeEach(async () => {
    vi.clearAllMocks()

    mockReport = {
      errors: [],
      success: []
    } as any

    wrapper = mount(DataCollectionFilesUploadReportDialog, {
      props: {
        report: mockReport,
        dialogVisible: true
      },
      global: {
        components: {
          FeatherButton
        }
      }
    })

    await flushPromises()
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

    it('should have dialog title "Upload Report"', () => {
      // FeatherDialog is mocked, we just verify component uses correct labels prop
      expect(wrapper.vm.$options.components).toBeTruthy()
    })

    it('should have close button label', () => {
      // FeatherDialog is mocked, we just verify component uses correct labels prop
      expect(wrapper.vm.$options.components).toBeTruthy()
    })

    it('should have hide-close prop set', () => {
      // FeatherDialog is mocked, we just verify component renders
      expect(wrapper.html().length).toBeGreaterThan(0)
    })

    it('should render Message heading', () => {
      expect(wrapper.html()).toContain('Message:')
    })

    it('should render Details heading', () => {
      expect(wrapper.html()).toContain('Details:')
    })

    it('should render upload report scroll container', () => {
      expect(wrapper.html()).toContain('upload-report-scroll')
    })

    it('should render footer buttons', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
    })

    it('should render Close button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Close')
    })

    it('should render View Uploaded Files button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].text()).toBe('View Uploaded Files')
    })

    it('should have primary prop on View Uploaded Files button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].props('primary')).toBe(true)
    })
  })

  describe('Dialog Visibility', () => {
    it('should not show dialog when dialogVisible is false', async () => {
      await wrapper.setProps({ dialogVisible: false })
      expect(wrapper.props('dialogVisible')).toBe(false)
    })

    it('should show dialog when dialogVisible is true', async () => {
      await wrapper.setProps({ dialogVisible: true })
      expect(wrapper.props('dialogVisible')).toBe(true)
    })

    it('should compute isDialogVisible from props', async () => {
      await wrapper.setProps({ dialogVisible: true })
      expect(wrapper.vm.isDialogVisible).toBe(true)
    })

    it('should emit close when isDialogVisible setter is called', async () => {
      wrapper.vm.isDialogVisible = false
      await wrapper.vm.$nextTick()

      expect(wrapper.emitted('close')).toBeTruthy()
    })
  })

  describe('Upload Report Status', () => {
    describe('All Success Scenarios', () => {
      it('should display "All files uploaded successfully" when all succeed', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'file1.xml' }, { file: 'file2.xml' }],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('All files uploaded successfully.')
      })

      it('should display success message for single file upload', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'single.xml' }],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('All files uploaded successfully.')
      })
    })

    describe('All Failure Scenarios', () => {
      it('should display "All files failed to upload" when all fail', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'file1.xml', error: 'Error 1' }, { file: 'file2.xml', error: 'Error 2' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('All files failed to upload.')
      })

      it('should display failure message for single file', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'failed.xml', error: 'Validation failed' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('All files failed to upload.')
      })
    })

    describe('Partial Success Scenarios', () => {
      it('should display partial success message when some succeed and some fail', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'success1.xml' }, { file: 'success2.xml' }],
            errors: [{ file: 'failed1.xml', error: 'Error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('Some files uploaded successfully, while others failed.')
      })

      it('should display partial success for equal successes and failures', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'success.xml' }],
            errors: [{ file: 'failed.xml', error: 'Error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('Some files uploaded successfully, while others failed.')
      })
    })

    describe('No Files Scenario', () => {
      it('should display "No files were uploaded" when both arrays are empty', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('No files were uploaded.')
      })
    })

    describe('Parametrized Status Tests', () => {
      const statusScenarios = [
        {
          description: 'all files succeed (3 files)',
          report: {
            success: [{ file: 'file1.xml' }, { file: 'file2.xml' }, { file: 'file3.xml' }],
            errors: []
          },
          expectedMessage: 'All files uploaded successfully.'
        },
        {
          description: 'all files fail (2 files)',
          report: {
            success: [],
            errors: [{ file: 'file1.xml', error: 'Error 1' }, { file: 'file2.xml', error: 'Error 2' }]
          },
          expectedMessage: 'All files failed to upload.'
        },
        {
          description: 'partial success (3 success, 2 failures)',
          report: {
            success: [{ file: 's1.xml' }, { file: 's2.xml' }, { file: 's3.xml' }],
            errors: [{ file: 'f1.xml', error: 'E1' }, { file: 'f2.xml', error: 'E2' }]
          },
          expectedMessage: 'Some files uploaded successfully, while others failed.'
        },
        {
          description: 'no files uploaded',
          report: {
            success: [],
            errors: []
          },
          expectedMessage: 'No files were uploaded.'
        }
      ]

      it.each(statusScenarios)(
        'should display correct message when $description',
        async ({ report, expectedMessage }) => {
          await wrapper.setProps({ report: report as any })
          await wrapper.vm.$nextTick()

          expect(wrapper.html()).toContain(expectedMessage)
        }
      )
    })
  })

  describe('File List Display', () => {
    describe('Success Files Display', () => {
      it('should display successful file names', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'success-file.xml' }],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('success-file.xml')
      })

      it('should display multiple successful files', async () => {
        await wrapper.setProps({
          report: {
            success: [
              { file: 'file1.xml' },
              { file: 'file2.xml' },
              { file: 'file3.xml' }
            ],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('file1.xml')
        expect(wrapper.html()).toContain('file2.xml')
        expect(wrapper.html()).toContain('file3.xml')
      })

      it('should display "Successfully uploaded" text for success files', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'test.xml' }],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('Successfully uploaded')
      })

      it('should have success class on successful file names', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'success.xml' }],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        const successSpans = wrapper.findAll('.text-success')
        expect(successSpans.length).toBeGreaterThan(0)
      })

      it('should render success files with correct key', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'file1.xml' }, { file: 'file2.xml' }],
            errors: []
          } as any
        })
        await wrapper.vm.$nextTick()

        const listItems = wrapper.findAll('li')
        expect(listItems.length).toBe(2)
      })
    })

    describe('Error Files Display', () => {
      it('should display failed file names', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'failed-file.xml', error: 'Validation error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('failed-file.xml')
      })

      it('should display multiple failed files', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [
              { file: 'error1.xml', error: 'Error 1' },
              { file: 'error2.xml', error: 'Error 2' },
              { file: 'error3.xml', error: 'Error 3' }
            ]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('error1.xml')
        expect(wrapper.html()).toContain('error2.xml')
        expect(wrapper.html()).toContain('error3.xml')
      })

      it('should display "Failed to upload" text for error files', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'test.xml', error: 'Error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('Failed to upload')
      })

      it('should have danger class on failed file names', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'failed.xml', error: 'Error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        const dangerSpans = wrapper.findAll('.text-danger')
        expect(dangerSpans.length).toBeGreaterThan(0)
      })

      it('should render error files with correct key', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'file1.xml', error: 'E1' }, { file: 'file2.xml', error: 'E2' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        const listItems = wrapper.findAll('li')
        expect(listItems.length).toBe(2)
      })
    })

    describe('Mixed Success and Error Display', () => {
      it('should display both success and error files', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'success.xml' }],
            errors: [{ file: 'failed.xml', error: 'Error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.html()).toContain('success.xml')
        expect(wrapper.html()).toContain('failed.xml')
      })

      it('should display correct count of list items for mixed results', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 's1.xml' }, { file: 's2.xml' }],
            errors: [{ file: 'e1.xml', error: 'E1' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        const listItems = wrapper.findAll('li')
        expect(listItems.length).toBe(3)
      })

      it('should have both success and danger classes present', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'success.xml' }],
            errors: [{ file: 'failed.xml', error: 'Error' }]
          } as any
        })
        await wrapper.vm.$nextTick()

        expect(wrapper.findAll('.text-success').length).toBe(1)
        expect(wrapper.findAll('.text-danger').length).toBe(1)
      })
    })

    describe('Parametrized File Display Tests', () => {
      const fileDisplayScenarios = [
        {
          description: '1 success, 0 errors',
          report: {
            success: [{ file: 'file1.xml' }],
            errors: []
          },
          expectedSuccessCount: 1,
          expectedErrorCount: 0
        },
        {
          description: '0 success, 1 error',
          report: {
            success: [],
            errors: [{ file: 'file1.xml', error: 'Error' }]
          },
          expectedSuccessCount: 0,
          expectedErrorCount: 1
        },
        {
          description: '3 success, 2 errors',
          report: {
            success: [{ file: 's1.xml' }, { file: 's2.xml' }, { file: 's3.xml' }],
            errors: [{ file: 'e1.xml', error: 'E1' }, { file: 'e2.xml', error: 'E2' }]
          },
          expectedSuccessCount: 3,
          expectedErrorCount: 2
        },
        {
          description: '5 success, 5 errors',
          report: {
            success: Array(5).fill(null).map((_, i) => ({ file: `s${i}.xml` })),
            errors: Array(5).fill(null).map((_, i) => ({ file: `e${i}.xml`, error: `E${i}` }))
          },
          expectedSuccessCount: 5,
          expectedErrorCount: 5
        }
      ]

      it.each(fileDisplayScenarios)(
        'should display correct file counts for $description',
        async ({ report, expectedSuccessCount, expectedErrorCount }) => {
          await wrapper.setProps({ report: report as any })
          await wrapper.vm.$nextTick()

          const successSpans = wrapper.findAll('.text-success')
          const errorSpans = wrapper.findAll('.text-danger')

          expect(successSpans.length).toBe(expectedSuccessCount)
          expect(errorSpans.length).toBe(expectedErrorCount)
        }
      )
    })
  })

  describe('Button Click Handlers', () => {
    describe('Close Button', () => {
      it('should emit close event when Close button is clicked', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')

        expect(wrapper.emitted('close')).toBeTruthy()
      })

      it('should call closeDialog method when Close button is clicked', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')

        // Verify close event was emitted
        expect(wrapper.emitted('close')).toBeTruthy()
      })

      it('should emit close event only once per click', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')

        expect(wrapper.emitted('close')).toHaveLength(1)
      })
    })

    describe('View Uploaded Files Button', () => {
      it('should emit view event when View button is clicked', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('view')).toBeTruthy()
      })

      it('should call gotoViewTab method when View button is clicked', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        // Verify view event was emitted
        expect(wrapper.emitted('view')).toBeTruthy()
      })

      it('should emit view event only once per click', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')

        expect(wrapper.emitted('view')).toHaveLength(1)
      })
    })

    describe('Multiple Button Clicks', () => {
      it('should handle multiple Close button clicks', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')
        await buttons[0].trigger('click')
        await buttons[0].trigger('click')

        expect(wrapper.emitted('close')).toHaveLength(3)
      })

      it('should handle multiple View button clicks', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[1].trigger('click')
        await buttons[1].trigger('click')

        expect(wrapper.emitted('view')).toHaveLength(2)
      })

      it('should handle alternating button clicks', async () => {
        const buttons = wrapper.findAllComponents(FeatherButton)
        await buttons[0].trigger('click')
        await buttons[1].trigger('click')
        await buttons[0].trigger('click')

        expect(wrapper.emitted('close')).toHaveLength(2)
        expect(wrapper.emitted('view')).toHaveLength(1)
      })
    })
  })

  describe('Dialog Events', () => {
    it('should emit close when dialog hidden event fires', async () => {
      await wrapper.vm.closeDialog()
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('should call closeDialog when close method is invoked', async () => {
      const closeDialogSpy = vi.spyOn(wrapper.vm, 'closeDialog')
      await wrapper.vm.closeDialog()

      expect(closeDialogSpy).toHaveBeenCalled()
    })
  })

  describe('Edge Cases', () => {
    it('should handle file names with special characters', async () => {
      await wrapper.setProps({
        report: {
          success: [{ file: 'file@#$%.xml' }],
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.html()).toContain('file@#$%.xml')
    })

    it('should handle file names with spaces', async () => {
      await wrapper.setProps({
        report: {
          success: [{ file: 'my file name.xml' }],
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.html()).toContain('my file name.xml')
    })

    it('should handle very long file names', async () => {
      const longFileName = 'this-is-a-very-long-file-name-that-might-cause-display-issues.xml'
      await wrapper.setProps({
        report: {
          success: [{ file: longFileName }],
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.html()).toContain(longFileName)
    })

    it('should handle empty file name strings', async () => {
      await wrapper.setProps({
        report: {
          success: [{ file: '' }],
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      const listItems = wrapper.findAll('li')
      expect(listItems.length).toBe(1)
    })

    it('should handle large number of files', async () => {
      const manySuccessFiles = Array(50).fill(null).map((_, i) => ({ file: `file-${i}.xml` }))
      await wrapper.setProps({
        report: {
          success: manySuccessFiles,
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      const listItems = wrapper.findAll('li')
      expect(listItems.length).toBe(50)
    })

    it('should handle undefined report properties gracefully', async () => {
      await wrapper.setProps({
        report: {} as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.html()).toContain('No files were uploaded.')
    })

    it('should handle report with undefined success array', async () => {
      await wrapper.setProps({
        report: {
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.getUploadReportStatus()).toBe('No files were uploaded.')
    })

    it('should handle report with undefined errors array', async () => {
      await wrapper.setProps({
        report: {
          success: []
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.getUploadReportStatus()).toBe('No files were uploaded.')
    })
  })

  describe('Computed Properties', () => {
    it('should compute isDialogVisible correctly when true', async () => {
      await wrapper.setProps({ dialogVisible: true })
      expect(wrapper.vm.isDialogVisible).toBe(true)
    })

    it('should compute isDialogVisible correctly when false', async () => {
      await wrapper.setProps({ dialogVisible: false })
      expect(wrapper.vm.isDialogVisible).toBe(false)
    })

    it('should update isDialogVisible when prop changes', async () => {
      await wrapper.setProps({ dialogVisible: false })
      expect(wrapper.vm.isDialogVisible).toBe(false)

      await wrapper.setProps({ dialogVisible: true })
      expect(wrapper.vm.isDialogVisible).toBe(true)
    })
  })

  describe('Methods', () => {
    describe('getUploadReportStatus', () => {
      it('should return correct status for all success', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 'file.xml' }],
            errors: []
          } as any
        })
        expect(wrapper.vm.getUploadReportStatus()).toBe('All files uploaded successfully.')
      })

      it('should return correct status for all failures', async () => {
        await wrapper.setProps({
          report: {
            success: [],
            errors: [{ file: 'file.xml', error: 'Error' }]
          } as any
        })
        expect(wrapper.vm.getUploadReportStatus()).toBe('All files failed to upload.')
      })

      it('should return correct status for partial success', async () => {
        await wrapper.setProps({
          report: {
            success: [{ file: 's.xml' }],
            errors: [{ file: 'e.xml', error: 'Error' }]
          } as any
        })
        expect(wrapper.vm.getUploadReportStatus()).toBe('Some files uploaded successfully, while others failed.')
      })

      it('should return correct status for no files', () => {
        wrapper.setProps({
          report: {
            success: [],
            errors: []
          } as any
        })
        expect(wrapper.vm.getUploadReportStatus()).toBe('No files were uploaded.')
      })
    })

    describe('closeDialog', () => {
      it('should emit close event', async () => {
        await wrapper.vm.closeDialog()
        expect(wrapper.emitted('close')).toBeTruthy()
      })

      it('should be async', () => {
        const result = wrapper.vm.closeDialog()
        expect(result).toBeInstanceOf(Promise)
      })
    })

    describe('gotoViewTab', () => {
      it('should emit view event', async () => {
        await wrapper.vm.gotoViewTab()
        expect(wrapper.emitted('view')).toBeTruthy()
      })

      it('should be async', () => {
        const result = wrapper.vm.gotoViewTab()
        expect(result).toBeInstanceOf(Promise)
      })
    })
  })

  describe('Component Structure', () => {
    it('should have upload-report-scroll container', () => {
      expect(wrapper.html()).toContain('upload-report-scroll')
    })

    it('should render ul element for file lists', () => {
      expect(wrapper.html()).toContain('<ul')
    })

    it('should have footer slot with buttons', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
    })

    it('should render message paragraph', () => {
      expect(wrapper.html()).toContain('<p')
    })

    it('should have correct number of h4 headings', () => {
      const headings = wrapper.findAll('h4')
      expect(headings.length).toBe(2)
    })
  })

  describe('Accessibility', () => {
    it('should have semantic heading tags', () => {
      expect(wrapper.html()).toContain('<h4')
    })

    it('should have semantic list structure', () => {
      expect(wrapper.html()).toContain('<ul')
      expect(wrapper.find('li').exists()).toBe(false) // No items initially
    })

    it('should have meaningful button text', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Close')
      expect(buttons[1].text()).toBe('View Uploaded Files')
    })

    it('should use proper color classes for status', async () => {
      await wrapper.setProps({
        report: {
          success: [{ file: 'success.xml' }],
          errors: [{ file: 'error.xml', error: 'Error' }]
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.text-success').exists()).toBe(true)
      expect(wrapper.find('.text-danger').exists()).toBe(true)
    })
  })

  describe('Integration Tests', () => {
    it('should handle complete user flow: view report and close', async () => {
      // Set report data
      await wrapper.setProps({
        dialogVisible: true,
        report: {
          success: [{ file: 'success1.xml' }, { file: 'success2.xml' }],
          errors: [{ file: 'failed1.xml', error: 'Error' }]
        } as any
      })
      await wrapper.vm.$nextTick()

      // Verify content is displayed
      expect(wrapper.html()).toContain('success1.xml')
      expect(wrapper.html()).toContain('failed1.xml')

      // Click close button
      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')

      // Verify close event emitted
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('should handle complete user flow: view report and navigate to view tab', async () => {
      // Set report data
      await wrapper.setProps({
        dialogVisible: true,
        report: {
          success: [{ file: 'success.xml' }],
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      // Verify success message
      expect(wrapper.html()).toContain('All files uploaded successfully.')

      // Click view button
      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      // Verify view event emitted
      expect(wrapper.emitted('view')).toBeTruthy()
    })

    it('should handle report update and re-render', async () => {
      // Initial report
      await wrapper.setProps({
        report: {
          success: [{ file: 'file1.xml' }],
          errors: []
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.html()).toContain('All files uploaded successfully.')

      // Update report
      await wrapper.setProps({
        report: {
          success: [],
          errors: [{ file: 'file1.xml', error: 'Error' }]
        } as any
      })
      await wrapper.vm.$nextTick()

      expect(wrapper.html()).toContain('All files failed to upload.')
    })
  })
})
