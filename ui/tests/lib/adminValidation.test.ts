import { describe, expect, it } from 'vitest'
import {
  isPathAddressable,
  validateAdminComments,
  validateAdminName,
  validateEmailShape,
  validatePasswordConfirmation,
  validateReservedName
} from '@/lib/adminValidation'
import { PROTECTED_USER_IDS } from '@/types/userAdmin'

describe('validateAdminName', () => {
  it('accepts ordinary names and empty values', () => {
    expect(validateAdminName('NOC-Duty_1', 'group name')).toBeNull()
    expect(validateAdminName('', 'group name')).toBeNull()
    expect(validateAdminName('   ', 'group name')).toBeNull()
  })

  it('rejects whitespace, markup and URL-hostile characters', () => {
    for (const bad of ['Test Group', 'a<b', 'a&b', 'a"b', 'a\'b', 'a`b', 'a:b', 'a/b', 'a\\b', 'a%b', 'a?b', 'a#b']) {
      expect(validateAdminName(bad, 'group name'), bad).toContain('must not contain')
    }
  })

  it('rejects other whitespace and invisible characters', () => {
    for (const bad of ['Ops\u00A0x', 'Ops\u2003x', 'a\u200Bb', 'a\u0000b', 'a\u2028b']) {
      expect(validateAdminName(bad, 'group name'), bad).toContain('must not contain')
    }
    expect(validateAdminName('Müller', 'user-id')).toBeNull()
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
    for (const bad of ['<b>x</b>', 'a & b', 'quote "x"', 'it\'s', 'tick `x`']) {
      expect(validateAdminComments(bad), bad).not.toBeNull()
    }
  })
})

describe('validateAdminComments length', () => {
  it('accepts exactly 4096 characters and rejects one more, after trimming', () => {
    expect(validateAdminComments('a'.repeat(4096))).toBeNull()
    expect(validateAdminComments(`  ${'a'.repeat(4096)}  `)).toBeNull()
    expect(validateAdminComments('a'.repeat(4097))).toBe('The comments must not be longer than 4096 characters.')
  })

  it('counts code points like the server, not UTF-16 units', () => {
    // 4096 emoji are 8192 UTF-16 units but 4096 characters
    expect(validateAdminComments('\u{1F600}'.repeat(4096))).toBeNull()
    expect(validateAdminComments('\u{1F600}'.repeat(4097))).not.toBeNull()
  })
})

describe('validatePasswordConfirmation', () => {
  it('reports a typed confirmation that differs, and nothing before one is typed', () => {
    expect(validatePasswordConfirmation('secret', 'secrex')).toBe('Passwords do not match')
    expect(validatePasswordConfirmation('', 'x')).toBe('Passwords do not match')
    expect(validatePasswordConfirmation('secret', '')).toBeNull()
    expect(validatePasswordConfirmation('secret', 'secret')).toBeNull()
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

  it('rejects the dot segments the browser folds into the collection URL', () => {
    expect(isPathAddressable('.')).toBe(false)
    expect(isPathAddressable('..')).toBe(false)
    expect(isPathAddressable('.hidden')).toBe(true)
  })

  it('flags names the security filter cannot address as path segments', () => {
    expect(isPathAddressable('NOC/Primary')).toBe(false)
    expect(isPathAddressable('a\\b')).toBe(false)
    expect(isPathAddressable('a%b')).toBe(false)
  })
})

describe('validateReservedName', () => {
  it('refuses a look-alike of a reserved name, using the trimmed value', () => {
    expect(validateReservedName('ADMIN', PROTECTED_USER_IDS, 'user-id')).toBe('The user-id "ADMIN" is reserved.')
    expect(validateReservedName(' rTC ', PROTECTED_USER_IDS, 'user-id')).toBe('The user-id "rTC" is reserved.')
    expect(validateReservedName('admin', ['Admin'], 'group name')).toBe('The group name "admin" is reserved.')
  })

  it('accepts the exact name, other names and empty values', () => {
    expect(validateReservedName('admin', PROTECTED_USER_IDS, 'user-id')).toBeNull()
    expect(validateReservedName('administrator', PROTECTED_USER_IDS, 'user-id')).toBeNull()
    expect(validateReservedName('Admin', ['Admin'], 'group name')).toBeNull()
    expect(validateReservedName('', PROTECTED_USER_IDS, 'user-id')).toBeNull()
  })
})
