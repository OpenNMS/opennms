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

import { defineStore } from 'pinia'
import API from '@/services'
import { SCVCredentials } from '@/types/scv'

export const useScvStore = defineStore('scvStore', () => {
  const aliases = ref([] as string[])
  const credentials = ref({
    alias: '',
    username: '',
    password: '',
    attributes: {}
  } as SCVCredentials)

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

  const addCredentials = async () => {
    const success = await API.addCredentials(credentials.value)

    if (success) {
      clearCredentials()
      getAliases()
    }
  }

  const updateCredentials = async () => {
    const success = await API.updateCredentials(credentials.value)

    if (success) {
      clearCredentials()
    }
  }

  const setValue = (keyVal: Record<string, string>) => {
    credentials.value = { ...credentials.value, ...keyVal }
  }

  const clearCredentials = async () => {
    const creds = {
      id: undefined,
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
    credentials.value.attributes = { ...credentials.value.attributes, ...{ '': '' } } // adds empty key/val inputs in form
  }

  const updateAttribute = (attribute: { key: string; keyVal: { key: string; value: string } }) => {
    // TODO: Do we need to replace entire credential.values object, or can we just modify credentials.value.attributes?
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
    aliases,
    credentials,
    dbCredentials,
    isEditing,
    getAliases,
    getCredentialsByAlias,
    addCredentials,
    updateCredentials,
    setValue,
    clearCredentials,
    addAttribute,
    updateAttribute,
    removeAttribute
  }
})
