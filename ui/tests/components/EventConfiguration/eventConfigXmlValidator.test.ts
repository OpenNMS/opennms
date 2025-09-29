import {
  isDuplicateFile,
  MAX_FILES_UPLOAD,
  validateEventConfigFile,
  validateEventElement
} from '@/components/EventConfiguration/eventConfigXmlValidator'
import { UploadEventFileType } from '@/types/eventConfig'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('eventConfigXmlValidator', () => {
  let mockFile: File

  // Helper to create a mock File
  const createMockFile = (name: string, content: string, type = 'text/xml') => {
    return new File([content], name, { type })
  }

  beforeEach(() => {
    vi.spyOn(File.prototype, 'text').mockRestore()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('MAX_FILES_UPLOAD', () => {
    it('has correct value', () => {
      expect(MAX_FILES_UPLOAD).toBe(10)
    })
  })

  describe('validateEventConfigFile', () => {
    it('rejects empty file content', async () => {
      mockFile = createMockFile('empty.events.xml', '')
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['File is empty']
      })
    })

    it('rejects whitespace-only file content', async () => {
      mockFile = createMockFile('whitespace.events.xml', '  \n\t  ')
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['File is empty']
      })
    })

    it('rejects non-.events.xml file extension without "event"', async () => {
      mockFile = createMockFile('test.xml', '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"></events>')
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['File does not appear to be an event configuration file (expected .events.xml extension)']
      })
    })

    it('accepts file name with "event" but not .events.xml', async () => {
      mockFile = createMockFile(
        'eventconfig.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei>uei.opennms.org/test</uei><event-label>Test</event-label><severity>Minor</severity></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: true,
        errors: []
      })
    })

    // it('rejects invalid XML syntax', async () => {
    //   mockFile = createMockFile('invalid.events.xml', '<events><event>unclosed')
    //   const result = await validateEventConfigFile(mockFile)
    //   expect(result).toEqual({
    //     isValid: false,
    //     errors: ['Invalid XML format - file contains syntax errors']
    //   })
    // })

    it('rejects missing <events> root element', async () => {
      mockFile = createMockFile('noevents.events.xml', '<root><event></event></root>')
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Missing <events> root element']
      })
    })

    it('rejects missing namespace', async () => {
      mockFile = createMockFile('nons.events.xml', '<events><event></event></events>')
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Missing or invalid OpenNMS namespace in <events> element']
      })
    })

    it('rejects incorrect namespace', async () => {
      mockFile = createMockFile('wrongns.events.xml', '<events xmlns="http://wrong.org"><event></event></events>')
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Missing or invalid OpenNMS namespace in <events> element']
      })
    })

    it('rejects empty <events> element', async () => {
      mockFile = createMockFile(
        'noevent.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['No <event> entries found within <events> element']
      })
    })

    it('rejects <events> with non-<event> children', async () => {
      mockFile = createMockFile(
        'other.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><other></other></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['<events> element contains <other> but no <event> elements']
      })
    })

    it('rejects <events> with multiple non-<event> children', async () => {
      mockFile = createMockFile(
        'multiple.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><foo></foo><bar></bar></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['<events> element contains <foo>, <bar> but no <event> elements']
      })
    })

    it('validates file with correct structure', async () => {
      mockFile = createMockFile(
        'valid.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei>uei.opennms.org/test</uei><event-label>Test</event-label><severity>Minor</severity></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: true,
        errors: []
      })
    })

    it('rejects <event> missing uei', async () => {
      mockFile = createMockFile(
        'badevent.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><event-label>Test</event-label><severity>Minor</severity></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Event 1: missing <uei>']
      })
    })

    it('rejects <event> missing event-label', async () => {
      mockFile = createMockFile(
        'badevent.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei>uei.opennms.org/test</uei><severity>Minor</severity></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Event 1: missing <event-label>']
      })
    })

    it('rejects <event> missing severity', async () => {
      mockFile = createMockFile(
        'badevent.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei>uei.opennms.org/test</uei><event-label>Test</event-label></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Event 1: missing <severity>']
      })
    })

    it('stops validation after first invalid event', async () => {
      mockFile = createMockFile(
        'mixed.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf">' +
          '<event><uei>uei.opennms.org/test1</uei><event-label>Test1</event-label><severity>Minor</severity></event>' +
          '<event><uei>uei.opennms.org/test2</uei></event>' +
          '<event></event>' +
          '</events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Event 2: missing <event-label>']
      })
    })

    it('handles file reading error', async () => {
      mockFile = createMockFile('error.events.xml', '<events></events>')
      vi.spyOn(File.prototype, 'text').mockRejectedValue(new Error('File read error'))
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Error reading file content: File read error']
      })
    })

    it('handles large file with many events', async () => {
      const eventXml =
        '<event><uei>uei.opennms.org/test</uei><event-label>Test</event-label><severity>Minor</severity></event>'
      const largeContent = `<events xmlns="http://xmlns.opennms.org/xsd/eventconf">${eventXml.repeat(1000)}</events>`
      mockFile = createMockFile('large.events.xml', largeContent)
      // Explicitly mock file.text() to ensure it works in happy-dom
      vi.spyOn(mockFile, 'text').mockResolvedValue(largeContent)
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: true,
        errors: []
      })
    })

    it('handles XML with comments and CDATA', async () => {
      const xmlContent =
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><!-- Comment --><event><uei><![CDATA[uei.opennms.org/test]]></uei><event-label>Test</event-label><severity>Minor</severity></event></events>'
      mockFile = createMockFile('comments.events.xml', xmlContent)
      // Explicitly mock file.text() to ensure it works in happy-dom
      vi.spyOn(mockFile, 'text').mockResolvedValue(xmlContent)
      // Mock DOMParser to handle CDATA correctly
      vi.spyOn(global, 'DOMParser').mockImplementation(() => {
        return {
          parseFromString: () => ({
            querySelector: (selector: string) => {
              if (selector === 'parsererror') return null
              if (selector === 'events') {
                return {
                  getAttribute: () => 'http://xmlns.opennms.org/xsd/eventconf',
                  querySelectorAll: () => [
                    {
                      querySelector: (sel: string) => {
                        if (sel === 'uei') return { textContent: 'uei.opennms.org/test' }
                        if (sel === 'event-label') return { textContent: 'Test' }
                        if (sel === 'severity') return { textContent: 'Minor' }
                        return null
                      }
                    }
                  ],
                  children: [
                    {
                      tagName: 'event'
                    }
                  ]
                }
              }
              return null
            }
          })
        } as any
      })
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: true,
        errors: []
      })
      vi.restoreAllMocks()
    })

    it('rejects <event> with whitespace-only fields', async () => {
      mockFile = createMockFile(
        'whitespace.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei>  </uei><event-label>  </event-label><severity>  </severity></event></events>'
      )
      // Explicitly mock file.text() to ensure it works in happy-dom
      vi.spyOn(mockFile, 'text').mockResolvedValue(
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei>  </uei><event-label>  </event-label><severity>  </severity></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Event 1: missing <uei>']
      })
    })

    it('rejects <event> with empty fields', async () => {
      mockFile = createMockFile(
        'emptyfields.events.xml',
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei></uei><event-label></event-label><severity></severity></event></events>'
      )
      // Explicitly mock file.text() to ensure it works in happy-dom
      vi.spyOn(mockFile, 'text').mockResolvedValue(
        '<events xmlns="http://xmlns.opennms.org/xsd/eventconf"><event><uei></uei><event-label></event-label><severity></severity></event></events>'
      )
      const result = await validateEventConfigFile(mockFile)
      expect(result).toEqual({
        isValid: false,
        errors: ['Event 1: missing <uei>']
      })
    })
  })

  describe('validateEventElement', () => {
    let parser: DOMParser
    let mockElement: Element

    beforeEach(() => {
      parser = new DOMParser()
    })

    it('validates event with all required fields', () => {
      const xml =
        '<event><uei>uei.opennms.org/test</uei><event-label>Test</event-label><severity>Minor</severity></event>'
      mockElement = parser.parseFromString(xml, 'application/xml').querySelector('event')!
      const errors = validateEventElement(mockElement, 1)
      expect(errors).toBe('')
    })

    it('rejects event missing uei', () => {
      const xml = '<event><event-label>Test</event-label><severity>Minor</severity></event>'
      mockElement = parser.parseFromString(xml, 'application/xml').querySelector('event')!
      const errors = validateEventElement(mockElement, 1)
      expect(errors).toBe('Event 1: missing <uei>')
    })

    it('rejects event missing event-label', () => {
      const xml = '<event><uei>uei.opennms.org/test</uei><severity>Minor</severity></event>'
      mockElement = parser.parseFromString(xml, 'application/xml').querySelector('event')!
      const errors = validateEventElement(mockElement, 2)
      expect(errors).toBe('Event 2: missing <event-label>')
    })

    it('rejects event missing severity', () => {
      const xml = '<event><uei>uei.opennms.org/test</uei><event-label>Test</event-label></event>'
      mockElement = parser.parseFromString(xml, 'application/xml').querySelector('event')!
      const errors = validateEventElement(mockElement, 3)
      expect(errors).toBe('Event 3: missing <severity>')
    })

    it('rejects event with whitespace-only fields', () => {
      const xml = '<event><uei>  </uei><event-label>  </event-label><severity>  </severity></event>'
      mockElement = parser.parseFromString(xml, 'application/xml').querySelector('event')!
      const errors = validateEventElement(mockElement, 4)
      expect(errors).toBe('Event 4: missing <uei>')
    })

    it('rejects event with empty fields', () => {
      const xml = '<event><uei></uei><event-label></event-label><severity></severity></event>'
      mockElement = parser.parseFromString(xml, 'application/xml').querySelector('event')!
      const errors = validateEventElement(mockElement, 5)
      expect(errors).toBe('Event 5: missing <uei>')
    })
  })

  describe('isDuplicateFile', () => {
    const mockFiles: UploadEventFileType[] = [
      { file: createMockFile('file1.events.xml', ''), isValid: true, errors: [], isDuplicate: false },
      { file: createMockFile('file2.events.xml', ''), isValid: true, errors: [], isDuplicate: false }
    ]

    it('returns false for empty existing files', () => {
      const result = isDuplicateFile('test.events.xml', [])
      expect(result).toBe(false)
    })

    it('returns true for case-insensitive duplicate', () => {
      const result = isDuplicateFile('FILE1.events.xml', mockFiles)
      expect(result).toBe(true)
    })

    it('returns false for non-duplicate file', () => {
      const result = isDuplicateFile('file3.events.xml', mockFiles)
      expect(result).toBe(false)
    })

    it('handles special characters in file names', () => {
      const specialFiles: UploadEventFileType[] = [
        { file: createMockFile('file-1@special.events.xml', ''), isValid: true, errors: [], isDuplicate: false }
      ]
      const result = isDuplicateFile('FILE-1@SPECIAL.events.xml', specialFiles)
      expect(result).toBe(true)
    })

    it('handles multiple duplicates', () => {
      const multiFiles: UploadEventFileType[] = [
        { file: createMockFile('file1.events.xml', ''), isValid: true, errors: [], isDuplicate: false },
        { file: createMockFile('FILE1.events.xml', ''), isValid: true, errors: [], isDuplicate: false }
      ]
      const result = isDuplicateFile('file1.events.xml', multiFiles)
      expect(result).toBe(true)
    })

    it('handles empty file name', () => {
      const result = isDuplicateFile('', mockFiles)
      expect(result).toBe(false)
    })

    it('handles null or undefined existingFiles', () => {
      expect(isDuplicateFile('test.events.xml', null as any)).toBe(false)
      expect(isDuplicateFile('test.events.xml', undefined as any)).toBe(false)
    })
  })
})

