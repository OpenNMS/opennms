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

import { SnmpDefinitionFormErrors, SnmpProfileFormErrors } from '@/types/snmpConfig'

export const validateDefinition = (
  snmpVersion: string,
  firstIpAddress: string,
  secondIpAddress: string
): SnmpDefinitionFormErrors => {
  const errors: SnmpDefinitionFormErrors = {}

  if (!snmpVersion) {
    errors.snmpVersion = 'SNMP Version is required'
  }

  if (!firstIpAddress) {
    errors.firstIpAddress = 'First IP Address is required'
  }

  // if (!secondIpAddress) {
  //   errors.secondIpAddress = 'Second IP Address is required'
  // }

  return errors
}

export const validateProfile = (
  label: string,
  filterExpression: string
): SnmpProfileFormErrors => {
  const errors: SnmpProfileFormErrors = {}

  if (!label) {
    errors.label = 'SNMP Profile label is required'
  }

  if (!filterExpression) {
    errors.filterExpression = 'FilterExpression is required'
  }

  return errors
}
