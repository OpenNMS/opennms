import { VALID_MIB_OBJ_TYPES } from '@/lib/constants'
import { UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { XMLValidator } from 'fast-xml-parser'

export const MAX_FILES_UPLOAD = 100

// Valid ifType values for groups
const VALID_IF_TYPES = ['all', 'ignore']

const DATACOLLECTION_NAMESPACE = 'http://xmlns.opennms.org/xsd/config/datacollection'

export interface ValidateSnmpDataCollectionSourceFileResult {
  isValid: boolean
  errors: string[]
  kind?: 'group' | 'config'
  groupName?: string
  profileNames?: string[]
}

export const validateSnmpDataCollectionSourceFile = async (
  file: File
): Promise<ValidateSnmpDataCollectionSourceFileResult> => {
  const validationErrors: string[] = []
  let parsedGroupName: string | undefined
  let parsedProfileNames: string[] | undefined
  let kind: 'group' | 'config' | undefined

  try {
    const text = await file.text()

    if (text.trim().length === 0) {
      validationErrors.push('File is empty')
      return { isValid: false, errors: validationErrors, kind }
    }

    if (!file.name.endsWith('.xml')) {
      validationErrors.push('File must have .xml extension')
      return { isValid: false, errors: validationErrors, kind }
    }

    let parser: any
    try {
      parser = new (DOMParser as any)()
    } catch (e) {
      parser = (DOMParser as any)()
    }
    const xmlDoc = parser.parseFromString(text, 'application/xml')
    const result = XMLValidator.validate(text)
    if (xmlDoc.querySelector('parsererror')) {
      validationErrors.push('Invalid XML format - file contains syntax errors')
      return { isValid: false, errors: validationErrors, kind }
    }
    if (result !== true) {
      validationErrors.push('Invalid XML format - file contains syntax errors')
      return { isValid: false, errors: validationErrors, kind }
    }

    // Server's /upload accepts two root elements:
    //   <datacollection-group>   → a single source definition
    //   <datacollection-config>  → a profiles file driving include-collection
    const groupRoot = xmlDoc.querySelector('datacollection-group')
    const configRoot = xmlDoc.querySelector('datacollection-config')

    if (configRoot) {
      kind = 'config'
      const xmlns = configRoot.getAttribute('xmlns') || ''
      if (xmlns && xmlns !== DATACOLLECTION_NAMESPACE) {
        validationErrors.push('Invalid OpenNMS namespace in <datacollection-config> element')
        return { isValid: false, errors: validationErrors, kind }
      }
      const snmpCollections = configRoot.querySelectorAll('snmp-collection')
      if (snmpCollections.length === 0) {
        validationErrors.push('<datacollection-config> must contain at least one <snmp-collection>')
        return { isValid: false, errors: validationErrors, kind }
      }
      const names: string[] = []
      for (const [idx, sc] of (Array.from(snmpCollections as any[]) as Element[]).entries()) {
        const name = sc.getAttribute('name')
        if (!name || name.trim().length === 0) {
          validationErrors.push(`<snmp-collection> ${idx + 1}: missing "name" attribute`)
          return { isValid: false, errors: validationErrors, kind }
        }
        names.push(name.trim())
      }
      parsedProfileNames = names
    } else if (groupRoot) {
      kind = 'group'
      const xmlns = groupRoot.getAttribute('xmlns') || ''
      if (xmlns && xmlns !== DATACOLLECTION_NAMESPACE) {
        validationErrors.push('Invalid OpenNMS namespace in <datacollection-group> element')
        return { isValid: false, errors: validationErrors, kind }
      }

      const groupName = groupRoot.getAttribute('name')
      if (!groupName || groupName.trim().length === 0) {
        validationErrors.push('<datacollection-group> element must have a non-empty "name" attribute')
        return { isValid: false, errors: validationErrors, kind }
      }
      parsedGroupName = groupName.trim()

      const resourceTypes = groupRoot.querySelectorAll('resourceType')
      const groups = groupRoot.querySelectorAll('group')
      const systemDefs = groupRoot.querySelectorAll('systemDef')
      const childElements = groupRoot.children

      if (childElements.length && resourceTypes.length === 0 && groups.length === 0 && systemDefs.length === 0) {
        const childNames = Array.from(childElements as any[])
          .map((el: any) => `<${String(el.tagName).toLowerCase()}>`)
          .join(', ')
        validationErrors.push(
          `<datacollection-group> element contains ${childNames} but no <resourceType>, <group>, or <systemDef> elements`
        )
        return { isValid: false, errors: validationErrors, kind }
      } else if (resourceTypes.length === 0 && groups.length === 0 && systemDefs.length === 0) {
        validationErrors.push(
          'No <resourceType>, <group>, or <systemDef> entries found within <datacollection-group> element'
        )
        return { isValid: false, errors: validationErrors, kind }
      } else {
        try {
          // Validate resourceType elements
          const resourceTypeList = Array.from(resourceTypes as any[]) as Element[]
          for (const [idx, resourceType] of resourceTypeList.entries()) {
            const resourceTypeError = validateResourceTypeElement(resourceType as any, idx + 1)
            if (resourceTypeError) {
              validationErrors.push(resourceTypeError)
              return { isValid: false, errors: validationErrors, kind }
            }
          }

          // Validate group elements
          const groupList = Array.from(groups as any[]) as Element[]
          for (const [idx, group] of groupList.entries()) {
            const groupError = validateGroupElement(group as any, idx + 1)
            if (groupError) {
              validationErrors.push(groupError)
              return { isValid: false, errors: validationErrors, kind }
            }
          }

          // Validate systemDef elements
          const systemDefList = Array.from(systemDefs as any[]) as Element[]
          for (const [idx, systemDef] of systemDefList.entries()) {
            const systemDefError = validateSystemDefElement(systemDef as any, idx + 1)
            if (systemDefError) {
              validationErrors.push(systemDefError)
              return { isValid: false, errors: validationErrors, kind }
            }
          }
        } catch (error) {
          validationErrors.push(
            `Error reading file content: ${error instanceof Error ? error.message : 'Unknown error'}`
          )
          return { isValid: false, errors: validationErrors, kind }
        }
      }
    } else {
      validationErrors.push('Expected <datacollection-group> or <datacollection-config> as root element')
      return { isValid: false, errors: validationErrors, kind }
    }
  } catch (error) {
    validationErrors.push(`Error reading file content: ${error instanceof Error ? error.message : 'Unknown error'}`)
    return { isValid: false, errors: validationErrors, kind }
  }
  return {
    isValid: validationErrors.length === 0,
    errors: validationErrors,
    kind,
    groupName: parsedGroupName,
    profileNames: parsedProfileNames
  }
}

export const validateResourceTypeElement = (
  resourceType: Element | any,
  resourceTypeNumber: number
): string => {
  if (!resourceType || typeof resourceType.querySelector !== 'function') {
    return `ResourceType ${resourceTypeNumber}: invalid element`
  }

  const name = resourceType.getAttribute('name')
  if (!name || name.trim().length === 0) {
    return `ResourceType ${resourceTypeNumber}: missing "name" attribute`
  }

  const label = resourceType.getAttribute('label')
  if (!label || label.trim().length === 0) {
    return `ResourceType "${name}": missing "label" attribute`
  }

  const persistenceStrategy = resourceType.querySelector('persistenceSelectorStrategy')
  if (!persistenceStrategy) {
    return `ResourceType "${name}": missing <persistenceSelectorStrategy>`
  }

  const persistenceClass = persistenceStrategy.getAttribute('class')
  if (!persistenceClass || persistenceClass.trim().length === 0) {
    return `ResourceType "${name}": <persistenceSelectorStrategy> missing "class" attribute`
  }

  const storageStrategy = resourceType.querySelector('storageStrategy')
  if (!storageStrategy) {
    return `ResourceType "${name}": missing <storageStrategy>`
  }

  const storageClass = storageStrategy.getAttribute('class')
  if (!storageClass || storageClass.trim().length === 0) {
    return `ResourceType "${name}": <storageStrategy> missing "class" attribute`
  }

  // Validate parameter elements within strategies
  const persistenceParams = persistenceStrategy.querySelectorAll('parameter')
  const persistenceParamList = Array.from(persistenceParams as any[]) as Element[]
  for (const [idx, param] of persistenceParamList.entries()) {
    const paramError = validateParameterElement(
      param,
      `ResourceType "${name}" persistenceSelectorStrategy parameter ${idx + 1}`
    )
    if (paramError) return paramError
  }

  const storageParams = storageStrategy.querySelectorAll('parameter')
  const storageParamList = Array.from(storageParams as any[]) as Element[]
  for (const [idx, param] of storageParamList.entries()) {
    const paramError = validateParameterElement(param, `ResourceType "${name}" storageStrategy parameter ${idx + 1}`)
    if (paramError) return paramError
  }

  return ''
}

export const validateParameterElement = (param: Element | any, context: string): string => {
  if (!param) {
    return `${context}: invalid element`
  }

  const key = param.getAttribute('key')
  if (!key || key.trim().length === 0) {
    return `${context}: missing "key" attribute`
  }

  const value = param.getAttribute('value')
  if (value === null) {
    return `${context}: missing "value" attribute`
  }

  return ''
}

export const validateGroupElement = (group: Element, groupNumber: number): string => {
  if (!group || typeof group.querySelector !== 'function') {
    return `Group ${groupNumber}: invalid element`
  }

  const name = group.getAttribute('name')
  if (!name || name.trim().length === 0) {
    return `Group ${groupNumber}: missing "name" attribute`
  }

  const ifType = group.getAttribute('ifType')
  if (!ifType || ifType.trim().length === 0) {
    return `Group "${name}": missing "ifType" attribute`
  }

  const ifTypeLower = ifType.toLowerCase()
  const isNumericIfType = /^\d+$/.test(ifType)
  if (!VALID_IF_TYPES.includes(ifTypeLower) && !isNumericIfType) {
    return `Group "${name}": invalid "ifType" value "${ifType}". Expected "all", "ignore", or numeric`
  }

  const mibObjs = group.querySelectorAll('mibObj')
  if (mibObjs.length === 0) {
    return `Group "${name}": missing <mibObj> elements`
  }

  const mibObjList = Array.from(mibObjs) as Element[]
  for (const [idx, mibObj] of mibObjList.entries()) {
    const mibObjError = validateMibObjElement(mibObj as any, name, idx + 1)
    if (mibObjError) return mibObjError
  }

  return ''
}

export const validateMibObjElement = (
  mibObj: Element,
  groupName: string,
  mibObjNumber: number
): string => {
  if (!mibObj) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: invalid element`
  }

  const oid = mibObj.getAttribute('oid')
  if (!oid || oid.trim().length === 0) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: missing "oid"`
  }

  if (!/^\.?\d+(\.\d+)*$/.test(oid)) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: invalid OID format "${oid}"`
  }

  const instance = mibObj.getAttribute('instance')
  if (!instance || instance.trim().length === 0) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: missing "instance"`
  }

  const alias = mibObj.getAttribute('alias')
  if (!alias || alias.trim().length === 0) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: missing "alias"`
  }

  const type = mibObj.getAttribute('type')
  if (!type || type.trim().length === 0) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: missing "type"`
  }

  const typeLower = type.toLowerCase()
  if (!VALID_MIB_OBJ_TYPES.includes(typeLower)) {
    return `Group "${groupName}" mibObj ${mibObjNumber}: invalid type "${type}"`
  }

  return ''
}

