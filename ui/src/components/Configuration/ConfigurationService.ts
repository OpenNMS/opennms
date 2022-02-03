import { requisitionTypes, requisitionSubTypes } from './copy/requisitionTypes'

export const ConfigurationService = {
  validateName: (name: string, nameType: string = 'Name') => {
    let nameError = ''
    if (!name) {
      nameError = `Must have a ${nameType.toLocaleLowerCase()}`
    }
    if (!nameError && name.length < 2) {
      nameError = `${nameType} must have at least two chars`
    }
    if (!nameError && name.length > 255) {
      nameError = `${nameType} must be shorter than 255`
    }
    return nameError
  },
  validateType: (typeName: string) => {
    let typeError = ''
    if (!typeName) {
      typeError = 'Must select a type'
    }
    return typeError
  },
  validateOccurance: (typeName: string) => {
    let typeError = ''
    if (!typeName) {
      typeError = 'Must select a schedule time'
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
      hostError = 'Invalid hostname'
    }
    return hostError
  },

  validatePath: (path: string) => {
    let pathError = ''
    if (!path) {
      pathError = 'Must include a file path'
    } else if (!path.startsWith('/')) {
      pathError = 'Path must start with a /'
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

      if (['DNS', 'VMWare', 'HTTP', 'HTTPS'].includes(localItem.type.name)) {
        errors.host = ConfigurationService.validateHost(localItem.host)
      }
      if (localItem.type.name === 'DNS') {
        errors.zone = ConfigurationService.validateHost(localItem.zone)

        // Only validate foreign source if it's set.
        if (!!localItem.foreignSource) {
          errors.foreignSource = ConfigurationService.validateHost(localItem.foreignSource)
        }
      }
      if (localItem.type.name === 'File') {
        errors.path = ConfigurationService.validatePath(localItem.path)
      }
      if (localItem.type.name === 'VMWare') {
        errors.username = ConfigurationService.validateName(localItem.username, 'Username')
        errors.password = ConfigurationService.validateName(localItem.password, 'Password')
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
    const protocol = localItem.type.name.toLowerCase()
    const type = localItem.type.name
    let host = localItem.host
    if (type === 'Requisition') {
      host = localItem.subType.value
    } else if (type === 'DNS') {
      host = `${localItem.host}/${localItem.zone || ''}`
      if (localItem.foreignSource) {
        host += `/${localItem.foreignSource}`
      }
    } else if (type === 'VMWare') {
      host = `${localItem.host}?username=${localItem.username}&password=${localItem.password}`
    } else if (type === 'File') {
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
    let rescanVal = 'true'
    if (localItem.rescanBehavior === 0) {
      rescanVal = 'false'
    } else if (localItem.rescanBehavior === 2) {
      rescanVal = 'dbonly'
    }
    const finalURL = ConfigurationService.convertItemToURL(localItem)
    let fullRet: ProvisionDServerConfiguration = {
      'import-name': localItem.name,
      'import-url-resource': finalURL,
      'cron-schedule': schedule,
      'rescan-existing': rescanVal,
      currentSort: { property: '', value: '' },
      originalIndex: 0
    }

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
    let type = requisitionTypes.find((item) => item.name.toLowerCase() === typeRaw)
    if (!type) {
      type = { id: 0, name: '' }
    }

    if (type.name === 'File') {
      let pathPart = urlIn.split('file://')[1]
      if (pathPart.includes('?')) {
        path = pathPart.split('?')[0]
      } else {
        path = pathPart
      }
    } else if (type.name === 'VMWare') {
      if (url[2].includes('?')) {
        const vals = url[2].split('?')
        host = vals[0]
      } else {
        host = url[2]
      }
    } else if (type.name === 'Requisition') {
      typeRaw = url[2].split('?')[0]
      const foundSubType = requisitionSubTypes.find((item) => item.value.toLowerCase() === typeRaw)
      if (foundSubType) {
        subType = foundSubType
      }
    } else if (type.name === 'DNS') {
      let urlPart = urlIn.split('dns://')[1].split('/')
      host = urlPart[0]
      zone = urlPart[1]
      if (urlPart[2]) {
        if (urlPart[2].includes('?')) {
          foreignSource = urlPart[2].split('?')[0]
        } else {
          foreignSource = urlPart[2]
        }
      }
    } else if (type.name === 'HTTP' || type.name === 'HTTPS') {
      host = url[2]
    }

    const advancedOptions = ConfigurationService.gatherAdvancedOptions(urlIn).filter((item) => {
      if (type?.name === 'VMWare') {
        if (item.key.name === 'username') {
          username = item.value
          return false
        }
        if (item.key.name === 'password') {
          password = item.value
          return false
        }
        return true
      }
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
    if (clickedItem?.['rescan-existing'] === 'false') {
      rescanBehavior = 0
    } else if (clickedItem?.['rescan-existing'] === 'dbonly') {
      rescanBehavior = 2
    }
    const { occurance, twentyFourHour: time } = ConfigurationService.convertCronTabToLocal(clickedItem['cron-schedule'])

    const urlVars = ConfigurationService.convertURLToLocal(clickedItem['import-url-resource'])

    return {
      name: clickedItem['import-name'],
      occurance: { name: occurance, id: 0 },
      time,
      rescanBehavior,
      ...urlVars
    }
  }
}
