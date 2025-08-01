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

export const RequisitionFields = {
  Name: 'Name'
}
export const RequisitionTypes = {
  VMWare: 'VMware',
  RequisitionPlugin: 'Requisition Plugin',
  DNS: 'DNS',
  File: 'File',
  HTTP: 'HTTP',
  HTTPS: 'HTTPS',
  RequisitionPluginForServer: 'requisition'
}

export const requisitionDNSField = {
  zone: 'zone',
  requisitionName: 'foreign source'
}

export const RequisitionPluginSubTypes = {
  ACI: 'ACI',
  AzureIot: 'Azure IoT',
  OpenDaylight: 'OpenDaylight',
  PRIS: 'PRIS',
  Zabbix: 'Zabbix'
}
export const RequisitionHTTPTypes = [RequisitionTypes.HTTP, RequisitionTypes.HTTPS]
export const RequsitionTypesUsingHost = [
  RequisitionTypes.DNS,
  RequisitionTypes.VMWare,
  RequisitionTypes.HTTP,
  RequisitionTypes.HTTPS
]

export const RequisitionData = {
  RescanExisting: 'rescan-existing',
  ImportName: 'import-name',
  CronSchedule: 'cron-schedule',
  ImportURL: 'import-url-resource'
}

export const RescanVals = {
  False: 'false',
  DBOnly: 'dbonly',
  True: 'true'
}

export const VMWareFields = {
  Username: 'username',
  Password: 'password',
  UpperUsername: 'Username',
  UpperPassword: 'Password',
  RequisitionName: 'Requisition Name'
}

export const SplitTypes = {
  dns: 'dns://',
  file: 'file://',
  vmware: 'vmware://'
}

export const ErrorStrings = {
  DuplicateName: 'Name must be unique.',
  QuartzFormatSupportError: (numPart: number) => `Error: Expression has only ${numPart} part${numPart > 1 ? 's' : ''}. At least 6 parts are required.`,
  InvalidHostname: 'Invalid hostname',
  InvalidZoneName: 'Invalid zone name',
  InvalidRequisitionName: 'Invalid requisition name',
  FilePathStart: 'Path must start with a /.',
  FilePathWithQueryChar: 'Path contains invalid character: ?.',
  Required: (nameType: string) => `${nameType} required.`,
  NameShort: (nameType: string) => `${nameType} must have at least two chars.`,
  NameLong: (nameType: string, length = 255) => `${nameType} must be shorter than ${length}.`
}

export const LabelStrings = {
  duplicateKey: 'Duplicate key',
  optionNotAvailable: 'Option not available'
}

export const requisitionTypeList = [
  {
    id: 3,
    name: RequisitionTypes.DNS
  },
  {
    id: 2,
    name: RequisitionTypes.File
  },
  {
    id: 4,
    name: RequisitionTypes.HTTP
  },
  {
    id: 5,
    name: RequisitionTypes.HTTPS
  },
  /**
   * Cleanup-work #23 - Remove all Requisition Plugin items from the UI for H30
   * Remove temporary the Requisition Plugin from the list. It will be put back
   * once the plugins are ready for real use.
   */
  /* {
    id: 6,
    name: RequisitionTypes.RequisitionPlugin
  }, */
  {
    id: 1,
    name: RequisitionTypes.VMWare
  }
]

export const requisitionSubTypes = [
  {
    id: 2,
    name: RequisitionPluginSubTypes.ACI,
    value: 'aci'
  },
  {
    id: 4,
    name: RequisitionPluginSubTypes.AzureIot,
    value: 'azure-iot'
  },
  {
    id: 1,
    name: RequisitionPluginSubTypes.OpenDaylight,
    value: 'opendaylight'
  },
  {
    id: 5,
    name: RequisitionPluginSubTypes.PRIS,
    value: 'pris'
  },
  {
    id: 3,
    name: RequisitionPluginSubTypes.Zabbix,
    value: 'zabbix-lab'
  }
]
