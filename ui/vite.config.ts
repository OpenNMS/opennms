import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader'
import AutoImport from 'unplugin-auto-import/vite'

export default defineConfig({
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
  build: {
    target: 'esnext'
  }
})
