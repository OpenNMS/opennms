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

import API from '@/services'
import { AdminCategory } from '@/services/categoryAdminService'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useCategoryAdminStore = defineStore('categoryAdminStore', () => {
  const categories = ref([] as AdminCategory[])
  const loadError = ref(false)

  const getCategories = async () => {
    const result = await API.listCategories()
    if (result !== null) {
      categories.value = result
      loadError.value = false
    } else {
      loadError.value = true
    }
  }

  const createCategory = async (category: AdminCategory) => {
    const error = await API.createCategory(category)
    if (error === null) {
      await getCategories()
    }
    return error
  }

  const updateCategoryDescription = async (name: string, description: string) => {
    const error = await API.updateCategoryDescription(name, description)
    if (error === null) {
      await getCategories()
    }
    return error
  }

  const deleteCategory = async (name: string) => {
    const error = await API.deleteCategory(name)
    if (error === null) {
      await getCategories()
    }
    return error
  }

  return { categories, loadError, getCategories, createCategory, updateCategoryDescription, deleteCategory }
})
