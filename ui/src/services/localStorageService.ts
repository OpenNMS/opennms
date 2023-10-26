import { NodePreferences, OpenNmsPreferences } from '@/types'

const OPENNMS_PREFERENCES_STORAGE_KEY = 'opennms-preferences'

const defaultPreferences = () => {
  return {
    nodePreferences: {
      nodeColumns: []
    }
  } as OpenNmsPreferences
}

export const savePreferences = (data: OpenNmsPreferences) => {
  localStorage.setItem(OPENNMS_PREFERENCES_STORAGE_KEY, JSON.stringify(data, getCircularReplacer()))
}

export const loadPreferences = (): OpenNmsPreferences | null => {
  const json = localStorage.getItem(OPENNMS_PREFERENCES_STORAGE_KEY)

  if (json) {
    const data = JSON.parse(json)
    
    if (data) {
      return data as OpenNmsPreferences
    }
  }

  return null
}

export const saveNodePreferences = (data: NodePreferences) => {
  const prefs = loadPreferences() || defaultPreferences()
  prefs.nodePreferences = data

  savePreferences(prefs)
}

export const loadNodePreferences = (): NodePreferences | null => {
  const prefs = loadPreferences() || defaultPreferences()

  return prefs.nodePreferences
}

const getCircularReplacer = () => {
  const seen = new WeakSet()

  return (key: any, value: any) => {
    if (typeof value === 'object' && value !== null) {
      if (seen.has(value)) {
        return
      }
      seen.add(value)
    }
    return value
  }
}
