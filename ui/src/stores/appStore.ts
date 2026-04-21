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
import {
  applyThemeClass,
  DARK_THEME,
  LIGHT_THEME,
  loadTheme,
  saveTheme,
  Theme
} from '@/services/themeService'

export const useAppStore = defineStore('appStore', () => {
  const theme = ref<Theme>(loadTheme())

  const setTheme = (newTheme: Theme) => {
    theme.value = newTheme
    saveTheme(newTheme)
    applyThemeClass(newTheme)
  }

  const initTheme = () => {
    applyThemeClass(theme.value)
  }

  const toggleTheme = () => {
    setTheme(theme.value === LIGHT_THEME ? DARK_THEME : LIGHT_THEME)
  }

  return {
    theme,
    setTheme,
    initTheme,
    toggleTheme
  }
})
