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

import { describe, it, expect } from 'vitest'
import {
  iconKeyForSysObjectId,
  resolveDeviceIcon,
  deviceIconForSysObjectId,
  deviceIconImage,
  powerStateForIconKey,
  DEVICE_ICON_SVG
} from '@/components/Topology/deviceIcons'

describe('deviceIcons (legacy-faithful sysObjectId -> icon)', () => {
  describe('iconKeyForSysObjectId', () => {
    it('returns the base key when there is no sysObjectId', () => {
      expect(iconKeyForSysObjectId(undefined)).toBe('linkd.system')
      expect(iconKeyForSysObjectId(null)).toBe('linkd.system')
      expect(iconKeyForSysObjectId('')).toBe('linkd.system')
    })

    it('appends a leading-dot sysObjectId without doubling the dot', () => {
      expect(iconKeyForSysObjectId('.1.3.6.1.4.1.9.1.559')).toBe(
        'linkd.system.snmp.1.3.6.1.4.1.9.1.559'
      )
    })

    it('inserts a dot when the sysObjectId has none', () => {
      expect(iconKeyForSysObjectId('1.3.6.1.4.1.9.1.559')).toBe(
        'linkd.system.snmp.1.3.6.1.4.1.9.1.559'
      )
    })
  })

  describe('resolveDeviceIcon', () => {
    it('resolves an exact iconKey match', () => {
      expect(resolveDeviceIcon('linkd.system.snmp.1.3.6.1.4.1.9.1.559')).toBe('router')
      expect(resolveDeviceIcon('linkd.system.snmp.1.3.6.1.4.1.9.1.283')).toBe('switch')
    })

    it('returns null for the generic/base key (so the node keeps a plain circle)', () => {
      expect(resolveDeviceIcon('linkd.system')).toBeNull()
    })

    it('falls back to the longest matching prefix for a more-specific OID', () => {
      // a sub-OID under a known router OID still resolves to router
      expect(resolveDeviceIcon('linkd.system.snmp.1.3.6.1.4.1.9.1.559.7')).toBe('router')
    })

    it('returns null when nothing matches', () => {
      expect(resolveDeviceIcon('linkd.system.snmp.9.9.9.9')).toBeNull()
      expect(resolveDeviceIcon(undefined)).toBeNull()
    })
  })

  describe('deviceIconForSysObjectId', () => {
    it('maps representative OIDs to device types', () => {
      expect(deviceIconForSysObjectId('.1.3.6.1.4.1.9.1.559')).toBe('router')
      expect(deviceIconForSysObjectId('.1.3.6.1.4.1.9.1.283')).toBe('switch')
      expect(deviceIconForSysObjectId('.1.3.6.1.4.1.8072.3.2.10')).toBe('server')
      expect(deviceIconForSysObjectId('.1.3.6.1.4.1.9.1.1034')).toBe('wifiAccess')
      expect(deviceIconForSysObjectId('.1.3.6.1.4.1.253.8.62.1.19.4.24.1')).toBe('printer')
    })

    it('returns null for an unknown or absent sysObjectId', () => {
      expect(deviceIconForSysObjectId('.1.2.3.4')).toBeNull()
      expect(deviceIconForSysObjectId(undefined)).toBeNull()
    })
  })

  describe('DEVICE_ICON_SVG', () => {
    it('provides an SVG data URL for every device type', () => {
      for (const id of ['router', 'switch', 'server', 'wifiAccess', 'printer', 'cloud'] as const) {
        expect(DEVICE_ICON_SVG[id]).toMatch(/^data:image\/svg\+xml;base64,/)
      }
    })
  })
})

// VMware spells the entity kind and its power state into one icon key, so the
// kind resolves by prefix and the state is read off the suffix separately.
describe('VMware icon keys', () => {
  it('maps each entity kind to its own glyph', () => {
    expect(resolveDeviceIcon('vmware.DATACENTER_ICON')).toBe('datacenter')
    expect(resolveDeviceIcon('vmware.NETWORK_ICON')).toBe('network')
    expect(resolveDeviceIcon('vmware.DATASTORE_ICON')).toBe('datastore')
  })

  it('reuses the server glyph for a host system, whatever its power state', () => {
    expect(resolveDeviceIcon('vmware.HOSTSYSTEM_ICON_ON')).toBe('server')
    expect(resolveDeviceIcon('vmware.HOSTSYSTEM_ICON_STANDBY')).toBe('server')
    expect(resolveDeviceIcon('vmware.HOSTSYSTEM_ICON_UNKNOWN')).toBe('server')
  })

  it('gives a virtual machine its own glyph, whatever its power state', () => {
    expect(resolveDeviceIcon('vmware.VIRTUALMACHINE_ICON_ON')).toBe('virtualMachine')
    expect(resolveDeviceIcon('vmware.VIRTUALMACHINE_ICON_SUSPENDED')).toBe('virtualMachine')
  })

  it('does not confuse a host system with a virtual machine', () => {
    expect(resolveDeviceIcon('vmware.HOSTSYSTEM_ICON_ON'))
      .not.toBe(resolveDeviceIcon('vmware.VIRTUALMACHINE_ICON_ON'))
  })

  it('leaves an enlinkd key alone, so nothing regresses', () => {
    // The generic enlinkd key must still fall through to the sysObjectId map.
    expect(resolveDeviceIcon('linkd.system')).toBeNull()
    expect(resolveDeviceIcon('linkd.system.snmp.1.3.6.1.4.1.9.1.283')).toBe('switch')
  })
})

describe('powerStateForIconKey', () => {
  it('reads the state off the suffix', () => {
    expect(powerStateForIconKey('vmware.VIRTUALMACHINE_ICON_ON')).toBe('on')
    expect(powerStateForIconKey('vmware.VIRTUALMACHINE_ICON_OFF')).toBe('off')
    expect(powerStateForIconKey('vmware.VIRTUALMACHINE_ICON_SUSPENDED')).toBe('suspended')
    expect(powerStateForIconKey('vmware.HOSTSYSTEM_ICON_STANDBY')).toBe('standby')
  })

  it('is null when there is no state to show, so no badge is drawn', () => {
    expect(powerStateForIconKey('vmware.VIRTUALMACHINE_ICON_UNKNOWN')).toBeNull()
    expect(powerStateForIconKey('vmware.DATACENTER_ICON')).toBeNull()
    expect(powerStateForIconKey(undefined)).toBeNull()
  })
})

describe('deviceIconImage', () => {
  it('is the plain glyph when there is no power state', () => {
    expect(deviceIconImage('server', null)).toBe(DEVICE_ICON_SVG.server)
  })

  it('composes a distinct image per state, so the badge is visible', () => {
    const on = deviceIconImage('virtualMachine', 'on')
    const off = deviceIconImage('virtualMachine', 'off')
    expect(on).not.toBe(DEVICE_ICON_SVG.virtualMachine)
    expect(on).not.toBe(off)
  })

  it('keeps the glyph itself, badging rather than replacing it', () => {
    const plain = atob(DEVICE_ICON_SVG.virtualMachine.split(',')[1])
    const badged = atob(deviceIconImage('virtualMachine', 'on').split(',')[1])
    const glyphBody = plain.replace(/^.*viewBox="0 0 24 24">/, '').replace('</svg>', '')
    expect(badged).toContain(glyphBody)
  })

  it('returns the same url for the same request', () => {
    expect(deviceIconImage('server', 'standby')).toBe(deviceIconImage('server', 'standby'))
  })
})
