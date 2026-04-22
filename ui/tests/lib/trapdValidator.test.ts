import { XMLParser, XMLValidator } from 'fast-xml-parser'
import { afterAll, beforeAll, describe, expect, it, vi } from 'vitest'
import {
  AUTH_PROTOCOL_OPTIONS,
  AuthProtocol,
  AuthProtocols,
  MAX_PORT,
  MIN_PORT,
  PRIVACY_PROTOCOL_OPTIONS,
  PrivacyProtocol,
  PrivacyProtocols,
  SECURITY_LEVEL_OPTIONS,
  SecurityLevel,
  TRAPD_XML_NAMESPACE,
  getDefaultTrapdConfig,
  isValidIP,
  isValidPort,
  isValidSnmpSecurityLevel,
  validateTrapdXml
} from '@/lib/trapdValidator'

// ---------------------------------------------------------------------------
// DOMParser polyfill (fast-xml-parser backed)
//
// happy-dom v9 parses ALL MIME types as HTML, so root.namespaceURI is always
// the XHTML namespace.  We replace window.DOMParser with a minimal but correct
// implementation for the subset of the DOM API that validateTrapdXml uses.
// ---------------------------------------------------------------------------

class FakeElement {
  localName: string
  namespaceURI: string | null
  textContent: string | null = null
  private attrs: Record<string, string>
  private children: FakeElement[]

  constructor(localName: string, attrs: Record<string, string>, children: FakeElement[] = []) {
    this.localName = localName
    this.attrs = attrs
    this.children = children
    this.namespaceURI = attrs['xmlns'] ?? null
  }

  getAttribute(name: string): string | null {
    return Object.prototype.hasOwnProperty.call(this.attrs, name) ? this.attrs[name] : null
  }

  getElementsByTagName(tagName: string): FakeElement[] {
    const results: FakeElement[] = []
    for (const child of this.children) {
      if (child.localName === tagName) results.push(child)
      results.push(...child.getElementsByTagName(tagName))
    }
    return results
  }
}

class FakeDocument {
  documentElement: FakeElement
  private parseError: FakeElement | null

  constructor(root: FakeElement, parseError: FakeElement | null = null) {
    this.documentElement = root
    this.parseError = parseError
  }

  querySelector(selector: string): FakeElement | null {
    return selector === 'parsererror' ? this.parseError : null
  }
}

function buildElement(tagName: string, node: Record<string, unknown>): FakeElement {
  const attrs: Record<string, string> = {}
  const children: FakeElement[] = []

  for (const [key, value] of Object.entries(node)) {
    if (key.startsWith('@_')) {
      attrs[key.slice(2)] = String(value)
    } else if (Array.isArray(value)) {
      for (const child of value as Record<string, unknown>[]) {
        children.push(buildElement(key, child))
      }
    } else if (typeof value === 'object' && value !== null) {
      children.push(buildElement(key, value as Record<string, unknown>))
    }
  }

  return new FakeElement(tagName, attrs, children)
}

const fxpParser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '@_',
  isArray: (name) => name === 'snmpv3-user',
  parseAttributeValue: false,
  trimValues: false
})

class FakeDOMParser {
  parseFromString(xmlString: string, _mimeType: string): FakeDocument {
    void _mimeType
    const validation = XMLValidator.validate(xmlString)
    if (validation !== true) {
      const errEl = new FakeElement('parsererror', {})
      errEl.textContent = (validation as { err: { msg: string } }).err?.msg ?? 'parse error'
      const dummyRoot = new FakeElement('parsererror', {})
      return new FakeDocument(dummyRoot, errEl)
    }

    const parsed = fxpParser.parse(xmlString) as Record<string, Record<string, unknown>>
    const rootTagName = Object.keys(parsed)[0]
    const rootNode = parsed[rootTagName] ?? {}
    const root = buildElement(rootTagName, rootNode)
    return new FakeDocument(root)
  }
}

beforeAll(() => {
  vi.stubGlobal('DOMParser', FakeDOMParser)
})

afterAll(() => {
  vi.unstubAllGlobals()
})

const VALID_NS = TRAPD_XML_NAMESPACE

