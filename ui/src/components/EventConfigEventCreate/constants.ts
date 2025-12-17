import { ISelectItemType } from '@featherds/select'

export const MAX_MASK_ELEMENTS = 12

export const statusOptions: ISelectItemType[] = [
  { _text: 'Enable', _value: 'enable' },
  { _text: 'Disable', _value: 'disable' }
]

export enum Severity {
  Critical = 'Critical',
  Major = 'Major',
  Minor = 'Minor',
  Warning = 'Warning',
  Normal = 'Normal',
  Indeterminate = 'Indeterminate',
  Cleared = 'Cleared'
}

export const SeverityOptions: ISelectItemType[] = [
  { _text: Severity.Critical, _value: Severity.Critical },
  { _text: Severity.Major, _value: Severity.Major },
  { _text: Severity.Minor, _value: Severity.Minor },
  { _text: Severity.Warning, _value: Severity.Warning },
  { _text: Severity.Normal, _value: Severity.Normal },
  { _text: Severity.Indeterminate, _value: Severity.Indeterminate },
  { _text: Severity.Cleared, _value: Severity.Cleared }
]

export enum Destination {
  LogAndDisplay = 'logndisplay',
  LogOnly = 'logonly',
  Suppress = 'suppress',
  DoNotPersist = 'donotpersist',
  DiscardTraps = 'discardtraps'
}

export const DestinationOptions: ISelectItemType[] = [
  { _text: Destination.LogAndDisplay, _value: Destination.LogAndDisplay },
  { _text: Destination.LogOnly, _value: Destination.LogOnly },
  { _text: Destination.Suppress, _value: Destination.Suppress },
  { _text: Destination.DoNotPersist, _value: Destination.DoNotPersist },
  { _text: Destination.DiscardTraps, _value: Destination.DiscardTraps }
]

export enum MaskElementName {
  Uei = 'uei',
  Source = 'source',
  NodeId = 'nodeid',
  Host = 'host',
  Interface = 'interface',
  SnmpHost = 'snmphost',
  Service = 'service',
  Id = 'id',
  Specific = 'specific',
  Generic = 'generic',
  Community = 'community',
  Trapoid = 'trapoid'
}

export const MaskElementNameOptions: ISelectItemType[] = [
  { _text: MaskElementName.Uei, _value: MaskElementName.Uei },
  { _text: MaskElementName.Source, _value: MaskElementName.Source },
  { _text: MaskElementName.NodeId, _value: MaskElementName.NodeId },
  { _text: MaskElementName.Host, _value: MaskElementName.Host },
  { _text: MaskElementName.Interface, _value: MaskElementName.Interface },
  { _text: MaskElementName.SnmpHost, _value: MaskElementName.SnmpHost },
  { _text: MaskElementName.Service, _value: MaskElementName.Service },
  { _text: MaskElementName.Id, _value: MaskElementName.Id },
  { _text: MaskElementName.Specific, _value: MaskElementName.Specific },
  { _text: MaskElementName.Generic, _value: MaskElementName.Generic },
  { _text: MaskElementName.Community, _value: MaskElementName.Community },
  { _text: MaskElementName.Trapoid, _value: MaskElementName.Trapoid }
]

export enum MaskVarbindsTypeText {
  vbNumber = 'Varbind Number',
  vbOid = 'Varbind OID'
}

export enum MaskVarbindsTypeValue {
  vbNumber = 'vbnumber',
  vbOid = 'vboid'
}

export const MaskVarbindsTypeOptions: ISelectItemType[] = [
  { _text: MaskVarbindsTypeText.vbNumber, _value: MaskVarbindsTypeValue.vbNumber },
  { _text: MaskVarbindsTypeText.vbOid, _value: MaskVarbindsTypeValue.vbOid }
]

export enum AlarmType {
  One = '1',
  Two = '2',
  Three = '3'
}

export enum AlarmTypeValue {
  One = '1',
  Two = '2',
  Three = '3'
}

export enum AlarmTypeName {
  One = 'Raise',
  Two = 'Resolution',
  Three = 'Unresolvable'
}

export const AlarmTypeOptions: ISelectItemType[] = [
  { _text: AlarmTypeName.One, _value: AlarmTypeValue.One },
  { _text: AlarmTypeName.Two, _value: AlarmTypeValue.Two },
  { _text: AlarmTypeName.Three, _value: AlarmTypeValue.Three }
]

