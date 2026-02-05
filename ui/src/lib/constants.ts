const OID_TYPE_SINGLE = 'single'
const OID_TYPE_MASK = 'mask'

export const OID_TYPE_OPTIONS = [
  { name: 'Single', value: OID_TYPE_SINGLE },
  { name: 'Mask', value: OID_TYPE_MASK }
]

export const DEFAULT_OID_TYPE = OID_TYPE_SINGLE

export const STATUS_OPTIONS = [
  { name: 'Enabled', value: true },
  { name: 'Disabled', value: false }
]

export const DEFAULT_STATUS = true

export const OID_PATTERN = /^\.?\d+(\.\d+)*$/

const IF_TYPE_ALL = 'all'
const IF_TYPE_IGNORE = 'ignore'

export const IF_TYPE_FILTERS_OPTIONS = [
  { name: 'Ignore', value: IF_TYPE_IGNORE },
  { name: 'All', value: IF_TYPE_ALL }
]

