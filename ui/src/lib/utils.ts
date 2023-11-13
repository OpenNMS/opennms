export const isNumber = (value: any) => {
  return value !== null && value !== undefined && typeof(value) === 'number'
}

export const isConvertibleToInteger = (value: any) => {
  if (value === null || value === undefined || value === '') {
    return false
  }

  const num = Number(value)

  return !Number.isNaN(num) && Number.isInteger(num)
}

/**
 * Returns true if value is non-null and is a primitive string or a String object
 */
export const isString = (value: any) => {
  return value !== null && (typeof(value) === 'string' || value instanceof String)
}

export const ellipsify = (text: string, count: number) => {
  if (text && count && text.length > count) {
    return text.substring(0, count) + '...'
  }

  return text
}

/**
 * Returns whether the object has at least one valid (non-empty) string property.
 */
export const hasNonEmptyProperty = (obj?: any) => {
  if (!obj) {
    return false
  }

  const keys = Object.getOwnPropertyNames(obj)

  return keys.some(k => {
    const value = (obj as any)[k]
    return value && isString(value) && value.length > 0
  })
}