/** Build a minimal valid trapd XML string, overriding individual attributes. */
const buildXml = (overrides: {
  root?: string
  xmlns?: string | null
  address?: string | null
  port?: string | null
  suspect?: string | null
  users?: string
} = {}): string => {
  const {
    root = 'trapd-configuration',
    xmlns = VALID_NS,
    address = '*',
    port = '162',
    suspect = null,
    users = ''
  } = overrides

  const nsAttr = xmlns === null ? '' : ` xmlns="${xmlns}"`
  const addrAttr = address === null ? '' : ` snmp-trap-address="${address}"`
  const portAttr = port === null ? '' : ` snmp-trap-port="${port}"`
  const suspectAttr = suspect === null ? '' : ` new-suspect-on-trap="${suspect}"`

  if (users) {
    return `<${root}${nsAttr}${addrAttr}${portAttr}${suspectAttr}>${users}</${root}>`
  }
  return `<${root}${nsAttr}${addrAttr}${portAttr}${suspectAttr} />`
}

/** Build a <snmpv3-user /> element string from an attribute map. */
const buildUser = (attrs: Record<string, string> = {}): string => {
  const attrStr = Object.entries(attrs)
    .map(([k, v]) => `${k}="${v}"`)
    .join(' ')
  return `<snmpv3-user ${attrStr} />`
}

describe('isValidPort', () => {
  it.each([MIN_PORT, MAX_PORT, 162, 10162, 1024])('returns true for valid port %i', (port) => {
    expect(isValidPort(port)).toBe(true)
  })

  it('returns false for undefined', () => {
    expect(isValidPort(undefined)).toBe(false)
  })

  it('returns false for 0 (below MIN_PORT)', () => {
    expect(isValidPort(0)).toBe(false)
  })

  it('returns false for MAX_PORT + 1', () => {
    expect(isValidPort(MAX_PORT + 1)).toBe(false)
  })

  it('returns false for negative port', () => {
    expect(isValidPort(-1)).toBe(false)
  })

  it('returns false for NaN', () => {
    expect(isValidPort(NaN)).toBe(false)
  })
})

describe('isValidIP', () => {
  it.each(['0.0.0.0', '192.168.1.1', '255.255.255.255', '10.0.0.1'])(
    'returns true for valid IPv4 %s',
    (ip) => {
      expect(isValidIP(ip)).toBe(true)
    }
  )

  it('returns false for too few octets', () => {
    expect(isValidIP('192.168.1')).toBe(false)
  })

  it('returns false for too many octets', () => {
    expect(isValidIP('192.168.1.1.1')).toBe(false)
  })

  it('returns false for octet > 255', () => {
    expect(isValidIP('192.168.1.256')).toBe(false)
  })

  it('returns false for non-numeric octet', () => {
    expect(isValidIP('abc.def.ghi.jkl')).toBe(false)
  })

  it('returns false for empty string', () => {
    expect(isValidIP('')).toBe(false)
  })

  it('returns false for wildcard *', () => {
    expect(isValidIP('*')).toBe(false)
  })
})

describe('isValidSnmpSecurityLevel', () => {
  it.each([SecurityLevel.NoAuthNoPriv, SecurityLevel.AuthNoPriv, SecurityLevel.AuthPriv])(
    'returns true for valid level %i',
    (level) => {
      expect(isValidSnmpSecurityLevel(level)).toBe(true)
    }
  )

  it('returns false for SecurityLevel.None (0)', () => {
    expect(isValidSnmpSecurityLevel(SecurityLevel.None)).toBe(false)
  })

  it('returns false for level 4 (out of range)', () => {
    expect(isValidSnmpSecurityLevel(4)).toBe(false)
  })

  it('returns false for undefined', () => {
    expect(isValidSnmpSecurityLevel(undefined)).toBe(false)
  })
})

describe('getDefaultTrapdConfig', () => {
  it('returns expected default values', () => {
    const config = getDefaultTrapdConfig()
    expect(config.snmpTrapAddress).toBe('*')
    expect(config.snmpTrapPort).toBe(10162)
    expect(config.newSuspectOnTrap).toBe(false)
    expect(config.includeRawMessage).toBe(false)
    expect(config.threads).toBe(0)
    expect(config.queueSize).toBe(10000)
    expect(config.batchSize).toBe(1000)
    expect(config.batchInterval).toBe(500)
    expect(config.useAddressFromVarbind).toBe(false)
    expect(config.snmpv3User).toEqual([])
  })

  it('returns a fresh object each call (no shared reference)', () => {
    const a = getDefaultTrapdConfig()
    const b = getDefaultTrapdConfig()
    a.snmpv3User.push({ securityName: 'x' } as any)
    expect(b.snmpv3User).toHaveLength(0)
  })
})

