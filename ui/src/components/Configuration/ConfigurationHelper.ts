import { LocalConfiguration, LocalErrors, LocalSubConfiguration, ProvisionDServerConfiguration } from './configuration.types'
import {
  RequisitionData,
  RescanVals,
  SplitTypes,
  VMWareFields,
  RequsitionTypesUsingHost,
  ErrorStrings,
  RequisitionFields,
  requisitionSubTypes,
  requisitionTypeList,
  RequisitionTypes
} from './copy/requisitionTypes'


/**
 * Create an empty/blank Configuration/ProvisionD error object.
 * @returns An empty/blank Error Object
 */
const createBlankErrors =  () => {
  return {
    name: '',
    hasErrors: false,
    host: '',
    path: '',
    username: '',
    password: '',
    type: '',
    zone: '',
    foreignSource: '',
    occurance: ''
  }
}

/**
 * 
 * @returns An empty/blank Configuration/ProvisionD Form Object
 */
const createBlankLocal =  () => {
  return {
    config: {
      name: '',
      type: { name: '', id: 0 },
      subType: { value: '', id: 0, name: '' },
      host: '',
      occurance: { name: '', id: 0 },
      time: '00:00',
      rescanBehavior: 1,
      path: '',
      username: '',
      password: '',
      advancedOptions: [{ key: { name: '', _text: '' }, value: '' }],
      zone: '',
      foreignSource: ''
    },
    errors: createBlankErrors()
  }
}

/**
 * 
 * @param cronFormatted A Crontab string
 * @returns A formatted object for display to humans
 */
const convertCronTabToLocal =  (cronFormatted: string) => {
  const split = cronFormatted.split(' ')
  let hour = parseInt(split?.[1] || '')
  let AM = true
  if (hour > 12) {
    AM = false
    hour = hour - 12
  }
  if (hour === 0) {
    hour = 12
  }
  let finalHour = `${hour}`
  if (hour < 12) {
    finalHour = '0' + hour
  }
  const time = finalHour + ':' + split[0] + (AM ? 'AM' : 'PM')
  const monthly = parseInt(split[2])
  const weekly = parseInt(split[4])
  let occurance = 'Daily'
  let occuranceIndex = 0
  if (!isNaN(monthly)) {
    occurance = 'Monthly'
    occuranceIndex = 1
  } else if (!isNaN(weekly)) {
    occurance = 'Weekly'
    occuranceIndex = 2
  }
  return {
    twentyFourHour: split[1] + ':' + split[0],
    time,
    occurance,
    occuranceIndex,
    monthly,
    weekly
  }
}

/**
 * Takes in the local form object, and spits out a URL ready for the server.
 * The logic in here is based on design specifications from JeffG
 * @param localItem Our Client/Local version of the Server Configuration
 * @returns A URL ready for the server
 */
const convertItemToURL =  (localItem: LocalConfiguration) => {
  let protocol = localItem.type.name.toLowerCase()
  const type = localItem.type.name
  let host = localItem.host
  if (type === RequisitionTypes.RequisitionPlugin) {
    host = localItem.subType.value
    protocol = RequisitionTypes.RequisitionPluginForServer
  } else if (type === RequisitionTypes.DNS) {
    host = `${localItem.host}/${localItem.zone || ''}`
    if (localItem.foreignSource) {
      host += `/${localItem.foreignSource}`
    }
  } else if (type === RequisitionTypes.VMWare) {
    host = `${localItem.host}?${VMWareFields.Username}=${localItem.username}&${VMWareFields.Password}=${localItem.password}`
  } else if (type === RequisitionTypes.File) {
    host = `${localItem.path}`
  }

  const fullURL = `${protocol}://${host}`
  let queryString = !fullURL.includes('?') && localItem.advancedOptions.length > 0 ? '?' : ''
  localItem.advancedOptions.forEach((option, index) => {
    queryString += `${index > 0 ? '&' : ''}${option.key.name}=${option.value}`
  })
  return fullURL + queryString
}

