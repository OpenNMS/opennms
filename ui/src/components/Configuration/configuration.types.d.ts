type ConfigurationTableSort = {
  property: string
  value: string
}

type ProvisionDServerConfiguration = {
  [b: string]: string | ConfigurationTableSort | number
  'import-name': string
  'cron-schedule': string
  'import-url-resource': string
  'rescan-existing': string
  currentSort: ConfigurationTableSort
  originalIndex: number
}

type ConfigurationPageVals = {
  total: number
  page: number
  pageSize: number
}

type AdvancedOption = {
  key: { _text: string; name: string }
  value: string
}

type LocalConfiguration = {
  name: string
  type: { name: string; id: number }
  subType: { name: string; value: string; id: number }
  host: string
  occurance: { name: string; id: number }
  time: string
  rescanBehavior: number
  advancedOptions: Array<AdvancedOption>
}
