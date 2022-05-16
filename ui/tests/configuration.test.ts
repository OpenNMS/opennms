import { mount } from '@vue/test-utils'
import store from '@/store'
import { ConfigurationHelper } from '../src/components/Configuration/ConfigurationHelper'
import { RequisitionTypes, RequisitionData, ErrorStrings } from '../src/components/Configuration/copy/requisitionTypes'
import { test, expect } from 'vitest'
import { LocalConfiguration, ProvisionDServerConfiguration } from '@/components/Configuration/configuration.types'
import ConfigurationTable from '@/components/Configuration/ConfigurationTable.vue'

const mockRequisitionProvisionDServiceConfig = {
  [RequisitionData.ImportName]: 'test',
  [RequisitionData.ImportURL]: 'requisition://dns?host=test',
  [RequisitionData.CronSchedule]: '0 0 0 * * ?'
} as ProvisionDServerConfiguration

const mockHttpProvisionDServiceConfig = {
  [RequisitionData.ImportName]: 'test',
  [RequisitionData.ImportURL]: 'https://aa?key=val',
  [RequisitionData.CronSchedule]: '0 0 0 * * ?'
} as ProvisionDServerConfiguration

const mockProps = {
  itemList: [mockHttpProvisionDServiceConfig],
  editClicked: () => '',
  deleteClicked: () => '',
  setNewPage: () => ''
}

const wrapper = mount(ConfigurationTable, {
  global: {
    plugins: [store]
  },
  propsData: mockProps
})

test('Convert item to URL query string', () => {
  const itemEmptyAdvancedOptions = {
    name: 'Test',
    type: { name: RequisitionTypes.HTTPS },
    host: 'aa',
    urlPath: '',
    advancedOptions: [
      {
        key: { _text: '', name: '' },
        value: ''
      }
    ]
  } as LocalConfiguration

  const itemAdvancedOptions = {
    ...itemEmptyAdvancedOptions,
    advancedOptions: [
      {
        key: { _text: 'key', name: 'key' },
        value: 'val'
      }
    ]
  } as LocalConfiguration

  const itemAdvancedOptionsNameOnly = {
    ...itemEmptyAdvancedOptions,
    advancedOptions: [
      {
        key: { _text: '', name: 'name' },
        value: ''
      }
    ]
  } as LocalConfiguration

  const itemAdvancedOptionsValueOnly = {
    ...itemEmptyAdvancedOptions,
    advancedOptions: [
      {
        key: { _text: '', name: '' },
        value: 'val'
      }
    ]
  } as LocalConfiguration

  const itemMultipleAdvancedOptions = {
    ...itemEmptyAdvancedOptions,
    advancedOptions: [
      {
        key: { _text: '', name: 'key1' },
        value: 'val1'
      },
      {
        key: { _text: '', name: 'key2' },
        value: 'val2'
      }
    ]
  } as LocalConfiguration

  const itemPathQueryString = {
    ...itemEmptyAdvancedOptions,
    urlPath: '/path?key1=val1',
    advancedOptions: [
      {
        key: { _text: '', name: 'key2' },
        value: 'val2'
      },
      {
        key: { _text: '', name: 'key3' },
        value: 'val3'
      }
    ]
  } as LocalConfiguration

  expect(ConfigurationHelper.convertItemToURL(itemAdvancedOptions)).toEqual('https://aa?key=val')
  expect(ConfigurationHelper.convertItemToURL(itemEmptyAdvancedOptions)).toEqual('https://aa')
  expect(ConfigurationHelper.convertItemToURL(itemAdvancedOptionsNameOnly)).toEqual('https://aa')
  expect(ConfigurationHelper.convertItemToURL(itemAdvancedOptionsValueOnly)).toEqual('https://aa')
  expect(ConfigurationHelper.convertItemToURL(itemMultipleAdvancedOptions)).toEqual('https://aa?key1=val1&key2=val2')
  expect(ConfigurationHelper.convertItemToURL(itemPathQueryString)).toEqual(
    'https://aa/path?key1=val1&key2=val2&key3=val3'
  )
})

test('Validate host/zone fn should allow ipv4, ipv6, and domains', () => {
  expect(ConfigurationHelper.validateHost('192.168.31.130')).toEqual('')
  expect(ConfigurationHelper.validateHost('2345:0425:2CA1:0000:0000:0567:5673:23b5')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain.xyz')).toEqual('')
  expect(ConfigurationHelper.validateHost('my.best.domain.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('domaindotcom')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain-dotcom')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain123-com')).toEqual('')
  expect(ConfigurationHelper.validateHost('my123.domain-123.com123')).toEqual('')
  expect(ConfigurationHelper.validateHost('my-123-domain.com-123')).toEqual('')
  expect(ConfigurationHelper.validateHost('123my.do-main.com')).toEqual('')

  // cannot start with hyphen
  expect(ConfigurationHelper.validateHost('-domaindotcom')).toEqual(ErrorStrings.InvalidHostname)
  // cannot contain space
  expect(ConfigurationHelper.validateHost('domain com')).toEqual(ErrorStrings.InvalidHostname)
  // cannot be over 49 characters unless valid domain
  const longHostname = 'testalonghostnameover49characterslongtestalonghostn'
  expect(ConfigurationHelper.validateHost(longHostname)).toEqual(ErrorStrings.InvalidHostname)
})

test('The HTTP/S type config path does not contain params', () => {
  const httpUrlIn = 'http://abc.xyz/mypath?key1=key2'
  const httpsUrlIn = 'https://abc.xyz/mypath?key1=key2'

  const httpRes = ConfigurationHelper.convertURLToLocal(httpUrlIn)
  expect(httpRes.urlPath).toEqual('/mypath')

  const httpsRes = ConfigurationHelper.convertURLToLocal(httpsUrlIn)
  expect(httpsRes.urlPath).toEqual('/mypath')
})

test('The File type config path keeps params', () => {
  const fileUrlIn = 'file:///mypath?key1=key2'
  const res = ConfigurationHelper.convertURLToLocal(fileUrlIn)
  expect(res.path).toEqual('/mypath?key1=key2')
})

test('The edit btn disables if the record starts with "requisition://"', async () => {
  const editBtn = wrapper.get('[data-test="edit-btn"]')
  // expect edit btn to be enabled
  expect(editBtn.attributes('aria-disabled')).toBeUndefined()

  // update props with requisition type url
  const newProps = { ...mockProps, itemList: [mockRequisitionProvisionDServiceConfig] }
  await wrapper.setProps(newProps)

  // expect edit btn to be disabled
  expect(editBtn.attributes('aria-disabled')).toBe('true')
})

test('Display appropriate form errors', async () => {
  const mockLocalConfig = {
    username: 'test',
    password: '',
    type: { name: 'VMware', id: 1 },
    occurance: { name: '' }
  } as LocalConfiguration

  let errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  // expect password input error
  expect(errors.password).toBe(ErrorStrings.Password)
  expect(errors.username).toBe('')

  // update form props
  mockLocalConfig.username = ''
  mockLocalConfig.password = 'pass'

  // expect username input error
  errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  expect(errors.username).toBe(ErrorStrings.Username)
  expect(errors.password).toBe('')

  // update form props
  mockLocalConfig.username = ''
  mockLocalConfig.password = ''

  // expect no errors if fields left blank
  errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  expect(errors.username).toBe('')
  expect(errors.password).toBe('')
})
