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
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'
// for process.env.VITE_APP_LOGO_NAME in resolve.alias; .env.local listed
// first so it wins, matching Vite's own env-file precedence
import dotenv from 'dotenv'
dotenv.config({ path: ['.env.local', '.env'] })

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
      // fileURLToPath, not URL#pathname: pathname percent-encodes (a checkout
      // path containing a space becomes %20 and fails to resolve)
      '@/': fileURLToPath(new URL('./src/', import.meta.url)),
      // Absolute path required, matching vite.config.ts: a relative
      // replacement resolves against the IMPORTER's directory in dev-mode
      // import analysis, while production builds resolve it against the
      // project root — absolute works identically in both.
      './src/assets/ProductLogo.vue': fileURLToPath(new URL(`./src/assets/${process.env.VITE_APP_LOGO_NAME}.vue`, import.meta.url))
    },
    dedupe: ['vue', 'primevue']
  },
  plugins: [
    vue(),
    svgLoader()
  ],
  root: './src/menu',
  // make sure we get environment variables from .env files in the main ui directory
  // path is relative to 'root' defined just above
  envDir: '../..',
  build: {
    emptyOutDir: true,
    outDir: './dist-menu',
    target: 'esnext',
    copyPublicDir: false,
    rollupOptions: {
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  }
})