/**
 * This method takes in our local form values and spits out a crontab 
 * value ready for the server. This method makes assumptions based 
 * on the provided UI and only lets users set a Daily/Monthy/Weekly
 * schedule which has to assume first day of the week or first day
 * of the month. If additional context options are added to the UI
 * this function can be updated to reflect a more customizable crontab.
 * @param occurance Local Occurance Value
 * @param time Local Time Value
 * @returns crontab-ready string
 */
const convertLocalToCronTab =  (occurance: { name: string }, time: string) => {
  let schedule = '1 * * * * *'
  if (occurance.name === 'Daily') {
    const [hours, minutes] = time.split(':')
    schedule = `${minutes} ${hours} * * *`
  } else if (occurance.name === 'Weekly') {
    const [hours, minutes] = time.split(':')
    schedule = `${minutes} ${hours} * * 0`
  } else if (occurance.name === 'Monthly') {
    const [hours, minutes] = time.split(':')
    schedule = `${minutes} ${hours} 1 * *`
  }
  return schedule
}

/**
 * This function acts as a DTO for Local -> Server.
 * @param localItem Our Local Configuration/ProvisionD Object
 * @param stripIndex Should we strip Non-Server values from the object?
 * @returns An object ready to be sent to the server.
 */
const convertLocalToServer =  (localItem: LocalConfiguration, stripIndex = false) => {
  const occurance = localItem.occurance
  const time = localItem.time
  const schedule = convertLocalToCronTab(occurance, time)
  let rescanVal = RescanVals.True
  if (localItem.rescanBehavior === 0) {
    rescanVal = RescanVals.False
  } else if (localItem.rescanBehavior === 2) {
    rescanVal = RescanVals.DBOnly
  }
  const finalURL = convertItemToURL(localItem)

  const fullRet = {
    [RequisitionData.ImportName]: localItem.name,
    [RequisitionData.ImportURL]: finalURL,
    [RequisitionData.CronSchedule]: schedule,
    [RequisitionData.RescanExisting]: rescanVal,
    currentSort: { property: '', value: '' },
    originalIndex: 0
  } as ProvisionDServerConfiguration

  if (stripIndex) {
    delete fullRet.originalIndex
  }

  return fullRet
}

/**
 * Acts a DTO for Server -> Local
 * @param clickedItem The ServerConfiguration item clicked in the table by the user
 * @returns A ProvisionD form-ready Object.
 */
const convertServerConfigurationToLocal =  (clickedItem: ProvisionDServerConfiguration) => {
  let rescanBehavior = 1
  if (clickedItem?.[RequisitionData.RescanExisting] === RescanVals.False) {
    rescanBehavior = 0
  } else if (clickedItem?.[RequisitionData.RescanExisting] === RescanVals.DBOnly) {
    rescanBehavior = 2
  }
  const { occurance, twentyFourHour: time } = convertCronTabToLocal(
    clickedItem[RequisitionData.CronSchedule]
  )

  const urlVars = convertURLToLocal(clickedItem[RequisitionData.ImportURL])
  return {
    name: clickedItem[RequisitionData.ImportName],
    occurance: { name: occurance, id: 0 },
    time,
    rescanBehavior,
    ...urlVars
  }
}

/**
 * Takes in a ProvisionD Requisition URL and spits
 * out an object that can be used by our form code.
 * Business Logic in here was provided by JeffG
 * @param urlIn A Raw URL from the server
 * @returns An object that can be used in the ProvisionD Form
 */
const convertURLToLocal =  (urlIn: string) => {
  let localConfig: LocalSubConfiguration = {
    host : '',
    path : '',
    username : '',
    password : '',
    zone : '',
    foreignSource : '',
    subType : { id: 0, name: '', value: '' },
    type:{name:'',id:0}
  }
  const url = urlIn.split('/')
  const typeRaw = url[0].split(':')[0]

  localConfig.type = findFullType(typeRaw)
  switch(localConfig.type.name){
    case RequisitionTypes.File:
      localConfig.path = findPath(urlIn)
      break
    case RequisitionTypes.VMWare:
      localConfig.host = findHost(url)
      break
    case RequisitionTypes.RequisitionPlugin:
      localConfig.subType = findSubType(url)
      break
    case RequisitionTypes.DNS:
      localConfig = findDNS(localConfig,urlIn)
      break
    case RequisitionTypes.HTTP:
      localConfig.host = url[2]
      break
    case RequisitionTypes.HTTPS:
      localConfig.host = url[2]
      break
  }
 
  const advancedOptions = gatherAdvancedOptions(urlIn).filter((item) => {
    const {newOptions,keepItem} = filterForVMWareValues(localConfig.type.name,item,localConfig)
    localConfig = newOptions
    return keepItem
  })

  return { ...localConfig, advancedOptions }
}

