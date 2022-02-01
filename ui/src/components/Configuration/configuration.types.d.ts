type ConfigurationTableSort = {
  property: string
  value: string
}

type ProvisionDServerConfiguration = {
  [b: string]: string | ConfigurationTableSort | number | Array
  'import-name': string
  'cron-schedule': string
  'import-url-resource': string
  'rescan-existing': string
  currentSort: ConfigurationTableSort
  originalIndex: number | undefined
}

type ConfigurationResponse = { error: boolean; message: unknown }
type FullProvisionDPayload = {}

type ConfigurationPageVals = {
  total: number
  page: number
  pageSize: number
}

type AdvancedOption = {
  key: { _text: string; name: string }
  value: string
}

type AdvancedKey = {
  _text: string
  name: string
  id: number
}

type LocalConfiguration = {
  name: string
  path: string
  type: { name: string; id: number }
  subType: { name: string; value: string; id: number }
  host: string
  username: string
  password: string
  occurance: { name: string; id: number }
  time: string
  rescanBehavior: number
  advancedOptions: Array<AdvancedOption>
  zone: string
  foreignSource: string
}

type LocalErrors = {
  hasErrors: boolean
  host: string
  name: string
  path: string
  username: string
  password: string
  type: string
  zone: string
  foreignSource: string
}

type LocalConfigurationWrapper = {
  config: LocalConfiguration
  errors: LocalErrors
}
