import { describe, expect, it } from 'vitest'
import { MAX_PORT, MIN_PORT, validateTrapdJson } from '@/lib/trapdValidator'

/** Build a minimal valid TrapConfig JSON string, overriding individual fields. */
const buildJson = (overrides: {
  address?: string | null    // null = omit the field
  port?: number | string | null  // null = omit; string to test wrong-type inputs
  suspect?: boolean | null   // null = omit the field
  users?: Record<string, unknown>[]
} = {}): string => {
  const { address = '*', port = 162, suspect = null, users } = overrides
  const obj: Record<string, unknown> = {}
  if (address !== null) {
    obj['snmpTrapAddress'] = address
  }
  if (port !== null) {
    obj['snmpTrapPort'] = port
  }
  if (suspect !== null) {
    obj['newSuspectOnTrap'] = suspect
  }
  if (users !== undefined) {
    obj['snmpv3User'] = users
  }
  return JSON.stringify(obj)
}

/** Build a snmpv3User-shaped object from a partial attribute map. */
const buildUser = (attrs: {
  securityName?: string
  securityLevel?: number
  authProtocol?: string
  authPassphrase?: string
  privacyProtocol?: string
  privacyPassphrase?: string
} = {}): Record<string, unknown> => ({ ...attrs })

describe('validateTrapdJson – parse errors', () => {
  it('returns invalid for empty string', () => {
    const result = validateTrapdJson('')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('json')
  })

  it('returns invalid for whitespace-only string', () => {
    const result = validateTrapdJson('   \n  ')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('json')
  })

  it('returns invalid for malformed JSON', () => {
    const result = validateTrapdJson('{invalid json')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('json')
  })

  it('returns invalid when JSON is a top-level array', () => {
    const result = validateTrapdJson('[]')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('json')
  })

  it('returns valid for minimal correct JSON', () => {
    const result = validateTrapdJson(buildJson())
    expect(result.valid).toBe(true)
    expect(result.errors).toHaveLength(0)
  })
})

describe('validateTrapdJson – snmpTrapAddress', () => {
  it('accepts omitted snmpTrapAddress because it is optional', () => {
    const result = validateTrapdJson(buildJson({ address: null }))
    expect(result.valid).toBe(true)
    expect(result.errors.some(e => e.field === 'snmpTrapAddress')).toBe(false)
  })

  it('accepts wildcard "*"', () => {
    const result = validateTrapdJson(buildJson({ address: '*' }))
    expect(result.valid).toBe(true)
  })

  it('accepts a valid IPv4 address', () => {
    const result = validateTrapdJson(buildJson({ address: '192.168.1.1' }))
    expect(result.valid).toBe(true)
  })

  it('returns error for invalid IPv4 address', () => {
    const result = validateTrapdJson(buildJson({ address: '999.0.0.1' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field === 'snmpTrapAddress')).toBe(true)
  })

  it('returns error for hostname (not IPv4)', () => {
    const result = validateTrapdJson(buildJson({ address: 'localhost' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field === 'snmpTrapAddress')).toBe(true)
  })
})

describe('validateTrapdJson – snmpTrapPort', () => {
  it('returns error when snmpTrapPort is missing', () => {
    const result = validateTrapdJson(buildJson({ port: null }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field === 'snmpTrapPort')).toBe(true)
  })

  it('accepts MIN_PORT boundary', () => {
    const result = validateTrapdJson(buildJson({ port: MIN_PORT }))
    expect(result.valid).toBe(true)
  })

  it('accepts MAX_PORT boundary', () => {
    const result = validateTrapdJson(buildJson({ port: MAX_PORT }))
    expect(result.valid).toBe(true)
  })

  it('returns error for port 0 (below MIN_PORT)', () => {
    const result = validateTrapdJson(buildJson({ port: 0 }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field === 'snmpTrapPort')).toBe(true)
  })

  it('returns error for port 65536 (above MAX_PORT)', () => {
    const result = validateTrapdJson(buildJson({ port: 65536 }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field === 'snmpTrapPort')).toBe(true)
  })

  it('returns error for non-numeric port', () => {
    const result = validateTrapdJson(buildJson({ port: 'abc' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field === 'snmpTrapPort')).toBe(true)
  })
})

describe('validateTrapdJson – newSuspectOnTrap', () => {
  it('accepts true', () => {
    const result = validateTrapdJson(buildJson({ suspect: true }))
    expect(result.valid).toBe(true)
  })

  it('accepts false', () => {
    const result = validateTrapdJson(buildJson({ suspect: false }))
    expect(result.valid).toBe(true)
  })

  it('is optional (absent defaults to false)', () => {
    const result = validateTrapdJson(buildJson({ suspect: null }))
    expect(result.valid).toBe(true)
  })
})

describe('validateTrapdJson – snmpv3User: securityName and securityLevel', () => {
  it('returns error when securityName is missing', () => {
    const user = buildUser({ securityLevel: 1 })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('securityName'))).toBe(true)
  })

  it('returns error for missing securityLevel', () => {
    const user = buildUser({ securityName: 'user1' })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field.includes('securityLevel'))).toBe(true)
  })

  it('returns error for securityLevel 0 (None)', () => {
    const user = buildUser({ securityName: 'user1', securityLevel: 0 })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('securityLevel'))).toBe(true)
  })

  it('returns error for securityLevel 4 (out of range)', () => {
    const user = buildUser({ securityName: 'user1', securityLevel: 4 })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('securityLevel'))).toBe(true)
  })
})

