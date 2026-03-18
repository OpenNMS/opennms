import { ISelectItemType } from '@featherds/select'

export const isValidIP = (ip: string): boolean => {
  const parts = ip.split('.')
  if (parts.length !== 4) return false
  return parts.every((part) => {
    const num = parseInt(part, 10)
    return !isNaN(num) && num >= 0 && num <= 255
  })
}

export const SECURITY_LEVEL_OPTIONS: ISelectItemType[] = [
  { _text: '1 - NoAuthNoPriv', _value: 1 },
  { _text: '2 - AuthNoPriv', _value: 2 },
  { _text: '3 - AuthPriv', _value: 3 }
]

export const PRIVACY_PROTOCOLS_OPTIONS: ISelectItemType[] = [
  { _text: 'DES', _value: 'DES' },
  { _text: 'AES', _value: 'AES' },
  { _text: 'AES192', _value: 'AES192' },
  { _text: 'AES256', _value: 'AES256' }
]

export const AUTH_PROTOCOLS_OPTIONS: ISelectItemType[] = [
  { _text: 'MD5', _value: 'MD5' },
  { _text: 'SHA', _value: 'SHA' },
  { _text: 'SHA224', _value: 'SHA224' },
  { _text: 'SHA256', _value: 'SHA256' },
  { _text: 'SHA512', _value: 'SHA512' }
]