export const validateSystemDefElement = (
  systemDef: Element,
  systemDefNumber: number
): string => {
  if (!systemDef || typeof systemDef.querySelector !== 'function') {
    return `SystemDef ${systemDefNumber}: invalid element`
  }

  const getInnerText = (el: Element, tag: string): string => {
    if (!el) {
      return ''
    }
    let node: any = null
    try {
      node = el.querySelector(tag) 
    } catch (e) {
      if (e instanceof Error) throw e
      node = null
    }
    if (!node) {
      try {
        node = el.getElementsByTagName(tag)[0]
      } catch (e) {
        if (e instanceof Error) throw e
        node = null
      }
    }
    return node?.textContent?.trim() || ''
  }

  const name = systemDef.getAttribute('name')
  if (!name || name.trim().length === 0) {
    return `SystemDef ${systemDefNumber}: missing "name" attribute`
  }

  const sysoid = getInnerText(systemDef, 'sysoid')
  const sysoidMask = getInnerText(systemDef, 'sysoidMask')

  if (!sysoid && !sysoidMask) {
    return `SystemDef "${name}": missing <sysoid> or <sysoidMask>`
  }

  if (sysoid && !/^\.?\d+(\.\d+)*$/.test(sysoid)) {
    return `SystemDef "${name}": invalid <sysoid> format "${sysoid}"`
  }

  if (sysoidMask && !/^\.?\d+(\.\d+)*\.?$/.test(sysoidMask)) {
    return `SystemDef "${name}": invalid <sysoidMask> format "${sysoidMask}"`
  }

  const collect = systemDef.querySelector('collect')
  if (!collect) {
    return `SystemDef "${name}": missing <collect>`
  }

  // Empty <collect/> is valid - means no data collection groups for this device
  const includeGroups = collect.querySelectorAll('includeGroup')
  const includeGroupList = Array.from(includeGroups) as Element[]
  for (const [idx, includeGroup] of includeGroupList.entries()) {
    const includeGroupValue = includeGroup.textContent?.trim()
    if (!includeGroupValue) {
      return `SystemDef "${name}": <includeGroup> ${idx + 1} is empty`
    }
  }

  return ''
}

export const isDuplicateFile = (fileName: string, existingFiles: UploadSnmpDataCollectionFileType[]): boolean => {
  return !!existingFiles?.some((element) => element.file.name.toLowerCase() === fileName.toLowerCase())
}

