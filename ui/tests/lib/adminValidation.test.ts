import { describe, expect, it } from 'vitest'
import {
  isPathAddressable,
  validateAdminComments,
  validateAdminName,
  validateEmailShape
} from '@/lib/adminValidation'

describe('validateAdminName', () => {
  it('accepts ordinary names and empty values', () => {
    expect(validateAdminName('NOC-Duty_1', 'group name')).toBeNull()
    expect(validateAdminName('', 'group name')).toBeNull()
    expect(validateAdminName('   ', 'group name')).toBeNull()
  })

  it('rejects whitespace, markup and URL-hostile characters', () => {
    for (const bad of ['Test Group', 'a<b', 'a&b', 'a"b', "a'b", 'a`b', 'a:b', 'a/b', 'a\\b', 'a%b', 'a?b', 'a#b']) {
      expect(validateAdminName(bad, 'group name'), bad).toContain('must not contain')
    }
  })

  it('rejects dot segments', () => {
    expect(validateAdminName('.', 'user-id')).toContain('dot segment')
    expect(validateAdminName('..', 'user-id')).toContain('dot segment')
  })

  it('names the field in the message', () => {
    expect(validateAdminName('a b', 'role name')).toContain('role name')
  })
})

describe('validateAdminComments', () => {
  it('accepts plain text and empty values', () => {
    expect(validateAdminComments('The administrators, on shift 24/7.')).toBeNull()
    expect(validateAdminComments('')).toBeNull()
  })

  it('rejects markup characters', () => {
    for (const bad of ['<b>x</b>', 'a & b', 'quote "x"', "it's", 'tick `x`']) {
      expect(validateAdminComments(bad), bad).not.toBeNull()
    }
  })
})

describe('validateEmailShape', () => {
  it('accepts empty values and common deliverable forms', () => {
    expect(validateEmailShape('', 'email')).toBeNull()
    expect(validateEmailShape('noc@example.org', 'email')).toBeNull()
    expect(validateEmailShape('Bill Smith <bill@example.com>', 'email')).toBeNull()
    expect(validateEmailShape('a@example.com, b@example.com', 'email')).toBeNull()
  })

  it('rejects values without a local@domain part', () => {
    expect(validateEmailShape('not-an-email', 'email')).toContain('email')
    expect(validateEmailShape('a@', 'email')).not.toBeNull()
    expect(validateEmailShape('a@example.com,,b@example.com', 'pager email')).toContain('pager email')
  })
})

describe('isPathAddressable', () => {
  it('allows ordinary names', () => {
    expect(isPathAddressable('NOC-Duty')).toBe(true)
    expect(isPathAddressable('Some Group')).toBe(true)
  })

  it('flags names the security filter cannot address as path segments', () => {
    expect(isPathAddressable('NOC/Primary')).toBe(false)
    expect(isPathAddressable('a\\b')).toBe(false)
    expect(isPathAddressable('a%b')).toBe(false)
  })
})
