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
import { scheduleTypes, weekTypes, weekNameTypes, dayTypes } from './copy/scheduleTypes'
import cronstrue from 'cronstrue'
import ipRegex from 'ip-regex'
import isValidDomain from 'is-valid-domain'

const cronTabLength = (cronTab: string) => {
  if(!cronTab) return 0

  return cronTab.replace(/\s$/, '').split(' ').length
}

const obfuscatePassword = (url: string) => {
  const regexPasswordValue = /(password=)([^&]+)/
  const matched = url.match(regexPasswordValue) || []
  
  if(matched.length > 0) {
    const passwordObfuscated = '*'.repeat(matched[2].length)

    return url.replace(regexPasswordValue, `$1${passwordObfuscated}`)
  }

  return url
}

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

const isCronAdvancedFormat = (cronFormatted: string) => {
  const cronFormattedList = cronFormatted.split(' ')
  const [sec,,, DoM, mth, DoW, yr] = cronFormattedList // yr (7th part: 1970-2099): can't be set in UI
  
  const regexDoMWeekdays = /\d+W/g
  const regexDoWLastNthDay = /[L#]/g
  const regexAnyOtherSpecChars = /[,-/]/g

  const hasSec = parseInt(sec) > 0 // sec: can't be set in UI
  const hasMth = mth !== '*' // specific month can't be set in UI
  const hasDoMWeekdays = regexDoMWeekdays.test(DoM) // 15W (the nearest weekday to the 15th of the month): can't be set in UI
  const hasDoWLastNthDay = regexDoWLastNthDay.test(DoW) // L and #: can't be set in UI
  const hasAnyOtherSpecChars = cronFormattedList.some((p) => regexAnyOtherSpecChars.test(p)) // [,-/]: can't be set in UI

  return hasSec || hasMth || hasDoMWeekdays || hasDoWLastNthDay || hasAnyOtherSpecChars || yr
}

/**
 * ['0', '45', '15', '?', '*', '7', 2022]
 * [sec   min   hr   DoM   mth  DoW, yr]
 * @param cronFormatted A Crontab string
 * @returns A formatted object for display to humans
 */
const convertCronTabToLocal = (cronFormatted: string) => {
  const cronFormattedList = cronFormatted.split(' ') 
  const [, min, hr, DoM] = [...cronFormattedList]
  let [,,,,, DoW] = [...cronFormattedList]
  
  const occuranceEmptyProps = {
    name: '',
    id: 0
  }
  const occuranceSection = {
    occurance: occuranceEmptyProps,
    occuranceDay: occuranceEmptyProps,
    occuranceWeek: occuranceEmptyProps
  }

  const prefixZero = (num: number) => {
    if(num >= 10) return num

    return `0${num}`
  }
  
  let advancedProps = {
    advancedCrontab: false,
    occuranceAdvanced: ''
  }

  let time = `${prefixZero(parseInt(hr))}:${prefixZero(parseInt(min))}`

  if(isCronAdvancedFormat(cronFormatted)) {
    advancedProps = {
      advancedCrontab: true,
      occuranceAdvanced: cronFormatted
    }
    
    time = '00:00'
  } else {
    const hasDoM = (dayOfMonth: string) => {
      const regexDoM = /[1-31L]/g
  
      return regexDoM.test(dayOfMonth)
    }
  
    /**
     * SUN...SAT pattern: additional expression support for the UI basic mode, if Day of Week (DoW) was set with name (SUN...SAT) in advanced mode.
     * Note
     *  - edit a requisition: when expression contains SUN...SAT, drawer will be opened in UI basic mode and expression containing SUN...SAT will be translated to 1...7 and set in Day of Week input field.
     * @param dayOfWeek (string) Can be 1...7 or SUN...SAT
     * @returns (boolean) Determine if day of week is set in the expression
     */
    const hasDoW = (dayOfWeek: string) => {
      const regexDoW = /([1-7])|(SUN)|(MON)|(TUE)|(WED)|(THU)|(FRI)|(SAT)/g
      const dowMatched = dayOfWeek.match(regexDoW) 
      
      if(!dowMatched) return false
  
      if(!Number.isInteger(Number(dayOfWeek))) {
        DoW = (weekNameTypes.find((d) => d.name === dowMatched[0]) || {}).id?.toString() || '?'
      }
  
      return true
    }
    
    if(hasDoW(DoW)) {
      occuranceSection.occurance = scheduleTypes.find((d) => d.name === 'Weekly') || occuranceEmptyProps
      occuranceSection.occuranceWeek = weekTypes.find((d) => d.id === parseInt(DoW)) || occuranceEmptyProps
    } else if(hasDoM(DoM)) {
      occuranceSection.occurance = scheduleTypes.find((d) => d.name === 'Monthly') || occuranceEmptyProps
      occuranceSection.occuranceDay = dayTypes.find((d) => d.id === (DoM === 'L' ? 32 : parseInt(DoM))) || occuranceEmptyProps
    } else {
      occuranceSection.occurance = scheduleTypes.find((d) => d.name === 'Daily') || occuranceEmptyProps
    }
  }

  return {
    ...occuranceSection,
    ...advancedProps,
    time,
    twentyFourHour: time,
    monthly: DoM === 'L' ? 32 : DoM, // 32: id of last day of the month
    weekly: DoW
  }
}

/**
 * In cases where query can also be set in Advanced Options section, the latter takes precedence over the one that is set in Path|Username|Password input field, if both contain same key=value (e.g. HTTP/HTTPS/VMWare external source type)
 * @param queryPart URL part after ? (? is not included)
 * @param advancedOptions array of kv pairs
 * @returns Query string to append to server URL
 */
const getQueryStringFromAdvancedOptions = (queryPart = '', advancedOptions: AdvancedOption[], type = ''): string => {
  let optionString = ''
  let queryPartList = queryPart.split('&')
  
  advancedOptions.forEach(({key, value}) => {
    if(key.name && value) {
      optionString += (optionString?.length > 0 ? '&' : '').concat(`${key.name}=${value}`)
    }

    if(type === RequisitionTypes.VMWare) {
      // if username and/or password is set in both input field and in Advanced Options section, then remove it from query Part, hence the one in Advanced Options takes precedence
      queryPartList = queryPart.split('&').filter((q) => !q.includes(key.name))
    }
  })

  if(queryPart?.length === 0 && optionString?.length === 0) return ''

  const optionsStringSplit = optionString?.split('&')
  const uniqueQuery = [...new Set([...queryPartList, ...optionsStringSplit])].filter((q) => q.length > 0)

  return `?${uniqueQuery.join('&')}`
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
  const path = localItem.urlPath.split('?')[0]
  let query = localItem.urlPath.split('?')[1]

  if(type === RequisitionTypes.DNS) {
    host = `${localItem.host}/${localItem.zone || ''}`
    if (localItem.foreignSource) {
      host += `/${localItem.foreignSource}`
    }
  } else if(type === RequisitionTypes.File) {
    host = `${localItem.path}`
  } else if(type === RequisitionTypes.HTTP || type === RequisitionTypes.HTTPS) {
    host = `${localItem.host}${path}`
  } else if(type === RequisitionTypes.RequisitionPlugin) {
    // Note: the following needs to be revalidated to ensure it's working as expected once the option is reactivated in the External Source input field
    host = localItem.subType.value
    protocol = RequisitionTypes.RequisitionPluginForServer
  } else if(type === RequisitionTypes.VMWare) {
    host = localItem.host

    if (localItem.foreignSource) {
      host += `/${localItem.foreignSource}`
    }

    // username/password if set in UI input field: translated to have/save them as query part of the URL
    const usernameQuery = localItem.username ? `${VMWareFields.Username}=${localItem.username}` : ''
    const passwordQuery = localItem.password ? `${VMWareFields.Password}=${localItem.password}` : ''

    query = usernameQuery && passwordQuery ? `${usernameQuery}&${passwordQuery}` : usernameQuery || passwordQuery
  }
  
  const fullURL = `${protocol}://${host}`

  // File type accepts all characters as path value, including separator character (?), which also means it does not have url query part. Hence we just return the path content as is.
  if(type === RequisitionTypes.File) return fullURL

  const queryString = getQueryStringFromAdvancedOptions(query, localItem.advancedOptions, type)

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

  // only translate if expression is in basic format
  if(!item.advancedCrontab) {
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
        schedule = '0 0 0 * * ?' // 'sec min hr DoM mth DOW' (occurance input fields empty)
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

  let urlPath = ''
  for (let i = 3; i < url.length; i++) {
    urlPath += '/' + url[i]
  }

  const path = urlPath.split('?')[0]
  const typeRaw = url[0].split(':')[0]
  localConfig.type = findFullType(typeRaw)

  switch (localConfig.type.name) {
    case RequisitionTypes.File:
      localConfig.path = urlPath
      break
    case RequisitionTypes.VMWare:
      localConfig = findVMware(localConfig, urlIn)
      break
    case RequisitionTypes.RequisitionPlugin:
      localConfig.subType = findSubType(url)
      break
    case RequisitionTypes.DNS:
      localConfig = findDNS(localConfig, urlIn)
      break
    case RequisitionTypes.HTTP:
      localConfig.host = findHost(url)
      localConfig.urlPath = path
      break
    case RequisitionTypes.HTTPS:
      localConfig.host = findHost(url)
      localConfig.urlPath = path
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

  const tabLength = cronTabLength(cronFormatted)
  if (tabLength > 0 && tabLength < 6 ) {
    error = ErrorStrings.QuartzFormatSupportError(tabLength) // custom error of 6th part quartz format support (i.e. cronstrue package supports 6th part as optional, thus no error message if expression contains only 5 parts)
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
 * indicating whether or not to filter this result from the list.
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
  return findDNSorVMware(currentConfig, urlIn, true)
}

/**
 *
 * @param currentConfig Current Local Configuration Object
 * @param urlIn Full URL In
 * @returns Current Local Configuration with updated VMware related fields.
 */
const findVMware = (currentConfig: LocalSubConfiguration, urlIn: string) => {
  return findDNSorVMware(currentConfig, urlIn, false)
}

/**
 *
 * @param currentConfig Current Local Configuration Object
 * @param urlIn Full URL In
 * @param isDNS true if DNS configuration, false if VMware configuration
 * @returns Current Local Configuration with updated DNS or VMware related fields.
 */
const findDNSorVMware = (currentConfig: LocalSubConfiguration, urlIn: string, isDNS: boolean) => {
  const localConfig = { ...currentConfig }

  const splitType = isDNS ? SplitTypes.dns : SplitTypes.vmware

  const urlPart = urlIn.split(splitType)[1].split('/')
  localConfig.host = urlPart[0]

  if (isDNS) {
    localConfig.zone = urlPart[1]
  }

  const urlIndex = isDNS ? 2 : 1

  if (urlPart[urlIndex]) {
    if (urlPart[urlIndex].includes('?')) {
      localConfig.foreignSource = urlPart[urlIndex].split('?')[0]
    } else {
      localConfig.foreignSource = urlPart[urlIndex]
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
  } else if (type === RequisitionTypes.HTTPS || type === RequisitionTypes.HTTP || type === RequisitionTypes.VMWare) {
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
  // File type does not support url query part (?key=value&...), hence no advanced options parsing is required
  if(type === RequisitionTypes.File) return []

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
 * Validates a Hostname. Can be an IP address or valid domain name.
 * @param host Hostname
 * @returns Blank if Valid, Error Message if Not.
 */
const validateHost = (host: string) => {
    const atIndex = host.indexOf('@');
      let credentials = '';
      let hostPart = host;

      if (atIndex !== -1) {
        credentials = host.slice(0, atIndex);
        hostPart = host.slice(atIndex + 1);

        // Must match user:pass with either static or ${...}
        const credRegex = /^(\$\{[^}]+\}|\w+):(\$\{[^}]+\}|\S+)$/;
        if (!credRegex.test(credentials)) {
          return ErrorStrings.InvalidHostname;
        }
      }

      // Extract optional port
      let hostname = hostPart;
      let port: number | undefined;

      // IPv6 with port
      const ipv6PortMatch = hostPart.match(/^\[([a-fA-F0-9:]+)\]:(\d+)$/);
      if (ipv6PortMatch) {
        hostname = `[${ipv6PortMatch[1]}]`;
        port = parseInt(ipv6PortMatch[2], 10);
      } else {
        // Generic port split, but not for IPv6
        const lastColonIndex = hostPart.lastIndexOf(':');
        if (lastColonIndex !== -1 && hostPart[lastColonIndex - 1] !== ']') {
          hostname = hostPart.substring(0, lastColonIndex);
          const portStr = hostPart.substring(lastColonIndex + 1);
          if (!/^\d+$/.test(portStr)) return ErrorStrings.InvalidHostname;
          port = parseInt(portStr, 10);
        }
      }

      // Validate port range
      if (port !== undefined && (isNaN(port) || port < 0 || port > 65535)) {
        return ErrorStrings.InvalidHostname;
      }

      // Remove brackets for IPv6 and validate
      const rawHostname = hostname.replace(/^\[|\]$/g, '');

      const doubleDot = rawHostname.includes('..');
      const leadingTrailingDotHyphen = /^[.-]|[.-]$/.test(rawHostname);
      if (doubleDot || leadingTrailingDotHyphen) {
        return ErrorStrings.InvalidHostname;
      }

      // Check for IPv4
      const ipv4Regex = /^(\d{1,3}\.){3}\d{1,3}$/;
      const isIPv4 = ipv4Regex.test(rawHostname) &&
        rawHostname.split('.').every(n => +n >= 0 && +n <= 255);

      // Check for IPv6
      const ipv6Regex = /^([a-fA-F0-9]{1,4}:){7}[a-fA-F0-9]{1,4}$/;
      const isIPv6 = ipv6Regex.test(rawHostname);

      // Check for valid domain
      const domainRegex = /^(?!.*\.\.)(?![.-])(?!.*[.-]$)[a-zA-Z\d](?:[a-zA-Z\d\-]{0,61}[a-zA-Z\d])?(?:\.[a-zA-Z\d](?:[a-zA-Z\d\-]{0,61}[a-zA-Z\d])?)*$/;
      const isDomain = domainRegex.test(rawHostname);

      // Allow template vars as full hostnames if no dots/hyphens to validate
      const isTemplate = /^\$\{[^}]+\}$/.test(rawHostname);

      // Final decision
      if (isIPv4 || isIPv6 || isDomain || isTemplate) {
        return '';
        }
}


/**
 * Field required
 * Field invalid if:
 *  - has space as first or last character
 *  - has: not alpha, digit, _, -, and .
 * @param fieldVal Field value
 * @returns Blank if valid, else error message
 */
const validateZoneField = (fieldVal: string) => {
  const regex = /^[\s]|[^\w\d\-.\s]|[\s]$/

  return !fieldVal || regex.test(fieldVal) ? ErrorStrings.InvalidZoneName : ''
}

/**
 * Field not required
 * Field invalid if:
 *  - has space as first or last character
 *  - has: /, \, ?, &, *, ', "
 * @param fieldVal field value
 * @returns Blank if valid, else error message
 */
const validateRequisitionNameField = (fieldVal: string) => {
  const regex = /^[\s]|[/\\?&*'"]|[\s]$/
  
  return regex.test(fieldVal) ? ErrorStrings.InvalidRequisitionName : ''
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
      if (!localItem.host) {
        errors.host = ErrorStrings.Required('Host')
      } else {
        errors.host = validateHost(localItem.host)
      }
    }
    if (RequisitionHTTPTypes.includes(localItem.type.name) && localItem.urlPath) {
      errors.urlPath = validatePath(localItem.urlPath)
    }
    if (localItem.type.name === RequisitionTypes.DNS) {
      errors.zone = validateZoneField(localItem.zone)

      // Only validate foreign source if it's set.
      if (localItem.foreignSource) {
        errors.foreignSource = validateRequisitionNameField(localItem.foreignSource)
      }
    }
    if (localItem.type.name === RequisitionTypes.File) {
      errors.path = validatePath(localItem.path)
    }
    if (localItem.type.name === RequisitionTypes.VMWare) {
      if (localItem.username && !localItem.password) {
        errors.password = ErrorStrings.Required(VMWareFields.UpperPassword)
      }
      if (localItem.password && !localItem.username) {
        errors.username = ErrorStrings.Required(VMWareFields.UpperUsername)
      }

      // Always validate Requisition Name / Foreign Source Name
      if (!localItem.foreignSource) {
        errors.foreignSource = ErrorStrings.Required(VMWareFields.RequisitionName)
      } else {
        errors.foreignSource = validateRequisitionNameField(localItem.foreignSource)
      }
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
    nameError = ErrorStrings.Required(nameType)
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
  return !occuranceName ? ErrorStrings.Required('Schedule') : ''
}

/**
 *
 * @param occuranceName Occurance selected (Monthly/Daily/Weekly)
 * @returns Message if empty, empty string on value.
 */
const validateOccuranceDay = (occuranceName: string) => {
  return !occuranceName ? ErrorStrings.Required('Day of the month') : ''
}

/**
 *
 * @param occuranceName Occurance selected (Monthly/Daily/Weekly)
 * @returns Message if empty, empty string on value.
 */
const validateOccuranceWeek = (occuranceName: string) => {
  return !occuranceName ? ErrorStrings.Required('Day of the week') : ''
}

/**
 *
 * @param path File path to validate
 * @returns Message if error, empty string on valid.
 */
const validatePath = (path: string) => {
  let pathError = ''
  if (!path) {
    pathError = ErrorStrings.Required('File path')
  } else if (!path.startsWith('/')) {
    pathError = ErrorStrings.FilePathStart
  } else if(/[?]/gm.test(path)) {
    pathError = ErrorStrings.FilePathWithQueryChar
  }
  return pathError
}

/**
 *
 * @param typeName Type selected (File,DNS,HTTP...)
 * @returns Message if empty, empty string on value.
 */
const validateType = (typeName: string) => {
  return !typeName ? ErrorStrings.Required('Type') : ''
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
  obfuscatePassword,
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
  validateZoneField,
  validateRequisitionNameField,
  validateLocalItem,
  validateName,
  validateOccurance,
  validatePath,
  validateType,
  copyToClipboard
}
