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
import { loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'

// this file is ESM with no __dirname; derive it from import.meta.url instead
const __dirname = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig(({ mode }) => {
  // loadEnv, not dotenv: it reads the same file set Vite gives app code for
  // import.meta.env (.env, .env.local, .env.[mode], .env.[mode].local, real
  // process.env winning), so VITE_APP_LOGO_NAME below can't disagree with
  // what the app itself sees. '' = no VITE_ prefix filter; __dirname matches
  // envDir below.
  const env = loadEnv(mode, __dirname, '')

  return {
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
        './src/assets/ProductLogo.vue': fileURLToPath(new URL(`./src/assets/${env.VITE_APP_LOGO_NAME}.vue`, import.meta.url))
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
  }
})
