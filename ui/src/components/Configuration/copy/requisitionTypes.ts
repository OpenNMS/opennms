export const RequisitionFields = {
  Name: 'Name'
}
export const RequisitionTypes = {
  VMWare: 'VMWare',
  RequisitionPlugin: 'Requisition Plugin',
  DNS: 'DNS',
  File: 'File',
  HTTP: 'HTTP',
  HTTPS: 'HTTPS',
  RequisitionPluginForServer: 'requisition'
}

export const RequisitionPluginSubTypes = {
  OpenDaylight: 'OpenDaylight',
  ACI: 'ACI',
  Zabbix: 'Zabbix',
  PRIS: 'PRIS',
  AzureIot: 'Azure IoT'
}

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
  UpperPassword: 'Password'
}

export const SplitTypes = {
  dns: 'dns://',
  file: 'file://'
}

export const ErrorStrings = {
  TypeError: 'Must select a type',
  ScheduleTime: 'Must schedule a time',
  InvalidHostname: 'Invalid hostname',
  FilePath: 'Must include a file path',
  FilePathStart: 'Path must start with a /',
  MustHave: (nameType: string) => `Must have a ${nameType.toLocaleLowerCase()}`,
  NameShort: (nameType: string) => `${nameType} must have at least two chars`,
  NameLong: (nameType: string, length: number = 255) => `${nameType} must be shorter than ${length}`
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
  {
    id: 6,
    name: RequisitionTypes.RequisitionPlugin
  },
  {
    id: 1,
    name: RequisitionTypes.VMWare
  }
]

export const requisitionSubTypes = [
  {
    id: 1,
    name: RequisitionPluginSubTypes.OpenDaylight,
    value: 'opendaylight'
  },
  {
    id: 2,
    name: RequisitionPluginSubTypes.ACI,
    value: 'aci'
  },
  {
    id: 3,
    name: RequisitionPluginSubTypes.Zabbix,
    value: 'zabbix-lab'
  },
  {
    id: 4,
    name: RequisitionPluginSubTypes.AzureIot,
    value: 'azure-iot'
  },
  {
    id: 5,
    name: RequisitionPluginSubTypes.PRIS,
    value: 'pris'
  }
]
