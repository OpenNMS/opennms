import { ISelectItemType } from '@featherds/select'

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

export const KEY_PATTERN = /^[\w-]+$/

export const OID_PATTERN = /^\.?\d+(\.\d+)*$/

const IF_TYPE_ALL = 'all'
const IF_TYPE_IGNORE = 'ignore'

export const DEFAULT_IF_TYPE_FILTER: ISelectItemType = { _text: 'Ignore', _value: IF_TYPE_IGNORE }

export const IF_TYPE_FILTERS_OPTIONS: ISelectItemType[] = [
  { _text: 'Ignore', _value: IF_TYPE_IGNORE },
  { _text: 'All', _value: IF_TYPE_ALL }
]

export const VALID_MIB_OBJ_TYPES = [
  'counter',
  'counter32',
  'counter64',
  'gauge',
  'gauge32',
  'gauge64',
  'integer',
  'integer32',
  'timeticks',
  'string',
  'octetstring',
  'opaque'
]

export const DEFAULT_MIB_OBJ_TYPE: ISelectItemType = { _text: 'gauge', _value: 'gauge' }

export const MIB_OBJECT_DATA_TYPE_OPTIONS: ISelectItemType[] = [
  ...VALID_MIB_OBJ_TYPES.map((type) => ({ _text: type, _value: type }))
]

export const PERSISTENCE_SELECTOR_STRATEGY_OPTIONS: ISelectItemType[] = [
  {
    _text: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
    _value: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy'
  },
  {
    _text: 'org.opennms.netmgt.collectd.PersistRegexSelectorStrategy',
    _value: 'org.opennms.netmgt.collectd.PersistRegexSelectorStrategy'
  }
]

export const STORAGE_STRATEGY_OPTIONS: ISelectItemType[] = [
  {
    _text: 'org.opennms.netmgt.collection.support.IndexStorageStrategy',
    _value: 'org.opennms.netmgt.collection.support.IndexStorageStrategy'
  },
  {
    _text: 'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy',
    _value: 'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy'
  }
]

