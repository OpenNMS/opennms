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

import { ref } from 'vue'
import { defineStore } from 'pinia'
import API from '@/services'
import { MainMenu, NotificationSummary } from '@/types/mainMenu'
// import { defaultMainMenu, defaultNotificationSummary } from './fakeMenuData'

// Set this to true to use local/fake data instead of making API call
// const useFakeMenuData = false
// const useFakeUserNotificationData = false

export const useMenuStore = defineStore('menuStore', () => {
  const mainMenu = ref({} as MainMenu)
  const notificationSummary = ref({} as NotificationSummary)

  const getMainMenu = async () => {
    // // for using local data for dev/debugging purposes
    // if (useFakeMenuData) {
    //   mainMenu.value = defaultMainMenu
    //   return
    // }

    const resp = await API.getMainMenu()

    if (resp) {
      mainMenu.value = resp as MainMenu
    }
  }

  const getNotificationSummary = async () => {
    // // for using local data for dev/debugging purposes
    // if (useFakeUserNotificationData) {
    //   notificationSummary.value = defaultNotificationSummary
    //   return
    // }

    const resp = await API.getNotificationSummary()

    if (resp) {
      notificationSummary.value = resp as NotificationSummary
    }
  }

  return {
    mainMenu,
    notificationSummary,
    getMainMenu,
    getNotificationSummary
  }
})

