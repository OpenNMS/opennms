export const isNumber = (value: any) => {
  return value !== null && value !== undefined && typeof(value) === 'number'
}

export const isConvertibleToInteger = (value: any) => {
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
