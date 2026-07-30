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

import { existsSync, readFileSync, statSync } from 'node:fs'
import { extname, resolve, sep } from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'
import type { PluginOption } from 'vite'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'
// for process.env.VITE_APP_LOGO_NAME in resolve.alias
import dotenv from 'dotenv'
dotenv.config()

// this file is ESM with no __dirname; derive it from import.meta.url instead
const __dirname = fileURLToPath(new URL('.', import.meta.url))

// Serves a built plugin module during `pnpm dev` only (NMS-20054).
// apply: 'serve' means this cannot exist in production builds. URL shape is
// load-bearing: externalComponent() derives the window-global key from the
// second-to-last path segment (the extensionId).
const PLUGIN_DEV_CONTENT_TYPES: Record<string, string> = {
  '.js': 'text/javascript',
  '.css': 'text/css'
}

const pluginDevServer = (extensionId: string, distDir: string): PluginOption => {
  return {
    name: `onms-plugin-dev-server-${extensionId}`,
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use(`/plugin-modules/${extensionId}`, (req, res, next) => {
        const fileName = (req.url ?? '').split('?')[0].replace(/^\//, '')
        const file = resolve(distDir, fileName)
        // Containment check prevents ../ traversal attacks (connect does not normalize mount-prefix match)
        if (!fileName || !file.startsWith(distDir + sep) || !existsSync(file) || !statSync(file).isFile()) {
          return next()
        }
        res.setHeader('Content-Type', PLUGIN_DEV_CONTENT_TYPES[extname(fileName)] ?? 'application/octet-stream')
        res.end(readFileSync(file))
      })
    }
  }
}

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
      // Absolute path required: a relative replacement resolves against the
      // IMPORTER's directory in dev-mode import analysis (breaking `pnpm dev`
      // with "Failed to resolve import ./src/assets/ProductLogo.vue"), while
      // production builds resolved it against the project root — absolute
      // works identically in both.
      './src/assets/ProductLogo.vue': new URL(`./src/assets/${process.env.VITE_APP_LOGO_NAME}.vue`, import.meta.url).pathname
    },
    dedupe: ['vue', 'primevue']
  },
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: tag => tag.includes('rapi-doc')
        }
      }
    }),
    svgLoader(),
    pluginDevServer('exampleUiExtension', resolve(__dirname, 'packages/onms-ui-example-plugin/dist')),
    // Dev harness for opennms-servicenow-plugin (MPLUG-100): point
    // VITE_SERVICENOW_PLUGIN_DIST at that repo's built UI, e.g.
    // /path/to/opennms-servicenow-plugin/plugin/src/main/ui/dist
    // (dotenv is loaded above, so .env.local works).
    ...(process.env.VITE_SERVICENOW_PLUGIN_DIST
      ? [pluginDevServer('serviceNowUiExtension', resolve(process.env.VITE_SERVICENOW_PLUGIN_DIST))]
      : [])
  ],
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
          /primevue/
        ]
      }
    }
  },
  root: './src/main',
  // make sure we get environment variables from .env files in the main ui directory
  // path is relative to 'root' defined just above
  envDir: '../..',
  build: {
    emptyOutDir: true,
    outDir: './dist',
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
