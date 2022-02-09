export type ConfigurationTableSort = {
  property: string
  value: string
}

export type ProvisionDServerConfiguration = {
  [b: string]: string | ConfigurationTableSort | number | Array
  'import-name': string
  'cron-schedule': string
  'import-url-resource': string
  'rescan-existing': string
  currentSort: ConfigurationTableSort | undefined
  originalIndex: number | undefined
}

export type ConfigurationResponse = { error: boolean; message: unknown }

export type ConfigurationPageVals = {
  total: number
  page: number
  pageSize: number
}

export type AdvancedOption = {
  key: { _text: string; name: string }
  value: string
}

export type AdvancedKey = {
  _text: string
  name: string
  id: number
}

export interface  LocalConfiguration extends LocalSubConfiguration {
  name: string
  occurance: { name: string; id: number }
  time: string
  rescanBehavior: number
  advancedOptions: Array<AdvancedOption>
}

export type LocalErrors = {
  hasErrors: boolean
  host: string
  name: string
  path: string
  username: string
  password: string
  type: string
  zone: string
  foreignSource: string
  occurance: string
}

export type LocalConfigurationWrapper = {
  config: LocalConfiguration
  errors: LocalErrors
}

export type LocalSubConfiguration = {
    host : string
    path : string
    username : string
    password : string
    zone : string
    foreignSource : string
    subType : { id: number, name: string, value: string },
    type:{name:string,id:number}
}
