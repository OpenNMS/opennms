import { ConfigurationHelper } from '../src/components/Configuration/ConfigurationHelper'
import { RequisitionTypes } from '../src/components/Configuration/copy/requisitionTypes'
import { test, expect } from 'vitest'
import { LocalConfiguration } from '@/components/Configuration/configuration.types'
import { ErrorStrings } from '@/components/Configuration/copy/requisitionTypes'

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
  expect(ConfigurationHelper.validateHost('192.168.31.1301')).toEqual(ErrorStrings.InvalidHostname)
  expect(ConfigurationHelper.validateHost('.com')).toEqual(ErrorStrings.InvalidHostname)
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
