import {
  AdvancedOption,
  LocalConfiguration,
  LocalConfigurationWrapper,
  LocalErrors,
  LocalSubConfiguration,
  ProvisionDServerConfiguration
} from './configuration.types'
import { aciKeys, dnsKeys, openDaylightKeys, prisKeys } from './copy/advancedKeys'
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
  RequisitionTypes,
  RequisitionPluginSubTypes,
  RequisitionHTTPTypes
} from './copy/requisitionTypes'
import { scheduleTypes, weekTypes, dayTypes } from './copy/scheduleTypes'
import cronstrue from 'cronstrue'

const cronTabLength = (cronTab: string) => cronTab.replace(/\s$/, '').split(' ').length

/**
 *
 * @param name The name we're looking for
 * @param existingError Any existing error message that's already been set.
 * @param existingList Our full list of ProvisionD Configuration Items
 * @returns An error message if we found a duplicate name.
 */
const checkForDuplicateName = (
  name: string,
  existingError: string,
  existingList: Array<ProvisionDServerConfiguration>,
  activeIndex: number
) => {
  let errorMessage = existingError
  existingList.some((item, index) => {
    if (name === item[RequisitionData.ImportName] && activeIndex !== index) {
      errorMessage = ErrorStrings.DuplicateName
      return true
    }
  })
  return errorMessage
}

/**
 * ['0', '45', '15', '?', '*', '7']
 * [sec   min   hr   DOM   mth  DOW]
 * @param cronFormatted A Crontab string
 * @returns A formatted object for display to humans
 */
