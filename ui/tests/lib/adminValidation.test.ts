import { describe, expect, it } from 'vitest'
import {
  validateOnCallRoleName,
  isPathAddressable,
  RESERVED_GROUP_NAMES,
  RESERVED_ONCALL_ROLE_NAMES,
  RESERVED_USER_IDS,
  validateAdminComments,
  validateAdminName,
  validateEmailShape,
  validatePasswordConfirmation,
  validateReservedName
} from '@/lib/adminValidation'
import { PROTECTED_USER_IDS } from '@/types/userAdmin'

describe('validateAdminName', () => {
  it('reserves the endpoints under /oncall-roles', () => {
    for (const reserved of ['.', '..', 'item', 'calendar', 'schedule-settings']) {
      expect(validateAdminName(reserved, 'on-call role name', RESERVED_ONCALL_ROLE_NAMES), reserved).toContain('is reserved')
    }
    expect(validateAdminName('Night', 'on-call role name', RESERVED_ONCALL_ROLE_NAMES)).toBeNull()
  })

  it('accepts ordinary names and empty values', () => {
    expect(validateAdminName('NOC-Duty_1', 'group name', RESERVED_GROUP_NAMES)).toBeNull()
    expect(validateAdminName('', 'group name', RESERVED_GROUP_NAMES)).toBeNull()
    expect(validateAdminName('   ', 'group name', RESERVED_GROUP_NAMES)).toBeNull()
  })

  it('rejects whitespace, markup and URL-hostile characters', () => {
    for (const bad of ['Test Group', 'a<b', 'a&b', 'a"b', 'a\'b', 'a`b', 'a:b', 'a/b', 'a\\b', 'a%b', 'a?b', 'a#b']) {
      expect(validateAdminName(bad, 'group name', RESERVED_GROUP_NAMES), bad).toContain('must not contain')
    }
  })

  it('rejects other whitespace and invisible characters', () => {
    for (const bad of ['Ops\u00A0x', 'Ops\u2003x', 'a\u200Bb', 'a\u0000b', 'a\u2028b']) {
      expect(validateAdminName(bad, 'group name', RESERVED_GROUP_NAMES), bad).toContain('must not contain')
    }
    expect(validateAdminName('Müller', 'user-id', RESERVED_USER_IDS)).toBeNull()
  })

  it('rejects the reserved names of its collection, exactly', () => {
    for (const name of ['.', '..', 'item', 'available-roles']) {
      expect(validateAdminName(name, 'user-id', RESERVED_USER_IDS)).toBe(`The user-id "${name}" is reserved.`)
    }
    for (const name of ['.', '..', 'item', 'available-categories']) {
      expect(validateAdminName(name, 'group name', RESERVED_GROUP_NAMES)).toBe(`The group name "${name}" is reserved.`)
    }
    // the endpoint of the other collection is a fine name
    expect(validateAdminName('available-categories', 'user-id', RESERVED_USER_IDS)).toBeNull()
    expect(validateAdminName('available-roles', 'group name', RESERVED_GROUP_NAMES)).toBeNull()
    for (const name of ['...', 'Item', 'items', 'available-categories-2', '.hidden']) {
      expect(validateAdminName(name, 'group name', RESERVED_GROUP_NAMES), name).toBeNull()
    }
  })

  it('names the field in the message', () => {
    expect(validateAdminName('a b', 'role name', [])).toContain('role name')
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

describe('validateOnCallRoleName', () => {
  it.each(['Ops Rota', 'Müller (Nacht)', 'a', '1st-line_v2.0'])('accepts %s', (name) => {
    expect(validateOnCallRoleName(name)).toBeNull()
  })

  it('requires a name', () => {
    expect(validateOnCallRoleName('')).toBe('An on-call role name is required.')
  })

  it.each([' Ops', 'Ops ', 'Ops  Rota', 'a/b', '-lead', 'a&b'])('rejects the shape of "%s"', (name) => {
    expect(validateOnCallRoleName(name)).toBe('The on-call role name must start with a letter or digit and contain only letters, digits, the characters _ - . ( ) and single spaces between words.')
  })

  it('rejects more than 256 characters, counting code points', () => {
    expect(validateOnCallRoleName('a'.repeat(256))).toBeNull()
    expect(validateOnCallRoleName('a'.repeat(257))).toBe('The on-call role name must not be longer than 256 characters.')
    expect(validateOnCallRoleName('\u{1D4D0}'.repeat(256))).toBeNull()
  })

  it('rejects the reserved names', () => {
    expect(validateOnCallRoleName('calendar')).toBe('The on-call role name "calendar" is reserved.')
    expect(validateOnCallRoleName('item')).toBe('The on-call role name "item" is reserved.')
  })
})