describe('validateTrapdJson – snmpv3User: level 1 (NoAuthNoPriv)', () => {
  it('is valid with only securityName and securityLevel=1', () => {
    const user = buildUser({ securityName: 'user1', securityLevel: 1 })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.valid).toBe(true)
  })

  it('returns error when authProtocol is present at level 1', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 1,
      authProtocol: 'MD5',
      authPassphrase: 'pass'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(true)
  })

  it('returns error when authPassphrase is present at level 1', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 1,
      authPassphrase: 'pass'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authPassphrase'))).toBe(true)
  })

  it('returns error when privacyProtocol is present at level 1', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 1,
      privacyProtocol: 'DES',
      privacyPassphrase: 'priv'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('privacyProtocol'))).toBe(true)
  })

  it('returns error when privacyPassphrase is present at level 1', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 1,
      privacyPassphrase: 'priv'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('privacyPassphrase'))).toBe(true)
  })
})

describe('validateTrapdJson – snmpv3User: level 2 (AuthNoPriv)', () => {
  it('is valid with authProtocol and authPassphrase at level 2', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'SHA',
      authPassphrase: 'authpass'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.valid).toBe(true)
  })

  it('returns error when authProtocol is missing at level 2', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authPassphrase: 'secret'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(true)
  })

  it('returns error when authPassphrase is missing at level 2', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'MD5'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authPassphrase'))).toBe(true)
  })

  it('returns error when privacyProtocol is present at level 2', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'MD5',
      authPassphrase: 'secret',
      privacyProtocol: 'DES',
      privacyPassphrase: 'priv'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('privacyProtocol'))).toBe(true)
  })

  it('returns error when privacyPassphrase is present at level 2', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'MD5',
      authPassphrase: 'secret',
      privacyPassphrase: 'priv'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('privacyPassphrase'))).toBe(true)
  })
})

