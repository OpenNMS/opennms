import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import TrapdConfiguration from '@/containers/TrapdConfiguration.vue'
import { validateTrapdXml } from '@/lib/trapdValidator'
import { uploadTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useMenuStore } from '@/stores/menuStore'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: showSnackBarMock
  })
}))

vi.mock('@/lib/trapdValidator', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/trapdValidator')>()
  return {
    ...actual,
    validateTrapdXml: vi.fn()
  }
})

vi.mock('@/services/trapdConfigurationService', () => ({
  uploadTrapdConfiguration: vi.fn()
}))

describe('TrapdConfiguration.vue', () => {
  let trapStore: ReturnType<typeof useTrapdConfigStore>
  let menuStore: ReturnType<typeof useMenuStore>

  const validateTrapdXmlMock = vi.mocked(validateTrapdXml)
  const uploadTrapdConfigurationMock = vi.mocked(uploadTrapdConfiguration)

  const mountComponent = () => {
    return mount(TrapdConfiguration, {
      global: {
        stubs: {
          GeneralConfiguration: true,
          SnmpV3UserManagement: true,
          CreateSnmpV3User: true,
          FeatherTabContainer: {
            template: '<div><slot name="tabs" /><slot /></div>'
          },
          FeatherTab: {
            template: '<div><slot /></div>'
          },
          FeatherTabPanel: {
            template: '<div><slot /></div>'
          },
          FeatherButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          BreadCrumbs: true
        }
      }
    })
  }

  const createXmlFile = (name = 'trapd.xml', content = '<trapd-configuration />') => {
    const file = new File([content], name, { type: 'text/xml' })
    vi.spyOn(file, 'text').mockResolvedValue(content)
    return file
  }

  const triggerUpload = async (wrapper: ReturnType<typeof mountComponent>, file?: File) => {
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: file ? [file] : [],
      configurable: true
    })
    await input.trigger('change')
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    trapStore = useTrapdConfigStore()
    menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home' } as any
    trapStore.fetchTrapConfig = vi.fn().mockResolvedValue(undefined)
    validateTrapdXmlMock.mockReturnValue({ valid: true, errors: [] })
    uploadTrapdConfigurationMock.mockResolvedValue(undefined)
  })

  it('renders heading and child sections', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h1').text()).toBe('Trap Listener Configuration')
    expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    expect(wrapper.find('input[type="file"]').exists()).toBe(true)
  })

  it('renders breadcrumbs with home and trap configuration entries', () => {
    const wrapper = mountComponent()
    const breadcrumbs = wrapper.findComponent(BreadCrumbs)
    const items = breadcrumbs.props('items')

    expect(items).toHaveLength(2)
    expect(items[0]).toEqual({ label: 'Home', to: '/home', isAbsoluteLink: true })
    expect(items[1]).toEqual({ label: 'Trap Listener Configuration', to: '#', position: 'last' })
  })

  it('calls fetchTrapConfig on mount', () => {
    mountComponent()
    expect(trapStore.fetchTrapConfig).toHaveBeenCalledTimes(1)
  })

  it('shows snackbar when initial fetch fails with Error', async () => {
    trapStore.fetchTrapConfig = vi.fn().mockRejectedValue(new Error('initial fetch failed'))

    mountComponent()
    await Promise.resolve()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'initial fetch failed', error: true })
  })

  it('shows snackbar when initial fetch fails with non-Error', async () => {
    trapStore.fetchTrapConfig = vi.fn().mockRejectedValue('boom')

    mountComponent()
    await Promise.resolve()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to retrieve trapd configuration.', error: true })
  })

  it('opens file chooser when upload button is clicked', async () => {
    const wrapper = mountComponent()
    const input = wrapper.find('input[type="file"]').element as HTMLInputElement
    const clickSpy = vi.spyOn(input, 'click').mockImplementation(() => undefined)

    const uploadButton = wrapper.findAll('button').find((button) => button.text().includes('Upload Configuration'))
    expect(uploadButton).toBeDefined()

    await uploadButton!.trigger('click')
    expect(clickSpy).toHaveBeenCalledTimes(1)
  })

  it('returns early when no file is selected', async () => {
    const wrapper = mountComponent()
    await triggerUpload(wrapper)

    expect(validateTrapdXmlMock).not.toHaveBeenCalled()
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('rejects non-xml files before validation', async () => {
    const wrapper = mountComponent()
    const invalidFile = createXmlFile('trapd.txt', 'not xml')

    await triggerUpload(wrapper, invalidFile)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Only .xml files are supported.', error: true })
    expect(validateTrapdXmlMock).not.toHaveBeenCalled()
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('shows read error when file.text fails', async () => {
    const wrapper = mountComponent()
    const file = new File(['x'], 'trapd.xml', { type: 'text/xml' })
    vi.spyOn(file, 'text').mockRejectedValue(new Error('read failed'))

    await triggerUpload(wrapper, file)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to read XML file.', error: true })
    expect(validateTrapdXmlMock).not.toHaveBeenCalled()
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('blocks upload and shows validation errors when XML is invalid (<=3 errors)', async () => {
    const wrapper = mountComponent()
    const file = createXmlFile('trapd.xml', '<invalid />')
    validateTrapdXmlMock.mockReturnValue({
      valid: false,
      errors: [
        { field: 'root', message: 'Root mismatch' },
        { field: 'xmlns', message: 'Namespace invalid' },
        { field: 'snmp-trap-port', message: 'Port invalid' }
      ]
    })

    await triggerUpload(wrapper, file)

    expect(validateTrapdXmlMock).toHaveBeenCalledWith('<invalid />')
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Invalid trap configuration XML: Root mismatch | Namespace invalid | Port invalid',
      error: true
    })
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('shows +N more suffix for validation errors beyond first 3', async () => {
    const wrapper = mountComponent()
    const file = createXmlFile('trapd.xml', '<invalid />')
    validateTrapdXmlMock.mockReturnValue({
      valid: false,
      errors: [
        { field: 'a', message: 'e1' },
        { field: 'b', message: 'e2' },
        { field: 'c', message: 'e3' },
        { field: 'd', message: 'e4' },
        { field: 'e', message: 'e5' }
      ]
    })

    await triggerUpload(wrapper, file)

    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Invalid trap configuration XML: e1 | e2 | e3 (+2 more)',
      error: true
    })
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('uploads file and refreshes store when XML is valid', async () => {
    const wrapper = mountComponent()
    const file = createXmlFile('trapd.xml', '<trapd-configuration />')

    await triggerUpload(wrapper, file)

    expect(validateTrapdXmlMock).toHaveBeenCalledWith('<trapd-configuration />')
    expect(uploadTrapdConfigurationMock).toHaveBeenCalledWith(file)
    expect(trapStore.fetchTrapConfig).toHaveBeenCalledTimes(2)
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Trap configuration uploaded successfully.' })
  })

  it('shows upload error message from Error instance', async () => {
    const wrapper = mountComponent()
    const file = createXmlFile('trapd.xml', '<trapd-configuration />')
    uploadTrapdConfigurationMock.mockRejectedValue(new Error('upload failed'))

    await triggerUpload(wrapper, file)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'upload failed', error: true })
  })

  it('shows generic upload error message for non-Error throw', async () => {
    const wrapper = mountComponent()
    const file = createXmlFile('trapd.xml', '<trapd-configuration />')
    uploadTrapdConfigurationMock.mockRejectedValue('bad')

    await triggerUpload(wrapper, file)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to upload trap configuration.', error: true })
  })
})