describe('SECURITY_LEVEL_OPTIONS', () => {
  it('contains exactly three entries', () => {
    expect(SECURITY_LEVEL_OPTIONS).toHaveLength(3)
  })

  it('has correct _value strings for all three levels', () => {
    const values = SECURITY_LEVEL_OPTIONS.map((o) => o._value)
    expect(values).toContain(String(SecurityLevel.NoAuthNoPriv))
    expect(values).toContain(String(SecurityLevel.AuthNoPriv))
    expect(values).toContain(String(SecurityLevel.AuthPriv))
  })
})

describe('AUTH_PROTOCOL_OPTIONS', () => {
  it('contains exactly as many entries as AuthProtocols', () => {
    expect(AUTH_PROTOCOL_OPTIONS).toHaveLength(AuthProtocols.length)
  })

  it('maps protocol values correctly', () => {
    const values = AUTH_PROTOCOL_OPTIONS.map((o) => o._value)
    expect(values).toEqual(['MD5', 'SHA', 'SHA-224', 'SHA-256', 'SHA-512'])
  })
})

describe('PRIVACY_PROTOCOL_OPTIONS', () => {
  it('contains exactly as many entries as PrivacyProtocols', () => {
    expect(PRIVACY_PROTOCOL_OPTIONS).toHaveLength(PrivacyProtocols.length)
  })

  it('maps protocol values correctly', () => {
    const values = PRIVACY_PROTOCOL_OPTIONS.map((o) => o._value)
    expect(values).toContain(PrivacyProtocol.DES)
    expect(values).toContain(PrivacyProtocol.AES)
    expect(values).toContain(PrivacyProtocol.AES256)
  })
})

describe('validateTrapdXml – XML structure', () => {
  it('returns invalid for empty string', () => {
    const result = validateTrapdXml('')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('xml')
  })

  it('returns invalid for whitespace-only string', () => {
    const result = validateTrapdXml('   \n  ')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('xml')
  })

  it('returns invalid for malformed XML', () => {
    const result = validateTrapdXml('<unclosed')
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('xml')
  })

  it('returns invalid when root element is not trapd-configuration', () => {
    const result = validateTrapdXml(`<other-root xmlns="${VALID_NS}" />`)
    expect(result.valid).toBe(false)
    expect(result.errors[0].field).toBe('root')
    expect(result.errors[0].message).toMatch(/trapd-configuration/)
  })

  it('returns invalid for wrong xmlns', () => {
    const result = validateTrapdXml(buildXml({ xmlns: 'http://wrong.namespace' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'xmlns')).toBe(true)
  })

  it('returns invalid when xmlns is omitted', () => {
    const result = validateTrapdXml(buildXml({ xmlns: null }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'xmlns')).toBe(true)
  })

  it('returns valid for minimal correct XML', () => {
    const result = validateTrapdXml(buildXml())
    expect(result.valid).toBe(true)
    expect(result.errors).toHaveLength(0)
  })
})

describe('validateTrapdXml – snmp-trap-address', () => {
  it('accepts omitted snmp-trap-address because the XSD defaults it to "*"', () => {
    const result = validateTrapdXml(buildXml({ address: null }))
    expect(result.valid).toBe(true)
    expect(result.errors.some((e) => e.field === 'snmp-trap-address')).toBe(false)
  })

  it('accepts wildcard "*"', () => {
    const result = validateTrapdXml(buildXml({ address: '*' }))
    expect(result.valid).toBe(true)
  })

  it('accepts a valid IPv4 address', () => {
    const result = validateTrapdXml(buildXml({ address: '192.168.1.1' }))
    expect(result.valid).toBe(true)
  })

  it('returns error for invalid IPv4 address', () => {
    const result = validateTrapdXml(buildXml({ address: '999.0.0.1' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'snmp-trap-address')).toBe(true)
  })

  it('returns error for hostname (not IPv4)', () => {
    const result = validateTrapdXml(buildXml({ address: 'localhost' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'snmp-trap-address')).toBe(true)
  })
})

describe('validateTrapdXml – snmp-trap-port', () => {
  it('returns error when snmp-trap-port is missing', () => {
    const result = validateTrapdXml(buildXml({ port: null }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'snmp-trap-port')).toBe(true)
  })

  it('accepts MIN_PORT boundary', () => {
    const result = validateTrapdXml(buildXml({ port: String(MIN_PORT) }))
    expect(result.valid).toBe(true)
  })

  it('accepts MAX_PORT boundary', () => {
    const result = validateTrapdXml(buildXml({ port: String(MAX_PORT) }))
    expect(result.valid).toBe(true)
  })

  it('returns error for port 0 (below MIN_PORT)', () => {
    const result = validateTrapdXml(buildXml({ port: '0' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'snmp-trap-port')).toBe(true)
  })

  it('returns error for port 65536 (above MAX_PORT)', () => {
    const result = validateTrapdXml(buildXml({ port: '65536' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'snmp-trap-port')).toBe(true)
  })

  it('returns error for non-numeric port', () => {
    const result = validateTrapdXml(buildXml({ port: 'abc' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'snmp-trap-port')).toBe(true)
  })
})

describe('validateTrapdXml – new-suspect-on-trap', () => {
  it('accepts "true"', () => {
    const result = validateTrapdXml(buildXml({ suspect: 'true' }))
    expect(result.valid).toBe(true)
  })

  it('accepts "false"', () => {
    const result = validateTrapdXml(buildXml({ suspect: 'false' }))
    expect(result.valid).toBe(true)
  })

  it('is optional (absent is valid)', () => {
    const result = validateTrapdXml(buildXml({ suspect: null }))
    expect(result.valid).toBe(true)
  })

  it('returns error for invalid value', () => {
    const result = validateTrapdXml(buildXml({ suspect: 'yes' }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field === 'new-suspect-on-trap')).toBe(true)
  })
})

describe('validateTrapdXml – snmpv3-user: security-name and security-level', () => {
  it('returns error when security-name is missing', () => {
    const user = buildUser({ 'security-level': '1' })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('security-name'))).toBe(true)
  })

  it('allows missing security-level (optional)', () => {
    const user = buildUser({ 'security-name': 'user1' })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.valid).toBe(true)
    expect(result.errors.some((e) => e.field.includes('security-level'))).toBe(false)
  })

  it('returns error for security-level 0 (None)', () => {
    const user = buildUser({ 'security-name': 'user1', 'security-level': '0' })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('security-level'))).toBe(true)
  })

  it('returns error for security-level 4 (out of range)', () => {
    const user = buildUser({ 'security-name': 'user1', 'security-level': '4' })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('security-level'))).toBe(true)
  })
})

