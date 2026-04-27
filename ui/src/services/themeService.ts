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

export type Theme = 'open-light' | 'open-dark'

export const LIGHT_THEME: Theme = 'open-light'
export const DARK_THEME: Theme = 'open-dark'
export const DEFAULT_THEME: Theme = LIGHT_THEME

const THEME_STORAGE_KEY = 'theme'

const isTheme = (value: unknown): value is Theme =>
  value === LIGHT_THEME || value === DARK_THEME

export const loadTheme = (): Theme => {
  const value = localStorage.getItem(THEME_STORAGE_KEY)
  return isTheme(value) ? value : DEFAULT_THEME
}

export const saveTheme = (theme: Theme): void => {
  localStorage.setItem(THEME_STORAGE_KEY, theme)
}

export const applyThemeClass = (theme: Theme): void => {
  const body = document.body
  body.classList.remove(LIGHT_THEME, DARK_THEME)
  body.classList.add(theme)
}
