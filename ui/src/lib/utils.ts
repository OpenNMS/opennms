export const isNumber = (value: any) => {
  return typeof(value) === 'number'
}

/**
 * Returns true if value is non-null and is a primitive string or a String object
 */
export const isString = (value: any) => {
  return value !== null && (typeof(value) === 'string' || value instanceof String)
}
