///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { ISelectItemType } from '@/types'
import type {
  BaseThresholdDef,
  Expression,
  ResourceFilter,
  ResourceFilterErrors,
  Threshold,
  ThreshdPackage,
  ThreshdPackageErrors,
  ThreshdService,
  ThreshdServiceErrors,
  ThresholdDefinition,
  ThresholdDefinitionErrors,
  ThresholdGroup,
  ThresholdGroupErrors,
  Thresholder,
  ThresholderErrors
} from '@/types/thresholdConfig'

/**
 * RRDtool cannot store a datasource name longer than this. The XSD does not encode the limit, so it is
 * enforced here and again by the REST service.
 */
export const MAX_DS_NAME_LENGTH = 19

/**
 * Mirrors the value/rearm pattern in thresholding.xsd: a number, or a metadata reference. Anchored, because
 * an unanchored copy would accept a value like '90abc' that the server then rejects.
 */
export const THRESHOLD_NUMERIC_PATTERN = /^(?:[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?|\$\{(?:.+:.+)\})$/

/** Mirrors the trigger pattern in thresholding.xsd: a positive integer, or a metadata reference. */
export const THRESHOLD_TRIGGER_PATTERN = /^(?:[0-9]*[1-9][0-9]*|\$\{(?:.+:.+)\})$/

export enum ThresholdDefinitionKind {
  Threshold = 'threshold',
  Expression = 'expression'
}

export enum ThresholdType {
  High = 'high',
  Low = 'low',
  RelativeChange = 'relativeChange',
  AbsoluteChange = 'absoluteChange',
  RearmingAbsoluteChange = 'rearmingAbsoluteChange'
}

export enum FilterOperator {
  And = 'and',
  Or = 'or'
}

export enum ThreshdServiceStatus {
  On = 'on',
  Off = 'off'
}

export const THRESHOLD_TYPE_OPTIONS: ISelectItemType[] = [
  { _text: 'High', _value: ThresholdType.High },
  { _text: 'Low', _value: ThresholdType.Low },
  { _text: 'Relative change', _value: ThresholdType.RelativeChange },
  { _text: 'Absolute change', _value: ThresholdType.AbsoluteChange },
  { _text: 'Rearming absolute change', _value: ThresholdType.RearmingAbsoluteChange }
]

export const FILTER_OPERATOR_OPTIONS: ISelectItemType[] = [
  { _text: 'Any filter matches (or)', _value: FilterOperator.Or },
  { _text: 'All filters match (and)', _value: FilterOperator.And }
]

export const SERVICE_STATUS_OPTIONS: ISelectItemType[] = [
  { _text: 'On', _value: ThreshdServiceStatus.On },
  { _text: 'Off', _value: ThreshdServiceStatus.Off }
]

/** Service parameter that binds a threshd package's service to a threshold group. */
export const THRESHOLDING_GROUP_PARAMETER = 'thresholding-group'

const isBlank = (value?: string): boolean => !value || value.trim().length === 0

const isThreshold = (definition: ThresholdDefinition): definition is Threshold =>
  Object.prototype.hasOwnProperty.call(definition, 'dsName')

/**
 * True when at least one field of the error bag is set. Takes a plain object rather than an indexed record
 * so the specific per-form error interfaces can be passed without widening them.
 */
export const hasErrors = (errors: object): boolean => Object.values(errors).some(message => !!message)

export const validateResourceFilter = (filter: ResourceFilter): ResourceFilterErrors => {
  const errors: ResourceFilterErrors = {}

  if (isBlank(filter.field)) {
    errors.field = 'Field name is required.'
  }

  if (filter.content) {
    try {
      new RegExp(filter.content)
    } catch {
      // Reported, not blocked: the server evaluates these with Java's regex engine, which accepts
      // constructs JavaScript does not.
      errors.content = 'This may not be a valid regular expression.'
    }
  }

  return errors
}

export const validateThresholdDefinition = (
  definition: ThresholdDefinition,
  kind: ThresholdDefinitionKind
): ThresholdDefinitionErrors => {
  const errors: ThresholdDefinitionErrors = {}
  const base = definition as BaseThresholdDef

  if (isBlank(base.type)) {
    errors.type = 'Type is required.'
  } else if (!Object.values(ThresholdType).includes(base.type as ThresholdType)) {
    errors.type = `'${base.type}' is not a known threshold type.`
  }

  if (isBlank(base.dsType)) {
    errors.dsType = 'Datasource type is required.'
  }

  if (kind === ThresholdDefinitionKind.Threshold) {
    const dsName = isThreshold(definition) ? definition.dsName : ''

    if (isBlank(dsName)) {
      errors.dsName = 'Datasource is required.'
    } else if (dsName.trim().length > MAX_DS_NAME_LENGTH) {
      errors.dsName = `RRDtool limits datasource names to ${MAX_DS_NAME_LENGTH} characters.`
    }
  } else {
    const expression = (definition as Expression).expression

    if (isBlank(expression)) {
      errors.expression = 'Expression is required.'
    }
  }

  if (isBlank(base.value)) {
    errors.value = 'Value is required.'
  } else if (!THRESHOLD_NUMERIC_PATTERN.test(base.value.trim())) {
    errors.value = 'Enter a number or a metadata reference such as ${scv:key}.'
  }

  // rearm and trigger stay required even for the change thresholds, which ignore them at runtime: the
  // schema declares them mandatory, so leaving them out produces a configuration the server rejects.
  if (isBlank(base.rearm)) {
    errors.rearm = 'Re-arm is required.'
  } else if (!THRESHOLD_NUMERIC_PATTERN.test(base.rearm.trim())) {
    errors.rearm = 'Enter a number or a metadata reference such as ${scv:key}.'
  }

  if (isBlank(base.trigger)) {
    errors.trigger = 'Trigger is required.'
  } else if (!THRESHOLD_TRIGGER_PATTERN.test(base.trigger.trim())) {
    errors.trigger = 'Enter a positive whole number or a metadata reference.'
  }

  if (base.filterOperator && !Object.values(FilterOperator).includes(base.filterOperator as FilterOperator)) {
    errors.filterOperator = `'${base.filterOperator}' is not a known filter operator.`
  }

  return errors
}

export const validateThresholdGroup = (
  group: ThresholdGroup,
  existingNames: string[],
  isCreate: boolean
): ThresholdGroupErrors => {
  const errors: ThresholdGroupErrors = {}
  const name = group.name?.trim() ?? ''

  if (isBlank(name)) {
    errors.name = 'Name is required.'
  } else if (name !== group.name) {
    errors.name = 'Name must not start or end with whitespace.'
  } else if (isCreate && existingNames.includes(name)) {
    errors.name = `A group named '${name}' already exists.`
  }

  if (isBlank(group.rrdRepository)) {
    errors.rrdRepository = 'RRD repository is required.'
  }

  return errors
}

export const validateThreshdPackage = (
  pkg: ThreshdPackage,
  existingNames: string[],
  isCreate: boolean
): ThreshdPackageErrors => {
  const errors: ThreshdPackageErrors = {}
  const name = pkg.name?.trim() ?? ''

  if (isBlank(name)) {
    errors.name = 'Name is required.'
  } else if (isCreate && existingNames.includes(name)) {
    errors.name = `A package named '${name}' already exists.`
  }

  if (isBlank(pkg.filter)) {
    errors.filter = 'Filter is required.'
  }

  return errors
}

export const validateThreshdService = (service: ThreshdService, groupNames: string[]): ThreshdServiceErrors => {
  const errors: ThreshdServiceErrors = {}

  if (isBlank(service.name)) {
    errors.name = 'Service name is required.'
  }

  if (service.interval === null || service.interval === undefined || Number.isNaN(service.interval)) {
    errors.interval = 'Interval is required.'
  } else if (!Number.isInteger(service.interval) || service.interval < 1) {
    errors.interval = 'Interval must be a whole number of milliseconds greater than 0.'
  }

  const thresholdingGroup = service.parameters?.find(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER)

  if (thresholdingGroup && !isBlank(thresholdingGroup.value) && !groupNames.includes(thresholdingGroup.value)) {
    errors.thresholdingGroup = `No threshold group named '${thresholdingGroup.value}' exists.`
  }

  return errors
}

export const validateThresholder = (thresholder: Thresholder): ThresholderErrors => {
  const errors: ThresholderErrors = {}

  if (isBlank(thresholder.service)) {
    errors.service = 'Service is required.'
  }

  if (isBlank(thresholder.className)) {
    errors.className = 'Class name is required.'
  }

  return errors
}

export const validateAddressRange = (begin?: string, end?: string): string | undefined => {
  const hasBegin = !isBlank(begin)
  const hasEnd = !isBlank(end)

  if (hasBegin !== hasEnd) {
    return 'A range needs both a begin and an end address.'
  }

  return undefined
}
