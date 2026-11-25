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

import { vi } from 'vitest'

// The prefix of every Vue and Vue Router warning. tests/setup.ts fails a test
// that logs one.
export const VUE_WARNING = /^\[Vue( Router)? warn\]/

export const isVueWarning = (message: unknown): boolean =>
  typeof message === 'string' && VUE_WARNING.test(message)

// Mute an expected console.warn from app code, and keep the Vue-warning check on.
// A bare vi.spyOn(console, 'warn').mockImplementation(() => {}) replaces the
// check's wrapper and would swallow every Vue warning in the test as well. This
// spy forwards Vue warnings to that wrapper, so they still print and fail the
// test. The returned spy records every call, for `toHaveBeenCalled` assertions;
// the file's afterEach(vi.restoreAllMocks) removes it.
export const muteAppWarnings = () => {
  const checked = console.warn

  return vi.spyOn(console, 'warn').mockImplementation((...args: unknown[]) => {
    if (isVueWarning(args[0])) {
      checked(...args)
    }
  })
}
