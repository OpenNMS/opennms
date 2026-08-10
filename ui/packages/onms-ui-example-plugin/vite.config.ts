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

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import externalGlobals from 'rollup-plugin-external-globals'

// Builds a real OpenNMS UI plugin module: an ES module whose framework
// imports resolve from the HOST page's window globals (set in
// ui/src/main/main.ts) instead of being bundled. This mirrors how production
// plugins (e.g. opennms-servicenow-plugin) are built — the externals map
// below is the plugin-developer contract. Rollup's own `external` +
// `output.globals` cannot do this for ES-module output (globals only apply
// to umd/iife), hence the plugin.
export default defineConfig({
  plugins: [
    vue()
  ],
  build: {
    rollupOptions: {
      external: ['vue', 'pinia', 'vue-router', '@opennms/onms-ui'],
      plugins: [
        externalGlobals({
          vue: 'window.Vue',
          pinia: 'window.Pinia',
          'vue-router': 'window.VueRouter',
          '@opennms/onms-ui': 'window.OnmsUI'
        })
      ]
    },
    lib: {
      entry: 'src/main.ts',
      formats: ['es'],
      fileName: () => 'exampleUiExtension.es.js'
    },
    outDir: 'dist',
    emptyOutDir: true
  }
})
