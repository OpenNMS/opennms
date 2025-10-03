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

export const severityOptions: ISelectItemType[] = [
  { _text: Severity.Critical, _value: Severity.Critical },
  { _text: Severity.Major, _value: Severity.Major },
  { _text: Severity.Minor, _value: Severity.Minor },
  { _text: Severity.Warning, _value: Severity.Warning },
  { _text: Severity.Normal, _value: Severity.Normal },
  { _text: Severity.Indeterminate, _value: Severity.Indeterminate },
  { _text: Severity.Cleared, _value: Severity.Cleared }
]
