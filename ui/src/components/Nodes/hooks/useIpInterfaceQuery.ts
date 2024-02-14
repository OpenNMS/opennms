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

import { IpInterface, IpInterfaceInfo } from '@/types'

export const useIpInterfaceQuery = () => {
  /**
   * Find the 'best' IP for the given node id.
   * If there is only one, return that one. Otherwise try to find the primary SNMP interface, or the otherwise best one.
   *
   * @returns a object with the IP address plus a modifier: 'M' for managed, 'P' for primary, 'S' for secondary, 'N' for not eligible
   */
  const getBestIpInterfaceForNode = (nodeId: string, nodeToIpInterfaceMap: Map<string, IpInterface[]>): IpInterfaceInfo => {
    if (nodeToIpInterfaceMap.has(nodeId)) {
      const ipInterfaces = nodeToIpInterfaceMap.get(nodeId) || []

      let intf: IpInterface | null = null

      if (ipInterfaces.length === 1) {
        intf = ipInterfaces[0]
      } else if (ipInterfaces.length > 1) {
        // try to get SNMP primary (even if unmanaged), or else the first managed interface, or else just the first interface
        intf = ipInterfaces.find(x => x.snmpPrimary === 'P') || ipInterfaces.find(x => x.isManaged === 'M') || ipInterfaces[0]
      }

      if (intf) {
        const managed = intf.isManaged === 'M'
        const primaryType = intf.snmpPrimary || ''
        const primaryLabel = getSnmpPrimaryLabel(primaryType)

        return {
          label: intf.ipAddress,
          managed,
          primaryLabel,
          primaryType
        } as IpInterfaceInfo
      }
    }

    return {
      label: '',
      managed: false,
      primaryLabel: '',
      primaryType: ''
    } as IpInterfaceInfo
  }

  return {
    getBestIpInterfaceForNode
  }
}

const getSnmpPrimaryLabel = (primaryType: string) => {
  switch (primaryType) {
    case 'P': return 'Primary'
    case 'S': return 'Secondary'
    case 'N': return 'Not Eligible'
    default: return ''
  }
}
