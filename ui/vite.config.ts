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

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'
import AutoImport from 'unplugin-auto-import/vite'

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
      '@/': new URL('./src/', import.meta.url).pathname,
      '~@featherds': '@featherds'
    },
    dedupe: ['vue']
  },
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) => tag.includes('rapi-doc')
        }
      }
    }),
    svgLoader(),

    // https://github.com/antfu/unplugin-auto-import
    AutoImport({
      imports: ['vue', 'vue-router', '@vueuse/core'],
      eslintrc: {
        enabled: true,
        filepath: './.eslintrc-auto-import.json'
      }
    })
  ],
  define: {
    'process.env': process.env
  },
  test: {
    dir: './tests',
    globals: true,
    environment: 'happy-dom',
    css: {
      include: /.+/
    },
    server: {
      deps: {
        // prevents this issue. Note deps.inline is deprecated, but unclear what the new configuration would be
        // https://github.com/vitest-dev/vitest/issues/3862
        inline: [
          /@featherds\/\w+/
        ]
      }
    }
  },
  root: './src/main',
  build: {
    emptyOutDir: true,
    outDir: './dist',
    target: 'esnext'
  }
})
