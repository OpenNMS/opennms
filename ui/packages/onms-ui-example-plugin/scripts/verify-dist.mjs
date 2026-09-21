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

// Asserts the built module actually honors the externals contract.
// `vite build` exits 0 even when rollup-plugin-external-globals silently
// skips a file (its transform bails if this.parse() fails, or if no globals
// key appears as a substring of the code). Because the externalized ids stay
// external either way, a bail leaves a bare `import ... from "vue"` in the
// ES output that only fails later, at runtime in the host browser — this
// script turns that into a build-time failure.

import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const distFile = fileURLToPath(new URL('../dist/exampleUiExtension.es.js', import.meta.url))

// Must match the externals map in vite.config.ts (and the window globals the
// host page sets in ui/src/main/main.ts).
const EXTERNALS = ['vue', 'pinia', 'vue-router', '@opennms/onms-ui']

// Globals this example's code imports, so they MUST appear rewritten in the
// output. (Not all of EXTERNALS: the example does not use pinia/vue-router.)
const REQUIRED_GLOBALS = ['window.Vue', 'window.OnmsUI']

let code
try {
  code = readFileSync(distFile, 'utf8')
} catch {
  console.error(`verify-dist: cannot read ${distFile} — run the build first`)
  process.exit(1)
}

const errors = []

for (const id of EXTERNALS) {
  const escaped = id.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  // Catches `from "vue"` / `from"vue"` (static and re-export),
  // `import "vue"` (side-effect) and `import("vue")` (dynamic).
  const residualImport = new RegExp(`\\b(?:from|import)\\s*\\(?\\s*["']${escaped}["']`)
  if (residualImport.test(code)) {
    errors.push(`residual import of "${id}" — externalization did not happen (rollup-plugin-external-globals bailed?)`)
  }
}

for (const global of REQUIRED_GLOBALS) {
  if (!code.includes(global)) {
    errors.push(`expected "${global}" in the output — imports were not rewritten to host window globals`)
  }
}

if (errors.length > 0) {
  console.error(`verify-dist: ${distFile} violates the plugin externals contract:`)
  for (const error of errors) {
    console.error(`  - ${error}`)
  }
  process.exit(1)
}

console.log(`verify-dist: OK — no residual framework imports; ${REQUIRED_GLOBALS.join(', ')} present`)