describe('validateTrapdJson – snmpv3User: level 3 (AuthPriv)', () => {
  const validLevel3 = {
    securityName: 'user1',
    securityLevel: 3,
    authProtocol: 'SHA',
    authPassphrase: 'authsecret',
    privacyProtocol: 'AES',
    privacyPassphrase: 'privsecret'
  }

  it('is valid with all required fields at level 3', () => {
    const result = validateTrapdJson(buildJson({ users: [buildUser(validLevel3)] }))
    expect(result.valid).toBe(true)
  })

  it('returns error when authProtocol is missing at level 3', () => {
    const { authProtocol: omittedAuthProtocol, ...attrs } = validLevel3
    void omittedAuthProtocol
    const result = validateTrapdJson(buildJson({ users: [buildUser(attrs)] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(true)
  })

  it('returns error when authPassphrase is missing at level 3', () => {
    const { authPassphrase: omittedAuthPassphrase, ...attrs } = validLevel3
    void omittedAuthPassphrase
    const result = validateTrapdJson(buildJson({ users: [buildUser(attrs)] }))
    expect(result.errors.some(e => e.field.includes('authPassphrase'))).toBe(true)
  })

  it('returns error when privacyProtocol is missing at level 3', () => {
    const { privacyProtocol: omittedPrivacyProtocol, ...attrs } = validLevel3
    void omittedPrivacyProtocol
    const result = validateTrapdJson(buildJson({ users: [buildUser(attrs)] }))
    expect(result.errors.some(e => e.field.includes('privacyProtocol'))).toBe(true)
  })

  it('returns error when privacyPassphrase is missing at level 3', () => {
    const { privacyPassphrase: omittedPrivacyPassphrase, ...attrs } = validLevel3
    void omittedPrivacyPassphrase
    const result = validateTrapdJson(buildJson({ users: [buildUser(attrs)] }))
    expect(result.errors.some(e => e.field.includes('privacyPassphrase'))).toBe(true)
  })
})

describe('validateTrapdJson – authProtocol values', () => {
  it('accepts dashed SHA-2 authProtocol values', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'SHA-256',
      authPassphrase: 'secret'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(false)
  })

  it('rejects undashed SHA-2 authProtocol values', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'SHA256',
      authPassphrase: 'secret'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(true)
  })

  it('rejects completely unknown authProtocol value', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 2,
      authProtocol: 'UNKNOWN',
      authPassphrase: 'secret'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(true)
  })

  it('rejects unknown privacyProtocol value', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 3,
      authProtocol: 'MD5',
      authPassphrase: 'secret',
      privacyProtocol: 'UNKNOWN',
      privacyPassphrase: 'priv'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('privacyProtocol'))).toBe(true)
  })
})

describe('validateTrapdJson – privacyProtocol without authProtocol', () => {
  it('returns error when privacyProtocol set but authProtocol absent', () => {
    const user = buildUser({
      securityName: 'user1',
      securityLevel: 3,
      privacyProtocol: 'AES',
      privacyPassphrase: 'priv',
      authPassphrase: 'secret'
    })
    const result = validateTrapdJson(buildJson({ users: [user] }))
    expect(result.errors.some(e => e.field.includes('authProtocol'))).toBe(true)
  })
})

describe('validateTrapdJson – multiple snmpv3User elements', () => {
  it('is valid with multiple well-formed users', () => {
    const user1 = buildUser({ securityName: 'userA', securityLevel: 1 })
    const user2 = buildUser({
      securityName: 'userB',
      securityLevel: 2,
      authProtocol: 'MD5',
      authPassphrase: 'authpass'
    })
    const result = validateTrapdJson(buildJson({ users: [user1, user2] }))
    expect(result.valid).toBe(true)
  })

  it('accumulates errors across multiple invalid users', () => {
    // user[1]: missing securityName; user[2]: invalid securityLevel
    const user1 = buildUser({ securityLevel: 1 })
    const user2 = buildUser({ securityName: 'userB', securityLevel: 99 })
    const result = validateTrapdJson(buildJson({ users: [user1, user2] }))
    expect(result.valid).toBe(false)
    expect(result.errors.some(e => e.field.includes('snmpv3User[1]'))).toBe(true)
    expect(result.errors.some(e => e.field.includes('snmpv3User[2]'))).toBe(true)
  })
})
