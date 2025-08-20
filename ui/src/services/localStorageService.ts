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

import { NodePreferences, OpenNmsPreferences } from '@/types'

const OPENNMS_PREFERENCES_STORAGE_KEY = 'opennms-preferences'

const defaultPreferences = () => {
  return {
    nodePreferences: {
      nodeColumns: []
    },
    isSideMenuExpanded: false
  } as OpenNmsPreferences
}

const savePreferences = (data: OpenNmsPreferences) => {
  localStorage.setItem(OPENNMS_PREFERENCES_STORAGE_KEY, JSON.stringify(data, getCircularReplacer()))
}

const loadPreferences = (): OpenNmsPreferences | null => {
  const json = localStorage.getItem(OPENNMS_PREFERENCES_STORAGE_KEY)

  if (json) {
    const data = JSON.parse(json)
    
    if (data) {
      return data as OpenNmsPreferences
    }
  }

  return null
}

const loadDefaultPreferences = () => {
  return defaultPreferences()
}

const saveNodePreferences = (data: NodePreferences) => {
  const prefs = loadPreferences() || defaultPreferences()
  prefs.nodePreferences = data
  if (prefs.nodePreferences.nodeFilter) {
    prefs.nodePreferences.nodeFilter.searchTerm = ''
  }
  savePreferences(prefs)
}

const loadNodePreferences = (): NodePreferences | null => {
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

export {
  loadDefaultPreferences,
  loadNodePreferences,
  loadPreferences,
  saveNodePreferences,
  savePreferences
}
