export const firstCharValidator = {
  $validator: (value: any) => {
    if (value !== '') {
      const firstCharRegex = new RegExp('^[A-Za-z]')
      const validFirstChar = firstCharRegex.test(value.charAt(0))
      if (validFirstChar) return validFirstChar
    } else {
      return true
    }
  },
  $message: 'Must begin with an alphabet A-Z a-z'
}

export const nameValidator = {
  $validator: (value: any) => {
    if (value.substring(1)) {
      const patternNameRegex = new RegExp('^[A-Za-z0-9-_ ]+$')
      const validName = patternNameRegex.test(value.substring(1))
      if (validName) return validName
    } else {
      return true
    }
  },
  $message: 'Allowed characters are A-Z a-z 0-9 - _ space'
}

export const hostAndIPValidator = {
  $validator: (value: any) => {
    if (value !== '') {
      let validHost: boolean = false
      const ipRegex = new RegExp(
        '^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$'
      )
      const hostRegex = new RegExp('^(?:[-A-Za-z0-9]+.)+[A-Za-z]{2,6}$')
      validHost = hostRegex.test(value) || ipRegex.test(value)
      if (validHost) return validHost
    } else {
      return true
    }
  },
  $message: 'Please enter valid host'
}
