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
import { rest, v2 } from './axiosInstances'

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

export interface CategoryNode {
  id: number
  label: string
  // set when the node comes from a requisition — category changes on such nodes
  // are overwritten on the next provisiond synchronization
  requisitioned?: boolean
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

// all nodes (id + label) as the candidate pool for the assignment picker
// null (not []) on failure so the caller can tell a failed fetch from an empty
// result — otherwise a transient error looks like "zero nodes" and can arm a
// destructive membership diff
const listAllNodes = async (): Promise<CategoryNode[] | null> => {
  try {
    const resp = await v2.get('/nodes?limit=0&orderBy=label')
    const raw = resp.data?.node ?? []
    return (Array.isArray(raw) ? raw : [raw]).map(toCategoryNode)
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load nodes.' })
    return null
  }
}

const toCategoryNode = (n: any): CategoryNode => ({
  id: Number(n.id),
  label: n.label ?? `Node ${n.id}`,
  requisitioned: !!n.foreignSource
})

// FIQL treats these as operators/grouping; a category name containing one can't
// be used in a category.name==NAME selector, so refuse rather than issue a
// malformed query that could return the wrong members (and arm a bad diff).
const FIQL_UNSAFE = /[;,()=<>!~]/

// nodes currently in a category — resolved via the node search property that
// actually filters by membership. null (not []) on failure — see listAllNodes.
const listCategoryNodes = async (name: string): Promise<CategoryNode[] | null> => {
  if (FIQL_UNSAFE.test(name)) {
    showSnackBar({ msg: `Category '${name}' has a name that can't be searched safely; rename it to manage its nodes.`, error: true })
    return null
  }
  try {
    const resp = await v2.get(`/nodes?limit=0&_s=${encodeURIComponent(`category.name==${name}`)}`)
    if (resp.status === 204) {
      return []
    }
    const raw = resp.data?.node ?? []
    return (Array.isArray(raw) ? raw : [raw]).map(toCategoryNode)
  } catch (_err) {
    showSnackBar({ msg: `Failed to load nodes for category '${name}'.` })
    return null
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
  listAllNodes,
  listCategories,
  listCategoryNodes,
  removeNodeFromCategory,
  updateCategoryDescription
}