/**
 * 
 * @param typeRaw Find the Server Type from our Local list.
 * @returns The Type value, ready for the ProvisionD Form.
 */
const findFullType = (typeRaw:string) => {
  let type = requisitionTypeList.find((item) => {
    let match = item.name.toLowerCase() === typeRaw
    if (item.name === RequisitionTypes.RequisitionPlugin) {
      match = true
    }
    return match
  })

  if (!type) {
    type = { id: 0, name: '' }
  }
  return type
}

/**
 * 
 * @param urlIn 
 * @returns A File Path from the URL
 */
const findPath = (urlIn:string) => {
  let path = ''
  const pathPart = urlIn.split(SplitTypes.file)[1]
  if (pathPart.includes('?')) {
    path = pathPart.split('?')[0]
  } else {
    path = pathPart
  }
  return path
}

/**
 * 
 * @param url A full URL split by '/'
 * @returns The hostname portion of the URL
 */
const findHost = (url:Array<string>) => {
  let host = ''
  if (url[2].includes('?')) {
    const vals = url[2].split('?')
    host = vals[0]
  } else {
    host = url[2]
  }
  return host
}

/**
 * 
 * @param url A full URL split by '/'
 * @returns The requisition subtype if it exists
 */
const findSubType = (url:Array<string>) => {
  let subType = {id:0,name:'',value:''}
  const typeRaw = url[2].split('?')[0]
  const foundSubType = requisitionSubTypes.find((item) => item.value.toLowerCase() === typeRaw)
  if (foundSubType) {
    subType = foundSubType
  }
  return subType
}

/**
 * 
 * @param currentConfig Current Local Configuration Object
 * @param urlIn Full URL In
 * @returns Current Local Configuration with updated DNS Related fields.
 */
const findDNS = (currentConfig:LocalSubConfiguration,urlIn:string) => {

  const localConfig = {...currentConfig}

  const urlPart = urlIn.split(SplitTypes.dns)[1].split('/')
  localConfig.host = urlPart[0]
  localConfig.zone = urlPart[1]
  if (urlPart[2]) {
    if (urlPart[2].includes('?')) {
      localConfig.foreignSource = urlPart[2].split('?')[0]
    } else {
      localConfig.foreignSource = urlPart[2]
    }
  }
  return localConfig
}

/**
 * 
 * @param type Type of Requisition
 * @param item Advanced Option
 * @param fullItem Local Configuration Object
 * @returns Local Configuration Object with attached username/password and boolean
 * indiciating whether or not to filter this result from the list.
 */
const filterForVMWareValues = (type:string,item: {key:{name:string},value:string},fullItem: LocalSubConfiguration) => {
  const newOptions = {...fullItem}
  let keepItem = true
  if (type === RequisitionTypes.VMWare) {
    if (item.key.name === VMWareFields.Username) {
      newOptions.username = item.value
      keepItem = false
    }
    if (item.key.name === VMWareFields.Password) {
      newOptions.password = item.value
    }
  }
  return {newOptions,keepItem}
}

/**
 * 
 * @param fullURL A full URL with query string parameters (advanced options)
 * @returns An Object we can use in our Advanced Options section of the ProvisionD Form
 */
const gatherAdvancedOptions =  (fullURL: string) => {
  return fullURL.includes('?')
    ? fullURL
      .split('?')[1]
      .split('&')
      .map((fullString) => {
        const [key, value] = fullString.split('=')
        return { key: { _text: key, name: key }, value }
      })
    : []
}

/**
 * Last step before saving to the server.
 * @param dataToUpdate Full Server-Ready, already converted with convertLocalToServer object to be saved to the server.
 * @returns An object for the server without the additional fields we tacked on for context.
 */
const stripOriginalIndexes =  (dataToUpdate: Array<ProvisionDServerConfiguration>) => {
  return dataToUpdate.map((item) => {
    delete item.originalIndex
    delete item.currentSort
    return item
  })
}

