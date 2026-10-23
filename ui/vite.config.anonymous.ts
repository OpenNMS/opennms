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

import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'

// Builds ui-components/assets/anonymous-pages.css: the base styles of the menu bundle for the JSP
// pages that users see without login (see src/menu/anonymous-pages.scss). A separate build, not a
// second input of vite.config.menu.ts: there, Rollup could move the shared stylesheets into a
// common chunk, and index.css would then lose them. Runs after the menu build, into the same
// dist-menu directory, so it must not empty it.
export default defineConfig({
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern',
        silenceDeprecations: ['color-functions', 'global-builtin', 'legacy-js-api', 'import']
      }
    }
  },
  resolve: {
    alias: {
      '@/': fileURLToPath(new URL('./src/', import.meta.url))
    }
  },
  root: './src/menu',
  build: {
    emptyOutDir: false,
    outDir: './dist-menu',
    copyPublicDir: false,
    rollupOptions: {
      input: {
        'anonymous-pages': fileURLToPath(new URL('./src/menu/anonymous-pages.scss', import.meta.url))
      },
      output: {
        entryFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  }
})
