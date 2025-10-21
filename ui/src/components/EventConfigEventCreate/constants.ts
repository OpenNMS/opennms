import { ISelectItemType } from '@featherds/select'

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
  LogNDisply = 'logndisplay',
  LogOnly = 'logonly',
  Suppress = 'suppress',
  DoNotPersist = 'donotpersist',
  DiscardTraps = 'discardtraps'
}

export const DestinationOptions: ISelectItemType[] = [
  { _text: Destination.LogNDisply, _value: Destination.LogNDisply },
  { _text: Destination.LogOnly, _value: Destination.LogOnly },
  { _text: Destination.Suppress, _value: Destination.Suppress },
  { _text: Destination.DoNotPersist, _value: Destination.DoNotPersist },
  { _text: Destination.DiscardTraps, _value: Destination.DiscardTraps }
]