/**
 * Validates a Hostname. Can be an IP address or valid hostname.
 * @param host Hostname
 * @returns Blank if Valid, Error Message if Not.
 */
const validateHost =  (host: string) => {
  let hostError = ''
  const ipv4 = new RegExp(
    /^(([a-z0-9]|[a-z0-9][a-z0-9-]*[a-z0-9])\.)*([a-z0-9]|[a-z0-9][a-z0-9-]*[a-z0-9])(:[0-9]+)?$/gim
  )
  const isHostValid = ipv4.test(host)
  if (!isHostValid) {
    hostError = ErrorStrings.InvalidHostname
  }
  return hostError
}

/**
 * This function validates the local configuration when a
 * user selects Save & Close, or when the user types in any field.
 * If you want the errors to stay on screen until the user clicks Save & Close again
 * remove the quickUpdate functionality below and it will validate on every keystroke.
 * @param localItem Local/ProvisionD Configuration Item
 * @param quickUpdate Just clear the errors when the user starts to type.
 * @returns If the Local Configuration Item is valid (and therefore ready to close the window)
 */
const validateLocalItem =  (localItem: LocalConfiguration, quickUpdate = false): LocalErrors => {
  const errors: LocalErrors = createBlankErrors()

  if (!quickUpdate) {
    errors.name = validateName(localItem.name)
    errors.type = validateType(localItem.type.name)

    if (RequsitionTypesUsingHost.includes(localItem.type.name)) {
      errors.host = validateHost(localItem.host)
    }
    if (localItem.type.name === RequisitionTypes.DNS) {
      errors.zone = validateHost(localItem.zone)

      // Only validate foreign source if it's set.
      if (localItem.foreignSource) {
        errors.foreignSource = validateHost(localItem.foreignSource)
      }
    }
    if (localItem.type.name === RequisitionTypes.File) {
      errors.path = validatePath(localItem.path)
    }
    errors.occurance = validateOccurance(localItem.occurance.name)
  }

  //If any key is set, then we have errors.
  for (const [_, val] of Object.entries(errors)) {
    if (val) {
      errors.hasErrors = true
    }
  }

  return errors
}

/**
 * 
 * @param name Name to validate
 * @param nameType Name of 'Name' field so this can be reused for fields not named 'Name'
 * @returns 
 */
const validateName = (name: string, nameType: string = RequisitionFields.Name) => {
  let nameError = ''
  const maxNameLength = 255
  if (!name) {
    nameError = ErrorStrings.MustHave(nameType)
  }
  if (!nameError && name.length < 2) {
    nameError = ErrorStrings.NameShort(nameType)
  }
  if (!nameError && name.length > maxNameLength) {
    nameError = ErrorStrings.NameLong(nameType, maxNameLength)
  }
  return nameError
}

/**
 * 
 * @param occuranceName Occurance selected (Monthly/Daily/Weekly)
 * @returns Message if empty, empty string on value.
 */
const validateOccurance =  (occuranceName: string) => {
  return !occuranceName ? ErrorStrings.OccuranceTime : ''
}

/**
 * 
 * @param path File path to validate
 * @returns Message if error, empty string on valid.
 */
const validatePath =  (path: string) => {
  let pathError = ''
  if (!path) {
    pathError = ErrorStrings.FilePath
  } else if (!path.startsWith('/')) {
    pathError = ErrorStrings.FilePathStart
  }
  return pathError
}

/**
 * 
 * @param typeName Type selected (File,DNS,HTTP...)
 * @returns Message if empty, empty string on value.
 */

const validateType =  (typeName: string) => {
  return !typeName ? ErrorStrings.TypeError : ''
}

export const ConfigurationHelper = {
  createBlankErrors,
  createBlankLocal,
  convertCronTabToLocal,
  convertItemToURL,
  convertLocalToCronTab,
  convertLocalToServer,
  convertServerConfigurationToLocal,
  convertURLToLocal,
  gatherAdvancedOptions,
  stripOriginalIndexes,
  validateHost,
  validateLocalItem,
  validateName,
  validateOccurance,
  validatePath,
  validateType,
}