const convertCronTabToLocal = (cronFormatted: string) => {
  const cronFormatterList = cronFormatted.split(' ') 
  const [sec, min, hr, DOM, mth, DOW] = [...cronFormatterList]
  const occuranceEmptyProps = {
    name: '',
    id: 0
  }
  const occuranceSection = {
    occurance: occuranceEmptyProps,
    occuranceDay: occuranceEmptyProps,
    occuranceWeek: occuranceEmptyProps
  }

  const hasDOM = (dayOfMonth: string) => {
    const regexDOM = /[1-31L]/g

    return regexDOM.test(dayOfMonth)
  }
  const hasDOW = (dayOfWeek: string) => {
    const regexDOW = /[1-7]/g
    
    return regexDOW.test(dayOfWeek)
  }
  if(hasDOW(DOW)) {
    occuranceSection.occurance = scheduleTypes.find((d) => d.name === 'Weekly') || occuranceEmptyProps
    occuranceSection.occuranceWeek = weekTypes.find((d) => d.id === parseInt(DOW)) || occuranceEmptyProps
  } else if(hasDOM(DOM)) {
    occuranceSection.occurance = scheduleTypes.find((d) => d.name === 'Monthly') || occuranceEmptyProps
    occuranceSection.occuranceDay = dayTypes.find((d) => d.id === (DOM === 'L' ? 32 : parseInt(DOM))) || occuranceEmptyProps
  } else {
    occuranceSection.occurance = scheduleTypes.find((d) => d.name === 'Daily') || occuranceEmptyProps
  }

  const isCronAdvancedFormat = () => {
    const regexDOMWeekdays = /\d+W/g
    const regexDOWLastNthDay = /[L#]/g
    const regexDOWCharValues = /[SUN|MON|TUE|WED|THU|FRI|SAT]/g
    const regexAnyOtherSpecChars = /[,-/]/g

    return (
      parseInt(sec) > 0
      || mth !== '*' // specific month can't be set in UI
      || regexDOMWeekdays.test(DOM) // 15W (the nearest weekday to the 15th of the month): can't be set in UI
      || regexDOWLastNthDay.test(DOW) // L and #: can't be set in UI
      || regexDOWCharValues.test(DOW) // todo: condition to be removed, once the support is implemented
      || regexAnyOtherSpecChars.test(DOW) // can't be set in UI
      || cronFormatterList.length > 6 // Year (7th part: 1970-2099): can't be set in UI
    )
  }
  let advancedProps = {
    advancedCrontab: false,
    occuranceAdvanced: ''
  }
  if(isCronAdvancedFormat()) {
    advancedProps = {
      advancedCrontab: true,
      occuranceAdvanced: cronFormatted
    } 
  }

  const prefixZero = (num: number) => {
    if(num >= 10) return num

    return `0${num}`
  }
  const time = `${prefixZero(parseInt(hr))}:${prefixZero(parseInt(min))}`

  return {
    ...occuranceSection,
    ...advancedProps,
    time,
    twentyFourHour: time,
    monthly: DOM === 'L' ? 32 : DOM, // 32: id of last day of the month
    weekly: DOW,
  }
}

/**
 * @param fullURL server URL
 * @param advancedOptions array of kv pairs
 * @returns Query string to append to server URL
 */
const getQueryStringFromAdvancedOptions = (fullURL: string, advancedOptions: AdvancedOption[]): string => {
  const hasQueryParams = () => fullURL.includes('?')

  let queryString = ''
  advancedOptions.forEach((option, index) => {
    if (option.key.name && option.value) {
      queryString += `${index > 0 || hasQueryParams() ? '&' : '?'}${option.key.name}=${option.value}`
    }
  })

  return queryString
}

/**
 * Takes in the local form object, and spits out a URL ready for the server.
 * The logic in here is based on design specifications from JeffG
 * @param localItem Our Client/Local version of the Server Configuration
 * @returns A URL ready for the server
 */
const convertItemToURL = (localItem: LocalConfiguration) => {
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
    host = `${localItem.host}?${VMWareFields.Username}=${localItem.username}&${VMWareFields.Password}=${localItem.password}&`
  } else if (type === RequisitionTypes.File) {
    host = `${localItem.path}`
  } else if (type === RequisitionTypes.HTTP || type === RequisitionTypes.HTTPS) {
    host = `${localItem.host}${localItem.urlPath}`
  }

  const fullURL = `${protocol}://${host}`
  const queryString = getQueryStringFromAdvancedOptions(fullURL, localItem.advancedOptions)
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
const convertLocalToCronTab = (item: LocalConfiguration) => {
  let schedule = ''

  if (!item.advancedCrontab) {
    const occurance = item.occurance
    const time = item.time
    const [hoursd, minutesd] = time.split(':')
    const hours = parseInt(hoursd)
    const minutes = parseInt(minutesd)

    switch(occurance.name) {
      case 'Daily':
        schedule = `0 ${minutes} ${hours} * * ?`
        break
      case 'Weekly':
        schedule = `0 ${minutes} ${hours} ? * ${item.occuranceWeek.id}`
        break
      case 'Monthly':
        schedule = `0 ${minutes} ${hours} ${item.occuranceDay.id === 32 ? 'L' : item.occuranceDay.id} * ?`
        break
      default:
        // basic mode, at drawer open
        schedule = '0 0 0 * * ?' // 'sec min hr DOM mth DOW' (occurance input fields empty)
    }
  } else {
    // advanced mode
    schedule = item.occuranceAdvanced
  }

  return schedule
}

/**
 * This function acts as a DTO for Local -> Server.
 * @param localItem Our Local Configuration/ProvisionD Object
 * @param stripIndex Should we strip Non-Server values from the object?
 * @returns An object ready to be sent to the server.
 */
const convertLocalToServer = (localItem: LocalConfiguration, stripIndex = false) => {
  const schedule = convertLocalToCronTab(localItem)
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
const convertServerConfigurationToLocal = (clickedItem: ProvisionDServerConfiguration): LocalConfiguration => {
  let rescanBehavior = 1
  if (clickedItem?.[RequisitionData.RescanExisting] === RescanVals.False) {
    rescanBehavior = 0
  } else if (clickedItem?.[RequisitionData.RescanExisting] === RescanVals.DBOnly) {
    rescanBehavior = 2
  }

  const {
    occurance,
    occuranceWeek,
    occuranceDay,
    twentyFourHour: time,
    occuranceAdvanced,
    advancedCrontab
  } = convertCronTabToLocal(clickedItem[RequisitionData.CronSchedule])

  const urlVars = convertURLToLocal(clickedItem[RequisitionData.ImportURL])
  
  return {
    name: clickedItem[RequisitionData.ImportName],
    rescanBehavior,
    occurance,
    occuranceWeek,
    occuranceDay,
    advancedCrontab,
    occuranceAdvanced,
    time,
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
const convertURLToLocal = (urlIn: string) => {
  let localConfig: LocalSubConfiguration = createBlankSubConfiguration()

  const url = urlIn.split('/')
  const typeRaw = url[0].split(':')[0]
  let urlPath = ''
  for (let i = 3; i < url.length; i++) {
    urlPath += '/' + url[i]
  }
  localConfig.type = findFullType(typeRaw)
  switch (localConfig.type.name) {
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
      localConfig = findDNS(localConfig, urlIn)
      break
    case RequisitionTypes.HTTP:
      localConfig.host = findHost(url)
      localConfig.urlPath = urlPath
      break
    case RequisitionTypes.HTTPS:
      localConfig.host = findHost(url)
      localConfig.urlPath = urlPath
      break
  }

  /**
   * Get our Advanced Options from the Query parameters.
   * While we're in there, if the type is set to VMWare,
   * remove username/password and attach them as fully-fledged
   * form elements.
   */
  const advancedOptions = parseAdvancedOptions(urlIn, localConfig.type.name, localConfig.subType.name).filter(
    (item) => {
      const { newOptions, keepItem } = filterForVMWareValues(localConfig.type.name, item, localConfig)
      localConfig = newOptions
      return keepItem
    }
  )

  return { ...localConfig, advancedOptions }
}

/**
 * Create an empty/blank Configuration/ProvisionD error object.
 * @returns An empty/blank Error Object
 */
const createBlankErrors: () => LocalErrors = () => {
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
    occurance: '',
    occuranceWeek: '',
    occuranceAdvanced: '',
    occuranceDay: '',
    urlPath: ''
  }
}

/**
 *
 * @returns An empty/blank Configuration/ProvisionD Form Object
 */
const createBlankLocal: () => LocalConfigurationWrapper = () => {
  return {
    config: {
      ...createBlankSubConfiguration(),
      name: '',
      occurance: { name: '', id: 0 },
      occuranceWeek: { name: '', id: 0 },
      occuranceDay: { name: '', id: 0 },
      occuranceAdvanced: '',
      advancedCrontab: false,
      time: '00:00',
      rescanBehavior: 1,
      advancedOptions: [{ key: { name: '', _text: '' }, value: '', hint: '' }]
    },
    errors: createBlankErrors()
  }
}

/**
 *
 * @returns Smaller part of our full Local Configuration item for ProvisionD
 */
const createBlankSubConfiguration = () => {
  return {
    host: '',
    path: '',
    username: '',
    password: '',
    zone: '',
    foreignSource: '',
    subType: { id: 0, name: '', value: '' },
    type: { name: '', id: 0 },
    urlPath: ''
  }
}

/**
 * Convert our Cron Schedules to Human Readable String.
 */
const cronToEnglish = (cronFormatted: string) => {
  let error = ''

  if (cronTabLength(cronFormatted) === 5) {
    error = ErrorStrings.QuartzFormatSupportError // custom error of 6th part quartz format support
  } else {
    try {
      error = cronstrue.toString(cronFormatted, { dayOfWeekStartIndexZero: false })
    } catch (e) {
      error = typeof e === 'string' ? e : 'Error Parsing Crontab'
    }
  }
  
  return error
}

/**
 *
 * @param type Type of Requisition
 * @param item Advanced Option
 * @param fullItem Local Configuration Object
 * @returns Local Configuration Object with attached username/password and boolean
 * indiciating whether or not to filter this result from the list.
 */
const filterForVMWareValues = (
  type: string,
  item: { key: { name: string }; value: string },
  fullItem: LocalSubConfiguration
) => {
  const newOptions = { ...fullItem }
  let keepItem = true
  if (type === RequisitionTypes.VMWare) {
    if (item.key.name === VMWareFields.Username) {
      newOptions.username = item.value
      keepItem = false
    }
    if (item.key.name === VMWareFields.Password) {
      newOptions.password = item.value
      keepItem = false
    }
  }
  return { newOptions, keepItem }
}

/**
 *
 * @param currentConfig Current Local Configuration Object
 * @param urlIn Full URL In
 * @returns Current Local Configuration with updated DNS Related fields.
 */
const findDNS = (currentConfig: LocalSubConfiguration, urlIn: string) => {
  const localConfig = { ...currentConfig }

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
 * @param typeRaw Find the Server Type from our Local list.
 * @returns The Type value, ready for the ProvisionD Form.
 */
const findFullType = (typeRaw: string) => {
  let type = requisitionTypeList.find((item) => {
    let match = item.name.toLowerCase() === typeRaw
    if (item.name === RequisitionTypes.RequisitionPlugin && typeRaw === RequisitionTypes.RequisitionPluginForServer) {
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
 * @param url A full URL split by '/'
 * @returns The hostname portion of the URL
 */
const findHost = (url: Array<string>) => {
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
 * @param urlIn
 * @returns A File Path from the URL
 */
const findPath = (urlIn: string) => {
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
 * @returns The requisition subtype if it exists
 */
const findSubType = (url: Array<string>) => {
  let subType = { id: 0, name: '', value: '' }
  const typeRaw = url[2].split('?')[0]
  const foundSubType = requisitionSubTypes.find((item) => item.value.toLowerCase() === typeRaw)
  if (foundSubType) {
    subType = foundSubType
  }
  return subType
}

/**
 * This is a workaround for FeatherInput
 * Attributes are not tracked reactively and
 * therefore do not update after the initial render.
 */
const forceSetHint = (key: { hint: string }, index: number, wrapperClass = '.hint-label') => {
  const labels = document.querySelectorAll(wrapperClass)
  if (labels && labels[index]) {
    const hintLabel = labels[index].querySelector('.feather-input-hint')
    if (hintLabel) {
      hintLabel.textContent = key.hint
    }
  }
}

/**
 *
 * @param type Currently set Type of Requisition
 * @returns The hint for the host field if there is one.
 */
const getHostHint = (type: string) => {
  let hintText = ''
  if (type === RequisitionTypes.DNS) {
    hintText = 'DNS resolver host to contact. Must allow IXFR or AXFR zone transfers.'
  } else if (type === RequisitionTypes.HTTPS || type === RequisitionTypes.HTTP) {
    hintText = 'Hostname or IP address'
  }
  return hintText
}

/**
 *
 * @param fullURL A full URL with query string parameters (advanced options)
 * @returns An Object we can use in our Advanced Options section of the ProvisionD Form
 */
const parseAdvancedOptions = (fullURL: string, type: string, subType: string) => {
  return fullURL.includes('?')
    ? fullURL
      .split('?')[1]
      .split('&')
      .map((fullString) => {
        const [key, value] = fullString.split('=')
        const hint = parseHint(key, type, subType)

        return { key: { _text: key, name: key }, value, hint }
      })
    : []
}

const parseHint = (key: string, type: string, subType: string) => {
  let keys = [{ name: '', hint: '' }]
  if (type === RequisitionTypes.RequisitionPlugin) {
    if (subType === RequisitionPluginSubTypes.OpenDaylight) {
      keys = openDaylightKeys
    } else if (subType === RequisitionPluginSubTypes.ACI) {
      keys = aciKeys
    } else if (subType === RequisitionPluginSubTypes.PRIS) {
      keys = prisKeys
    }
  } else if (type === RequisitionTypes.DNS) {
    keys = dnsKeys
  }
  return keys.find((d) => d.name === key)?.hint
}

/**
 * Last step before saving to the server.
 * @param dataToUpdate Full Server-Ready, already converted with convertLocalToServer object to be saved to the server.
 * @returns An object for the server without the additional fields we tacked on for context.
 */
const stripOriginalIndexes = (dataToUpdate: Array<ProvisionDServerConfiguration>) => {
  return dataToUpdate.map((item) => {
    delete item.originalIndex
    delete item.currentSort
    return item
  })
}

/**
 * Just ensures that it's a valid quartz crontab.
 * @param cronTab Our advanced crontab field
 * @returns
 */
const validateBasicCron = (cronTab: string) => {
  let error: unknown | string = ''

  if (cronTabLength(cronTab) === 5) {
    error = ErrorStrings.QuartzFormatSupportError // custom error of 6th part quartz format support
  } else {
    try {
      cronstrue.toString(cronTab, { dayOfWeekStartIndexZero: false })
    } catch (e) {
      error = e
    }
  }

  return error
}

/**
 *
 * @param item LocalConfiguration item (Form Content for ProvisionD)
 * @param oldErrors Our existing error object that will be copied.
 * @returns LocalErrors but with some Cron information
 */
const validateCronTab = (item: LocalConfiguration, oldErrors: LocalErrors) => {
  const errors = { ...oldErrors }
  if (item.occurance.name === 'Monthly') {
    errors.occuranceDay = validateOccuranceDay(item.occuranceDay.name)
  } else if (item.occurance.name === 'Weekly') {
    errors.occuranceWeek = validateOccuranceWeek(item.occuranceWeek.name)
  } else {
    errors.occurance = validateOccurance(item.occurance.name)
  }
  return errors
}

/**
 * Validates a Hostname. Can be an IP address or valid hostname.
 * @param host Hostname
 * @returns Blank if Valid, Error Message if Not.
 */
const validateHost = (host: string) => {
  let hostError = ''
  /**
   * Character separators: hyphen, point, space or % (%20: encoded space).
   * Expression only valid if:
   *  - only one separator betwwen word
   *  - no separator at begin, end or between uri and port
   *  - expression names (uri, port) can be used, with expression.match(), to target specific part of the host string
   * */
  const ipv4 = new RegExp(/^[^\s.\-%](?<uri>[\w]*[\s.\-%]?[\w]*[^\s.\-%20:])*(?<port>:\d{4})?$/, 'gmi')
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
const validateLocalItem = (
  localItem: LocalConfiguration,
  existingList: Array<ProvisionDServerConfiguration>,
  activeIndex: number,
  quickUpdate = false
): LocalErrors => {
  let errors: LocalErrors = createBlankErrors()

  if (!quickUpdate) {
    errors.name = validateName(localItem.name)
    errors.name = checkForDuplicateName(localItem.name, errors.name, existingList, activeIndex)
    errors.type = validateType(localItem.type.name)

    if (RequsitionTypesUsingHost.includes(localItem.type.name)) {
      errors.host = validateHost(localItem.host)
    }
    if (RequisitionHTTPTypes.includes(localItem.type.name) && localItem.urlPath) {
      errors.urlPath = validatePath(localItem.urlPath)
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
    if (!localItem.advancedCrontab) {
      errors = validateCronTab(localItem, errors)
    } else {
      const errorMessage = validateBasicCron(localItem.occuranceAdvanced)
      errors.occuranceAdvanced = typeof errorMessage === 'string' ? errorMessage.replace('Error:', '') : ''
    }
  }

  //If any key is set, then we have errors.
  for (const val of Object.entries(errors)) {
    if (val[1]) {
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
const validateOccurance = (occuranceName: string) => {
  return !occuranceName ? ErrorStrings.OccuranceTime : ''
}

/**
 *
 * @param occuranceName Occurance selected (Monthly/Daily/Weekly)
 * @returns Message if empty, empty string on value.
 */
const validateOccuranceDay = (occuranceName: string) => {
  return !occuranceName ? ErrorStrings.OccuranceDayTime : ''
}

/**
 *
 * @param occuranceName Occurance selected (Monthly/Daily/Weekly)
 * @returns Message if empty, empty string on value.
 */
const validateOccuranceWeek = (occuranceName: string) => {
  return !occuranceName ? ErrorStrings.OccuranceWeekTime : ''
}

/**
 *
 * @param path File path to validate
 * @returns Message if error, empty string on valid.
 */
const validatePath = (path: string) => {
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
const validateType = (typeName: string) => {
  return !typeName ? ErrorStrings.TypeError : ''
}

/**
 * Allows for copying to clipboard in both HTTPS & HTTP
 * @param text text to copy
 * @returns promise
 */
const copyToClipboard = (text: string) => {
  // navigator clipboard api needs a secure context (https)
  if (navigator.clipboard && window.isSecureContext) {
    // navigator clipboard api method'
    return navigator.clipboard.writeText(text)
  } else {
    // text area method
    const textArea = document.createElement('textarea')
    textArea.value = text
    // make the textarea out of viewport
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    return new Promise<void>((res, rej) => {
      document.execCommand('copy') ? res() : rej()
      textArea.remove()
    })
  }
}

export const ConfigurationHelper = {
  checkForDuplicateName,
  convertCronTabToLocal,
  convertItemToURL,
  convertLocalToCronTab,
  convertLocalToServer,
  convertServerConfigurationToLocal,
  convertURLToLocal,
  createBlankErrors,
  createBlankLocal,
  cronToEnglish,
  forceSetHint,
  getHostHint,
  parseHint,
  stripOriginalIndexes,
  validateHost,
  validateLocalItem,
  validateName,
  validateOccurance,
  validatePath,
  validateType,
  copyToClipboard
}
