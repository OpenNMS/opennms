import { ConfigurationHelper } from '../src/components/Configuration/ConfigurationHelper'
import { RequisitionTypes } from '../src/components/Configuration/copy/requisitionTypes'
import { test, expect } from 'vitest'
import { LocalConfiguration } from '@/components/Configuration/configuration.types'

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
  expect(ConfigurationHelper.convertItemToURL(itemPathQueryString)).toEqual('https://aa/path?key1=val1&key2=val2&key3=val3')
})
