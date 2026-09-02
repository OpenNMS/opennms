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
import { rest } from './axiosInstances'

// PrimeVue Manage Surveillance Categories (NMS-20131). Reuses existing REST —
// no backend change: category CRUD via the v1 /rest/categories service, and
// node membership via its granular /rest/categories/{name}/nodes/{nodeId}
// sub-resource. Member nodes are read from the v2 node search (the only place
// that resolves category membership: `_s=category.name==NAME`; the bare
// ?category= param does NOT filter).

export interface AdminCategory {
  id?: number
  name: string
  description?: string | null
  authorizedGroups?: string[]
}

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/categories'

// Only surface a server detail if it looks like a short, plain message — a 500
// often returns a servlet HTML error page, which must not be shown verbatim.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  if (typeof detail === 'string') {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 200 && !/[<>]/.test(trimmed)) return trimmed
  }
  return fallback
}

const listCategories = async (): Promise<AdminCategory[] | null> => {
  try {
    startSpinner()
    const resp = await rest.get(endpoint)
    if (resp.status === 204) {
      return []
    }
    const raw = resp.data?.category ?? []
    return Array.isArray(raw) ? raw : [raw]
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load surveillance categories.' })
    return null
  } finally {
    stopSpinner()
  }
}

const createCategory = async (category: AdminCategory): Promise<string | null> => {
  try {
    startSpinner()
    await rest.post(endpoint, category)
    showSnackBar({ msg: `Category '${category.name}' created.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to create category '${category.name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

// v1 category update is form-urlencoded bean-property update (used for the
// description; name is the immutable id).
const updateCategoryDescription = async (name: string, description: string): Promise<string | null> => {
  try {
    startSpinner()
    const body = new URLSearchParams()
    body.set('description', description)
    await rest.put(`${endpoint}/${encodeURIComponent(name)}`, body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    showSnackBar({ msg: `Category '${name}' updated.` })
    return null
  } catch (err: any) {
    const msg = errorMessage(err, `Failed to update category '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const deleteCategory = async (name: string): Promise<string | null> => {
  try {
    startSpinner()
    await rest.delete(`${endpoint}/${encodeURIComponent(name)}`)
    showSnackBar({ msg: `Category '${name}' deleted.` })
    return null
  } catch (err: any) {
    // already gone — the desired end-state holds, so treat it as success
    if (err?.response?.status === 404) {
      showSnackBar({ msg: `Category '${name}' deleted.` })
      return null
    }
    const msg = errorMessage(err, `Failed to delete category '${name}'.`)
    showSnackBar({ msg, error: true })
    return msg
  } finally {
    stopSpinner()
  }
}

const addNodeToCategory = async (name: string, nodeId: number): Promise<boolean> => {
  try {
    await rest.put(`${endpoint}/${encodeURIComponent(name)}/nodes/${nodeId}`)
    return true
  } catch (_err) {
    showSnackBar({ msg: `Failed to add node to '${name}'.`, error: true })
    return false
  }
}

const removeNodeFromCategory = async (name: string, nodeId: number): Promise<boolean> => {
  try {
    await rest.delete(`${endpoint}/${encodeURIComponent(name)}/nodes/${nodeId}`)
    return true
  } catch (_err) {
    showSnackBar({ msg: `Failed to remove node from '${name}'.`, error: true })
    return false
  }
}

export {
  addNodeToCategory,
  createCategory,
  deleteCategory,
  listCategories,
  removeNodeFromCategory,
  updateCategoryDescription
}
