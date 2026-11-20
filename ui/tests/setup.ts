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

// Global test setup, run before every test file (vite.config.ts `test.setupFiles`).

import { config } from '@vue/test-utils'
import { afterEach } from 'vitest'
// Import the directive module itself, not the `@opennms/onms-ui` barrel: the
// barrel pulls in every component and PrimeVue service before a test file's
// `vi.mock` calls run, so a mock of e.g. `primevue/toasteventbus` would miss.
import OnmsTooltip from '../packages/onms-ui/src/directives/OnmsTooltip'

// The app registers `v-onms-tooltip` globally in src/theme/primevue-setup.ts,
// so components use it without a local import. Register it the same way for
// every mount, or each use logs "Failed to resolve directive: onms-tooltip".
config.global.directives = {
  ...config.global.directives,
  'onms-tooltip': OnmsTooltip
}

// Fail any test that triggers a Vue or Vue Router warning. Each one points at a
// real problem (an unregistered component or directive, a wrong prop type, a
// composable used outside a component, ...), and passing tests must not print
// anything. The warning is still printed, so the failing test shows what it was.
//
// This wraps console.warn rather than setting `config.global.config.warnHandler`,
// because Vue sends warnings raised outside a mounted app (e.g. onScopeDispose
// with no active scope) straight to console.warn. It is a plain assignment, not
// vi.spyOn, so vi.restoreAllMocks() in a test file doesn't remove it. A test
// that expects a warning can still silence it with
// vi.spyOn(console, 'warn').mockImplementation(...), which replaces this wrapper
// for that test.
const VUE_WARNING = /^\[Vue( Router)? warn\]/
const vueWarnings: string[] = []
const originalWarn = console.warn

console.warn = (...args: unknown[]) => {
  if (typeof args[0] === 'string' && VUE_WARNING.test(args[0])) {
    vueWarnings.push(args[0])
  }
  originalWarn(...args)
}

afterEach(() => {
  if (vueWarnings.length > 0) {
    const warnings = vueWarnings.splice(0)
    throw new Error(`Test triggered ${warnings.length} Vue warning(s); fix the cause (see stderr above):\n${warnings.join('\n')}`)
  }
})
