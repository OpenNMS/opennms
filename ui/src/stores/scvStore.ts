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

import { SCV_GET_ALL_ALIAS } from '@/lib/constants'
import API from '@/services'
import { SCVCredentials, ScvSearchItem } from '@/types/scv'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useScvStore = defineStore('scvStore', () => {
  const aliases = ref([] as string[])
  const credentials = ref({
    alias: '',
    username: '',
    password: '',
    attributes: {}
  } as SCVCredentials)

  const allCredentials = ref([] as SCVCredentials[])

  // used to track changes
  const dbCredentials = ref({} as SCVCredentials)
  const isEditing = ref(false)

  const getAliases = async () => {
    const resp = await API.getAliases()
    aliases.value = resp
  }

  const getCredentialsByAlias = async (alias: string) => {
    const resp = await API.getCredentialsByAlias(alias)

    if (resp) {
      credentials.value = resp
      dbCredentials.value = resp
      isEditing.value = true
    }
  }

  const getAllCredentials = () => {
    return {
      ...allCredentials.value
    }
  }

  const populate = async () => {
    const resp = await API.getAllCredentials()

    if (resp) {
      allCredentials.value = resp
    }
  }

  const addCredentials = async () => {
    if (!credentials.value.alias) {
      throw new Error('Alias is required to add new credentials.')
    }

    if (credentials.value.alias.toLowerCase() === SCV_GET_ALL_ALIAS) {
      throw new Error(`The alias "${SCV_GET_ALL_ALIAS}" is reserved and cannot be used.`)
    }

    const success = await API.addCredentials(credentials.value)

    if (success) {
      clearCredentials()
      getAliases()
    }
  }

  const updateCredentials = async () => {
    if (!credentials.value.alias) {
      throw new Error('Alias is required to add new credentials.')
    }

    if (credentials.value.alias.toLowerCase() === SCV_GET_ALL_ALIAS) {
      throw new Error(`The alias "${SCV_GET_ALL_ALIAS}" is reserved and cannot be used.`)
    }

    const success = await API.updateCredentials(credentials.value)

    if (success) {
      clearCredentials()
    }
  }

  const createScvSearchItem = (cred: SCVCredentials, key: string, type: 'alias' | 'key'): ScvSearchItem => {
    return {
      alias: cred.alias,
      key: key,
      type: type
    }
  }

  /**
   * Returns a sorted list of ScvSearchItems grouped by alias which matches the query.
   * Match by alias returns the alias items, as well as all keys for the aliases.
   * Match by key returns the parent alias, then only the matching keys.
   * If query is empty, returns all aliases and keys.
   */
  const queryCredentials = (query: string) => {
    const items = [] as ScvSearchItem[]

    const sortedByAlias = [...allCredentials.value].sort((a, b) => a.alias.localeCompare(b.alias))
    const displayAll = !query

    sortedByAlias.forEach((cred) => {
      let aliasPushed = false
      let aliasMatched = false

      if (displayAll || cred.alias.toLowerCase().includes(query.toLowerCase())) {
        items.push(createScvSearchItem(cred, cred.alias, 'alias'))
        aliasPushed = true
        aliasMatched = true
      }

      const keys = ['username', 'password', ...Object.keys(cred.attributes)]

      keys.forEach((key) => {
        if (displayAll || aliasMatched || key.toLowerCase().includes(query.toLowerCase())) {
          if (!aliasPushed) {
            items.push(createScvSearchItem(cred, cred.alias, 'alias'))
            aliasPushed = true
          }

          items.push(createScvSearchItem(cred, key, 'key'))
        }
      })
    })

    return items
  }

  const setValue = (keyVal: Record<string, string>) => {
    credentials.value = { ...credentials.value, ...keyVal }
  }

  const clearCredentials = async () => {
    const creds = {
      alias: '',
      username: '',
      password: '',
      attributes: {}
    } as SCVCredentials

    credentials.value = creds
    dbCredentials.value = creds
    isEditing.value = false
  }

  const addAttribute = () => {
    credentials.value.attributes = { ...credentials.value.attributes, ...{ '': '' }} // adds empty key/val inputs in form
  }

  const updateAttribute = (attribute: { key: string; keyVal: { key: string; value: string }}) => {
    const attributes = { ...credentials.value.attributes }

    // updating the value
    if (attribute.key === attribute.keyVal.key) {
      attributes[attribute.key] = attribute.keyVal.value
      credentials.value.attributes = attributes
      return
    }

    // else remove and replace the key
    delete attributes[attribute.key]
    attributes[attribute.keyVal.key] = attribute.keyVal.value
    credentials.value.attributes = attributes
  }

  const removeAttribute = (key: string) => {
    const attributes = { ...credentials.value.attributes }
    delete attributes[key]
    credentials.value.attributes = attributes
  }

  return {
    addAttribute,
    addCredentials,
    aliases,
    clearCredentials,
    credentials,
    dbCredentials,
    getAliases,
    getAllCredentials,
    getCredentialsByAlias,
    isEditing,
    populate,
    queryCredentials,
    removeAttribute,
    setValue,
    updateAttribute,
    updateCredentials
  }
})
