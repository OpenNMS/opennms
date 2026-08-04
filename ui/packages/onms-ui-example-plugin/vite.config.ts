import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { viteExternalsPlugin } from 'vite-plugin-externals'

// Builds a real OpenNMS UI plugin module: an ES module whose framework
// imports resolve from the HOST page's window globals (set in
// ui/src/main/main.ts) instead of being bundled. This mirrors how production
// plugins (e.g. opennms-servicenow-plugin) are built — the externals map
// below is the plugin-developer contract.
export default defineConfig({
  plugins: [
    vue(),
    viteExternalsPlugin({
      vue: 'Vue',
      pinia: 'Pinia',
      'vue-router': 'VueRouter',
      '@opennms/onms-ui': 'OnmsUI'
    })
  ],
  build: {
    lib: {
      entry: 'src/main.ts',
      formats: ['es'],
      fileName: () => 'exampleUiExtension.es.js'
    },
    outDir: 'dist',
    emptyOutDir: true
  }
})
