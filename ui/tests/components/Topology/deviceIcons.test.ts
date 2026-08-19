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
