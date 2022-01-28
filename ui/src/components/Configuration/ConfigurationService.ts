import { requisitionTypes, requisitionSubTypes } from './copy/requisitionTypes'

export const ConfigurationService = {
  validateLocalItem: (localItem: LocalConfiguration) => {
    const errors = { host: '', hasErrors: false, name: '' }
    const ipv4 = new RegExp(
      /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])\.)+([A-Za-z]|[A-Za-z][A-Za-z0-9-]*[A-Za-z0-9])$/
    )
    const isHostValid = !localItem?.subType?.value ? ipv4.test(localItem.host) : true
    if (!isHostValid) {
      errors.host = 'Invalid hostname'
    }

    if (!localItem.name) {
      errors.name = 'Must have a name'
    }
    if (localItem.name.length < 2) {
      errors.name = 'Name must have at least two chars'
    }
    if (localItem.name.length > 255) {
      errors.name = 'Name must be shorter than 255'
    }

    if (errors.host || errors.name) {
      errors.hasErrors = true
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
  convertLocalToServer: (localItem: LocalConfiguration) => {
    const protocol = localItem.type.name.toLowerCase()
    const host = localItem?.subType?.value ? localItem?.subType?.value : `${localItem?.host}/`
    let queryString = localItem?.advancedOptions?.length > 0 ? '?' : ''
    localItem.advancedOptions.forEach((option, index) => {
      queryString += `${index > 0 ? '&' : ''}${option.key.name}=${option.value}`
    })
    const occurance = localItem.occurance
    const time = localItem.time
    const schedule = ConfigurationService.convertLocalToCronTab(occurance, time)
    let rescanVal = 'true'
    if (localItem.rescanBehavior === 0) {
      rescanVal = 'false'
    } else if (localItem.rescanBehavior === 2) {
      rescanVal = 'dbonly'
    }
    const finalURL = `${protocol}://${host}${queryString}`
    return {
      'import-name': localItem.name,
      'import-url-resource': finalURL,
      'cron-schedule': schedule,
      'rescan-existing': rescanVal,
      currentSort: { property: '', value: '' },
      originalIndex: 0
    }
  },
  convertServerConfigurationToLocal: (clickedItem: ProvisionDServerConfiguration) => {
    const advancedOptions = clickedItem['import-url-resource'].includes('?')
      ? clickedItem['import-url-resource']
          .split('?')[1]
          .split('&')
          .map((fullString) => {
            const [key, value] = fullString.split('=')
            return { key: { _text: key, name: key }, value }
          })
      : []
    let rescanBehavior = 1
    if (clickedItem?.['rescan-existing'] === 'false') {
      rescanBehavior = 0
    } else if (clickedItem?.['rescan-existing'] === 'dbonly') {
      rescanBehavior = 2
    }
    const { occurance, twentyFourHour: time } = ConfigurationService.convertCronTabToLocal(clickedItem['cron-schedule'])

    const url = clickedItem['import-url-resource'].split('/')
    let typeRaw = url[0].split(':')[0]
    let type = requisitionTypes.find((item) => item.name.toLowerCase() === typeRaw)
    if (!type) {
      type = { id: 0, name: '' }
    }
    let host = ''
    let subType = { id: 0, name: '', value: '' }
    if (url.length === 3) {
      typeRaw = url[2].split('?')[0]
      const foundSubType = requisitionSubTypes.find((item) => item.value.toLowerCase() === typeRaw)
      if (foundSubType) {
        subType = foundSubType
      }
    } else if (url.length === 4) {
      host = url[2]
    }

    return {
      name: clickedItem['import-name'],
      type,
      host,
      subType,
      occurance: { name: occurance, id: 0 },
      time,
      advancedOptions,
      rescanBehavior
    }
  }
}
