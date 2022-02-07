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

export const ConfigurationService = {
  validateName: (name: string, nameType: string = RequisitionFields.Name) => {
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
  },
  validateType: (typeName: string) => {
    let typeError = ''
    if (!typeName) {
      typeError = ErrorStrings.TypeError
    }
    return typeError
  },
  validateOccurance: (typeName: string) => {
    let typeError = ''
    if (!typeName) {
      typeError = ErrorStrings.ScheduleTime
    }
    return typeError
  },
  validateHost: (host: string) => {
    let hostError = ''
    const ipv4 = new RegExp(
      /^(([a-z0-9]|[a-z0-9][a-z0-9\-]*[a-z0-9])\.)*([a-z0-9]|[a-z0-9][a-z0-9\-]*[a-z0-9])(:[0-9]+)?$/gim
    )
    const isHostValid = ipv4.test(host)
    if (!isHostValid) {
      hostError = ErrorStrings.InvalidHostname
    }
    return hostError
  },

  validatePath: (path: string) => {
    let pathError = ''
    if (!path) {
      pathError = ErrorStrings.FilePath
    } else if (!path.startsWith('/')) {
      pathError = ErrorStrings.FilePathStart
    }
    return pathError
  },
  createBlankErrors: () => {
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
  },
  validateLocalItem: (localItem: LocalConfiguration, quickUpdate = false): LocalErrors => {
    const errors: LocalErrors = ConfigurationService.createBlankErrors()

    if (!quickUpdate) {
      errors.name = ConfigurationService.validateName(localItem.name)
      errors.type = ConfigurationService.validateType(localItem.type.name)

      if (RequsitionTypesUsingHost.includes(localItem.type.name)) {
        errors.host = ConfigurationService.validateHost(localItem.host)
      }
      if (localItem.type.name === RequisitionTypes.DNS) {
        errors.zone = ConfigurationService.validateHost(localItem.zone)

        // Only validate foreign source if it's set.
        if (!!localItem.foreignSource) {
          errors.foreignSource = ConfigurationService.validateHost(localItem.foreignSource)
        }
      }
      if (localItem.type.name === RequisitionTypes.File) {
        errors.path = ConfigurationService.validatePath(localItem.path)
      }
      errors.occurance = ConfigurationService.validateOccurance(localItem.occurance.name)
    }

    //If any key is set, then we have errors.
    for (const [_, val] of Object.entries(errors)) {
      if (!!val) {
        errors.hasErrors = true
      }
    }

    return errors
  },
  convertLocalToCronTab: (occurance: { name: string }, time: string) => {
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
  },
  convertCronTabToLocal: (cronFormatted: string) => {
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
  },
  convertItemToURL: (localItem: LocalConfiguration) => {
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

    let fullURL = `${protocol}://${host}`
    let queryString = !fullURL.includes('?') && localItem.advancedOptions.length > 0 ? '?' : ''
    localItem.advancedOptions.forEach((option, index) => {
      queryString += `${index > 0 ? '&' : ''}${option.key.name}=${option.value}`
    })
    return fullURL + queryString
  },
  convertLocalToServer: (localItem: LocalConfiguration, stripIndex = false) => {
    const occurance = localItem.occurance
    const time = localItem.time
    const schedule = ConfigurationService.convertLocalToCronTab(occurance, time)
    let rescanVal = RescanVals.True
    if (localItem.rescanBehavior === 0) {
      rescanVal = RescanVals.False
    } else if (localItem.rescanBehavior === 2) {
      rescanVal = RescanVals.DBOnly
    }
    const finalURL = ConfigurationService.convertItemToURL(localItem)

    let fullRet = {
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
  },
  createBlankLocal: () => {
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
      errors: {
        hasErrors: false,
        host: '',
        name: '',
        username: '',
        password: '',
        path: '',
        type: '',
        zone: '',
        foreignSource: ''
      }
    }
  },

  stripOriginalIndexes: (dataToUpdate: Array<ProvisionDServerConfiguration>) => {
    return dataToUpdate.map((item) => {
      let { originalIndex, currentSort, ...others } = item
      return others
    })
  },
  convertURLToLocal: (urlIn: string) => {
    let host = ''
    let path = ''
    let username = ''
    let password = ''
    let zone = ''
    let foreignSource = ''
    let subType = { id: 0, name: '', value: '' }

    const url = urlIn.split('/')
    let typeRaw = url[0].split(':')[0]
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

    if (type.name === RequisitionTypes.File) {
      let pathPart = urlIn.split(SplitTypes.file)[1]
      if (pathPart.includes('?')) {
        path = pathPart.split('?')[0]
      } else {
        path = pathPart
      }
    } else if (type.name === RequisitionTypes.VMWare) {
      if (url[2].includes('?')) {
        const vals = url[2].split('?')
        host = vals[0]
      } else {
        host = url[2]
      }
    } else if (type.name === RequisitionTypes.RequisitionPlugin) {
      typeRaw = url[2].split('?')[0]
      const foundSubType = requisitionSubTypes.find((item) => item.value.toLowerCase() === typeRaw)
      if (foundSubType) {
        subType = foundSubType
      }
    } else if (type.name === RequisitionTypes.DNS) {
      let urlPart = urlIn.split(SplitTypes.dns)[1].split('/')
      host = urlPart[0]
      zone = urlPart[1]
      if (urlPart[2]) {
        if (urlPart[2].includes('?')) {
          foreignSource = urlPart[2].split('?')[0]
        } else {
          foreignSource = urlPart[2]
        }
      }
    } else if (type.name === RequisitionTypes.HTTP || type.name === RequisitionTypes.HTTPS) {
      host = url[2]
    }

    const advancedOptions = ConfigurationService.gatherAdvancedOptions(urlIn).filter((item) => {
      if (type?.name === RequisitionTypes.VMWare) {
        if (item.key.name === VMWareFields.Username) {
          username = item.value
          return false
        }
        if (item.key.name === VMWareFields.Password) {
          password = item.value
          return false
        }
      }
      return true
    })
    return { path, type, host, username, password, subType, zone, foreignSource, advancedOptions }
  },
  gatherAdvancedOptions: (fullURL: string) => {
    return fullURL.includes('?')
      ? fullURL
          .split('?')[1]
          .split('&')
          .map((fullString) => {
            const [key, value] = fullString.split('=')
            return { key: { _text: key, name: key }, value }
          })
      : []
  },
  convertServerConfigurationToLocal: (clickedItem: ProvisionDServerConfiguration) => {
    let rescanBehavior = 1
    if (clickedItem?.[RequisitionData.RescanExisting] === RescanVals.False) {
      rescanBehavior = 0
    } else if (clickedItem?.[RequisitionData.RescanExisting] === RescanVals.DBOnly) {
      rescanBehavior = 2
    }
    const { occurance, twentyFourHour: time } = ConfigurationService.convertCronTabToLocal(
      clickedItem[RequisitionData.CronSchedule]
    )

    const urlVars = ConfigurationService.convertURLToLocal(clickedItem[RequisitionData.ImportURL])

    return {
      name: clickedItem[RequisitionData.ImportName],
      occurance: { name: occurance, id: 0 },
      time,
      rescanBehavior,
      ...urlVars
    }
  }
}
