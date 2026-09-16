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
import { AdminCategory } from '@/types/categoryAdmin'
import { createFailureResult, createSuccessResponse, ValidationResult } from '@/types/validation'
import { rest } from './axiosInstances'

// Uses the existing v1 /rest/categories service for category CRUD and its
// /rest/categories/{name}/nodes/{nodeId} sub-resource for membership. Member
// nodes are read from the v2 node search (`_s=category.name==NAME`), the only
// place that resolves category membership; the bare ?category= param does not.

const endpoint = '/categories'

// Only surface a server detail if it looks like a short, plain message — a 500
// often returns a servlet HTML error page, which must not be shown verbatim.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  if (typeof detail === 'string') {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 200 && !/[<>]/.test(trimmed)) {
      return trimmed
    }
  }
  return fallback
}

// null on failure (not []) so callers can keep showing the previous list
const listCategories = async (): Promise<AdminCategory[] | null> => {
  try {
    const resp = await rest.get(endpoint)
    if (resp.status === 204) {
      return []
    }
    const raw = resp.data?.category ?? []
    return Array.isArray(raw) ? raw : [raw]
  } catch (err) {
    console.error('Error loading surveillance categories:', err)
    return null
  }
}

const createCategory = async (category: AdminCategory): Promise<ValidationResult> => {
  try {
    await rest.post(endpoint, category)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error creating category:', err)
    return createFailureResult(errorMessage(err, `Failed to create category '${category.name}'.`))
  }
}

// v1 category update is a form-urlencoded bean-property update (used for the
// description; name is the immutable id).
const updateCategoryDescription = async (name: string, description: string): Promise<ValidationResult> => {
  try {
    const body = new URLSearchParams()
    body.set('description', description)
    await rest.put(`${endpoint}/${encodeURIComponent(name)}`, body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error updating category:', err)
    return createFailureResult(errorMessage(err, `Failed to update category '${name}'.`))
  }
}

const deleteCategory = async (name: string): Promise<ValidationResult> => {
  try {
    await rest.delete(`${endpoint}/${encodeURIComponent(name)}`)
    return createSuccessResponse()
  } catch (err: any) {
    // already gone — the desired end-state holds, so treat it as success
    if (err?.response?.status === 404) {
      return createSuccessResponse()
    }
    console.error('Error deleting category:', err)
    return createFailureResult(errorMessage(err, `Failed to delete category '${name}'.`))
  }
}

const addNodeToCategory = async (name: string, nodeId: number): Promise<boolean> => {
  try {
    await rest.put(`${endpoint}/${encodeURIComponent(name)}/nodes/${nodeId}`)
    return true
  } catch (err) {
    console.error('Error adding node to category:', err)
    return false
  }
}

const removeNodeFromCategory = async (name: string, nodeId: number): Promise<boolean> => {
  try {
    await rest.delete(`${endpoint}/${encodeURIComponent(name)}/nodes/${nodeId}`)
    return true
  } catch (err) {
    console.error('Error removing node from category:', err)
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
