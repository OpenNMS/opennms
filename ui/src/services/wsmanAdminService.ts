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

import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { WsmanConfig, WsmanConfigInput, WsmanDataCollection, WsmanDataCollectionFileInput, WsmanStatus, WsmanSyncResult } from '@/types/wsmanAdmin'
import { rest, v2 } from './axiosInstances'

// Manage WS-Man (NMS-20286): wsman-config.xml through /api/v2/wsman-config.

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/wsman-config'

// null on failure (not an empty config) so the page can show an error state
const getWsmanConfig = async (): Promise<WsmanConfig | null> => {
  try {
    startSpinner()
    const resp = await v2.get(endpoint, { headers: { Accept: 'application/json' }})
    const data = resp.data
    if (!data || !data.defaults || typeof data.version !== 'string') {
      return null
    }
    return {
      defaults: data.defaults,
      definitions: Array.isArray(data.definitions) ? data.definitions : [],
      version: data.version
    }
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load the WS-Man configuration.', error: true })
    return null
  } finally {
    stopSpinner()
  }
}

// Only surface a server detail if it looks like a short, plain message — a 500
// often returns a servlet HTML error page, which must not be shown verbatim.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  if (typeof detail === 'string') {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 300 && !/[<>]/.test(trimmed)) {
      return trimmed
    }
  }
  return fallback
}

// Whole-document replace; resolves to null on success or the reason to show
// in the dialog. No toast here: the dialog owns the error surface.
const updateWsmanConfig = async (input: WsmanConfigInput): Promise<string | null> => {
  try {
    startSpinner()
    await v2.put(endpoint, input, { headers: { Accept: 'application/json' }})
    return null
  } catch (err: any) {
    return errorMessage(err, 'Failed to save the WS-Man configuration.')
  } finally {
    stopSpinner()
  }
}

const asList = <T>(v: unknown): T[] => (Array.isArray(v) ? (v as T[]) : [])

// null on failure so the tab can show an error state
const getWsmanDataCollection = async (): Promise<WsmanDataCollection | null> => {
  try {
    startSpinner()
    const resp = await v2.get(`${endpoint}/data-collection`, { headers: { Accept: 'application/json' }})
    const data = resp.data
    if (!data || !Array.isArray(data.sources)) {
      return null
    }
    return {
      rrdRepository: data.rrdRepository ?? null,
      sources: data.sources,
      versions: data.versions ?? {},
      collections: asList(data.collections),
      groups: asList(data.groups),
      systemDefinitions: asList(data.systemDefinitions)
    }
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load the WS-Man data collection configuration.', error: true })
    return null
  } finally {
    stopSpinner()
  }
}

// Replaces one source file; null on success or the reason to show in the dialog.
const updateWsmanDataCollectionFile = async (file: string, input: WsmanDataCollectionFileInput): Promise<string | null> => {
  try {
    startSpinner()
    await v2.put(`${endpoint}/data-collection?file=${encodeURIComponent(file)}`, input, { headers: { Accept: 'application/json' }})
    return null
  } catch (err: any) {
    return errorMessage(err, 'Failed to save the WS-Man data collection file.')
  } finally {
    stopSpinner()
  }
}

// Poller-side view per definition; null on failure (the page then shows no
// counts rather than zeros, which would read as "no servers").
const getWsmanStatus = async (): Promise<WsmanStatus | null> => {
  try {
    const resp = await v2.get(`${endpoint}/status`, { headers: { Accept: 'application/json' }})
    const data = resp.data
    if (!data || !Array.isArray(data.definitions) || !data.defaults) {
      return null
    }
    return data as WsmanStatus
  } catch (_err) {
    return null
  }
}

// Provisions a definition's servers into its requisition; the result on
// success, else the reason to show.
const syncWsmanDefinition = async (index: number): Promise<WsmanSyncResult | string> => {
  try {
    startSpinner()
    const resp = await v2.post(`${endpoint}/definitions/${index}/sync`, null, { headers: { Accept: 'application/json' }})
    return resp.data as WsmanSyncResult
  } catch (err: any) {
    return errorMessage(err, 'Failed to sync the server definition with its requisition.')
  } finally {
    stopSpinner()
  }
}

// Requisition names for the definition dialog's picker; [] on failure (the
// field stays free text, so a missing list is not blocking)
const getRequisitionNames = async (): Promise<string[]> => {
  try {
    const resp = await rest.get('/requisitionNames', { headers: { Accept: 'application/json' }})
    const names = resp.data?.['foreign-source']
    return Array.isArray(names) ? names.map(String) : []
  } catch (_err) {
    return []
  }
}

export { getRequisitionNames, getWsmanConfig, getWsmanDataCollection, getWsmanStatus, syncWsmanDefinition, updateWsmanConfig, updateWsmanDataCollectionFile }