describe('validateTrapdXml – snmpv3-user: level 1 (NoAuthNoPriv)', () => {
  it('is valid with only security-name and security-level=1', () => {
    const user = buildUser({ 'security-name': 'user1', 'security-level': '1' })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.valid).toBe(true)
  })

  it('returns error when auth-protocol is present at level 1', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '1',
      'auth-protocol': 'MD5',
      'auth-passphrase': 'pass'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(true)
  })

  it('returns error when auth-passphrase is present at level 1', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '1',
      'auth-passphrase': 'pass'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-passphrase'))).toBe(true)
  })

  it('returns error when privacy-protocol is present at level 1', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '1',
      'privacy-protocol': 'DES',
      'privacy-passphrase': 'priv'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('privacy-protocol'))).toBe(true)
  })

  it('returns error when privacy-passphrase is present at level 1', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '1',
      'privacy-passphrase': 'priv'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('privacy-passphrase'))).toBe(true)
  })
})

describe('validateTrapdXml – snmpv3-user: level 2 (AuthNoPriv)', () => {
  it('is valid with auth-protocol and auth-passphrase at level 2', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'SHA',
      'auth-passphrase': 'authpass'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.valid).toBe(true)
  })

  it('returns error when auth-protocol is missing at level 2', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-passphrase': 'secret'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(true)
  })

  it('returns error when auth-passphrase is missing at level 2', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'MD5'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-passphrase'))).toBe(true)
  })

  it('returns error when privacy-protocol is present at level 2', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'MD5',
      'auth-passphrase': 'secret',
      'privacy-protocol': 'DES',
      'privacy-passphrase': 'priv'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('privacy-protocol'))).toBe(true)
  })

  it('returns error when privacy-passphrase is present at level 2', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'MD5',
      'auth-passphrase': 'secret',
      'privacy-passphrase': 'priv'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('privacy-passphrase'))).toBe(true)
  })
})

