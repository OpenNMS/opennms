///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { ConfigurationHelper } from '../src/components/Configuration/ConfigurationHelper'
import { RequisitionTypes, RequisitionData, ErrorStrings, VMWareFields } from '../src/components/Configuration/copy/requisitionTypes'
import { test, expect, describe, it } from 'vitest'
import { LocalConfiguration, ProvisionDServerConfiguration } from '@/components/Configuration/configuration.types'
import ConfigurationTable from '@/components/Configuration/ConfigurationTable.vue'
import ProvisionDConfig from '@/containers/ProvisionDConfig.vue'
import { findByText } from './utils'

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
    plugins: [createTestingPinia()],
    stubs: ['router-link']
  },
  propsData: mockProps
})

const provisionDConfig = mount(ProvisionDConfig, {
  global: {
    plugins: [createTestingPinia()],
    stubs: ['router-link']
  }
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

test('Validate host fn should allow ipv4, ipv6, and domains', () => {
  expect(ConfigurationHelper.validateHost('192.168.31.130')).toEqual('')
  expect(ConfigurationHelper.validateHost('2345:0425:2CA1:0000:0000:0567:5673:23b5')).toEqual('')
  expect(ConfigurationHelper.validateHost('[2345:0425:2CA1:0000:0000:0567:5673:23b5]')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain.xyz')).toEqual('')
  expect(ConfigurationHelper.validateHost('my.best.domain.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('domaindotcom')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain-dotcom')).toEqual('')
  expect(ConfigurationHelper.validateHost('domain123-com')).toEqual('')
  expect(ConfigurationHelper.validateHost('my123.domain-123.com123')).toEqual('')
  expect(ConfigurationHelper.validateHost('my-123-domain.com-123')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('123my.do-main.com')).toEqual('')

  expect(ConfigurationHelper.validateHost('user:pass@domain.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('admin:admin@33horizon.example.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('user:pass@192.168.1.10')).toEqual('')
  expect(ConfigurationHelper.validateHost('user:pass@[2345:0425:2CA1:0000:0000:0567:5673:23b5]')).toEqual('')
  expect(ConfigurationHelper.validateHost('admin:admin@33horizon.example.com:8980')).toEqual('')
  expect(ConfigurationHelper.validateHost('admin:admin@host-with-dashes.com')).toEqual('')

  expect(ConfigurationHelper.validateHost('domain.com:8080')).toEqual('')
  expect(ConfigurationHelper.validateHost('192.168.1.1:3000')).toEqual('')
  expect(ConfigurationHelper.validateHost('[2345:0425:2CA1:0000:0000:0567:5673:23b5]:443')).toEqual('')



  expect(ConfigurationHelper.validateHost('-domaindotcom')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('domain com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('bad@host:com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('user:@host')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost(':pass@host')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('http://user:pass@')).toEqual(ErrorStrings.InvalidHostname)

  expect(ConfigurationHelper.validateHost('domain..com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('domain@com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('com@domain.com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('user:pass@host:abc')).toEqual(ErrorStrings.InvalidHostname) // invalid port

  expect(ConfigurationHelper.validateHost('${scv:requisition:username}:${scv:requisition:password}@host.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('${scv:requisition:username}:${scv:requisition:password}@192.168.0.1')).toEqual('')
  expect(ConfigurationHelper.validateHost('${scv:requisition:username}:${scv:requisition:password}@[2001:db8::1]:443')).toEqual('')
  expect(ConfigurationHelper.validateHost('${scv:requisition:password}@example.com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('user:${scv:requisition:password}@host.com')).toEqual('')
  expect(ConfigurationHelper.validateHost('${scv:requisition:username}@example.com:8080')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('${scv:requisition:username}:${scv:requisition:password}@host-with-dashes.com:9090')).toEqual('')

  expect(ConfigurationHelper.validateHost('${scv:requisition:username}@host.com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('${scv:requisition:username}@')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('${scvuser}@host')).toEqual(ErrorStrings.InvalidHostname) // missing {}

  expect(ConfigurationHelper.validateHost('user@host:pass')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('user:pass@host:abc')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('user:pass@host:65536')).toEqual(ErrorStrings.InvalidHostname) // invalid port
  expect(ConfigurationHelper.validateHost('host:-1')).toEqual(ErrorStrings.InvalidHostname)

  expect(ConfigurationHelper.validateHost('host with space')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('host#.com')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('-hostname')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('hostname-')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('.hostname')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('hostname.')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('123.456.789.000')).toEqual(ErrorStrings.InvalidHostname) // not a real IP
})

describe('Zone field - validateZoneField()', () => {
  it('should be valid', () => {
    expect(ConfigurationHelper.validateZoneField('Zone-field_value .123')).toEqual('')
  })
  it('should be invalid', () => {
    expect(ConfigurationHelper.validateZoneField('')).toEqual(ErrorStrings.InvalidZoneName)
    expect(ConfigurationHelper.validateZoneField(' Zone-field_value .123')).toEqual(ErrorStrings.InvalidZoneName)
    expect(ConfigurationHelper.validateZoneField('Zone-field_value .123 ')).toEqual(ErrorStrings.InvalidZoneName)
    expect(ConfigurationHelper.validateZoneField('zone/')).toEqual(ErrorStrings.InvalidZoneName)
    expect(ConfigurationHelper.validateZoneField('[zone')).toEqual(ErrorStrings.InvalidZoneName)
    expect(ConfigurationHelper.validateZoneField('zone@')).toEqual(ErrorStrings.InvalidZoneName)
  })
})

describe('Requisition name field - validateRequisitionNameField()', () => {
  it('should be valid', () => {
    expect(ConfigurationHelper.validateRequisitionNameField('')).toEqual('')
    expect(ConfigurationHelper.validateRequisitionNameField('Requisition-name_field .123')).toEqual('')
  })
  it('should be invalid', () => {
    expect(ConfigurationHelper.validateRequisitionNameField(' Requisition-name_field .123')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('Requisition-name_field .123 ')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('/requisition')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('requisition/')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('requisition?')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('&requisition')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('requisition*')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('requisition\'')).toEqual(ErrorStrings.InvalidRequisitionName)
    expect(ConfigurationHelper.validateRequisitionNameField('requisition"')).toEqual(ErrorStrings.InvalidRequisitionName)
  })
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

test('Display appropriate form errors for VMware requisition', async () => {
  const mockLocalConfig = {
    username: 'test',
    password: '',
    type: { name: 'VMware', id: 1 },
    occurance: { name: '' }
  } as LocalConfiguration

  let errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)

  // expect host required error
  expect(errors.host).toBe(ErrorStrings.Required('Host'))

  // expect invalid requisition name error
  expect(errors.foreignSource).toBe(ErrorStrings.Required(VMWareFields.RequisitionName))

  mockLocalConfig.foreignSource = ' . '
  errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  expect(errors.foreignSource).toBe(ErrorStrings.InvalidRequisitionName)

  // expect password input error
  expect(errors.password).toBe(ErrorStrings.Required(VMWareFields.UpperPassword))
  expect(errors.username).toBe('')

  // update form props
  mockLocalConfig.username = ''
  mockLocalConfig.password = 'pass'

  // expect username input error
  errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  expect(errors.username).toBe(ErrorStrings.Required(VMWareFields.UpperUsername))
  expect(errors.password).toBe('')

  // update form props
  mockLocalConfig.username = ''
  mockLocalConfig.password = ''

  // expect no errors if fields left blank
  errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  expect(errors.username).toBe('')
  expect(errors.password).toBe('')

  // if host is invalid, expect error
  mockLocalConfig.host = '  '
  errors = ConfigurationHelper.validateLocalItem(mockLocalConfig, [], 1, false)
  expect(errors.host).toBe(ErrorStrings.InvalidHostname)
})

test('External sources is populated', async () => {
  const btn = provisionDConfig.get('[data-test="external-req-btn"]')
  await btn.trigger('click')
  const select = provisionDConfig.get('[data-test="external-source-select"] .feather-select-input')
  await select.trigger('click')
  const DNS = findByText(provisionDConfig, 'span', 'DNS')
  expect(DNS).toBeDefined()
})

test('Schedule Type is populated', async () => {
  const btn = provisionDConfig.get('[data-test="external-req-btn"]')
  await btn.trigger('click')
  const select = provisionDConfig.get('[data-test="schedule-type-select"] .feather-select-input')
  await select.trigger('click')
  const Monthly = findByText(provisionDConfig, 'span', 'Monthly')
  expect(Monthly).toBeDefined()
})
