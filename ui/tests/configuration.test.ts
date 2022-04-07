import { ConfigurationHelper } from '../src/components/Configuration/ConfigurationHelper'
import { RequisitionTypes } from '../src/components/Configuration/copy/requisitionTypes'
import { test } from 'uvu'
import * as assert from 'uvu/assert'
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

  assert.equal(ConfigurationHelper.convertItemToURL(itemAdvancedOptions), 'https://aa?key=val')
  assert.equal(ConfigurationHelper.convertItemToURL(itemEmptyAdvancedOptions), 'https://aa')
  assert.equal(ConfigurationHelper.convertItemToURL(itemAdvancedOptionsNameOnly), 'https://aa')
  assert.equal(ConfigurationHelper.convertItemToURL(itemAdvancedOptionsValueOnly), 'https://aa')
  assert.equal(ConfigurationHelper.convertItemToURL(itemMultipleAdvancedOptions), 'https://aa?key1=val1&key2=val2')
  assert.equal(ConfigurationHelper.convertItemToURL(itemPathQueryString), 'https://aa/path?key1=val1&key2=val2&key3=val3')
})

test.run()