describe('validateTrapdXml – snmpv3-user: level 3 (AuthPriv)', () => {
  const validLevel3 = {
    'security-name': 'user1',
    'security-level': '3',
    'auth-protocol': 'SHA',
    'auth-passphrase': 'authsecret',
    'privacy-protocol': 'AES',
    'privacy-passphrase': 'privsecret'
  }

  it('is valid with all required fields at level 3', () => {
    const result = validateTrapdXml(buildXml({ users: buildUser(validLevel3) }))
    expect(result.valid).toBe(true)
  })

  it('returns error when auth-protocol is missing at level 3', () => {
    const { 'auth-protocol': omittedAuthProtocol, ...attrs } = validLevel3
    void omittedAuthProtocol
    const result = validateTrapdXml(buildXml({ users: buildUser(attrs) }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(true)
  })

  it('returns error when auth-passphrase is missing at level 3', () => {
    const { 'auth-passphrase': omittedAuthPassphrase, ...attrs } = validLevel3
    void omittedAuthPassphrase
    const result = validateTrapdXml(buildXml({ users: buildUser(attrs) }))
    expect(result.errors.some((e) => e.field.includes('auth-passphrase'))).toBe(true)
  })

  it('returns error when privacy-protocol is missing at level 3', () => {
    const { 'privacy-protocol': omittedPrivacyProtocol, ...attrs } = validLevel3
    void omittedPrivacyProtocol
    const result = validateTrapdXml(buildXml({ users: buildUser(attrs) }))
    expect(result.errors.some((e) => e.field.includes('privacy-protocol'))).toBe(true)
  })

  it('returns error when privacy-passphrase is missing at level 3', () => {
    const { 'privacy-passphrase': omittedPrivacyPassphrase, ...attrs } = validLevel3
    void omittedPrivacyPassphrase
    const result = validateTrapdXml(buildXml({ users: buildUser(attrs) }))
    expect(result.errors.some((e) => e.field.includes('privacy-passphrase'))).toBe(true)
  })
})

describe('validateTrapdXml – auth-protocol values', () => {
  it('accepts dashed SHA-2 auth-protocol values', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'SHA-256',
      'auth-passphrase': 'secret'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(false)
  })

  it('rejects undashed SHA-2 auth-protocol values', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'SHA256',
      'auth-passphrase': 'secret'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(true)
  })

  it('rejects completely unknown auth-protocol value', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '2',
      'auth-protocol': 'UNKNOWN',
      'auth-passphrase': 'secret'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(true)
  })

  it('rejects unknown privacy-protocol value', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '3',
      'auth-protocol': 'MD5',
      'auth-passphrase': 'secret',
      'privacy-protocol': 'UNKNOWN',
      'privacy-passphrase': 'priv'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('privacy-protocol'))).toBe(true)
  })
})

describe('validateTrapdXml – privacy-protocol without auth-protocol', () => {
  it('returns error when privacy-protocol set but auth-protocol absent', () => {
    const user = buildUser({
      'security-name': 'user1',
      'security-level': '3',
      'privacy-protocol': 'AES',
      'privacy-passphrase': 'priv',
      'auth-passphrase': 'secret'
    })
    const result = validateTrapdXml(buildXml({ users: user }))
    expect(result.errors.some((e) => e.field.includes('auth-protocol'))).toBe(true)
  })
})

describe('validateTrapdXml – multiple snmpv3-user elements', () => {
  it('is valid with multiple well-formed users', () => {
    const user1 = buildUser({ 'security-name': 'userA', 'security-level': '1' })
    const user2 = buildUser({
      'security-name': 'userB',
      'security-level': '2',
      'auth-protocol': 'MD5',
      'auth-passphrase': 'authpass'
    })
    const result = validateTrapdXml(buildXml({ users: user1 + user2 }))
    expect(result.valid).toBe(true)
  })

  it('accumulates errors across multiple invalid users', () => {
    // user[1]: missing security-name; user[2]: invalid security-level
    const user1 = buildUser({ 'security-level': '1' })
    const user2 = buildUser({ 'security-name': 'userB', 'security-level': '99' })
    const result = validateTrapdXml(buildXml({ users: user1 + user2 }))
    expect(result.valid).toBe(false)
    expect(result.errors.some((e) => e.field.includes('snmpv3-user[1]'))).toBe(true)
    expect(result.errors.some((e) => e.field.includes('snmpv3-user[2]'))).toBe(true)
  })
})
